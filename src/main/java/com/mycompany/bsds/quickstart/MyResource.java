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
import javax.ws.rs.core.Response;

@Path("myresource")
public class MyResource {
    public static ConcurrentLinkedQueue<RFIDLiftData> rawData = new ConcurrentLinkedQueue();
    public MessageProcessor messageProcessor;

    // On server load, start the process that regularly checks the queue size.
    static {
        MessageProcessor.checkQueue();
    }
    
    /**
     * GET endpoint to retrieve a user's statistics.
     *
     * @param skierID the skier's ID
     * @param dayNum the day represented by a number
     * @return Response containing status code and SkierData object
     */
    @GET
    @Path("myvert")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIt(
            @QueryParam("skierID") int skierID,
            @QueryParam("dayNum") int dayNum) 
    {
        if(this.messageProcessor == null) {
            this.messageProcessor = new MessageProcessor();
        }
        SkierData skierData = this.messageProcessor.processGet(skierID, dayNum);
        return Response
                .status(200)
                .entity(skierData)
                .build();
    }

    
     /**
     * Consumes a post request and stores the data into a queue. 
     *
     * @param liftData list of RFIDLiftObjects to write to the DB.
     * @return a Response
     */
    @POST
    @Path("/loadBatch")
    @Consumes("application/json")
    public int postBatch(List<RFIDLiftData> liftData) {
        rawData.addAll(liftData);
        return rawData.size();
    }
    
}
