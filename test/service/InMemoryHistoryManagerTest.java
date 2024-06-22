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
        historyManager.add(task);
        epic = new Epic("Переезд", "Переезд на новую квартиру");
        historyManager.add(epic);
        subtask = new Subtask(epic, "Грузчики", "Найти грузчиков");
        historyManager.add(subtask);
        savedHistoryViews = new ArrayList<>();
        savedHistoryViews.add(task);
        savedHistoryViews.add(epic);
        savedHistoryViews.add(subtask);
    }

    @Test
    void getHistory() {
        assertArrayEquals(savedHistoryViews.toArray(), historyManager.getHistory().toArray(), "История просмотров" +
                " не сохраняется");
    }

    @Test
    void shouldBeMax10Tasks() {
        for (int i = 0; i < 12; i++) {
            task = new Task("Задача " + i, "Описание " + i);
            historyManager.add(task);
        }
        assertEquals(10, historyManager.getHistory().size(), "В истории не может находится более " +
                "10 задач");
    }
}