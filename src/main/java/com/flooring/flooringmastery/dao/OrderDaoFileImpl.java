package com.flooring.flooringmastery.dao;

import com.flooring.flooringmastery.model.Order;
import com.flooring.flooringmastery.exceptions.PersistenceException;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
public class OrderDaoFileImpl implements OrderDao {

    private static final String ORDER_FOLDER = "FileData/Orders/";
    private static final String DELIMITER = ",";
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("MMddyyyy");

    private final Map<LocalDate, Map<Integer, Order>> ordersByDate = new HashMap<>();

    public OrderDaoFileImpl() {
        try{
            loadAllOrders();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }
    private void loadAllOrders() throws  PersistenceException {
        File folder = new File(ORDER_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs(); // create folder if missing
        }

        File[] files = folder.listFiles((dir, name) -> name.startsWith("Orders_") && name.endsWith(".txt"));
        if (files == null) return;

        for (File file : files) {
            LocalDate date = getDateFromFileName(file.getName());
            loadOrdersForDate(date);
        }
    }
    @Override
    public List<Order> getOrdersForDate(LocalDate date) throws PersistenceException {
        loadOrdersForDate(date);
        Map<Integer, Order> orders = ordersByDate.get(date);
        return orders == null ? new ArrayList<>() : new ArrayList<>(orders.values());
    }

    @Override
    public Order getOrder(LocalDate date, int orderNumber) throws PersistenceException {
        loadOrdersForDate(date);
        Map<Integer, Order> orders = ordersByDate.get(date);
        return orders != null ? orders.get(orderNumber) : null;
    }

    @Override
    public Order addOrder(LocalDate date, Order order) throws PersistenceException {
        loadOrdersForDate(date);

        //If new Date store as new entry in Hashmap so it is easy to create new order files for each day
        ordersByDate.computeIfAbsent(date, k -> new HashMap<>()).put(order.getOrderNumber(), order);
        writeOrdersForDate(date);
        return order;
    }

    @Override
    public Order removeOrder(LocalDate date, int orderNumber) throws PersistenceException {
        loadOrdersForDate(date);
        Map<Integer, Order> orders = ordersByDate.get(date);
        Order removedOrder = null;
        if (orders != null) {
            removedOrder = orders.remove(orderNumber);
            writeOrdersForDate(date);
        }
        return removedOrder;
    }

    @Override
    public void saveAllOrders() throws PersistenceException {
        for (LocalDate date : ordersByDate.keySet()) {
            writeOrdersForDate(date);
        }
    }

    //loads orders for a specific date from file into the ordersByDate map
    private void loadOrdersForDate(LocalDate date) throws PersistenceException {
        if (ordersByDate.containsKey(date)) return;

        String fileName = ORDER_FOLDER + "Orders_" + date.format(FILE_DATE) + ".txt";
        Path path = Paths.get(fileName);

        if (!Files.exists(path)) {
            ordersByDate.put(date, new HashMap<>());
            return;
        }

        try {
            List<String> lines = Files.readAllLines(path);
            lines.remove(0); // remove header

            Map<Integer, Order> orders = new HashMap<>();
            for (String line : lines) {
                String[] t = line.split(DELIMITER);
                if (t.length < 12) continue;

                Order order = new Order();
                order.setOrderNumber(Integer.parseInt(t[0]));
                // set the order date from the file being read
                order.setOrderDate(date);
                order.setCustomerName(t[1]);
                order.setState(t[2]);
                order.setTaxRate(new BigDecimal(t[3]));
                order.setProductType(t[4]);
                order.setArea(new BigDecimal(t[5]));
                order.setCostPerSquareFoot(new BigDecimal(t[6]));
                order.setLaborCostPerSquareFoot(new BigDecimal(t[7]));
                order.setMaterialCost(new BigDecimal(t[8]));
                order.setLaborCost(new BigDecimal(t[9]));
                order.setTax(new BigDecimal(t[10]));
                order.setTotal(new BigDecimal(t[11]));

                orders.put(order.getOrderNumber(), order);
            }

            ordersByDate.put(date, orders);
        } catch (IOException e) {
            throw new PersistenceException("Could not read orders file for date " + date, e);
        }
    }
    private LocalDate getDateFromFileName(String fileName) {
        // Example: "Orders_08212025.txt"
        String datePart = fileName.substring(7, 15); // MMDDYYYY
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyy");
        return LocalDate.parse(datePart, formatter);
    }

    @Override
    //generate next order number by finding max across all dates
    public int getNextOrderNumber() throws PersistenceException {
        int maxOrderNumber = 0;

        for (Map<Integer, Order> ordersForDate : ordersByDate.values()) {
            if (ordersForDate != null && !ordersForDate.isEmpty()) {
                int localMax = Collections.max(ordersForDate.keySet());
                if (localMax > maxOrderNumber) {
                    maxOrderNumber = localMax;
                }
            }
        }

        return maxOrderNumber + 1;
    }


    public List<Order> getAllOrders() throws PersistenceException {
        List<Order> all = new ArrayList<>();
        try {
            File folder = new File(ORDER_FOLDER);
            File[] files = folder.listFiles();

            if (files == null) {
                // No files => empty list
                return all;
            }

            for (File file : files) {
                String fileName = file.getName();

                // Only process valid order files like "Orders_08212025.txt"
                if (fileName.startsWith("Orders_") && fileName.endsWith(".txt")) {
                    LocalDate date = getDateFromFileName(fileName);
                    loadOrdersForDate(date);  // ensure entries are loaded into ordersByDate
                }
            }

            for (Map<Integer, Order> map : ordersByDate.values()) {
                if (map != null) all.addAll(map.values());
            }

            return all;

        } catch (Exception e) {
            throw new PersistenceException("Error reading all orders files", e);
        }
    }
    private void writeOrdersForDate(LocalDate date) throws PersistenceException {
        String fileName = "Orders_" + date.format(FILE_DATE) + ".txt";
        Path path = Paths.get(ORDER_FOLDER, fileName);
        Map<Integer, Order> orders = ordersByDate.get(date);

        // If there are no orders for the date, delete the file if it exists.
        if (orders == null || orders.isEmpty()) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new PersistenceException("Could not delete empty orders file for date " + date, e);
            }
            return;
        }

        List<String> lines = new ArrayList<>();

        // Header line
        lines.add("OrderNumber,CustomerName,State,TaxRate,ProductType,Area,"
                + "CostPerSquareFoot,LaborCostPerSquareFoot,MaterialCost,"
                + "LaborCost,Tax,Total");

        // Add order lines
        for (Order order : orders.values()) {
            String line = String.join(",",
                    String.valueOf(order.getOrderNumber()),
                    order.getCustomerName(),
                    order.getState(),
                    order.getTaxRate().toString(),
                    order.getProductType(),
                    order.getArea().toString(),
                    order.getCostPerSquareFoot().toString(),
                    order.getLaborCostPerSquareFoot().toString(),
                    order.getMaterialCost().toString(),
                    order.getLaborCost().toString(),
                    order.getTax().toString(),
                    order.getTotal().toString()
            );
            lines.add(line);
        }

        try {
            // Write all lines to the file (overwrites if exists)
            Files.write(path, lines);
        } catch (IOException e) {
            throw new PersistenceException("Could not write orders file for date " + date, e);
        }
    }
}
