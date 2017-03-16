package org.hadatac.data.loader;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.lang.String;

public class GenerateSubjects {

    public static void main(String[] args) throws IOException {

        int pilotNum=6;
        String kbPrefix="chear-kb:";
        String inputFile="subjectList.csv";
        String outputFile="subjects.tsv";
        BufferedWriter bw = null;

        System.out.println("About to Read in file");
        Scanner s = new Scanner(new File(inputFile));
        ArrayList<String> subjectIDs = new ArrayList<String>();
        while (s.hasNext()){
            subjectIDs.add(s.next());
        }
        s.close();
        
        int numSubjects=subjectIDs.size();

        try {
        	File file = new File(outputFile);
        	if (!file.exists()) {
    	        file.createNewFile();
        	}
    	    FileWriter fw = new FileWriter(file);
    	    bw = new BufferedWriter(fw);
            bw.write("hasURI\ta\trdfs:label\thasco:originalID\thasco:isSubjectOf\n");
            for (int i=0;i<numSubjects;i++) {
                //hasURI
                bw.write(kbPrefix + "SBJ-" + String.format("%04d", i+1) + "-Pilot-" + pilotNum + "\t");
                //rdf:type                
                bw.write("sio:Human\t");
                //rdfs:label
                bw.write("ID " + String.format("%04d", i+1) + " - Pilot " + pilotNum + "\t");
                //hasco:originalID
                bw.write(subjectIDs.get(i) + "\t");
                //hasco:isSubjectOf
                bw.write(kbPrefix + "CH-Pilot-" + pilotNum + "\n");         
            }
            System.out.println("File written Successfully");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    	finally
    	{ 
    	    try{ 
    	       if(bw!=null)
        		   bw.close();
    	    }catch(Exception ex){
    	        System.out.println("Error in closing the BufferedWriter: " + ex);
	        }
    	}
    }

}