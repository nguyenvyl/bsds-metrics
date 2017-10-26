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
    
    private final DataAccess dataAccess;
    private final int startUser;
    private final int endUser;
    private final int dayNum;
    
    public ProcessCalcBatch(int startUser, int endUser, int dayNum) {
        dataAccess = new DataAccess();
        this.startUser = startUser;
        this.endUser = endUser;
        this.dayNum = dayNum;
    }
    
    @Override
    public void run() {
        for(int i = startUser; i < endUser; i++) {
            dataAccess.calculateUserStats(i, dayNum);
        }
    }
}
