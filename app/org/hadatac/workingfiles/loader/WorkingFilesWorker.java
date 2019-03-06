package org.hadatac.workingfiles.loader;

import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.utils.ConfigProp;

public class WorkingFilesWorker {

    public WorkingFilesWorker() {}

    public static void scan() {
        DataFile.includeUnrecognizedFiles(
                ConfigProp.getPathWorking(), 
                ConfigProp.getDefaultOwnerEmail());
    }

    public static void process() {

        String pathWorking = ConfigProp.getPathWorking();
        List<DataFile> workingFiles = DataFile.findAll(DataFile.WORKING);
        DataFile.filterNonexistedFiles(pathWorking, workingFiles);

        workingFiles.sort(new Comparator<DataFile>() {
            @Override
            public int compare(DataFile o1, DataFile o2) {
                return o1.getLastProcessTime().compareTo(o2.getLastProcessTime());
            }
        });

        for (DataFile dataFile : workingFiles) {
            dataFile.setLastProcessTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            dataFile.save();

            String fileName = dataFile.getFileName();
            String filePath = pathWorking + "/" + fileName;

            /*
            if (workingFiles.contains(dataFile)) {
                AnnotationLog.printException(String.format(
                        "Already processed a file with the same name %s . "
                                + "Please delete the old file before moving forward ", fileName), 
                        fileName);
                return;
            }*/

        }
    }
}
