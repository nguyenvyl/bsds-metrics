package com.mycompany.bsds.quickstart;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Root resource (exposed at "myresource" path)
 */
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.QueryParam;

@Path("myresource")
public class MyResource {
    public static ConcurrentLinkedQueue<RFIDLiftData> rawData = new ConcurrentLinkedQueue();
    public static final int flag = 0;

//    
    static {
        MessageProcessor.checkQueue();
    }
    
    /**
     * To retrieve a message
     *
     * @param skierID
     * @param dayNum
     * @return a string
     */
    @GET
    @Path("myvert")
    @Produces(MediaType.APPLICATION_JSON)
    public SkierData getIt(
            @QueryParam("skierID") int skierID,
            @QueryParam("dayNum") int dayNum) 
    {
        return new SkierData();
//        return dataAccess.getUserData(skierID, dayNum);
    }

    /**
     * Consumes a post request and stores the data into a queue. 
     *
     * @param liftData
     * @param message a message
     * @return a Response
     */
    @POST
    @Path("/load")
    @Consumes("application/json")
    public int postIt(RFIDLiftData liftData) {
        rawData.add(liftData);
        return rawData.size();
    }
    
        /**
     * Consumes a post request and stores the data into a queue. 
     *
     * @param liftData
     * @param message a message
     * @return a Response
     */
    @POST
    @Path("/loadBatch")
    @Consumes("application/json")
    public int postBatch(List<RFIDLiftData> liftData) {
        rawData.addAll(liftData);
//        if(rawData.size() >= 80000) {
//            int dayNum = liftData.get(0).getDayNum();
//            processQueue(dayNum);
//        }
        return rawData.size();
    }
    
    @POST
    @Path("/loadUsers")
    @Consumes(MediaType.TEXT_PLAIN)
    public void loadUsers(int dayNum) {
        MessageProcessor.startUserCalculations(dayNum);
    }
    
    @GET
    @Path("/queuesize")
    public int queuesize() {
        return rawData.size();
    }
    
    @GET
    @Path("/emptyBatch")
    public int emptyBatch() {
        rawData.removeAll(rawData);
        return rawData.size();
    }
    
//    private void processQueue(final int dayNum) {
//        System.out.println("Process queue!");
//        final String fileName = CSVCreator.writeRFIDToCSV(rawData, "tempFile");
//        rawData.removeAll(rawData);
//        new Thread(new Runnable() {
//            public void run() {
//                DataAccess dataAccess = new DataAccess();
//                dataAccess.loadCSVToDatabase(fileName, dayNum);
//            }
//        }).start();
//    }
}
