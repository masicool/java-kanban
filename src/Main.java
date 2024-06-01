import java.util.Collection;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task;
        Epic epic;
        Subtask subtask;

        task = new Task("Почистить ковер", "Отвезти в химчистку Ковер-33", Status.NEW);
        taskManager.addTask(task);
        epic = new Epic("Переезд", "Переезд на новую квартиру");
        taskManager.addTask(epic);
        subtask = new Subtask(epic, "Грузчики", "Найти грузчиков", Status.NEW);
        taskManager.addTask(subtask);
        subtask = new Subtask(epic, "Кот", "Поймать кота, упаковать", Status.NEW);
        taskManager.addTask(subtask);
        subtask = new Subtask(epic, "Кот", "Поймать кота, упаковать", Status.NEW);
        taskManager.addTask(subtask);
        epic = new Epic("Помыть окна", "Помыть окна после зимы");
        taskManager.addTask(epic);
        subtask = new Subtask(epic, "Средство", "Купить средство для стекол", Status.NEW);
        taskManager.addTask(subtask);
        task = new Task("АЗС", "Заправить авто", Status.NEW);
        taskManager.addTask(task);

        System.out.println("\nСписок всех задач:");
        printTasks(taskManager.getAllTasks());
        System.out.println();

        subtask = new Subtask(epic, "Кот", "Поймать кота, упаковать", Status.IN_PROGRESS);
        taskManager.updateTaskById(4, subtask);
        System.out.println("Обновили задачу с ID = 4:");
        System.out.println(taskManager.getTaskById(4));
        System.out.println();

        // сменим Эпик у подзадачи


        System.out.println("Список эпиков:");
        printTasks(taskManager.getEpicTasks());
        System.out.println("Список всех подзадач:");
        printTasks(taskManager.getSubtasks());
        System.out.println("Список подзадач эпика с ID = 2:");
        printTasks(taskManager.getSubtasks(2));
        System.out.println("Список подзадач указанного эпика с ID = 5:");
        epic = (Epic) taskManager.getTaskById(5);
        printTasks(taskManager.getSubtasks(epic));
        System.out.println("Список всех обычных задач:");
        printTasks(taskManager.getTasks());
        System.out.println("Удалили задачу с ID = 7");
        taskManager.deleteTask(7);
        printTasks(taskManager.getAllTasks());

        System.out.println("Меняем статус подзадачи с ID = 3, поменялся статус Эпика с ID = 2:");
        subtask = (Subtask) taskManager.getTaskById(3);
        taskManager.updateTaskById(3, new Subtask(subtask.getEpic(), "Грузчики",
                "Заплатить грузчикам", Status.IN_PROGRESS));
        System.out.println(taskManager.getTaskById(2));
        System.out.println("Удалили Эпик с ID = 5 (удалились все подзадачи):");
        taskManager.deleteTask(5);
        printTasks(taskManager.getAllTasks());

        System.out.println("Удалили все задачи, выводим список:");
        taskManager.deleteAllTasks();
        printTasks(taskManager.getAllTasks());
    }

    /**
     * Метод для более наглядного вывода списков в отличие от реализации toString в Collection
     *
     * @param tasks список задач
     */
    public static void printTasks(Collection<Task> tasks) {
        for (Task item : tasks) {
            System.out.println(item);
        }

    }
}
