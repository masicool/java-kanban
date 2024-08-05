package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class Subtask extends Task {
    private int epicId;

    /**
     * Конструктор объекта
     *
     * @param epicId      Эпик
     * @param name        имя подзадачи
     * @param description описание подзадачи
     */
    public Subtask(int epicId, String name, String description) {
        super(name, description);
        this.epicId = epicId;
    }

    /**
     * Конструктор объекта
     *
     * @param epic        Эпик
     * @param name        имя подзадачи
     * @param description описание подзадачи
     */
    public Subtask(Epic epic, String name, String description) {
        super(name, description);
        this.epicId = epic.getId();
    }

    /**
     * Конструктор объекта
     *
     * @param epic        Эпик
     * @param name        имя подзадачи
     * @param description описание подзадачи
     */
    public Subtask(Epic epic, String name, String description, Status status) {
        super(name, description, status);
        this.epicId = epic.getId();
    }

    /**
     * Конструктор объекта
     *
     * @param epic        Эпик
     * @param name        имя подзадачи
     * @param description описание подзадачи
     */
    public Subtask(Epic epic, String name, String description, Status status, LocalDateTime startTime,
                   Duration duration) {
        super(name, description, status, startTime, duration);
        this.epicId = epic.getId();
    }

    /**
     * Конструктор объекта с установкой всех полей
     *
     * @param id          id подзадачи
     * @param name        имя подзадачи
     * @param description описание подзадачи
     * @param status      статус подзадачи
     * @param epicId      id Эпика
     * @param startTime   время начала задачи
     * @param duration    продолжительность задачи
     */
    public Subtask(int id, String name, String description, Status status, int epicId, LocalDateTime startTime,
                   Duration duration) {
        super(id, name, description, status, startTime, duration);
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    /**
     * Получение ID эпика
     *
     * @return Эпик
     */
    public int getEpicId() {
        return epicId;
    }

    /**
     * Задание ID эпика
     *
     * @return true - успешно, false - ошибка указания id эпика
     */
    public boolean setEpicId(int epicId) {
        if (epicId <= 0 || epicId == getId()) {
            return false;
        }
        this.epicId = epicId;
        return true;
    }

    /**
     * Переопределенный метод для форматированного вывода
     *
     * @return форматированная строка вывода описания объекта
     */
    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                ", startTime=" + getStartTime() +
                ", duration=" + getDuration() +
                '}';
    }

    @Override
    public String toCsvString() {
        String startTime = getStartTime() != null ? "" + getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli() : "";
        String duration = getDuration() != null ? "" + getDuration().toMinutes() : "";

        return "SUBTASK," +
                getId() + "," +
                getName() + "," +
                getDescription() + "," +
                getStatus() + "," +
                epicId + "," +
                startTime + "," +
                duration + ",\n";
    }
}
