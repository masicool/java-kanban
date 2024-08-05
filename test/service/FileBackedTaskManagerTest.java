package service;

import exception.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    File tmpFile;
    BufferedWriter file;
    ManagerSaveException thrown;

    FileBackedTaskManagerTest() throws IOException {
        taskManager = new FileBackedTaskManager("tasks.csv");
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
    void compareTwoFileBackedManagersAndCheckId() {
        // создадим 1-й файловый менеджер и заполним его задачами
        FileBackedTaskManager taskManager1 = new FileBackedTaskManager(tmpFile.toString());
        Task task;
        Epic epic;
        Subtask subtask;
        // создадим две задачи
        task = new Task("Почистить ковер", "Отвезти в химчистку Ковер-33");
        taskManager1.addTask(task); // id будет = 1
        task = new Task("Сварить борщ", "Найти рецепт борща");
        task.setStartTime(LocalDateTime.of(2024, 8, 5, 10, 0));
        task.setDuration(Duration.ofMinutes(13));
        taskManager1.addTask(task); // id будет = 2
        // создадим эпик с тремя подзадачами
        epic = new Epic("Переезд", "Переезд на новую квартиру");
        taskManager1.addEpic(epic); // id будет = 3
        subtask = new Subtask(epic, "Грузчики", "Найти грузчиков");
        taskManager1.addSubtask(subtask); // id будет = 4
        subtask = new Subtask(epic, "Кот", "Поймать кота и упаковать");
        taskManager1.addSubtask(subtask); // id будет = 5
        subtask = new Subtask(epic, "Мебель", "Запаковать мебель");
        subtask.setStartTime(LocalDateTime.of(2024, 8, 4, 14, 0));
        subtask.setDuration(Duration.ofMinutes(55));
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

        // в новом менеджере должен быть актуальный ID задач
        assertEquals(6, taskManager2.taskId, "В созданном из файла менеджере " +
                "не актуализировался ID задач");
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
        file.close();
        Files.delete(tmpFile.toPath());

        // пропущено поле в заголовке файла CSV
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
        file.write("type,id,,description,status,epic,starttime,duration,endtime\n");
        file.write("TASK,1,Почистить ковер,Отвезти в химчистку Ковер-33,NEW,\n");
        file.close();
        thrown = assertThrows(ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(tmpFile.toString()),
                "Должно быть исключение с текстом: Поврежден заголовок файла CSV: неизвестное поле!");
        file.close();
        Files.delete(tmpFile.toPath());

        // не все поля в заголовке файла CSV
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
        file.write("type,id,,description,status,epic,starttime,duration,endtime\n");
        file.write("TASK,1,Почистить ковер,Отвезти в химчистку Ковер-33,NEW,\n");
        file.close();
        thrown = assertThrows(ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(tmpFile.toString()),
                "Должно быть исключение с текстом: не хватает полей!");
        file.close();
        Files.delete(tmpFile.toPath());

        // дублируются поля в заголовке
        // не должно быть исключения
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
        file.write("type,type,id,name,description,status,epic,starttime,duration,endtime\n");
        file.close();
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(tmpFile.toString()), "Исключения быть не должно!");
        file.close();
        Files.delete(tmpFile.toPath());

        // не корректно указываем ID задачи
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
        file.write("type,id,description,status,epic,starttime,duration,endtime\n");
        file.write("TASK,один,Почистить ковер,Отвезти в химчистку Ковер-33,NEW,\n");
        file.close();
        thrown = assertThrows(ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(tmpFile.toString()),
                "Должно быть исключение: ID задачи должен быть числом!");
        file.close();
        Files.delete(tmpFile.toPath());

        // указываем ID задачи меньше нуля
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
        file.write("type,id,description,status,epic,starttime,duration,endtime\n");
        file.write("TASK,-100,Почистить ковер,Отвезти в химчистку Ковер-33,NEW,\n");
        file.close();
        thrown = assertThrows(ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(tmpFile.toString()),
                "Должно быть исключение: ID задачи должен быть больше нуля!");
        file.close();
        Files.delete(tmpFile.toPath());

        // указываем ID задачи = 0
        tmpFile = File.createTempFile("tasks", null);
        file = new BufferedWriter(new FileWriter(tmpFile));
        file.write("type,id,description,status,epic,starttime,duration,endtime\n");
        file.write("TASK,0,Почистить ковер,Отвезти в химчистку Ковер-33,NEW,\n");
        file.close();
        thrown = assertThrows(ManagerSaveException.class,
                () -> FileBackedTaskManager.loadFromFile(tmpFile.toString()),
                "Должно быть исключение: ID задачи должен быть больше нуля!");
    }

    @AfterEach
    void afterEach() throws IOException {
        file.close();
        Files.delete(tmpFile.toPath());
    }
}