package org.hadatac.entity.pojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hadatac.metadata.loader.ValueCellProcessing;

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
		return (sddFile.getName().split("\\.")[0]).replace("SDD-", "");
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
				for (int rowNum = 1; rowNum < Math.min(7, sheet.getLastRowNum()); rowNum++) {
					Row r = sheet.getRow(rowNum);
					if (r == null) {
						// This whole row is empty
						// Handle it as needed
						continue;
					} else {
						mapCatalog.put(r.getCell(0).getStringCellValue(), r.getCell(1).getStringCellValue());
					}
				}
				inputStream.close();
			} catch (Exception e) {
				System.out.println("Error annotateDataAcquisitionSchemaFile: Unable to Read XLSX File");
			}
		}
	}
	
	public File downloadFile(String fileURL, String fileName) {
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
	
	public void readDataDictionary(File file) {
		Iterable<CSVRecord> records = null;
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(file));
			records = CSVFormat.DEFAULT.withHeader().parse(bufRdr);
			for (CSVRecord record : records) {
				mapAttrObj.put(record.get(0), record.get(2));
			}
			bufRdr.close();
			System.out.println("mapAttrObj: " + mapAttrObj);
		} catch (Exception e) {
			System.out.println("Error readDataDictionary(): Unable to Read File");
		}
	}
	
	public void readCodeMapping(File file) {
		Iterable<CSVRecord> records = null;
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(file));
			records = CSVFormat.DEFAULT.withHeader().parse(bufRdr);
			for (CSVRecord record : records) {
				codeMappings.put(record.get(0), record.get(1));
			}
			bufRdr.close();
		} catch (Exception e) {
			System.out.println("Error readCodeMapping(): Unable to Read File");
		}
	}
	
	public void readCodebook(File file) {
		Iterable<CSVRecord> records = null;
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(file));
			records = CSVFormat.DEFAULT.withHeader().parse(bufRdr);
			for (CSVRecord record : records) {
				if (!record.get(0).isEmpty()) {
					String colName = record.get(0);
					Map<String, String> mapCodeClass = null;
					if (!codebook.containsKey(colName)) {
						mapCodeClass = new HashMap<String, String>();
						codebook.put(colName, mapCodeClass);
					} else {
						mapCodeClass = codebook.get(colName);
					}
					String classUri = "";
					if (!record.get(3).isEmpty()) {
						// Class column
						classUri = ValueCellProcessing.replacePrefixEx(record.get(3));
					} else {
						// Resource column
						classUri = ValueCellProcessing.replacePrefixEx(record.get(4));
					}
					mapCodeClass.put(record.get(1), classUri);
				}
			}
			bufRdr.close();
		} catch (Exception e) {
			System.out.println("Error readCodebook(): Unable to Read File");
		}
	}
	
	public void readtimelineFile(File file) {
		Iterable<CSVRecord> records = null;
		try {
			BufferedReader bufRdr = new BufferedReader(new FileReader(file));
			records = CSVFormat.DEFAULT.withHeader().parse(bufRdr);
			for (CSVRecord record : records) {
				if (!record.get(0).isEmpty()) {
					String colName = record.get(0);
					List<String> tmpList = new ArrayList<String>();
					tmpList.add(record.get(1));
					tmpList.add(record.get(2));
					tmpList.add(record.get(5));
					System.out.println(tmpList.get(0) + " " + tmpList.get(1) + " " + tmpList.get(2));
					timelineMap.put(colName, tmpList);
				}
			}
			bufRdr.close();
		} catch (Exception e) {
			System.out.println("Error readtimelineFile(): Unable to Read File");
		}
	}
}
