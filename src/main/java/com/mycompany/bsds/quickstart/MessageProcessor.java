package com.mycompany.bsds.quickstart;

import static com.mycompany.bsds.quickstart.MyResource.rawData;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author nguyenvyl
 */
public class MessageProcessor {
    
    private DataAccess dataAccess;
    
    /**
     * Checks if the queue needs to be processed every 10 seconds.
     */
    public static void checkQueue() {
        Timer timer = new Timer();
        timer.schedule(new ProcessQueue(), 0, 10000);
    }
    
    /**
     * Processes a get request.
     * @param skierID skier ID to retrieve data for
     * @param dayNum day to retrieve data for.
     * @return SkierData representing the given user's stats for the day.
     */
    public SkierData processGet(int skierID, int dayNum) {
        if(this.dataAccess == null) {
            this.dataAccess = new DataAccess();
        }
        SkierData skierData = dataAccess.getUserData(skierID, dayNum);
        return skierData;
    }

    /**
      Timer Task that processes the queue. If it's over 800,000, it writes the queue
      * into a CSV file, empties the queue, loads the file to the DB, and then deletes
      * the CSV file.
     */
    static class ProcessQueue extends TimerTask {
        @Override
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


}
