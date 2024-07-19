package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileBackedTaskManager extends InMemoryTaskManager {
    public static void main(String[] args) {
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
        subtask = new Subtask(epic, "Кот", "Поймать кота, упаковать");
        taskManager.addSubtask(subtask); // id будет = 5
        subtask = new Subtask(epic, "Мебель", "Запаковать мебель");
        taskManager.addSubtask(subtask); // id будет = 6

        // создадим эпик без подзадач
        epic = new Epic("Помыть окна", "Помыть окна после зимы");
        taskManager.addEpic(epic); // id будет = 7

    }

    private String path;

    FileBackedTaskManager(String fileName) {
        setPath(fileName);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public static FileBackedTaskManager loadFromFile(String fileName) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(fileName);
        if (Files.notExists(Paths.get(fileBackedTaskManager.getPath()))) {
            return null;
        }

        // дописать чтение из файла

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
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(getPath(), StandardCharsets.UTF_8))) {
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
