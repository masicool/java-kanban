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

public class HttpTaskManagerEpicsTest {

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

    public HttpTaskManagerEpicsTest() throws IOException {
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
    public void getEpics() throws IOException, InterruptedException {
        // добавим два эпика
        Epic epic1 = new Epic("Epic 1", "Epic 1", Status.NEW);
        manager.addEpic(epic1);
        Epic epic2 = new Epic("Epic 2", "Epic 2", Status.NEW);
        manager.addEpic(epic2);
        url = URI.create("http://localhost:8080/epics");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "В теле ответа не массив JSON элементов!");
        List<Epic> epics = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {
        }.getType());
        assertEquals(manager.getEpics(), epics, "Список эпиков не совпадает!");
    }

    @Test
    public void getEpicById() throws IOException, InterruptedException {
        // добавим два эпика
        Epic epic1 = new Epic("Epic 1", "Epic 1", Status.NEW);
        manager.addEpic(epic1);
        Epic epic2 = new Epic("Epic 2", "Epic 2", Status.NEW);
        manager.addEpic(epic2);

        // запросим эпик с ID = 1
        url = URI.create("http://localhost:8080/epics/1");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject(), "В теле ответа не JSON элемент!");
        Epic epicFromServer = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic1, epicFromServer, "Эпики не одинаковые!");
        assertEquals(200, response.statusCode());

        // запросим эпик с ID = 2
        url = URI.create("http://localhost:8080/epics/2");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonObject(), "В теле ответа не JSON элемент!");
        epicFromServer = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic2, epicFromServer, "Эпики не одинаковые!");
        assertEquals(200, response.statusCode());

        // запросим эпик с несуществующим ID
        url = URI.create("http://localhost:8080/epics/777");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());

        // запросим эпик с неправильным ID
        url = URI.create("http://localhost:8080/epics/id303");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void getEpicSubtasks() throws IOException, InterruptedException {
        // добавим в менеджер эпик и две его подзадачи
        Epic epic = new Epic("Epic 1", "Epic 1", Status.NEW);
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask(epic, "Subtask 1", "Subtask 1", Status.NEW);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask(epic, "Subtask 2", "Subtask 2", Status.DONE);
        manager.addSubtask(subtask2);

        // запросим подзадачи эпика с ID = 1
        url = URI.create("http://localhost:8080/epics/1/subtasks");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "В теле ответа не JSON массив!");
        List<Subtask> subtasksFromServer = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
        }.getType());
        assertEquals(200, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getEpicSubtasks(epic);
        assertEquals(2, subtasksFromServer.size(), "Некорректное количество подзадач!");
        assertTrue(subtasksFromManager.stream().allMatch(t -> t.getEpicId() == 1), "Изменились подзадачи!");
    }

    @Test
    public void addEpic() throws IOException, InterruptedException {
        // сначала создадим эпик в менеджере без подзадач, добавим через клиента его в менеджер
        Epic epic1 = new Epic("Epic 1", "Epic 1", Status.NEW);
        String taskJson = gson.toJson(epic1);
        url = URI.create("http://localhost:8080/epics");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Epic> epicsFromManager = manager.getEpics();
        assertNotNull(epicsFromManager, "Эпики не возвращаются!");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков!");
        assertEquals("Epic 1", epicsFromManager.getFirst().getName(),
                "Некорректное имя эпика!");

        // добавим второй эпика безе подзадач, добавим через клиента его в менеджер
        Epic epic2 = new Epic("Epic 2", "Epic 2", Status.NEW);
        taskJson = gson.toJson(epic2);
        url = URI.create("http://localhost:8080/epics");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        epicsFromManager = manager.getEpics();
        assertNotNull(epicsFromManager, "Эпики не возвращаются!");
        assertEquals(2, epicsFromManager.size(), "Некорректное количество эпиков!");
        assertEquals("Epic 2", epicsFromManager.get(1).getName(),
                "Некорректное имя эпика!");
    }

    @Test
    public void updateEpic() throws IOException, InterruptedException {
        // добавим в менеджер эпик и две его подзадачи
        Epic epic = new Epic("Epic 1", "Epic 1", Status.NEW);
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask(epic, "Subtask 1", "Subtask 1", Status.NEW);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask(epic, "Subtask 2", "Subtask 2", Status.DONE);
        manager.addSubtask(subtask2);

        // обновим эпик через клиента (удалим у него 1-ю подзадачу, сменим имя) и проверим изменения
        String jsonStr = "{ \"id\": 1, \"name\": \"Epic 1 updated\", \"description\": \"Epic 1\", \"subTasksId\":[2]}";

        url = URI.create("http://localhost:8080/epics");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(jsonStr)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        epic = manager.getEpicById(1);
        List<Subtask> subtasksFromManager = manager.getEpicSubtasks(epic);
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются!");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач!");
        assertEquals(1, epic.getId(), "Изменилось ID эпика");
        assertEquals("Epic 1 updated", epic.getName(), "Не обновилось имя эпика");
        assertEquals(2, subtasksFromManager.getFirst().getId(), "Задача не удалилась!");
        assertEquals(1, subtasksFromManager.getFirst().getEpicId(), "У подзадачи не правильный эпик!");
    }

    @Test
    public void deleteEpic() throws IOException, InterruptedException {
        // добавим в менеджер эпик и две его подзадачи
        Epic epic = new Epic("Epic 1", "Epic 1", Status.NEW);
        manager.addEpic(epic);
        Subtask subtask1 = new Subtask(epic, "Subtask 1", "Subtask 1", Status.NEW);
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask(epic, "Subtask 2", "Subtask 2", Status.DONE);
        manager.addSubtask(subtask2);

        // удалим эпик с ID = 1 и проверим
        url = URI.create("http://localhost:8080/epics/1");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(204, response.statusCode());
        List<Subtask> subtasksFromManager = manager.getSubtasks();
        assertTrue(subtasksFromManager.isEmpty(), "Подзадачи не не удалилсь!");
        List<Epic> epicsFromManager = manager.getEpics();
        assertTrue(epicsFromManager.stream().noneMatch(t -> t.getId() == 1),
                "Подзадача не удалилась!");

        // попробуем удалить эпик с не существующим ID
        epic = new Epic("Epic 1", "Epic 1", Status.NEW);
        manager.addEpic(epic);
        subtask1 = new Subtask(epic, "Subtask 1", "Subtask 1", Status.NEW);
        manager.addSubtask(subtask1);
        subtask2 = new Subtask(epic, "Subtask 2", "Subtask 2", Status.DONE);
        manager.addSubtask(subtask2);
        url = URI.create("http://localhost:8080/epics/111");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
        subtasksFromManager = manager.getSubtasks();
        assertEquals(2, subtasksFromManager.size(), "Изменились подзадачи!");
        epicsFromManager = manager.getEpics();
        assertEquals(1, epicsFromManager.size(), "Измениись эпики!");
    }
}