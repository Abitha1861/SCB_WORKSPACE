package com.hdsoft.models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class Hive2ConnectionExample {

    private static String driverName = "org.apache.hive.jdbc.HiveDriver";
    private static String url = "jdbc:hive2://hklvathdp001.hk.standardchartered.com:2181,hklvathdp001.hk.standardchartered.com:2181,hklvathdp001.hk.standardchartered.com:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2;ssl=true;sslTrustStore=/etc/hive-jks/hivetrust.jks;trustStorePassword=SCBpassword@123;transportMode=http;httpPath=cliservice;";
    private static String user = "g.gbanalysapp.001";
    private static String password = "SCBPassword@123";

    public static void main(String[] args)
    {
        try 
        {
            Class.forName(driverName);

            // Establish the connection
            Connection con = DriverManager.getConnection(url, user, password);
            Statement stmt = con.createStatement();
            
            System.out.println("Database connection success");

            // Execute a query
            String sql = "SELECT CURRENT_DATE";
            ResultSet res = stmt.executeQuery(sql);

            // Process the result set
            while (res.next()) 
            {
                System.out.println("Current Date: " + res.getDate(1));
            }

            // Close the connections
            res.close();
            stmt.close();
            con.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

