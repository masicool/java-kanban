import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class TaskManager {
    private int taskId; // уникальный идентификатор задачи
    private HashMap<Integer, Task> tasks; // список задач

    /**
     * Генерация нового идентификатора задачи
     *
     * @return ID задачи
     */
    public int getNextTaskId() {
        return ++taskId;
    }

    /**
     * Конструктор объекта
     */
    public TaskManager() {
        taskId = 0;
        tasks = new HashMap<>();
    }

    /**
     * Проверка наличия задачи в Хэш-таблице
     * По переопределенному методу equals() в классе Task задачи считаются равными если
     * у них одинаковые наименования и описания, id задачи не учитывается
     *
     * @param task задача
     * @return true - если задача найдена, иначе - false
     */
    public boolean isTaskExist(Task task) {
        Collection<Task> tasks = this.tasks.values();
        for (Task item : tasks) {
            if (item.equals(task)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Вспомогательный метод добавления (или обновления) задачи
     *
     * @param taskId ID задачи
     * @param task   задача
     */
    private void putTaskToHashMap(int taskId, Task task) {
        // если это подзадача, то нужно в Эпике добавить эту подзадачу и пересчитать статус
        if (task instanceof Subtask) {
            Epic epic = ((Subtask) task).getEpic();
            epic.addSubTask(task);
        }
        tasks.put(taskId, task);

    }

    /**
     * Добавление новой задачи
     *
     * @param task задача
     */
    public void addTask(Task task) {
        // проверим, была ли уже такая задача, если была, то ничего не делаем
        if (isTaskExist(task)) {
            return;
        }

        task.setId(getNextTaskId());
        putTaskToHashMap(task.getId(), task);
    }

    /**
     * обновление задачи по идентификатору ID
     *
     * @param taskId  идентификатор задачи
     * @param newTask новая задача
     */
    public void updateTaskById(int taskId, Task newTask) {
        if (!tasks.containsKey(taskId)) { // если нет задачи с заданным ID, то возврат
            return;
        }
        newTask.setId(taskId);
        // так как тип обновляемой задачи может не совпадать с существующей и
        // даже если обновляется подзадача, у нее может быть другой Эпик
        // или был Эпик, а стала подзадача или просто задача, ТО
        // удалим текущую строку Хэш-таблицы (в методе удаления проработаны все действия по
        // изменению статусов задач, добавления подзадач в Эпики и т.д.)
        deleteTask(taskId);
        // добавляем задачу в заданному ID
        putTaskToHashMap(taskId, newTask);
    }

    /**
     * Получение списка всех задач из Хэш-таблицы
     *
     * @return список задач
     */
    public Collection<Task> getAllTasks() {
        return tasks.values();
    }

    /**
     * Получение списка всех Эпиков
     *
     * @return список задач
     */
    public Collection<Task> getEpicTasks() {
        Collection<Task> epicTasks = new ArrayList<>();
        for (Task item : getAllTasks()) {
            if (item instanceof Epic) {
                epicTasks.add(item);
            }
        }
        return epicTasks;
    }

    /**
     * Получение списка всех подзадач из Хэш-таблицы вне зависимости от Эпика
     *
     * @return список задач
     */
    public Collection<Task> getSubtasks() {
        Collection<Task> subTasks = new ArrayList<>();
        for (Task item : getAllTasks()) {
            if (item instanceof Subtask) {
                subTasks.add(item);
            }
        }
        return subTasks;
    }

    /**
     * Получение списка подзадач Эпика
     *
     * @param epic Эпик
     * @return список подзадач
     */
    public Collection<Task> getSubtasks(Epic epic) {
        return epic.getSubTasks();
    }

    /**
     * Получение списка подзадач Эпика с заданным ID
     *
     * @param epicId ID эпика
     * @return список задач
     */
    public Collection<Task> getSubtasks(int epicId) {
        if (tasks.containsKey(epicId) && tasks.get(epicId) instanceof Epic task) {
            return task.getSubTasks();
        }
        return null;
    }

    /**
     * Получение списка обычных задач (не Эпиков и не подзадач) из Хэш-таблицы
     *
     * @return список задач
     */
    public Collection<Task> getTasks() {
        Collection<Task> tasks = new ArrayList<>();
        for (Task item : getAllTasks()) {
            if (item.getClass() == Task.class) {
                tasks.add(item);
            }
        }
        return tasks;
    }

    /**
     * Получение задачи по ID
     *
     * @param taskId ID задачи
     * @return задача
     */
    public Task getTaskById(int taskId) {
        return tasks.get(taskId);
    }

    /**
     * Удаление всех задач
     */
    public void deleteAllTasks() {
        tasks.clear();
    }

    /**
     * Удаление задачи по указанному ID
     * (при удалении Эпика - удаляются все подзадачи, при удалении подзадачи - обновляются подзадачи в Эпике)
     *
     * @param taskId ID задачи
     */
    public void deleteTask(int taskId) {
        if (!tasks.containsKey(taskId)) {
            return;
        }
        Task task = tasks.get(taskId);
        // если удаляемая задача - Эпик, то удаляем все ее подзадачи
        if (task instanceof Epic) {
            Epic epic = (Epic) task;
            ArrayList<Task> subTasks = epic.getSubTasks();
            for (Task item : subTasks) {
                tasks.remove(item.getId());
            }
        }
        // если удаляемая задача - подзадача Эпика, то ее нужно удалить из списка подзадач Эпика
        else if (task instanceof Subtask) {
            Epic epic = ((Subtask) task).getEpic();
            epic.deleteSubtask(task);
        }
        tasks.remove(taskId);
    }
}
