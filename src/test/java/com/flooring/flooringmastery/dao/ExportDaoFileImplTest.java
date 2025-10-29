package com.flooring.flooringmastery.dao;

import com.flooring.flooringmastery.exceptions.PersistenceException;
import com.flooring.flooringmastery.model.Order;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class ExportDaoFileImplTest {

    private static final Path EXPORT_PATH = Path.of("FileData/Backup/DataExport.txt");

    @AfterEach
    void cleanup() throws IOException {
        if (Files.exists(EXPORT_PATH)) Files.delete(EXPORT_PATH);
    }

    @Test
    void testExportDataWritesFile() throws PersistenceException, IOException {
    ExportDaoFileImpl exportDao = new ExportDaoFileImpl(new com.flooring.flooringmastery.view.UserIOConsoleImpl());

        // prepare a single order map
        Order o = new Order();
        o.setOrderNumber(42);
        o.setCustomerName("ExportTest");
        o.setState("TX");
        o.setOrderDate(LocalDate.of(2025,10,29));
        o.setTaxRate(new BigDecimal("4.45"));
        o.setProductType("Tile");
        o.setArea(new BigDecimal("100"));
        o.setCostPerSquareFoot(new BigDecimal("3.50"));
        o.setLaborCostPerSquareFoot(new BigDecimal("4.15"));
        o.setMaterialCost(new BigDecimal("350.00"));
        o.setLaborCost(new BigDecimal("415.00"));
        o.setTax(new BigDecimal("33.83"));
        o.setTotal(new BigDecimal("798.83"));

        Map<LocalDate, Map<Integer, Order>> map = new TreeMap<>();
        map.computeIfAbsent(o.getOrderDate(), d -> new TreeMap<>()).put(o.getOrderNumber(), o);

        exportDao.exportData(map);

        assertTrue(Files.exists(EXPORT_PATH), "Export file should exist after exportData");
        long lines = Files.readAllLines(EXPORT_PATH).size();
        assertTrue(lines >= 2, "Export file should contain header and at least one order line");
        String content = Files.readString(EXPORT_PATH);
        assertTrue(content.contains("ExportTest"), "Exported content should include the customer name");
    }
}
