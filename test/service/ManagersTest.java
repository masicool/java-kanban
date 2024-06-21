package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {

    @Test
    void shouldBeNotNull() {
        Managers managers = new Managers();
        TaskManager taskManager = managers.getDefault();
        assertNotNull(taskManager, "Объект TaskManager не создан.");
    }
}