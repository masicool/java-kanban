package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    Task task1;
    Task task2;

    @BeforeEach
    void beforeEach() {
        task1 = new Task("Почистить ковер", "Отвезти в химчистку Ковер-33");
        task1.setId(1);
        task2 = new Task("АЗС", "Заправить авто");
        task2.setId(2);
    }

    @Test
    void shouldBeEqualsWithSameId() {
        task2.setId(1);
        assertEquals(task1, task2, "Задачи с одинаковыми ID не равны");
    }
}