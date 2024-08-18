package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.TaskValidateException;
import model.Task;
import server.ContentTypes;
import server.EndpointGroups;
import server.Endpoints;
import service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

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
        try {
            if (endpoint == Endpoints.UNKNOWN) throw new NotFoundException("Not Found");
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            int tmpTaskId = -1;
            if (pathParts.length > 2) {
                tmpTaskId = Integer.parseInt(pathParts[2]);
            }
            switch (endpoint) {
                case GET_TASKS -> {
                    sendData(exchange, gson.toJson(taskManager.getTasks()), 200, ContentTypes.JSON);
                }
                case GET_TASK_BY_ID -> {
                    sendData(exchange, gson.toJson(taskManager.getTaskById(tmpTaskId)), 200, ContentTypes.JSON);
                }
                case POST_TASK -> {
                    Task task = gson.fromJson(requestBody, Task.class);
                    tmpTaskId = task.getId();
                    try {
                        taskManager.getTaskById(tmpTaskId);
                        taskManager.updateTask(task);
                        sendData(exchange, "The task with ID=" + tmpTaskId + " has been updated.", 201, ContentTypes.HTML);
                    } catch (NotFoundException e) {
                        taskManager.addTask(task);
                        sendData(exchange, "The task was created with ID=" + task.getId(), 201, ContentTypes.HTML);
                    }
                }
                case DELETE_TASK -> {
                    taskManager.deleteTaskById(tmpTaskId);
                    sendData(exchange, "The task with ID=" + tmpTaskId + " has been deleted.", 201, ContentTypes.HTML);
                }
            }
        } catch (NotFoundException | NumberFormatException e) {
            sendData(exchange, e.getMessage(), 404, ContentTypes.HTML);
        } catch (TaskValidateException e) {
            sendData(exchange, e.getMessage(), 406, ContentTypes.HTML);
        } catch (ManagerSaveException e) {
            sendData(exchange, e.getMessage(), 500, ContentTypes.HTML);
        }
    }
}
