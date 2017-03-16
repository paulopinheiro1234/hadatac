package org.hadatac.data.loader;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.lang.String;

public class GenerateSamples {

    public static void main(String[] args) throws IOException {
        
        int pilotNum=6;
        String kbPrefix="chear-kb:";
        String inputFile="sampleList.csv";
        String outputFile="samples.tsv";
        BufferedWriter bw = null;

        System.out.println("About to Read in file");
        Scanner s = new Scanner(new File(inputFile));
        ArrayList<String> sampleIDs = new ArrayList<String>();
        while (s.hasNext()){
            sampleIDs.add(s.next());
        }
        s.close();
        
        int numSamples=sampleIDs.size();
        // Pilot 6 Sample Structure
        String samplingVolUnit = "obo:UO_0000095"; //"uo:ml"
        int storageTemp = -80;
        String storageTempUnit = "obo:UO_0000027"; //"uo:degree Celsius"
        //DNA Isolation
        int FTcount = 0;
        String sampleType = "chear:WholeBlood";
        String objectOf = "chear-kb:DA-PS6-DNAIsolation";
        float samplingVol = 8.5f;
        String samplingMethod = ""; //"PaxGene Blood DNA";
        String sampleSuffix = "Whole_Blood-DNA_Isolation";
/*        //Untargeted Metabolomics and Lipidomics
        int FTcount = 1;
        String sampleType = "chear:BloodPlasma";
        String objectOf = "chear-kb:DA-PS6-UML";
        float samplingVol = 0.0f;
        String samplingMethod = ""; //null
        String sampleSuffix = "Plasma-UML";
        //ICP-MS
        int FTcount = 1;
        String sampleType = "chear:Urine";
        String objectOf = "chear-kb:DA-PS6-ICP-MS";
        float samplingVol = 0.0f;
        String samplingMethod = ""; //null
        String sampleSuffix = "Urine-ICP-MS";
*/
        try {
        	File file = new File(outputFile);
        	if (!file.exists()) {
    	        file.createNewFile();
        	}
    	    FileWriter fw = new FileWriter(file);
    	    bw = new BufferedWriter(fw);
            bw.write("hasURI\ta\trdfs:label\thasco:originalID\thasco:isSampleOf\thasco:isMeasuredObjectOf\trdfs:comment\thasco:hasSamplingMethod\thasco:hasSamplingVolume\thasco:hasSamplingVolumeUnit\thasco:hasStorageTemperature\thasco:hasStorageTemperatureUnit\thasco:hasNumFreezeThaw\n");
            for (int i=0;i<numSamples;i++) {
                //hasURI
                bw.write(kbPrefix + "SPL-" + String.format("%04d", i+1) + "-Pilot-" + pilotNum + "-" + sampleSuffix + "\t");
                //rdf:type                
                bw.write(sampleType + "\t");
                //rdfs:label
                bw.write("SID " + String.format("%04d", i+1) + " - Pilot " + pilotNum + " " + sampleSuffix + "\t");
                //hasco:originalID
                bw.write(sampleIDs.get(i) + "\t");
                //hasco:isSampleOf
                bw.write(kbPrefix + "SBJ-" + String.format("%04d", i+1) + "-Pilot-" + pilotNum + "\t");
                // hasco:isMeasuredObjectOf
                bw.write(objectOf + "\t");
                // rdfs:comment
                bw.write("Sample " + String.format("%04d", i+1) + " for Pilot " + pilotNum + " " + sampleSuffix + "\t");
                // hasco:hasSamplingMethod
                bw.write(samplingMethod + "\t");
                //hasco:hasSamplingVolume
                bw.write(String.format("%1.2f", samplingVol) + "\t");
                //hasco:hasSamplingVolumeUnit
                bw.write(samplingVolUnit + "\t");
                //hasco:hasStorageTemperature
                bw.write(storageTemp + "\t");
                //hasco:hasStorageTemperatureUnit
                bw.write(storageTempUnit + "\t");
                //hasNumFreezeThaw
                bw.write(FTcount + "\n");
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