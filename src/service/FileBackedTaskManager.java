package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class FileBackedTaskManager extends InMemoryTaskManager {
    public static final int NUMBER_OF_FIELDS_IN_CSV_FILE = 6; // максимальное кол-во полей в файле CSV
    public static final String CSV_SEPARATOR = ","; // разделитель между полями файла CSV
    private String path; // путь и наименование файла для сохранения

    FileBackedTaskManager(String fileName) {
        if (!fileName.isBlank()) {
            setPath(fileName);
        }
    }

    // еще один main для тестирования класса
    public static void main(String[] args) throws IOException {
        FileBackedTaskManager taskManager = new FileBackedTaskManager("tasks.csv");

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
        Files.copy(Path.of("tasks.csv"), Path.of("taskscopy.csv"), StandardCopyOption.REPLACE_EXISTING);
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
        if (Files.notExists(Paths.get(fileName))) {
            return null;
        }

        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager("");

        try (BufferedReader fileReader = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8))) {
            // чтение заголовка файла (первой строки)
            String firstLine = fileReader.readLine();
            String[] split = firstLine.split(CSV_SEPARATOR);
            int[] orderOfFields = new int[NUMBER_OF_FIELDS_IN_CSV_FILE]; // порядок полей в файле

            // заполним массив порядка полей в файле начальным значением -1 для последующей проверки
            Arrays.fill(orderOfFields, -1);

            // порядок полей в файле CSV может быть разный, но количество полей должно совпадать
            for (int i = 0; i < split.length; i++) {
                switch (split[i]) {
                    case "type" -> orderOfFields[0] = i;
                    case "id" -> orderOfFields[1] = i;
                    case "name" -> orderOfFields[2] = i;
                    case "description" -> orderOfFields[3] = i;
                    case "status" -> orderOfFields[4] = i;
                    case "epic" -> orderOfFields[5] = i;
                    default -> System.out.println("Ошибка в формате файла CSV!");
                }
            }

            // порядок следования каждого поля в файле должен быть установлен, иначе ошибка формата
            for (int orderOfField : orderOfFields) {
                if (orderOfField == -1) {
                    System.out.println("Ошибка в формате файла CSV!");
                    break;
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
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка в формате файла CSV!");
                    return null;
                }

                // проверяем поле статуса на корректность
                Status status = null;
                switch (splitLine[orderOfFields[4]]) {
                    case "NEW" -> status = Status.NEW;
                    case "IN_PROGRESS" -> status = Status.IN_PROGRESS;
                    case "DONE" -> status = Status.DONE;
                    default -> System.out.println("Ошибка в формате файла CSV!");
                }

                switch (splitLine[orderOfFields[0]]) {
                    case "TASK" ->
                            fileBackedTaskManager.addTask(new Task(id, splitLine[orderOfFields[2]], splitLine[orderOfFields[3]], status));
                    case "EPIC" ->
                            fileBackedTaskManager.addEpic(new Epic(id, splitLine[orderOfFields[2]], splitLine[orderOfFields[3]], status));
                    case "SUBTASK" -> {
                        // проверяем id эпика на корректность
                        int epicId;
                        try {
                            epicId = Integer.parseInt(splitLine[orderOfFields[5]]);
                            if (epicId <= 0) {
                                throw new NumberFormatException();
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Ошибка в формате файла CSV!");
                            return null;
                        }

                        fileBackedTaskManager.addSubtask(new Subtask(id, splitLine[orderOfFields[2]], splitLine[orderOfFields[3]], status, epicId));
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Произошла ошибка во время записи файла.");
        }

        fileBackedTaskManager.setPath(fileName);
        return fileBackedTaskManager;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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
        if (getPath() == null || getPath().isBlank()) return;

        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(getPath(), StandardCharsets.UTF_8))) {
            fileWriter.write("type,id,name,description,status,epic");
            fileWriter.newLine();
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
            System.out.println("Произошла ошибка во время записи файла.");
        }
    }
}
