package org.hadatac.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Comparator;
import java.util.Collections;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.hadatac.utils.ConfigProp;

public class NameSpaces {

    public static String CACHE_PATH   = ConfigProp.getTmp() + "cache/";
    public static String CACHE_PREFIX = "copy-";

    private ConcurrentHashMap<String, NameSpace> table = new ConcurrentHashMap<String, NameSpace>();

    private String turtleNameSpaceList = "";
    private String sparqlNameSpaceList = "";

    private static NameSpaces instance = null;

    public static NameSpaces getInstance() {
        if(instance == null) {
            instance = new NameSpaces();
        }
        return instance;
    }

    private NameSpaces() {
        List<NameSpace> namespaces = NameSpace.findAll();
        if (namespaces.isEmpty()) {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("namespaces.properties");
            namespaces = loadFromFile(inputStream);
        }

        for (NameSpace ns : namespaces) {
            ns.updateLoadedTripleSize();
            table.put(ns.getAbbreviation(), ns);
        }
    }

    public String getNameByAbbreviation(String abbr) {
        NameSpace ns = table.get(abbr);
        if (ns == null) {
            return "owl";
        } else {
            return ns.getName();
        }
    }

    public Map<String, Integer> getLoadedOntologies() {
        Map<String, Integer> loadedOntologies = new HashMap<String, Integer>();
        List<NameSpace> list = new ArrayList<NameSpace>(table.values());
        for (NameSpace ns: list) {
            if (ns.getNumberOfLoadedTriples() > 0) {
            	loadedOntologies.put(ns.getAbbreviation(), ns.getNumberOfLoadedTriples());
            }
        }
        return loadedOntologies;
    }

    public void reload() {
        table.clear();
        List<NameSpace> namespaces = NameSpace.findAll();
        for (NameSpace ns : namespaces) {
            ns.updateLoadedTripleSize();
            table.put(ns.getAbbreviation(), ns);
        }

        sparqlNameSpaceList = getSparqlNameSpaceList();
        turtleNameSpaceList = getTurtleNameSpaceList();
    }

    public static List<NameSpace> loadFromFile(InputStream inputStream) {
        List<NameSpace> namespaces = new ArrayList<NameSpace>();

        try {
            Properties prop = new Properties();
            prop.load(inputStream);
            for (Map.Entry<Object, Object> nsEntry : prop.entrySet()) {
                String nsAbbrev = ((String)nsEntry.getKey());

                System.out.println("nsAbbrev = " + nsAbbrev);

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
                        if (tmpList.length >= 4 && tmpList[3] != null && !tmpList[3].equals("")) {
                            try{
                                int priority = Integer.parseInt(tmpList[3]);
                                tmpNS.setPriority(priority);
                            }
                            catch(NumberFormatException e){
                               System.err.println("Bad priority value for " + nsAbbrev + ". Expected an integer and got " + tmpList[3]);
                            }
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

    public ConcurrentHashMap<String, NameSpace> getNamespaces() {
        return table;
    }

    public List<NameSpace> getOrderedNamespacesAsList() {
        List<NameSpace> nameSpaces = new ArrayList<NameSpace>(table.values());

        nameSpaces.sort(new Comparator<NameSpace>() {
            @Override
            public int compare(NameSpace o1, NameSpace o2) {
                return o1.getAbbreviation().toLowerCase().compareTo(
                        o2.getAbbreviation().toLowerCase());
            }
        });

        return nameSpaces;
    }

    private String getTurtleNameSpaceList() {
        String namespaces = "";
        for (Map.Entry<String, NameSpace> entry : table.entrySet()) {
            String abbrev = entry.getKey().toString();;
            NameSpace ns = entry.getValue();
            namespaces = namespaces + "@prefix " + abbrev + ": <" + ns.getName() + "> . \n";
        }

        return namespaces;
    }

    private String getSparqlNameSpaceList() {
        String namespaces = "";
        for (Map.Entry<String, NameSpace> entry : table.entrySet()) {
            String abbrev = entry.getKey().toString();;
            NameSpace ns = entry.getValue();
            namespaces = namespaces + "PREFIX " + abbrev + ": <" + ns.getName() + "> \n";
        }

        return namespaces;
    }

    public String printTurtleNameSpaceList() {
        if (!"".equals(turtleNameSpaceList)) {
            return turtleNameSpaceList;
        }

        return getTurtleNameSpaceList();
    }

    public String printSparqlNameSpaceList() {
        if (!"".equals(sparqlNameSpaceList)) {
            return sparqlNameSpaceList;
        }

        return getSparqlNameSpaceList();
    }

    public int getNumOfNameSpaces() {
        if (table == null) {
            return 0;
        }

        return table.size();
    }

    public String jsonLoadedOntologies() {
        String json = "";
        boolean first = true;
        List<Map.Entry<String,Integer>> entries =
                new ArrayList<Map.Entry<String,Integer>>(getLoadedOntologies().entrySet());
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

    public List<String> listLoadedOntologies() {
        List<String> loadedList = new ArrayList<String>();
        for(Map.Entry<String, NameSpace> entry : table.entrySet()) {
            if(entry.getValue().getNumberOfLoadedTriples() >= 1)
                loadedList.add(entry.getKey());
        }
        return loadedList;
    }
    public List<String> getOrderedPriorityLoadedOntologyKeyList() {
        // generate a list of all loaded ontolgies
        List<NameSpace> namespaceList = new ArrayList<NameSpace>();
        for(Map.Entry<String, NameSpace> entry : table.entrySet()) {
            if(entry.getValue().getNumberOfLoadedTriples() >= 1)
                namespaceList.add(entry.getValue());
        }

        // sort by priority
        namespaceList.sort(new Comparator<NameSpace>() {
            @Override
            public int compare(NameSpace o1, NameSpace o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });

        // Get URIs
        List<String> loadedList = new ArrayList<String>();
        
        for(NameSpace n: namespaceList){
            loadedList.add(n.getAbbreviation().toString());
           
           
        }
        return loadedList;
    }
    public List<String> getOrderedPriorityLoadedOntologyList() {
        // generate a list of all loaded ontolgies
        List<NameSpace> namespaceList = new ArrayList<NameSpace>();
        for(Map.Entry<String, NameSpace> entry : table.entrySet()) {
            if(entry.getValue().getNumberOfLoadedTriples() >= 1)
                namespaceList.add(entry.getValue());
        }

        // sort by priority
        namespaceList.sort(new Comparator<NameSpace>() {
            @Override
            public int compare(NameSpace o1, NameSpace o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });

        // Get URIs
        List<String> loadedList = new ArrayList<String>();
        for(NameSpace n: namespaceList){
            
           loadedList.addAll(n.getOntologyURIs());
           
        }
        
        return loadedList;
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
