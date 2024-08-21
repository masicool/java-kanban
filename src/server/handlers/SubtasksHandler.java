package server.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.TaskValidateException;
import model.Subtask;
import server.ContentTypes;
import server.EndpointGroups;
import server.Endpoints;
import service.TaskManager;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public SubtasksHandler(TaskManager taskManager) {
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
        Endpoints endpoint = getEndpoint(pathParts, exchange.getRequestMethod(), EndpointGroups.SUBTASKS);
        try {
            if (endpoint == Endpoints.UNKNOWN) throw new NotFoundException("Not Found");
            int tmpSubtaskId = -1;
            if (pathParts.length > 2) {
                tmpSubtaskId = Integer.parseInt(pathParts[2]);
            }
            switch (endpoint) {
                case GET_SUBTASKS -> sendData(exchange, gson.toJson(taskManager.getSubtasks()), 200, ContentTypes.JSON);
                case GET_SUBTASK_BY_ID ->
                        sendData(exchange, gson.toJson(taskManager.getSubtaskById(tmpSubtaskId)), 200, ContentTypes.JSON);
                case POST_SUBTASK -> {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), CHAR_SET);
                    Subtask subtask = gson.fromJson(requestBody, Subtask.class);
                    tmpSubtaskId = subtask.getId();
                    try {
                        taskManager.getSubtaskById(tmpSubtaskId);
                        taskManager.updateSubtask(subtask);
                        sendData(exchange, "The subtask with ID=" + tmpSubtaskId + " has been updated.", 201,
                                ContentTypes.HTML);
                    } catch (NotFoundException e) {
                        taskManager.addSubtask(subtask);
                        sendData(exchange, "The subtask was created with ID=" + subtask.getId(), 201,
                                ContentTypes.HTML);
                    }
                }
                case DELETE_SUBTASK -> {
                    taskManager.deleteSubtaskById(tmpSubtaskId);
                    sendData(exchange, "The subtask with ID=" + tmpSubtaskId + " has been deleted.", 201,
                            ContentTypes.HTML);
                }
            }
        } catch (NotFoundException | NumberFormatException e) {
            sendData(exchange, e.getMessage(), 404, ContentTypes.HTML);
        } catch (TaskValidateException e) {
            sendData(exchange, e.getMessage(), 406, ContentTypes.HTML);
        } catch (ManagerSaveException e) {
            sendData(exchange, e.getMessage(), 500, ContentTypes.HTML);
        } catch (JsonSyntaxException e) {
            sendData(exchange, "JSON syntax error.", 406, ContentTypes.HTML);
        }
    }
}
