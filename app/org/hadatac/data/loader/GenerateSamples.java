package org.hadatac.data.loader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.String;

public class GenerateSamples {

    public static void main(String[] args) throws IOException {
        String inputFile="SID.csv";
        String outputFile="samples.tsv";
        String line = "";
        String comma = ",";
        int counter=0;
        int pilotNum;
        String samplingVolUnit = ""; //"obo:UO_0000095"; //"uo:ml"
        String storageTemp; //-80;
        String storageTempUnit = "obo:UO_0000027"; //"uo:degree Celsius"
        String FTcount; // = 0;
        String sampleType = "";// = "chear:WholeBlood";
        String objectOf = ""; // = "chear-kb:DA-PS6-DNAIsolation";
        //float samplingVol; // = 8.5f;
        String samplingVol;
        String samplingMethod = ""; //"PaxGene Blood DNA";
        String sampleSuffix = "";// "Whole_Blood-DNA_Isolation";
        String sampleID = "";
        String subjectID = "";
        String kbPrefix="chear-kb:";
        BufferedWriter bw = null;
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        
        try {
        	File file = new File(outputFile);
        	if (!file.exists()) {
    	        file.createNewFile();
        	}
    	    FileWriter fw = new FileWriter(file);
    	    bw = new BufferedWriter(fw);
            bw.write("hasURI\ta\trdfs:label\thasco:originalID\thasco:isSampleOf\thasco:isMeasuredObjectOf\trdfs:comment\thasco:hasSamplingMethod\thasco:hasSamplingVolume\thasco:hasSamplingVolumeUnit\thasco:hasStorageTemperature\thasco:hasStorageTemperatureUnit\thasco:hasNumFreezeThaw\n");
            //try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
                while ((line = br.readLine()) != null) {
                    // use comma as separator
                    String[] columns = line.split(comma);
                    if(counter==0){
                        /*System.out.println("columns: " + columns);  
                        System.out.println("columns[0]: " + columns[0]); 
                        System.out.println("columns[1]: " + columns[1]); 
                        System.out.println("columns[2]: " + columns[2]); 
                        System.out.println("columns[3]: " + columns[3]); 
                        System.out.println("columns[4]: " + columns[4]); 
                        System.out.println("columns[5]: " + columns[5]); 
                        System.out.println("columns[6]: " + columns[6]); 
                        System.out.println("columns[7]: " + columns[7]); 
                        System.out.println("columns[8]: " + columns[8]);   
                        System.out.println("columns[9]: " + columns[9]); 
                        System.out.println("columns[10]: " + columns[10]); 
                        System.out.println("columns[11]: " + columns[11]); 
                        System.out.println("columns[12]: " + columns[12]);   
                        System.out.println("columns[13]: " + columns[13]); 
                        System.out.println("columns[14]: " + columns[14]); 
                        System.out.println("columns[15]: " + columns[15]); 
                        System.out.println("columns[16]: " + columns[16]);    
                        System.out.println("columns[17]: " + columns[17]); 
                        System.out.println("columns[18]: " + columns[18]); 
                        System.out.println("columns[19]: " + columns[19]); 
                        System.out.println("columns[20]: " + columns[20]);   
                        System.out.println("columns[21]: " + columns[21]); 
                        System.out.println("columns[22]: " + columns[22]); 
                        System.out.println("columns[23]: " + columns[23]); 
                        System.out.println("columns[24]: " + columns[24]);  
                        System.out.println("columns[25]: " + columns[25]);   
                        System.out.println("columns[26]: " + columns[26]); 
                        System.out.println("columns[27]: " + columns[27]); 
                        System.out.println("columns[28]: " + columns[28]); 
                        System.out.println("columns[29]: " + columns[29]);  */   
                    } else {
                        sampleID = columns[0];
                        sampleSuffix = columns[3];
//                        kbPrefix = columns[6] + ":";
                        subjectID = columns[7];
                        pilotNum = Integer.parseInt(columns[8]);
                        sampleType = columns[11];
                        samplingMethod = columns[12];
                        samplingVol = columns[15];
                        samplingVolUnit = columns[16];
                        storageTemp = columns[18];
                        FTcount = columns[19];
                        //hasURI
                        bw.write(kbPrefix + "SPL-" + String.format("%04d", counter) + "-Pilot-" + pilotNum + "-" + sampleSuffix + "\t");
                        //rdf:type                
                        bw.write(sampleType + "\t");
                        //rdfs:label
                        bw.write("SID " + String.format("%04d", counter) + " - Pilot " + pilotNum + " " + sampleSuffix + "\t");
                        //hasco:originalID
                        bw.write(sampleID + "\t");
                        //hasco:isSampleOf
                        bw.write(kbPrefix + "SBJ-" + String.format("%04d", counter) + "-" + subjectID + "-Pilot-" + pilotNum + "\t");
                        // hasco:isMeasuredObjectOf
                        bw.write(objectOf + "\t");
                        // rdfs:comment
                        bw.write("Sample " + String.format("%04d", counter) + " for Pilot " + pilotNum + " " + sampleSuffix + "\t");
                        // hasco:hasSamplingMethod
                        bw.write(samplingMethod + "\t");
                        //hasco:hasSamplingVolume
                        bw.write(samplingVol + "\t");
                        //hasco:hasSamplingVolumeUnit
                        bw.write(samplingVolUnit + "\t");
                        //hasco:hasStorageTemperature
                        bw.write(storageTemp + "\t");
                        //hasco:hasStorageTemperatureUnit
                        bw.write(storageTempUnit + "\t");
                        //hasNumFreezeThaw
                        bw.write(FTcount + "\n");
                    }              
                    counter++;
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
    	    try{ 
     	       if(br!=null)
         		   br.close();
     	    }catch(Exception ex){
     	        System.out.println("Error in closing the BufferedReader: " + ex);
 	        }
    	}
    }
}