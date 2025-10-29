package com.flooring.flooringmastery.dao;

import com.flooring.flooringmastery.exceptions.PersistenceException;
import com.flooring.flooringmastery.model.Tax;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TaxDaoFileImplTest {

    @Test
    void testGetTaxByState() throws PersistenceException {
        TaxDaoFileImpl dao = new TaxDaoFileImpl();
        Tax tx = dao.getTaxByState("TX");
        assertNotNull(tx, "TX tax should be present in Taxes.txt");
        assertEquals("TX", tx.getStateAbbr());
        assertEquals(new BigDecimal("4.45"), tx.getTaxRate());
    }

    @Test
    void testGetAllTaxes() throws PersistenceException {
        TaxDaoFileImpl dao = new TaxDaoFileImpl();
        assertTrue(dao.getAllTaxes().size() >= 1);
    }
}
