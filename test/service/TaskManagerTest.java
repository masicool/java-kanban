package service;

import exception.TaskValidateException;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    static Task task;
    static Epic epic;
    static Subtask subtask1;
    static Subtask subtask2;
    protected T taskManager;
    TaskValidateException thrown;

    @BeforeEach
    void beforeEach() {
        task = new Task("Почистить ковер", "Отвезти в химчистку Ковер-33");
        taskManager.addTask(task);
        epic = new Epic("Переезд", "Переезд на новую квартиру");
        taskManager.addEpic(epic);
        subtask1 = new Subtask(epic, "Грузчики", "Найти грузчиков");
        taskManager.addSubtask(subtask1);
    }

    @Test
    void addTaskEpicSubtaskAndCheckGeneratedId() {
        assertEquals(1, task.getId(), "Не сгенерировался ID задачи при добавлении");
        assertNotNull(taskManager.getTaskById(1), "Не сохранилась задача в списке");
        assertEquals(2, epic.getId(), "Не сгенерировался ID эпика при добавлении");
        assertNotNull(taskManager.getEpicById(2), "Не сохранился эпик в списке");
        assertEquals(2, subtask1.getEpicId(), "Не указан ID эпика у подзадачи");
        assertEquals(3, subtask1.getId(), "Не сгенерировался ID подзадачи при добавлении");
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
        assertNotNull(taskManager.getSubtaskById(subtask1.getId()), "Не найдена подзадача по ID");
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
        assertFalse(subtasks != null && subtasks.contains(tmpSubtask), "При удалении подзадачи, она не удалилась в " + "эпике");
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
        assertEquals(taskManager.getTaskById(newTask.getId()).getName(), newTask.getName(), "Не сохранилось имя задачи в списке");
        assertEquals(taskManager.getTaskById(newTask.getId()).getDescription(), newTask.getDescription(), "Не сохранилось описание задачи в списке");
        assertEquals(taskManager.getTaskById(newTask.getId()).getStatus(), newTask.getStatus(), "Не сохранился статус задачи в списке");
    }

    @Test
    void shouldBeChangeStatusEpicWhenChangeStatusSubtask() {
        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);
        Epic tmpEpic = taskManager.getEpicById(subtask1.getEpicId());
        assertEquals(Status.DONE, tmpEpic.getStatus(), "Не изменился статус Эпика " + "при смене статуса подзадачи");
        subtask1.setStatus(Status.NEW);
        taskManager.updateSubtask(subtask1);
        tmpEpic = taskManager.getEpicById(subtask1.getEpicId());
        assertEquals(Status.NEW, tmpEpic.getStatus(), "Не изменился статус Эпика " + "при смене статуса подзадачи");
    }

    @Test
    void getHistory() {
        getTaskEpicSubtaskById();
        assertNotNull(taskManager.getHistory(), "История задач не сохраняется");
    }

    // расчет статуса эпика
    @Test
    void addTwoSubtasksWithStatusNew() {
        subtask1.setStatus(Status.NEW);
        subtask2 = new Subtask(epic, "Мебель", "Запаковать мебель", Status.NEW);
        taskManager.addSubtask(subtask2);
        assertEquals(Status.NEW, epic.getStatus());
    }

    // расчет статуса эпика
    @Test
    void addTwoSubtasksWithStatusDone() {
        subtask1.setStatus(Status.DONE);
        subtask2 = new Subtask(epic, "Мебель", "Запаковать мебель", Status.DONE);
        taskManager.addSubtask(subtask2);
        assertEquals(Status.DONE, epic.getStatus());
    }

    // расчет статуса эпика
    @Test
    void addTwoSubtasksWithStatusNewAndDone() {
        subtask1.setStatus(Status.NEW);
        subtask2 = new Subtask(epic, "Мебель", "Запаковать мебель", Status.DONE);
        taskManager.addSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    // расчет статуса эпика
    @Test
    void addTwoSubtasksWithStatusInProgress() {
        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2 = new Subtask(epic, "Мебель", "Запаковать мебель", Status.IN_PROGRESS);
        taskManager.addSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    // у подзадач должен быть эпик
    @Test
    void checkEpicForSubtasks() {
        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2 = new Subtask(epic, "Мебель", "Запаковать мебель", Status.IN_PROGRESS);
        taskManager.addSubtask(subtask2);
        assertEquals(epic, taskManager.getEpicById(subtask1.getEpicId()));
        assertEquals(epic, taskManager.getEpicById(subtask2.getEpicId()));
    }

    // у подзадач должен быть эпик
    @Test
    void shouldBeEpicForSubtasks() {
        subtask2 = new Subtask(epic, "Мебель", "Запаковать мебель", Status.IN_PROGRESS, LocalDateTime.now(), Duration.ofMinutes(30));
        taskManager.addSubtask(subtask2);
        for (Subtask subtask : taskManager.getSubtasks()) {
            assertEquals(taskManager.getEpicById(subtask.getEpicId()), epic);
        }
    }

    // расчет пересечения временных интервалов
    @Test
    void checkTimeRanges() {
        // временные отрезки пересекаются (полное перекрытие)
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofMinutes(30));
        taskManager.updateTask(task);
        subtask2 = new Subtask(epic, "Мебель", "Запаковать мебель", Status.IN_PROGRESS, LocalDateTime.now(), Duration.ofMinutes(30));
        thrown = assertThrows(TaskValidateException.class, () -> taskManager.addSubtask(subtask2), "Должно быть исключение: Пересекается время с уже существующей задачей!");

        // временные отрезки пересекаются (перекрытие новой задачи слева)
        subtask2.setStartTime(LocalDateTime.now().minusMinutes(30));
        subtask2.setDuration(Duration.ofMinutes(30));
        thrown = assertThrows(TaskValidateException.class, () -> taskManager.addSubtask(subtask2), "Должно быть исключение: Пересекается время с уже существующей задачей!");

        // временные отрезки пересекаются (перекрытие новой задачи справа)
        subtask2.setStartTime(LocalDateTime.now().plusMinutes(25));
        subtask2.setDuration(Duration.ofMinutes(30));
        thrown = assertThrows(TaskValidateException.class, () -> taskManager.addSubtask(subtask2), "Должно быть исключение: Пересекается время с уже существующей задачей!");

        // временные отрезки не пересекаются (Task справа от Subtask)
        task.setStartTime(LocalDateTime.now().plusDays(1));
        task.setDuration(Duration.ofMinutes(30));
        assertDoesNotThrow(() -> taskManager.updateTask(task), "Не должно быть исключение: Пересекается время с уже существующей задачей!");
        subtask2.setStartTime(LocalDateTime.now());
        subtask2.setDuration(Duration.ofMinutes(30));
        assertDoesNotThrow(() -> taskManager.updateSubtask(subtask2), "Не должно быть исключение: Пересекается время с уже существующей задачей!");

        // временные отрезки не пересекаются (Task слева от Subtask)
        task.setStartTime(LocalDateTime.now().minusDays(1));
        task.setDuration(Duration.ofMinutes(30));
        assertDoesNotThrow(() -> taskManager.updateTask(task), "Не должно быть исключение: Пересекается время с уже существующей задачей!");
        subtask2.setStartTime(LocalDateTime.now());
        subtask2.setDuration(Duration.ofMinutes(30));
        assertDoesNotThrow(() -> taskManager.updateSubtask(subtask2), "Не должно быть исключение: Пересекается время с уже существующей задачей!");
    }

    // в отсортированный список задач не должна добавляться задача без указанного времени начала
    @Test
    void shouldNotBeAddedTaskToSortedTasks() {
        assertTrue(taskManager.getPrioritizedTasks().isEmpty(), "Задач без времени начала не должна быть добавлена в отсортированный список задач!");
    }

    // в отсортированный список задач должны добавляться только задачи с указанным временем начала
    @Test
    void shouldBeAddedTaskToSortedTasks() {
        task.setStartTime(LocalDateTime.now().minusMinutes(31));
        task.setDuration(Duration.ofMinutes(30));
        taskManager.updateTask(task);
        assertTrue(taskManager.getPrioritizedTasks().contains(task), "Не добавился задача в отсортированный список задач!");

        subtask2 = new Subtask(epic, "Мебель", "Запаковать мебель", Status.IN_PROGRESS, LocalDateTime.now(), Duration.ofMinutes(30));
        taskManager.addSubtask(subtask2);
    }

    // время эпика считается по его подзадачам
    @Test
    void updateTimeEpic() {
        subtask1.setStartTime(LocalDateTime.of(2000, 8, 5, 10, 0));
        subtask1.setDuration(Duration.ofMinutes(45));
        taskManager.updateSubtask(subtask1);
        subtask2 = new Subtask(epic, "Мебель", "Запаковать мебель", Status.IN_PROGRESS);
        subtask2.setStartTime(LocalDateTime.of(2024, 8, 7, 10, 0));
        subtask2.setDuration(Duration.ofMinutes(45));
        taskManager.addSubtask(subtask2);
        assertEquals(LocalDateTime.of(2000, 8, 5, 10, 0), epic.getStartTime());
        assertEquals(LocalDateTime.of(2024, 8, 7, 10, 45), epic.getEndTime());
        assertEquals(Duration.ofMinutes(90), epic.getDuration());
    }

    // задачи в отсортированном списке должны быть расположены по возрастанию даты
    @Test
    void checkAscendingOrder() {
        task.setStartTime(LocalDateTime.of(2000, 1, 5, 0, 0));
        taskManager.updateTask(task);
        subtask1.setStartTime(LocalDateTime.of(2000, 1, 3, 10, 0));
        taskManager.updateSubtask(subtask1);
        subtask2 = new Subtask(epic, "Мебель", "Запаковать мебель", Status.IN_PROGRESS);
        subtask2.setStartTime(LocalDateTime.of(2000, 1, 1, 10, 0));
        taskManager.addSubtask(subtask2);
        assertEquals(subtask2, taskManager.getPrioritizedTasks().getFirst());
        assertEquals(subtask1, taskManager.getPrioritizedTasks().get(1));
        assertEquals(task, taskManager.getPrioritizedTasks().get(2));
    }
}
