package server.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import server.EndpointGroups;
import server.Endpoints;
import server.adapters.DurationAdapter;
import server.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler {
    protected Gson gson;

    public BaseHttpHandler() {
        gson = new GsonBuilder()
                //.serializeNulls() //TODO подумать, убрать или оставить
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter()).create();
    }

    protected void sendText(HttpExchange exchange, String text, int rCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");

        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(rCode, resp.length);
            os.write(resp);
        }
        exchange.close();
    }

    protected String[] getSplitPath(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        return requestPath.split("/");
    }

    protected Endpoints getEndpoint(String[] pathParts, String requestMethod, EndpointGroups group) {
        switch (group) {
            case TASKS -> {
                switch (requestMethod) {
                    case "GET" -> {
                        if (pathParts.length == 2) return Endpoints.GET_TASKS;
                        if (pathParts.length == 3) return Endpoints.GET_TASK_BY_ID;
                    }
                    case "POST" -> {
                        if (pathParts.length == 2) return Endpoints.POST_TASK;
                    }
                    case "DELETE" -> {
                        if (pathParts.length == 3) return Endpoints.DELETE_TASK;
                    }
                }
            }
            case SUBTASKS -> {
                switch (requestMethod) {
                    case "GET" -> {
                        if (pathParts.length == 2) return Endpoints.GET_SUBTASKS;
                        if (pathParts.length == 3) return Endpoints.GET_SUBTASK_BY_ID;
                    }
                    case "POST" -> {
                        if (pathParts.length == 2) return Endpoints.POST_SUBTASK;
                    }
                    case "DELETE" -> {
                        if (pathParts.length == 3) return Endpoints.DELETE_SUBTASK;
                    }
                }
            }
            case EPICS -> {
                switch (requestMethod) {
                    case "GET" -> {
                        if (pathParts.length == 2) return Endpoints.GET_EPICS;
                        if (pathParts.length == 3) return Endpoints.GET_EPIC_BY_ID;
                        if (pathParts.length == 4 && pathParts[3].equals("subtasks"))
                            return Endpoints.GET_EPIC_SUBTASKS;
                    }
                    case "POST" -> {
                        if (pathParts.length == 2) return Endpoints.POST_EPIC;
                    }
                    case "DELETE" -> {
                        if (pathParts.length == 3) return Endpoints.DELETE_EPIC;
                    }
                }
            }
            case HISTORY -> {
                if (requestMethod.equals("GET") && pathParts.length == 2) return Endpoints.GET_HISTORY;
            }
            case PRIORITIZED -> {
                if (requestMethod.equals("GET") && pathParts.length == 2) return Endpoints.GET_PRIORITIZED_TASKS;
            }
        }

        return Endpoints.UNKNOWN;
    }
}