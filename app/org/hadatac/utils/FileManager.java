package org.hadatac.utils;

import java.util.HashMap;
import java.util.Map;

public class FileManager {
    
    public static String UNPROC = "unproc";
    public static String PROC = "proc";
    // public static String DOWNLOAD = "download";
    public static String WORKING = "working";
    
    private static FileManager manager = null;
    private Map<String, Path> mapLabelToPath = new HashMap<String, Path>();
    
    private FileManager() {
        addPath(new Path(UNPROC, /*ConfigProp.AUTOANNOTATOR_CONFIG_FILE*/ "hadatac.autoccsv", "path_unproc"));
        addPath(new Path(PROC, /*ConfigProp.AUTOANNOTATOR_CONFIG_FILE*/ "hadatac.autoccsv", "path_proc"));
        // addPath(new Path(DOWNLOAD, /*ConfigProp.AUTOANNOTATOR_CONFIG_FILE*/ "hadatac.autoccsv", "path_download"));
        addPath(new Path(WORKING, /*ConfigProp.AUTOANNOTATOR_CONFIG_FILE*/ "hadatac.autoccsv", "path_working"));
    }
    
    public static FileManager getInstance() { 
        if (manager == null) {
            manager = new FileManager(); 
        }
  
        return manager; 
    } 
    
    public void addPath(Path path) {
        if (!mapLabelToPath.containsKey(path.getLabel())) {
            mapLabelToPath.put(path.getLabel(), path);
        }
    }
    
    public String getPathByLabel(String label) {
        String path = "";
        
        if (mapLabelToPath.containsKey(label)) {
            path = mapLabelToPath.get(label).getPath();
        }
        
        return path;
    }
}
