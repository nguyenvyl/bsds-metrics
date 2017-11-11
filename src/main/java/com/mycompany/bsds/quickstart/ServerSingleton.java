/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bsds.quickstart;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerSingleton {
    private static ServerSingleton instance;
    private ConcurrentLinkedQueue<RequestMetrics> metricsList;
//    private String instanceID;
    
    private ServerSingleton(){}
    
    public static synchronized ServerSingleton getInstance(){
        if(instance == null){
            synchronized (ServerSingleton.class) {
                if(instance == null){
                    instance = new ServerSingleton();
                }
            }
        }
        return instance;
    }
    
    public synchronized void addMetrics(RequestMetrics metrics) {
        this.metricsList.add(metrics);
    } 
    
    public synchronized ConcurrentLinkedQueue<RequestMetrics> getMetrics() {
        return this.metricsList;
    }
    
    public synchronized void clearMetrics() {
        this.metricsList.removeAll(this.metricsList);
    }
    
}
