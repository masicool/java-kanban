package service;

import exception.TaskValidateException;
import model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks; // список обычных задач
    protected final HashMap<Integer, Epic> epics; // список эпиков
    protected final HashMap<Integer, Subtask> subtasks; // список подзадач
    private final HistoryManager historyManager; // объект класса для работы с историей просмотров
    private final Set<Task> sortedTasks; // для хранения зада в отсортированном виде по времени начала в
    protected int taskId; // уникальный идентификатор задачи

    public InMemoryTaskManager() {
        taskId = 0;
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
        // компаратор для сортировки задач по времени начала
        Comparator<Task> startTimeComparator = Comparator.comparing(Task::getStartTime);
        sortedTasks = new TreeSet<>(startTimeComparator);
    }

    /**
     * Добавление обычной задачи
     *
     * @param task задача
     */
    @Override
    public void addTask(Task task) {
        if (task == null) return;
        validateAndAddToSortedTaskList(task);
        setId(task);
        tasks.put(task.getId(), task);
    }

    /**
     * Добавление Эпика
     *
     * @param epic задача
     */
    @Override
    public void addEpic(Epic epic) {
        if (epic == null) return;
        setId(epic);
        epics.put(epic.getId(), epic);
    }

    /**
     * Добавление подзадачи
     *
     * @param subtask подзадача
     */
    @Override
    public void addSubtask(Subtask subtask) {
        if ((subtask == null) || (!epics.containsKey(subtask.getEpicId()))) return;
        validateAndAddToSortedTaskList(subtask);
        setId(subtask);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic); // обновим статус эпика
        updateEpicTime(epic);
    }

    /**
     * Обновление обычной задачи
     *
     * @param task новая задача с верным идентификатором
     */
    @Override
    public void updateTask(Task task) {
        if ((task == null) || (!tasks.containsKey(task.getId()))) return;
        validateAndAddToSortedTaskList(task);
        tasks.replace(task.getId(), task);
    }

    /**
     * Обновление эпика
     *
     * @param epic новая задача с верным идентификатором
     */
    @Override
    public void updateEpic(Epic epic) {
        if ((epic == null) || (!epics.containsKey(epic.getId()))) { // если нет задачи с заданным ID, то возврат
            return;
        }

        // у нового эпика могу быть указаны подзадачи, если так, то проверим, существуют ли они
        // если хоть одной подзадачи нет, то возврат
        for (Integer subtask : epic.getSubtasksId()) {
            if (!subtasks.containsKey(subtask)) {
                return;
            }
            // также проверим, что подзадачи эпика ссылаются на него
            if (subtasks.get(subtask).getEpicId() != epic.getId()) {
                return;
            }
        }

        // сравним списки подзадач старого и нового эпика и оставим только задачи нового эпика, а другие удалим
        Epic oldEpic = epics.get(epic.getId());
        HashSet<Integer> oldSubtasksId = oldEpic.getSubtasksId();
        HashSet<Integer> newSubtasksId = epic.getSubtasksId();

        oldSubtasksId.stream().
                filter(oldSubtaskId -> !newSubtasksId.contains(oldSubtaskId)).
                forEach(oldSubtaskId -> {
                    sortedTasks.remove(subtasks.get(oldSubtaskId));
                    subtasks.remove(oldSubtaskId);
                });

        // обновим статус эпика
        updateEpicStatus(epic);
        // пересчитаем время выполнения подзадач эпика
        updateEpicTime(epic);
        // заменим эпик в мапе
        epics.replace(epic.getId(), epic);
    }

    /**
     * Обновление подзадачи
     *
     * @param subtask подзадача
     */
    @Override
    public void updateSubtask(Subtask subtask) {
        // если нет задачи с заданным ID, то возврат
        if ((subtask == null) || (!subtasks.containsKey(subtask.getId()))) return;

        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return; // если у подзадачи не указан эпик или он не правильно указан, то возврат

        validateAndAddToSortedTaskList(subtask);
        subtasks.replace(subtask.getId(), subtask);
        updateEpicStatus(epic); // обязательно обновим статус эпика
        updateEpicTime(epic); // обновить общее время выполнения задач эпика
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
        return subtasks.
                entrySet().
                stream().
                filter(entry -> epic.getSubtasksId().contains(entry.getKey())).
                map(Map.Entry::getValue).toList();
    }

    /**
     * Удаление обычной задачи по ID
     */
    @Override
    public void deleteTaskById(int id) {
        historyManager.remove(id); // удаление задачи из истории просмотров
        tasks.remove(id);
        sortedTasks.removeIf(task -> task.getId() == id);
    }

    /**
     * Удаление всех обычных задач
     */
    @Override
    public void deleteTasks() {
        tasks.keySet().forEach(historyManager::remove);
        sortedTasks.removeIf(task -> task.getType() == TaskType.TASK);
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
            sortedTasks.remove(subtasks.get(subtaskId));
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
        sortedTasks.removeIf(task -> task.getType() == TaskType.SUBTASK);
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
        sortedTasks.remove(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        subtasks.remove(id);
        updateEpicStatus(epic);
        updateEpicTime(epic);
    }

    /**
     * Удаление всех подзадач
     */
    @Override
    public void deleteSubtasks() {
        // удаление всех подзадач из истории просмотров
        subtasks.keySet().forEach(historyManager::remove);
        // удаление подзадач из сортированного списка
        sortedTasks.removeIf(task -> task.getType() == TaskType.SUBTASK);
        subtasks.clear();
        // обновим статус всех эпиков
        epics.values().forEach(epic -> {
            updateEpicStatus(epic);
            updateEpicTime(epic);
        });
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return sortedTasks;
    }

    /**
     * Генерация нового идентификатора задачи
     * с проверкой id по всем спискам
     *
     * @return ID задачи
     */
    private int getNextId() {
        while (tasks.containsKey(taskId + 1) || epics.containsKey(taskId + 1) || subtasks.containsKey(taskId + 1)) {
            taskId++;
        }
        return ++taskId;
    }

    // Генерация нового идентификатора задачи
    // с учетом того, что у задачи уже может быть задан ID
    private void setId(Task task) {
        // если у задачи задан ID, то используем этот ID при добавлении в список
        if (task.getId() != 0) return;
        task.setId(getNextId());
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

    /**
     * Проверка задачи на пересечение времени выполнения с другими задачами
     *
     * @param task - задачи или подзадача
     */
    private void validateAndAddToSortedTaskList(Task task) {
        if (task.getStartTime() != null) {
            if (isNotIntersectRanges(task)) {
                sortedTasks.add(task);
            } else {
                throw new TaskValidateException("Пересекается время с уже существующей задачей!");
            }
        }
    }

    private boolean isNotIntersectRanges(Task newTask) {
        return sortedTasks.stream().allMatch(task -> isNotIntersectRangesTwoTasks(task, newTask));
    }

    private boolean isNotIntersectRangesTwoTasks(Task task1, Task task2) {
        return task1.getStartTime().isAfter(task2.getEndTime()) || task1.getEndTime().isBefore(task2.getStartTime());
    }

    private void updateEpicTime(Epic epic) {
        LocalDateTime startTime = LocalDateTime.MAX;
        LocalDateTime endTime = LocalDateTime.MIN;
        Duration duration = Duration.ZERO;

        for (int subtasksId : epic.getSubtasksId()) {
            Subtask subtask = subtasks.get(subtasksId);
            if (subtask.getStartTime() == null) continue;
            if (subtask.getStartTime().isBefore(startTime)) startTime = subtask.getStartTime();
            if (subtask.getEndTime().isAfter(endTime)) endTime = subtask.getEndTime();
            duration = duration.plus(subtask.getDuration());
        }
        if (startTime != LocalDateTime.MAX && endTime != LocalDateTime.MIN) {
            epic.setStartTime(startTime);
            epic.setEndTime(endTime);
            epic.setDuration(duration);
        } else {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(null);
        }
    }
}
