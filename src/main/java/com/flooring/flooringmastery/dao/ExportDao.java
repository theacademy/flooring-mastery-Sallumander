package com.flooring.flooringmastery.dao;
import com.flooring.flooringmastery.model.Order;
import java.time.LocalDate;
import java.util.Map;

import com.flooring.flooringmastery.exceptions.PersistenceException;

public interface ExportDao {
    void exportData(Map<LocalDate, Map<Integer, Order>> orders) throws PersistenceException;
}
