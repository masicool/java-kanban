package service;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubtask(Subtask subtask);

    void updateTask(Task newTask);

    void updateEpic(Epic newEpic);

    void updateSubtask(Subtask newSubtask);

    List<Task> getTasks();

    Task getTaskById(int taskId);

    List<Epic> getEpics();

    Epic getEpicById(int epicId);

    List<Subtask> getSubtasks();

    Subtask getSubtaskById(int subtaskId);

    List<Subtask> getEpicSubtasks(Epic epic);

    void deleteTaskById(int id);

    void deleteTasks();

    void deleteEpicById(int id);

    void deleteEpics();

    void deleteSubtaskById(int id);

    void deleteSubtasks();

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();
}
