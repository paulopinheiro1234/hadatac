package org.hadatac.console.controllers.fileviewer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVParser;
import java.nio.charset.StandardCharsets;

import org.hadatac.utils.ConfigProp;
import org.hadatac.console.views.html.fileviewer.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

import play.mvc.Controller;
import play.mvc.Result;

public class ExcelPreview extends Controller {
    private static String path_proc = ConfigProp.getPathProc();
    private static String path_unproc = ConfigProp.getPathUnproc();

    public Result index(String folder, String fileName) {
        File file = null;
        if (folder.equals("proc")) {
            file = new File(path_proc + fileName);
        } else {
            file = new File(path_unproc + fileName);
        }
        
        return ok(excel_preview.render(file));
    }
    
    public Result postIndex(String folder, String fileName) {
        return index(folder, fileName);
    }
}

