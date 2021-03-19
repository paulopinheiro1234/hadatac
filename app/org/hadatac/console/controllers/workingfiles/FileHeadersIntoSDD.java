package org.hadatac.console.controllers.workingfiles;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLEncoder;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

//import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.workingfiles.*;
import org.hadatac.entity.pojo.DataFile;

import java.util.List;
import java.util.ArrayList;

import org.pac4j.play.java.Secure;
import play.mvc.*;
import play.mvc.Result;

import play.libs.Json;

import javax.inject.Inject;

public class FileHeadersIntoSDD extends Controller {
    public static String headerSheetColumn;
    public static String commentSheetColumn;
    public static DataFile dd_df;

    @Inject
    Application application;

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result createHeaders(String dir, String dd_id, Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

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
        getdd_df(dataFile);
        // dd_id=dataFile.getId();
        // System.out.println(dd_id);
		return ok(fileHeadersIntoSDD.render(dir, dataFile.getFileName(), dirFile,headerSheetColumn,commentSheetColumn,user.getEmail()));
	}

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postCreateHeaders(String dir, String dd_uri, Http.Request request) {
        return createHeaders(dir, dd_uri,request);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result createHeadersForm(String dir, String dd_uri, String sdd_uri) {

    	/* ADD THE DD CONTENT FROM DD_URI INTO THE SDD)URI  */

    	return redirect(routes.WorkingFiles.index(dir, ".", false));
    }
    public Result getdd_df(DataFile d){
        dd_df=d;
        return new Result(200);
    }
    public Result getHeaderLoc(String header_loc){

        headerSheetColumn=header_loc;
        System.out.println(headerSheetColumn);
        return new Result(200);
    }
    public Result getCommentLoc(String desc_loc){
        commentSheetColumn=desc_loc;
        System.out.println(commentSheetColumn);
        return new Result(200);
    }


    public Result getCheckedSDD(String sddFileName, Http.Request request){
        System.out.println("sdd filename: "+sddFileName);
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
        List<DataFile> files = null;
        String path = ConfigProp.getPathWorking();

        files = DataFile.find(user.getEmail());
        String sdd_filename=sddFileName;
        DataFile sdd_dataFile = new DataFile("");
        for(DataFile df : files){
           if(df.getFileName().equals(sdd_filename)){
             sdd_dataFile = df;
          }
       }

       String sdd_id=sdd_dataFile.getId();
       System.out.println("sdd fileid: "+sdd_id);
       return ok(Json.toJson(sdd_id));
    }

}


