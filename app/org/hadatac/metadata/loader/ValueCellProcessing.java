package org.hadatac.metadata.loader;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.hadatac.utils.NameSpace;
import org.hadatac.utils.NameSpaces;

public class ValueCellProcessing {
	
	private static boolean isFullURI(String str) {
		return str.startsWith("http");
	}
	
	public static boolean isAbbreviatedURI(String str) {
		if (str.trim().split(" ").length >= 2){
			return false;
		}
		if (!str.contains(":") || str.contains("//"))
			return false;
		if (str.substring(0,str.indexOf(':')).contains(" "))
			return false;
		if (str.substring(str.indexOf(':') + 1, str.length()).contains(":"))
			return false;
		return true;
	}
	
	/*
	 *  the method verifies if cellContent contains a set of URIs, which we call an object set. Returns true if 
	 *  the content is regarded to be an object set.
	 */
	public static boolean isObjectSet (String cellContent) {

		if (cellContent.startsWith("<") && (cellContent.endsWith(">"))){
			cellContent = cellContent.replace("<", "").replace(">", "").replace("&", ", ");
		}
		else if(cellContent.contains("&")){
			boolean isValid = true;
			StringTokenizer st = new StringTokenizer(cellContent, "&");
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				if(!isAbbreviatedURI(token)){
					isValid = false;
					break;
				}
			}
			if(isValid){
				cellContent = cellContent.replace("&", ", ");
			}
		}
		// we need to tokanize the string and verify that the first token is an URI
		StringTokenizer st = new StringTokenizer(cellContent, ",");

		// the string needs to have at least two tokens
		String firstToken;
		if (!st.hasMoreTokens()) {
			return false;
		}
		firstToken = st.nextToken().trim();
		if (!st.hasMoreTokens()) {
			return false;
		}

