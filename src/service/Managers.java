package service;

public class Managers {
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public TaskManager getDefault() {
        return new InMemoryTaskManager();
    }
}
