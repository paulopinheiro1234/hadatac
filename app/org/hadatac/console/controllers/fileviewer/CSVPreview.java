package org.hadatac.console.controllers.fileviewer;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVParser;
import java.nio.charset.Charset;

import org.hadatac.entity.pojo.DataFile;
import org.hadatac.utils.ConfigProp;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.console.views.html.fileviewer.*;
import org.hadatac.console.views.html.*;
//import org.hadatac.data.loader.util.FileFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;


import play.mvc.Controller;
import play.mvc.Result;

/*
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.annotator.*;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.http.GetSparqlQuery;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.entity.pojo;

import org.hadatac.console.views.html.error_page;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.DataAcquisition;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
*/

public class CSVPreview extends Controller{
//	private FileFactory files;
	private int defPreviewRows = 10;
	private String path_proc = ConfigProp.getPropertyValue("autoccsv.config", "path_proc");
	private String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");

	public ArrayList<String> getCSVHeaders(DataFile df){
		String filename = df.getFileName();
		File toPreview = new File(filename);
		CSVParser parser = CSVParser.parse(toPreview, CSVFormat.RFC4180);
		HashMap<String,Integer> headerMap = parser.getHeaderMap();
		int mapSize = headerMap.size();
		Iterator it = headerMap.entrySet().iterator();
		ArrayList<String> headerList = new ArrayList<String>(mapSize);
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
			headerList.add(pair.value, pair.key);
			it.remove();
		}
		try{
			parser.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return headerList;
	}// /getCSVHeaders

	public ArrayList<String> getCSVPreview(DataFile df, int numRows){
		/* TODO: implement this using the FileFactory
		Arguments arguments = new Arguments();
		arguments.setInputPath(UPLOAD_NAME);
		arguments.setInputType("CSV");
		arguments.setOutputPath("upload/");
		arguments.setVerbose(true);
		arguments.setPv(false);)
		files = new FileFactory(arguments);
		*/

		ArrayList<String> previewString = new ArrayList<String>();

		String filename = df.getFileName();
		File toPreview = new File(filename);
		CSVParser parser = CSVParser.parse(toPreview, CSVFormat.RFC4180);
		int rowCount = 0;
		int recordSize;
		Iterator it = parser.iterator();
		CSVRecord currentRow;
		while(rowCount < numRows){
			rowCount++;
			currentRow = it.next();
			int numCols = currentRow.size();
			String temp = "";
			for(i=0;i<numCols;i++){
				temp += currentRow.get(i) + "|";
			}
			previewString.add(temp);
		}
		try{
			parser.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return previewString;
	}// /getCSVPreview

	public static Result getCSVPreview(String ownerEmail, String fileName, int numRows){
		DataFile df = findByName(ownerEmail, fileName);
		return ok(CSVPreview.render(getCSVHeaders(df), getCSVPreview(df, numRows)));
	}// /getCSVPreview
}

