package org.hadatac.workingfiles.loader;

import org.hadatac.entity.pojo.DataFile;
import org.hadatac.utils.ConfigProp;


public class WorkingFilesWorker {

    public WorkingFilesWorker() {}

    public static void scan() {
        DataFile.includeUnrecognizedFiles(
                ConfigProp.getPathWorking(), "",
                DataFile.findByStatus(DataFile.WORKING),
                ConfigProp.getDefaultOwnerEmail(),
                DataFile.WORKING);
    }
}
