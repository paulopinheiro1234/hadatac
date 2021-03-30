package org.hadatac.console.controllers.fileviewer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVParser;
import java.nio.charset.StandardCharsets;

import org.hadatac.console.controllers.Application;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.FileManager;
import org.hadatac.console.views.html.fileviewer.*;
import org.hadatac.entity.pojo.DataFile;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;

public class CSVPreview extends Controller{

	private static String pathProc = ConfigProp.getPathProc();
	private static String pathUnproc = ConfigProp.getPathUnproc();
	private static String pathWorking = ConfigProp.getPathWorking();

	@Inject
	Application application;

	public static ArrayList<String> getCSVHeaders(String folder, String fileId) {
	    ArrayList<String> headerList = null;
	    
	    DataFile dataFile = DataFile.findById(fileId);
	    if (null == dataFile) {
	        return headerList;
	    }
	    
		File toPreview = new File(dataFile.getAbsolutePath());
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
	}

	public static ArrayList<ArrayList<String>> getCSVPreview(String folder, String dir, String fileId, int numRows){
		ArrayList<ArrayList<String>> previewList = null;
		
		DataFile dataFile = DataFile.findById(fileId);
		if (null == dataFile) {
		    return previewList;
		}
		
		File toPreview = new File(dataFile.getAbsolutePath());

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
	}

	public Result getCSVPreview(String folder, String dir, String fileId,
								String da_uri, String oc_uri, int numRows, int page, Http.Request request) {
		if (da_uri != null && !da_uri.equals("")) {
			return ok(csv_preview.render("selectCol", dir, fileId, da_uri, oc_uri, getCSVHeaders(folder, fileId), getCSVPreview(folder, dir, fileId, numRows), page, application.getUserEmail(request)));
		}
		
		return ok(csv_preview.render("preview", dir, fileId, da_uri, oc_uri, getCSVHeaders(folder, fileId), getCSVPreview(folder, dir, fileId, numRows), page, application.getUserEmail(request)));
	}

	public static ArrayList<String> getColumn(String ownerEmail, String fileId, int selectedCol) {
	    ArrayList<String> theColumn = new ArrayList<String>();
	    
	    DataFile dataFile = DataFile.findById(fileId);
        if (null == dataFile) {
            return theColumn;
        }
	    
		File toPreview = new File(dataFile.getAbsolutePath());
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
		
		return theColumn;
	}
}

