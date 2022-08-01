package org.hadatac.data.dsgen;

import org.apache.commons.io.FileUtils;
import org.hadatac.entity.pojo.AlignmentEntityRole;
import org.hadatac.entity.pojo.Attribute;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Entity;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.TimeAlignment;
import org.hadatac.entity.pojo.TimeVariable;
import org.hadatac.metadata.loader.URIUtils;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GenTimeAlignment {

    public static void exec(List<Measurement> measurements, File file, String fileId, String categoricalOption, String timeResolution) {
        DataFile dataFile = null;
        try {
            // Write empty string to create the file
            FileUtils.writeStringToFile(file, "", "utf-8", true);

            // Initiate Alignment and Results
            TimeAlignment timeAlignment = new TimeAlignment();

            // Initiate Results
            //     HashMap<base timestamp, Map<measurement's key, value>, where
            //        - base timestamp is the timestamp to be aligned. 
            Map<String, Map<String, String>> results = new HashMap<String, Map<String, String>>();

            // Prepare rows: Measurements are compared already collected alignment attributes (role/entity/attribute/inrelationto/unit/time)
            //               New alignment attributes are created for measurements with no corresponding alignment attributes.
            int i = 1;
            int prev_ratio = 0;
            int total = measurements.size();
            List<String> tss = new ArrayList<String>();
            //System.out.println("Align-Debug: Measurement size is " + total);
            for (Measurement m : measurements) {
                String referenceTS = null;
                StudyObject referenceObj = null;

                if (m.getTimestampString() != null && !m.getTimestampString().isEmpty()) {
 
                	//System.out.println("Align-Debug: ReferenceTS is [" + m.getTimestampString() + "]   ObjectURI is [" + m.getObjectUri() + "]");

                    // Perform following actions required if the object of the measurement has not been processed yet
                    //   - add a row in the result set for aligning object, if such row does not exist
                    //   - add entity-role to the collection of entity-roles of the alignment
                    //   - add object to the collection of objects of the alignment 
                
                	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    referenceTS = sdf.format(m.getTimestamp()).trim();
                    if (timeResolution.equals("yyyy-MM-dd HH:mm:ss.SS")) {
                    	referenceTS = referenceTS.substring(0,referenceTS.length() - 1);
                    } else if (timeResolution.equals("yyyy-MM-dd HH:mm:ss.S")) {
                    	referenceTS = referenceTS.substring(0,referenceTS.length() - 2);
                    } else if (timeResolution.equals("yyyy-MM-dd HH:mm:ss")) {
                    	referenceTS = referenceTS.substring(0,referenceTS.length() - 4);
                    }
                    //System.out.println("Align-Debug: timestampTS [" + referenceTS + "]");

                    //referenceTS = m.getTimestampString();
            		//System.out.println("Align-Debug: Total timestamps size is " + tss.size());
                	if (!tss.contains(referenceTS)) {
                		tss.add(referenceTS);
                		//System.out.println("Align-Debug: Adding reference timestamp. Total size is " + tss.size());
                    }
                	referenceObj = timeAlignment.getObject(m.getStudyObjectUri());
                    if (referenceObj == null || !referenceObj.getUri().equals(m.getStudyObjectUri())) {
                    	//System.out.println("Align-Debug: Reading object [" + m.getStudyObjectUri() + "]");
                        referenceObj = StudyObject.find(m.getStudyObjectUri());
                        if (referenceObj != null) {
                        	//System.out.println("Align-Debug: Caching object [" + referenceObj.getUri() + "]");
                            timeAlignment.addObject(referenceObj);
                        }
                    }
                	if (referenceTS == null) {
                		//System.out.println("[ERROR] could not find reference timestamp with object with uri " + m.getObjectUri());
                	} else {
                		if (!results.containsKey(referenceTS)) {
                			//System.out.println("Align-Debug: adding entity-role");
                	        Entity referenceObjEntity = timeAlignment.getEntity(referenceObj.getUri());
                			//System.out.println("Align-Debug: HERE 1");
                	        if (referenceObjEntity == null || !referenceObjEntity.getUri().equals(referenceObj.getTypeUri())) {
                	        	referenceObjEntity = Entity.find(referenceObj.getTypeUri());
                	            if (referenceObjEntity == null) {
                	                System.out.println("[ERROR] retrieving entity " + referenceObj.getTypeUri());
                	            } else {
                	                timeAlignment.addEntity(referenceObjEntity);
                	            }
                	        }
                	        if (referenceObjEntity != null) {
                	        	//AlignmentEntityRole referenceEntRole = new AlignmentEntityRole(referenceObjEntity,m.getRole());
                	        	AlignmentEntityRole referenceEntRole = new AlignmentEntityRole(referenceObjEntity,null);
                				if (!timeAlignment.containsRole(referenceObj, referenceEntRole.getKey())) {  // entRole's key is the string of the role plus the label of the entity
                					timeAlignment.addRole(referenceObj, referenceEntRole);
                				}
			    
                				if (results.get(referenceTS) == null) {
                					results.put(referenceTS, new HashMap<String, String>());
                					/*
                					if (results.get(referenceTS) != null && timeAlignment.objectKey(referenceEntRole) != null) {
                						if (referenceObj.getOriginalId() != null) { 
                							//System.out.println("Align-Debug: adding PID " + referenceObj.getOriginalId() + " to result's map as a key: " + alignment.objectKey(referenceEntRole)); 
                							results.get(referenceTS).put(timeAlignment.objectKey(referenceEntRole), referenceObj.getOriginalId());
                						}
                					} */
                				}

                	        } 
                		}
                	}
		    
                	//System.out.println("Align-Debug: processing Object with PID " + m.getObjectPID());
		    
                	// assign values to results
                	String key = timeAlignment.timeMeasurementKey(m);
                	//System.out.println("Align-Debug: computed measurement key [" + key + "]");
                	if (key != null) {
                		String finalValue = "";
			
                		if (categoricalOption.equals(GenConst.WITH_VALUES)){
                			finalValue = m.getValue();
                		} else {
                			//System.out.println("Align-Debug: valueClass :[" + m.getValueClass() + "]    value: [" + m.getValue() + "]"); 
                			if (m.getValueClass() != null && !m.getValueClass().equals("") && URIUtils.isValidURI(m.getValueClass())) {
                				if (!timeAlignment.containsCode(m.getValueClass())) {
                					String code = Attribute.findHarmonizedCode(m.getValueClass());
                					//System.out.println("Align-Debug: new alignment attribute Code [" + code + "] for URI-value [" + m.getValueClass() + "]"); 
                					if (code != null && !code.equals("")) {
                						List<String> newEntry = new ArrayList<String>();
                						newEntry.add(code);
                						newEntry.add(m.getValue());
                						newEntry.add(key);
                						timeAlignment.addCode(m.getValueClass(), newEntry);
                					}	
                				}
                			}
			    
                			if (timeAlignment.containsCode(m.getValueClass())) {
                				// get code for qualitative variables
                				List<String> entry = timeAlignment.getCode(m.getValueClass()); 
                				finalValue = entry.get(0);
                			} else {
                				// get actual value for quantitative variables
                				finalValue = m.getValueClass();
                			}
                		}

                		if (referenceTS != null) {
                			//System.out.println("Align-Debug: final value [" + finalValue + "]");
                			results.get(referenceTS).put(key, finalValue);
                		}
                			
                	} else {
                			
                		System.out.println("[ERROR] the following measurement could not match any alignment attribute (and no alignment " + 
                				"attribute could be created for this measurement): " + 
                				//m.getEntityUri() + " " + m.getCharacteristicUri());
                				m.getEntityUri() + " " + m.getCharacteristicUris().get(0));
                	}
                }
                

                // compute and show progress 
                double ratio = (double)i / total * 100;
                int current_ratio = (int)ratio;
                if (current_ratio > prev_ratio) {
                    prev_ratio = current_ratio;
                    System.out.println("Progress: " + current_ratio + "%");

                    dataFile = DataFile.findById(fileId);
                    if (dataFile != null) {
                        if (dataFile.getStatus() == DataFile.DELETED) {
                            dataFile.delete();

                            return;
                        }
                        dataFile.setCompletionPercentage(current_ratio);
                        dataFile.save();
                    } else {
                        return;
                    }
                }
                i++;
            }
            
            //timeAlignment.printAlignment();
            
            // Write headers: Labels are derived from collected alignment attributes
            List<TimeVariable> aaList = timeAlignment.getAlignmentAttributes();
            aaList.sort(new Comparator<TimeVariable>() {
                @Override
                public int compare(TimeVariable o1, TimeVariable o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            //System.out.println("aligned attributes size: " + aaList.size());
            FileUtils.writeStringToFile(file, "\"Timestamp\"", "utf-8", true);
            for (TimeVariable aa : aaList) {
            	FileUtils.writeStringToFile(file, ",\"" + aa + "\"", "utf-8", true);
            }
            FileUtils.writeStringToFile(file, "\n", "utf-8", true);

            // Sort collected objects by their original ID
            //List<String> timestamps = alignment.getTimestamps();
            Collections.sort(tss);
            //System.out.println("Align-Debug: timestamps size: " + tss.size());

            // Write rows: traverse collected object. From these objects, traverse alignment objects
            for (String ts : tss) {
                if (results.containsKey(ts)) {
                    //System.out.println("Align-Debug: WRITING: timestamp = " + ts);
                	FileUtils.writeStringToFile(file, "\"" + ts + "\"", "utf-8", true);
                    Map<String, String> row = results.get(ts);
                    for (TimeVariable aa : aaList) {
                        //System.out.println("Align-Debug: WRITING: variable = " + aa + "  value = " + row.get(aa.toString()));
                    	FileUtils.writeStringToFile(file, ",\"" + row.get(aa.toString()) + "\"", "utf-8", true);
                    }
                    FileUtils.writeStringToFile(file, "\n", "utf-8", true);
                }
            }

            System.out.println("Finished writing!");

            dataFile = DataFile.findById(fileId);
            if (dataFile != null) {

            	/*
            	// Write harmonized code book
            	if (categoricalOption.equals(WITH_CODE_BOOK)) {
            		outputHarmonizedCodebook(timeAlignment, file, dataFile.getOwnerEmail());
            	}*/
		
                if (dataFile.getStatus() == DataFile.DELETED) {
                    dataFile.delete();
                    return;
                }
                dataFile.setCompletionPercentage(100);
                dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                dataFile.setStatus(DataFile.CREATED);
                dataFile.save();
             
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

