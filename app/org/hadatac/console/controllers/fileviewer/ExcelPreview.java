package org.hadatac.console.controllers.fileviewer;

//import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.fileviewer.*;
import org.hadatac.entity.pojo.DataFile;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.pac4j.play.java.Secure;
import org.w3c.dom.html.HTMLTableCaptionElement;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;


public class ExcelPreview extends Controller {

    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String fileId, boolean bSavable, Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
        DataFile dataFile = DataFile.findById(fileId);
        if (null == dataFile) {
            return ok(excel_preview.render(dataFile, false, user.getEmail()));
        }
        
        dataFile.updatePermissionByUserEmail(user.getEmail());
        
        if (dataFile.getAllowEditing()) {
            return ok(excel_preview.render(dataFile, bSavable, user.getEmail()));
        }
        
        if (dataFile.getAllowViewing()) {
            return ok(excel_preview.render(dataFile, false, user.getEmail()));
        }
        
        return badRequest("No perview permission!");
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String fileId, boolean bSavable, Http.Request request) {
        return index(fileId, bSavable, request);
    }
    
    @Secure(authorizers = Constants.FILE_VIEWER_EDITOR_ROLE)
    public Result fromViewableLink(String viewableId,Http.Request request) {
        DataFile dataFile = DataFile.findByViewableId(viewableId);
        if (null == dataFile) {
            return badRequest("Invalid link!");
        }
        
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
        if (!user.isDataManager() && !dataFile.getViewerEmails().contains(user.getEmail())) {
            return badRequest("You don't have permission to view this file!");
        }
        
        return ok(excel_preview.render(dataFile, false,user.getEmail()));
    }

    @Secure(authorizers = Constants.FILE_VIEWER_EDITOR_ROLE)
    public Result postFromViewableLink(String viewableId,Http.Request request) {
        return fromViewableLink(viewableId,request);
    }

    @Secure(authorizers = Constants.FILE_VIEWER_EDITOR_ROLE)
    public Result fromEditableLink(String editableId, Http.Request request) {
        DataFile dataFile = DataFile.findByViewableId(editableId);
        if (null == dataFile) {
            return badRequest("Invalid link!");
        }
        
        return ok(excel_preview.render(dataFile, false,application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.FILE_VIEWER_EDITOR_ROLE)
    public Result postFromEditableLink(String editableId, Http.Request request) {
        return fromEditableLink(editableId, request);
    }
}

