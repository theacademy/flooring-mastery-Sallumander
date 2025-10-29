package com.flooring.flooringmastery;

import com.flooring.flooringmastery.exceptions.PersistenceException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.beans.factory.annotation.Autowired;

import com.flooring.flooringmastery.controller.Controller;

@SpringBootApplication
public class FlooringMasteryApp implements CommandLineRunner {

    private final Controller controller;

    @Autowired
    public FlooringMasteryApp(Controller controller) {
        this.controller = controller;
    }

    public static void main(String[] args) {
        SpringApplication.run(FlooringMasteryApp.class, args);
    }


    public void run(String... args) throws PersistenceException {
        controller.run();
    }
}
