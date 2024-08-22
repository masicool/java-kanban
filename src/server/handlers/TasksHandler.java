package server.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.TaskValidateException;
import model.Task;
import server.EndpointGroups;
import server.Endpoints;
import service.TaskManager;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public TasksHandler(TaskManager taskManager) {
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
        try {
            if (endpoint == Endpoints.UNKNOWN) throw new NotFoundException("Not Found");
            int tmpTaskId = -1;
            if (pathParts.length > 2) {
                tmpTaskId = Integer.parseInt(pathParts[2]);
            }
            switch (endpoint) {
                case GET_TASKS -> sendData(exchange, gson.toJson(taskManager.getTasks()), 200);
                case GET_TASK_BY_ID -> sendData(exchange, gson.toJson(taskManager.getTaskById(tmpTaskId)), 200);
                case POST_TASK -> {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), CHAR_SET);
                    Task task = gson.fromJson(requestBody, Task.class);
                    tmpTaskId = task.getId();
                    try {
                        taskManager.getTaskById(tmpTaskId);
                        taskManager.updateTask(task);
                        sendData(exchange, gson.toJson(task), 201);
                    } catch (NotFoundException e) {
                        taskManager.addTask(task);
                        sendData(exchange, 201);
                    }
                }
                case DELETE_TASK -> {
                    taskManager.deleteTaskById(tmpTaskId);
                    sendData(exchange, 204);
                }
            }
        } catch (NotFoundException | NumberFormatException e) {
            sendData(exchange, e.getMessage(), 404);
        } catch (TaskValidateException e) {
            sendData(exchange, e.getMessage(), 406);
        } catch (ManagerSaveException e) {
            sendData(exchange, e.getMessage(), 500);
        } catch (JsonSyntaxException e) {
            sendData(exchange, "JSON syntax error.", 406);
        }
    }
}
