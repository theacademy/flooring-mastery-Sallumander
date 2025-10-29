package com.flooring.flooringmastery.dao;

import com.flooring.flooringmastery.model.Tax;
import com.flooring.flooringmastery.exceptions.PersistenceException;
import java.util.List;

public interface TaxDao {
    List<Tax> getAllTaxes() throws PersistenceException;
    Tax getTaxByState(String stateAbbreviation) throws PersistenceException;
}
