package com.flooring.flooringmastery.view;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public interface UserIO {

    void print(String message);
    String readString(String prompt);
    int readInt(String prompt);
    int readInt(String prompt, int min, int max);
    boolean readYesNo(String prompt);
    BigDecimal readBigDecimal(String prompt);
    BigDecimal readBigDecimal(String prompt, BigDecimal min);
    LocalDate readDate(String prompt);
    String readStringAllowEmpty(String prompt);


}
