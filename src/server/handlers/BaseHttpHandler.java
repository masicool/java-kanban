package server.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import server.ContentTypes;
import server.EndpointGroups;
import server.Endpoints;
import server.adapters.DurationAdapter;
import server.adapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler {
    protected Gson gson;
    private final Charset CHAR_SET = StandardCharsets.UTF_8;

    public BaseHttpHandler() {
        gson = new GsonBuilder()
                //.serializeNulls() //TODO подумать, убрать или оставить
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter()).create();
    }

    protected void sendData(HttpExchange exchange, String text, int rCode, ContentTypes contentType) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType.getValue() + ";charset=" + CHAR_SET.name());
        String respStr = text;
        if (contentType == ContentTypes.HTML) {
            respStr = "<h1>" + rCode + " " + text + "</h1";
        }
        byte[] response = respStr.getBytes(CHAR_SET);
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(rCode, response.length);
            os.write(response);
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
                if (!pathParts[1].equals("tasks")) return Endpoints.UNKNOWN; // доп. проверка на путь
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
                if (!pathParts[1].equals("subtasks")) return Endpoints.UNKNOWN; // доп. проверка на путь
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
                if (!pathParts[1].equals("epics")) return Endpoints.UNKNOWN; // доп. проверка на путь
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
                if (!pathParts[1].equals("history")) return Endpoints.UNKNOWN; // доп. проверка на путь
                if (requestMethod.equals("GET") && pathParts.length == 2) return Endpoints.GET_HISTORY;
            }
            case PRIORITIZED -> {
                if (!pathParts[1].equals("prioritized")) return Endpoints.UNKNOWN; // доп. проверка на путь
                if (requestMethod.equals("GET") && pathParts.length == 2) return Endpoints.GET_PRIORITIZED_TASKS;
            }
        }

        return Endpoints.UNKNOWN;
    }
}