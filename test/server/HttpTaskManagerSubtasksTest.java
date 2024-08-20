package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Status;
import model.Subtask;
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

public class HttpTaskManagerSubtasksTest {

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

    public HttpTaskManagerSubtasksTest() throws IOException {
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
    public void getSubtasks() throws IOException, InterruptedException {
        // добавим один эпик и две его подзадачи в менеджер и проверим
        Epic epic = new Epic("Epic 1", "Epic 1", Status.NEW);
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask(epic, "Subtask 1", "Subtask 1", Status.NEW);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask(epic, "Subtask 2", "Subtask 2", Status.DONE);
        manager.addTask(subtask2);
        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "В теле ответа не массив JSON элементов!");
        List<Subtask> tasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
        }.getType());
        assertEquals(manager.getSubtasks(), tasks, "Список подзадач не совпадает!");
    }

    @Test
    public void getSubtaskById() throws IOException, InterruptedException {
        // добавим в менеджер эпик и две его подзадачи
        Epic epic = new Epic("Epic 1", "Epic 1", Status.NEW);
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask(epic, "Subtask 1", "Subtask 1", Status.NEW);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask(epic, "Subtask 2", "Subtask 2", Status.DONE);
        manager.addSubtask(subtask2);

        // запросим подзадачу с ID = 2
        url = URI.create("http://localhost:8080/subtasks/2");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject(), "В теле ответа не JSON элемент!");
        Subtask subtaskFromServer1 = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask1, subtaskFromServer1, "Подзадачи не одинаковые!");
        assertEquals(200, response.statusCode());

        // запросим подзадачу с ID = 3
        url = URI.create("http://localhost:8080/subtasks/3");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject(), "В теле ответа не JSON элемент!");
        Subtask subtaskFromServer2 = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask2, subtaskFromServer2, "Подзадачи не одинаковые!");
        assertEquals(200, response.statusCode());

        // запросим подзадачу с несуществующим ID
        url = URI.create("http://localhost:8080/subtasks/777");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());

        // запросим задачу с неправильным ID
        url = URI.create("http://localhost:8080/subtasks/id303");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void addSubtask() throws IOException, InterruptedException {
        // сначала создадим эпик в менеджере
        Epic epic = new Epic("Epic 1", "Epic 1", Status.NEW);
        manager.addEpic(epic);

        // добавим через клиента одну подзадачу эпика и проверим, появилась ли она в менеджере
        Subtask subtask1 = new Subtask(epic, "Subtask 1", "Subtask 1", Status.NEW,
                LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(subtask1);
        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются!");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Subtask 1", subtasksFromManager.getFirst().getName(),
                "Некорректное имя подзадачи");

        // добавим вторую подзадачу этого же эпика и тоже проверим ее в менеджере
        Subtask subtask2 = new Subtask(epic, "Subtask 2", "Subtask 2", Status.NEW,
                LocalDateTime.now().minusDays(1), Duration.ofMinutes(5));
        taskJson = gson.toJson(subtask2);
        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются!");
        assertEquals(2, subtasksFromManager.size(), "Некорректное количество подзадач!");
        assertEquals("Subtask 2", subtasksFromManager.get(1).getName(), "Некорректное имя подзадачи!");

        // добавим третью подзадачу у которой пересекается время и проверим ее в менеджере
        Subtask subtask3 = new Subtask(epic, "Subtask 3", "Subtask 3", Status.NEW,
                LocalDateTime.now().minusDays(1), Duration.ofMinutes(5));
        taskJson = gson.toJson(subtask3);
        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Должна быть ошибка 406 - время пересекается!");
        subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Задачи не возвращаются!");
        assertEquals(2, subtasksFromManager.size(), "Подзадача не должна была быть добавлена!");
    }

    @Test
    public void updateSubtask() throws IOException, InterruptedException {
        // добавим в менеджер эпик и три его подзадачи
        Epic epic = new Epic("Epic 1", "Epic 1", Status.NEW);
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask(epic, "Subtask 1", "Subtask 1", Status.NEW);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask(epic, "Subtask 2", "Subtask 2", Status.DONE);
        manager.addSubtask(subtask2);
        Subtask subtask3 = new Subtask(epic, "Subtask 3", "Subtask 3", Status.DONE);
        manager.addSubtask(subtask3);

        // обновим подзадачу с ID = 2 (установим ей время и сменим имя) и проверим
        Subtask subtask = new Subtask(2, "Subtask 1 updated", "Subtask 1", Status.NEW, 1,
                LocalDateTime.of(2024, 8, 19, 21, 0), Duration.ofMinutes(5));
        String taskJson = gson.toJson(subtask);
        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются!");
        assertEquals(3, subtasksFromManager.size(), "Некорректное количество подзадач!");
        assertEquals("Subtask 1 updated", subtasksFromManager.getFirst().getName(), "Не обновилось имя подзадачи!");
        assertEquals(LocalDateTime.of(2024, 8, 19, 21, 0), manager.getSubtaskById(2).getStartTime(),
                "Не обновилось время начала подзадачи!");
        assertEquals(Duration.ofMinutes(5), manager.getSubtaskById(2).getDuration(),
                "Не обновилось время выполнения подзадачи!");
    }

    @Test
    public void deleteSubtask() throws IOException, InterruptedException {
        // добавим в менеджер эпик и три его подзадачи
        Epic epic = new Epic("Epic 1", "Epic 1", Status.NEW);
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask(epic, "Subtask 1", "Subtask 1", Status.NEW);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask(epic, "Subtask 2", "Subtask 2", Status.DONE);
        manager.addSubtask(subtask2);
        Subtask subtask3 = new Subtask(epic, "Subtask 3", "Subtask 3", Status.DONE);
        manager.addSubtask(subtask3);

        // удалим подзадачу с ID = 2 и проверим
        url = URI.create("http://localhost:8080/subtasks/2");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются!");
        assertEquals(2, subtasksFromManager.size(), "Некорректное количество подзадач!");
        assertTrue(subtasksFromManager.stream().noneMatch(t -> t.getId() == 2),
                "Подзадача не удалилась!");

        // попробуем удалить подзадачу с не существующим ID, в менеджере сейчас 2 подзадачи
        url = URI.create("http://localhost:8080/subtasks/777");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        subtasksFromManager = manager.getSubtasks();
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются!");
        assertEquals(2, subtasksFromManager.size(), "Некорректное количество подзадач!");
        assertTrue(subtasksFromManager.stream().noneMatch(t -> t.getId() == 2),
                "Подзадача не удалилась!");
    }
}