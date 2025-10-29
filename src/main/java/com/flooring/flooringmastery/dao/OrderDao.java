package com.flooring.flooringmastery.dao;

import com.flooring.flooringmastery.model.Order;
import com.flooring.flooringmastery.exceptions.PersistenceException;

import java.time.LocalDate;
import java.util.List;

public interface OrderDao {
    List<Order> getOrdersForDate(LocalDate date) throws PersistenceException;
    Order getOrder(LocalDate date, int orderNumber) throws PersistenceException;
    Order addOrder(LocalDate date, Order order) throws PersistenceException;
    Order removeOrder(LocalDate date, int orderNumber) throws PersistenceException;
    void saveAllOrders() throws PersistenceException;
    List<Order> getAllOrders() throws PersistenceException;
    int getNextOrderNumber() throws PersistenceException;
}
