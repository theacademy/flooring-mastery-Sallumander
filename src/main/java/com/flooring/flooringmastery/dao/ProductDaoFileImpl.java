package com.flooring.flooringmastery.dao;

import com.flooring.flooringmastery.model.Product;
import com.flooring.flooringmastery.exceptions.PersistenceException;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;

@Repository
public class ProductDaoFileImpl implements ProductDao {

    private static final String PRODUCT_FILE = "FileData/Data/Products.txt";
    private static final String DELIMITER = ",";
    private final Map<String, Product> products = new HashMap<>();

    public ProductDaoFileImpl() {
        try {
            loadProducts();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public List<Product> getAllProducts() throws PersistenceException {
        if (products.isEmpty()) {
            loadProducts();
        }
        return new ArrayList<>(products.values());
    }

    @Override
    public Product getProductByType(String productType) throws PersistenceException {
        if (products.isEmpty()) {
            loadProducts();
        }
        return products.get(productType.toLowerCase());
    }

    private void loadProducts() throws PersistenceException {
        try {
            List<String> lines = Files.readAllLines(Paths.get(PRODUCT_FILE));
            lines.remove(0); // remove header

            for (String line : lines) {
                String[] tokens = line.split(DELIMITER);
                if (tokens.length < 3) continue;

                Product product = new Product();
                product.setProductType(tokens[0]);
                product.setCostPerSquareFoot(new BigDecimal(tokens[1]));
                product.setLaborCostPerSquareFoot(new BigDecimal(tokens[2]));

                products.put(product.getProductType().toLowerCase(), product);
            }
        } catch (IOException e) {
            throw new PersistenceException("Could not load product data from " + PRODUCT_FILE, e);
        }
    }
}
