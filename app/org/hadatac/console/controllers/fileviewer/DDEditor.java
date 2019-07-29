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
   String headerSheetColumn="*";
   String commentSheetColumn="*";
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))

    
    public Result index(String fileId, boolean bSavable,String dir) {
        final SysUser user = AuthApplication.getLocalUser(session());
        DataFile dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        if (null == dataFile) {

            return ok(dd_editor.render(dataFile, null, false,dir,headerSheetColumn,commentSheetColumn));
        }
        
        List<DataFile> files = null;
        String path = ConfigProp.getPathDownload();

        files = DataFile.find(user.getEmail());

        String dd_filename=dataFile.getFileName();
        dd_filename = dd_filename.substring(1); // Only files with the prefix SDD are allowed so were always going to have a second character
        DataFile dd_dataFile = new DataFile(""); // This is being used in place of null but we might want to come up with a better way

        for(DataFile df : files){
           if(df.getFileName().equals(dd_filename)){
             dd_dataFile = df;
          }
       }

         System.out.println(headerSheetColumn);
          System.out.println(commentSheetColumn);
    	// System.out.println("files = " + files);
    	// System.out.println("dd_dataFile = " + dd_dataFile.getFileName());

        
        return ok(dd_editor.render(dataFile, dd_dataFile, bSavable,dir,headerSheetColumn,commentSheetColumn));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String fileId, boolean bSavable,String dir) {
        return index(fileId, bSavable,dir);
    }

    public Result fromSharedLink(String sharedId,String dir) {
        DataFile dataFile = DataFile.findBySharedId(sharedId);
        if (null == dataFile) {
            return badRequest("Invalid link!");
        }

        return ok(dd_editor.render(dataFile,null, false,dir,headerSheetColumn,commentSheetColumn));
    }

    public Result postFromSharedLink(String sharedId,String dir) {
        return fromSharedLink(sharedId,dir);
    }
    public Result getHeaderLoc(String header_loc){
        headerSheetColumn=header_loc;
        return ok(Json.toJson(headerSheetColumn)); 
    }   
    public Result getDescLoc(String desc_loc){
        commentSheetColumn=desc_loc;
        return ok(Json.toJson(commentSheetColumn)); 
    } 

}
