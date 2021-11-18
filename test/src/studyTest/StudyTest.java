package src.studyTest;

import org.apache.commons.io.IOUtils;
import org.hadatac.console.controllers.dataacquisitionsearch.Downloader;
import org.hadatac.data.loader.CSVRecordFile;
import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.entity.pojo.SPARQLUtilsFacetSearch;
import org.hadatac.utils.ConfigProp;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StudyTest {

    final double epsilon = 0.000001d;

    Map<String, Object> testDescriptions = new HashMap<>();
    String ownerUri = null;
    String ownerEmail = null;
    String categoricalOption = null;
    int totalDownloads = 0;

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Before
    public void initialize()  {
        SPARQLUtilsFacetSearch.createInMemoryModel();
        retrieveTestConfig();
    }

    @After
    public void tearDown() {
        System.out.println("\n\n------ FINAL REPORT ------\n");
        System.out.println("=> total number of downloaded DA files that have been tested: " + totalDownloads + "\n\n");
    }

    @Test
    public void testAll() {

        for ( Map.Entry<String, Object> testEntry : testDescriptions.entrySet() ) {

            String test = testEntry.getKey();
            Map<String, Object> testDetails = (Map<String, Object>)testEntry.getValue();
            String facets = (String)testDetails.get("facets");

            // execute the main codebase to download the data file
            Downloader.generateCSVFileBySubjectAlignment(ownerUri, facets, ownerEmail, categoricalOption, true);

            // get the downloaded data file from hard drive and make some initial check
            File file = getDownloadedFile(ownerEmail, "object_alignment");
            assertTrue("cannot find the downloaded file for " + test + ".", file != null);
            System.out.println("downloaded file: " + file.getAbsolutePath());

            // compare the downloaded file to the original DA file
            check(file.getAbsolutePath(), test, testDetails);

            totalDownloads ++;
        }

    }

    private void check(String downloadFilePath, String testURL, Map<String, Object> testDetails) {

        Map<String, Object> columnMappings = (Map<String, Object>)testDetails.get("columnMapping");
        List<Record> recordsDownloaded = null;
        List<Record> recordsOriginal = null;
        RecordFile recordFile = null;

        // retrieve all the original records in the DA file
        recordFile = new CSVRecordFile(new File((String)testDetails.get("originalDApath")));
        recordsOriginal = recordFile.getRecords();

        // retrieve all the downloaded records
        recordFile = new CSVRecordFile(new File(downloadFilePath));
        recordsDownloaded = recordFile.getRecords();

        /*
            check if the numbers of records are the same?
            and other few things... add those later
         */

        for (Map.Entry<String, Object> columnMapping : columnMappings.entrySet() ) {

            Column column = (Column)columnMapping.getValue();
            String downloadedColumnName = columnMapping.getKey();
            String originalColumnName = column.columnName;

            if ( column.categorical == false ) checkNonCategoricalColumn(testURL, recordsOriginal, recordsDownloaded, originalColumnName, downloadedColumnName);
            else checkCategoricalColumn(testURL, recordsOriginal, recordsDownloaded, originalColumnName, downloadedColumnName);
        }

    }

    private void checkNonCategoricalColumn(String testURL, List<Record> recordsOriginal, List<Record> recordsDownloaded,
                                           String originalColumnName, String downloadedColumnName) {
        List<Double> original = new ArrayList<>();
        for ( Record record : recordsOriginal ) {
            String value = record.getValueByColumnName(originalColumnName);
            if ( value != null ) value = value.trim();
            if ( value != null && value.length() != 0 && !value.equalsIgnoreCase("NA")) {
                try {
                    original.add(Double.valueOf(value));
                } catch (Exception e) {
                    System.out.println("-----------!!!! encounter abnormal value: [" + value + "], for " + testURL + ", culumn: " + originalColumnName);
                }
            }
            // else original.add(0.0);
        }

        List<Double> downloaded = new ArrayList<>();
        for ( Record record : recordsDownloaded ) {
            String value = record.getValueByColumnName(downloadedColumnName);
            if ( value != null ) value = value.trim();
            if ( value != null && value.length() != 0 && !value.equalsIgnoreCase("NA")) {
                try {
                    downloaded.add(Double.valueOf(value));
                } catch (Exception e) {
                    System.out.println("-----------!!!! encounter abnormal value: [" + value + "], for " + testURL + ", culumn: " + downloadedColumnName);
                }
            }
            // else downloaded.add(0.0);
        }

        double meanOriginal = getMean(original), meanDownloaded = getMean(downloaded);
        double stddevOriginal = getStandardDev(original), stddevDownloaded = getStandardDev(downloaded);

        System.out.println(downloadedColumnName + ": mean = " + meanDownloaded + ", stddev = " + stddevDownloaded);
        System.out.println(originalColumnName + ": mean = " + meanOriginal + ", stddev = " + stddevOriginal);

        try {
            assertEquals("[" + testURL + ": " + downloadedColumnName + " => " + originalColumnName + "], unmatched mean: ",
                    meanOriginal, meanDownloaded, epsilon);
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
            collector.addError(e);
        }

        try {
            assertEquals("[" + testURL + ": " + downloadedColumnName + " => " + originalColumnName + "], unmatched stddev:  ",
                    stddevOriginal, stddevDownloaded, epsilon);
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
            collector.addError(e);
        }

    }

    private void checkCategoricalColumn(String testURL, List<Record> recordsOriginal, List<Record> recordsDownloaded,
                                        String originalColumnName, String downloadedColumnName) {

        if ( originalColumnName.contains(" ") ) {
            checkCompositeCategoricalColumn(testURL, recordsOriginal, recordsDownloaded, originalColumnName, downloadedColumnName);
        } else checkSingleCategoricalColumn(testURL, recordsOriginal, recordsDownloaded, originalColumnName, downloadedColumnName);

    }

    private void checkCompositeCategoricalColumn(String testURL, List<Record> recordsOriginal, List<Record> recordsDownloaded,
                                              String originalColumnName, String downloadedColumnName) {

        String[] columns = originalColumnName.split(" ");
        Map<String, Integer> originalFreq = new HashMap<>();
        for ( Record record : recordsOriginal ) {
            for ( String column : columns ) {
                String value = record.getValueByColumnName(column);
                if (value == null || value.length() == 0 || value.equalsIgnoreCase("NA")) continue;
                String tmp = column + "-" + value.trim();
                originalFreq.put(tmp, originalFreq.getOrDefault(tmp, 0) + 1);
            }
        };

        Map<String, Integer> downloadedFreq = new HashMap<>();
        for ( Record record : recordsDownloaded ) {
            String value = record.getValueByColumnName(downloadedColumnName);
            if (value == null || value.length() == 0 || value.equalsIgnoreCase("NA")) continue;
            String[] items = value.split(" ");
            for (String item : items) downloadedFreq.put(item, downloadedFreq.getOrDefault(item, 0) + 1);
        }

        Map<Integer, Integer> freqCount = new HashMap<>();
        for ( int f : originalFreq.values() ) freqCount.put(f, freqCount.getOrDefault(f, 0)+1);

        boolean matched = true;
        for ( Map.Entry<String, Integer> entry : downloadedFreq.entrySet() ) {
            if ( freqCount.containsKey(entry.getValue()) == false ) {
                // System.out.println("ERROR: unmatched categorical variable frequency found for [" + entry.getKey() + "], freq = " + entry.getValue());
                try {
                    throw new AssertionError("unmatched categorical variable frequency found for [" + entry.getKey() + "], freq = " + entry.getValue());
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
        if ( matched) System.out.println("[" + downloadedColumnName + ", " + originalColumnName + "] categorical value frequency matched.");
    }

    private void checkSingleCategoricalColumn(String testURL, List<Record> recordsOriginal, List<Record> recordsDownloaded,
                                           String originalColumnName, String downloadedColumnName) {

        List<Integer> original = new ArrayList<>();
        for ( Record record : recordsOriginal ) {
            String value = record.getValueByColumnName(originalColumnName);
            if ( value != null ) value = value.trim();
            if ( value != null && value.length() != 0 && !value.equalsIgnoreCase("NA")) {
                try {
                    original.add(Integer.valueOf(value));
                } catch (Exception e) {
                    System.out.println("-----------!!!! encounter abnormal value: [" + value + "], for " + testURL+ ", culumn: " + originalColumnName);
                }
            }
            // else original.add(0);
        }

        List<Integer> downloaded = new ArrayList<>();
        for ( Record record : recordsDownloaded ) {
            String value = record.getValueByColumnName(downloadedColumnName);
            if ( value != null ) value = value.trim();
            if ( value != null && value.length() != 0 && !value.equalsIgnoreCase("NA") ) {
                try {
                    downloaded.add(Integer.valueOf(value));
                } catch (Exception e) {
                    System.out.println("-----------!!!! encounter abnormal value: [" + value + "], for " + testURL+ ", culumn: " + downloadedColumnName);
                }
            }
            // else downloaded.add(0);
        }

        //System.out.println("for categorical value [" + originalColumnName + "], its mapping column is [" + downloadedColumnName + "]:");
        //System.out.println(originalColumnName + " has " + original.size() + " observations, " + downloadedColumnName + " has " + downloaded.size() + " observations. ");
        try {
            assertEquals("[" + testURL + ": " + originalColumnName + " and " + downloadedColumnName + "] should have the same number of observations: ",
                    original.size(), downloaded.size());
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
            collector.addError(e);
        }

        Map<Integer, Integer> originalFreq = new HashMap<>();
        for ( int f : original ) originalFreq.put(f, originalFreq.getOrDefault(f, 0)+1);
        Map<Integer, Integer> freqCount = new HashMap<>();
        for ( int f : originalFreq.values() ) freqCount.put(f, freqCount.getOrDefault(f, 0)+1);

        Map<Integer, Integer> downloadedFreq = new HashMap<>();
        for ( int f : downloaded ) downloadedFreq.put(f, downloadedFreq.getOrDefault(f, 0)+1);

        boolean matched = true;
        for ( Map.Entry<Integer, Integer> entry : downloadedFreq.entrySet() ) {
            if ( freqCount.containsKey(entry.getValue()) == false ) {
                // System.out.println("ERROR: unmatched categorical variable frequency found for [" + entry.getKey() + "], freq = " + entry.getValue());
                try {
                    throw new AssertionError("unmatched categorical variable frequency found for [" + entry.getKey() + "], freq = " + entry.getValue());
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
        if ( matched) System.out.println("[" + downloadedColumnName + ", " + originalColumnName + "] categorical value frequency matched.");
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
        float sum = 0;
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

    private void retrieveTestConfig() {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("testConfig.json");
        String jsonText = "";
        JSONParser jsonParser = new JSONParser();

        try {

            jsonText = IOUtils.toString(inputStream, "UTF-8");
            Object object = jsonParser.parse(jsonText);
            JSONObject jsonObject = (JSONObject)object;

            ownerUri = (String)jsonObject.get("ownerUri");
            ownerEmail = (String)jsonObject.get("ownerEmail");
            categoricalOption = (String)jsonObject.get("categoricalOption");

            JSONArray tests = (JSONArray) jsonObject.get("testCases");
            Iterator<JSONObject> iterator = tests.iterator();
            while (iterator.hasNext()) {
                JSONObject test = iterator.next();
                Map<String, Object> testDescription = new HashMap<>();
                testDescription.put("acquisition_uri_str", (String)test.get("acquisition_uri_str"));
                testDescription.put("originalDApath", (String)test.get("originalDApath"));
                JSONObject facetsObj = (JSONObject)test.get("facets");
                testDescription.put("facets", facetsObj.toJSONString());
                Map<String, Object> columnMapping = new HashMap<>();
                JSONArray columns = (JSONArray)test.get("columnMapping");
                Iterator<JSONObject> columnIter = columns.iterator();
                while (columnIter.hasNext()) {
                    JSONObject columnDesc = columnIter.next();
                    String key = (String)columnDesc.get("generated");
                    columnMapping.put(key, new Column((String)columnDesc.get("original"),
                            ((String)columnDesc.get("categorical")).equalsIgnoreCase("true")? true : false));
                }
                testDescription.put("columnMapping", columnMapping);
                testDescriptions.put((String)test.get("acquisition_uri_str"), testDescription);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

class Column {
    protected String columnName;
    protected boolean categorical;
    public Column(String columnName, boolean categorical) {
        this.columnName = columnName;
        this.categorical = categorical;
    }
}