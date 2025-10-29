package com.flooring.flooringmastery.dao;

import com.flooring.flooringmastery.model.Product;
import com.flooring.flooringmastery.exceptions.PersistenceException;
import java.util.List;

public interface ProductDao {
    List<Product> getAllProducts() throws PersistenceException;
    Product getProductByType(String productType) throws PersistenceException;
}
