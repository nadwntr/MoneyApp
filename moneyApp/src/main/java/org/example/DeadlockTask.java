package org.example;

import java.math.BigDecimal;
import java.sql.SQLException;

public class DeadlockTask extends Thread {
    private Client clientFrom; // клиент-отправитель
    private Client clientTo; // клиент-получатель
    private BigDecimal money; // сумма перевода

    public DeadlockTask(Client clientFrom, Client clientTo, BigDecimal money) {
        this.clientFrom = clientFrom;
        this.clientTo = clientTo;
        this.money = money;
    }

    public void run() {
        DatabaseHandler dbHandler = new DatabaseHandler();
        while (true) {
            if (clientFrom.tryLock()) {
                if (clientTo.tryLock()) {
                    try {
                        // Получаем баланс клиента-отправителя
                        BigDecimal balanceFrom = dbHandler.getBalance(clientFrom);
                        // Проверяем возможность перевести деньги с учетом баланса
                        if (money.compareTo(balanceFrom) > 0) {
                            throw new BalanceException("Недостаточно денег на балансе для осуществления перевода (" +
                                    balanceFrom + " руб.)");
                        }
                        // Если перевод возможен - получаем баланс клиента-получателя
                        BigDecimal balanceTo = dbHandler.getBalance(clientTo);
                        // Обновляем балансы обоих клиентов
                        dbHandler.updateBalance(clientFrom, clientTo, money);
                        // Получаем балансы клиентов после обновления
                        BigDecimal balanceFromUpdate = dbHandler.getBalance(clientFrom);
                        BigDecimal balanceToUpdate = dbHandler.getBalance(clientTo);

                        // Выводим сообщение о факте перевода и информацию по балансам
                        System.out.println(
                                String.format(
                                        "Клиент %s перевёл %s рублей клиенту %s.\n" +
                                                "Изменение в балансе клиента %s: %s руб./%s руб.\n" +
                                                "Изменение в балансе клиента %s: %s руб./%s руб.\n",
                                        clientFrom.getName(),
                                        money,
                                        clientTo.getName(),
                                        clientFrom.getName(),
                                        balanceFrom,
                                        balanceFromUpdate,
                                        clientTo.getName(),
                                        balanceTo,
                                        balanceToUpdate
                                )
                        );
                        // Ловим исключения
                    } catch (SQLException e) {
                        System.out.println("Не удалось выполнить SQL-запросы: \n" + e);
                    } catch (BalanceException e) {
                        System.out.println(
                                String.format(
                                        "Не удалось выполнить перевод клиента %s в %s руб. клиенту %s:\n" +
                                                e.getMessage() + "\n",
                                        clientFrom.getName(),
                                        money,
                                        clientTo.getName()
                                )
                        );
                    } finally {
                        clientTo.unlock();
                        clientFrom.unlock();
                        break;
                    }
                } else {
                    clientFrom.unlock();
                }
            }
        }
    }
}

