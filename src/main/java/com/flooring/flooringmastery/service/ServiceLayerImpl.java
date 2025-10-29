package com.flooring.flooringmastery.service;
import com.flooring.flooringmastery.dao.*;
import com.flooring.flooringmastery.exceptions.PersistenceException;
import com.flooring.flooringmastery.model.Order;
import com.flooring.flooringmastery.model.Product;
import com.flooring.flooringmastery.model.Tax;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.math.BigDecimal;

@Service
public class ServiceLayerImpl implements ServiceLayer {
    private AuditDao auditDao;
    private ExportDao exportDao;
    private OrderDao orderDao;
    private ProductDao productDao;
    private TaxDao taxDao;

    @Autowired
    public ServiceLayerImpl(OrderDao orderDao,
                            ProductDao productDao,
                            TaxDao taxDao,
                            AuditDao auditDao,
                            ExportDao exportDao) {
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.taxDao = taxDao;
        this.auditDao = auditDao;
        this.exportDao = exportDao;
    }

    @Override
    public int getNextOrderNumber() {
        try {
            return orderDao.getNextOrderNumber();
        } catch (PersistenceException e) {
            // As a fallback, return 1
            return 1;
        }
    }

    public void addOrder(LocalDate date, Order order) throws PersistenceException {
        // Basic validation of required fields
        if (order == null) {
            throw new PersistenceException("Order cannot be null");
        }
        if (order.getOrderDate() == null) {
            order.setOrderDate(date);
        }
        if (order.getOrderDate() == null) {
            throw new PersistenceException("Order date is required");
        }
        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
            throw new PersistenceException("Customer name is required");
        }
        if (!isValidName(order.getCustomerName())) {
            throw new PersistenceException("Customer name is invalid");
        }
        if (order.getState() == null || order.getState().trim().isEmpty()) {
            throw new PersistenceException("State is required");
        }
        if (!isValidState(order.getState())) {
            throw new PersistenceException("State is invalid or unsupported");
        }
        if (order.getProductType() == null || order.getProductType().trim().isEmpty()) {
            throw new PersistenceException("Product type is required");
        }
        if (!isValidProduct(order.getProductType())) {
            throw new PersistenceException("Product type is invalid or unsupported");
        }
        if (order.getArea() == null || order.getArea().compareTo(new BigDecimal("100")) < 0) {
            throw new PersistenceException("Area is required and must be at least 100");
        }

        // Ensure cost/tax fields are populated; if missing, try to refill from DAOs and recalculate
        try {
            if (order.getCostPerSquareFoot() == null || order.getLaborCostPerSquareFoot() == null) {
                Product p = productDao.getProductByType(order.getProductType());
                if (p == null) throw new PersistenceException("Product data not found for type: " + order.getProductType());
                order.setCostPerSquareFoot(p.getCostPerSquareFoot());
                order.setLaborCostPerSquareFoot(p.getLaborCostPerSquareFoot());
            }
            if (order.getTaxRate() == null) {
                Tax t = taxDao.getTaxByState(order.getState());
                if (t == null) throw new PersistenceException("Tax data not found for state: " + order.getState());
                order.setTaxRate(t.getTaxRate());
            }
        } catch (PersistenceException e) {
            throw e;
        }

        // Calculate derived fields (material,labor,tax,total)
        calculateOrder(order);

        // Save via DAO
        orderDao.addOrder(date, order);
   }

   public boolean isValidProduct(String productType) throws PersistenceException {
        if(productType == null) {
            return false;
        }
        List<String> knownTypes=getAllProductTypes();
       return knownTypes.contains(productType.toLowerCase());
   }
    public boolean isValidState(String stateAbbr) throws PersistenceException {
        if (stateAbbr == null) {
            return false;
        }
        List<String> states=getAllStateAbbrs();
        return states.contains(stateAbbr);
    }
    //get List of every state Abbr
    public List<String> getAllStateAbbrs() throws PersistenceException{
        List<String> states=new ArrayList<>();
        List<Tax> taxes=taxDao.getAllTaxes();
        for (Tax tax : taxes) {
            states.add(tax.getStateAbbr());
        }
        return states;
    }

    //get list of every product types to check if valid adn display all options for user
    public List<String> getAllProductTypes() throws PersistenceException {
        if (productDao == null) {
            return null;
        }
        List<String> productTypes=new ArrayList<>();
        List<Product>products=productDao.getAllProducts();
        for(Product product : products) {
            productTypes.add(product.getProductType().toLowerCase());
        }
        return productTypes;
    }

    @Override
    public Order createOrder(LocalDate orderDate, String customerName, String state, String productType, BigDecimal area) throws PersistenceException {

        //Get tax Info for order
        Tax taxInfo = taxDao.getTaxByState(state);
        if(taxInfo == null) {
            throw new IllegalArgumentException("State not found in Tax data: " + state);
        }

        //Get Product Info
        Product productInfo = productDao.getProductByType(productType);
        if(productInfo == null) {
            throw new IllegalArgumentException("Product type not found in Product data: " + productType);
        }

        //Create Order Object
        Order order = new Order();
        //Generate and add order number
        int orderNumber = orderDao.getNextOrderNumber();
        order.setOrderNumber(orderNumber);
        order.setCustomerName(customerName);
        order.setOrderDate(orderDate);
        order.setState(state);
        order.setTaxRate(taxInfo.getTaxRate());
        order.setProductType(productType);
        order.setArea(area);
        order.setCostPerSquareFoot(productInfo.getCostPerSquareFoot());
        order.setLaborCostPerSquareFoot(productInfo.getLaborCostPerSquareFoot());
        calculateOrder(order);
        return order;
    }
    public void calculateOrder(Order order){
        order.setLaborCost(order.getLaborCostPerSquareFoot().multiply(order.getArea()));
        order.setMaterialCost(order.getCostPerSquareFoot().multiply(order.getArea()));
        BigDecimal totalCost = (order.getMaterialCost().add(order.getLaborCost()));
        order.setTax(totalCost.multiply((order.getTaxRate().divide(BigDecimal.valueOf(100)))));
        order.setTotal(totalCost.add(order.getTax()));
    }
    public boolean isValidName(String name){
        return name.matches("[A-Za-z.,\\- ]+");

    }

    public Order getOrder(LocalDate date, int orderNumber) throws PersistenceException {
        return orderDao.getOrder(date, orderNumber);
    }
    @Override
    public void removeOrder(LocalDate date, int orderNumber) throws PersistenceException {
        Order removed = orderDao.removeOrder(date, orderNumber);
        if (removed == null) {
            throw new PersistenceException("Order not found for that date and number.");
        }
        auditDao.writeAuditEntry("Order #" + orderNumber + " removed for date " + date);
    }

    public List<Order> getOrdersForDate(LocalDate date) throws PersistenceException {
        return orderDao.getOrdersForDate(date);
    }
    public List<Product> getProducts() { return null; }
    public List<Tax> getTaxes() { return null; }
    public void exportData() {
        try {
            List<Order> all = orderDao.getAllOrders();
            Map<LocalDate, Map<Integer, Order>> map = new TreeMap<>();
            for (Order o : all) {
                LocalDate d = o.getOrderDate();
                if (d == null) continue;
                map.computeIfAbsent(d, k -> new TreeMap<>()).put(o.getOrderNumber(), o);
            }
            exportDao.exportData(map);
        } catch (PersistenceException e) {
            throw new RuntimeException("Failed to export data: " + e.getMessage(), e);
        }
    }
}
