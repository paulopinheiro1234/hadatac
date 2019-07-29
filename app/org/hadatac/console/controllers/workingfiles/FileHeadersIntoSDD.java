package org.hadatac.console.controllers.workingfiles;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLEncoder;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.workingfiles.*;
import org.hadatac.entity.pojo.DataFile;

import play.mvc.*;
import play.mvc.Result;

public class FileHeadersIntoSDD extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result createHeaders(String dir, String dd_id, String headerSheetColumn, String commentSheetColumn) {
        final SysUser user = AuthApplication.getLocalUser(session());

    	DataFile dataFile = null;
        if (user.isDataManager()) {
            dataFile = DataFile.findById(dd_id);
        } else {
            dataFile = DataFile.findByIdAndEmail(dd_id, user.getEmail());
        }

        if (null == dataFile) {
            return badRequest("You do NOT have the permission to operate this file!");
        }

        DataFile dirFile = new DataFile("/");
        dirFile.setStatus(DataFile.WORKING);

		return ok(fileHeadersIntoSDD.render(dir, dataFile.getFileName(), dirFile, headerSheetColumn, commentSheetColumn));
	}

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postCreateHeaders(String dir, String dd_uri, String headerSheetColumn, String commentSheetColumn) {
        return createHeaders(dir, dd_uri, headerSheetColumn, commentSheetColumn);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result createHeadersForm(String dir, String dd_uri, String sdd_uri, String headerSheetColumn, String commentSheetColumn) {

    	/* ADD THE DD CONTENT FROM DD_URI INTO THE SDD)URI  */

    	return redirect(routes.WorkingFiles.index(dir, "."));
    }

}
