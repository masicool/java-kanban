package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int taskId; // уникальный идентификатор задачи
    private final HashMap<Integer, Task> tasks; // список обычных задач
    private final HashMap<Integer, Epic> epics; // список эпиков
    private final HashMap<Integer, Subtask> subtasks; // список подзадач
    private final HistoryManager historyManager; // объект класса для работы с историей просмотров

    public InMemoryTaskManager() {
        taskId = 0;
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
    }

    /**
     * Добавление обычной задачи
     *
     * @param task задача
     */
    @Override
    public void addTask(Task task) {
        if (task == null) {
            return;
        }
        if (!setId(task)) {
            return;
        }
        tasks.put(task.getId(), task);
    }

    /**
     * Добавление Эпика
     *
     * @param epic задача
     */
    @Override
    public void addEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        if (!setId(epic)) {
            return;
        }
        epics.put(epic.getId(), epic);
    }

    /**
     * Добавление подзадачи
     *
     * @param subtask подзадача
     */
    @Override
    public void addSubtask(Subtask subtask) {
        if ((subtask == null) || (!epics.containsKey(subtask.getEpicId()))) {
            return;
        }
        if (!setId(subtask)) {
            return;
        }
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic); // обновим статус эпика
    }

    /**
     * Обновление обычной задачи
     *
     * @param newTask новая задача с верным идентификатором
     */
    @Override
    public void updateTask(Task newTask) {
        if ((newTask == null) || (!tasks.containsKey(newTask.getId()))) { // если нет задачи с заданным ID, то возврат
            return;
        }
        tasks.replace(newTask.getId(), newTask);
    }

    /**
     * Обновление эпика
     *
     * @param newEpic новая задача с верным идентификатором
     */
    @Override
    public void updateEpic(Epic newEpic) {
        if ((newEpic == null) || (!epics.containsKey(newEpic.getId()))) { // если нет задачи с заданным ID, то возврат
            return;
        }

        // у нового эпика могу быть указаны подзадачи, если так, то проверим, существуют ли они
        // если хоть одной подзадачи нет, то возврат
        for (Integer subtask : newEpic.getSubtasksId()) {
            if (!subtasks.containsKey(subtask)) {
                return;
            }
            // также проверим, что подзадачи эпика ссылаются на него
            if (subtasks.get(subtask).getEpicId() != newEpic.getId()) {
                return;
            }
        }

        // сравним списки подзадач старого и нового эпика и оставим только задачи нового эпика, а другие удалим
        Epic oldEpic = epics.get(newEpic.getId());
        HashSet<Integer> oldSubtasksId = oldEpic.getSubtasksId();
        HashSet<Integer> newSubtasksId = newEpic.getSubtasksId();
        for (Integer oldSubtaskId : oldSubtasksId) {
            if (!newSubtasksId.contains(oldSubtaskId)) {
                subtasks.remove(oldSubtaskId);
            }
        }

        // обновим статус эпика
        updateEpicStatus(newEpic);
        // заменим эпик в мапе
        epics.replace(newEpic.getId(), newEpic);
    }

    /**
     * Обновление подзадачи
     *
     * @param newSubtask подзадача
     */
    @Override
    public void updateSubtask(Subtask newSubtask) {
        // если нет задачи с заданным ID, то возврат
        if ((newSubtask == null) || (!subtasks.containsKey(newSubtask.getId()))) {
            return;
        }
        Epic epic = epics.get(newSubtask.getEpicId());
        // если у подзадачи не указан эпик или он не правильно указан, то возврат
        if (epic == null) {
            return;
        }
        subtasks.replace(newSubtask.getId(), newSubtask);
        updateEpicStatus(epic); // обязательно обновим статус эпика
    }

    /**
     * Получение списка обычных задач
     *
     * @return список задач
     */
    @Override
    public Collection<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * Получение обычной задачи по ID
     *
     * @param taskId ID задачи
     * @return задача
     */
    @Override
    public Task getTaskById(int taskId) {
        Task task = tasks.get(taskId);
        historyManager.add(task);
        return task;
    }

    /**
     * Получение списка эпиков
     *
     * @return список задач
     */
    @Override
    public Collection<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    /**
     * Получение эпика по ID
     *
     * @return список задач
     */
    @Override
    public Epic getEpicById(int epicId) {
        Epic epic = epics.get(epicId);
        historyManager.add(epic);
        return epic;
    }

    /**
     * Получение списка всех подзадач
     *
     * @return список задач
     */
    @Override
    public Collection<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    /**
     * Получение подзадачи по ID
     *
     * @return список задач
     */
    @Override
    public Subtask getSubtaskById(int subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        historyManager.add(subtask);
        return subtask;
    }

    /**
     * Получение списка подзадач Эпика
     *
     * @param epic Эпик
     * @return список подзадач
     */
    @Override
    public Collection<Subtask> getEpicSubtasks(Epic epic) {
        if ((epic == null) || (epic.getSubtasksId().isEmpty())) {
            return null;
        }
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        for (int subtaskId : epic.getSubtasksId()) {
            epicSubtasks.add(subtasks.get(subtaskId));
        }
        return new ArrayList<>(epicSubtasks);
    }

    /**
     * Удаление обычной задачи по ID
     */
    @Override
    public void deleteTaskById(int id) {
        historyManager.remove(id); // удаление задачи из истории просмотров
        tasks.remove(id);
    }

    /**
     * Удаление всех обычных задач
     */
    @Override
    public void deleteTasks() {
        for (int taskId : tasks.keySet()) { // удаление всех задач из истории просмотров
            historyManager.remove(taskId);
        }
        tasks.clear();
    }

    /**
     * Удаление эпика по ID
     */
    @Override
    public void deleteEpicById(int id) {
        if (!epics.containsKey(id)) {
            return;
        }
        historyManager.remove(id); // удаление эпика из истории просмотров
        Epic epic = epics.get(id);
        // нужно удалить все подзадачи эпика вместе с самим эпиком
        for (int subtaskId : epic.getSubtasksId()) {
            historyManager.remove(subtaskId); // удаление подзадач эпика из истории просмотров
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    /**
     * Удаление всех эпиков
     */
    @Override
    public void deleteEpics() {
        // удаление всех эпиков из истории просмотров
        for (int epicId : epics.keySet()) {
            historyManager.remove(epicId);
        }

        epics.clear();

        // удаление всех подзадач из истории просмотров
        for (int subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }

        subtasks.clear(); // удаляем также и все подзадачи
    }

    /**
     * Удаление подзадачи по ID
     *
     * @param id ID подзадачи
     */
    @Override
    public void deleteSubtaskById(int id) {
        if (!subtasks.containsKey(id)) {
            return;
        }

        historyManager.remove(id); // удаление подзадачи из истории просмотров

        Subtask subtask = subtasks.get(id);
        Epic epic = epics.get(subtask.getEpicId());
        subtasks.remove(id);
        updateEpicStatus(epic);
    }

    /**
     * Удаление всех подзадач
     */
    @Override
    public void deleteSubtasks() {
        // удаление всех подзадач из истории просмотров
        for (int subtaskId : subtasks.keySet()) {
            historyManager.remove(subtaskId);
        }

        subtasks.clear();
        // обновим статус всех эпиков
        for (Epic epic : epics.values()) {
            // при обновлении эпика, обновляется список подзадач эпика (несуществующие удаляются)
            updateEpicStatus(epic);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    /**
     * Генерация нового идентификатора задачи
     * с проверкой id по всем спискам
     *
     * @return ID задачи
     */
    public int getNextId() {
        while (tasks.containsKey(taskId + 1) || epics.containsKey(taskId + 1)
                || subtasks.containsKey(taskId + 1)) {
            taskId++;
        }
        return ++taskId;
    }

    // Генерация нового идентификатора задачи
    // с учетом того, что у задачи уже может быть задан ID
    private boolean setId(Task task) {
        // если у задачи задан ID, то используем этот ID при добавлении в список
        if (task.getId() == 0) {
            task.setId(getNextId());
            return true;
        }
        // возврат false, если уже есть такой ID
        return !tasks.containsKey(task.getId());
    }

    /**
     * Обновление статуса эпика
     *
     * @param epic эпика
     */
    private void updateEpicStatus(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            return;
        }

        HashSet<Integer> epicSubtasksId = epic.getSubtasksId();

        int countStatusNew = 0;
        int countStatusDone = 0;
        for (int epicSubtaskId : epicSubtasksId) {
            if (!subtasks.containsKey(epicSubtaskId)) {
                // если не нашли подзадачу, то ее нужно удалить из списка подзадач эпика
                epic.deleteSubtaskId(epicSubtaskId);
                continue;
            }
            switch (subtasks.get(epicSubtaskId).getStatus()) {
                case Status.NEW -> countStatusNew++;
                case Status.DONE -> countStatusDone++;
            }
        }

        if (epicSubtasksId.isEmpty() || countStatusNew == epicSubtasksId.size()) {
            epic.setStatus(Status.NEW);
        } else if (countStatusDone == epicSubtasksId.size()) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}
