/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bsds.quickstart;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vy
 */
public class CSVCreator {
        
    public static String writeRFIDToCSV(Collection<RFIDLiftData> data, String fileName){
        // create mapper and schema
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(RFIDLiftData.class).withColumnSeparator(',');

        // output writer
        ObjectWriter myObjectWriter = mapper.writer(schema);
        
        try {
            File tempFile = File.createTempFile(fileName, ".csv");
            FileOutputStream tempFileOutputStream;
            tempFileOutputStream = new FileOutputStream(tempFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(tempFileOutputStream, 1024);
            OutputStreamWriter writerOutputStream = new OutputStreamWriter(bufferedOutputStream, "UTF-8");
            myObjectWriter.writeValue(writerOutputStream, data);
            System.out.println("File written to system!");
            System.out.println("File Name: " + tempFile.getName());
            return tempFile.getName();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CSVCreator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(CSVCreator.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CSVCreator.class.getName()).log(Level.SEVERE, null, ex);
        }    
        return "";
    }

}
