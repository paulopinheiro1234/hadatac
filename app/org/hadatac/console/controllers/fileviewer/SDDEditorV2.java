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
import org.hadatac.utils.NameSpaces;



public class SDDEditorV2 extends Controller {
    NameSpaces ns = NameSpaces.getInstance();
        List<String> loadedList=ns.listLoadedOntologies();
        List<String> currentCart=new ArrayList<String>();
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    
   
    public Result index(String fileId, boolean bSavable) {
         
        final SysUser user = AuthApplication.getLocalUser(session());
        DataFile dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        if (null == dataFile) {
            
            return ok(sdd_editor_v2.render(dataFile, null, false,loadedList,this));
        }


        List<DataFile> files = null;
        String path = ConfigProp.getPathDownload();

        files = DataFile.find(user.getEmail());

        String dd_filename=dataFile.getFileName();
        dd_filename="DD-"+ dd_filename.split("-")[1];
        DataFile dd_dataFile = null;
 		for(DataFile df : files){
 			if(df.getFileName().equals(dd_filename)){
 				dd_dataFile = df;
 			}
 		}
       
       
    	// System.out.println("files = " + files);
    	// System.out.println("dd_dataFile = " + dd_dataFile.getFileName());


        return ok(sdd_editor_v2.render(dataFile, dd_dataFile, bSavable,loadedList,this));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String fileId, boolean bSavable) {
        return index(fileId, bSavable);
    }

    public Result fromSharedLink(String sharedId) {
        DataFile dataFile = DataFile.findBySharedId(sharedId);
        if (null == dataFile) {
            return badRequest("Invalid link!");
        }

        return ok(sdd_editor_v2.render(dataFile,null, false,loadedList,this));
    }

    public Result postFromSharedLink(String sharedId) {
        return fromSharedLink(sharedId);
    }

    // public Result testPrint(String s){
    //     System.out.println(s);
    //     return new Result(200);
    // }

    public List<String> getCart(){
        return currentCart;
    }

    public Result addToCart(String ontology){
        currentCart.add(ontology);
        for(int i=0;i<currentCart.size();i++){
            System.out.println(currentCart.get(i));
        }
        return new Result(200);
    }

    public Result removeFromCart(String ontology){
        currentCart.remove(ontology);
        return new Result(200);
    }
    public Result sizeOfCart(int cartamount){
        cart_amount= currentCart.size();
        
        return new Result(200);
    }
}
