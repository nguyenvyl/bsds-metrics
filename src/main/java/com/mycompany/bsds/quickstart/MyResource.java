package com.mycompany.bsds.quickstart;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Root resource (exposed at "myresource" path)
 */
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("myresource")
public class MyResource {
    public static ConcurrentLinkedQueue<RFIDLiftData> rawData = new ConcurrentLinkedQueue();
    
    static {
        MessageProcessor.startMessageProcessor();
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
    public Response getIt(
            @QueryParam("skierID") int skierID,
            @QueryParam("dayNum") int dayNum) 
    {
        Response response = Response.status(Response.Status.OK).build();
        return response;
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
        System.out.println("Post batch hit!");
        rawData.addAll(liftData);
        return rawData.size();
    }
    
    @GET
    @Path("/queuesize")
    public int queuesize() {
        return rawData.size();
    }
}