		// the first token (we could also test the second) needs to be an URI
		return (isFullURI(firstToken) || isAbbreviatedURI(firstToken));
	}
	
	/* 
	 *  if the argument str starts with the URI of one of the name spaces registered in NameSpaces.table, the
	 *  URI gets replaced by the name space's abbreviation. Otherwise, the string is returned wrapper
	 *  around angular brackets.
	 */
	public static String replaceNameSpace(String str) {
		String resp = str;
	    for (Map.Entry<String, NameSpace> entry : NameSpaces.getInstance().table.entrySet()) {
	        String abbrev = entry.getKey().toString();
	        String nsString = entry.getValue().getName();
	        if (str.startsWith(nsString)) {
	        	resp = str.replace(nsString, abbrev + ":");
	        	return resp; 
	        }
	    }
	    return "<" + str + ">";
	}
	
	public static String replaceNameSpaceEx(String str) {
		if (null == str) {
			return "";
		}
		String resp = str;
	    for (Map.Entry<String, NameSpace> entry : NameSpaces.getInstance().table.entrySet()) {
	        String abbrev = entry.getKey().toString();
	        String nsString = entry.getValue().getName();
	        if (str.startsWith(nsString)) {
	        	resp = str.replace(nsString, abbrev + ":");
	        	return resp;
	        }
	    }
	    return str;
	}
	
	/* 
	 *  if the argument str starts with the abbreviation of one of the name spaces registered in NameSpaces.table, the
	 *  abbreviation gets replaced by the name space's URI. Otherwise, the string is returned wrapper
	 *  around angular brackets.
	 */
	public static String replacePrefix(String str) {
		String resp = str;
	    for (Map.Entry<String, NameSpace> entry : NameSpaces.table.entrySet()) {
	        String abbrev = entry.getKey().toString();
	        String nsString = entry.getValue().getName();
	        if (str.startsWith(abbrev + ":")) {
	        	resp = str.replace(abbrev + ":", nsString);
	        	return resp; 
	        }
	    }
	    return "<" + str + ">";
	}
	
	/* 
	 *  if the argument str starts with the abbreviation of one of the name spaces registered in NameSpaces.table, the
	 *  abbreviation gets replaced by the name space's URI. Otherwise, the string is returned wrapper
	 *  around angular brackets.
	 */
	public static String replacePrefixEx(String str) {
		String resp = str;
	    for (Map.Entry<String, NameSpace> entry : NameSpaces.table.entrySet()) {
	        String abbrev = entry.getKey().toString();
	        String nsString = entry.getValue().getName();
	        if (str.startsWith(abbrev + ":")) {
	        	resp = str.replace(abbrev + ":", nsString);
	        	return resp; 
	        }
	    }
	    return str;
	}
	
	/* 
	 *  check if the namespace in str is in the namamespace list (NameSpaces.table). 
	 *  If not, it issues a warning message. A warning message is issue if the name 
	 *  space used in the argument str is not registered in NameSpaces.table.
	 */
	public static void validateNameSpace(String str) {
		if (str.indexOf(':') <= 0)
			return;
		String abbrev = "";
		String nsName = str.substring(0,(str.indexOf(':') + 1));
	    for (Map.Entry<String, NameSpace> entry : NameSpaces.table.entrySet()) {
	        abbrev = entry.getKey().toString() + ":";
	        if (abbrev.equals(nsName)) {
	        	return;
	        }
	    }
		System.out.println("# WARNING: NAMESPACE NOT DEFINED <" + nsName + ">");
		return;
	}
	
	public static String processSubjectValue(String subject) {
		if (isAbbreviatedURI(subject)) {
			validateNameSpace(subject);
			return (subject + "\n");
		}
		// no indentation or semicolon at the end of the string
		return (replaceNameSpace(subject) + "\n");
	}
	
	public static String processObjectValue(String object) {
		// if abbreviated URI, just print it
		if (isAbbreviatedURI(object)) { 
			validateNameSpace(object);
			return object;
		}

		// if full URI, either abbreviated it or print it between angled brackets
		if (isFullURI(object)) {
			// either replace namespace with acronym or add angled brackets
			return replaceNameSpace(object);
		}
		
		// if not URI, print the object between quotes
		object = object.replace("\n", " ").replace("\r", " ").replace("\"", "''");
		return "\"" + object + "\"";
	}
	
	public static String convertToWholeURI(String object){
    	if (isAbbreviatedURI(object)) {
    		validateNameSpace(object);
    		String resp = object;
    	    for (Map.Entry<String, NameSpace> entry : NameSpaces.table.entrySet()) {
    	        String abbrev = entry.getKey().toString();
    	        String nsString = entry.getValue().getName();
    	        if (object.startsWith(abbrev + ":")) {
    	        	resp = object.replace(abbrev + ":", nsString);
    	        	return resp;
    	        }
    	    }
    	}
    	
    	return object;
    }
	
	public static String exec(Cell cell, Vector<String> predicates) {
		String clttl = "";
		String cellValue = cell.getStringCellValue();
		String predicate = predicates.get(cell.getColumnIndex());

		// cell has subject value
		if (predicate.equals("hasURI")) {
			clttl = clttl + processSubjectValue(cell.getStringCellValue());
			return clttl;
		}
		
		// cell has object value
		clttl = clttl + "   " + predicate + " ";
		if (isObjectSet(cellValue)) {
		     StringTokenizer st = new StringTokenizer(cellValue,",");
		     while (st.hasMoreTokens()) {
		         clttl = clttl + processObjectValue(st.nextToken().trim());
		         if (st.hasMoreTokens()) {
		        	 clttl = clttl + ", ";
		         }
		     }
		} else {
			clttl = clttl + processObjectValue(cellValue);
		}
		clttl = clttl + ";\n";
				
		return clttl;
	}
	
	public static String execCellValue(String cellValue, String predicate) {
		String clttl = "";

		// cell has subject value
		if (predicate.equals("hasURI")) {
			clttl = clttl + processSubjectValue(cellValue);
			return clttl;
		}
		
		// cell has object value
		clttl = clttl + "   " + predicate + " ";
		if (isObjectSet(cellValue)) {
		     StringTokenizer st = new StringTokenizer(cellValue,",");
		     while (st.hasMoreTokens()) {
		         clttl = clttl + processObjectValue(st.nextToken().trim());
		         if (st.hasMoreTokens()) {
		        	 clttl = clttl + ", ";
		         }
		     }
		} else {
			clttl = clttl + processObjectValue(cellValue);
		}
		clttl = clttl + ";\n";
				
		return clttl;
	}
}
