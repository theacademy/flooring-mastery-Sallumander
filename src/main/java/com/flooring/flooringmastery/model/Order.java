package com.flooring.flooringmastery.model;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Order {
    private int orderNumber;
    private String customerName;
    private String state;
    private LocalDate orderDate;
    private BigDecimal taxRate;
    private String productType;
    private BigDecimal costPerSquareFoot;
    private BigDecimal laborCostPerSquareFoot;
    private BigDecimal materialCost;
    private BigDecimal area;
    private BigDecimal laborCost;
    private BigDecimal tax;
    private BigDecimal total;


    public Order() {
            // default no-arg constructor (keep this!)
        }

        // ✅ copy constructor
    public Order(Order other) {
            this.orderNumber = other.orderNumber;
            this.orderDate = other.orderDate;
            this.customerName = other.customerName;
            this.state = other.state;
            this.taxRate = other.taxRate;
            this.productType = other.productType;
            this.area = other.area;
            this.costPerSquareFoot = other.costPerSquareFoot;
            this.laborCostPerSquareFoot = other.laborCostPerSquareFoot;
            this.materialCost = other.materialCost;
            this.laborCost = other.laborCost;
            this.tax = other.tax;
            this.total = other.total;
        }



    // Getters and Setters
    // hashCode, equals, toString
    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }
    public String getProductType() {
        return productType;
    }

    public void setCostPerSquareFoot(BigDecimal costPerSquareFoot) {
        this.costPerSquareFoot = costPerSquareFoot;
    }
    public BigDecimal getCostPerSquareFoot() {
        return costPerSquareFoot;
    }

    public void setLaborCostPerSquareFoot(BigDecimal laborCostPerSquareFoot) {
        this.laborCostPerSquareFoot = laborCostPerSquareFoot;
    }
    public BigDecimal getLaborCostPerSquareFoot() {
        return laborCostPerSquareFoot;
    }

    public void setMaterialCost(BigDecimal materialCost) {
        this.materialCost = materialCost;
    }
    public BigDecimal getMaterialCost() {
        return materialCost;
    }
    public void setArea(BigDecimal area) {
        this.area = area;
    }
    public BigDecimal getArea() {
        return area;
    }

    public BigDecimal getLaborCost() {
        return laborCost;
    }
    public void setLaborCost(BigDecimal laborCost) {
        this.laborCost = laborCost;
    }
    public BigDecimal getTax() {
        return tax;
    }
    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }
    public BigDecimal getTotal() {
        return total;
    }
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}