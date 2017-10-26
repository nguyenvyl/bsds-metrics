package com.mycompany.bsds.quickstart;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.sql.CallableStatement;


public class DataAccess {

    private static final String PUBLIC_DNS = "aaevg5ww0x1b7m.cdqh8w1txiil.us-west-2.rds.amazonaws.com";
    private static final String PORT = "3306";
    private static final String DATABASE = "ebdb";
    private static final String REMOTE_DATABASE_USERNAME = "admin";
    private static final String DATABASE_USER_PASSWORD = "adminadmin";

    private Connection connection;

    public DataAccess() {
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
        System.out.println("Write RFID batch to database");
        Statement statement = null;
        try {
            if (this.connection == null) {
                getAWSConnection();
            }
            for (RFIDLiftData data : dataList) {
                String tableName = "rfid_data_day_" + data.getDayNum();
                connection.setAutoCommit(false);
                statement = this.connection.createStatement();
                String sql = "INSERT INTO " + tableName + "(resortID, dayNum, skierID, liftID, time) VALUES " + data.toSQLString();
                statement.addBatch(sql);
            }
            statement.executeBatch();
            connection.commit();
            statement.clearBatch();
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


    // Given a skier and day, calculates the user's stats for that day and writes the result into a table
    public void calculateUserStats(int skierID, int dayNum) {
        System.out.println("calculate user stats");
        try {
            if (this.connection == null) {
                getAWSConnection();
            }
            String skierIDString = Integer.toString(skierID);
            String dayNumString = Integer.toString(dayNum);
            String sourceTableName = "rfid_data_day_" + Integer.toString(dayNum);
            String destTableName = "user_data_day_" + dayNumString;
            String query = "REPLACE " + destTableName + " (skierID, totalLifts, totalVert, dayNum) "
                    + "SELECT " + skierIDString + " as skierID, "
                    + "SUM(CASE WHEN skierID = " + skierIDString + " THEN 1 ELSE 0 END) AS lifts,"
                    + "SUM(CASE WHEN skierID = " + skierIDString + " AND liftID < 11  THEN 200 ELSE 0 END) + "
                    + "SUM(CASE WHEN skierID = " + skierIDString + " AND liftID > 10 AND liftID < 21 THEN 300 ELSE 0 END) + "
                    + "SUM(CASE WHEN skierID = " + skierIDString + " AND liftID > 20 AND liftID < 31 THEN 400 ELSE 0 END) + "
                    + "SUM(CASE WHEN skierID = " + skierIDString + " AND liftID > 30 AND liftID < 41 THEN 500 ELSE 0 END) AS vert, "
                    + dayNumString + " as dayNum FROM " + sourceTableName;
            Statement statement = this.connection.createStatement();
            System.out.println(query);
            statement.executeUpdate(query);
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    // Given a skier and day, retrieves the user's statistics for that day from the DB. 
    public SkierData getUserData(int skierID, int dayNum) {
        System.out.println("GetUserData");
        SkierData skierData = new SkierData(skierID, dayNum);
        try {
            if (this.connection == null) {
                getAWSConnection();
            }
            String skierIDString = Integer.toString(skierID);
            String tableName = "user_data_day_" + Integer.toString(dayNum);
            String query
                    = "SELECT * FROM " + tableName + " WHERE skierID = " + skierIDString;
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
    
    // Executes a stored proc that will calculate all the user's stats for a given day. 
    public void executeUserCalculations(int dayNum){
        try {
            CallableStatement cStmt = this.connection.prepareCall("{CALL `ebdb`.`calculate_user_stats`(?)}");
            cStmt.setInt(1, dayNum);
            cStmt.execute();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }

    }

    // Loads the specified csv file into the database. 
    public void loadCSVToDatabase(String fileName, int dayNum) {
        try {
            if (this.connection == null) {
                getAWSConnection();
            }
            String property = "java.io.tmpdir";
            String tempDir = System.getProperty(property);
            String path = tempDir + "/" + fileName;
            Statement statement = connection.createStatement();
            String tableName = "rfid_data_day_" + dayNum;
            String query = "LOAD DATA LOCAL INFILE '" + path + "'"
                    + " INTO TABLE " + tableName + " FIELDS TERMINATED BY ',' "
                    + " LINES TERMINATED BY '" + "\\" + "n' IGNORE 1 LINES (resortID, dayNum, skierID, liftID, time);";
            statement.executeUpdate(query);
            Files.deleteIfExists(Paths.get(path));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
