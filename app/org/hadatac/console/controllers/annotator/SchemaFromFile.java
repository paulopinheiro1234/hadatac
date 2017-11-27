package org.hadatac.console.controllers.annotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLEncoder;
import org.hadatac.utils.ConfigProp;
import org.hadatac.console.views.html.schema.*;

import play.mvc.*;
import play.mvc.Result;

public class SchemaFromFile extends Controller {

	public static Result create(String file_name) {

		String path = "";
		String labels = "";

		try {
			file_name = URLEncoder.encode(file_name, "UTF-8");
		} catch (Exception e) {
			System.out.println("[ERROR] encoding file name");
		}

		System.out.println("file <" + file_name + ">");

		//String[] fields;

		path = ConfigProp.getPathUnproc();

		System.out.println("Path: " + path + "  Name: " + file_name);

		//List<DataAcquisitionSchemaAttribute> attributes = new ArrayList<DataAcquisitionSchemaAttribute>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(path + "/" + file_name));
			StringBuilder builder = new StringBuilder();
			String line = reader.readLine();

			while (line != null) {
				builder.append(line);
				break;
			}
			if(!builder.toString().trim().equals("")) {
				labels = builder.toString();
			}

		} catch (Exception e) {
			System.out.println("Could not process uploaded file.");
		}

		System.out.println("SchemaFromFile: labels = <" + labels + ">");

		return ok(newDASFromFile.render(file_name, labels));
	}

}

