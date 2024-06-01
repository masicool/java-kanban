import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Task> subTasks; // список подзадач Эпика

    /**
     * Конструктор объекта
     *
     * @param name        название задачи
     * @param description описания
     */
    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        subTasks = new ArrayList<>();
    }

    /**
     * Получение списка всех подзадач Эпика
     *
     * @return список подзадач Эпика
     */
    public ArrayList<Task> getSubTasks() {
        return subTasks;
    }

    /**
     * Добавление подзадачи Эпика
     *
     * @param task подзадача
     */
    public void addSubTask(Task task) {
        if (!subTasks.contains(task)) {
            subTasks.add(task);
        }
        updateStatus();
    }

    /**
     * Обновление статуса Эпика
     */
    public void updateStatus() {
        int countStatusNew = 0;
        int countStatusDone = 0;
        for (Task item : subTasks) {
            switch (item.getStatus()) {
                case NEW -> countStatusNew++;
                case DONE -> countStatusDone++;
            }
        }
        if (subTasks.isEmpty() || countStatusNew == subTasks.size()) {
            setStatus(Status.NEW);
        } else if (countStatusDone == subTasks.size()) {
            setStatus(Status.DONE);
        } else {
            setStatus(Status.IN_PROGRESS);
        }
    }

    /**
     * Удаление подзадачи Эпика с пересчетом статуса Эпика
     *
     * @param task задача
     */
    public void deleteSubtask(Task task) {
        subTasks.remove(task);
        updateStatus();
    }

    /**
     * Переопределенный метод
     *
     * @return строка в форматированном виде
     */
    @Override
    public String toString() {
        String subTasksIdToStr = "[";
        for (int i = 0; i < subTasks.size(); i++) {
            subTasksIdToStr += subTasks.get(i).getId();
            if (i != subTasks.size() - 1) {
                subTasksIdToStr += ", ";
            }
        }
        subTasksIdToStr += "]";

        return super.toString() +
                ", subTasksId=" + subTasksIdToStr +
                "}";
    }
}
