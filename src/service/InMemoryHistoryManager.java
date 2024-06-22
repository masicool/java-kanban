package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> historyViews; // история просмотров
    private final int MAX_HISTORY_SIZE = 10; // размер списка просмотров

    public InMemoryHistoryManager() {
        historyViews = new ArrayList<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        if (historyViews.size() >= MAX_HISTORY_SIZE) {
            historyViews.removeFirst();
        }
        historyViews.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(historyViews);
    }
}
