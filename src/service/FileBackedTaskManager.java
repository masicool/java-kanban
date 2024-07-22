package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class FileBackedTaskManager extends InMemoryTaskManager {
    public static final int NUMBER_OF_FIELDS_IN_CSV_FILE = 6; // максимальное кол-во полей в файле CSV
    public static final String CSV_SEPARATOR = ","; // разделитель между полями файла CSV
    private String path; // путь и наименование файла для сохранения

    FileBackedTaskManager(String fileName) {
        if (!fileName.isBlank()) {
            path = fileName;
        }
    }

    // еще один main для тестирования класса
    public static void main(String[] args) {
        FileBackedTaskManager taskManager;

        try {
            taskManager = new FileBackedTaskManager("tasks.csv");
            Task task;
            Epic epic;
            Subtask subtask;

            // создадим две задачи
            task = new Task("Почистить ковер", "Отвезти в химчистку Ковер-33");
            taskManager.addTask(task); // id будет = 1
            task = new Task("Сварить борщ", "Найти рецепт борща");
            taskManager.addTask(task); // id будет = 2

            // создадим эпик с тремя подзадачами
            epic = new Epic("Переезд", "Переезд на новую квартиру");
            taskManager.addEpic(epic); // id будет = 3
            subtask = new Subtask(epic, "Грузчики", "Найти грузчиков");
            taskManager.addSubtask(subtask); // id будет = 4
            subtask = new Subtask(epic, "Кот", "Поймать кота и упаковать");
            taskManager.addSubtask(subtask); // id будет = 5
            subtask = new Subtask(epic, "Мебель", "Запаковать мебель");
            taskManager.addSubtask(subtask); // id будет = 6

            // создадим эпик без подзадач
            epic = new Epic("Помыть окна", "Помыть окна после зимы");
            taskManager.addEpic(epic); // id будет = 7

            // создадим копию менеджера из файла сохранения, предварительно скопировав файл
            try {
                Files.copy(Path.of("tasks.csv"), Path.of("taskscopy.csv"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException | RuntimeException e) {
                throw new ManagerSaveException("Ошибка копирования файла!");
            }

            FileBackedTaskManager taskManagerFromFile = FileBackedTaskManager.loadFromFile("taskscopy.csv");
            if (taskManagerFromFile == null) return;

            // выведем в терминал списки задач оригинала и копии и убедимся, что в копии те же задачи
            System.out.println(taskManager.getTasks());
            System.out.println(taskManagerFromFile.getTasks());
            System.out.println();
            System.out.println(taskManager.getEpics());
            System.out.println(taskManagerFromFile.getEpics());
            System.out.println();
            System.out.println(taskManager.getSubtasks());
            System.out.println(taskManagerFromFile.getSubtasks());
            System.out.println();
        } catch (ManagerSaveException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Создание менеджера задач и загрузка его задачами из файла CSV
     * в первой строке файл - заголовок со списком всех полей для загрузки-выгрузки
     * (порядок полей может быть произвольным)
     *
     * @param fileName файл с задачами в формате CSV
     * @return менеджер задач
     */
    public static FileBackedTaskManager loadFromFile(String fileName) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager("");

        try (LineNumberReader fileReader = new LineNumberReader(new FileReader(fileName, StandardCharsets.UTF_8))) {
            // чтение и проверка заголовка файла (первой строки)
            if (!fileReader.ready()) throw new ManagerSaveException("Файл пустой!");

            String firstLine = fileReader.readLine();
            if (firstLine.isBlank()) throw new ManagerSaveException("Поврежден заголовок файла CSV: пустая первая" +
                    " строка!");

            String[] split = firstLine.split(CSV_SEPARATOR);
            int[] orderOfFields = new int[NUMBER_OF_FIELDS_IN_CSV_FILE]; // порядок полей в файле

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
                    default -> throw new ManagerSaveException("Поврежден заголовок файла CSV: неизвестное поле!");
                }
            }

            // порядок следования каждого поля в файле должен быть установлен, иначе ошибка формата
            for (int orderOfField : orderOfFields) {
                if (orderOfField == -1) {
                    throw new ManagerSaveException("Поврежден заголовка файла CSV: не хватает полей!");
                }
            }

            // читаем построчно файл, разбираем строки и создаем задачи
            while (fileReader.ready()) {
                String[] splitLine = fileReader.readLine().split(CSV_SEPARATOR);

                // проверяем id задачи на корректность
                int id;
                try {
                    id = Integer.parseInt(splitLine[orderOfFields[1]]);
                    if (id <= 0) {
                        throw new ManagerSaveException("Строка " + fileReader.getLineNumber() +
                                ": ID задачи должен быть больше нуля!");
                    }
                } catch (NumberFormatException e) {
                    throw new ManagerSaveException("Строка " + fileReader.getLineNumber() +
                            ": ID задачи должен быть числом!");
                }

                // проверяем поле статуса на корректность
                Status status;
                switch (splitLine[orderOfFields[4]]) {
                    case "NEW" -> status = Status.NEW;
                    case "IN_PROGRESS" -> status = Status.IN_PROGRESS;
                    case "DONE" -> status = Status.DONE;
                    default -> throw new ManagerSaveException("Строка " + fileReader.getLineNumber() +
                            ": не корректно указан статус задачи!");
                }

                switch (splitLine[orderOfFields[0]]) {
                    case "TASK" -> {
                        Task task = new Task(id, splitLine[orderOfFields[2]], splitLine[orderOfFields[3]], status);
                        fileBackedTaskManager.tasks.put(id, task);
                    }
                    case "EPIC" -> {
                        Epic epic = new Epic(id, splitLine[orderOfFields[2]], splitLine[orderOfFields[3]], status);
                        fileBackedTaskManager.epics.put(id, epic);
                    }
                    case "SUBTASK" -> {
                        // проверяем id эпика на корректность
                        int epicId;
                        try {
                            epicId = Integer.parseInt(splitLine[orderOfFields[5]]);
                            if (epicId <= 0) {
                                throw new ManagerSaveException("Строка " + fileReader.getLineNumber() +
                                        ": ID эпика подзадачи должен быть больше нуля!");
                            }
                        } catch (NumberFormatException e) {
                            throw new ManagerSaveException("Строка " + fileReader.getLineNumber() +
                                    ": ID эпика подзадачи должен быть числом!");
                        }

                        Subtask subtask = new Subtask(id, splitLine[orderOfFields[2]], splitLine[orderOfFields[3]], status, epicId);
                        fileBackedTaskManager.subtasks.put(id, subtask);
                    }
                    default -> throw new ManagerSaveException("Строка " + fileReader.getLineNumber() +
                            ": не корректно указан тип задачи!");
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла!");
        }

        fileBackedTaskManager.path = fileName;
        return fileBackedTaskManager;
    }

    /**
     * Добавление обычной задачи
     *
     * @param task задача
     */
    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    /**
     * Добавление Эпика
     *
     * @param epic задача
     */
    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    /**
     * Добавление подзадачи
     *
     * @param subtask подзадача
     */
    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    /**
     * Обновление обычной задачи
     *
     * @param newTask новая задача с верным идентификатором
     */
    @Override
    public void updateTask(Task newTask) {
        super.updateTask(newTask);
        save();
    }

    /**
     * Обновление эпика
     *
     * @param newEpic новая задача с верным идентификатором
     */
    @Override
    public void updateEpic(Epic newEpic) {
        super.updateEpic(newEpic);
        save();
    }

    /**
     * Обновление подзадачи
     *
     * @param newSubtask подзадача
     */
    @Override
    public void updateSubtask(Subtask newSubtask) {
        super.updateSubtask(newSubtask);
        save();
    }

    /**
     * Удаление обычной задачи по ID
     */
    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    /**
     * Удаление всех обычных задач
     */
    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    /**
     * Удаление эпика по ID
     */
    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    /**
     * Удаление всех эпиков
     */
    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    /**
     * Удаление подзадачи по ID
     *
     * @param id ID подзадачи
     */
    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    /**
     * Удаление всех подзадач
     */
    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    /**
     * Метод сохранения состояния менеджера в файл со всеми задачами
     */
    private void save() {
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(path, StandardCharsets.UTF_8))) {
            fileWriter.write("type,id,name,description,status,epic\n");
            for (Task task : getTasks()) {
                fileWriter.write(task.toCsvString());
            }
            for (Task task : getEpics()) {
                fileWriter.write(task.toCsvString());
            }
            for (Task task : getSubtasks()) {
                fileWriter.write(task.toCsvString());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка записи файла!");
        }
    }
}
