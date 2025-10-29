package com.flooring.flooringmastery.view;

import com.flooring.flooringmastery.model.Order;
import com.flooring.flooringmastery.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class View {
    private UserIO io;

    @Autowired
    public View(UserIO io) {
        this.io = io;
    }

    // Example methods
    public int displayMainMenuAndGetSelection() {
        io.print("=== Main Menu ===");
        io.print("1. Display Orders");
        io.print("2. Add an Order");
        io.print("3. Edit an Order");
        io.print("4. Remove an Order");
        io.print("5. Export Data");
        io.print("6. Quit");
        return io.readInt("Please select from the above choices: ", 1, 6);
    }
    public LocalDate getDateFromString(String prompt) {
        return io.readDate(prompt);
    }

    public void displayOrders(List<Order> orders) {
        io.print("===== Orders =====");
        for (Order o : orders) {
           displayOrder(o);

        }
        io.print("==================");
    }

    public void displayOrder(Order order) {
        io.print(String.format(
                "Order #%d\nCustomer: %s\nState: %s (Tax %.2f%%)\nProduct: %s\nArea: %.2f sq ft\nTotal: $%.2f\n",
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getState(),
                order.getTaxRate(),
                order.getProductType(),
                order.getArea(),
                order.getTotal()));
    }
    public void displayList(List<String> strings){
        String result = String.join(",", strings);
        io.print(result);

    }
    public void displayProducts(List<Product> products) {
        for (Product product : products) {
            io.print(product.toString());
        }
    }
    public String readString(String prompt) {
       return io.readString(prompt);
    }
    public BigDecimal readBigDecimal(String prompt,BigDecimal min) {
        return io.readBigDecimal(prompt,min);
    }
    public void displayMessage(String message) {
        io.print(message);
    }
    public int readInt(String prompt) {
        return io.readInt(prompt);
    }
    public String readStringAllowEmpty(String prompt) {
        return io.readStringAllowEmpty(prompt);
    }
}
