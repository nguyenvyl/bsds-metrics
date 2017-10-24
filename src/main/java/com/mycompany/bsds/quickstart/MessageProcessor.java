/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bsds.quickstart;

import java.util.ArrayList;
import java.util.Arrays;
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
    private final static int THREAD_POOL_SIZE = 200;
    private final static int THREAD_DELAY = 100;
    
    // Starts a scheduled executor service that constantly processes any POST requests waiting 
    // in the queue to be written to the database. 
//    public static void startMessageProcessor() {
//        Runnable processBatch = new ProcessPostBatch();
//        ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
//        scheduledPool.scheduleAtFixedRate(processBatch, 10, THREAD_DELAY, TimeUnit.MILLISECONDS);
//    }
    
    
//    // Spawns a bunch of threads for calculating all the user's statistics for the given day.
      public static void startUserCalculations(int dayNum) {
          ArrayList<Integer> startIndicies = new ArrayList<>();
          int i = 1;
          int numSkiers = 40000;
          int requestsPerThread = 400;
          while(i <= numSkiers) {
              startIndicies.add(i);
              i += requestsPerThread;
          }
          ExecutorService executor = Executors.newFixedThreadPool(numSkiers / requestsPerThread);
          for(Integer startIndex : startIndicies) {
              executor.submit(new ProcessCalcBatch(startIndex, startIndex+requestsPerThread, dayNum));
          }
          
      }
    
}
