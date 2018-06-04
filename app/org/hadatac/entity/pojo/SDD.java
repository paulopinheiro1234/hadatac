package org.hadatac.entity.pojo;

import java.io.File;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

public class SDD {
	private Map<String, String> mapCatalog = new HashMap<String, String>();
	private Map<String, String> codeMappings = new HashMap<String, String>();
	private Map<String, String> mapAttrObj = new HashMap<String, String>();
	private Map<String, Map<String, String>> codebook = new HashMap<String, Map<String, String>>();
	private Map<String, Map<String, String>> timeline = new HashMap<String, Map<String, String>>();
	private RecordFile sddfile = null;
	
	public SDD(RecordFile file) {
		this.sddfile = file;
		readCatalog(file);
	}
	
	public String getName() {
	    String sddName = mapCatalog.get("Study_ID");
	    System.out.println("sddName: " + sddName);
	    if (sddName == null) {
	        return "";
	    }
	    return sddName;
	}
	
	public String getNameFromFileName() {
	    return (sddfile.getFile().getName().split("\\.")[0]).replace("_", "-").replace("SDD-", "");
    }
	
	public String getFileName() {
	    return sddfile.getFile().getName();
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
	
	public Map<String, Map<String, String>> getTimeLine() {
		return timeline;
	}
	
	private void readCatalog(RecordFile file) {
	    if (!file.isValid()) {
            return;
        }
	    
	    for (Record record : file.getRecords()) {
	        mapCatalog.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(1));
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
	
	public boolean checkStudyIndicatorPath(String str) {
		String indvIndicatorQuery = "";
		indvIndicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
		indvIndicatorQuery += " SELECT ?a ?b WHERE { "
			  +  str + "  rdfs:subClassOf ?a ."
			  + " ?a rdfs:subClassOf ?b . "
			  +	" } ";
		
		try {		
			ResultSetRewindable resultsrwIndvInd = SPARQLUtils.select(
	                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), indvIndicatorQuery);
			
			List<String> answer = new ArrayList<String>();
			while (resultsrwIndvInd.hasNext()) {
				QuerySolution soln = resultsrwIndvInd.next();
				answer.add(soln.get("a").toString());
				answer.add(soln.get("b").toString());
				}
//			System.out.println(answer);
			if (answer.contains("http://hadatac.org/ont/hasco/StudyIndicator")) {
				return true;
			} else {
				return false;
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
			AnnotationLog.printException("The 'Attribute' column " + str + " formed a bad query to the KG.", sddfile.getFile().getName());
		}
		return true;
	}
	
	public void printErrList(List<String> list, int num) {
		if (list.size()>0){
			String listString = "";
			for (String s : list) {
			    listString += s + "; ";
			}
		if (num == 1){
			AnnotationLog.printException("The Dictionary Mapping has unresolvable uris in cells: " + listString + " .", sddfile.getFile().getName());
		} else if (num == 2){
			AnnotationLog.printException("The Dictionary Mapping has unregistered namespace in cells: " + listString + " .", sddfile.getFile().getName());
		} else if (num == 3){
			AnnotationLog.printException("The Attributes: " + listString + " NOT hasco:StudyIndicator .", sddfile.getFile().getName());
		} else if (num == 4){
			AnnotationLog.printException("The Dictionary Mapping has incorrect content in :" + listString + "in \"attributeOf\" column.", sddfile.getFile().getName());
		}
		}
	}
		
	public boolean readDataDictionary(RecordFile file) {
		
		if (!file.isValid()) {
			return false;
		}
        AnnotationLog.println("The Dictionary Mapping has " + file.getHeaders().size() + " columns.", sddfile.getFile().getName());
        
        Boolean uriResolvable = true;
        Boolean namespaceRegisterd = true;
        Boolean isStudyIndicator = true;
        
		List<String> checkUriRegister = new ArrayList<String>();
		List<String> checkCellVal = new ArrayList<String>();
		List<String> checkUriResolve = new ArrayList<String>();
		List<String> checkStudyIndicatePath = new ArrayList<String>();
        
		for (Record record : file.getRecords()) {
			if (checkCellValue(record.getValueByColumnIndex(0))){
				String attributeCell = record.getValueByColumnName("Attribute");
				String entityCell = record.getValueByColumnName("Entity");
				String roleCell = record.getValueByColumnName("Role");
				String relationCell = record.getValueByColumnName("Relation");
				
				if (checkCellUriResolvable(attributeCell)){
					if (checkCellUriResolvable(entityCell)){
						if (checkCellUriResolvable(roleCell)){
							if (checkCellUriResolvable(relationCell)){

							} else {
								uriResolvable = false;
								checkUriResolve.add(relationCell);
							}
						} else {
							uriResolvable = false;
							checkUriResolve.add(roleCell);
						}
					} else {
						uriResolvable = false;
						checkUriResolve.add(entityCell);
					}
				} else {
					uriResolvable = false;
					checkUriResolve.add(attributeCell);
				}
				
				if (checkCellUriRegistered(attributeCell)){
					if (checkCellUriRegistered(entityCell)){
						if (checkCellUriRegistered(roleCell)){
							if (checkCellUriRegistered(relationCell)){

							} else {
								namespaceRegisterd = false;
								checkUriRegister.add(relationCell);
							}
						} else {
							namespaceRegisterd = false;
							checkUriRegister.add(roleCell);
						}
					} else {
						namespaceRegisterd = false;
						checkUriRegister.add(entityCell);
					}
				} else {
					namespaceRegisterd = false;
					checkUriRegister.add(attributeCell);
				}
				
				if (URIUtils.isValidURI(attributeCell)){
					isStudyIndicator = checkStudyIndicatorPath(attributeCell);
					if (!isStudyIndicator) {
						checkStudyIndicatePath.add(attributeCell);
					}
				} else {
					if (entityCell.length() == 0){
						isStudyIndicator = false;
						checkStudyIndicatePath.add(attributeCell);
					}
				}

				if (checkCellValue(record.getValueByColumnName("attributeOf"))){
					mapAttrObj.put(record.getValueByColumnIndex(0), record.getValueByColumnName("attributeOf"));
				} else {
					checkCellVal.add(record.getValueByColumnName("attributeOf"));
					return false;
				}
			} else {
				AnnotationLog.printException("The Dictionary Mapping has incorrect content " + record.getValueByColumnName("Column") + "in \"Column\" column.", sddfile.getFile().getName());
				return false;
			}
		}
		
		printErrList(checkUriResolve, 1);
		printErrList(checkUriRegister, 2);
		printErrList(checkStudyIndicatePath, 3);
		printErrList(checkCellVal, 4);	
		
		if (uriResolvable == true){
			AnnotationLog.println("The Dictionary Mapping has resolvable uris.", sddfile.getFile().getName());	
		}
		if (namespaceRegisterd == true){
			AnnotationLog.println("The Dictionary Mapping has namespaces all registered.", sddfile.getFile().getName());	
		}
		if (isStudyIndicator == true){
			AnnotationLog.println("The Dictionary Mapping has all attributes being subclasses of hasco:StudyIndicator.", sddfile.getFile().getName());	
		}
		
		AnnotationLog.println("The Dictionary Mapping has correct content under \"Column\" and \"attributeOf\" columns.", sddfile.getFile().getName());
		System.out.println("mapAttrObj: " + mapAttrObj);
		
		return true;
	}
	
	public boolean readCodeMapping(RecordFile file) {
		if (!file.isValid()) {
			return false;
		}
		
		for (Record record : file.getRecords()) {
			codeMappings.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(1));
		}
		
		if (codeMappings.isEmpty()){
			return false;
		} else {
			return true;
		}
	}
	
	public boolean readCodebook(RecordFile file) {
		if (!file.isValid()) {
			return false;
		}
		
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
		
		if (codebook.isEmpty()){
			return false;
		} else {
			return true;
		}
	}
	
	public boolean readTimeline(RecordFile file) {
		if (!file.isValid()) {
			return false;
		}
		
		for (Record record : file.getRecords()) {
			if (!record.getValueByColumnName("Name").isEmpty()) {
				String primaryKey = record.getValueByColumnName("Name");
				
				Map<String, String> timelineRow = new HashMap<String, String>();
				List<String> colNames = new ArrayList<String>();
				colNames.add("Label");
				colNames.add("Type");
				colNames.add("Start");
				colNames.add("End");
				colNames.add("Unit");
				colNames.add("inRelationTo");
				
				for (String col : colNames) {
				    if (!record.getValueByColumnName(col).isEmpty()) {
	                    timelineRow.put(col, record.getValueByColumnName(col));
	                } else {
	                    timelineRow.put(col, "null");
	                }
				}
				timeline.put(primaryKey, timelineRow);
			}
		}
		
		if(timeline.isEmpty()){
			return false;
		} else {
			return true;
		}
	}
}
