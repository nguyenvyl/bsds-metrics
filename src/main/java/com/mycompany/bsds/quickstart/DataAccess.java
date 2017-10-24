package com.mycompany.bsds.quickstart;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DataAccess {

    private static final String PUBLIC_DNS = "aaevg5ww0x1b7m.cdqh8w1txiil.us-west-2.rds.amazonaws.com";
    private static final String PORT = "3306";
    private static final String DATABASE = "ebdb";
    private static final String REMOTE_DATABASE_USERNAME = "admin";
    private static final String DATABASE_USER_PASSWORD = "adminadmin";


    private Connection connection;

    public DataAccess() {
        getAWSConnection();
    }

    public void getAWSConnection() {

        System.out.println("----MySQL JDBC Connection Testing -------");

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Where is your MySQL JDBC Driver?");
            e.printStackTrace();
        }

        System.out.println("MySQL JDBC Driver Registered!");
        Connection connection = null;

        try {
            String connectionString = "jdbc:mysql://" + PUBLIC_DNS + ":" + PORT + "/" + DATABASE;
            connection = DriverManager.
                    getConnection(connectionString, REMOTE_DATABASE_USERNAME, DATABASE_USER_PASSWORD);
        } catch (SQLException e) {
            System.out.println("Connection Failed!:\n" + e.getMessage());
        }

        if (connection == null) {
            System.out.println("Unable to connect to database!");
        } else {
            System.out.println("Database connected!");
            this.connection = connection;
        }
    }


    public void writeRFIDBatchToDatabase(List<RFIDLiftData> dataList) {
        Statement statement = null;
        try {
            if(this.connection == null) {
                getAWSConnection();
            }
            for (RFIDLiftData data : dataList) {
                String tableName = "rfid_data_day_" + data.getDayNum();
                connection.setAutoCommit(false);
                statement = this.connection.createStatement();
                String sql = "INSERT INTO " + tableName + " VALUES " + data.toSQLString();
                statement.addBatch(sql);
            }
            statement.executeBatch();
            connection.commit();
            System.out.println("Batch successfully committed");
        } catch (SQLException se) {
            System.out.println("Batch failed; SQL exception");

            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            System.out.println("Batch failed; general exception");

            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }


    // Writes a single user to the database. 
    public void writeSkierToDatabase(SkierData skierData) {
        Statement statement;
        try {
            if(this.connection == null) {
                getAWSConnection();
            }
            String tableName = "user_data_day_" + Integer.toString(skierData.getDayNum());
            statement = this.connection.createStatement();
            String sql = "INSERT INTO " + tableName + " VALUES " + skierData.toSQLString() + 
                    " ON duplicate key update totalLifts = " + skierData.getTotalLifts() + ", totalVert = " + skierData.getTotalVert();
            statement.executeUpdate(sql);
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }
    
    // Given a skier and day, calculates the user's stats for that day and returns
    // their data.
    public SkierData calculateUserStats(int skierID, int dayNum) {
        SkierData skierData = new SkierData(skierID, dayNum);
        try {
            if(this.connection == null) {
                getAWSConnection();
            }
            String skierIDString = Integer.toString(skierID);
            String tableName = "rfid_data_day_" + Integer.toString(dayNum);

            String query = 
                "SELECT SUM(CASE WHEN skierID = " + skierIDString + " THEN 1 ELSE 0 END) AS totalLifts " + 
                ",SUM(CASE WHEN skierID = " + skierIDString + " AND liftID < 11  THEN 1 ELSE 0 END) AS lift200m_count " +
                ",SUM(CASE WHEN skierID = " + skierIDString + " AND liftID > 10 AND liftID < 21 THEN 1 ELSE 0 END) AS lift300m_count " +
                ",SUM(CASE WHEN skierID = " + skierIDString + " AND liftID > 20 AND liftID < 31 THEN 1 ELSE 0 END) AS lift400m_count " + 
                ",SUM(CASE WHEN skierID = " + skierIDString + " AND liftID > 30 AND liftID < 41 THEN 1 ELSE 0 END) AS lift500m_count " +
                "  FROM " + tableName;
            Statement statement = this.connection.createStatement();
//            System.out.println(query);
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            int totalLifts = rs.getInt("totalLifts");
            int total200 = rs.getInt("lift200m_count") * 200;
            int total300 = rs.getInt("lift300m_count") * 300;
            int total400 = rs.getInt("lift400m_count") * 400;
            int total500 = rs.getInt("lift500m_count") * 500;
            int totalVert = total200 + total300 + total400 + total500;
            skierData.setTotalLifts(totalLifts);
            skierData.setTotalVert(totalVert);
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return skierData;
    }
    
    // Given a skier and day, retrieves the user's statistics for that day from the DB. 
    public SkierData getUserData(int skierID, int dayNum) {
        SkierData skierData = new SkierData(skierID, dayNum);
        try {
            if(this.connection == null) {
                getAWSConnection();
            }
            String skierIDString = Integer.toString(skierID);
            String tableName = "user_data_day_" + Integer.toString(dayNum);
            String query = 
                "SELECT * FROM " + tableName + " WHERE skierID = " + skierIDString;
            Statement statement = this.connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            int totalLifts = rs.getInt("totalLifts");
            int totalVert = rs.getInt("totalVert");
            skierData.setTotalLifts(totalLifts);
            skierData.setTotalVert(totalVert);
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return skierData;
    }
    
    

}
