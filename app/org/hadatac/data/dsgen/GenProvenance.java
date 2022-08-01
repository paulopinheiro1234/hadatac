package org.hadatac.data.dsgen;

import org.apache.commons.io.FileUtils;
import org.hadatac.entity.pojo.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GenProvenance {

    public static void exec(Alignment alignment, File file, String ownerEmail, String dataDir) {
        try {
            String fileName = "download_" + file.getName().substring(7, file.getName().lastIndexOf("_")) + "_sources.csv";
            Date date = new Date();
            DataFile dataFile = DataFile.create(fileName, dataDir, ownerEmail, DataFile.CREATING);
            dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date));
            dataFile.save();

            // Write empty string to create the file
            File provenanceFile = new File(dataFile.getAbsolutePath());
            FileUtils.writeStringToFile(provenanceFile, "", "utf-8", true);

            //System.out.println("Sources file  [" + provenanceFile.getName() + "]");

            FileUtils.writeStringToFile(provenanceFile, "used_DOI\n", "utf-8", true);
            // Write provenance file
            List<String> provenance = alignment.getDOIs();
            provenance.sort(Comparator.comparing( String::toString));
            for (String prov : provenance) {
                FileUtils.writeStringToFile(provenanceFile, prov + "\n", "utf-8", true);
            }

            dataFile.setCompletionPercentage(100);
            dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            dataFile.setStatus(DataFile.CREATED);
            dataFile.save();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}

