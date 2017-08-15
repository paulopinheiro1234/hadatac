package org.hadatac.console.controllers.fileviewer;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVParser;
import java.nio.charset.StandardCharsets;

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
	private static int defPreviewRows = 10;
	private static String path_proc = ConfigProp.getPropertyValue("autoccsv.config", "path_proc");
	private static String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");

	public static ArrayList<String> getCSVHeaders(DataFile df){
		String filename = path_proc + df.getFileName();
		//System.out.println("filename: " + filename);
		File toPreview = new File(filename);
		ArrayList<String> headerList = null;
		try{
			CSVParser parser = CSVParser.parse(toPreview, StandardCharsets.UTF_8, CSVFormat.RFC4180.withHeader());
			Map<String,Integer> headerMap = parser.getHeaderMap();
			int mapSize = headerMap.size();
			Iterator it = headerMap.entrySet().iterator();
			headerList = new ArrayList<String>(mapSize);
			while(it.hasNext()){
				Map.Entry pair = (Map.Entry)it.next();
				headerList.add((int)pair.getValue(), (String)pair.getKey());
				it.remove();
			}
			parser.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return headerList;
	}// /getCSVHeaders

	public static ArrayList<ArrayList<String>> getCSVPreview(DataFile df, int numRows){
		/* TODO: implement this using the FileFactory
		Arguments arguments = new Arguments();
		arguments.setInputPath(UPLOAD_NAME);
		arguments.setInputType("CSV");
		arguments.setOutputPath("upload/");
		arguments.setVerbose(true);
		arguments.setPv(false);)
		files = new FileFactory(arguments);
		*/

		ArrayList<ArrayList<String>> previewList = null;

		String filename = path_proc + df.getFileName();
		File toPreview = new File(filename);
		//System.out.println("filename: " + filename);
		try{
			CSVParser parser = CSVParser.parse(toPreview, StandardCharsets.UTF_8, CSVFormat.RFC4180.withHeader());
			int rowCount = 0;
			int recordSize;
			Iterator it = parser.iterator();
			CSVRecord currentRow;
			previewList = new ArrayList<ArrayList<String>>();
			while(rowCount < numRows && it.hasNext()){
				rowCount++;
				ArrayList<String> row = new ArrayList<String>();
				currentRow = (CSVRecord)it.next();
				int numCols = currentRow.size();
				for(int i=0; i<numCols; i++){
					row.add(currentRow.get(i));
				}
				previewList.add(row);
			}
			parser.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return previewList;
	}// /getCSVPreview

	public static Result getCSVPreview(String ownerEmail, String fileName, int numRows){
		DataFile df = DataFile.findByName(ownerEmail, fileName);
		//System.out.println("Headers: \n" + getCSVHeaders(df));
		//System.out.println("Sample: \n" + getCSVPreview(df, numRows));
		return ok(csv_preview.render(ownerEmail, fileName, getCSVHeaders(df), getCSVPreview(df, numRows)));
	}// /getCSVPreview

	public static ArrayList<String> getColumn(String ownerEmail, String fileName, int selectedCol){
		//DataFile df = DataFile.findByName(ownerEmail, fileName);
		String filename = path_proc + fileName;
		File toPreview = new File(filename);
		ArrayList<String> theColumn = new ArrayList<String>();
		try{
			CSVParser parser = CSVParser.parse(toPreview, StandardCharsets.UTF_8, CSVFormat.RFC4180.withHeader());
			Iterator it = parser.iterator();
			CSVRecord currentRow;
			String temp = "";
			while(it.hasNext()){
				currentRow = (CSVRecord)it.next();
				temp = currentRow.get(selectedCol);
				theColumn.add(temp);
			}
			int count = theColumn.size();
			System.out.println("Added " + count + " rows to column list");
			parser.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println(theColumn);
		return theColumn;
	}// /getColumn

	/*public static Result selectColumn(String ownerEmail, String fileName, int selectedCol){
		DataFile df = DataFile.findByName(ownerEmail, fileName);
		if (selectedCol < 0){
			return badRequest(csv_preview.render(ownerEmail, fileName, getCSVHeaders(df), getCSVPreview(df, 10)));
		}
		else{
			ArrayList<String> theColumn = getColumn(ownerEmail, fileName, selectedCol);
			return redirect(routes.CSVPreview.getCSVPreview(ownerEmail, fileName, 10));
		}
	}// /selectColumn
	*/
}

