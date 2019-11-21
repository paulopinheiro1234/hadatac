package org.hadatac.console.controllers.fileviewer;

import java.util.List;
import java.util.ArrayList;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.fileviewer.*;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.mvc.Controller;
import play.mvc.Result;

import play.libs.Json;



public class DDEditor extends Controller {
    public static String headerSheetColumn;
    public static String commentSheetColumn;
    DataFile dirFile = new DataFile("/");
    public static DataFile dd_df;
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))    
    public Result index(String fileId, boolean bSavable,String dir) {
        final SysUser user = AuthApplication.getLocalUser(session());
        DataFile dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        dirFile.setStatus(DataFile.WORKING);
        if (null == dataFile) {

            return ok(dd_editor.render(dataFile,false,dir,dirFile));
        }
        
        
        getdd_df(dataFile);
        List<DataFile> files = null;
        String path = ConfigProp.getPathDownload();

        files = DataFile.find(user.getEmail());

    //     String sdd_filename=dataFile.getFileName();
    //     sdd_filename = "S"+sdd_filename; // Only files with the prefix SDD are allowed so were always going to have a second character
    //     DataFile sdd_dataFile = new DataFile(""); // This is being used in place of null but we might want to come up with a better way

    //     for(DataFile df : files){
    //        if(df.getFileName().equals(sdd_filename)){
    //          sdd_dataFile = df;
    //       }
    //    }

        

        
        return ok(dd_editor.render(dataFile, bSavable,dir,dirFile));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String fileId, boolean bSavable,String dir) {
        return index(fileId, bSavable,dir);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result fromSharedLink(String sharedId,String dir) {
        DataFile dataFile = DataFile.findBySharedId(sharedId);
        if (null == dataFile) {
            return badRequest("Invalid link!");
        }

        return ok(dd_editor.render(dataFile, false,dir,dirFile));
    }

    public Result postFromSharedLink(String sharedId,String dir) {
        return fromSharedLink(sharedId,dir);
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
   
    
    public Result getCheckedSDD(String sddFileName){
        System.out.println("sdd filename: "+sddFileName);
        final SysUser user = AuthApplication.getLocalUser(session());
        List<DataFile> files = null;
        String path = ConfigProp.getPathDownload();

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
