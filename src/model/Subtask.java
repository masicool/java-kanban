package model;

public class Subtask extends Task {
    private final int epicId;

    /**
     * Конструктор объекта
     *
     * @param epicId      Эпик
     * @param name        имя подзадачи
     * @param description описание подзадачи
     * @param status      статус подзадачи
     */
    public Subtask(int epicId, String name, String description, Status status) {
        super(name, description, status);
        this.epicId = epicId;
    }

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
     * Получение ID эпика
     *
     * @return Эпик
     */
    public int getEpicId() {
        return epicId;
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
                ", epicId=" + epicId + "}";
    }
}
