package model;

public class Subtask extends Task {
    private int epicId;

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
     * Конструктор для глубокого копирования объекта
     *
     * @param subtask объект
     */
    public Subtask(Subtask subtask) {
        super(subtask.getName(), subtask.getDescription(), subtask.getStatus());
        this.epicId = subtask.epicId;
        super.setId(subtask.getId());
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
                ", epicId=" + epicId + "}";
    }

    @Override
    public String toCsvString() {
        return "SUBTASK," +
                getId() + "," +
                getName() + "," +
                getDescription() + "," +
                getStatus() + "," +
                epicId + "\n";
    }
}
