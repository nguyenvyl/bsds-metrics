package com.mycompany.bsds.metrics;

import com.google.gson.Gson;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class SkiServer {

  public static final String RDS_URL = "jdbc:mysql://aaevg5ww0x1b7m.cdqh8w1txiil.us-west-2.rds.amazonaws.com:3306";
  public static final String USERNAME = "admin";
  public static final String PASSWORD = "adminadmin";
  public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
  public static final int RDS_MAX_CONNECTIONS = 100;



  @POST
  @Path("addstats")
  @Consumes(MediaType.APPLICATION_JSON)
  public void addStats() {

  }


  @POST
  @Path("getstats")
  @Produces(MediaType.APPLICATION_JSON)
  public void getStats(String json) {

  }

}