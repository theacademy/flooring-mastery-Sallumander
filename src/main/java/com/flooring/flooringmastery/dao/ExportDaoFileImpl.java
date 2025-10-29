package com.flooring.flooringmastery.dao;

import com.flooring.flooringmastery.model.Order;
import com.flooring.flooringmastery.exceptions.PersistenceException;
import com.flooring.flooringmastery.view.UserIO;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Repository
public class ExportDaoFileImpl implements ExportDao {
    private static final String EXPORT_FILE = "FileData/Backup/DataExport.txt";
    private final UserIO userIO;

    public ExportDaoFileImpl(UserIO userIO) {
        this.userIO = userIO;
    }

    @Override
    public void exportData(Map<LocalDate, Map<Integer, Order>> orders) throws PersistenceException {
        try {
            // ensure parent dir exists
            int lastSlash = EXPORT_FILE.lastIndexOf('/');
            if (lastSlash == -1) lastSlash = EXPORT_FILE.lastIndexOf('\\');
            if (lastSlash != -1) {
                String parent = EXPORT_FILE.substring(0, lastSlash);
                userIO.createDirectories(parent);
            }

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
                            o.getCustomerName() == null ? "" : o.getCustomerName(),
                            o.getState() == null ? "" : o.getState(),
                            o.getTaxRate() == null ? "" : o.getTaxRate().toString(),
                            o.getProductType() == null ? "" : o.getProductType(),
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

            userIO.writeLines(EXPORT_FILE, lines);
        } catch (IOException e) {
            throw new PersistenceException("Failed to export data to " + EXPORT_FILE, e);
        }
    }
}
