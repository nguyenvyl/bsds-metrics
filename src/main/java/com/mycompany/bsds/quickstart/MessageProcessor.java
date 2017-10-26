/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bsds.quickstart;

import static com.mycompany.bsds.quickstart.MyResource.rawData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author nguyenvyl
 */
public class MessageProcessor {

    public static void checkQueue() {
        Timer timer = new Timer();
        timer.schedule(new ProcessQueue(), 0, 10000);
    }

    static class ProcessQueue extends TimerTask {
        public void run() {
            if (rawData.size() >= 800000) {
                int dayNum = rawData.peek().getDayNum();
                final String fileName = CSVCreator.writeRFIDToCSV(rawData, "tempFile");
                rawData.removeAll(rawData);
                DataAccess dataAccess = new DataAccess();
                dataAccess.loadCSVToDatabase(fileName, dayNum);
                dataAccess.executeUserCalculations(dayNum);
            }
        }
    }

//    // Spawns a bunch of threads for calculating all the user's statistics for the given day.
    public static void startUserCalculations(int dayNum) {
        ArrayList<Integer> startIndicies = new ArrayList<>();
        int i = 1;
        int numSkiers = 40000;
        int requestsPerThread = 400;
        while (i <= numSkiers) {
            startIndicies.add(i);
            i += requestsPerThread;
        }
        ExecutorService executor = Executors.newFixedThreadPool(numSkiers / requestsPerThread);
        for (Integer startIndex : startIndicies) {
            executor.submit(new ProcessCalcBatch(startIndex, startIndex + requestsPerThread, dayNum));
        }

    }

}
