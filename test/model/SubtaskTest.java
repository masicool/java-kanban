package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SubtaskTest {
    Subtask subtask1;
    Subtask subtask2;

    @BeforeEach
    void beforeEach() {
        subtask1 = new Subtask(1, "Грузчики", "Найти грузчиков");
        subtask1.setId(2);
        subtask2 = new Subtask(1, "Кот", "Поймать кота, упаковать");
        subtask2.setId(3);
    }

    @Test
    void shouldBeEpicSavedWhenNewSubtask() {
        assertEquals(1, subtask1.getEpicId(), "Эпик не добавился в подзадачу");
    }

    @Test
    void shouldBeEqualsWithSameId() {
        subtask2.setId(2);
        assertEquals(subtask1, subtask2, "Подзадачи с одинаковыми ID не равны");
    }

    @Test
    void shouldBeFalseWhenSubtaskIsEpic() {
        assertFalse(subtask1.setEpicId(subtask1.getId()), "Подзадачу нельзя сделать своим эпиком");
    }
}