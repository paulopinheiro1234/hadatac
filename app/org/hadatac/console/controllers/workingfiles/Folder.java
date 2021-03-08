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
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Inject;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.Environment;
import play.data.*;

import org.hadatac.console.models.FileType;
import org.hadatac.console.models.NewFileForm;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
//import org.hadatac.console.controllers.AuthApplication;
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
    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String dir, Http.Request request) {
        return index(dir,request);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String dir,Http.Request request) {
    	return ok(newFolder.render(dir,application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result processForm(String dir, Http.Request request) {
        Form<NewFileForm> form = formFactory.form(NewFileForm.class).bindFromRequest(request);
        NewFileForm data = form.get();
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        // store new values
        String newName = data.getNewName();

        File folder = new File(Paths.get(ConfigProp.getPathWorking(), dir, newName).toString());
        if (!folder.exists()) {
            folder.mkdirs();
        }

        return redirect(routes.WorkingFiles.index(dir, ".",false));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result delete(String dir, String path,Http.Request request) {
    	if (dir == null || path == null || path.equals("/")) {
    		return redirect(routes.WorkingFiles.index(dir, ".",false));
    	}

        String fullPath = Paths.get(ConfigProp.getPathWorking(), dir, path).toString();
    	File folder = new File(fullPath);

        File[] listOfFiles = folder.listFiles();
        Boolean folderEmpty = listOfFiles == null || listOfFiles.length == 0;

        return ok(deleteFolder.render(dir, path, folderEmpty, application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postDelete(String dir, String path, Http.Request request) {
        return delete(dir, path, request);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result deleteForm(String dir, String path) {
    	if (dir == null || path == null || path.equals("/")) {
    		return redirect(routes.WorkingFiles.index(dir, ".",false));
    	}

    	String fullPath = Paths.get(ConfigProp.getPathWorking(), dir, path).toString();
        File deleteFolder = new File(fullPath);

        try {
        	FileUtils.deleteDirectory(deleteFolder);
        } catch (Exception e) {
        	e.printStackTrace();
        }

        return redirect(routes.WorkingFiles.index(dir, ".",false));
    }
}
