package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private int id; // уникальный идентификатор задачи
    private String name; // название задачи
    private String description; // описание задачи
    private Status status; // статус задачи
    private LocalDateTime startTime; // дата и время начала выполнения задачи
    private Duration duration; // продолжительность задачи в минутах

    /**
     * Конструктор объекта
     *
     * @param name        наименование задачи
     * @param description описание задачи
     */
    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
    }

    /**
     * Конструктор объекта с установкой статуса
     *
     * @param name        наименование задачи
     * @param description описание задачи
     * @param status      статус задачи
     */
    public Task(String name, String description, Status status) {
        this(name, description);
        this.status = status;
    }

    /**
     * Конструктор объекта с установкой статуса, времени начала и продолжительности задачи
     *
     * @param name        наименование задачи
     * @param description описание задачи
     * @param status      статус задачи
     * @param startTime   время начала задачи
     * @param duration    продолжительность задачи
     */
    public Task(String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        this(name, description);
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
    }

    /**
     * Конструктор объекта с установкой id
     *
     * @param id          id задачи
     * @param name        наименование задачи
     * @param description описание задачи
     * @param status      статус задачи
     */
    public Task(int id, String name, String description, Status status) {
        this(name, description, status);
        this.id = id;
    }

    /**
     * Конструктор объекта с установкой всех полей
     *
     * @param id          id задачи
     * @param name        наименование задачи
     * @param description описание задачи
     * @param status      статус задачи
     * @param startTime   время начала задачи
     * @param duration    продолжительность задачи
     */
    public Task(int id, String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        this(id, name, description, status);
        this.startTime = startTime;
        this.duration = duration;
    }

    /**
     * Конструктор для глубокого копирования объекта
     *
     * @param task объект
     */
    public Task(Task task) {
        this(task.getName(), task.getDescription());
        this.status = task.getStatus();
        this.id = task.getId();
        this.duration = task.duration;
        this.startTime = task.startTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Получение статуса задачи
     *
     * @return статус задачи
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Установка статуса задачи
     *
     * @param status новый статус
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Получение типа задачи
     *
     * @return типа задачи
     */
    public TaskType getType() {
        return TaskType.TASK;
    }

    /**
     * Переопределенный метод сравнения двух объектов
     * считаются равными если у них одинаковые наименования и описания, id задачи не учитывается
     *
     * @param o объект
     * @return true - если объекты равны, иначе - false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        // считается что объекты равны друг другу если у них одинаковые ID
        return id == task.id;
    }

    /**
     * Переопределенный метод
     *
     * @return Хэш-код
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }

    /**
     * Получение ID задачи
     *
     * @return ID задачи
     */
    public int getId() {
        return id;
    }

    /**
     * Установка ID задачи
     *
     * @param id ID задачи
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Переопределенный метод вывода представления объекта
     *
     * @return форматирования строка
     */
    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status + "}";
    }

    public String toCsvString() {
        return "TASK," +
                getId() + "," +
                getName() + "," +
                getDescription() + "," +
                getStatus() + ",\n";
    }

    /**
     * Расчет даты и времени завершения задачи
     *
     * @return дата и время завершения
     */
    public LocalDateTime getEndTime() {
        return startTime.plusMinutes(duration.toMinutes());
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }
}
