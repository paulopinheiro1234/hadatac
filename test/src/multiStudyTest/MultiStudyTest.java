package src.multiStudyTest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hadatac.console.controllers.dataacquisitionsearch.Downloader;
import org.hadatac.data.loader.CSVRecordFile;
import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.entity.pojo.ColumnMapping;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.SPARQLUtilsFacetSearch;
import org.hadatac.utils.ConfigProp;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import src.domain.MultiStudyTestConfig;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MultiStudyTest {

    Map<String, Object> testDescriptions = new HashMap<>();
    List<MultiStudyTestConfig> testCases = new ArrayList<>();
    int totalDownloads = 0;

    final String EXACT_MATCH = "ExactEquality";
    final String CLASSPATH = "classpath:";
    final String OBJECT_ALIGNMENT = "object_alignment";
    final String SOURCES = "sources";
    final String SOURCES_HEADER = "used_DOI";
    final String CODEBOOK = "codebook";

    String ownerUri = "http://hadatac.org/kb/hhear#PER-YU";
    String ownerEmail = "liyang.yu@mssm.edu";
    String categoricalOption = "withCodeBook";
    public ErrorCollector collector;

    public MultiStudyTest(ErrorCollector collector) {
        this.collector = collector;
    }

    /*@Rule
    public ErrorCollector collector = new ErrorCollector();

    @Before
    public void initialize()  {
        // SPARQLUtilsFacetSearch.createInMemoryModel();
        // retrieveTestConfig();
    }

    @After
    public void tearDown() {
        System.out.println("\n\n------ FINAL REPORT ------\n");
        System.out.println("=> multi-study test: total number of downloaded DA/Lab files that have been tested: " + totalDownloads + "\n\n");
    }*/

    // @Test
    public void multiStudyTest() {

        /*
        here is how to run:
        sbt "testOnly *MultiStudyTest"
        */

        int totalTests = testCases.size();
        int testCount = 0;

        for ( MultiStudyTestConfig testEntry : testCases ) {

            testCount++;
            String facets = testEntry.getQuery();

            // execute the main codebase to download the data file
            System.out.println("\n\n\n====> working on " + testEntry.getTag() + ", " + testCount + " of " + totalTests);
            ColumnMapping columnMapping = new ColumnMapping();
            Downloader.generateCSVFileBySubjectAlignment(ownerUri, facets, ownerEmail, Measurement.SUMMARY_TYPE_NONE, categoricalOption, true, columnMapping);
            // Downloader.generateCSVFileBySubjectAlignment(ownerUri, facets, ownerEmail, categoricalOption, true, columnMapping);

            // System.out.println(columnMapping.toString());

            // get the downloaded data files from hard drive and match them to benchmark files
            Map<String, String> filePaths = pairBenchmarksToGeneratedFiles(testEntry.getBenchmarkFilePath());
            assertTrue("cannot find the downloaded file.", filePaths.size() > 0);
            System.out.println("mapping between benchmark files to generated datasets:");
            for ( Map.Entry<String, String> entry : filePaths.entrySet() ) System.out.println(entry.getKey() + " --> " + entry.getValue());

            // compare the downloaded file to the benchmark file
            for ( Map.Entry<String, String> entry : filePaths.entrySet() ) {
                // if ( entry.getKey().contains("1449-1438") ) continue;
                System.out.printf("\n\ncomparing %s with %s\n", entry.getKey(), entry.getValue());
                if ( testEntry.getValidationMethod().indexOf(EXACT_MATCH) >= 0 ) {
                    if ( entry.getKey().indexOf(OBJECT_ALIGNMENT) > 0 ) checkExactMatchBySubsetChecking(testEntry.getTag(), entry.getKey(), entry.getValue());
                    else checkExactMatch(testEntry.getTag(), entry.getKey(), entry.getValue());
                } else {
                    if ( entry.getKey().indexOf(SOURCES) >= 0 ) checkSubsetMatchForSource(entry.getKey(), entry.getValue());
                    else if ( entry.getKey().indexOf(CODEBOOK) >= 0 ) checkSubsetMatchForCodebook(entry.getKey(), entry.getValue());
                    else checkSubsetMatch(entry.getKey(), entry.getValue());
                }
            }

            totalDownloads ++;

        }

    }

    public void retrieveTestConfig() {

        URL url = getClass().getResource("/multiStudyTestConfig.json");
        if ( url == null ) return;

        try {

            BufferedReader br = new BufferedReader(new FileReader(url.getPath()));
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(br).getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("testCases");

            for ( int i = 0; i < jsonArray.size(); i++ ) {
                MultiStudyTestConfig multiStudyTestConfig = new MultiStudyTestConfig();
                JsonObject obj = jsonArray.get(i).getAsJsonObject();
                multiStudyTestConfig.setTag(obj.get("tag").getAsString());
                multiStudyTestConfig.setQuery(obj.get("query").toString());
                multiStudyTestConfig.setBenchmarkFilePath(obj.get("benchmarkFilePath").getAsString());
                multiStudyTestConfig.setValidationMethod(obj.get("validationMethod").getAsString());
                multiStudyTestConfig.setDescription(obj.get("description").getAsString());
                testCases.add(multiStudyTestConfig);
            }

        } catch (Exception e) {
            System.out.println("something is wrong when parsing the test configuration: " + e.getMessage());
            System.exit(1);
        }


    }

    private Map<String, String> pairBenchmarksToGeneratedFiles(String benchmarkFilePath) {

        Map<String, String> map = new HashMap<>();
        String[] paths = benchmarkFilePath.split("\\s+");
        String root = paths[0];
        for ( int i = 1; i < paths.length; i++ ) {
            String key = root + paths[i];
            File generatedFile = getDownloadedFile(ownerEmail, paths[i].substring(paths[i].lastIndexOf("-")+1, paths[i].indexOf(".")));
            map.put(key, generatedFile.getAbsolutePath());
        }

        return map;
    }

    private void checkExactMatch(String tag, String benchmarkFile, String targetPath) {

        String benchmarkPath = benchmarkFile;
        if ( benchmarkFile.contains(CLASSPATH) ) {
            URL url = getClass().getResource("/" +
                    benchmarkFile.substring(benchmarkFile.indexOf(CLASSPATH) + CLASSPATH.length()).trim());
            benchmarkPath = url.getPath();
        }

        // get the hash for benchmark
        String benchmarkHash = getHash(benchmarkPath);
        // get the hash for generated dataset
        String targetHash = getHash(targetPath);

        // check to see if they match
        try {
            assertEquals("[" + tag + "]: benchmark and generated dataset does not match:\n benchmark: " + benchmarkPath +
                    "\n generated dataset: " + targetPath,
                    benchmarkHash, targetHash);
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
            collector.addError(e);
            return;
        }

        System.out.println("[" + tag + "]: benchmark and generated dataset matches successfully");
    }

    private void checkExactMatchBySubsetChecking(String tag, String benchmarkFile, String targetPath) {

        String benchmarkPath = benchmarkFile;
        if ( benchmarkFile.contains(CLASSPATH) ) {
            URL url = getClass().getResource("/" +
                    benchmarkFile.substring(benchmarkFile.indexOf(CLASSPATH) + CLASSPATH.length()).trim());
            benchmarkPath = url.getPath();
        }

        // check to see if they match
        try {
            if ( !checkSubsetMatch(benchmarkPath, targetPath) || !checkSubsetMatch(targetPath, benchmarkPath) ) {
                assertEquals("[" + tag + "]: benchmark and generated dataset does not match:\n benchmark: " + benchmarkPath +
                                "\n generated dataset: " + targetPath,
                        benchmarkPath, targetPath);
            }
        } catch (AssertionError e) {
            System.out.println(e.getMessage());
            collector.addError(e);
        }

    }

    private String getHash(String fileName) {

        byte[] buffer= new byte[8192];
        int count;

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");

            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
            while ((count = bis.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            bis.close();
        } catch (Exception e) {
            System.out.println("something goes wrong when calculating the hash value of a given file: " + e.getMessage());
            System.exit(1);
        }

        byte[] hash = digest.digest();

        // This hash[] has bytes in decimal format, convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< hash.length ;i++) sb.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));

        return sb.toString();
    }

    private boolean checkSubsetMatch(String benchmarkPath, String downloadFilePath) {

        List<Record> generatedRecords = null, benchmarkRecords = null;
        List<String> generatedHeaders = null, benchmarkHeaders = null;
        RecordFile recordFile = null;

        if ( benchmarkPath.contains(CLASSPATH) ) {
            benchmarkPath = getResourceFile(benchmarkPath);
            if ( benchmarkPath == null ) return false;
        }

        // retrieve all the original records in the DA file
        recordFile = new CSVRecordFile(new File(benchmarkPath));
        benchmarkRecords = recordFile.getRecords();
        benchmarkHeaders = recordFile.getHeaders();

        // retrieve all the downloaded records
        recordFile = new CSVRecordFile(new File(downloadFilePath));
        generatedRecords = recordFile.getRecords();
        generatedHeaders = recordFile.getHeaders();

        Set<String> generatedHeaderSet = new HashSet<>();
        for ( String header : generatedHeaders ) generatedHeaderSet.add(header);

        // each header in the benchmark dataset has to be presented in the generated dataset
        for ( String header : benchmarkHeaders ) {
            if (generatedHeaderSet.contains(header) == false) {
                String errMsg = MessageFormat.format("benchmark dataset {0}, has header {1}, " +
                        "that is not present in the downloaded dataset {2}",
                        benchmarkPath, header, downloadFilePath);
                System.out.println(errMsg);
                collector.addError(new AssertionError(errMsg));
                return false;
            }
        }

        /*
        each row in the benchmark dataset has to be present in the generated file.
        Map<String, Map<String, List<String>>> is used to represent each row. for now,
        the key is Human-ID (could be a risky assumption?)
         */
        Map<String, Map<String, List<String>>> benchmarkDataMap = retrieveRowData(benchmarkRecords, benchmarkHeaders, "Human-D");
        Map<String, Map<String, List<String>>> generatedDataMap = retrieveRowData(generatedRecords, generatedHeaders, "Human-D");

        for ( Map.Entry<String, Map<String, List<String>>> entry : benchmarkDataMap.entrySet() ) {

            String sampleId = entry.getKey();
            if ( !generatedDataMap.containsKey(sampleId) ) {
                String errMsg = MessageFormat.format("the benchmark dataset {0} has a sample with ID {1} " +
                        "that is not contained by the generated file {2}", benchmarkPath, sampleId, downloadFilePath);
                System.out.println(errMsg);
                collector.addError(new AssertionError(errMsg));
                return false;
            }

            Map<String, List<String>> rowBenchmark = entry.getValue();
            Map<String, List<String>> rowGenerated = generatedDataMap.get(sampleId);

            for ( Map.Entry<String, List<String>> cellBenchmark : rowBenchmark.entrySet() ) {

                String cellHeader = cellBenchmark.getKey();
                List<String> cellContent = cellBenchmark.getValue();

                if ( !rowGenerated.containsKey(cellHeader) ) {
                    String errMsg = MessageFormat.format("the benchmark dataset {0} has a cell with header {1} " +
                            "that is not contained by the generated dataset {2}", benchmarkPath, cellHeader, downloadFilePath);
                    System.out.println(errMsg);
                    collector.addError(new AssertionError(errMsg));
                    return false;
                }
                List<String> cellValueGenerated = rowGenerated.get(cellHeader);

                if ( !equalLists(cellContent, cellValueGenerated) ) {
                    String errMsg = MessageFormat.format("the benchmark dataset {0} has a cell whose " +
                                    "content is not equal to the same cell in the generated dataset {1}: \n" +
                                    "cell header = {2}, cell key = {3}, cell content = {4}, benchmark content = {5}",
                            benchmarkPath, downloadFilePath, cellHeader, sampleId, cellValueGenerated, cellContent);
                    System.out.println(errMsg);
                    collector.addError(new AssertionError(errMsg));
                    return false;
                }
            }
        }

        System.out.println(MessageFormat.format("-- success: benchmark {0} is a sub-set of generated dataset {1}",
                benchmarkPath, downloadFilePath));

        return true;

    }

    private boolean checkSubsetMatchForSource(String benchmarkPath, String downloadFilePath) {

        List<Record> generatedRecords = null, benchmarkRecords = null;
        List<String> generatedHeaders = null, benchmarkHeaders = null;
        RecordFile recordFile = null;

        if ( benchmarkPath.contains(CLASSPATH) ) {
            benchmarkPath = getResourceFile(benchmarkPath);
            if ( benchmarkPath == null ) return false;
        }

        // retrieve all the original records in the DA file
        recordFile = new CSVRecordFile(new File(benchmarkPath));
        benchmarkRecords = recordFile.getRecords();
        benchmarkHeaders = recordFile.getHeaders();

        // retrieve all the downloaded records
        recordFile = new CSVRecordFile(new File(downloadFilePath));
        generatedRecords = recordFile.getRecords();
        generatedHeaders = recordFile.getHeaders();

        Set<String> generatedHeaderSet = new HashSet<>();
        for ( String header : generatedHeaders ) generatedHeaderSet.add(header);

        // both the benchmark and generated file should have "used_DOI" as the only header
        if ( benchmarkHeaders.size() != 1 || !benchmarkHeaders.get(0).equalsIgnoreCase(SOURCES_HEADER) ) {
            String errMsg = MessageFormat.format("benchmark dataset {0}, has header {1}, " +
                            "that is not equal to {2}", benchmarkPath, benchmarkHeaders.get(0), SOURCES_HEADER);
            System.out.println(errMsg);
            collector.addError(new AssertionError(errMsg));
            return false;
        }
        if ( generatedHeaders.size() != 1 || !generatedHeaders.get(0).equalsIgnoreCase(SOURCES_HEADER) ) {
            String errMsg = MessageFormat.format("generated dataset {0}, has header {1}, " +
                    "that is not equal to {2}", downloadFilePath, generatedHeaders.get(0), SOURCES_HEADER);
            System.out.println(errMsg);
            collector.addError(new AssertionError(errMsg));
            return false;
        }

        // each row in the benchmark dataset has to be present in the generated file.

        Map<String, Integer> benchmarkDataMap = retrieveRowDataForSource(benchmarkRecords, benchmarkHeaders);
        Map<String, Integer> generatedDataMap = retrieveRowDataForSource(generatedRecords, generatedHeaders);

        for ( Map.Entry<String, Integer> entry : benchmarkDataMap.entrySet() ) {

            String sourceId = entry.getKey();
            int sourceFrequency = entry.getValue();

            if ( !generatedDataMap.containsKey(sourceId) ) {
                String errMsg = MessageFormat.format("the benchmark source {0} has a source {1} " +
                        "that is not contained by the generated source {2}", benchmarkPath, sourceId, downloadFilePath);
                System.out.println(errMsg);
                collector.addError(new AssertionError(errMsg));
                return false;
            }

            if ( generatedDataMap.get(sourceId) < sourceFrequency ) {
                String errMsg = MessageFormat.format("the benchmark source {0} has a source {1} " +
                        "that has less frequency count than the generated source {2}", benchmarkPath, sourceId, downloadFilePath);
                System.out.println(errMsg);
                collector.addError(new AssertionError(errMsg));
                return false;
            }

        }

        System.out.println(MessageFormat.format("-- success: benchmark {0} is a sub-set of generated dataset {1}",
                benchmarkPath, downloadFilePath));

        return true;

    }

    private boolean checkSubsetMatchForCodebook(String benchmarkPath, String downloadFilePath) {

        List<Record> generatedRecords = null, benchmarkRecords = null;
        List<String> generatedHeaders = null, benchmarkHeaders = null;
        RecordFile recordFile = null;

        if ( benchmarkPath.contains(CLASSPATH) ) {
            benchmarkPath = getResourceFile(benchmarkPath);
            if ( benchmarkPath == null ) return false;
        }

        // retrieve all the original records in the DA file
        recordFile = new CSVRecordFile(new File(benchmarkPath));
        benchmarkRecords = recordFile.getRecords();
        benchmarkHeaders = recordFile.getHeaders();

        // retrieve all the downloaded records
        recordFile = new CSVRecordFile(new File(downloadFilePath));
        generatedRecords = recordFile.getRecords();
        generatedHeaders = recordFile.getHeaders();

        Set<String> generatedHeaderSet = new HashSet<>();
        for ( String header : generatedHeaders ) generatedHeaderSet.add(header);

        // each header in the benchmark dataset has to be presented in the generated dataset
        for ( String header : benchmarkHeaders ) {
            if (generatedHeaderSet.contains(header) == false) {
                String errMsg = MessageFormat.format("benchmark dataset {0}, has header {1}, " +
                                "that is not present in the downloaded dataset {2}",
                        benchmarkPath, header, downloadFilePath);
                System.out.println(errMsg);
                collector.addError(new AssertionError(errMsg));
                return false;
            }
        }

        /*
        each row in the benchmark dataset has to be present in the generated file.
        Map<String, Map<String, List<String>>> is used to represent each row. for now,
        the key is "code" (could be a risky assumption?)
         */
        Map<String, Map<String, List<String>>> benchmarkDataMap = retrieveRowData(benchmarkRecords, benchmarkHeaders, "code");
        Map<String, Map<String, List<String>>> generatedDataMap = retrieveRowData(generatedRecords, generatedHeaders, "code");

        for ( Map.Entry<String, Map<String, List<String>>> entry : benchmarkDataMap.entrySet() ) {

            String code = entry.getKey();
            if ( !generatedDataMap.containsKey(code) ) {
                String errMsg = MessageFormat.format("the benchmark dataset {0} has a sample with ID {1} " +
                        "that is not contained by the generated file {2}", benchmarkPath, code, downloadFilePath);
                System.out.println(errMsg);
                collector.addError(new AssertionError(errMsg));
                return false;
            }

            Map<String, List<String>> rowBenchmark = entry.getValue();
            Map<String, List<String>> rowGenerated = generatedDataMap.get(code);

            for ( Map.Entry<String, List<String>> cellBenchmark : rowBenchmark.entrySet() ) {

                String cellHeader = cellBenchmark.getKey();
                List<String> cellContent = cellBenchmark.getValue();

                if ( !rowGenerated.containsKey(cellHeader) ) {
                    String errMsg = MessageFormat.format("the benchmark dataset {0} has a cell with header {1} " +
                            "that is not contained by the generated dataset {2}", benchmarkPath, cellHeader, downloadFilePath);
                    System.out.println(errMsg);
                    collector.addError(new AssertionError(errMsg));
                    return false;
                }
                List<String> cellValueGenerated = rowGenerated.get(cellHeader);

                if ( !equalLists(cellContent, cellValueGenerated) ) {
                    String errMsg = MessageFormat.format("the benchmark dataset {0} has a cell whose " +
                                    "content is not equal to the same cell in the generated file {1}: \n" +
                                    "cell header = {2}, cell key = {3}, cell content = {4}, benchmark content = {5}",
                            benchmarkPath, downloadFilePath, cellHeader, code, cellValueGenerated, cellContent);
                    System.out.println(errMsg);
                    collector.addError(new AssertionError(errMsg));
                    return false;
                }
            }
        }

        System.out.println(MessageFormat.format("-- success: benchmark {0} is a sub-set of generated dataset {1}",
                benchmarkPath, downloadFilePath));

        return true;
    }

    private String getResourceFile(String benchmarkPath) {

        String resourcePath = "/" + benchmarkPath.substring(benchmarkPath.indexOf(CLASSPATH) + CLASSPATH.length()).trim();
        URL url = getClass().getResource(resourcePath);
        if ( url == null ) {
            String errMsg = MessageFormat.format("the given resource path {0} cannot be located.", resourcePath);
            System.out.println(errMsg);
            collector.addError(new AssertionError(errMsg));
            return null;
        }
        return url.getPath();
    }

    private boolean equalLists(List<String> one, List<String> two) {

        if (one == null && two == null) return true;
        if( (one == null && two != null)  || (one != null && two == null) || one.size() != two.size()) return false;

        // to avoid messing the order of the lists we will use a copy
        List<String> oneCopy = new ArrayList<>();
        List<String> twoCopy = new ArrayList<>();

        for ( String item : one ) oneCopy.add(item.trim());
        for ( String item : two ) twoCopy.add(item.trim());

        return oneCopy.containsAll(twoCopy) && twoCopy.containsAll(oneCopy);
    }

    private Map<String, Map<String, List<String>>> retrieveRowData(List<Record> rows, List<String> headers, String headerKey) {

        Map<String, Map<String, List<String>>> rowDataMap = new HashMap<>();

        for ( Record record : rows ) {

            String rowKey = null;
            Map<String, List<String>> row = new HashMap<>();

            for (String header : headers ) {

                if ( headerKey.equalsIgnoreCase(header) ) {
                    rowKey = record.getValueByColumnName(header);
                    continue;
                }

                String cell = record.getValueByColumnName(header);
                if ( cell == null) continue;

                cell = cell.trim();
                String[] cellValue = null;

                if ( cell.contains(",") ) cellValue = cell.split(",");
                else cellValue = cell.split("\\s+");

                row.put(header, Arrays.asList(cellValue));
            }

            rowDataMap.put(rowKey, row);
        }

        return rowDataMap;

    }

    private Map<String, Integer> retrieveRowDataForSource(List<Record> rows, List<String> headers) {

        Map<String, Integer> rowDataMap = new HashMap<>();

        for ( Record record : rows ) {
            String header = headers.get(0);
            String cell = record.getValueByColumnName(header);
            if ( cell == null) continue;
            cell = cell.trim();
            rowDataMap.put(cell, rowDataMap.getOrDefault(cell, 0)+1);
        }
        return rowDataMap;
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
}