package org.hadatac.console.controllers.dataacquisitionsearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hadatac.console.views.html.dataacquisitionsearch.*;
import org.hadatac.data.loader.util.FileFactory;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.utils.ConfigProp;
import org.labkey.remoteapi.CommandException;

public class ImportSDD extends Controller {

	static FileFactory files;

	public static final String UPLOAD_NAME = "uploads/sdd.csv";

	public static Result importSDD(String oper) {
		return ok(importSDD.render(oper, ""));
	}

	//    public static Result postLoadSDD(String oper) {
	//    	return ok(importSDD.render(oper, ""));
	//    }

	//    public Map<String, Object> createRow() {
	//    	Map<String, Object> row = new HashMap<String, Object>();
	//    	row.put("hasURI", getUri());
	//    	row.put("a", getType());
	//    	row.put("rdfs:label", getTitle());
	//    	row.put("skos:definition", getAims());
	//    	row.put("rdfs:comment", getSignificance());
	//    	row.put("hasco:hasAgent", getAgent());
	//    	row.put("hasco:hasInstitution", getInstitution());
	//    	counter++;
	//    	
	//    	return row;
	//    }
	//    
	//    public List< Map<String, Object> > createRows() {
	//    	for (CSVRecord record : records) {
	//    		rec = record;
	//    		rows.add(createRow());
	//    	}
	//
	//    	return rows;
	//    }

	public static void playLoadSDD() throws IOException, CommandException {	

		BufferedReader reader = new BufferedReader(new FileReader(UPLOAD_NAME));
		List<List<String>> lines = new ArrayList<>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			List<String> elephantList = Arrays.asList(line.split(","));
			lines.add(elephantList);
		}

		Map<String, List<String>> sddMap = new HashMap<String, List<String>>();
		for (int i = 0; i < lines.size(); i++){
			sddMap.put("i", lines.get(i));
		}
		//		System.out.println(lines.get(0).get(0));

		Map<String, Object> row = new HashMap<String, Object>();
		List< Map<String, Object> > rows = null;

		String site = ConfigProp.getPropertyValue("labkey.config", "site");
		String path = "/SIDPIDTEST";
		Credential cred = Credential.find();

		LabkeyDataHandler labkeyDataHandler = new LabkeyDataHandler(
				site, cred.getUserName(), cred.getPassword(), path);

		int nRows = labkeyDataHandler.updateRows("DASchemaAttribute", rows);

		reader.close();
	}

	@BodyParser.Of(value = BodyParser.MultipartFormData.class, maxLength = 500 * 1024 * 1024)
	public static Result uploadFile() {
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart uploadedfile = body.getFile("sddsheet");
		if (uploadedfile != null) {
			File file = uploadedfile.getFile();
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
						return ok (importSDD.render("fail", "Could not save uploaded file."));
					}
				} catch (Exception e) {
					return ok (importSDD.render("fail", "Could not process uploaded file."));
				}
			} catch (FileNotFoundException e1) {
				return ok (importSDD.render("fail", "Could not find uploaded file"));
			}
			return ok(importSDD.render("loaded", "File uploaded successfully."));
		} else {
			return ok (importSDD.render("fail", "Error uploading file. Please try again."));
		}
	}
}
