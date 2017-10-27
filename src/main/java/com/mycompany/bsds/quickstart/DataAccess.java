package com.mycompany.bsds.quickstart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.sql.CallableStatement;

/**
 * Class used to access RDS.
 */
public class DataAccess {

    private static final String PUBLIC_DNS = "aaevg5ww0x1b7m.cdqh8w1txiil.us-west-2.rds.amazonaws.com";
    private static final String PORT = "3306";
    private static final String DATABASE = "ebdb";
    private static final String REMOTE_DATABASE_USERNAME = "admin";
    private static final String DATABASE_USER_PASSWORD = "adminadmin";

    private Connection connection;

    public DataAccess() {
    }

    /**
     * Establishes a connection to the AWS RDS database.
     */
    public void getAWSConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println(e.getCause());
        }

        Connection myConnection = null;

        try {
            String connectionString = "jdbc:mysql://" + PUBLIC_DNS + ":" + PORT + "/" + DATABASE;
            myConnection = DriverManager.
                    getConnection(connectionString, REMOTE_DATABASE_USERNAME, DATABASE_USER_PASSWORD);
        } catch (SQLException e) {
            System.err.println("Connection Failed!:\n" + e.getMessage());
        }

        if (myConnection == null) {
            System.err.println("Unable to connect to database!");
        } else {
            this.connection = myConnection;
        }
    }

    /**
     * Creates a batch of SQL inserts for the given list of RFIDLiftData objects.
     * @param dataList List of data to write to the DB.
     */
    public void writeRFIDBatchToDatabase(List<RFIDLiftData> dataList) {
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
        } catch (SQLException se) {
            System.err.println(se.getCause());
        } catch (Exception e) {
            System.err.println(e.getCause());
        }
    }

    /**
     * Given a skier and day, retrieves the user's statistics for that day from the DB. 
     * @param skierID ID of the skier
     * @param dayNum number representing the day 
     * @return SkierData object containing the user's statistics
     */
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
            System.err.println(se.getCause());
        } catch (Exception e) {
            //Handle errors for Class.forName
            System.err.println(e.getCause());
        }
        return skierData;
    }
    
    /** Executes a stored proc that will calculate all the user's stats for a given day. 
     * You can find the CREATE PROCEDURE script for the stored proc in 
     * \src\main\resources\calculate_user_stats.sql.
     * @param dayNum day number to calculate all user stats for
     */
    public void executeUserCalculations(int dayNum){
        try {
            CallableStatement cStmt = this.connection.prepareCall("{CALL `ebdb`.`calculate_user_stats`(?)}");
            cStmt.setInt(1, dayNum);
            cStmt.execute();
        } catch (SQLException se) {
            //Handle errors for JDBC
            System.err.println(se.getCause());
        } catch (Exception e) {
            //Handle errors for Class.forName
            System.err.println(e.getCause());
        }

    }

    /**
     * Loads the specified CSV file into the database.
     * @param fileName name of file to bulk load
     * @param dayNum day of the file's data
     */
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
        } catch (IOException | SQLException e) {
            System.err.println(e.getCause());
        }
    }

}
