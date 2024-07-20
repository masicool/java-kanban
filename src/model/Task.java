package model;

import java.util.Objects;

public class Task {
    private int id; // уникальный идентификатор задачи
    private String name; // название задачи
    private String description; // описание задачи
    private Status status; // статус задачи

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
     * Конструктор объекта с установкой всех полей
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
     * Конструктор для глубокого копирования объекта
     *
     * @param task объект
     */
    public Task(Task task) {
        this(task.getName(), task.getDescription());
        this.status = task.getStatus();
        this.id = task.getId();
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
     * Установка ID задачи
     *
     * @param id ID задачи
     */
    public void setId(int id) {
        this.id = id;
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
}
