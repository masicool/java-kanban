import model.Epic;
import model.Subtask;
import model.Task;
import service.Managers;
import service.TaskManager;

import java.util.Collection;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

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

        // запрашиваем задачи, эпики и подзадачи в произвольном порядке и выводим историю просмотров
        // убеждаемся, что нет дубликатов в истории просмотров
        System.out.println();
        taskManager.getTaskById(1);
        taskManager.getTaskById(2);
        taskManager.getTaskById(1);
        printTasks(taskManager.getHistory());
        System.out.println();
        taskManager.getEpicById(3);
        printTasks(taskManager.getHistory());
        System.out.println();
        taskManager.getEpicById(2);
        printTasks(taskManager.getHistory());
        System.out.println();
        taskManager.getSubtaskById(6);
        taskManager.getSubtaskById(4);
        taskManager.getSubtaskById(5);
        taskManager.getEpicById(7);
        printTasks(taskManager.getHistory());
        System.out.println();

        // удалим задачу, которая есть в истории и убедимся, что при выводе истории ее нет
        taskManager.deleteTaskById(2);
        printTasks(taskManager.getHistory());
        System.out.println();

        // удалим эпик с тремя подзадачами и убедимся, что из истории удалился этот эпик и его подзадачи
        taskManager.deleteEpicById(3);
        printTasks(taskManager.getHistory());
        System.out.println();
        printTasks(taskManager.getHistory());
    }

    /**
     * Метод для более наглядного вывода списков в отличие от реализации toString в Collection
     *
     * @param tasks список задач
     */
    public static <T> void printTasks(Collection<T> tasks) {
        if (tasks == null) {
            return;
        }
        for (T item : tasks) {
            System.out.println(item);
        }

    }
}
