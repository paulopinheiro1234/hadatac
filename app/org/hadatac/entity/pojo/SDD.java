package org.hadatac.entity.pojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.metadata.loader.URIUtils;

public class SDD {
	private Map<String, String> mapCatalog = new HashMap<String, String>();
	private Map<String, String> codeMappings = new HashMap<String, String>();
	private Map<String, String> mapAttrObj = new HashMap<String, String>();
	private Map<String, Map<String, String>> codebook = new HashMap<String, Map<String, String>>();
	private Map<String, List<String>> timelineMap = new HashMap<String, List<String>>();
	private File sddFile = null;
	
	public SDD(File file) {
		this.sddFile = file;
		readCatalog();
	}
	
	public String getName() {
		return (sddFile.getName().split("\\.")[0]).replace("_", "-").replace("SDD-", "");
	}
	
	public Map<String, String> getCatalog() {
		return mapCatalog;
	}
	
	public Map<String, String> getCodeMapping() {
		return codeMappings;
	}
	
	public Map<String, String> getMapAttrObj() {
		return mapAttrObj;
	}
	
	public Map<String, Map<String, String>> getCodebook() {
		return codebook;
	}
	
	public Map<String, List<String>> getTimeLineMap() {
		return timelineMap;
	}
	
	private void readCatalog() {
		if(sddFile.getName().endsWith(".csv")) {
			Iterable<CSVRecord> records = null;
			System.out.println("we are here ... " + sddFile);
			try {
				BufferedReader bufRdr = new BufferedReader(new FileReader(sddFile));
				records = CSVFormat.DEFAULT.withHeader().parse(bufRdr);
				for (CSVRecord record : records) {
					mapCatalog.put(record.get(0), record.get(1));
				}
				bufRdr.close();
			} catch (Exception e) {
				System.out.println("Error annotateDataAcquisitionSchemaFile: Unable to Read CSV File");
			}
		} else if (sddFile.getName().endsWith(".xlsx")){
			try {
				InputStream inputStream = new FileInputStream(sddFile);
				Sheet sheet = WorkbookFactory.create(inputStream).getSheetAt(0);
				for (int rowNum = 1; rowNum < 11; rowNum++) {
					Row r = sheet.getRow(rowNum);
					if (r == null) {
						// This whole row is empty
						// Handle it as needed
						continue;
					} else {
						mapCatalog.put(r.getCell(0).getStringCellValue(), r.getCell(1).getStringCellValue());
						System.out.println(r.getCell(0).getStringCellValue() + " and " + r.getCell(1).getStringCellValue());
					}
				}
				inputStream.close();
			} catch (Exception e) {
				System.out.println("Error annotateDataAcquisitionSchemaFile: Unable to Read XLSX File");
			}
		}
	}
	
	public File downloadFile(String fileURL, String fileName) {
		System.out.println("fileURL " + fileURL + fileURL.getClass());
		System.out.println("fileName " + fileName);
		if(fileURL == null || fileURL.length() == 0){
			System.out.println("I'm actually here!!!");
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
		
	public void readDataDictionary(RecordFile file) {
		for (Record record : file.getRecords()) {
			mapAttrObj.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(2));
		}
		System.out.println("mapAttrObj: " + mapAttrObj);
	}
	
	public void readCodeMapping(RecordFile file) {
		for (Record record : file.getRecords()) {
			codeMappings.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(1));
		}
	}
	
	public void readCodebook(RecordFile file) {
		for (Record record : file.getRecords()) {
			if (!record.getValueByColumnIndex(0).isEmpty()) {
				String colName = record.getValueByColumnIndex(0);
				Map<String, String> mapCodeClass = null;
				if (!codebook.containsKey(colName)) {
					mapCodeClass = new HashMap<String, String>();
					codebook.put(colName, mapCodeClass);
				} else {
					mapCodeClass = codebook.get(colName);
				}
				String classUri = "";
				if (!record.getValueByColumnIndex(3).isEmpty()) {
					// Class column
					classUri = URIUtils.replacePrefixEx(record.getValueByColumnIndex(3));
				} 
//					else {
//						// Resource column
//						classUri = URIUtils.replacePrefixEx(record.get(4));
//					}
				mapCodeClass.put(record.getValueByColumnIndex(1), classUri);
			}
		}
	}
	
	public void readtimelineFile(RecordFile file) {
		for (Record record : file.getRecords()) {
			if (!record.getValueByColumnIndex(0).isEmpty()) {
				String colName = record.getValueByColumnIndex(0);
				List<String> tmpList = new ArrayList<String>();
				if (!record.getValueByColumnIndex(1).isEmpty()) {
					tmpList.add(record.getValueByColumnIndex(1));
				} else {
					tmpList.add("null");
				}
				if (!record.getValueByColumnIndex(2).isEmpty()) {
					tmpList.add(record.getValueByColumnIndex(2));
				} else {
					tmpList.add("null");
				}
				if (!record.getValueByColumnIndex(5).isEmpty()) {
					tmpList.add(record.getValueByColumnIndex(5));
				} else {
					tmpList.add("null");
				}
				System.out.println(tmpList.get(0) + " " + tmpList.get(1) + " " + tmpList.get(2));
				timelineMap.put(colName, tmpList);
			}
		}
	}
}
