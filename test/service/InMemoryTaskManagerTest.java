package service;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    InMemoryTaskManagerTest() {
        taskManager = new InMemoryTaskManager();
    }
}