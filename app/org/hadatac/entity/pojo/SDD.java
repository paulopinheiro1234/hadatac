package org.hadatac.entity.pojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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
	
	public File readSheetfromExcel(String sheetName, Workbook wb, String fileName) {
		
		System.out.println(fileName);
		if(sheetName.charAt(0) == '#')  {
			Sheet currentsheet = wb.getSheet(sheetName.replace("#", ""));

			HashMap<Integer, List<String>> filetbc = new HashMap<Integer,List<String>>();
			Iterator<Row> ritr = currentsheet.iterator();
			int j = 0;
			int lasycellnum = 0;
			while(ritr.hasNext()){
				Row current_r = ritr.next();
				Iterator<Cell> citr = current_r.iterator();
				if (citr.hasNext()){
					if(current_r.getCell(0).toString().length()>0){
						List<String> row_content = new ArrayList<String>();				
						if(j == 0){
							lasycellnum = current_r.getLastCellNum();
							System.out.println(lasycellnum + " cells in this row");
						}
						for(int i=0; i<lasycellnum; i++){
							if(current_r.getCell(i) == null || current_r.getCell(i).toString().isEmpty()){
								row_content.add("");
							} else {
								row_content.add(current_r.getCell(i).toString());
							}
						}
							
						System.out.println(row_content);
						filetbc.put(j, row_content);
						j++;
					}
				}
			}
			try {
				
				FileWriter writer = new FileWriter(fileName);
		        for (int i=0; i<filetbc.entrySet().size(); i++){
		        	List<String> c_str = filetbc.get(i);
		        	for (String str : c_str){
		        		if (str.contains(", ")){
		        			c_str.set(c_str.indexOf(str), str.replace(", ", "&"));
		        		}
		        	}
		            String collect = filetbc.get(i).stream().collect(Collectors.joining(","));
		            writer.write(collect);
		            writer.write("\n");
		        }
	            writer.close();
			    
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
		} else {
			System.out.println("Error readSheetfromExcel(): Contaisn illegal links in infosheet.");
		}
		
	    File file = new File(fileName);
		return file;
		
	}
	
	public void readDataDictionary(File file) {
		if (file == null){
			System.out.println("Error readDataDictionary(): Empty URL");
		} else {
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

	}
	
	public void readCodeMapping(File file) {
		if (file == null){
			System.out.println("Error readCodeMapping(): Empty URL");
		} else {
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
	}
	
	public void readCodebook(File file) {
		if (file == null){
			System.out.println("Error readCodebook(): Empty URL");
		} else {
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
						} 
	//					else {
	//						// Resource column
	//						classUri = ValueCellProcessing.replacePrefixEx(record.get(4));
	//					}
						mapCodeClass.put(record.get(1), classUri);
					}
				}
				bufRdr.close();
			} catch (Exception e) {
				System.out.println("Error readCodebook(): Unable to Read File");
			}
		}
	}
	
	public void readtimelineFile(File file) {
		if (file == null){
			System.out.println("Error readtimelineFile(): Empty URL");
		} else {
			Iterable<CSVRecord> records = null;
			try {
				BufferedReader bufRdr = new BufferedReader(new FileReader(file));
				records = CSVFormat.DEFAULT.withHeader().parse(bufRdr);
				for (CSVRecord record : records) {
					if (!record.get(0).isEmpty()) {
						String colName = record.get(0);
						List<String> tmpList = new ArrayList<String>();
						if (!record.get(1).isEmpty()) {
							tmpList.add(record.get(1));
						} else {
							tmpList.add("null");
						}
						if (!record.get(2).isEmpty()) {
							tmpList.add(record.get(2));
						} else {
							tmpList.add("null");
						}
						if (!record.get(5).isEmpty()) {
							tmpList.add(record.get(5));
						} else {
							tmpList.add("null");
						}
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
}
