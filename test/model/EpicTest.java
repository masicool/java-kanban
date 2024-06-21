package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    Epic epic1;
    Epic epic2;

    @BeforeEach
    void beforeEach() {
        epic1 = new Epic("Переезд", "Переезд на новую квартиру");
        epic1.setId(1);
        epic2 = new Epic("Ремонт", "Ремонт в новой квартире");
        epic2.setId(2);
    }

    @Test
    void shouldBeEqualsWithSameId() {
        epic2.setId(1);
        assertEquals(epic1, epic2, "Эпики с одинаковыми ID не равны");
        assertFalse(epic1.addSubtaskId(1), "Эпик нельзя добавлять в самого себя в виде подзадачи");
    }

    @Test
    void shouldBeFalseWhenEpicIsSubtask() {
        assertFalse(epic1.addSubtaskId(1), "Эпик нельзя добавлять в самого себя в виде подзадачи");
    }

    @Test
    void addSubtask() {
        epic1.addSubtaskId(3);
        assertTrue(epic1.getSubtasksId().contains(3), "Подзадача не добавлена");
        assertFalse(epic1.addSubtaskId(3), "Нельзя добавить подзадачу с тем же ID");
    }

    @Test
    void deleteSubtask() {
        epic1.addSubtaskId(3);
        assertTrue(epic1.deleteSubtaskId(3), "Подзадача с указанным ID не удалена");
    }

}