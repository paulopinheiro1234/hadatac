package org.hadatac.metadata.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.hadatac.metadata.loader.NameSpace;
import org.hadatac.metadata.loader.NameSpaces;

public class NameSpaces {

	public static Map<String, NameSpace> table = new HashMap<String, NameSpace>(); 

	private static NameSpaces instance = null;

	public static NameSpaces getInstance() {
	      if(instance == null) {
	         instance = new NameSpaces();
	      }
	      return instance;
	   }
	   
	private NameSpaces() {
				
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			System.out.println("loading properties file for namespaces.");
			//prop.load(new FileInputStream("namespaces.properties"));
			input = getClass().getClassLoader().getResourceAsStream("namespaces.properties");
			prop.load(input);
			String tmpList[];
			NameSpace tmpNS;
			for (Map.Entry<Object, Object> nsEntry : prop.entrySet()) {
				String nsAbbrev = ((String)nsEntry.getKey());
				//System.out.println("abbrev.: " + nsAbbrev);
			    if (nsAbbrev != null) {
			        tmpList = prop.getProperty(nsAbbrev).split(",");
			        //System.out.println("Value: " + nsEntry.getValue());
			        tmpNS = null;
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
				        //System.out.println("loaded " + tmpNS.toString());
			        }
			        if (tmpNS != null) {
			        	table.put(nsAbbrev, tmpNS);
			        }
			    }
			}
		} catch (FileNotFoundException e) {
			System.out.println("[NameSpaces.java ERROR]: could not read file namespaces.properties");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*	table.put(":","http://jefferson.tw.rpi.edu/ontology/jp-sn.owl#");
		table.put("jp-sn:", "http://jefferson.tw.rpi.edu/ontology/jp-kb.owl#");
		table.put("jp-entities:", "http://jefferson.tw.rpi.edu/ontology/jp-entities#");
		table.put("jp-standards:", "http://jefferson.tw.rpi.edu/ontology/jp-standards.owl#");
		table.put("jp-characteristics:", "http://jefferson.tw.rpi.edu/ontology/jp-characteristics.owl#");
		table.put("oboe:", "http://ecoinformatics.org/oboe/oboe.1.0/oboe.owl#");
		table.put("oboe-core:","http://ecoinformatics.org/oboe/oboe.1.0/oboe-core.owl#");
		table.put("oboe-standards:", "http://ecoinformatics.org/oboe/oboe.1.0/oboe-standards.owl#");
		table.put("oboe-chemistry:", "http://ecoinformatics.org/oboe/oboe.1.0/oboe-chemistry.owl#");
		table.put("oboe-ecology:", "http://ecoinformatics.org/oboe/oboe.1.0/oboe-ecology.owl#");
		table.put("oboe-characteristics:", "http://ecoinformatics.org/oboe/oboe.1.0/oboe-characteristics.owl#");
		table.put("rdf:", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		table.put("rdfs:", "http://www.w3.org/2000/01/rdf-schema#");
		table.put("xsd:", "http://www.w3.org/2001/XMLSchema#");
		table.put("owl:", "http://www.w3.org/2002/07/owl#");
	    table.put("vstoi:", "http://jefferson.tw.rpi.edu/ontology/vstoi#");
	    table.put("prov:", "http://www.w3.org/ns/prov-o#");
	    table.put("hasneto:", "http://jefferson.tw.rpi.edu/ontology/hasneto.owl#");
	    table.put("foaf:", "http://xmlns.com/foaf/0.1/");
	    */
	}

	public String printNameSpaceList() {
		String ttl = "";
	    for (Map.Entry<String, NameSpace> entry : table.entrySet()) {
	        String abbrev = entry.getKey().toString();;
	        NameSpace ns = entry.getValue();
	        //System.out.println("@prefix " + abbrev + " <" + ns.toString() + "> .");
	        ttl = ttl + "@prefix " + abbrev + ": <" + ns.getName() + "> . \n";
	    }
	    return ttl;
	}
		
	public void copyNameSpacesLocally() {
	    //byte[] buffer = new byte[1024];
	    //int bytesRead;

	    // copy supporting ontologies locally
	    for (Map.Entry<String, NameSpace> entry : table.entrySet()) {
	    	String abbrev = entry.getKey().toString();
	    	String nsURL = entry.getValue().getURL();
	    	if (nsURL != null && !nsURL.equals("") && !nsURL.equals(":")) {
	    		String filePath = "copy" + "-" + abbrev.replace(":","");
	    		System.out.println("   creating local copy of " + abbrev);		
	         
	    		//if (!abbrev.equals("jp-entities:") && (!abbrev.equals(":")) && (!abbrev.equals("jp-sn:"))) {
	    			URL url;

	    			
	    			try {
						url = new URL(nsURL);
						File f = new File(filePath);
		    			FileUtils.copyURLToFile(url, f);
		    			
		    			/*
		    			BufferedInputStream inputStream = null;
						BufferedOutputStream outputStream = null;
						URLConnection connection = url.openConnection();
						// If you need to use a proxy for your connection, the URL class has another openConnection method.
						// For example, to connect to my local SOCKS proxy I can use:
						// url.openConnection(new Proxy(Proxy.Type.SOCKS, newInetSocketAddress("localhost", 5555)));
						inputStream = new BufferedInputStream(connection.getInputStream());
						File f = new File(filePath);
						outputStream = new BufferedOutputStream(new FileOutputStream(f));
						while ((bytesRead = inputStream.read(buffer)) != -1) {
							outputStream.write(buffer, 0, bytesRead);
						}
						inputStream.close();
						outputStream.close();
						*/
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		//}
	    	}
	        
	    }	   
	}	

    public static void main(String[] args) {
    	NameSpaces ns = new NameSpaces();
    	System.out.println(ns.printNameSpaceList());
    }

	
}
