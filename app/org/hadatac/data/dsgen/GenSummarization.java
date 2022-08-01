package org.hadatac.data.dsgen;

import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.hadatac.entity.pojo.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

public class GenSummarization {

    public static void exec(String ownerUri, String facets, File file, String fileId,
                                                     String summaryType, String categoricalOption,
                                                     ColumnMapping columnMapping) {

        System.out.println("outputAsCSVBySummarization: facets=[" + facets + "]");
        boolean keepSameValue = true;

        // Initiate Alignment
        Alignment alignment = new Alignment();
        Map<String, List<String>> alignCache = new HashMap<String, List<String>>();
        Map<String, List<String>> studyMap = new HashMap<>();

        // read the page size from config
        int pageSize = 32000;
        try {
            String sPageSize = ConfigFactory.load().getString("hadatac.download.pageSize");
            pageSize = Integer.parseInt(sPageSize);
        } catch(Exception e) {
            e.printStackTrace();
        };

        // Initiate Summary
        // First key (string): variable; second key (string): value; value of the inner map (Value): annotated value including frequency
        Map<String, Map<String, AnnotatedValueSummary>> summary = new HashMap<String, Map<String, AnnotatedValueSummary>>();
        Map<String, AnnotatedValueSummary> valuesSummary;

        // read backend Solr page by page and merge the results
        System.out.println("SummaryType [" + summaryType + "]");
        System.out.println("CategoricalOption [" + categoricalOption + "]");
        System.out.println("start the pagination process...pageSize = " + pageSize);
        Map<String, Map<String, List<AnnotatedValue>>> results = SolrReader.readSolrPagesAndMerge(ownerUri, facets, fileId, pageSize, studyMap, alignment,alignCache, categoricalOption, keepSameValue, columnMapping);
        System.out.println("ended the pagination process.");


        // write the results to hard drive
        System.out.print("start to write the dataset to hard drive...");
        DataFile dataFile = null;

        try {
            // Write empty string to create the file
            FileUtils.writeStringToFile(file, "", "utf-8", true);

            // Initiate Results
            //     HashMap<base object, Map<measurement's key, List<value>>, where
            //        - base object is the object to be aligned. For example, if the alignment is a subject and the current object of the measurement is a sample
            //          from the subject, the base object is the subject of the sample
            List<String> values = null;
            boolean processOriginalID = false;
            boolean fileCreated = false;

            // Prepare rows: Measurements are compared against alignment attributes from previous measurements (role/entity/attribute-list/inrelationto/unit/time)
            //               New alignment attributes are created for measurements with no corresponding alignment attributes.

            //alignment.printAlignment();

            // Write headers: Labels are derived from collected alignment attributes
            List<VariableSpec> aaList = alignment.getVariables();
            System.out.println("Sorting variable list... " + aaList.size() + " items");
            aaList.sort(new Comparator<VariableSpec>() {
                @Override
                public int compare(VariableSpec o1, VariableSpec o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            // Sort collected objects by their original ID
            //List<StudyObject> objects = alignment.getObjects();
            //objects.sort(new Comparator<StudyObject>() {
            //    @Override
            //    public int compare(StudyObject o1, StudyObject o2) {
            //        return o1.getOriginalId().compareTo(o2.getOriginalId());
            //}
            //});

            System.out.println("Compute summarization into a map of maps from " + results.size() + " measurements");
            for (String objectId : results.keySet()) {

                //update variables of a given objectId
                Map<String, List<AnnotatedValue>> variable = results.get(objectId);
                for (String variableId : variable.keySet()) {
                    if (!variableId.endsWith("-ID")) {

                        // retrieve/create value summary for current variable
                        if (summary.containsKey(variableId)) {
                            valuesSummary = summary.get(variableId);
                        } else {
                            valuesSummary = new HashMap<String, AnnotatedValueSummary>();
                        }

                        // update values of a given variable
                        for (AnnotatedValue value : variable.get(variableId)) {
                            AnnotatedValueSummary vs;
                            if (valuesSummary.containsKey(value.getValue())) {
                                vs = valuesSummary.get(value.getValue());
                                vs.setFrequency(vs.getFrequency() + 1);
                                valuesSummary.put(value.getValue(), vs);
                            } else {
                                vs = new AnnotatedValueSummary(value);
                                vs.setFrequency(1);
                                valuesSummary.put(vs.getAnnotatedValue().getValue(), vs);
                            }
                        }
                        summary.put(variableId, valuesSummary);
                    }
                }
            }

            if (categoricalOption.equals(GenConst.NON_CATG_CATG)) {
                summary = AnnotatedValueSummary.categorizeNonCategorical(alignment, aaList, summary);
            }

            Map<String, AnnotatedValueSummary> valuesResp;
            //System.out.println("Phase II: variable list size: " + aaList.size());
            System.out.println("Printing map of maps... ");
            FileUtils.writeStringToFile(file, "\"Variable\",\"Code\",\"Code Label\",\"Code Class\",\"Frequency\"\n", "utf-8", true);
            for (VariableSpec aa : aaList) {
                String varStr = aa.toString();
                if (summary.get(varStr) != null) {
                    valuesResp = summary.get(varStr);
                    for (AnnotatedValueSummary vs : valuesResp.values()) {
                        String valueClass = "";
                        String codeLabel = "";
                        if (vs.getAnnotatedValue().getValueClass() != null && vs.getAnnotatedValue().getValueClass().startsWith("http")) {
                            valueClass = vs.getAnnotatedValue().getValueClass();
                            codeLabel =  GenCodeBook.prettyCodeBookLabel(alignment, valueClass);
                        }
                        if ((categoricalOption.equals(GenConst.NON_CATG_IGNORE) &&
                                vs.getAnnotatedValue().getValueClass() != null &&
                                vs.getAnnotatedValue().getValueClass().startsWith("http")) ||
                                (!categoricalOption.equals(GenConst.NON_CATG_IGNORE))) {
                            if (vs != null) {
                                FileUtils.writeStringToFile(file, "\"" + varStr + "\",\"" + vs.getAnnotatedValue().getValue() +
                                        "\",\"" + codeLabel +
                                        "\",\"" + valueClass +
                                        "\"," + vs.getFrequency() + "\n", "utf-8", true);
                            }
                        }
                    }
                }


            }
            //System.out.println("");
            //FileUtils.writeStringToFile(file, "\n", "utf-8", true);

            System.out.println("Finished writing!");

            dataFile = DataFile.findById(fileId);
            if (dataFile != null) {

                if (dataFile.getStatus() == DataFile.DELETED) {
                    dataFile.delete();
                    return;
                }

                // finalize the main download file
                dataFile.setCompletionPercentage(100);
                dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                dataFile.setStatus(DataFile.CREATED);
                dataFile.save();
                fileCreated = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

