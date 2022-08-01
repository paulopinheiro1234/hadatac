package org.hadatac.data.dsgen;

import org.apache.commons.io.FileUtils;
import org.hadatac.entity.pojo.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GenCSV {

    public static void exec(List<Measurement> measurements,
            List<String> fieldNames, File file, String fileId) {
        try {
            // Create headers
            FileUtils.writeStringToFile(file, String.join(",", fieldNames) + "\n", "utf-8", true);

            // Create rows
            int i = 1;
            int total = measurements.size();
            DataFile dataFile = null;
            for (Measurement m : measurements) {
                if (file.exists()) {
                    FileUtils.writeStringToFile(file, m.toCSVRow(fieldNames) + "\n", "utf-8", true);
                }
                int prev_ratio = 0;
                double ratio = (double)i / total * 100;
                if (((int)ratio) != prev_ratio) {
                    prev_ratio = (int)ratio;

                    dataFile = DataFile.findById(fileId);
                    if (dataFile != null) {
                        if (dataFile.getStatus() == DataFile.DELETED) {
                            dataFile.delete();
                            return;
                        }
                        dataFile.setCompletionPercentage((int)ratio);
                        dataFile.save();
                    } else {
                        return;
                    }
                }
                i++;
            }

            dataFile = DataFile.findById(fileId);
            if (dataFile != null) {
                if (dataFile.getStatus() == DataFile.DELETED) {
                    dataFile.delete();
                    return;
                }
                dataFile.setCompletionPercentage(100);
                dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                dataFile.setStatus(DataFile.CREATED);
                dataFile.save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

