package org.hadatac.console.controllers.fileviewer;

import org.hadatac.console.views.html.fileviewer.*;

import play.mvc.Controller;
import play.mvc.Result;

public class ExcelPreview extends Controller {
    
    public Result index(String folder, String fileName) {
        return ok(excel_preview.render(folder, fileName));      
    }
    
    public Result postIndex(String folder, String fileName) {
        return index(folder, fileName);
    }
}

