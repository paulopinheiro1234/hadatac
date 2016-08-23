package org.hadatac.console.controllers.triplestore;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import java.io.FileOutputStream;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.metadata.loader.CSVExporterContext;
import org.hadatac.metadata.loader.CSVExporting;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ExportKB extends Controller {
	
	private static final String DOWNLOAD_CSV_FILE_NAME = "public/csv/triple.csv";

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result exportKB(String oper) {
		System.out.println("exportKB CALLED!");
		return ok(exportKB.render(oper, ""));
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result postExportKB(String oper) {
		System.out.println("postExportKB CALLED!");
		return ok(exportKB.render(oper, ""));
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public static Result exportFile(String oper) {
		System.out.println("exportFile CALLED!");
		NameSpaces.getInstance();
		CSVExporterContext metadata = new CSVExporterContext(
				"user",
				"password", 
				Play.application().configuration().getString("hadatac.solr.triplestore"), 
				false);
		String message = CSVExporting.generateCSV(Feedback.WEB, oper, metadata, DOWNLOAD_CSV_FILE_NAME);
		return ok(exportKB.render("init", ""));
	} 
}