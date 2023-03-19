package org.example;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantLock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Client extends ReentrantLock {
    public String cardNumber;
    public String name;
    public BigDecimal cardBalance;
}

