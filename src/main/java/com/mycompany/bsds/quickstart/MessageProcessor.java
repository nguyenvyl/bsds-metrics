/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bsds.quickstart;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author nguyenvyl
 */
public class MessageProcessor {
    private static int THREAD_POOL_SIZE = 300;
    private static int THREAD_DELAY = 200;
    
    
    public static void startMessageProcessor() {
        Runnable processBatch = new ProcessBatch();
        ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
        scheduledPool.scheduleAtFixedRate(processBatch, 10, THREAD_DELAY, TimeUnit.MILLISECONDS);
    }
    
}
