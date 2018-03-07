package org.hadatac.data.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.sparql.function.library.print;

public class CSVRecordFile implements RecordFile {

    private File file;

    public CSVRecordFile(File file) {
        this.file = file;
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

    @Override
    public List<String> getHeaders() {
        try {
            Map<String,Integer> headerMap = CSVFormat.DEFAULT.withHeader().parse(
                    new FileReader(file)).getHeaderMap();

            List<String> headerList = new ArrayList<String>(headerMap.size());
            for (String key : headerMap.keySet()) {
                headerList.add(headerMap.get(key).intValue(), key);
            }

            return headerList;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public boolean isValid() {
        return file != null;
    }
}
