package service;

import model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;

public class FileCsvUtils {
    private static final int NUMBER_OF_FIELDS_IN_CSV_FILE = 6; // максимальное кол-во полей в файле CSV
    private static final String CSV_SEPARATOR = ","; // разделитель между полями файла CSV
    private static final int[] orderOfFields = new int[NUMBER_OF_FIELDS_IN_CSV_FILE]; // порядок полей в файле

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

    // парсинг строки и создание задачи
    public static Task fromString(String value) {
        String[] splitLine = value.split(CSV_SEPARATOR);
        int id = parseIdFromString(splitLine[orderOfFields[1]]);
        Status status = Status.valueOf(splitLine[orderOfFields[4]]);
        Task task;

        switch (TaskType.valueOf(splitLine[orderOfFields[0]])) {
            case TASK -> {
                task = new Task(id, splitLine[orderOfFields[2]], splitLine[orderOfFields[3]], status);
            }
            case EPIC -> {
                task = new Epic(id, splitLine[orderOfFields[2]], splitLine[orderOfFields[3]], status);
            }
            case SUBTASK -> {
                int epicId = parseIdFromString(splitLine[orderOfFields[5]]);
                task = new Subtask(id, splitLine[orderOfFields[2]], splitLine[orderOfFields[3]], status, epicId);
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
}
