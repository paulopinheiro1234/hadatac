package org.hadatac.data.dsgen;

import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.hadatac.entity.pojo.*;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GenConceptAlignment {

    public static void exec(String ownerUri, String facets, File file, String fileId,
                                                     String summaryType, String categoricalOption,
                                                     boolean keepSameValue, ColumnMapping columnMapping) {

        //System.out.println("outputAsCSVBySubjectAlignment: facets=[" + facets + "]");

        // Initiate Alignment
        Alignment alignment = new Alignment();
        Map<String, List<String>> alignCache = new HashMap<String, List<String>>();
        Map<String, List<String>> studyMap = new HashMap<>();

        // read the page size from config
        String sPageSize = ConfigFactory.load().getString("hadatac.download.pageSize");
        int pageSize = 32000;
        try {
            pageSize = Integer.parseInt(sPageSize);
        } catch(Exception e) {};

        // read backend Solr page by page and merge the results
        System.out.println("start the pagination process...pageSize = " + pageSize);
        Map<String, Map<String, List<AnnotatedValue>>> results = SolrReader.readSolrPagesAndMerge(ownerUri, facets, fileId, pageSize, studyMap, alignment,alignCache, categoricalOption, keepSameValue, columnMapping);

        Map<String, AnnotatedGroupSummary> groupSummaryMap = new HashMap<String, AnnotatedGroupSummary>();

        // write the results to hard drive
        //System.out.print("start to write the dataset to hard drive...");
        DataFile dataFile = null;

        try {
            // Write empty string to create the file
            FileUtils.writeStringToFile(file, "", "utf-8", true);

            // Initiate Results
            //     HashMap<base object, Map<measurement's key, List<value>>, where
            //        - base object is the object to be aligned. For example, if the alignment is a subject and the current object of the measurement is a sample 
            //          from the subject, the base object is the subject of the sample  
            List<AnnotatedValue> values = null;
            boolean processOriginalID = false;
            boolean fileCreated = false;
            
            // Prepare rows: Measurements are compared against alignment attributes from previous measurements (role/entity/attribute-list/inrelationto/unit/time)
            //               New alignment attributes are created for measurements with no corresponding alignment attributes.

            //alignment.printAlignment();
            
            // Write headers: Labels are derived from collected alignment attributes
            List<VariableSpec> aaList = alignment.getVariables();
            Map<String, VariableSpec> varMap = new HashMap<String, VariableSpec>();
            aaList.sort(new Comparator<VariableSpec>() {
                @Override
                public int compare(VariableSpec o1, VariableSpec o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            //System.out.println("Phase II: variable list size: " + aaList.size());
            //System.out.print("Phase II: variable list content: ");
            if (summaryType.equals(GenConst.SUMMARY_TYPE_NONE)) {
                FileUtils.writeStringToFile(file, "\"STUDY-ID\"", "utf-8", true);
                for (VariableSpec aa : aaList) {
                    //System.out.print(aa + " ");
                    FileUtils.writeStringToFile(file, ",\"" + aa + "\"", "utf-8", true);
                }
                //System.out.println("");
                FileUtils.writeStringToFile(file, "\n", "utf-8", true);

            }
            for (VariableSpec aa : aaList) {
                varMap.put(aa.toString(), aa);
                //System.out.println("init varMap: [" + aa.toString() + "]");
            }

            // Sort collected objects by their original ID
            List<StudyObject> objects = alignment.getObjects();
            objects.sort(new Comparator<StudyObject>() {
                @Override
                public int compare(StudyObject o1, StudyObject o2) {
                    return o1.getOriginalId().compareTo(o2.getOriginalId());
                }
            });

            //System.out.println("Phase II: download objects size: " + objects.size());
            // Write rows: traverse collected object. From these objects, traverse alignment objects
            for (StudyObject obj : objects) {
                if (results.containsKey(obj.getUri())) {
                    Map<String, List<AnnotatedValue>> row = results.get(obj.getUri());

                    if (summaryType.equals(GenConst.SUMMARY_TYPE_NONE)) {

                        // write study id
                        FileUtils.writeStringToFile(file, "\"" + getRelatedStudies(studyMap, obj.getUri()) + "\"", "utf-8", true);
                        for (VariableSpec aa : aaList) {
                            values = row.get(aa.toString());
                            FileUtils.writeStringToFile(file, ",", "utf-8", true);
                            if (values == null) {
                                //System.out.println("[WARNING] Measurement: No values for variable [" + aa.toString() + "]  of object [" + obj.getUri() + "]");
                            } else {
                                for (AnnotatedValue val : values) {
                                    if (val != null && val.getValue() != null) {
                                        //FileUtils.writeStringToFile(file, "\"" + val + "\" ", "utf-8", true);
                                        FileUtils.writeStringToFile(file, val.getValue() + " ", "utf-8", true);
                                    }
                                }
                            }
                        }
                        FileUtils.writeStringToFile(file, "\n", "utf-8", true);

                    } else {

                        AnnotatedGroup currGroup = new AnnotatedGroup(row, varMap, alignment, categoricalOption);
                        //System.out.println("Current Group size: " + currGroup.getGroup().size());
                        //System.out.println("Current Group key: " + currGroup.getKey());
                        AnnotatedGroupSummary currGroupSummary;
                        if (!groupSummaryMap.containsKey(currGroup.getKey())) {
                             currGroupSummary = new AnnotatedGroupSummary(currGroup);
                        } else {
                            currGroupSummary = groupSummaryMap.get(currGroup.getKey());
                        }
                        currGroupSummary.increaseFrequency();
                        groupSummaryMap.put(currGroupSummary.getAnnotatedGroup().getKey(), currGroupSummary);

                    }
                }
            }

            // Phase III - Print group summary
            if (summaryType.equals(GenConst.SUMMARY_TYPE_SUBGROUP)) {

                // Sort collected objects by their original ID
                List<AnnotatedGroupSummary> ags = new ArrayList<AnnotatedGroupSummary>(groupSummaryMap.values());
                ags.sort(new Comparator<AnnotatedGroupSummary>() {
                    @Override
                    public int compare(AnnotatedGroupSummary a1,AnnotatedGroupSummary a2) {
                        return a1.getAnnotatedGroup().getKey().compareTo(a2.getAnnotatedGroup().getKey());
                    }
                });

                List<VariableSpec> finalVar = new ArrayList<VariableSpec>();
                for (VariableSpec var : aaList) {
                    if (categoricalOption.equals(GenConst.NON_CATG_CATG)) {
                        if (var.isCategorical() || CategorizedValue.isCategorizable(var)) {
                            finalVar.add(var);
                            FileUtils.writeStringToFile(file, "\"" + var + "\",", "utf-8", true);
                        }
                    } else {
                        if (var.isCategorical()) {
                            finalVar.add(var);
                            FileUtils.writeStringToFile(file, "\"" + var + "\",", "utf-8", true);
                        }
                    }
                }
                FileUtils.writeStringToFile(file, "Frequency \n", "utf-8", true);
                for (AnnotatedGroupSummary groupSummary : ags) {
                    for (VariableSpec var : finalVar) {
                        boolean firstValue = true;
                        FileUtils.writeStringToFile(file, "\"", "utf-8", true);
                        for (AnnotatedValue value : groupSummary.getAnnotatedGroup().getGroup()) {
                            if (value.getVariable().getKey().equals(var.getKey())) {
                                if (value == null || value.getValue() == null) {
                                    //System.out.println("[WARNING] Measurement: No values for variable [" + aa.toString() + "]  of object [" + obj.getUri() + "]");
                                } else {
                                    if (firstValue) {
                                        firstValue = false;
                                    } else {
                                        FileUtils.writeStringToFile(file, " | ", "utf-8", true);
                                    }
                                    FileUtils.writeStringToFile(file, value.getValue(), "utf-8", true);
                                }
                            }
                        }
                        FileUtils.writeStringToFile(file, "\", ", "utf-8", true);
                    }
                    FileUtils.writeStringToFile(file, groupSummary.getFrequency() + " \n", "utf-8", true);
                }
            }

            System.out.println("Finished writing!");

            dataFile = DataFile.findById(fileId);
            if (dataFile != null) {

                if (dataFile.getStatus() == DataFile.DELETED) {
                    dataFile.delete();
                    return;
                }

                // Write harmonized code book
                alignment.getCodeBook();
                if (categoricalOption.equals(GenConst.WITH_CODE_BOOK)) {
                    fileCreated = GenCodeBook.exec(alignment, file, dataFile.getOwnerEmail(), dataFile.getDir());
                }

                // Write DOI list of sources
                if (summaryType.equals(GenConst.SUMMARY_TYPE_NONE)) {
                    GenProvenance.exec(alignment, file, dataFile.getOwnerEmail(), dataFile.getDir());
                }

                // finalize the main download file
                dataFile.setCompletionPercentage(100);
                dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                dataFile.setStatus(DataFile.CREATED);
                dataFile.save();
                fileCreated = true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String getRelatedStudies(Map<String, List<String>> studyMap, String subject) {
        if ( studyMap == null || studyMap.size() == 0 ) return "";
        if ( subject == null || subject.length() == 0 ) return "";
        if ( studyMap.containsKey(subject) == false ) return "";
        String ans = studyMap.get(subject).toString();
        return ans.substring(1, ans.length()-1);
    }



}

