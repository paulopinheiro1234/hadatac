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

import play.mvc.Controller;
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

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import controllers.Assets;

public class NewFile extends Controller {

    @Inject
    private FormFactory formFactory;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir) {
        return index(dir);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir) {
 
        return ok(newFile.render(FileType.FILETYPES, FileTemplate.TEMPLATETYPES, dir));

    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processForm(String dir) {
        Form<NewFileForm> form = formFactory.form(NewFileForm.class).bindFromRequest();
        NewFileForm data = form.get();

        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        // store new values
        String newType = data.getNewType();
        String newTemplate = data.getNewTemplate();
        String newName = data.getNewName();

        FileType ft = FileType.find(newType);
        FileTemplate tp = FileTemplate.find(newType, newTemplate);
        		
        //System.out.println("Creating new metadata file. type: [" + newType + "]  template: [" + newTemplate + "]   name: [" + newName + "]");
        //System.out.println("Creating new metadata file. path: [" + tp.getPath() + "]  suffix: [" + ft.getSuffix() + "]");
        
        Environment env = Environment.simple();
        //ReadResource resource = new ReadResource(controller.);
        
        //System.out.println( env.getFile("public/example/data/templates/STD.csv").getAbsolutePath());
        try  {
            final File templateFile = env.getFile(tp.getPath());
            final InputStream inputStream = new FileInputStream(templateFile);
            
            String fileName = newType + "-" + newName + ft.getSuffix();
            
            DataFile dataFile = new DataFile(fileName);
            dataFile.setDir(dir);
            dataFile.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
            dataFile.setStatus(DataFile.WORKING);
            dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            dataFile.save();
            
            File newFile = new File(dataFile.getAbsolutePath());            
            byte[] byteFile = IOUtils.toByteArray(inputStream);
            FileUtils.writeByteArrayToFile(newFile, byteFile);
            inputStream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        return redirect(routes.WorkingFiles.index(dir, "."));
    }
}
