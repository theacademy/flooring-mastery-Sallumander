package com.flooring.flooringmastery.dao;

import com.flooring.flooringmastery.model.Order;
import com.flooring.flooringmastery.exceptions.PersistenceException;
import com.flooring.flooringmastery.view.UserIO;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Repository
public class OrderDaoFileImpl implements OrderDao {

    private static final String ORDER_FOLDER = "FileData/Orders/";
    private static final String DELIMITER = ",";
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("MMddyyyy");

    private final Map<LocalDate, Map<Integer, Order>> ordersByDate = new HashMap<>();
    private final UserIO userIO;

    public OrderDaoFileImpl(UserIO userIO) {
        this.userIO = userIO;
        try{
            // ensure directory exists
            this.userIO.createDirectories(ORDER_FOLDER);
            loadAllOrders();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
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

        try {
            if (!userIO.exists(fileName)) {
                ordersByDate.put(date, new HashMap<>());
                return;
            }

            List<String> lines = userIO.readAllLines(fileName);
            if (!lines.isEmpty()) lines.remove(0); // remove header

            Map<Integer, Order> orders = new HashMap<>();
            for (String line : lines) {
                String[] t = line.split(DELIMITER, -1);

                Order order = new Order();
                // order number (required) - if missing, skip this line
                if (t.length > 0 && t[0] != null && !t[0].isEmpty()) {
                    try {
                        order.setOrderNumber(Integer.parseInt(t[0]));
                    } catch (NumberFormatException e) {
                        // skip malformed order number
                        continue;
                    }
                } else {
                    continue; // cannot identify order without number
                }

                // set the order date from the file being read
                order.setOrderDate(date);

                // optional fields - set if present
                if (t.length > 1 && t[1] != null && !t[1].isEmpty()) order.setCustomerName(t[1]);
                if (t.length > 2 && t[2] != null && !t[2].isEmpty()) order.setState(t[2]);
                if (t.length > 3 && t[3] != null && !t[3].isEmpty()) {
                    try { order.setTaxRate(new BigDecimal(t[3])); } catch (NumberFormatException ignored) {}
                }
                if (t.length > 4 && t[4] != null && !t[4].isEmpty()) order.setProductType(t[4]);
                if (t.length > 5 && t[5] != null && !t[5].isEmpty()) {
                    try { order.setArea(new BigDecimal(t[5])); } catch (NumberFormatException ignored) {}
                }
                if (t.length > 6 && t[6] != null && !t[6].isEmpty()) {
                    try { order.setCostPerSquareFoot(new BigDecimal(t[6])); } catch (NumberFormatException ignored) {}
                }
                if (t.length > 7 && t[7] != null && !t[7].isEmpty()) {
                    try { order.setLaborCostPerSquareFoot(new BigDecimal(t[7])); } catch (NumberFormatException ignored) {}
                }
                if (t.length > 8 && t[8] != null && !t[8].isEmpty()) {
                    try { order.setMaterialCost(new BigDecimal(t[8])); } catch (NumberFormatException ignored) {}
                }
                if (t.length > 9 && t[9] != null && !t[9].isEmpty()) {
                    try { order.setLaborCost(new BigDecimal(t[9])); } catch (NumberFormatException ignored) {}
                }
                if (t.length > 10 && t[10] != null && !t[10].isEmpty()) {
                    try { order.setTax(new BigDecimal(t[10])); } catch (NumberFormatException ignored) {}
                }
                if (t.length > 11 && t[11] != null && !t[11].isEmpty()) {
                    try { order.setTotal(new BigDecimal(t[11])); } catch (NumberFormatException ignored) {}
                }

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
        String filePath = ORDER_FOLDER + fileName;
        Map<Integer, Order> orders = ordersByDate.get(date);

        // If there are no orders for the date, delete the file if it exists.
        if (orders == null || orders.isEmpty()) {
            try {
                userIO.deleteIfExists(filePath);
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

    // Add order lines (null-safe: empty string if field is null)
    for (Order order : orders.values()) {
        //check every value isnt null before converting to string
        //if value is null will be dealt with by service layer so unit tests still work
        String orderNum = String.valueOf(order.getOrderNumber());
        String cust = order.getCustomerName() == null ? "" : order.getCustomerName();
        String state = order.getState() == null ? "" : order.getState();
        String taxRate = order.getTaxRate() == null ? "" : order.getTaxRate().toString();
        String product = order.getProductType() == null ? "" : order.getProductType();
        String areaStr = order.getArea() == null ? "" : order.getArea().toString();
        String costPerSq = order.getCostPerSquareFoot() == null ? "" : order.getCostPerSquareFoot().toString();
        String laborPerSq = order.getLaborCostPerSquareFoot() == null ? "" : order.getLaborCostPerSquareFoot().toString();
        String materialCost = order.getMaterialCost() == null ? "" : order.getMaterialCost().toString();
        String laborCost = order.getLaborCost() == null ? "" : order.getLaborCost().toString();
        String tax = order.getTax() == null ? "" : order.getTax().toString();
        String total = order.getTotal() == null ? "" : order.getTotal().toString();

        String line = String.join(",",
            orderNum,
            cust,
            state,
            taxRate,
            product,
            areaStr,
            costPerSq,
            laborPerSq,
            materialCost,
            laborCost,
            tax,
            total
        );
        lines.add(line);
    }

        try {
            // Write all lines to the file (overwrites if exists)
            userIO.writeLines(filePath, lines);
        } catch (IOException e) {
            throw new PersistenceException("Could not write orders file for date " + date, e);
        }
    }
}
