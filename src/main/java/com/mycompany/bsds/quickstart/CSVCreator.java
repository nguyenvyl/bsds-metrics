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


public class CSVCreator {
        
    /**
     * Utility method that writes a collection of RFID lift data into a temporary CSV file.
     * @param data list of RFIDLiftData objects to write to file
     * @param fileName desired filename
     * @return The name of the created file
     */
    public static String writeRFIDToCSV(Collection<RFIDLiftData> data, String fileName){
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(RFIDLiftData.class).withColumnSeparator(',');
        ObjectWriter myObjectWriter = mapper.writer(schema);
        
        try {
            File tempFile = File.createTempFile(fileName, ".csv");
            FileOutputStream tempFileOutputStream;
            tempFileOutputStream = new FileOutputStream(tempFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(tempFileOutputStream, 1024);
            OutputStreamWriter writerOutputStream = new OutputStreamWriter(bufferedOutputStream, "UTF-8");
            myObjectWriter.writeValue(writerOutputStream, data);
            return tempFile.getName();
        } catch (FileNotFoundException ex) {
            System.err.print(ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            System.err.print(ex.getMessage());
        } catch (IOException ex) {
            System.err.print(ex.getMessage());
        }    
        return "";
    }

}
