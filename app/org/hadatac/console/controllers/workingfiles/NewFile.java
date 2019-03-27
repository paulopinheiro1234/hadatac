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
        String newTemplate = data.getNewTamplate();
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
            final InputStream inputStream = new DataInputStream(new FileInputStream(templateFile));
            String partialPath = dir + newType + "-" + newName + ft.getSuffix();
            //System.out.println("Creating new metadata file. partial path: [" + partialPath + "]");
            String fullPath = ConfigProp.getPathWorking() + partialPath;
            //System.out.println("Creating new metadata file. full path: [" + fullPath + "]");
            partialPath = partialPath.replace("//", "/");           
            fullPath = fullPath.replace("//", "/");           
            File newFile = new File(fullPath);
            //System.out.println("Creating new metadata file. full path: [" + fullPath + "]   dir: [" + dir + "]  email : [" + AuthApplication.getLocalUser(session()).getEmail() + "]");
            OutputStream outStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outStream);  
            DataFile file = new DataFile(partialPath);
            file.setOwnerEmail(AuthApplication.getLocalUser(session()).getEmail());
            file.setStatus(DataFile.WORKING);
            file.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            file.save();
        } catch(IOException e) {

        }
        
        return redirect(routes.WorkingFiles.index(dir, "."));
    }
}
