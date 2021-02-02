package org.hadatac.console.controllers.triplestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.hadatac.Constants;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.entity.pojo.SPARQLUtilsFacetSearch;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.SpreadsheetProcessing;
import org.hadatac.metadata.loader.TripleProcessing;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.data.FormFactory;
import play.libs.Files;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;

public class LoadKB extends Controller {

	private static final String UPLOAD_NAME = ConfigProp.getTmp() + "uploads/hasneto-spreadsheet.xls";
	private static final String UPLOAD_TURTLE_NAME = ConfigProp.getTmp() + "uploads/turtle.ttl";

	@Inject
	private FormFactory formFactory;

	@Restrict(@Group(Constants.DATA_MANAGER_ROLE))
	public Result loadKB(String oper) {
		return ok(loadKB.render(oper, ""));
	}

	@Restrict(@Group(Constants.DATA_MANAGER_ROLE))
	public Result createInMemoryDataset(String oper) {
		Model model = SPARQLUtilsFacetSearch.createInMemoryModel();
		String msg = "in-memory model created, with # of triples = " + model.size();
		System.out.println(msg);
		return ok(loadInMemory.render(msg));
	}

	@Restrict(@Group(Constants.DATA_MANAGER_ROLE))
	public Result postLoadKB(String oper) {
		return ok(loadKB.render(oper, ""));
	}

	public static String playLoadKB(String oper) {
		NameSpaces.getInstance();
		MetadataContext metadata = new
				MetadataContext("user",
				"password",
				ConfigFactory.load().getString("hadatac.solr.triplestore"),
				false);
		String message = "";
		if(oper.equals("turtle")){
			message = TripleProcessing.processTTL(Feedback.WEB, oper, metadata, UPLOAD_TURTLE_NAME);
		}
		else{
			message = SpreadsheetProcessing.generateTTL(Feedback.WEB, oper, metadata, UPLOAD_NAME);
		}
		return message;
	}


	@Restrict(@Group(Constants.DATA_MANAGER_ROLE))
	@BodyParser.Of(value = BodyParser.MultipartFormData.class)
	public Result uploadFile(String oper, Http.Request request) {
		System.out.println("uploadFile CALLED!");
		FilePart uploadedfile = request.body().asMultipartFormData().getFile("pic");
		if (uploadedfile != null) {
			Files.TemporaryFile temporaryFile = (Files.TemporaryFile) uploadedfile.getRef();
			File file = temporaryFile.path().toFile();
			File newFile = new File(UPLOAD_NAME);
			InputStream isFile;
			try {
				isFile = new FileInputStream(file);
				byte[] byteFile;
				try {
					byteFile = IOUtils.toByteArray(isFile);
					try {
						FileUtils.writeByteArrayToFile(newFile, byteFile);
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						isFile.close();
					} catch (Exception e) {
						return ok (loadKB.render("fail", "Could not save uploaded file."));
					}
				} catch (Exception e) {
					return ok (loadKB.render("fail", "Could not process uploaded file."));
				}
			} catch (FileNotFoundException e1) {
				return ok (loadKB.render("fail", "Could not find uploaded file"));
			}
			return ok(loadKB.render(oper, "File uploaded successfully."));
		} else {
			return ok (loadKB.render("fail", "Error uploading file. Please try again."));
		}
	}

	@Restrict(@Group(Constants.DATA_MANAGER_ROLE))
	@BodyParser.Of(value = BodyParser.MultipartFormData.class)
	public Result uploadTurtleFile(String oper, Http.Request request) {
		System.out.println("uploadTurtleFile CALLED!");
		FilePart uploadedfile = request.body().asMultipartFormData().getFile("pic");
		if (uploadedfile != null) {
			Files.TemporaryFile temporaryFile = (Files.TemporaryFile) uploadedfile.getRef();
			File file = temporaryFile.path().toFile();
			File newFile = new File(UPLOAD_TURTLE_NAME);
			InputStream isFile;
			try {
				isFile = new FileInputStream(file);
				byte[] byteFile;
				try {
					byteFile = IOUtils.toByteArray(isFile);
					try {
						FileUtils.writeByteArrayToFile(newFile, byteFile);
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						isFile.close();
					} catch (Exception e) {
						return ok (loadKB.render("fail", "Could not save uploaded file."));
					}
				} catch (Exception e) {
					return ok (loadKB.render("fail", "Could not process uploaded file."));
				}
			} catch (FileNotFoundException e1) {
				return ok (loadKB.render("fail", "Could not find uploaded file"));
			}
			return ok(loadKB.render("turtle", "File uploaded successfully."));
		} else {
			return ok (loadKB.render("fail", "Error uploading file. Please try again."));
		}
	}
}