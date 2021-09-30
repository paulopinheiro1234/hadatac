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
import java.text.SimpleDateFormat;
import java.util.Date;
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

import org.hadatac.console.models.FileTemplate;
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

import com.typesafe.config.ConfigFactory;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class NewFile extends Controller {

    @Inject
    private FormFactory formFactory;
    @Inject
    Application application;

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String dir,Http.Request request) {
        return ok(newFile.render(FileType.FILETYPES, FileTemplate.TEMPLATETYPES, dir, application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String dir,Http.Request request) {
        return index(dir,request);
    }

    @Secure (authorizers = Constants.DATA_OWNER_ROLE)
    public Result processForm(String dir, Http.Request request) {
        Form<NewFileForm> form = formFactory.form(NewFileForm.class).bindFromRequest(request);
        NewFileForm data = form.get();

        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        // store new values
        String newType = data.getNewType();
        String newTemplate = data.getNewTemplate();
        String newName = data.getNewName();

        FileType fileType = FileType.find(newType);
        FileTemplate fileTemplate = FileTemplate.find(newType, newTemplate);

        try  {
            String fileName = newType + "-" + newName + fileType.getSuffix();

            DataFile dataFile = new DataFile(fileName);
            dataFile.setDir(dir);
            dataFile.setOwnerEmail(AuthApplication.getLocalUser(application.getUserEmail(request)).getEmail());
            dataFile.setStatus(DataFile.WORKING);
            dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

            String url = ConfigFactory.load().getString("hadatac.console.host") + fileTemplate.getPath();

            File newFile = new File(dataFile.getAbsolutePath());
            FileUtils.copyURLToFile(new URL(url), newFile);

            dataFile.save();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return redirect(routes.WorkingFiles.index(dir, ".", false));
    }
}