package org.hadatac.console.controllers.fileviewer;

import org.hadatac.utils.ConfigProp;
import org.hadatac.console.views.html.fileviewer.*;

import java.io.File;

import play.mvc.Controller;
import play.mvc.Result;

public class ExcelPreview extends Controller {
    
    public Result index(String folder, String fileName) {
        File file = null;
        if (folder.equals("proc")) {
            return ok(excel_preview.render(fileName, true));
        } else {
            return ok(excel_preview.render(fileName, false));
        }        
    }
    
    public Result postIndex(String folder, String fileName) {
        return index(folder, fileName);
    }
}

