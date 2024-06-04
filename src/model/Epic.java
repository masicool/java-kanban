package model;

import java.util.Arrays;
import java.util.HashSet;

public class Epic extends Task {
    private final HashSet<Integer> subTasksId; // список из ID подзадач Эпика

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
     * @param subtaskId подзадача
     */
    public void addSubtaskId(int subtaskId) {
        subTasksId.add(subtaskId);
    }

    /**
     * Удаление подзадачи эпика по ID
     *
     * @param subtaskId ID подзадачи
     */
    public void deleteSubtaskId(int subtaskId) {
        subTasksId.remove(subtaskId);
    }

    /**
     * Удаление подзадачи Эпика с пересчетом статуса Эпика
     *
     * @param subtaskId ID подзадачи
     */
    public void deleteSubtask(int subtaskId) {
        subTasksId.remove(subtaskId);
    }

    /**
     * Переопределенный метод
     *
     * @return строка в форматированном виде
     */
    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subTasksId=" + Arrays.toString(subTasksId.toArray()) + "}";
    }
}
