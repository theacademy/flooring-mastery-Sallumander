package com.flooring.flooringmastery.controller;

import com.flooring.flooringmastery.exceptions.PersistenceException;
import com.flooring.flooringmastery.exceptions.NoSuchOrderException;
import com.flooring.flooringmastery.model.Order;
import com.flooring.flooringmastery.service.ServiceLayer;
import com.flooring.flooringmastery.view.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Component
public class Controller {
    private View view;
    private ServiceLayer service;

    @Autowired
    public Controller(View view, ServiceLayer service) {
        this.view = view;
        this.service = service;
    }
    public void run() throws PersistenceException {
        int choice;
        while (true) {
            choice = view.displayMainMenuAndGetSelection();
            switch (choice) {
                case 1:
                    displayOrders();
                    break;
                case 2:
                    addOrder();
                    break;
                case 3:
                    editOrder();
                    break;
                case 4:
                    removeOrder();
                    break;
                case 5:
                    exportData();
                    break;
                case 6:
                    exitMessage();
                    return;
            }
        }

    }
    public void displayOrders() throws PersistenceException {
        LocalDate orderDate = view.getDateFromString("Enter Date of Orders to Display (YYYY-MM-DD)");
        List<Order> orders=service.getOrdersForDate(orderDate);
        view.displayOrders(orders);
    }
    public void addOrder() throws PersistenceException {
        //Ask for date and validate
        LocalDate orderDate = view.getDateFromString("Enter date of order to add (YYYY-MM-DD): ");
        while (orderDate.isBefore(LocalDate.now())) {
            orderDate = view.getDateFromString("Order date must be in the future (YYYY-MM-DD): ");
        }

        // Ask for customer name
        String customerName = view.readString("Enter customer name (letters, -, ., , only): ");
        while(!service.isValidName(customerName))
            customerName=view.readString("Invalid name: Please enter a valid customer name");


        // Ask for state (must exist in tax file)
        view.displayList(service.getAllStateAbbrs());
        String state = view.readString("Enter state abbreviation from available states: ").toUpperCase();
        while (!service.isValidState(state)) {
            state = view.readString("Invalid state. Please Enter state abbreviation from list: ").toUpperCase();
        }

        //  Ask for product type (must be existing type)
        view.displayList(service.getAllProductTypes());
        String productType = view.readString("Enter product type from the list above: ");
        while (!service.isValidProduct(productType)) {
            view.displayMessage("Invalid product type. Please choose one from the list.");
            productType = view.readString("Enter product type: ");
        }

        //Ask for area (minimum 100 sq ft)
        BigDecimal area = view.readBigDecimal("Enter area (must be at least 100 sq ft): ", BigDecimal.valueOf(100));

        // Create a temporary Order object with user input
        Order newOrder = service.createOrder(orderDate, customerName, state, productType, area);

        // Show the calculated summary
        view.displayOrder(newOrder);

        // Ask user to confirm
        
        if (view.readYesNo("Would you like to place this order? (Y/N): ")) {
            // Add the order to storage
            service.addOrder(orderDate, newOrder);
            view.displayMessage("Order successfully added!");
        } else {
            view.displayMessage("Order not saved.");
        }

    }

    public void editOrder() {
            // Get date and order number
            LocalDate date = view.getDateFromString("Enter order date (YYYY-MM-DD): ");
            int orderNum = view.readInt("Enter order number to edit: ");

            try {
                // Get existing order (service will throw NoSuchOrderException if not found)
                Order existingOrder = service.getOrder(date, orderNum);

                //Display current info
                view.displayOrder(existingOrder);

                // Ask for updated fields (blank to keep old)
                String newName = view.readStringAllowEmpty("Enter new customer name (" + existingOrder.getCustomerName() + "): ");
                if (!newName.trim().isEmpty()) {
                    if (service.isValidName(newName)) {
                        existingOrder.setCustomerName(newName);
                    } else {
                        view.displayMessage("Invalid name. Must contain only letters, numbers, commas, or periods.");
                    }
                }

                String newState = view.readStringAllowEmpty("Enter new state (" + existingOrder.getState() + "): ");
                if (!newState.trim().isEmpty()) {
                    // normalize to upper-case because tax state abbreviations are stored upper-case
                    newState = newState.toUpperCase();
                    if (service.isValidState(newState)) {
                        existingOrder.setState(newState);
                    } else {
                        view.displayMessage("Invalid or unsupported state abbreviation.");
                        // clear newState so we don't overwrite later
                        newState = "";
                    }
                }

                String newProduct = view.readStringAllowEmpty("Enter new product type (" + existingOrder.getProductType() + "): ");
                if (!newProduct.trim().isEmpty()) {
                    if (service.isValidProduct(newProduct)) {
                        existingOrder.setProductType(newProduct);
                    } else {
                        view.displayMessage("Product type not found.");
                    }
                }

                String newAreaStr = view.readStringAllowEmpty("Enter new area (" + existingOrder.getArea() + "): ");
                if (!newAreaStr.trim().isEmpty()) {
                    try {
                        BigDecimal newArea = new BigDecimal(newAreaStr);
                        if (newArea.compareTo(new BigDecimal("100")) >= 0) {
                            existingOrder.setArea(newArea);
                        } else {
                            view.displayMessage("Area must be at least 100 sq ft.");
                        }
                    } catch (NumberFormatException e) {
                        view.displayMessage("Invalid number format for area.");
                    }
                }


                // Note: fields were already updated above only when valid; no further unconditional overwrites.

                // Recalculate costs & totals
                Order newOrder=new Order(existingOrder);
                service.calculateOrder(newOrder);

                // Show summary and confirm
                view.displayOrder(newOrder);
                if (view.readYesNo("Save changes? (Y/N): ")) {
                    service.removeOrder(date, orderNum);
                    service.addOrder(date, newOrder);
                    view.displayMessage("Order successfully updated.");
                } else {
                    view.displayMessage("Changes discarded.");
                }

            } catch (NoSuchOrderException e) {
                view.displayMessage("No order found: " + e.getMessage());
            } catch (PersistenceException e) {
                view.displayMessage("Error accessing order data: " + e.getMessage());
            }
        }


    public void removeOrder() {
        LocalDate date = view.getDateFromString("Enter order date (MM-DD-YYYY): ");
        int orderNumber = view.readInt("Enter order number to remove: ");

        try {
            Order order = service.getOrder(date, orderNumber);
            view.displayOrder(order);

            if (view.readYesNo("Are you sure you want to delete this order? (Y/N): ")) {
                service.removeOrder(date, orderNumber);
                view.displayMessage("Order successfully removed.");
            } else {
                view.displayMessage("Deletion cancelled.");
            }
        } catch (NoSuchOrderException e) {
            view.displayMessage("No order found: " + e.getMessage());
        } catch (PersistenceException e) {
            view.displayMessage("Error removing order: " + e.getMessage());
        }
    }

    public void exportData() {
        try {
            service.exportData();
            view.displayMessage("Data exported to FileData/Backup/DataExport.txt");
        } catch (Exception e) {
            view.displayMessage("Export failed: " + e.getMessage());
        }
    }
    public void exitMessage() {
        view.displayMessage("Exiting...");

    }
}
