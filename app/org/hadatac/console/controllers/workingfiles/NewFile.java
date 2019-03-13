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
import play.Play;
import play.data.*;

import org.hadatac.console.models.FileType;
import org.hadatac.console.models.NewFileForm;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.workingfiles.routes;
import org.hadatac.console.views.html.workingfiles.*;
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
 
        return ok(newFile.render(FileType.FILETYPES, dir));

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
        String newName = data.getNewName();

        FileType ft = FileType.find(newType);
        
        System.out.println("Creating new metadata file. type: [" + newType + "]  name: [" + newName + "]");
        System.out.println("Creating new metadata file. path: [" + ft.getPath() + "]  suffix: [" + ft.getSuffix() + "]");
        
        Environment env = Environment.simple();
        //ReadResource resource = new ReadResource(controller.);
        
        System.out.println( env.getFile("public/example/data/templates/STD.csv").getAbsolutePath());
        try  {
            final File templateFile = env.getFile(ft.getPath());
            final InputStream inputStream = new DataInputStream(new FileInputStream(templateFile));
            File newFile = new File(ConfigProp.getPathWorking() + newType + "-" + newName + ft.getSuffix());
            OutputStream outStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outStream);  
        } catch(IOException e) {

        }

        /*
        try {
			String ftPath = "";
        	URL url = new URL(ftPath);
        	String destPath = ConfigProp.getPathWorking() + newType + "-" + newName + ft.getSuffix();
        	System.out.println("Creating new metadata file. Destination path: [" + destPath + "]");
        	ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        	FileOutputStream fileOutputStream = new FileOutputStream(destPath);
        	FileChannel fileChannel = fileOutputStream.getChannel();
        } catch (MalformedURLException e1) {
        	e1.printStackTrace();
        } catch (IOException e2) {
        	e2.printStackTrace();
        }
        */
                
        /*

        @controllers.routes.Assets.versioned("example/data/templates/STD.csv")
        
        // when a new study is created in the scope of a datafile, the new study needs to be associated to the datafile's DA 
        if (filename != null && !filename.equals("") && da_uri != null && !da_uri.equals("")) {
            ObjectAccessSpec da = ObjectAccessSpec.findByUri(URIUtils.replacePrefixEx(da_uri));
            if (da != null) {
                da.setStudyUri(std.getUri());
                
                System.out.println("Inserting new Study from file. Found DA");
                if (!ConfigProp.getLabKeyLoginRequired() || da.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword")) > 0) {
                    da.save();
                } else {
                    System.out.println("[WARNING] Could not update DA from associated DataFile when creating a new study");
                }
            } else {
                System.out.println("[WARNING] DA from associated DataFile not found when creating a new study");
            }
        }
        */
        
        return redirect(routes.WorkingFiles.index(dir));
    }
}
