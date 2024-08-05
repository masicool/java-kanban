package service;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryHistoryManagerTest {
    static InMemoryHistoryManager historyManager;
    static Task task;
    static Epic epic;
    static Subtask subtask;
    static List<Task> savedHistoryViews;

    @BeforeEach
    void beforeEach() {
        historyManager = new InMemoryHistoryManager();
        task = new Task("Почистить ковер", "Отвезти в химчистку Ковер-33");
        task.setId(1);
        historyManager.add(task);
        epic = new Epic("Переезд", "Переезд на новую квартиру");
        epic.setId(2);
        historyManager.add(epic);
        subtask = new Subtask(epic, "Грузчики", "Найти грузчиков");
        subtask.setId(3);
        historyManager.add(subtask);
        savedHistoryViews = new ArrayList<>();
        savedHistoryViews.add(task);
        savedHistoryViews.add(epic);
        savedHistoryViews.add(subtask);
    }

    // получение истории
    @Test
    void getHistory() {
        assertArrayEquals(savedHistoryViews.toArray(), historyManager.getHistory().toArray(), "История просмотров" +
                " не сохраняется");
    }

    // пустая история задач
    @Test
    void shouldBeNullSizeAfterClear() {
        historyManager.remove(1);
        historyManager.remove(2);
        historyManager.remove(3);
        assertEquals(0, historyManager.getHistory().size(),
                "История не удалилась");
    }

    // нет ограничений на размер истории задач
    @Test
    void shouldBeMaxMore10Tasks() {
        historyManager.remove(1);
        historyManager.remove(2);
        historyManager.remove(3);
        for (int i = 1; i <= 42; i++) {
            task = new Task("Задача " + i, "Описание " + i);
            task.setId(i);
            historyManager.add(task);
        }
        assertEquals(42, historyManager.getHistory().size(), "История просмотров не ограниченна по размеру");
    }

    // удаление не существующей задачи
    @Test
    void removeNotExistTask() {
        historyManager.remove(1237812);
        assertArrayEquals(savedHistoryViews.toArray(), historyManager.getHistory().toArray(),
                "При удалении несуществующей задачи - история просмотров изменилась");
    }

    // удаление задачи по ID
    @Test
    void deleteTaskById() {
        int oldSize = historyManager.getHistory().size();
        historyManager.remove(1);
        assertEquals(oldSize - 1, historyManager.getHistory().size(),
                "Задача из истории просмотров не удалилась");
    }

    // дублирование
    @Test
    void addExistTaskId() {
        Task task = new Task("Дубликат задачи с ID = 1", "Описание");
        task.setId(1);
        historyManager.add(task);
        task = new Task("Дубликат задачи с ID = 1", "Описание");
        task.setId(1);
        historyManager.add(task);
        int countTasks = 0;
        for (Task taskItem : historyManager.getHistory()) {
            if (taskItem.getId() == 1) {
                countTasks++;
            }
        }
        assertEquals(1, countTasks,
                "Добавилась задача с существующим ID в историю");
    }

    // удаление из истории с начала
    @Test
    void deleteFirstTask() {
        historyManager.remove(1);
        savedHistoryViews.removeFirst();
        assertArrayEquals(savedHistoryViews.toArray(), historyManager.getHistory().toArray(),
                "Изменился порядок в истории после удалении первой задачи");
    }

    // удаление из истории с середины
    @Test
    void deleteTaskFromMiddle() {
        historyManager.remove(2);
        savedHistoryViews.remove(1);
        assertArrayEquals(savedHistoryViews.toArray(), historyManager.getHistory().toArray(),
                "Изменился порядок в истории после удалении задачи из середины");
    }

    // удаление из истории с конца
    @Test
    void deleteLastTask() {
        historyManager.remove(3);
        savedHistoryViews.removeLast();
        assertArrayEquals(savedHistoryViews.toArray(), historyManager.getHistory().toArray(),
                "Изменился порядок в истории после удалении последней задачи");
    }
}