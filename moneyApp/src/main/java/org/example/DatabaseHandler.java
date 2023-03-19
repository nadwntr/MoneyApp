package org.example;

import java.math.BigDecimal;
import java.sql.*;

public class DatabaseHandler {
    private final String url = "jdbc:postgresql://localhost:5432/clientdb?sslmode=disable";
    private final String username = "postgres";
    private final String password = "postgres";

    // Метод для закрытия соединения с базой данных
    public void closeConnection(Connection connection) throws SQLException {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("Не удалось закрыть соединение с базой данных: " + e);
            throw new SQLException(e);
        }
    }

    // Метод для получения данных клиентов (отправителя и получателя)
    public Client[] selectClientData() throws SQLException {
        // Создаем строку запроса
        String query = "SELECT * FROM tableClient ORDER BY random() LIMIT 2;";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStat = connection.prepareStatement(query)) {

            // Выполняем запрос и получаем его результат
            ResultSet result = preparedStat.executeQuery();
            Client[] clients = new Client[2];

            int i = 0;
            while (result.next()) {
                Client client = new Client(result.getString(1), result.getString(2),
                        result.getBigDecimal(3));
                clients[i] = client;
                i++;
            }

            result.close();

            return clients;

            // Ловим исключения
        } catch (SQLException e) {
            System.out.println("Не удалось выполнить SELECT-запросы: " + e);
            throw new SQLException(e);
        }
    }

    // Метод для получения баланса клиента
    public BigDecimal getBalance(Client client) throws SQLException, BalanceException {
        // Создаем строку запроса для получения баланса клиента
        String query = "SELECT cardBalance FROM tableClient WHERE cardNumber=?;";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStat = connection.prepareStatement(query)) {

            // Подставляем значения параметров в запрос
            preparedStat.setString(1, client.cardNumber);

            // Получаем результат запроса
            ResultSet result = preparedStat.executeQuery();
            result.next();
            BigDecimal balanceForCheck = result.getBigDecimal(1);
            result.close();

            return balanceForCheck;

            // Ловим исключения
        } catch (SQLException e) {
            throw new SQLException("Не удалось выполнить SELECT-запросы: " + e);
        }
    }

    // Метод для обновления балансов клиентов
    public void updateBalance(Client clientFrom, Client clientTo, BigDecimal money) throws SQLException {
        // Создаем строки запросов на обновление балансов клиентов
        String queryClientFrom = "UPDATE tableClient SET cardBalance=cardBalance-? WHERE tableClient.cardNumber=?;";
        String queryClientTo = "UPDATE tableClient SET cardBalance=cardBalance+? WHERE tableClient.cardNumber=?;";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStatFrom = connection.prepareStatement(queryClientFrom);
             PreparedStatement preparedStatTo = connection.prepareStatement(queryClientTo)) {

            // Подставляем значения параметров в запрос для обновления баланса первого клиента
            preparedStatFrom.setBigDecimal(1, money);
            preparedStatFrom.setString(2, clientFrom.getCardNumber());
            preparedStatFrom.executeUpdate();

            // Подставляем значения параметров в запрос для обновления баланса второго клиента
            preparedStatTo.setBigDecimal(1, money);
            preparedStatTo.setString(2, clientTo.getCardNumber());
            preparedStatTo.executeUpdate();

            // Ловим исключения
        } catch (SQLException e) {
            throw new SQLException("Не удалось выполнить UPDATE-запросы: " + e);
        }
    }
}
