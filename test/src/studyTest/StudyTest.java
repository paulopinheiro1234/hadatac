package src.studyTest;

import org.apache.solr.client.solrj.SolrQuery;
import org.hadatac.console.controllers.dataacquisitionsearch.Downloader;
import org.hadatac.data.loader.CSVRecordFile;
import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.entity.pojo.ColumnMapping;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.SPARQLUtilsFacetSearch;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import scala.io.Source;
import src.multiStudyTest.MultiStudyTest;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StudyTest {

    final double epsilon = 0.000001d;
    final Integer MAX_DA_FILES = 1000;

    Map<String, Object> testDescriptions = new HashMap<>();
    int totalDownloads = 0;

    // these should be replaced by a config file
    String ownerUri = "http://hadatac.org/kb/hhear#PER-YU";
    String ownerEmail = "liyang.yu@mssm.edu";
    String categoricalOption = "withCodeBook";

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Before
    public void initialize()  {
        SPARQLUtilsFacetSearch.createInMemoryModel();
    }

    @After
    public void tearDown() {
        System.out.println("\n\n------ FINAL REPORT ------\n");
        System.out.println("=> total number of downloaded DA/Lab files that have been tested: " + totalDownloads + "\n\n");
    }

    @Test
    public void testAll() {

        /*
        here is how to run:
        sbt "testOnly *StudyTest -- -DdataFileName=DA-2016-34-Lab-Metals"
        sbt "testOnly *StudyTest -- -DstudyID=2016-34"
        sbt "testOnly *StudyTest"
        sbt "testOnly *StudyTest -- -Dtarget=multiStudy"
        reference: https://stackoverflow.com/questions/37978961/passing-command-line-argument-to-sbt-test
        */

        String target = System.getProperty("target");
        if ( target != null && target.equalsIgnoreCase("multiStudy") ) {
            System.out.println("Tests will be done for multi-study cases.");
            MultiStudyTest multiStudyTest = new MultiStudyTest(collector);
            multiStudyTest.retrieveTestConfig();
            multiStudyTest.multiStudyTest();
            return;
        }

        System.out.println("Tests will be done for single study cases.");
        createTestCases();

        String dataFileName = System.getProperty("dataFileName");
        if  ( dataFileName == null || dataFileName.length() == 0 ) System.out.println("NO datafile is provided.");
        else System.out.println("provided data file = " + dataFileName);

        String studyID = System.getProperty("studyID");
        if  ( studyID == null || studyID.length() == 0 ) System.out.println("NO studyID is provided.");
        else System.out.println("provided studyID = " + studyID);

        for ( Map.Entry<String, Object> testEntry : testDescriptions.entrySet() ) {

            String test = testEntry.getKey();
            Map<String, Object> testDetails = (Map<String, Object>)testEntry.getValue();
            String facets = (String)testDetails.get("facets");

            if ( dataFileName != null && dataFileName.length() > 0 ) {
                if ( test.contains(dataFileName) == false ) continue;
            }

            if ( studyID != null && studyID.length() > 0 ) {
                if ( test.contains(studyID) == false ) continue;
            }

            // debug
            // if ( test.contains("DA-2018-2273-PD-DemoHealth") == false ) continue;
            // end of debug

            // execute the main codebase to download the data file
            System.out.println("\n\n\n====> working on " + test);
            ColumnMapping columnMapping = new ColumnMapping();
            //Downloader.generateCSVFileBySubjectAlignment(ownerUri, facets, ownerEmail, Measurement.SUMMARY_TYPE_NONE, categoricalOption, true, columnMapping);
            Downloader.generateCSVFileBySubjectAlignment(ownerUri, facets, ownerEmail, categoricalOption, true, columnMapping);
            testDetails.put("columnMapping", columnMapping);
            System.out.println("-------------------------------------------");
            System.out.println(columnMapping.toString());

            // get the downloaded data file from hard drive and make some initial check
            File file = getDownloadedFile(ownerEmail, "object_alignment");
            assertTrue("cannot find the downloaded file for " + test + ".", file != null);
            System.out.println("downloaded file: " + file.getAbsolutePath());

            // compare the downloaded file to the original DA file
            if ( test.toUpperCase().contains("PD") ) check(file.getAbsolutePath(), test, testDetails);

            // for the Lab files
            if ( test.toUpperCase().contains("LAB") ) checkLabFile(file.getAbsolutePath(), test, testDetails);

            totalDownloads ++;
        }

    }

    private void checkLabFile(String downloadFilePath, String testURL, Map<String, Object> testDetails) {

        ColumnMapping columnMapping = (ColumnMapping) testDetails.get("columnMapping");
        List<Record> recordsDownloaded = null;
        List<Record> recordsOriginal = null;
        RecordFile recordFile = null;

        // retrieve all the original records in the DA file
        recordFile = new CSVRecordFile(new File((String)testDetails.get("originalDApath")));
        recordsOriginal = recordFile.getRecords();
        List<String> originalHeaders = recordFile.getHeaders();

        // retrieve all the downloaded records
        recordFile = new CSVRecordFile(new File(downloadFilePath));
        recordsDownloaded = recordFile.getRecords();

        List<Double> original = new ArrayList<>();
        List<Double> downloaded = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        for ( String header : recordFile.getHeaders() ) {

            boolean isCategorical = false;
            List<String> originalColumnNames = columnMapping.getMappings().get(header + "|false");
            if (originalColumnNames == null) {
                originalColumnNames = columnMapping.getMappings().get(header + "|true");
                isCategorical = true;
            }
            if (originalColumnNames == null || originalColumnNames.size() == 0) continue;

            standardizedColumnNames(originalHeaders, originalColumnNames);
            String originalColumnName = originalColumnNames.get(0);  // assume this is a single mapping at this point
            if (visited.contains(originalColumnName)) continue;
            visited.add(originalColumnName);

            isCategorical = false;
            List<String> downloadedColumnNames = columnMapping.getMappings().get(originalColumnName + "|false");
            if (downloadedColumnNames == null) {
                downloadedColumnNames = columnMapping.getMappings().get(originalColumnName + "|true");
                isCategorical = true;
            }
            if (downloadedColumnNames == null || downloadedColumnNames.size() == 0) continue;

            columnMapping.getMappings().remove(isCategorical == true ? originalColumnName + "|true" : originalColumnName + "false");

            // collect all observations for this column from original DA
            original.clear();
            for (Record record : recordsOriginal) {
                String value = record.getValueByColumnName(originalColumnName);
                if (value == null) continue;
                value = value.trim();
                if (value.length() != 0 && !value.equalsIgnoreCase("NA")) {
                    try {
                        if (value.contains(",")) value = value.replaceAll(",", "");
                        original.add(Double.valueOf(value));
                    } catch (Exception e) {
                        System.out.println("-----------!!!! encounter abnormal value: [" + value + "], for " + testURL + ", in Concentration column.");
                    }
                }
            }

            // collect all observations in the harmonized dataset for this originalColumnName
            downloaded.clear();
            for (Record record : recordsDownloaded) {
                for (String downloadedColumnName : downloadedColumnNames) {
                    String value = record.getValueByColumnName(downloadedColumnName);
                    if (value == null) continue;
                    value = value.trim();
                    if (value.length() != 0 && !value.equalsIgnoreCase("NA")) {
                        String[] items = value.split("\\s+");
                        for ( String item : items ) {
                            try {
                                downloaded.add(Double.valueOf(item));
                            } catch (Exception e) {
                                System.out.println("-----------!!!! encounter abnormal value: [" + item + "], for " + testURL + ", column: " + downloadedColumnName);
                            }
                        }
                    }
                }
            }

            try {
                assertEquals("[" + testURL + ": original Lab file has " + original.size() + originalColumnName +
                                " values, downloaded lab file has " + downloaded.size() + " corresponding observations.",
                        original.size(), downloaded.size(), 0);
            } catch (AssertionError e) {
                System.out.println(e.getMessage());
                collector.addError(e);
            }

            double meanOriginal = getMean(original), meanDownloaded = getMean(downloaded);
            double stddevOriginal = getStandardDev(original), stddevDownloaded = getStandardDev(downloaded);

            System.out.println("[" + testURL + "]: mean/stddev for downloaded " + originalColumnName + " values: mean = " + meanDownloaded + ", stddev = " + stddevDownloaded);
            System.out.println("[" + testURL + "]: mean/stddev for original " + originalColumnName + " values: mean = " + meanOriginal + ", stddev = " + stddevOriginal);

            try {
                assertEquals("[" + testURL + "]: unmatched mean for the concentration observations. ",
                        meanOriginal, meanDownloaded, epsilon);
            } catch (AssertionError e) {
                System.out.println(e.getMessage());
                collector.addError(e);
            }

            try {
                assertEquals("[" + testURL + "]: unmatched stddev for the concentration observations. ",
                        stddevOriginal, stddevDownloaded, epsilon);
            } catch (AssertionError e) {
                System.out.println(e.getMessage());
                collector.addError(e);
            }

        }

    }

    private void check(String downloadFilePath, String testURL, Map<String, Object> testDetails) {

        ColumnMapping columnMapping = (ColumnMapping) testDetails.get("columnMapping");
        List<Record> recordsDownloaded = null;
        List<Record> recordsOriginal = null;
        RecordFile recordFile = null;

        // retrieve all the original records in the DA file
        recordFile = new CSVRecordFile(new File((String)testDetails.get("originalDApath")));
        recordsOriginal = recordFile.getRecords();
        List<String> originalHeaders = recordFile.getHeaders();

        // retrieve all the downloaded records
        recordFile = new CSVRecordFile(new File(downloadFilePath));
        recordsDownloaded = recordFile.getRecords();

        for ( String header : recordFile.getHeaders() ) {

            boolean isCategorical = false;
            List<String> originalColumnNames = columnMapping.getMappings().get(header + "|false");
            if ( originalColumnNames == null ) {
                originalColumnNames = columnMapping.getMappings().get(header + "|true");
                isCategorical = true;
            }
            if ( originalColumnNames == null || originalColumnNames.size() == 0 ) continue;

            standardizedColumnNames(originalHeaders, originalColumnNames);
            if ( isCategorical == false )
                checkNonCategoricalColumn(testURL, recordsOriginal, recordsDownloaded, originalColumnNames, header);
            else checkCategoricalColumn(testURL, recordsOriginal, recordsDownloaded, originalColumnNames, header);

        }

    }

    private void checkNonCategoricalColumn(String testURL, List<Record> recordsOriginal, List<Record> recordsDownloaded,
                                           List<String> originalColumnNames, String downloadedColumnName) {

        List<Double> original = new ArrayList<>();
        String originalColumnHeader = "";
        for ( String originalColumnName : originalColumnNames ) {
            originalColumnHeader += originalColumnName + " ";
            for (Record record : recordsOriginal) {
                String value = record.getValueByColumnName(originalColumnName);
                if (value != null) value = value.trim();
                if (value != null && value.length() != 0 && !value.equalsIgnoreCase("NA")) {
                    try {
                        original.add(Double.valueOf(value));
                    } catch (Exception e) {
                        System.out.println("-----------!!!! encounter abnormal value: [" + value + "], for " + testURL + ", column: " + originalColumnName);
                    }
                }
                // else original.add(0.0);
            }
        }

        List<Double> downloaded = new ArrayList<>();
        for ( Record record : recordsDownloaded ) {
            String value = record.getValueByColumnName(downloadedColumnName);
            if ( value != null ) value = value.trim();
            if ( value != null && value.length() != 0 && !value.equalsIgnoreCase("NA")) {
                String[] items = value.split("\\s+");
                for ( String item : items ) {
                    try {
                        downloaded.add(Double.valueOf(item));
                    } catch (Exception e) {
                        System.out.println("-----------!!!! encounter abnormal value: [" + item + "], for " + testURL + ", culumn: " + downloadedColumnName);
                    }
                }
            }
            // else downloaded.add(0.0);
        }

        double meanOriginal = getMean(original), meanDownloaded = getMean(downloaded);
        double stddevOriginal = getStandardDev(original), stddevDownloaded = getStandardDev(downloaded);

        System.out.println("[" + downloadedColumnName + ", " + originalColumnHeader + "] m/s = "
                + meanDownloaded + "/" + stddevDownloaded + ", m/s = " + meanOriginal + "/" + stddevOriginal);

        try {
            assertEquals("[" + testURL + ": " + downloadedColumnName + " => " + originalColumnHeader + "], unmatched mean: ",
                    meanOriginal, meanDownloaded, epsilon);
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
            collector.addError(e);
        }

        try {
            assertEquals("[" + testURL + ": " + downloadedColumnName + " => " + originalColumnHeader + "], unmatched stddev:  ",
                    stddevOriginal, stddevDownloaded, epsilon);
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
            collector.addError(e);
        }

    }

    private void checkCategoricalColumn(String testURL, List<Record> recordsOriginal, List<Record> recordsDownloaded,
                                        List<String> originalColumnNames, String downloadedColumnName) {

        if ( originalColumnNames.size() > 1 ) {
            checkCompositeCategoricalColumn(testURL, recordsOriginal, recordsDownloaded, originalColumnNames, downloadedColumnName);
        } else checkSingleCategoricalColumn(testURL, recordsOriginal, recordsDownloaded, originalColumnNames, downloadedColumnName);

    }

    private void checkCompositeCategoricalColumn(String testURL, List<Record> recordsOriginal, List<Record> recordsDownloaded,
                                              List<String> originalColumnNames, String downloadedColumnName) {

        StringBuilder originalColumnHeader = new StringBuilder();
        for ( String originalColumnName : originalColumnNames ) originalColumnHeader.append(originalColumnName).append(",");
        originalColumnHeader.setLength(originalColumnHeader.length()-1);

        Map<String, Integer> originalFreq = new HashMap<>();
        for ( Record record : recordsOriginal ) {
            for ( String column : originalColumnNames ) {
                String value = record.getValueByColumnName(column);
                if (value == null || value.length() == 0 ) continue;
                value = value.trim();
                if ( value.equalsIgnoreCase("NA")) continue;
                String tmp = column + "-" + value;
                originalFreq.put(tmp, originalFreq.getOrDefault(tmp, 0) + 1);
            }
        };

        Map<String, Integer> downloadedFreq = new HashMap<>();
        for ( Record record : recordsDownloaded ) {
            String value = record.getValueByColumnName(downloadedColumnName);
            if (value == null || value.length() == 0 ) continue;
            value = value.trim();
            String[] items = value.split("\\s+");
            for (int i = 0; i < items.length; i++ ) {
                if ( items[i].equalsIgnoreCase("NA") ) continue;
                String tmp = "" + i + "-" + items[i];
                downloadedFreq.put(tmp, downloadedFreq.getOrDefault(tmp, 0) + 1);
            }
        }

        if ( originalFreq.size() != downloadedFreq.size() ) {
            // merge the ones in downloadedFreq
            Map<String, Integer> tmp = new HashMap<>();
            for ( Map.Entry<String, Integer> entry : downloadedFreq.entrySet() ) {
                String key = entry.getKey();
                Integer freq = entry.getValue();
                key = key.substring(key.indexOf("-")+1);
                tmp.put(key, tmp.getOrDefault(key,0)+freq);
            }
            downloadedFreq.clear();
            downloadedFreq.putAll(tmp);
        }

        Map<Integer, Integer> freqCount = new HashMap<>();
        for ( int f : originalFreq.values() ) freqCount.put(f, freqCount.getOrDefault(f, 0)+1);

        boolean matched = true;
        for ( Map.Entry<String, Integer> entry : downloadedFreq.entrySet() ) {
            if ( freqCount.containsKey(entry.getValue()) == false ) {
                // System.out.println("[" + testURL + "] unmatched categorical variable: [" + originalColumnHeader.toString() + "] => [" + downloadedColumnName + "]");
                try {
                    throw new AssertionError("[" + testURL + "] unmatched categorical variable frequency: [" +
                            originalColumnHeader.toString() + "] => [" + downloadedColumnName + "]");
                } catch (AssertionError e) {
                    System.out.println(e.getMessage());
                    collector.addError(e);
                    matched = false;
                }
            } else {
                int xf = freqCount.get(entry.getValue());
                if ( xf == 1 ) freqCount.remove(entry.getValue());
                else freqCount.put(entry.getValue(), xf-1);
            }
        }
        if ( matched) System.out.println("[" + testURL + "] " + downloadedColumnName + ", " +
                originalColumnHeader + "] categorical value frequency matched.");
    }

    private void checkSingleCategoricalColumn(String testURL, List<Record> recordsOriginal, List<Record> recordsDownloaded,
                                           List<String> originalColumnNames, String downloadedColumnName) {

        String originalColumnName = originalColumnNames.get(0);
        List<String> original = new ArrayList<>();
        for ( Record record : recordsOriginal ) {
            String value = record.getValueByColumnName(originalColumnName);
            if ( value != null ) value = value.trim();
            if ( value != null && value.length() != 0 && !value.equalsIgnoreCase("NA")) original.add(value);
        }

        List<String> downloaded = new ArrayList<>();
        for ( Record record : recordsDownloaded ) {
            String value = record.getValueByColumnName(downloadedColumnName);
            if (value == null || value.length() == 0 ) continue;
            value = value.trim();
            if ( value.equalsIgnoreCase("NA") ) continue;
            downloaded.add(value);
            // else downloaded.add(0);
        }

        try {
            assertEquals("[" + testURL + ": " + originalColumnName + " and " + downloadedColumnName + "] should have the same number of observations: ",
                    original.size(), downloaded.size());
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
            collector.addError(e);
        }

        Map<String, Integer> originalFreq = new HashMap<>();
        for ( String value : original ) originalFreq.put(value, originalFreq.getOrDefault(value, 0)+1);
        Map<Integer, Integer> freqCount = new HashMap<>();
        for ( int f : originalFreq.values() ) freqCount.put(f, freqCount.getOrDefault(f, 0)+1);

        Map<String, Integer> downloadedFreq = new HashMap<>();
        for ( String f : downloaded ) downloadedFreq.put(f, downloadedFreq.getOrDefault(f, 0)+1);

        boolean matched = true;
        for ( Map.Entry<String, Integer> entry : downloadedFreq.entrySet() ) {
            if ( freqCount.containsKey(entry.getValue()) == false ) {
                //System.out.println("ERROR: unmatched categorical variable frequency found for [" + originalColumnName + "] => [" + downloadedColumnName + "]");
                try {
                    throw new AssertionError("[" + testURL + "] unmatched categorical variable frequency [" +
                            originalColumnName + "] => [" + downloadedColumnName + "]");
                } catch (AssertionError e) {
                    System.out.println(e.getMessage());
                    collector.addError(e);
                    matched = false;
                }
            } else {
                int xf = freqCount.get(entry.getValue());
                if ( xf == 1 ) freqCount.remove(entry.getValue());
                else freqCount.put(entry.getValue(), xf-1);
            }
        }
        if ( matched) System.out.println("[" + testURL + "] " + downloadedColumnName + ", " +
                originalColumnName + "] categorical value frequency matched.");
    }

    private double getStandardDev(List<Double> nums) {
        double mean = getMean(nums);
        double temp = 0;

        for (int i = 0; i < nums.size(); i++) {
            double x = nums.get(i);
            double squrDiffToMean = Math.pow(x-mean, 2);
            temp += squrDiffToMean;
        }
        double meanOfDiffs = temp / (double) (nums.size());
        return round(Math.sqrt(meanOfDiffs),4);
    }

    private double getMean(List<Double> nums) {
        double sum = 0;
        for ( double x : nums ) sum += x;
        return round(sum/(double)nums.size(), 4);
    }

    private File getDownloadedFile(String ownerEmail, String fileName) {
        String path = ConfigProp.getPathWorking() + "/download/" + ownerEmail;
        File directory = new File(path);
        long lastModifiedTime = Long.MIN_VALUE;
        File chosenFile = null;

        File[] files = directory.listFiles(File::isFile);
        if ( files != null ) {
            for ( File file : files ) {
                if ( file.lastModified() > lastModifiedTime && file.getName().contains(fileName) ) {
                    chosenFile = file;
                    lastModifiedTime = file.lastModified();
                }
            }
        }

        return chosenFile;
    }

    public void createTestCases()  {

        // query csv dataset to get all the DA files
        SolrQuery query = new SolrQuery();
        query.setQuery("*:*");
        query.setQuery("status_str:\"" + DataFile.PROCESSED + "\"");
        query.setRows(MAX_DA_FILES);
        List<DataFile> files = DataFile.findByQuery(query);

        // create map for test cases
        testDescriptions.clear();
        for ( DataFile dataFile : files ) {

            String originalFilePath = dataFile.getAbsolutePath();
            if ( originalFilePath == null ) continue;
            if ( originalFilePath.toUpperCase().contains("SDD") ) continue;
            if ( !originalFilePath.toUpperCase().contains("DA") && !originalFilePath.toUpperCase().contains("LAB") ) continue;
            if ( checkFileExists(originalFilePath) == false ) {
                System.out.println("===> ingested DA File [" + originalFilePath +"] does not exists, skip.");
                continue;
            }

            Map<String, Object> testDescription = new HashMap<>();
            String dataAcquisitionName = getDAFileName(dataFile.getDataAcquisitionUri());
            if ( dataAcquisitionName == null ) continue;

            testDescription.put("acquisition_uri_str", dataAcquisitionName);
            testDescription.put("facets",getSearchFacet(dataFile.getStudyUri(), dataAcquisitionName));
            testDescription.put("originalDApath", dataFile.getAbsolutePath());

            testDescriptions.put(dataAcquisitionName, testDescription);
        }

        return;
    }

    private void standardizedColumnNames(List<String> headers, List<String> columns) {

        if ( columns == null || columns.size() == 0 ) return;
        if ( headers == null || headers.size() == 0 ) return;

        Map<String, String> map = new HashMap<>();
        for ( String header : headers ) map.put(header.toLowerCase(), header);
        for ( int i = 0; i < columns.size(); i++ ) {
            if ( map.containsKey(columns.get(i).toLowerCase()) ) {
                columns.set(i, map.get(columns.get(i).toLowerCase()));
            }
        }
    }

    private boolean checkFileExists(String path) {
        File f = new File(path);
        if (f.exists() && !f.isDirectory()) return true;
        return false;
    }

    private String getSearchFacet(String studyUriStr, String acquisitionUriStr) {

        JSONArray facetsEC = new JSONArray();
        JSONArray facetsS = new JSONArray();
        JSONArray facetsOC = new JSONArray();
        JSONArray facetsU = new JSONArray();
        JSONArray facetsT = new JSONArray();
        JSONArray facestPI = new JSONArray();

        JSONArray facetsSChildArray = new JSONArray();
        JSONObject facetsSChild = new JSONObject();
        facetsSChild.put("id", acquisitionUriStr);
        facetsSChild.put("acquisition_uri_str", acquisitionUriStr);
        facetsSChildArray.add(facetsSChild);

        JSONObject facetsSObj = new JSONObject();
        facetsSObj.put("id", studyUriStr);
        facetsSObj.put("study_uri_str", studyUriStr);
        facetsSObj.put("children", facetsSChildArray);
        facetsS.add(facetsSObj);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("facetsEC",facetsEC);
        jsonObject.put("facetsS", facetsS);
        jsonObject.put("facetsOC", facetsOC);
        jsonObject.put("facetsU", facetsU);
        jsonObject.put("facetsT", facetsT);
        jsonObject.put("facetsPI", facestPI);

        //JSONObject top = new JSONObject();
        //top.put("facets", jsonObject);

        return jsonObject.toJSONString();
    }

    private String getDAFileName(String acquisitionURL) {
        if ( acquisitionURL == null || acquisitionURL.length() == 0 ) return null;
        if ( acquisitionURL.indexOf(":") < 0 ) return acquisitionURL;
        String[] names = acquisitionURL.split(":");
        return NameSpaces.getInstance().getNameByAbbreviation(names[0])+names[1];
    }

    private String writeToTestConfig(Map<String, Object> testDescriptions) {
        JSONArray tests = new JSONArray();
        for ( Map.Entry<String, Object> entry : testDescriptions.entrySet() ) {
            JSONObject test = new JSONObject();
            Object value = entry.getValue();
            if ( value instanceof String )
                test.put(entry.getKey(), (String)value);
            else
                test.put(entry.getKey(), writeToTestConfig((Map<String,Object>)entry.getValue()));
            tests.add(test);
        }
        return tests.toJSONString();
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}


/*
https://stackoverflow.com/questions/12762969/resource-directory-for-tests-in-a-play-application
https://stackoverflow.com/questions/52268681/how-to-read-resources-in-test-code-with-play-framework
 */