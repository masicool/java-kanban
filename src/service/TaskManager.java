package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class TaskManager {
    private int taskId; // уникальный идентификатор задачи
    private final HashMap<Integer, Task> tasks; // список обычных задач
    private final HashMap<Integer, Epic> epics; // список эпиков
    private final HashMap<Integer, Subtask> subtasks; // список подзадач

    /**
     * Генерация нового идентификатора задачи
     *
     * @return ID задачи
     */
    public int getNextId() {
        return ++taskId;
    }

    /**
     * Конструктор объекта
     */
    public TaskManager() {
        taskId = 0;
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    /**
     * Добавление обычной задачи
     *
     * @param task задача
     */
    public void addTask(Task task) {
        if (task == null) {
            return;
        }
        task.setId(getNextId());
        tasks.put(task.getId(), task);
    }

    /**
     * Добавление Эпика
     *
     * @param epic задача
     */
    public void addEpic(Epic epic) {
        if (epic == null) {
            return;
        }
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
    }

    /**
     * Добавление подзадачи
     *
     * @param subtask подзадача
     */
    public void addSubtask(Subtask subtask) {
        if ((subtask == null) || (!epics.containsKey(subtask.getEpicId()))) {
            return;
        }
        subtask.setId(getNextId());
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
    public Collection<Task> getTasks() {
        return tasks.values();
    }

    /**
     * Получение обычной задачи по ID
     *
     * @param taskId ID задачи
     * @return задача
     */
    public Task getTaskById(int taskId) {
        return tasks.get(taskId);
    }

    /**
     * Получение списка эпиков
     *
     * @return список задач
     */
    public Collection<Epic> getEpics() {
        return epics.values();
    }

    /**
     * Получение эпика по ID
     *
     * @return список задач
     */
    public Epic getEpicById(int epicId) {
        return epics.get(epicId);
    }

    /**
     * Получение списка всех подзадач
     *
     * @return список задач
     */
    public Collection<Subtask> getSubtasks() {
        return subtasks.values();
    }

    /**
     * Получение подзадачи по ID
     *
     * @return список задач
     */
    public Subtask getSubtaskById(int subtaskId) {
        return subtasks.get(subtaskId);
    }

    /**
     * Получение списка подзадач Эпика
     *
     * @param epic Эпик
     * @return список подзадач
     */
    public Collection<Subtask> getEpicSubtasks(Epic epic) {
        if ((epic == null) || (epic.getSubtasksId().isEmpty())) {
            return null;
        }
        ArrayList<Subtask> epicSubtasks = new ArrayList<>();
        for (int subtaskId : epic.getSubtasksId()) {
            epicSubtasks.add(subtasks.get(subtaskId));
        }
        return epicSubtasks;
    }

    /**
     * Удаление обычной задачи по ID
     */
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    /**
     * Удаление всех обычных задач
     */
    public void deleteTasks() {
        tasks.clear();
    }

    /**
     * Удаление эпика по ID
     */
    public void deleteEpicById(int id) {
        if (!epics.containsKey(id)) {
            return;
        }
        Epic epic = epics.get(id);
        // нужно удалить все подзадачи эпика вместе с самим эпиком
        for (int subtaskId : epic.getSubtasksId()) {
            subtasks.remove(subtaskId);
        }
        epics.remove(id);
    }

    /**
     * Удаление всех эпиков
     */
    public void deleteEpics() {
        epics.clear();
        // значит удаляем и все подзадачи
        subtasks.clear();
    }

    /**
     * Удаление подзадачи по ID
     *
     * @param id ID подзадачи
     */
    public void deleteSubtaskById(int id) {
        if (!subtasks.containsKey(id)) {
            return;
        }
        Subtask subtask = subtasks.get(id);
        Epic epic = epics.get(subtask.getEpicId());
        subtasks.remove(id);
        updateEpicStatus(epic);
    }

    /**
     * Удаление всех подзадач
     */
    public void deleteSubtasks() {
        subtasks.clear();
        // обновим статус всех эпиков
        for (Epic epic : epics.values()) {
            // при обновлении эпика, обновляется список подзадач эпика (несуществующие удаляются)
            updateEpicStatus(epic);
        }
    }

    /**
     * Обновление статуса эпика
     *
     * @param epic эпика
     */
    public void updateEpicStatus(Epic epic) {
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