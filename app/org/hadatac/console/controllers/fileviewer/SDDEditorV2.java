package org.hadatac.console.controllers.fileviewer;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

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
import org.hadatac.utils.NameSpace;
import play.libs.Json;
import org.hadatac.console.controllers.workingfiles.FileHeadersIntoSDD;
import play.core.j.JavaResultExtractor;

import org.hadatac.entity.pojo.Ontology;


public class SDDEditorV2 extends Controller {
    NameSpaces ns = NameSpaces.getInstance();
        String bioportalKey="";
        List<String> loadedList = ns.listLoadedOntologies();
        List<String> currentCart = new ArrayList<String>();
        ArrayList<ArrayList<String>> storeEdits=new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> oldEdits=new ArrayList<ArrayList<String>>();
        DataFile ddDF;
        String headerSheetColumn;
        String commentSheetColumn;

       // ArrayList<ArrayList<String>> storeRows=new ArrayList<ArrayList<String>>();
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String fileId, boolean bSavable, int indicator) {
        Collections.sort(loadedList);
        final SysUser user = AuthApplication.getLocalUser(session());
        DataFile dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
        if (null == dataFile && indicator==1) {

            return ok(sdd_editor_v2.render(dataFile, null, false,loadedList,this));
        }
        DataFile finalDF=new DataFile("");;
        if(indicator==1 && dataFile!=null){
            headerSheetColumn=FileHeadersIntoSDD.headerSheetColumn;
            commentSheetColumn=FileHeadersIntoSDD.commentSheetColumn;

            ddDF=FileHeadersIntoSDD.dd_df;
            finalDF=ddDF;

        }
        else if(indicator==0){
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
            finalDF=dd_dataFile;
        }
        return ok(sdd_editor_v2.render(dataFile, finalDF, bSavable,loadedList,this));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String fileId, boolean bSavable,int indicator) {
        return index(fileId, bSavable,indicator);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result fromViewableLink(String viewableId) {
        Collections.sort(loadedList);
        DataFile dataFile = DataFile.findByViewableId(viewableId);
        if (null == dataFile) {
            return badRequest("Invalid link!");
        }

        return ok(sdd_editor_v2.render(dataFile,null, false, loadedList, this));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postFromViewableLink(String viewableId) {
        return fromViewableLink(viewableId);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result fromEditableLink(String editableId) {
        Collections.sort(loadedList);
        DataFile dataFile = DataFile.findByEditableId(editableId);
        if (null == dataFile) {
            return badRequest("Invalid link!");
        }

        return ok(sdd_editor_v2.render(dataFile,null, false, loadedList, this));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postFromEditableLink(String editableId) {
        return fromEditableLink(editableId);
    }

    public Result getBioportalKey() {
        bioportalKey=ConfigProp.getBioportalApiKey();
        return ok(Json.toJson(bioportalKey));
    }

    public Result getOntologies() {
        return ok(Json.toJson(Ontology.find()));
    }

    public Result getCart(){
        return ok(Json.toJson(currentCart));
    }

    public Result addToCart(String ontology){
        if(currentCart.contains(ontology)){
            System.out.println("This item already exists");
        }
        else{
            currentCart.add(ontology);
        }

        return new Result(200);
    }

    public Result removeFromCart(String item){

       currentCart.remove(item);

        return ok(Json.toJson(currentCart));
    }
    public Result sizeOfCart(int cartamount){
        cartamount= currentCart.size();
        System.out.println(cartamount);
        return ok(Json.toJson(cartamount));

    }

    public Result addToEdits(String row, String col,String editValue){
        ArrayList<String> temp = new ArrayList<String>();
        temp.add(row);
        temp.add(col);
        temp.add(editValue);
        storeEdits.add(temp);
        //return new Result(200);
        return ok(Json.toJson(storeEdits));
    }

    public Result removingRow(String removedValue){
        for( int i=0;i<storeEdits.size();i++){
            if(storeEdits.get(i).get(2)==removedValue){
                storeEdits.remove(storeEdits.get(i));
            }
        }
        return ok(Json.toJson(storeEdits));
    }

    public Result getEdit(){
        ArrayList<String> temp=storeEdits.get(storeEdits.size()-1);

        //String lastKnown=;
        System.out.println(temp);
        oldEdits.add(temp);
        storeEdits.remove(storeEdits.size()-1);

        ArrayList<String> lastEdit=storeEdits.get(storeEdits.size()-1);
        return ok(Json.toJson(lastEdit));
    }

    public Result getOldEdits(){
         ArrayList<String> recentoldEdit=oldEdits.get(0);
         oldEdits.remove(0);
        return ok(Json.toJson(recentoldEdit));
    }
    public Result getHeaderLoc(){

        return ok(Json.toJson(headerSheetColumn));
    }
    public Result getCommentLoc(){

        return ok(Json.toJson(commentSheetColumn));
    }
}
