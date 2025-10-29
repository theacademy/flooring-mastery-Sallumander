package com.flooring.flooringmastery.dao;

import com.flooring.flooringmastery.model.Order;
import com.flooring.flooringmastery.exceptions.PersistenceException;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Repository
public class ExportDaoFileImpl implements ExportDao {
    private static final String EXPORT_FILE = "FileData/Backup/DataExport.txt";

    @Override
    public void exportData(Map<LocalDate, Map<Integer, Order>> orders) throws PersistenceException {
        try {
            Path exportPath = Paths.get(EXPORT_FILE);
            if (exportPath.getParent() != null) Files.createDirectories(exportPath.getParent());

            List<String> lines = new ArrayList<>();
            lines.add("OrderDate,OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot,LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total");

            List<LocalDate> dates = new ArrayList<>(orders.keySet());
            Collections.sort(dates);

            for (LocalDate date : dates) {
                Map<Integer, Order> map = orders.get(date);
                if (map == null) continue;
                List<Integer> nums = new ArrayList<>(map.keySet());
                Collections.sort(nums);
                for (Integer num : nums) {
                    Order o = map.get(num);
                    if (o == null) continue;
                    String line = String.join(",",
                            date.toString(),
                            String.valueOf(o.getOrderNumber()),
                            o.getCustomerName(),
                            o.getState(),
                            o.getTaxRate() == null ? "" : o.getTaxRate().toString(),
                            o.getProductType(),
                            o.getArea() == null ? "" : o.getArea().toString(),
                            o.getCostPerSquareFoot() == null ? "" : o.getCostPerSquareFoot().toString(),
                            o.getLaborCostPerSquareFoot() == null ? "" : o.getLaborCostPerSquareFoot().toString(),
                            o.getMaterialCost() == null ? "" : o.getMaterialCost().toString(),
                            o.getLaborCost() == null ? "" : o.getLaborCost().toString(),
                            o.getTax() == null ? "" : o.getTax().toString(),
                            o.getTotal() == null ? "" : o.getTotal().toString()
                    );
                    lines.add(line);
                }
            }

            Files.write(exportPath, lines);
        } catch (IOException e) {
            throw new PersistenceException("Failed to export data to " + EXPORT_FILE, e);
        }
    }
}
