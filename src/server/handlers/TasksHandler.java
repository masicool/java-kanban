package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Task;
import server.EndpointGroups;
import server.Endpoints;
import service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
        //super();
        this.taskManager = taskManager;
    }

    /**
     * Handle the given request and generate an appropriate response.
     * See {@link HttpExchange} for a description of the steps
     * involved in handling an exchange.
     *
     * @param exchange the exchange containing the request from the
     *                 client and used to send the response
     * @throws NullPointerException if exchange is {@code null}
     * @throws IOException          if an I/O error occurs
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String[] pathParts = getSplitPath(exchange);
        Endpoints endpoint = getEndpoint(pathParts, exchange.getRequestMethod(), EndpointGroups.TASKS);
        if (endpoint == Endpoints.UNKNOWN) {
            // TODO сделать правильный ответ с ошибкой
            return;
        }
        String requestBody =  new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        int tmpTaskId = -1;
        try {
            if (pathParts.length > 2) {
                tmpTaskId = Integer.parseInt(pathParts[2]);
            }
            switch (endpoint) {
                case GET_TASKS -> {
                    sendText(exchange, gson.toJson(taskManager.getTasks()), 200);
                }
                case GET_TASK_BY_ID -> {
                    sendText(exchange, gson.toJson(taskManager.getTaskById(tmpTaskId)), 200);
                }
                case POST_TASK -> {
                    Task task = gson.fromJson(requestBody, Task.class);
                    tmpTaskId = task.getId();
                    if (tmpTaskId != 0) {
                        taskManager.updateTask(task);
                        sendText(exchange, "The task with ID=" + tmpTaskId + " has been updated.", 201);
                    }
                    else {
                        taskManager.addTask(task);
                        sendText(exchange, "The task was created with ID=" + task.getId(), 201);
                    }
                }
                case DELETE_TASK -> {
                    taskManager.deleteTaskById(tmpTaskId);
                    sendText(exchange, "The task with ID=" + tmpTaskId + " has been deleted.", 201);
                }
            }
        } catch (NumberFormatException e) {
            // TODO не найдет ID задачи
            System.out.println(e.getMessage());
        }
    }
}
