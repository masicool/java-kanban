package service;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    File tmpFile;
    BufferedWriter file;
    ManagerSaveException thrown;

    @BeforeEach
    void beforeEach() throws IOException {
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
    }

    @Test
    void shouldBeExceptionWhenFileIsNotExist() throws IOException {
        file.close();
        thrown = assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile("something.csv"));
        assertNotNull(thrown.getMessage(), "Должно быть исключение: Ошибка чтения файла!");
    }

    @Test
    void shouldBeExceptionWhenFileIsEmpty() throws IOException {
        file.close();
        thrown = assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tmpFile.toString()));
        assertNotNull(thrown.getMessage(), "Должно быть исключение с текстом: Файл пустой!");
    }

    @Test
    void compareTwoFileBackedManagers() throws IOException {
        // создадим 1-й файловый менеджер и заполним его задачами
        File tmpFile = File.createTempFile("tasks", null);
        FileBackedTaskManager taskManager1 = new FileBackedTaskManager(tmpFile.toString());
        Task task;
        Epic epic;
        Subtask subtask;
        // создадим две задачи
        task = new Task("Почистить ковер", "Отвезти в химчистку Ковер-33");
        taskManager1.addTask(task); // id будет = 1
        task = new Task("Сварить борщ", "Найти рецепт борща");
        taskManager1.addTask(task); // id будет = 2
        // создадим эпик с тремя подзадачами
        epic = new Epic("Переезд", "Переезд на новую квартиру");
        taskManager1.addEpic(epic); // id будет = 3
        subtask = new Subtask(epic, "Грузчики", "Найти грузчиков");
        taskManager1.addSubtask(subtask); // id будет = 4
        subtask = new Subtask(epic, "Кот", "Поймать кота и упаковать");
        taskManager1.addSubtask(subtask); // id будет = 5
        subtask = new Subtask(epic, "Мебель", "Запаковать мебель");
        taskManager1.addSubtask(subtask); // id будет = 6

        // создадим 2-й файловый менеджер из файла 1-го менеджера
        FileBackedTaskManager taskManager2 = FileBackedTaskManager.loadFromFile(tmpFile.toString());

        // сравним оба файловых менеджера
        assertArrayEquals(taskManager1.getTasks().toArray(), taskManager2.getTasks().toArray(),
                "Задачи менеджеров не равны!");
        assertArrayEquals(taskManager1.getEpics().toArray(), taskManager2.getEpics().toArray(),
                "Эпики менеджеров не равны!");
        assertArrayEquals(taskManager1.getSubtasks().toArray(), taskManager2.getSubtasks().toArray(),
                "Подзадачи менеджеров не равны!");
    }

    // эмуляция неправильных заголовков файла CSV и его полей
    @Test
    void generateExceptionManagerSaveException() throws IOException {
        // нет заголовка файла CSV
        file.write("TASK,1,Почистить ковер,Отвезти в химчистку Ковер-33,NEW,\n");
        file.close();
        thrown = assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tmpFile.toString()));
        assertNotNull(thrown.getMessage(), "Должно быть исключение с текстом: Поврежден заголовок файла CSV: " +
                "неизвестное поле");

        // пропущено поле в заголовке файла CSV
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
        file.write("type,id,,description,status,epic\n");
        file.write("TASK,1,Почистить ковер,Отвезти в химчистку Ковер-33,NEW,\n");
        file.close();
        thrown = assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tmpFile.toString()));
        assertNotNull(thrown.getMessage(), "Должно быть исключение с текстом: Поврежден заголовок файла CSV: " +
                "неизвестное поле!");

        // не все поля в заголовке файла CSV
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
        file.write("type,id,description,status,epic\n");
        file.write("TASK,1,Почистить ковер,Отвезти в химчистку Ковер-33,NEW,\n");
        file.close();
        thrown = assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tmpFile.toString()));
        assertNotNull(thrown.getMessage(), "Должно быть исключение с текстом: не хватает полей!");

        // дублируются поля в заголовке
        // не должно быть исключения
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
        file.write("type,type,id,name,description,status,epic\n");
        file.close();
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(tmpFile.toString()), "Исключения быть не должно!");

        // не корректно указываем ID задачи
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
        file.write("type,id,description,status,epic\n");
        file.write("TASK,один,Почистить ковер,Отвезти в химчистку Ковер-33,NEW,\n");
        file.close();
        thrown = assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tmpFile.toString()));
        assertNotNull(thrown.getMessage(), "Должно быть исключение: ID задачи должен быть числом!");

        // указываем ID задачи меньше нуля
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
        file.write("type,id,description,status,epic\n");
        file.write("TASK,-100,Почистить ковер,Отвезти в химчистку Ковер-33,NEW,\n");
        file.close();
        thrown = assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tmpFile.toString()));
        assertNotNull(thrown.getMessage(), "Должно быть исключение: ID задачи должен быть больше нуля!");

        // указываем ID задачи = 0
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
        file.write("type,id,description,status,epic\n");
        file.write("TASK,0,Почистить ковер,Отвезти в химчистку Ковер-33,NEW,\n");
        file.close();
        thrown = assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tmpFile.toString()));
        assertNotNull(thrown.getMessage(), "Должно быть исключение: ID задачи должен быть больше нуля!");
    }
}