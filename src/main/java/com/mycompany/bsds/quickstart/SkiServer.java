package com.mycompany.bsds.quickstart;

import java.sql.BatchUpdateException;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.inject.Singleton;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Singleton
public class SkiServer {

    public static final String RDS_URL = "jdbc:mysql://aaevg5ww0x1b7m.cdqh8w1txiil.us-west-2.rds.amazonaws.com:3306";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "adminadmin";
    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public static final int RDS_MAX_CONNECTIONS = 100;

    private static BasicDataSource dataSource = getDataSource();
    private static final Map<Integer, Integer> liftToHeight = loadMap();
    private ConcurrentLinkedQueue<RequestMetrics> metricsList = new ConcurrentLinkedQueue<>();

    private static BasicDataSource getDataSource() {
        if (dataSource == null) {
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl(RDS_URL);
            ds.setUsername(USERNAME);
            ds.setPassword(PASSWORD);
            ds.setDriverClassName(JDBC_DRIVER);
            ds.setInitialSize(50);
            ds.setMaxTotal(RDS_MAX_CONNECTIONS);
            dataSource = ds;
        }
        return dataSource;
    }

    private static Map<Integer, Integer> loadMap() {

        Map<Integer, Integer> map = new HashMap<>();
        String query = "SELECT * FROM ebdb.LiftHeights";

        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement prepStatement = conn.prepareStatement(query);
            ResultSet rs = prepStatement.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt(1), rs.getInt(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    @GET
    @Path("myvert/{skierId}/{dayNum}")
    @Produces(MediaType.APPLICATION_JSON)
    public SkierData getData(@PathParam("skierId") int skierId,
            @PathParam("dayNum") int dayNum) {
        double responseStartTime = System.currentTimeMillis();
        RequestMetrics metrics = new RequestMetrics();
        metrics.setRequestType("GET");
        String query = "SELECT NumLifts, TotalVert FROM ebdb.SkierStats WHERE(SkierId=" + skierId
                + " AND DayNum=" + dayNum + ");";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int numLifts = 0;
        int totalVert = 0;

        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(query);
            double queryStartTime = System.currentTimeMillis();
            rs = stmt.executeQuery();
            double queryWallTime = System.currentTimeMillis() - queryStartTime;
            metrics.setDbQueryTime(queryWallTime);
            while (rs.next()) {
                numLifts = rs.getInt(1);
                totalVert = rs.getInt(2);
            }
        } catch (SQLException e) {
            metrics.setErrorCode(e.getErrorCode());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (rs != null) {
                    rs.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                metrics.setErrorCode(500);
                e.printStackTrace();
            }

            SkierData data = new SkierData(numLifts, totalVert);
            double responseWallTime = System.currentTimeMillis() - responseStartTime;
            metrics.setResponseTime(responseWallTime);
            ServerSingleton.getInstance().addMetrics(metrics);
//            this.metricsList.add(metrics);
            return data;
        }
    }

    @POST
    @Path("load")
    @Consumes(MediaType.APPLICATION_JSON)
    public int postData(List<RFIDLiftData> liftData) {
        double responseStartTime = System.currentTimeMillis();
        Connection conn = null;
        PreparedStatement prepStatement = null;
        PreparedStatement prepStatement2 = null;
        RequestMetrics metrics = new RequestMetrics();
        metrics.setRequestType("POST");
        String query = "INSERT INTO ebdb.SkierData (ResortId, Day, SkierId, LiftId, Time) VALUES (?,?,?,?,?)";
        String query2 = "INSERT INTO ebdb.SkierStats (SkierId, DayNum, NumLifts, TotalVert) "
                + "VALUES(?,?,1,?) ON DUPLICATE KEY UPDATE NumLifts=NumLifts + 1,TotalVert = TotalVert + ?";
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            prepStatement = conn.prepareStatement(query);
            prepStatement2 = conn.prepareStatement(query2);
            for (RFIDLiftData data : liftData) {
                prepStatement.setInt(1, data.getResortID());
                prepStatement.setInt(2, data.getDayNum());
                prepStatement.setInt(3, data.getSkierID());
                prepStatement.setInt(4, data.getLiftID());
                prepStatement.setInt(5, data.getTime());
                prepStatement.addBatch();

                prepStatement2.setInt(1, data.getSkierID());
                prepStatement2.setInt(2, data.getDayNum());
                prepStatement2.setInt(3, liftToHeight.get(data.getLiftID()));
                prepStatement2.setInt(4, liftToHeight.get(data.getLiftID()));
                prepStatement2.addBatch();
            }
            double queryStartTime = System.currentTimeMillis();
            prepStatement.executeBatch();
            prepStatement2.executeBatch();
            conn.commit();
            double queryWallTime = System.currentTimeMillis() - queryStartTime;
            metrics.setDbQueryTime(queryWallTime);
            prepStatement.clearBatch();
            prepStatement2.clearBatch();
        } catch (SQLException e) {
            metrics.setErrorCode(e.getErrorCode());
            e.printStackTrace();
        } finally {
            try {
                if (prepStatement != null) {
                    prepStatement.close();
                }
                if (prepStatement2 != null) {
                    prepStatement2.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception e) {
                metrics.setErrorCode(500);
                e.printStackTrace();
            }
        }
        double responseWallTime = System.currentTimeMillis() - responseStartTime;
        metrics.setResponseTime(responseWallTime);
        ServerSingleton.getInstance().addMetrics(metrics);
//        this.metricsList.add(metrics);
        return 0;
    }

    @GET
    @Path("metrics")
    @Produces(MediaType.APPLICATION_JSON)
    public ConcurrentLinkedQueue<RequestMetrics> getMetrics() {
        return ServerSingleton.getInstance().getMetrics();
    }

    @GET
    @Path("clear")
    public void clearMetrics() {
        ServerSingleton.getInstance().clearMetrics();
    }
}
