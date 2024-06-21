import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import service.InMemoryTaskManager;
import service.Managers;
import service.TaskManager;

import java.util.Collection;

public class Main {

    public static void main(String[] args) {
        Managers managers = new Managers();
        TaskManager taskManager = managers.getDefault();

        Task task;
        Epic epic;
        Subtask subtask;

        task = new Task("Почистить ковер", "Отвезти в химчистку Ковер-33");
        taskManager.addTask(task);
        epic = new Epic("Переезд", "Переезд на новую квартиру");
        taskManager.addEpic(epic);
        subtask = new Subtask(epic, "Грузчики", "Найти грузчиков");
        taskManager.addSubtask(subtask);
        subtask = new Subtask(epic, "Кот", "Поймать кота, упаковать");
        taskManager.addSubtask(subtask);
        subtask = new Subtask(epic, "Кот", "Поймать кота, упаковать");
        taskManager.addSubtask(subtask);
        epic = new Epic("Помыть окна", "Помыть окна после зимы");
        taskManager.addEpic(epic);
        subtask = new Subtask(epic, "Средство", "Купить средство для стекол");
        taskManager.addSubtask(subtask);
        task = new Task("АЗС", "Заправить авто");
        taskManager.addTask(task);

        System.out.println("\nСписок всех обычных задач:");
        printTasks(taskManager.getTasks());
        System.out.println("Список эпиков:");
        printTasks(taskManager.getEpics());
        System.out.println("Список всех подзадач:");
        printTasks(taskManager.getSubtasks());

        System.out.println("Обновили подзадачу с ID = 4:");
        subtask = new Subtask(2, "Кот", "Поймать кота, упаковать");
        subtask.setId(4);
        taskManager.updateSubtask(subtask);
        System.out.println(taskManager.getSubtaskById(4));
        System.out.println(taskManager.getEpicById(2));

        System.out.println("Список подзадач эпика с ID = 2:");
        epic = taskManager.getEpicById(2);
        printTasks(taskManager.getEpicSubtasks(epic));

        System.out.println("Удалили задачу с ID = 7");
        taskManager.deleteTaskById(1);
        printTasks(taskManager.getTasks());

        System.out.println("Изменим подзадачу с ID = 3 -> поменялся статус Эпика с ID = 2:");
        subtask = taskManager.getSubtaskById(3);
        subtask.setName("Грузчики");
        subtask.setDescription("Заплатит грузчикам");
        subtask.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask);
        System.out.println(taskManager.getSubtaskById(3));
        System.out.println(taskManager.getEpicById(2));

        System.out.println("Удалили Эпик с ID = 6 (удалились все подзадачи эпика):");
        taskManager.deleteEpicById(6);
        printTasks(taskManager.getSubtasks());

        System.out.println("Удалили все эпики (удалились и все подзадачи):");
        taskManager.deleteEpics();
        printTasks(taskManager.getTasks());
        printTasks((taskManager.getEpics()));
        printTasks(taskManager.getSubtasks());
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
