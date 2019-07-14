package org.hadatac.console.controllers.metadata;

import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadata.*;
import org.hadatac.console.controllers.schema.EditingOptions;
import org.hadatac.utils.NameSpaces;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class BrowseOntology extends Controller {

    public Result index(String oper) {
	return ok(browseOntology.render(oper, EditingOptions.getEntities(), EditingOptions.getAttributes(), EditingOptions.getUnits()));
    }

    public Result postIndex(String oper) {
	return index(oper);
    }

    public Result graphIndex(String oper, String className) {
    //System.out.println(className);
    //System.out.println(EditingOptions.getHierarchy(className));
    return ok(browseKnowledgeGraph.render(oper, className, EditingOptions.getHierarchy(className), getLoadedList()));
    }


    private List<String> getCacheList(String oper) {

        List<String> cacheList = new ArrayList<String>();
        File folder = new File(NameSpaces.CACHE_PATH);

        // if the directory does not exist, create it
        if (!folder.exists()) {
            System.out.println("creating directory: " + NameSpaces.CACHE_PATH);
            try{
                folder.mkdir();
            } 
            catch(SecurityException se){
                System.out.println("Failed to create directory.");
            }
            System.out.println("DIR created");
        }

        String name = "";
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                name = fileEntry.getName();
                if (name.startsWith(NameSpaces.CACHE_PREFIX)) {
                    name = name.substring(NameSpaces.CACHE_PREFIX.length());
                    cacheList.add(name);
                }
            }
        }
        //String jsonStr = NameSpaces.jsonLoadedOntologies();
        //System.out.println(jsonStr);
        return cacheList;
    }

    private List<String> getLoadedList() {
        NameSpaces ns = NameSpaces.getInstance();
        return ns.listLoadedOntologies();
    }

}
