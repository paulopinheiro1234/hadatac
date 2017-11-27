package org.hadatac.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import play.Play;

public class ConfigProp {
	public static final String AUTOANNOTATOR_CONFIG_FILE = "autoccsv.config";
	
	private static Properties getProperties(String confFileName) {
		Properties prop = new Properties();
		try {
			InputStream is = ConfigProp.class.getClassLoader().getResourceAsStream(confFileName);
			prop.load(is);
			is.close();
		} catch (Exception e) {
			return null;
		}
		
		return prop;
	}
	
	public static String getPropertyValue(String confFileName, String field) {
		Properties prop = getProperties(confFileName);
		if (null == prop) {
			return "";
		}
		return prop.getProperty(field);
	}
	
	public static void setPropertyValue(String confFileName, String field, String value) {
		Properties prop = getProperties(confFileName);
		if (null == prop) {
			return;
		}
		prop.setProperty(field, value);
		URL url = ConfigProp.class.getClassLoader().getResource(confFileName);
		try {
			prop.store(new FileOutputStream(new File(url.toURI())), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public static String getKbPrefix() {
		return Play.application().configuration().getString("hadatac.community.ont_prefix") + "-kb:";
	}
	
	public static String getTemplateFileName() {
		return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "template_file_name");
	}
	
	public static String getPathUnproc() {
		return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_unproc");
	}
	
	public static String getPathProc() {
		return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_proc");
	}
}
