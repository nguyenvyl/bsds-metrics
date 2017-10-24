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
 * @author Vy
 */
public class ProcessCalcBatch implements Runnable {
    
    private DataAccess dataAccess;
    private int startUser;
    private int endUser;
    private int dayNum;
    
    public ProcessCalcBatch(int startUser, int endUser, int dayNum) {
        dataAccess = new DataAccess();
        this.startUser = startUser;
        this.endUser = endUser;
        this.dayNum = dayNum;
    }
    
    @Override
    public void run() {
        for(int i = startUser; i < endUser; i++) {
            SkierData skier = dataAccess.calculateUserStats(i, dayNum);
            dataAccess.writeSkierToDatabase(skier);
        }

    }



}
