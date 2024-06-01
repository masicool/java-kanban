public class Subtask extends Task {
    private Epic epic;

    /**
     * Конструктор объекта
     *
     * @param epic        Эпик
     * @param name        имя подзадачи
     * @param description описание подзадачи
     * @param status      статус подзадачи
     */
    public Subtask(Epic epic, String name, String description, Status status) {
        super(name, description, status);
        this.epic = epic;
    }

    /**
     * Получение Эпика
     *
     * @return Эпик
     */
    public Epic getEpic() {
        return epic;
    }

    /**
     * Переопределенный метод для форматированного вывода
     *
     * @return форматированная строка вывода описания объекта
     */
    @Override
    public String toString() {
        return super.toString() +
                ", epicId=" + epic.getId() +
                "}";
    }
}
