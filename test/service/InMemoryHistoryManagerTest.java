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
    }

    @Test
    void getHistory() {
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
        assertArrayEquals(savedHistoryViews.toArray(), historyManager.getHistory().toArray(), "История просмотров" +
                " не сохраняется");
    }

    @Test
    void shouldBeMaxMore10Tasks() {
        for (int i = 1; i <= 42; i++) {
            task = new Task("Задача " + i, "Описание " + i);
            task.setId(i);
            historyManager.add(task);
        }
        assertEquals(42, historyManager.getHistory().size(), "История просмотров не ограниченна по размеру");
    }
}