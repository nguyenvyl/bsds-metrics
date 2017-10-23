/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bsds.quickstart;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author nguyenvyl
 */
public class ProcessBatch implements Runnable {

    private static final int BATCH_SIZE = 5000;

    List<RFIDLiftData> dataList;
    DataAccess dataAccess;

    public ProcessBatch() {
        dataAccess = new DataAccess();
    }

    @Override
    public void run() {
        List<RFIDLiftData> batch = getBatch(BATCH_SIZE);
        System.out.println("Trying to write batch of size " + batch.size());
        if(batch.size() > 0){
            dataAccess.writeRFIDBatchToDatabase(batch);
        }
    }

    // Takes a specified number of RFIDLiftData objects from the queue
    // for batch processing. 
    public List<RFIDLiftData> getBatch(int batchSize) {
        List<RFIDLiftData> batch = new ArrayList<>();
        int i = 0;
        while (i < batchSize && !MyResource.rawData.isEmpty()) {
            batch.add(MyResource.rawData.poll());
            i++;
        }
        return batch;
    }

}
