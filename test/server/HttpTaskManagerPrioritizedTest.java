package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import model.Epic;
import model.Status;
import model.Subtask;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskManagerPrioritizedTest {

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

    public HttpTaskManagerPrioritizedTest() throws IOException {
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
    public void getPrioritized() throws IOException, InterruptedException {
        // добавим две задачи, эпик и подзадачи в менеджер
        Task task1 = new Task("Task 1", "Task 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        manager.addTask(task1); // ID = 1
        Task task2 = new Task("Task 2", "Task 2", Status.NEW, LocalDateTime.now().plusDays(1), Duration.ofMinutes(5));
        manager.addTask(task2); // ID = 2
        // добавим в менеджер эпик и две его подзадачи
        Epic epic = new Epic("Epic 1", "Epic 1", Status.NEW);
        manager.addEpic(epic); // ID = 3
        Subtask subtask1 = new Subtask(epic, "Subtask 1", "Subtask 1", Status.NEW, LocalDateTime.now().plusDays(2),
                Duration.ofMinutes(5));
        manager.addSubtask(subtask1); // ID = 4
        Subtask subtask2 = new Subtask(epic, "Subtask 2", "Subtask 2", Status.DONE);
        manager.addSubtask(subtask2); // ID = 5

        url = URI.create("http://localhost:8080/prioritized");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "В теле ответа не массив JSON элементов!");
        List<Task> prioritized = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        // в списке должны быть задачи с указанным временем
        assertEquals(3, prioritized.size(), "Не верный размер списка отсортированных задая!");
        assertEquals(1, prioritized.getFirst().getId(), "Отсортированный по времени списко задач не совпадает!");
        assertEquals(2, prioritized.get(1).getId(), "Отсортированный по времени списко задач не совпадает!");
        assertEquals(4, prioritized.get(2).getId(), "Отсортированный по времени списко задач не совпадает!");
    }

    @Test
    public void getPrioritizedWhenIsEmpty() throws IOException, InterruptedException {
        url = URI.create("http://localhost:8080/prioritized");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        assertTrue(jsonElement.isJsonArray(), "В теле ответа не массив JSON элементов!");
        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertTrue(history.isEmpty(), "Отсортированный список не пустой!");
    }
}