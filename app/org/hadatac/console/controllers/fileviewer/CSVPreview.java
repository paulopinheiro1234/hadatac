package org.hadatac.console.controllers.fileviewer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVParser;
import java.nio.charset.StandardCharsets;

import org.hadatac.utils.ConfigProp;
import org.hadatac.console.views.html.fileviewer.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;

import play.mvc.Controller;
import play.mvc.Result;

public class CSVPreview extends Controller{

	private static String pathProc = ConfigProp.getPathProc();
	private static String pathUnproc = ConfigProp.getPathUnproc();
	private static String pathWorking = ConfigProp.getPathWorking();

	public static ArrayList<String> getCSVHeaders(String folder, String fileName) {
		//System.out.println("filename: " + filename);
		String fullFileName = "";
		if (folder.equals("proc")) {
			fullFileName = pathProc + fileName;
		} else if (folder.equals("unproc")) {
			fullFileName = pathUnproc + fileName;
		} else {
			fullFileName = pathWorking + fileName;
		}
		File toPreview = new File(fullFileName);
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
	}

	public static ArrayList<ArrayList<String>> getCSVPreview(String folder, String dir, String fileName, int numRows){
		ArrayList<ArrayList<String>> previewList = null;
		File toPreview = null;
		if (folder.equals("proc")) {
			toPreview = new File(pathProc + fileName);
		} else if (folder.equals("unproc")) {
			toPreview = new File(pathUnproc + fileName);
		} else {
			toPreview = new File(pathWorking + fileName);
		}
		System.out.println("fileName: " + fileName);
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

	public Result getCSVPreview(String folder, String dir, String fileName, String da_uri, String oc_uri, int numRows, int page){
		if (da_uri != null && !da_uri.equals("")) {
			return ok(csv_preview.render("selectCol", dir, fileName, da_uri, oc_uri, getCSVHeaders(folder, fileName), getCSVPreview(folder, dir, fileName, numRows), page));
		}
		return ok(csv_preview.render("preview", dir, fileName, da_uri, oc_uri, getCSVHeaders(folder, fileName), getCSVPreview(folder, dir, fileName, numRows), page));
	}

	public static ArrayList<String> getColumn(String ownerEmail, String fileName, int selectedCol){
		String filename = pathProc + fileName;
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
	}
}

