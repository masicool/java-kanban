public class Subtask extends Task {
    private Epic epic;

    public Subtask(Epic epic, String name, String description, Status status) {
        super(name, description, status);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", epicId=" + epic.getId() +
                "}";
    }
}
