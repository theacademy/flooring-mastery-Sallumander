package com.flooring.flooringmastery.model;
import java.math.BigDecimal;

public class Tax {
    private String state;
    private String stateAbbr;
    private BigDecimal taxRate;

    // Getters, toString, equals, hashCode
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getStateAbbr() {
        return stateAbbr;
    }
    public void setStateAbbr(String stateAbbr) {
        this.stateAbbr = stateAbbr;
    }
    public BigDecimal getTaxRate() {
        return taxRate;
    }
    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }
}
