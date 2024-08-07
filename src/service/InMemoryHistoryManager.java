package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    private final Map<Integer, Node> historyEntries;
    private Node headHistoryNode; // указатель на начало списка истории просмотров
    private Node tailHistoryNode; // указатель на конец списка истории просмотров

    public InMemoryHistoryManager() {
        historyEntries = new HashMap<>();
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        int idNewTask = task.getId(); // вспомогательная переменная, так как много обращений
        removeNode(historyEntries.get(idNewTask));

        linkLast(task); // добавим ноду в конец списка
        historyEntries.put(idNewTask, tailHistoryNode); // добавим или обновим связку ID с узлом
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int taskId) {
        Node foundNode = historyEntries.get(taskId); // ищем узел в связке
        removeNode(foundNode);
        historyEntries.remove(taskId);
    }

    // добавление задачи в конец списка
    private void linkLast(Task task) {
        if (task == null) {
            return;
        }

        Node newTaskNode = new Node(task); // создаем новый узел

        if (tailHistoryNode == null) { // если список пустой, то новый узел - это и начало и конец списка
            headHistoryNode = newTaskNode;
        } else { // иначе добавляем узел и обновляем указатель на конец списка
            newTaskNode.prev = tailHistoryNode;
            tailHistoryNode.next = newTaskNode;
        }
        tailHistoryNode = newTaskNode;
    }

    // Получение списка задач в виде обычного списка
    private List<Task> getTasks() {
        Node node = headHistoryNode;
        List<Task> historyViews = new ArrayList<>();
        while (node != null) {
            historyViews.add(node.task);
            node = node.next;
        }
        return historyViews;
    }

    // метод для удаления узла из списка просмотров
    private void removeNode(Node node) {
        if (node == null) {
            return;
        }
        Node prev = node.prev;
        Node next = node.next;
        if (prev != null) {
            prev.next = next;
        }
        if (next != null) {
            next.prev = prev;
        }
        node.next = null;
        node.prev = null;
        if (node == headHistoryNode) {
            headHistoryNode = next;
        }
        if (node == tailHistoryNode) {
            tailHistoryNode = prev;
        }
    }

    static class Node {
        public Task task;
        public Node next;
        public Node prev;

        public Node(Task task) {
            this.task = task;
        }
    }
}
