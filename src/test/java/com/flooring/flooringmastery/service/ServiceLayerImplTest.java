package com.flooring.flooringmastery.service;

import com.flooring.flooringmastery.dao.*;
import com.flooring.flooringmastery.model.*;
import com.flooring.flooringmastery.exceptions.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ServiceLayerImplTest {

    private ServiceLayerImpl service;
    private InMemoryOrderDao orderDao;
    private InMemoryProductDao productDao;
    private InMemoryTaxDao taxDao;
    private InMemoryAuditDao auditDao;
    private InMemoryExportDao exportDao;

    @BeforeEach
    void setUp() {
        orderDao = new InMemoryOrderDao();
        productDao = new InMemoryProductDao();
        taxDao = new InMemoryTaxDao();
        auditDao = new InMemoryAuditDao();
        exportDao = new InMemoryExportDao();

    service = new ServiceLayerImpl(orderDao, productDao, taxDao, auditDao, exportDao);
    }

    @Test
    void testIsValidState() throws PersistenceException {
        assertTrue(service.isValidState("TX"));
        assertFalse(service.isValidState("ZZ"));
    }

    @Test
    void testRecalculateOrder() throws PersistenceException {
        // Use createOrder to populate costs and tax information from DAOs
        Order order = service.createOrder(LocalDate.now().plusDays(1), "Test User", "TX", "Tile", new BigDecimal("200"));
        assertNotNull(order.getTotal());
        assertTrue(order.getTotal().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testAddOrderAndGetNextNumber() throws PersistenceException {
        LocalDate date = LocalDate.now();

        Order order = service.createOrder(date, "Test User", "TX", "Tile", new BigDecimal("150"));
        service.addOrder(date, order);

        assertEquals(1, orderDao.getOrdersForDate(date).size());
    }

    // ----- In-memory fake DAO implementations -----

    static class InMemoryOrderDao implements OrderDao {
        private final Map<LocalDate, Map<Integer, Order>> data = new HashMap<>();

        @Override
        public Order addOrder(LocalDate date, Order order) throws PersistenceException {
            data.putIfAbsent(date, new HashMap<>());
            data.get(date).put(order.getOrderNumber(), order);
            return order;
        }

        @Override
        public Order getOrder(LocalDate date, int orderNumber) {
            return data.getOrDefault(date, new HashMap<>()).get(orderNumber);
        }

        @Override
        public List<Order> getOrdersForDate(LocalDate date) {
            return new ArrayList<>(data.getOrDefault(date, new HashMap<>()).values());
        }

        @Override
        public Order removeOrder(LocalDate date, int orderNumber) {
            if (!data.containsKey(date)) return null;
            return data.get(date).remove(orderNumber);
        }

        @Override
        public void saveAllOrders() throws PersistenceException {

        }

        @Override
        public List<Order> getAllOrders() throws PersistenceException {
            List<Order> all = new ArrayList<>();
            data.values().forEach(map -> all.addAll(map.values()));
            return all;
        }

        @Override
        public int getNextOrderNumber() throws PersistenceException {
            return data.values().stream()
                    .flatMap(map -> map.keySet().stream())
                    .max(Integer::compareTo)
                    .orElse(0) + 1;
        }
    }

    static class InMemoryProductDao implements ProductDao {
        private final Map<String, Product> products = new HashMap<>();

        public InMemoryProductDao() {
            Product tile = new Product();
            tile.setProductType("Tile");
            tile.setCostPerSquareFoot(new BigDecimal("3.50"));
            tile.setLaborCostPerSquareFoot(new BigDecimal("4.15"));
            products.put("tile", tile);
        }

        @Override
        public Product getProductByType(String productType) throws PersistenceException {
            if (productType == null) return null;
            return products.get(productType.toLowerCase());
        }

        @Override
        public List<Product> getAllProducts() throws PersistenceException {
            return new ArrayList<>(products.values());
        }
    }

    static class InMemoryTaxDao implements TaxDao {
        private final Map<String, Tax> taxes = new HashMap<>();

        public InMemoryTaxDao() {
            Tax tx = new Tax();
            tx.setStateAbbr("TX");
            tx.setState("Texas");
            tx.setTaxRate(new BigDecimal("4.45"));
            taxes.put("TX", tx);
        }

        @Override
        public Tax getTaxByState(String stateAbbreviation) throws PersistenceException {
            if (stateAbbreviation == null) return null;
            return taxes.get(stateAbbreviation.toUpperCase());
        }

        @Override
        public List<Tax> getAllTaxes() throws PersistenceException {
            return new ArrayList<>(taxes.values());
        }
    }

    static class InMemoryAuditDao implements AuditDao {
        @Override
        public void writeAuditEntry(String entry) {
            // No-op for test
        }
    }

    static class InMemoryExportDao implements ExportDao {
        @Override
        public void exportData(Map<LocalDate, Map<Integer, Order>> orders) throws PersistenceException {
            // No-op for test
        }
    }
}
