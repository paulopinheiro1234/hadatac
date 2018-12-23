package org.hadatac.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Comparator;
import java.util.Collections;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;


public class NameSpaces {

    public static String CACHE_PATH   = "tmp/cache/";
    public static String CACHE_PREFIX = "copy-";

    public static Map<String, NameSpace> table = new HashMap<String, NameSpace>();

    public Map<String, Integer> loadedOntologies; 

    private static NameSpaces instance = null;

    public static NameSpaces getInstance() {
        if(instance == null) {
            instance = new NameSpaces();
        }
        return instance;
    }

    private NameSpaces() {
        loadedOntologies = new HashMap<String, Integer>();
        
        List<NameSpace> namespaces = NameSpace.findAll();
        if (namespaces.isEmpty()) {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("namespaces.properties");
            namespaces = loadFromFile(inputStream);
        }
        
        for (NameSpace ns : namespaces) {
            ns.updateLoadedTripleSize();
            table.put(ns.getAbbreviation(), ns);
        }
    }
    
    public static void reload() {
        table.clear();
        List<NameSpace> namespaces = NameSpace.findAll();
        for (NameSpace ns : namespaces) {
            ns.updateLoadedTripleSize();
            table.put(ns.getAbbreviation(), ns);
        }
    }
    
    public static List<NameSpace> loadFromFile(InputStream inputStream) {
        List<NameSpace> namespaces = new ArrayList<NameSpace>();
        
        try {
            Properties prop = new Properties();
            prop.load(inputStream);
            for (Map.Entry<Object, Object> nsEntry : prop.entrySet()) {
                String nsAbbrev = ((String)nsEntry.getKey());
                if (nsAbbrev != null) {
                    String[] tmpList = prop.getProperty(nsAbbrev).split(",");
                    NameSpace tmpNS = null;
                    if (tmpList.length >= 1 && tmpList[0] != null && !tmpList[0].equals("")) {
                        tmpNS = new NameSpace();
                        tmpNS.setAbbreviation(nsAbbrev);
                        tmpNS.setName(tmpList[0]);
                        if (tmpList.length >= 2 && tmpList[1] != null && !tmpList[1].equals("")) {
                            tmpNS.setType(tmpList[1]);
                        }
                        if (tmpList.length >= 3 && tmpList[2] != null && !tmpList[2].equals("")) {
                            tmpNS.setURL(tmpList[2]);
                        }
                    }
                    if (tmpNS != null) {
                        namespaces.add(tmpNS);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("[NameSpaces.java ERROR]: could not read file namespaces.properties");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return namespaces;
    }
    
    public Map<String, NameSpace> getNamespaces() {
        return table;
    }

    public String printTurtleNameSpaceList() {
        String ttl = "";
        for (Map.Entry<String, NameSpace> entry : table.entrySet()) {
            String abbrev = entry.getKey().toString();;
            NameSpace ns = entry.getValue();
            ttl = ttl + "@prefix " + abbrev + ": <" + ns.getName() + "> . \n";
        }
        return ttl;
    }

    public String printSparqlNameSpaceList() {
        String ttl = "";
        for (Map.Entry<String, NameSpace> entry : table.entrySet()) {
            String abbrev = entry.getKey().toString();;
            NameSpace ns = entry.getValue();
            ttl = ttl + "PREFIX " + abbrev + ": <" + ns.getName() + "> \n";
        }
        return ttl;
    }

    public int getNumberNameSpaces() {
        if (table == null) {  
            return 0;
        }
        return table.size();
    }

    public String jsonLoadedOntologies() {
        String json = "";
        boolean first = true;
        List<Map.Entry<String,Integer>> entries = 
                new ArrayList<Map.Entry<String,Integer>>(loadedOntologies.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String,Integer>>() {
            public int compare(Map.Entry<String,Integer> a, Map.Entry<String,Integer> b) {
                return Integer.compare(b.getValue(), a.getValue());
            }
        });
        for (Map.Entry<String, Integer> entry : entries) {
            if (first) {
                first = false;
            } else {
                json = json + ",";
            }
            String abbrev = entry.getKey().toString();;
            int triples = entry.getValue();
            json = json + " [\"" + abbrev + "\"," + triples +"]";
        }
        
        return json;
    }

    public String copyNameSpacesLocally(int mode) {
        // copy supporting ontologies locally
        String message = "";
        for (Map.Entry<String, NameSpace> entry : table.entrySet()) {
            String abbrev = entry.getKey().toString();
            String nsURL = entry.getValue().getURL();
            if (nsURL != null && !nsURL.equals("") && !nsURL.equals(":")) {
                String filePath = CACHE_PATH + CACHE_PREFIX + abbrev.replace(":","");
                message += Feedback.print(mode, "   Creating local copy of " + abbrev + ". ");		
                for (int i = abbrev.length(); i < 36; i++) {
                    message += Feedback.print(mode, ".");
                }

                try {
                    URL url = new URL(nsURL);
                    File f = new File(filePath);
                    FileUtils.copyURLToFile(url, f);
                    message += Feedback.println(mode, "Local copy created.");
                } catch (Exception e) {
                    message += Feedback.println(mode, "Failed to create local copy.");
                }
            }

        }	
        message += Feedback.println(mode," ");
        
        return message;
    }	

    public static void main(String[] args) {
        NameSpaces ns = new NameSpaces();
        System.out.println(ns.printTurtleNameSpaceList());
    }
}
