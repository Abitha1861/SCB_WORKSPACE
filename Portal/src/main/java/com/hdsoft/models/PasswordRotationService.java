package com.hdsoft.models;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hdsoft.common.Common_Utils;

@Component
public class PasswordRotationService 
{
    @Autowired
    private VaultService vaultService;

    //@Scheduled(fixedRate = 2 * 24 * 60 * 60 * 1000) // Every 2 days
    //@Scheduled(cron = "0/59 * * * * *") 
    public void rotateDatabasePassword() 
    {
        // Generate a new password and rotate it
        //String newPassword = vaultService.generatePassword();
        
        Common_Utils util = new Common_Utils();
        
       // newPassword = util.Generate_Random_onlyString(7);
        
        ////vaultService.rotatePassword(newPassword);
        
        //System.out.println("sch rotate password : "+newPassword);
        // Optionally, reconfigure DataSource with the new password
        // This can be done by recreating the DataSource bean or updating it dynamically
    }
}
