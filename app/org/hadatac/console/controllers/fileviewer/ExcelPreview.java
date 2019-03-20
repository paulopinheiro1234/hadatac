package org.hadatac.console.controllers.fileviewer;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.fileviewer.*;
import org.hadatac.entity.pojo.DataFile;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.mvc.Controller;
import play.mvc.Result;

public class ExcelPreview extends Controller {
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String folder, String fileName, boolean bSavable) {
        if (bSavable) {
            final SysUser user = AuthApplication.getLocalUser(session());
            if (null == DataFile.findByName(user.getEmail(), fileName)) {
                return ok(excel_preview.render(folder, fileName, false));
            }
        }
        
        return ok(excel_preview.render(folder, fileName, bSavable));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String folder, String fileName, boolean bSavable) {
        return index(folder, fileName, bSavable);
    }
}

