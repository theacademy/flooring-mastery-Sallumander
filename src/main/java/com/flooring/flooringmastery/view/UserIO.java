package com.flooring.flooringmastery.view;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    // File I/O helpers (paths are relative to project root)
    List<String> readAllLines(String path) throws IOException;
    void writeLines(String path, List<String> lines) throws IOException;
    boolean exists(String path) throws IOException;
    void deleteIfExists(String path) throws IOException;
    void createDirectories(String dirPath) throws IOException;

}
