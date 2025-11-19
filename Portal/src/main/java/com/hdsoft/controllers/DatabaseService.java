package com.hdsoft.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hdsoft.configuration.WebConfiguration;

@Service
public class DatabaseService {

    private final WebConfiguration dataSourceConfig;

    @Autowired
    public DatabaseService(WebConfiguration dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }

    public void changeDatabasePassword(String newPassword) {
       // dataSourceConfig.updatePassword(newPassword);
        System.out.println("Password updated successfully.");
    }

    // Other database operations can go here...
}

