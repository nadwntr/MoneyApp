package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        DatabaseHandler dbHandler = new DatabaseHandler();

        Client[] clients; // массив с данными клиентов
        Client clientOne; // первый клиент
        Client clientTwo; // второй клиент
        BigDecimal money; // сумма перевода

        final int size = 10; // количество потоков
        DeadlockTask[] tasks = new DeadlockTask[size];

        try {
            // Выбираем 2 случайных клиента из базы
            clients = dbHandler.selectClientData();
            clientOne = clients[0];
            clientTwo = clients[1];

            for (int i = 0; i < size; i++) {
                if (i % 2 == 0) { // для четных потоков первый клиент переводит второму
                    // Генерируем случайную сумму перевода
                    money = new BigDecimal(1.00 + Math.random() * 10001.00).setScale(2, RoundingMode.HALF_UP);
                    tasks[i] = new DeadlockTask(clientOne, clientTwo, money);
                } else {
                    money = new BigDecimal(1.00 + Math.random() * 10001.00).setScale(2, RoundingMode.HALF_UP);
                    tasks[i] = new DeadlockTask(clientTwo, clientOne, money);
                }
            }
            for (int i = 0; i < size; i++) {
                tasks[i].start();
            }
            for (int i = 0; i < size; i++) {
                tasks[i].join();
            }
            // Ловим исключения
        } catch (SQLException e) {
            System.out.println("Не удалось выполнить SELECT-запросы: " + e);
        }
    }
}