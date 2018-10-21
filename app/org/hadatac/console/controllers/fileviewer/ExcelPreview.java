package org.hadatac.console.controllers.fileviewer;

import org.hadatac.utils.ConfigProp;
import org.hadatac.console.views.html.fileviewer.*;

import java.io.File;

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
        
        return ok(excel_preview.render(fileName));
    }
    
    public Result postIndex(String folder, String fileName) {
        return index(folder, fileName);
    }
}

