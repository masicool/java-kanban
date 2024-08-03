package service;

import model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

public class FileCsvUtils {
    private static final int NUMBER_OF_FIELDS_IN_CSV_FILE = 9; // максимальное кол-во полей в файле CSV
    private static final String CSV_SEPARATOR = ","; // разделитель между полями файла CSV
    private static final int[] orderOfFields = new int[NUMBER_OF_FIELDS_IN_CSV_FILE]; // порядок полей в файле

    /**
     * Проверка заголовка файла CSV
     *
     * @param fileReader ссылка на объект файоа
     * @throws IOException возможные исключения в методе
     */
    public static void checkHeader(BufferedReader fileReader) throws IOException {
        // чтение и проверка заголовка файла (первой строки)
        if (!fileReader.ready()) throw new ManagerSaveException("Файл пустой!");

        String firstLine = fileReader.readLine();

        if (firstLine.isBlank())
            throw new ManagerSaveException("Поврежден заголовок файла CSV: пустая первая" + " строка!");

        String[] split = firstLine.split(CSV_SEPARATOR);

        // заполним массив порядка полей в файле начальным значением -1 для последующей проверки
        Arrays.fill(orderOfFields, -1);

        // порядок полей в файле CSV может быть произвольный, но количество полей должно совпадать
        for (int i = 0; i < split.length; i++) {
            switch (split[i]) {
                case "type" -> orderOfFields[0] = i;
                case "id" -> orderOfFields[1] = i;
                case "name" -> orderOfFields[2] = i;
                case "description" -> orderOfFields[3] = i;
                case "status" -> orderOfFields[4] = i;
                case "epic" -> orderOfFields[5] = i;
                case "starttime" -> orderOfFields[6] = i;
                case "duration" -> orderOfFields[7] = i;
                case "endtime" -> orderOfFields[8] = i;
                default ->
                        throw new ManagerSaveException("Поврежден заголовок файла CSV: неизвестное поле '" + split[i] + "'!");
            }
        }

        // порядок следования каждого поля в файле должен быть установлен, иначе ошибка формата
        for (int orderOfField : orderOfFields) {
            if (orderOfField == -1) {
                throw new ManagerSaveException("Поврежден заголовка файла CSV: не хватает полей!");
            }
        }
    }

    /**
     * Парсинг строки и создание задачи
     *
     * @param value строка в формате CSV
     * @return новый объект - задача
     */
    public static Task fromString(String value) {
        String[] splitLine = value.split(CSV_SEPARATOR, NUMBER_OF_FIELDS_IN_CSV_FILE);
        int id = parseIdFromString(splitLine[orderOfFields[1]]);
        Status status = Status.valueOf(splitLine[orderOfFields[4]]);
        LocalDateTime startTime = parseDateTimeFromString(splitLine[orderOfFields[6]]);
        Duration duration = parseDurationFromString(splitLine[orderOfFields[7]]);
        LocalDateTime endTime;
        Task task;

        switch (TaskType.valueOf(splitLine[orderOfFields[0]])) {
            case TASK -> task = new Task(id, splitLine[orderOfFields[2]], splitLine[orderOfFields[3]], status,
                    startTime, duration);
            case EPIC -> {
                endTime = parseDateTimeFromString(splitLine[orderOfFields[8]]);
                task = new Epic(id, splitLine[orderOfFields[2]], splitLine[orderOfFields[3]], status, startTime,
                        duration, endTime);
            }
            case SUBTASK -> {
                int epicId = parseIdFromString(splitLine[orderOfFields[5]]);
                task = new Subtask(id, splitLine[orderOfFields[2]], splitLine[orderOfFields[3]], status, epicId,
                        startTime, duration);
            }
            default -> throw new ManagerSaveException("Не корректный тип задачи: " + splitLine[orderOfFields[0]] + "!");
        }
        return task;
    }

    // парсинг строки и возврат числа - ID задачи
    private static int parseIdFromString(String strId) {
        int id;
        try {
            id = Integer.parseInt(strId);
            if (id <= 0) {
                throw new ManagerSaveException("ID задачи должен быть больше нуля, а не '" + strId + "'!");
            }
        } catch (NumberFormatException e) {
            throw new ManagerSaveException("ID задачи должен быть числом, а не '" + strId + "'!");
        }
        return id;
    }

    // парсинг строки со значением времени и возврат LocalDateTime
    private static LocalDateTime parseDateTimeFromString(String strId) {
        if (strId.isBlank()) return null;

        long utcTime;

        try {
            utcTime = Long.parseLong(strId);
            if (utcTime <= 0) {
                throw new ManagerSaveException("Время задачи должен быть больше нуля, а не '" + strId +
                        "'!");
            }
        } catch (NumberFormatException e) {
            throw new ManagerSaveException("Время задачи должен быть числом, а не '" + strId + "'!");
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(utcTime), ZoneOffset.UTC);
    }

    // парсинг строки со значением времени в минутах и возврат Duration
    private static Duration parseDurationFromString(String strId) {
        if (strId.isBlank()) return null;

        long minutes;

        try {
            minutes = Long.parseLong(strId);
            if (minutes <= 0) {
                throw new ManagerSaveException("Продолжительность задачи должен быть больше нуля, а не '" + strId +
                        "'!");
            }
        } catch (NumberFormatException e) {
            throw new ManagerSaveException("Продолжительность задачи должен быть числом, а не '" + strId + "'!");
        }
        return Duration.ofMinutes(minutes);
    }
}
