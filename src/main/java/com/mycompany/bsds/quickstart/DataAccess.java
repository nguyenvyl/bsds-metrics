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

    private static final String userQuery = "SELECT lift_id, COUNT(*) FROM rides WHERE skier_id=? AND day=? GROUP BY lift_id";

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

//    public void writeRFIDDataToDatabase(RFIDLiftData liftData) {
//        Statement statement = null;
//        String tableName = "rfid_data_day_" + liftData.getDayNum();
//        try {
//            String dayNum = Integer.toString(liftData.getDayNum());
//            statement = this.connection.createStatement();
//            String sql = "INSERT INTO " + tableName + " VALUES " + liftData.toSQLString();
//            System.out.println(sql);
//            int result = statement.executeUpdate(sql);
//            System.out.println(Integer.toString(result));
//        } catch (SQLException se) {
//            //Handle errors for JDBC
//            se.printStackTrace();
//        } catch (Exception e) {
//            //Handle errors for Class.forName
//            e.printStackTrace();
//        }
//    }

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


    /**
     * One time use to load user IDs into user table
     *
     * @param liftData
     */
    public void writeUserIdToDatabase(RFIDLiftData liftData) {
        Statement statement;
        try {
            statement = this.connection.createStatement();
            String sql = "INSERT INTO user_data VALUES (" + Integer.toString(liftData.getSkierID()) + ", 0,0)";
            System.out.println(sql);
            statement.executeUpdate(sql);
//            System.out.println(Integer.toString(result));
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

}
