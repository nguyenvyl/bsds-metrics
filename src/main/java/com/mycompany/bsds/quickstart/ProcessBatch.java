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

    private static int BATCH_SIZE = 1000;

    List<RFIDLiftData> dataList;
    DataAccess dataAccess;

    public ProcessBatch() {
        dataAccess = new DataAccess();
    }

    @Override
    public void run() {
        List<RFIDLiftData> batch = getBatch(BATCH_SIZE);
        if(batch.size() > 0){
            dataAccess.writeRFIDBatchToDatabase(batch);
        }
    }

    // Takes a specified number of RFIDLiftData objects from the queue
    // for batch processing. 
    public List<RFIDLiftData> getBatch(int batchSize) {
        List<RFIDLiftData> batch = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            if (!MyResource.rawData.isEmpty()) {
                batch.add(MyResource.rawData.poll());
            }
        }
        return batch;
    }

}
