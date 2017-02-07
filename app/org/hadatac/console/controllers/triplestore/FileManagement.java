package org.hadatac.console.controllers.triplestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionsearch.LoadCCSV;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.controllers.annotator.Downloads;
import org.hadatac.console.http.DataAcquisitionSchemaQueries;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.models.AssignOwnerForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.triplestore.fileManagement;
import org.hadatac.console.views.html.triplestore.assignOwner;
import org.hadatac.data.api.DataFactory;
import org.hadatac.data.model.DatasetParsingResult;
import org.hadatac.entity.pojo.CSVFile;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;

import com.avaje.ebean.enhance.ant.AntEnhanceTask;
import com.typesafe.config.impl.ConfigImpl;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.BodyParser;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.data.*;

public class FileManagement extends Controller {
	
	private static void filesInFolder(String path, List<CSVFile> files) {
		File folder = new File(path);
		if (!folder.exists()){
			folder.mkdirs();
	    }
		
		List<CSVFile> ownedFiles = CSVFile.findAll(State.PROCESSED);
		ownedFiles.addAll(CSVFile.findAll(State.UNPROCESSED));
		
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				Iterator<CSVFile> iterFile = ownedFiles.iterator();
				CSVFile match = null;
				while(iterFile.hasNext()) {
					CSVFile aux = iterFile.next();
					if (aux.getFileName().equals(listOfFiles[i].getName())) {
						match = aux;
					}
				}
				if (match != null) {
					files.add(match);
				} else {
					CSVFile newFile = new CSVFile();
					newFile.setFileName(listOfFiles[i].getName());
					files.add(newFile);
				}
			}
		}
	}
		
	private static void filterNonexistedFiles(String path, List<CSVFile> files) {
		File folder = new File(path);
		if (!folder.exists()){
			folder.mkdirs();
	    }
		
		File[] listOfFiles = folder.listFiles();
		Iterator<CSVFile> iterFile = files.iterator();
		while(iterFile.hasNext()) {
			CSVFile file = iterFile.next();
			boolean isExisted = false;
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					if(file.getFileName().equals(listOfFiles[i].getName())) {
						isExisted = true;
						break;
					}
				}
			}
			if (!isExisted) {
				iterFile.remove();
			}
		}
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result index() {		
		final SysUser user = AuthApplication.getLocalUser(session());
		
		//List<CSVFile> proc_files = CSVFile.find(user.getEmail(), State.PROCESSED);
		List<CSVFile> proc_files = new ArrayList<CSVFile>();
		//List<CSVFile> unproc_files = CSVFile.find(user.getEmail(), State.UNPROCESSED);
		List<CSVFile> unproc_files = new ArrayList<CSVFile>();
		
		String path_proc = ConfigProp.getPropertyValue("autoccsv.config", "path_proc");
		String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");
		
		filesInFolder(path_proc, proc_files);
		filesInFolder(path_unproc, unproc_files);

		return ok(fileManagement.render(unproc_files, proc_files));
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postIndex() {
		return index();
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result assignFileOwner(String selectedFile) {		
    	return ok(assignOwner.render(Form.form(AssignOwnerForm.class), selectedFile, User.find()));
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result postAssignFileOwner(String selectedFile) {
		return assignFileOwner(selectedFile);
	}
	
    /**
     * Handles the form submission.
     */
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public static Result processForm(String selectedFile) {
        Form<AssignOwnerForm> form = Form.form(AssignOwnerForm.class).bindFromRequest();
        AssignOwnerForm data = form.get();
        
        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest(assignOwner.render(form, selectedFile, User.find()));
        } else {
            CSVFile newCSV = new CSVFile();
            newCSV.setFileName(selectedFile);
            newCSV.setOwnerEmail(data.getUser());
    		newCSV.setProcessStatus(false);
    		newCSV.setUploadTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            newCSV.save();
    		return redirect(routes.FileManagement.index());
        }

    }
}

