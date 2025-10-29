package com.flooring.flooringmastery.model;
import java.math.BigDecimal;

public class Product {
    private String productType;
    private BigDecimal costPerSquareFoot;
    private BigDecimal laborCostPerSquareFoot;

    // Getters, toString, equals, hashCode
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
}
