package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import model.Status;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.adapters.DurationAdapter;
import server.adapters.LocalDateTimeAdapter;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter()).create();
    HttpClient client;
    HttpRequest request;
    URI url;
    HttpResponse<String> response;
    JsonElement jsonElement;

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
        taskServer.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void getTasks() throws IOException, InterruptedException {
        // добавим две задачи в менеджер, запросим список задач через клиента и проверим
        Task task1 = new Task("Task 1", "Task 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        manager.addTask(task1);
        Task task2 = new Task("Task 2", "Task 2", Status.NEW, LocalDateTime.now().plusDays(1), Duration.ofMinutes(5));
        manager.addTask(task2);
        url = URI.create("http://localhost:8080/tasks");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "В теле ответа не массив JSON элементов!");
        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertEquals(manager.getTasks(), tasks, "Список задач не совпадает!");
    }

    @Test
    public void getTaskById() throws IOException, InterruptedException {
        // добавим в менеджер две задачи
        Task task1 = new Task("Task 1", "Task 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        manager.addTask(task1);
        Task task2 = new Task("Task 2", "Task 2", Status.NEW, LocalDateTime.now().plusDays(1), Duration.ofMinutes(5));
        manager.addTask(task2);

        // запросим задачу с ID = 1
        url = URI.create("http://localhost:8080/tasks/1");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject(), "В теле ответа не JSON элемент!");
        Task taskFromServer1 = gson.fromJson(response.body(), Task.class);
        assertEquals(task1, taskFromServer1, "Задачи не одинаковые!");
        assertEquals(200, response.statusCode());

        // запросим задачу с ID = 2
        url = URI.create("http://localhost:8080/tasks/2");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject(), "В теле ответа не JSON элемент!");
        Task taskFromServer2 = gson.fromJson(response.body(), Task.class);
        assertEquals(task2, taskFromServer2, "Задачи не одинаковые!");
        assertEquals(200, response.statusCode());

        // запросим задачу с несуществующим ID
        url = URI.create("http://localhost:8080/tasks/777");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());

        // запросим задачу с неправильным ID
        url = URI.create("http://localhost:8080/tasks/id303");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void wrongGetRequest() throws IOException, InterruptedException {
        // сформируем неправильный GET запрос, должна быть ошибка 404
        url = URI.create("http://localhost:8080/tasksssssssss");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void addTask() throws IOException, InterruptedException {
        // добавим через клиента одну задачу и проверим, появилась ли она в менеджере
        Task task1 = new Task("Task 1", "Task 1", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task1);
        url = URI.create("http://localhost:8080/tasks");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются!");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Task 1", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");

        // добавим вторую задачу и тоже проверим ее в менеджере
        Task task2 = new Task("Task 2", "Task 2", Status.NEW,
                LocalDateTime.now().minusDays(1), Duration.ofMinutes(5));
        taskJson = gson.toJson(task2);
        url = URI.create("http://localhost:8080/tasks");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются!");
        assertEquals(2, tasksFromManager.size(), "Некорректное количество задач!");
        assertEquals("Task 2", tasksFromManager.get(1).getName(), "Некорректное имя задачи!");

        // добавим третью задачи у которой пересекается время и проверим ее в менеджере
        Task task3 = new Task("Task 3", "Task 3", Status.NEW,
                LocalDateTime.now().minusDays(1), Duration.ofMinutes(5));
        taskJson = gson.toJson(task3);
        url = URI.create("http://localhost:8080/tasks");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Должна быть ошибка 406 - время пересекается!");
        tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются!");
        assertEquals(2, tasksFromManager.size(), "Задача не должна была быть добавлена!");
    }

    @Test
    public void updateTask() throws IOException, InterruptedException {
        // добавим несколько задач в менеджер
        Task task = new Task("Task 1", "Task 1", Status.NEW);
        manager.addTask(task);
        task = new Task("Task 2", "Task 2", Status.NEW);
        manager.addTask(task);
        task = new Task("Task 3", "Task 3", Status.NEW);
        manager.addTask(task);

        // обновим задачу с ID = 2 (установим ей время и сменим имя) и проверим
        task = new Task(2, "Task 2 updated", "Task 2", Status.NEW,
                LocalDateTime.of(2024, 8, 19, 21, 0), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);
        url = URI.create("http://localhost:8080/tasks");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются!");
        assertEquals(3, tasksFromManager.size(), "Некорректное количество задач!");
        assertEquals("Task 2 updated", tasksFromManager.get(1).getName(), "Не обновилось имя задачи!");
        assertEquals(LocalDateTime.of(2024, 8, 19, 21, 0), manager.getTaskById(2).getStartTime(),
                "Не обновилось время начала задачи!");
        assertEquals(Duration.ofMinutes(5), manager.getTaskById(2).getDuration(),
                "Не обновилось время выполнения задачи!");
    }

    @Test
    public void wrongPostRequest() throws IOException, InterruptedException {
        // сформируем неправильный POST запрос, должна быть ошибка 404
        url = URI.create("http://localhost:8080/posttttttttt");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("")).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void deleteTask() throws IOException, InterruptedException {
        // добавим несколько задач в менеджер
        Task task = new Task("Task 1", "Task 1", Status.NEW);
        manager.addTask(task);
        task = new Task("Task 2", "Task 2", Status.NEW);
        manager.addTask(task);
        task = new Task("Task 3", "Task 3", Status.NEW);
        manager.addTask(task);

        // удалим задачу с ID = 2 и проверим
        url = URI.create("http://localhost:8080/tasks/2");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются!");
        assertEquals(2, tasksFromManager.size(), "Некорректное количество задач!");
        assertTrue(tasksFromManager.stream().filter(t -> t.getId() == 2).findFirst().isEmpty(),
                "Задача не удалилась!");

        // попробуем удалить задачу с не существующим ID, в менеджере сейчас 2 задачи
        url = URI.create("http://localhost:8080/tasks/777");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        tasksFromManager = manager.getTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются!");
        assertEquals(2, tasksFromManager.size(), "Некорректное количество задач!");
        assertTrue(tasksFromManager.stream().filter(t -> t.getId() == 2).findFirst().isEmpty(),
                "Задача не удалилась!");
    }

    @Test
    public void wrongDeleteRequest() throws IOException, InterruptedException {
        // сформируем неправильный DELETE запрос, должна быть ошибка 404
        url = URI.create("http://localhost:8080/deleteeeeee");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("")).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

}