package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

public class Epic extends Task {
    private final HashSet<Integer> subTasksId; // список из ID подзадач Эпика
    private LocalDateTime endTime; // продолжительность выполнения эпика

    /**
     * Конструктор объекта
     *
     * @param name        название задачи
     * @param description описания
     */
    public Epic(String name, String description) {
        super(name, description);
        subTasksId = new HashSet<>();
    }

    /**
     * Конструктор объекта
     *
     * @param name        название задачи
     * @param description описания
     * @param status      описания
     */
    public Epic(String name, String description, Status status) {
        super(name, description, status);
        subTasksId = new HashSet<>();
    }

    /**
     * Конструктор объекта
     *
     * @param id          id задачи
     * @param name        название задачи
     * @param description описания
     * @param status      описания
     */
    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
        subTasksId = new HashSet<>();
    }

    /**
     * Конструктор объекта
     *
     * @param id          id задачи
     * @param name        название задачи
     * @param description описания
     * @param status      описания
     * @param startTime   время начала задачи
     * @param duration    продолжительность задачи
     * @param endTime     время окончания задачи
     */
    public Epic(int id, String name, String description, Status status, LocalDateTime startTime, Duration duration, LocalDateTime endTime) {
        super(id, name, description, status, startTime, duration);
        subTasksId = new HashSet<>();
        this.endTime = endTime;
    }

    /**
     * Конструктор для глубокого копирования объекта
     *
     * @param epic объект
     */
    public Epic(Epic epic) {
        super(epic.getName(), epic.getDescription(), epic.getStatus());
        super.setId(epic.getId());
        subTasksId = new HashSet<>();
        subTasksId.addAll(epic.getSubtasksId());
        endTime = epic.endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Получение списка всех ID подзадач Эпика
     *
     * @return список подзадач Эпика
     */
    public HashSet<Integer> getSubtasksId() {
        return subTasksId;
    }

    /**
     * Добавление подзадачи эпика
     *
     * @param subtaskId ID подзадачи
     * @return true - если подзадача добавлена, false - если ошибка добавления
     */
    public boolean addSubtaskId(int subtaskId) {
        if (subtaskId <= 0 || getId() == subtaskId) {
            return false;
        }
        if (subTasksId.contains(subtaskId)) {
            return false;
        }
        subTasksId.add(subtaskId);
        return true;
    }

    /**
     * Удаление подзадачи эпика по ID
     *
     * @param subtaskId ID подзадачи
     * @return true - если подзадача удалена, false - если ошибка удаления
     */
    public boolean deleteSubtaskId(int subtaskId) {
        if (!subTasksId.contains(subtaskId)) {
            return false;
        }
        subTasksId.remove(subtaskId);
        return true;
    }

    /**
     * Переопределенный метод
     *
     * @return строка в форматированном виде
     */
    @Override
    public String toString() {
        return "Epic{" + "id=" + getId() + ", name='" + getName() + '\'' + ", description='" + getDescription() + '\'' + ", status=" + getStatus() + ", subTasksId=" + Arrays.toString(subTasksId.toArray()) + "}";
    }

    @Override
    public String toCsvString() {
        return "EPIC," + getId() + "," + getName() + "," + getDescription() + "," + getStatus() + ",\n";
    }
}
