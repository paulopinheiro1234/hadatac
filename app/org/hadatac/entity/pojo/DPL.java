package org.hadatac.entity.pojo;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.data.loader.CSVRecordFile;
import org.hadatac.data.loader.SpreadsheetRecordFile;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.metadata.model.SpreadsheetParsingResult;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

public class DPL {

	private RecordFile dplfile = null;
	private Map<String, String> mapCatalog = new HashMap<String, String>();
	private Map<String, RecordFile> recordFiles = new HashMap<String, RecordFile>();
	
	public DPL(RecordFile file) {
		this.dplfile = file;
		readCatalog(file);
		createRecordFiles(file);
		checkSheets();
		String ttl = createTurtle();
		uploadTurtle(ttl);
	}
	
	public String getNameFromFileName() {
	    return (dplfile.getFile().getName().split("\\.")[0]).replace("_", "-").replace("DPL-", "");
    }
	
	public String getFileName() {
	    return dplfile.getFile().getName();
    }
	
	public Map<String, String> getCatalog() {
		return mapCatalog;
	}
	
	private void readCatalog(RecordFile file) {
	    if (!file.isValid()) {
            return;
        }
	    
	    for (Record record : file.getRecords()) {
	        mapCatalog.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(1));
	    }

	}

	private void createRecordFiles(RecordFile file) {

		Iterator it = mapCatalog.entrySet().iterator();
		if(getFileName().endsWith(".csv")) {
			String prefix = "dpltmp/" + getFileName().replace(".csv", "-");
			while(it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				File tFile = downloadFile(mapCatalog.get(pair.getKey()), prefix + pair.getKey() + ".csv");
				recordFiles.put("temp", new CSVRecordFile(tFile)); // TODO: Replace temp with sheet/file name
				it.remove();
			}
		}
		else if(file.getFile().getName().endsWith(".xlsx")) {
			while(it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				String sheetName = mapCatalog.get(pair.getKey()).replace("#", "");
				recordFiles.put(sheetName, new SpreadsheetRecordFile(file.getFile(), sheetName));
				it.remove();
			}
		}

	}

	public boolean checkSheets() {

		// TODO: Add code to verify sheets

		return true;

	}

	private String createTurtle() {

		String ttl = NameSpaces.getInstance().printTurtleNameSpaceList();

		Iterator it = recordFiles.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry next = (Map.Entry) it.next();
			SpreadsheetParsingResult spr = ((SpreadsheetRecordFile) next.getValue()).processSheet((String) next.getKey());
			ttl += "\n# concept: " + next.getKey() + "\n" + spr.getTurtle() + "\n";
			it.remove();
		}

		return ttl;

	}

	private void uploadTurtle(String ttl) {

		String tempFile = "";
		try {
			String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
			tempFile = "tmp/ttl/DPL-" + timeStamp + ".ttl";
			FileUtils.writeStringToFile(new File(tempFile), ttl, "utf-8");

			DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_GRAPH));
			Model model = RDFDataMgr.loadModel(tempFile);
			accessor.add(model);
			AnnotationLog.println(Feedback.println(Feedback.WEB, String.format("[OK] %d triple(s) have been committed to triple store", model.size())), getFileName());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

	}

	public File downloadFile(String fileURL, String fileName) {
		if(fileURL == null || fileURL.length() == 0){
			return null;
		} else {
			try {
				URL url = new URL(fileURL);
				File file = new File(fileName);
				FileUtils.copyURLToFile(url, file);
				return file;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public boolean checkCellValue(String str) {
		if(str.contains(",")){
			return false;
		}
		if(str.contains(" ")){
			return false;
		}
		return true;
	}
	
	public boolean checkCellUriRegistered(String str) {
        String prefixString = NameSpaces.getInstance().printSparqlNameSpaceList();
//        System.out.println(prefixString);
        if (str.contains(":")){
        	String[] split = str.split(":");
        	String prefixname = split[0];
    		if (!prefixString.contains(prefixname)){
    			return false;
    		}
    		return true;
        } else {
        	return true;
        }
	}
	
	public boolean checkCellUriResolvable(String str) {

        if (str.contains(":")){
        	if (URIUtils.isValidURI(str)){
	        	try {
	        		URIUtils.convertToWholeURI(str);
	        	} catch (Exception e) {
					return false;
				}
        	} else {
        		return false;
        	}
        }
        return true;
	}
	
}
