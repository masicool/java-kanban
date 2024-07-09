package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    static TaskManager taskManager;
    static Task task;
    static Epic epic;
    static Subtask subtask;

    @BeforeEach
    void beforeEach() {
        taskManager = new InMemoryTaskManager();
        task = new Task("Почистить ковер", "Отвезти в химчистку Ковер-33");
        taskManager.addTask(task);
        epic = new Epic("Переезд", "Переезд на новую квартиру");
        taskManager.addEpic(epic);
        subtask = new Subtask(epic, "Грузчики", "Найти грузчиков");
        taskManager.addSubtask(subtask);
    }

    @Test
    void addTaskEpicSubtaskAndCheckGeneratedId() {
        assertEquals(1, task.getId(), "Не сгенерировался ID задачи при добавлении");
        assertNotNull(taskManager.getTaskById(1), "Не сохранилась задача в списке");
        assertEquals(2, epic.getId(), "Не сгенерировался ID эпика при добавлении");
        assertNotNull(taskManager.getEpicById(2), "Не сохранился эпик в списке");
        assertEquals(2, subtask.getEpicId(), "Не указан ID эпика у подзадачи");
        assertEquals(3, subtask.getId(), "Не сгенерировался ID подзадачи при добавлении");
        assertNotNull(taskManager.getSubtaskById(3), "Не сохранилась подзадача в списке");
    }

    @Test
    void updateTask() {
        task.setName("Новое наименование задачи");
        task.setDescription("Новое описание задачи");
        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);
        assertEquals(task, taskManager.getTaskById(task.getId()), "Задача не обновилась");
    }

    @Test
    void getTaskEpicSubtaskById() {
        assertNotNull(taskManager.getTaskById(task.getId()), "Не найдена задача по ID");
        assertNotNull(taskManager.getEpicById(epic.getId()), "Не найден эпик по ID");
        assertNotNull(taskManager.getSubtaskById(subtask.getId()), "Не найдена подзадача по ID");
    }

    @Test
    void deleteTaskAndFindTaskById() {
        int tmpId = task.getId();
        taskManager.deleteTaskById(tmpId);
        assertNull(taskManager.getTaskById(tmpId), "Задача не удалена из списка задач");
    }

    @Test
    void deleteSubtask() {
        Subtask tmpSubtask = taskManager.getSubtaskById(3);
        Epic tmpEpic = taskManager.getEpicById(tmpSubtask.getEpicId());
        taskManager.deleteSubtaskById(3);
        Collection<Subtask> subtasks = taskManager.getEpicSubtasks(tmpEpic);
        assertFalse(subtasks != null && subtasks.contains(tmpSubtask), "При удалении подзадачи, она не удалилась в " +
                "эпике");
    }

    @Test
    void checkGeneratedIdAndManualId() {
        Task newTask = new Task("Пойти в ресторан", "Забронировать столик");
        newTask.setId(4); // установили ID в ручную
        taskManager.addTask(newTask);
        assertNotNull(taskManager.getTaskById(4), "Не сохранилась задача в списке");
        newTask = new Task("Пойти в ресторан еще раз", "Забронировать столик");
        taskManager.addTask(newTask); // ID генерируется
        assertNotNull(taskManager.getTaskById(5), "Не сохранилась задача в списке");
    }

    @Test
    void equalsAllFieldsWhenAddTask() {
        Task newTask = new Task("Пойти в ресторан", "Забронировать столик", Status.NEW);
        taskManager.addTask(newTask);
        assertEquals(taskManager.getTaskById(newTask.getId()).getName(), newTask.getName(),
                "Не сохранилось имя задачи в списке");
        assertEquals(taskManager.getTaskById(newTask.getId()).getDescription(), newTask.getDescription(),
                "Не сохранилось описание задачи в списке");
        assertEquals(taskManager.getTaskById(newTask.getId()).getStatus(), newTask.getStatus(),
                "Не сохранился статус задачи в списке");
    }

    @Test
    void shouldBeChangeStatusEpicWhenChangeStatusSubtask() {
        subtask.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask);
        Epic tmpEpic = taskManager.getEpicById(subtask.getEpicId());
        assertEquals(Status.DONE, tmpEpic.getStatus(), "Не изменился статус Эпика " +
                "при смене статуса подзадачи");
        subtask.setStatus(Status.NEW);
        taskManager.updateSubtask(subtask);
        tmpEpic = taskManager.getEpicById(subtask.getEpicId());
        assertEquals(Status.NEW, tmpEpic.getStatus(), "Не изменился статус Эпика " +
                "при смене статуса подзадачи");
    }

    @Test
    void getHistory() {
        getTaskEpicSubtaskById();
        assertNotNull(taskManager.getHistory(), "История задач не сохраняется");
    }
}