package org.hadatac.data.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;


public class CSVRecordFile implements RecordFile {

    private File file;
    private int numberOfRows;
    private List<String> headers;

    public CSVRecordFile(File file) {
        this.file = file;
        init();
    }
    
    private void init() {
        try {
            CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(new FileReader(file));
            Map<String, Integer> headerMap = parser.getHeaderMap();

            headers = new ArrayList<String>(headerMap.size());
            for (String key : headerMap.keySet()) {
                headers.add(headerMap.get(key).intValue(), key);
            }
            
            numberOfRows = parser.getRecords().size();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Record> getRecords() {
        List<CSVRecord> records = null;
        try {
            records = CSVFormat.DEFAULT.withHeader().parse(new FileReader(file)).getRecords();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return records.stream().map(rec -> {
            return new CSVFileRecord(rec);
        }).collect(Collectors.toList());
    }
    
    public int getNumberOfSheets() {
        return 1;
    }

    @Override
    public List<String> getHeaders() {
        return headers;
    }

    @Override
    public File getFile() {
        return file;
    }
    
    @Override
    public String getFileName() {
        return file.getName();
    }
    
    @Override
    public String getSheetName() {
        return "";
    }

    @Override
    public boolean isValid() {
        return file != null;
    }

    @Override
    public int getNumberOfRows() {
        return numberOfRows;
    }
}
