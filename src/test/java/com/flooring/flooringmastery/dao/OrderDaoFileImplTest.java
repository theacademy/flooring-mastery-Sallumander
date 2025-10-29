package com.flooring.flooringmastery.dao;

import com.flooring.flooringmastery.dao.OrderDaoFileImpl;
import com.flooring.flooringmastery.view.UserIOConsoleImpl;
import com.flooring.flooringmastery.model.Order;
import com.flooring.flooringmastery.exceptions.PersistenceException;

import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;


class OrderDaoFileImplTest {

    private OrderDaoFileImpl dao;
    private final LocalDate date = LocalDate.of(2025, 10, 29);

    @BeforeEach
    public void setUp() throws Exception {
        dao = new OrderDaoFileImpl(new UserIOConsoleImpl());
        // Optional: clear or reset in-memory map for test isolation
        Files.createDirectories(Paths.get("FileData/Orders"));
    }

    @Test
    public void testAddAndGetOrder() throws PersistenceException {
        Order order = new Order();
        order.setOrderNumber(1);
        order.setCustomerName("Test Customer");
        order.setState("TX");
        order.setTaxRate(new BigDecimal("4.45"));
        order.setProductType("Tile");
        order.setArea(new BigDecimal("100"));
        order.setCostPerSquareFoot(new BigDecimal("3.50"));
        order.setLaborCostPerSquareFoot(new BigDecimal("4.15"));
        order.setMaterialCost(new BigDecimal("350.00"));
        order.setLaborCost(new BigDecimal("415.00"));
        order.setTax(new BigDecimal("33.83"));
        order.setTotal(new BigDecimal("798.83"));

        dao.addOrder(date, order);

        Order fetched = dao.getOrder(date, 1);
        assertNotNull(fetched);
        assertEquals("Test Customer", fetched.getCustomerName());
    }

    @Test
    public void testRemoveOrder() throws PersistenceException {

        dao.removeOrder(date, 1);
        assertNull(dao.getOrder(date, 1));
    }

    @Test
    public void testRemoveLastOrderDeletesFile() throws Exception {
        // Ensure a clean file state
        Path filePath = Paths.get("FileData/Orders/Orders_" + date.format(java.time.format.DateTimeFormatter.ofPattern("MMddyyyy")) + ".txt");
        if (Files.exists(filePath)) Files.delete(filePath);

        // Add a single order -> file should be created
        Order order = new Order();
        order.setOrderNumber(10);
        order.setCustomerName("Solo");
        dao.addOrder(date, order);
        assertTrue(Files.exists(filePath));

        // Remove the only order -> file should be deleted
        dao.removeOrder(date, 10);
        assertFalse(Files.exists(filePath));
    }
}
