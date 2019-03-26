package org.hadatac.console.controllers.workingfiles;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import javax.inject.Inject;

import play.mvc.Controller;
import play.mvc.Result;
import play.Environment;
import play.data.*;

import org.hadatac.console.models.FileType;
import org.hadatac.console.models.NewFileForm;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.workingfiles.routes;
import org.hadatac.console.views.html.workingfiles.*;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import controllers.Assets;

public class Folder extends Controller {

    @Inject
    private FormFactory formFactory;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir) {
        return index(dir);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir) {
    	
    	return ok(newFolder.render(dir));

    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processForm(String dir) {
    	
        Form<NewFileForm> form = formFactory.form(NewFileForm.class).bindFromRequest();
        NewFileForm data = form.get();

        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        // store new values
        String newName = data.getNewName();
        String fullPath = ConfigProp.getPathWorking() + dir + newName;
        fullPath = fullPath.replace("//", "/");

        //System.out.println("Creating new folder.  dir: [" + dir + "]");
        //System.out.println("Creating new folder.  newName: [" + newName + "]");
        //System.out.println("Creating new folder.  newName: [" + ConfigProp.getPathWorking() + "]");
        //System.out.println("Creating new folder.  name: [" + ConfigProp.getPathWorking() + dir + newName + "]");
        //System.out.println("Creating new folder.  name: [" + fullPath + "]");

        new File(fullPath).mkdirs();        

        return redirect(routes.WorkingFiles.index(dir, "."));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result delete(String dir, String path) {
    	if (dir == null || path == null || path.equals("/")) {
    		return redirect(routes.WorkingFiles.index(dir, "."));
    	}
    	
        String fullPath = ConfigProp.getPathWorking() + dir + path;
    	fullPath = fullPath.replace("//","/");
    	//System.out.println("Deleting folder.  name: [" + fullPath + "]");

    	File folder = new File(fullPath);

        File[] listOfFiles = folder.listFiles();
        Boolean folderEmpty = listOfFiles == null || listOfFiles.length <= 0;

        return ok(deleteFolder.render(dir, path, folderEmpty));

    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postDelete(String dir, String path) {
        return delete(dir, path);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result deleteForm(String dir, String path) {
    	if (dir == null || path == null || path.equals("/")) {
    		return redirect(routes.WorkingFiles.index(dir, "."));
    	}

        String fullPath = ConfigProp.getPathWorking() + dir + path;
    	fullPath = fullPath.replace("//","/");
    	//System.out.println("Deleting folder.  name: [" + fullPath + "]");

        File deleteFolder = new File(fullPath);
        try {
        	FileUtils.deleteDirectory(deleteFolder);
        } catch (Exception e) {
        	e.printStackTrace();
        }

        return redirect(routes.WorkingFiles.index(dir, "."));
    }

}
