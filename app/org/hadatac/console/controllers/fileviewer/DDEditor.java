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
   
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))    
    public Result index(String fileId, boolean bSavable,String dir) {
        final SysUser user = AuthApplication.getLocalUser(session());
        DataFile dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        if (null == dataFile) {

            return ok(dd_editor.render(dataFile,false,dir));
        }
        
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

        

        
        return ok(dd_editor.render(dataFile, bSavable,dir));
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

        return ok(dd_editor.render(dataFile, false,dir));
    }

    public Result postFromSharedLink(String sharedId,String dir) {
        return fromSharedLink(sharedId,dir);
    }
   

}
