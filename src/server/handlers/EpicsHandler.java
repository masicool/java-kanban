package server.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import exception.ManagerSaveException;
import exception.NotFoundException;
import exception.TaskValidateException;
import model.Epic;
import model.Subtask;
import server.EndpointGroups;
import server.Endpoints;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {
    private final TaskManager taskManager;

    public EpicsHandler(TaskManager taskManager) {
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
        Endpoints endpoint = getEndpoint(pathParts, exchange.getRequestMethod(), EndpointGroups.EPICS);
        try {
            if (endpoint == Endpoints.UNKNOWN) throw new NotFoundException("Not Found");
            int tmpEpicId = -1;
            if (pathParts.length > 2) {
                tmpEpicId = Integer.parseInt(pathParts[2]);
            }
            switch (endpoint) {
                case GET_EPICS -> sendData(exchange, gson.toJson(taskManager.getEpics()), 200);
                case GET_EPIC_BY_ID -> sendData(exchange, gson.toJson(taskManager.getEpicById(tmpEpicId)), 200);
                case GET_EPIC_SUBTASKS -> {
                    Epic epic = taskManager.getEpicById(tmpEpicId);
                    List<Subtask> subtasks = taskManager.getEpicSubtasks(epic);
                    sendData(exchange, gson.toJson(subtasks), 200);
                }
                case POST_EPIC -> {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes(), CHAR_SET);
                    Epic epic = gson.fromJson(requestBody, Epic.class);
                    tmpEpicId = epic.getId();
                    boolean isEpicExist = true;
                    try {
                        taskManager.getEpicById(tmpEpicId);
                    } catch (NotFoundException e) {
                        isEpicExist = false;
                    }
                    if (isEpicExist) {
                        taskManager.updateEpic(epic);
                        sendData(exchange, gson.toJson(epic), 201);
                    } else {
                        taskManager.addEpic(epic);
                        sendData(exchange, 201);
                    }
                }
                case DELETE_EPIC -> {
                    taskManager.deleteEpicById(tmpEpicId);
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
