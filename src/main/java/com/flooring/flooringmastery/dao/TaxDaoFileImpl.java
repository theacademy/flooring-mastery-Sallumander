package com.flooring.flooringmastery.dao;

import com.flooring.flooringmastery.model.Tax;
import com.flooring.flooringmastery.exceptions.PersistenceException;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;

@Repository
public class TaxDaoFileImpl implements TaxDao {

    //set static variables in all caps so easy to change file or splitter char
    private static final String TAX_FILE = "FileData/Data/Taxes.txt";
    private static final String DELIMITER = ",";

    private final Map<String, Tax> taxes = new HashMap<>();

    public TaxDaoFileImpl() {
        try{
            loadTaxes();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Tax> getAllTaxes() throws PersistenceException {
        if (taxes.isEmpty()) {
            loadTaxes();
        }
        return new ArrayList<>(taxes.values());
    }

    @Override
    public Tax getTaxByState(String stateAbbreviation) throws PersistenceException {
        if (taxes.isEmpty()) {
            loadTaxes();
        }
        return taxes.get(stateAbbreviation.toUpperCase());
    }

    private void loadTaxes() throws PersistenceException {
        try {
            List<String> lines = Files.readAllLines(Paths.get(TAX_FILE));
            lines.remove(0);

            for (String line : lines) {
                String[] tokens = line.split(DELIMITER);
                if (tokens.length < 3) continue;

                Tax tax = new Tax();
                tax.setStateAbbr(tokens[0]);
                tax.setState(tokens[1]);
                tax.setTaxRate(new BigDecimal(tokens[2]));

                taxes.put(tax.getStateAbbr().toUpperCase(), tax);
            }
        } catch (IOException e) {
            throw new PersistenceException("Could not load tax data from " + TAX_FILE, e);
        }
    }
}
