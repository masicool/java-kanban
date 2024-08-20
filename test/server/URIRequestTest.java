package server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class URIRequestTest {
    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    HttpClient client;
    HttpRequest request;
    URI url;
    HttpResponse<String> response;

    public URIRequestTest() throws IOException {
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
    public void wrongGetRequestTask() throws IOException, InterruptedException {
        // сформируем неправильный GET запрос, должна быть ошибка 404
        url = URI.create("http://localhost:8080/tasksssssssss");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void wrongPostRequestTask() throws IOException, InterruptedException {
        // сформируем неправильный POST запрос, должна быть ошибка 404
        url = URI.create("http://localhost:8080/taskssssssss");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("")).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void wrongDeleteRequestTask() throws IOException, InterruptedException {
        // сформируем неправильный DELETE запрос, должна быть ошибка 404
        url = URI.create("http://localhost:8080/taskssssssss");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("")).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void wrongGetRequestSubtask() throws IOException, InterruptedException {
        // сформируем неправильный GET запрос, должна быть ошибка 404
        url = URI.create("http://localhost:8080/subtasksssssssss");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void wrongPostRequestSubtask() throws IOException, InterruptedException {
        // сформируем неправильный POST запрос, должна быть ошибка 404
        url = URI.create("http://localhost:8080/subtaskssssssss");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("")).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void wrongDeleteRequestSubtask() throws IOException, InterruptedException {
        // сформируем неправильный DELETE запрос, должна быть ошибка 404
        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void wrongGetRequestEpic() throws IOException, InterruptedException {
        // сформируем неправильный GET запрос, должна быть ошибка 404
        url = URI.create("http://localhost:8080/epicsssssssss");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void wrongPostRequestEpic() throws IOException, InterruptedException {
        // сформируем неправильный POST запрос, должна быть ошибка 404
        url = URI.create("http://localhost:8080/epickssssssss");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("")).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void wrongDeleteRequestEpic() throws IOException, InterruptedException {
        // сформируем неправильный DELETE запрос, должна быть ошибка 404
        url = URI.create("http://localhost:8080/epics");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }
}
