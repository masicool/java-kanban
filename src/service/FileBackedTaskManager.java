package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileBackedTaskManager extends InMemoryTaskManager {
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
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(fileName);

        try (BufferedReader fileReader = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8))) {

            FileCsvUtils.checkHeader(fileReader); // проверяем заголовок файла

            // читаем построчно файл, разбираем строки и создаем задачи прямо в HashMap
            int id = Integer.MIN_VALUE;
            while (fileReader.ready()) {
                Task task = FileCsvUtils.fromString(fileReader.readLine());
                int taskId = task.getId();
                switch (task.getType()) {
                    case TASK -> fileBackedTaskManager.tasks.put(taskId, task);
                    case EPIC -> fileBackedTaskManager.epics.put(taskId, (Epic) task);
                    case SUBTASK -> {
                        Subtask subtask = (Subtask) task;
                        fileBackedTaskManager.subtasks.put(taskId, subtask);
                        Epic epic = fileBackedTaskManager.epics.get(subtask.getEpicId());
                        epic.addSubtaskId(taskId); // в эпике нужно добавить подзадачу
                    }
                }
                // обновим счетчик ID в менеджере до актуального значения
                id = Math.max(id, taskId);
                fileBackedTaskManager.taskId = id;
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла!");
        }

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
