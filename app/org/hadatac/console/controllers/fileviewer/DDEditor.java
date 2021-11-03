package org.hadatac.console.controllers.fileviewer;

import java.util.List;
import java.util.ArrayList;

import java.nio.file.Path;
import java.nio.file.Paths;

//import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.fileviewer.*;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import play.libs.Json;

import javax.inject.Inject;


public class DDEditor extends Controller {
    public static String headerSheetColumn;
    public static String commentSheetColumn;
    DataFile dirFile = new DataFile("/");
    public static DataFile dd_df;

    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String fileId, boolean bSavable, String dir, Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
        DataFile dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        dirFile.setStatus(DataFile.WORKING);
        if (null == dataFile) {

            return ok(dd_editor.render(dataFile,false,dir,dirFile,user.getEmail()));
        }


        getdd_df(dataFile);
        List<DataFile> files = null;
        String path = ConfigProp.getPathWorking();

        files = DataFile.find(user.getEmail());

    //     String sdd_filename=dataFile.getFileName();
    //     sdd_filename = "S"+sdd_filename; // Only files with the prefix SDD are allowed so were always going to have a second character
    //     DataFile sdd_dataFile = new DataFile(""); // This is being used in place of null but we might want to come up with a better way

    //     for(DataFile df : files){
    //        if(df.getFileName().equals(sdd_filename)){
    //          sdd_dataFile = df;
    //       }
    //    }




        return ok(dd_editor.render(dataFile, bSavable,dir,dirFile,user.getEmail()));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String fileId, boolean bSavable,String dir,Http.Request request) {
        return index(fileId, bSavable,dir,request);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result fromViewableLink(String viewableId, String dir, Http.Request request) {
        DataFile dataFile = DataFile.findByViewableId(viewableId);
        if (null == dataFile) {
            return badRequest("Invalid link!");
        }

        return ok(dd_editor.render(dataFile, false,dir,dirFile,application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postFromViewableLink(String viewableId, String dir, Http.Request request) {
        return fromViewableLink(viewableId, dir,request);
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
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
        List<DataFile> files = DataFile.find(user.getEmail());

        DataFile sdd_dataFile = null;
        for(DataFile df : files){
           String localPath = Paths.get(df.getDir(), df.getFileName()).toString();
           if(localPath.equals(sddFileName)){
             sdd_dataFile = df;
             break;
          }
       }

       return ok(Json.toJson(sdd_dataFile.getId()));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result fromEditableLink(String editableId, String dir,Http.Request request) {
        DataFile dataFile = DataFile.findByEditableId(editableId);
        if (null == dataFile) {
            return badRequest("Invalid link!");
        }


        return ok(dd_editor.render(dataFile, false, dir,dirFile,application.getUserEmail(request)));
    }
    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postFromEditableLink(String editableId, String dir,Http.Request request) {
        return fromEditableLink(editableId, dir, request);
    }
}
