package server;

import com.sun.net.httpserver.HttpServer;
import model.Epic;
import model.Subtask;
import model.Task;
import server.handlers.EpicsHandler;
import server.handlers.SubtasksHandler;
import server.handlers.TasksHandler;
import service.InMemoryTaskManager;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    public final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager manager;

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TasksHandler(manager));
        server.createContext("/subtasks", new SubtasksHandler(manager));
        server.createContext("/epics", new EpicsHandler(manager));
        //httpServer.createContext("/history", new HistoryHandler(taskManager));
        //httpServer.createContext("/prioritized", new prioritizedHandler(taskManager));
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer httpServer = new HttpTaskServer(Managers.getDefault());

        Task task;
        Epic epic;
        Subtask subtask;

        // создадим две задачи
        task = new Task("Почистить ковер", "Отвезти в химчистку Ковер-33");
        httpServer.manager.addTask(task); // id будет = 1
        task = new Task("Сварить борщ", "Найти рецепт борща");
        task.setStartTime(LocalDateTime.of(2024, 8, 5, 10, 0));
        task.setDuration(Duration.ofMinutes(13));
        httpServer.manager.addTask(task); // id будет = 2

        // создадим эпик с тремя подзадачами
        epic = new Epic("Переезд", "Переезд на новую квартиру");
        httpServer.manager.addEpic(epic); // id будет = 3
        subtask = new Subtask(epic, "Грузчики", "Найти грузчиков");
        httpServer.manager.addSubtask(subtask); // id будет = 4
        subtask = new Subtask(epic, "Кот", "Поймать кота и упаковать");
        httpServer.manager.addSubtask(subtask); // id будет = 5
        subtask = new Subtask(epic, "Мебель", "Запаковать мебель");
        subtask.setStartTime(LocalDateTime.of(2024, 8, 4, 14, 0));
        subtask.setDuration(Duration.ofMinutes(55));
        httpServer.manager.addSubtask(subtask); // id будет = 6

        // создадим эпик без подзадач
        epic = new Epic("Помыть окна", "Помыть окна после зимы");
        httpServer.manager.addEpic(epic); // id будет = 7

        httpServer.start();
        System.out.println("HTTP-сервер запущен на " + httpServer.PORT + " порту!");
        //httpServer.stop(1);
    }

    private void stop() {
        server.stop(1);
    }

    private void start() {
        server.start();
    }
}
