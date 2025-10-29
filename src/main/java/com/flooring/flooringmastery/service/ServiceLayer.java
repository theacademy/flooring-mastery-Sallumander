package com.flooring.flooringmastery.service;
import com.flooring.flooringmastery.exceptions.PersistenceException;
import com.flooring.flooringmastery.exceptions.NoSuchOrderException;
import com.flooring.flooringmastery.model.Order;
import com.flooring.flooringmastery.model.Product;
import com.flooring.flooringmastery.model.Tax;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ServiceLayer {
    int getNextOrderNumber();
    void addOrder(LocalDate date, Order order) throws PersistenceException;
    Order getOrder(LocalDate date, int orderNumber) throws PersistenceException, NoSuchOrderException;
    List<Order> getOrdersForDate(LocalDate date) throws PersistenceException;
    List<Product> getProducts();
    List<Tax> getTaxes();
    void exportData();
    boolean isValidState(String stateAbbr) throws PersistenceException;
    boolean isValidProduct(String productType) throws PersistenceException;
    boolean isValidName(String productName) throws PersistenceException;
    List<String> getAllProductTypes() throws PersistenceException;
    List<String> getAllStateAbbrs() throws PersistenceException;
    Order createOrder(LocalDate orderDate, String customerName, String state, String productType, BigDecimal area) throws PersistenceException;
    void calculateOrder(Order order) throws PersistenceException;
    void removeOrder(LocalDate date, int orderNumber) throws PersistenceException, NoSuchOrderException;
}
