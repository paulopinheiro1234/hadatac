package org.hadatac.data.dsgen;

import org.apache.commons.io.FileUtils;
import org.hadatac.console.models.*;
import org.hadatac.entity.pojo.*;
import org.hadatac.utils.Feedback;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GenCodeBook {

    public static boolean exec(Alignment alignment, File file, String ownerEmail, String dataDir) {
      boolean fileCreated = false;
	  try {
	    //File codeBookFile = new File(ConfigProp.getPathDownload() + "/" + file.getName().replace(".csv","_codebook.csv"));
	    String fileName = "download_" + file.getName().substring(7, file.getName().lastIndexOf("_")) + "_codebook.csv";
	    Date date = new Date();
	    //String fileName = "download_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(date) + "_codebook.csv";
	    //DataFile dataFile = new DataFile(codeBookFile.getName());
	    //dataFile.setOwnerEmail(ownerEmail);
	    //dataFile.setStatus(DataFile.CREATING);
	    DataFile dataFile = DataFile.create(fileName, dataDir, ownerEmail, DataFile.CREATING);
	    dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date));
	    dataFile.save();

	    // Write empty string to create the file
        File codeBookFile = new File(dataFile.getAbsolutePath());
        FileUtils.writeStringToFile(codeBookFile, "", "utf-8", true);

	    System.out.println("Harmonized code book [" + codeBookFile.getName() + "]");

	    FileUtils.writeStringToFile(codeBookFile, "code, value, class, varSpec\n", "utf-8", true);
	    // Write code book
	    List<CodeBookEntry> codeBook = new ArrayList<CodeBookEntry>();
	    for (Map.Entry<String, List<String>> entry : alignment.getCodeBook().entrySet()) {
	    	List<String> list = entry.getValue();
	    	System.out.println(list.get(0) + ", " + list.get(1) + ", " + entry.getKey());
	    	String pretty = list.get(1).replace("@en","");
	    	if (!pretty.equals("")) {
	    		String c0 = pretty.substring(0,1).toUpperCase();
	    		if (pretty.length() == 1) {
	    			pretty = c0;
	    		} else {
	    			pretty = c0 + pretty.substring(1);
	    		}
	    	}
    		String codeStr = list.get(0).trim();
    		if (codeStr.length() > 7) {
    			codeStr = codeStr.substring(0,7);
    		}
    		String varSpecKey = list.get(2).trim();
	    	CodeBookEntry cbe = new CodeBookEntry(codeStr, pretty, entry.getKey(), varSpecKey);
	    	codeBook.add(cbe);
	    }
	    if (codeBook != null && codeBook.size() > 0) {
		    codeBook.sort(new Comparator<CodeBookEntry>() {
	            @Override
	            public int compare(CodeBookEntry cbe1, CodeBookEntry cbe2) {
	            	String v1Str = null;
	            	String v2Str = null;
	            	try {
	            		v1Str = cbe1.getCode().trim();
	            		v2Str = cbe2.getCode().trim();
                        return v1Str.compareTo(v2Str);
	            	} catch (Exception e) {
	            		dataFile.getLogger().addLine(Feedback.println(Feedback.WEB, "[ERROR] Measurement: not possible to convert one or both of following codes into integers: [" + v1Str + "] and [" + v2Str + "]"));
	            	}
	            	return 0;
	            }
	        });
		    for (CodeBookEntry cbe : codeBook) {
		    	FileUtils.writeStringToFile(codeBookFile, cbe.getCode() + ",\"" + cbe.getValue() + "\", " + cbe.getCodeClass() +  "\", " + cbe.getVariableSpecKey() + "\n" , "utf-8", true);
		    }
	    }

    	dataFile.setCompletionPercentage(100);
	    dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
	    dataFile.setStatus(DataFile.CREATED);
	    dataFile.save();
	    fileCreated = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
	  	return fileCreated;
    }

    public static String prettyCodeBookLabel(Alignment alignment, String codeClass) {
        List<String> list = alignment.getCodeBook().get(codeClass);
        //System.out.println("CodeClass: [" + codeClass + "]");
        //System.out.println(list.get(0) + ", " + list.get(1));
        String pretty = list.get(1).replace("@en","");
        if (!pretty.equals("")) {
            String c0 = pretty.substring(0,1).toUpperCase();
            if (pretty.length() == 1) {
                pretty = c0;
            } else {
                pretty = c0 + pretty.substring(1);
            }
        }
        return pretty;
    }

}

