package server.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
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

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final Charset CHAR_SET = StandardCharsets.UTF_8;
    protected Gson gson;

    public BaseHttpHandler() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter()).create();
    }

    /**
     * Отправка ответа сервера с сообщением в теле ответа
     *
     * @param exchange - объект класса HttpExchange для обмена данными
     * @param text     - текстовая часть ответа сервера
     * @param rCode    - код ответа сервера
     * @throws IOException - возможное исключение
     */
    protected void sendData(HttpExchange exchange, String text, int rCode) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", ContentTypes.JSON.getValue() + ";charset=" + CHAR_SET.name());

        if (text.isBlank()) {
            exchange.sendResponseHeaders(rCode, -1);
        } else {
            try (OutputStream os = exchange.getResponseBody()) {
                byte[] response = text.getBytes(CHAR_SET);
                exchange.sendResponseHeaders(rCode, response.length);
                os.write(response);
            }
        }
        exchange.close();
    }

    /**
     * Отправка ответа сервера без сообщения в теле ответа
     *
     * @param exchange - объект класса HttpExchange для обмена данными
     * @param rCode    - код ответа сервера
     * @throws IOException - возможное исключение
     */
    protected void sendData(HttpExchange exchange, int rCode) throws IOException {
        sendData(exchange, "", rCode);
    }

    /**
     * Разделение пути запроса на отдельные строки
     *
     * @param exchange - контейнер обмена
     * @return - массив строк
     */
    protected String[] getSplitPath(HttpExchange exchange) {
        String requestPath = exchange.getRequestURI().getPath();
        return requestPath.split("/");
    }

    /**
     * Получение конечного эндпоинта из пути запроса
     *
     * @param pathParts     - путь запроса, разбитый на отдельные подстроки
     * @param requestMethod - тип запроса
     * @param group         - тип эндпоинта для поиска
     * @return - эндпоинт
     */
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