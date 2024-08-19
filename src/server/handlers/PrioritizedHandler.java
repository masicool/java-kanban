package server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.NotFoundException;
import server.ContentTypes;
import server.EndpointGroups;
import server.Endpoints;
import service.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;

    public PrioritizedHandler(TaskManager taskManager) {
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
        Endpoints endpoint = getEndpoint(pathParts, exchange.getRequestMethod(), EndpointGroups.PRIORITIZED);
        try {
            if (endpoint == Endpoints.UNKNOWN) throw new NotFoundException("Not Found");
            sendData(exchange, gson.toJson(taskManager.getPrioritizedTasks()), 200, ContentTypes.JSON);
        } catch (NotFoundException e) {
            sendData(exchange, e.getMessage(), 404, ContentTypes.HTML);
        }
    }
}
