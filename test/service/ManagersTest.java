package service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ManagersTest {

    @Test
    void shouldBeNotNull() {
        assertNotNull(Managers.getDefault(), "Объект TaskManager не создан.");
        assertNotNull(Managers.getDefaultHistory(), "Объект InMemoryHistoryManager не создан.");
    }
}