package org.hadatac.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Properties;

import org.hadatac.console.controllers.sandbox.Sandbox;
import org.json.simple.JSONArray;

import com.typesafe.config.ConfigFactory;

public class ConfigProp {

	public static final String AUTOANNOTATOR_CONFIG_FILE = "autoccsv.config";

	public static final String GUI_CONFIG_FILE = "gui.config";

	public static final String MEDIA_FOLDER = "media";

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

	public static String getBasePrefix() {
		return ConfigFactory.load().getString("hadatac.community.ont_prefix");
	}

	public static String getKbPrefix() {
		return ConfigFactory.load().getString("hadatac.community.ont_prefix") + "-kb:";
	}

	public static String getTemplateFileName() {
		return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "template_file_name");
	}

	public static String getPathUnproc() {
	    if (CollectionUtil.isSandboxMode()) {
	        return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_unproc") + Sandbox.SUFFIX;
	    }
		return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_unproc");
	}

	public static String getPathProc() {
	    if (CollectionUtil.isSandboxMode()) {
	        return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_proc") + Sandbox.SUFFIX;
	    }
	    return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_proc");
	}

	public static String getPathMedia() {
	    return Paths.get(getPathProc(), "/" + MEDIA_FOLDER + "/").toString();
	}

	public static String getPathDownload() {
	    if (CollectionUtil.isSandboxMode()) {
	        return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_download") + Sandbox.SUFFIX;
	    }
		return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_download");
	}

	public static String getPathDataDictionary() {
        if (CollectionUtil.isSandboxMode()) {
            return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_data_dict") + Sandbox.SUFFIX;
        }
        return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_data_dict");
    }

	public static String getPathWorking() {
	    if (CollectionUtil.isSandboxMode()) {
	        return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_working") + Sandbox.SUFFIX;
	    }
		return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_working");
	}

	public static String getDefaultOwnerEmail() {
        return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "default_owner_email");
    }

	public static String getFacetedDataUnit() {
        return getPropertyValue(GUI_CONFIG_FILE, "faceted_data_unit");
    }

	public static String getFacetedDataTime() {
        return getPropertyValue(GUI_CONFIG_FILE, "faceted_data_time");
    }

	public static String getFacetedDataSpace() {
        return getPropertyValue(GUI_CONFIG_FILE, "faceted_data_space");
    }

	public static String getFacetedDataPlatform() {
        return getPropertyValue(GUI_CONFIG_FILE, "faceted_data_platform");
    }

    public static String getBioportalApiKey() {
        return ConfigFactory.load().getString("hadatac.search.bioportal_api_key");
    }

    public static boolean hasBioportalApiKey() {
      String apikey = getBioportalApiKey();
      if(apikey.isEmpty()){
         return false;
      }

      if(apikey.length() != 36){
         System.err.println("Bad Bioportal API key, please update hadatac.config");
         return false;
      }

      return true;
    }

    public static String getSDDGenAddress() {
        return ConfigFactory.load().getString("hadatac.search.sdd_gen_address");
    }

    public static boolean hasSDDGenAddress() {
      String sddAddress = getSDDGenAddress();
      return !sddAddress.isEmpty();
    }



	@SuppressWarnings("unchecked")
	public static String toGuiJson() {

    	JSONArray gui = new JSONArray();

    	gui.add(true);
    	gui.add(true);
    	gui.add(true);
    	if (ConfigProp.getFacetedDataUnit().equals("on")) {
    		gui.add(true);
    	} else {
    		gui.add(false);
    	}
    	if (ConfigProp.getFacetedDataTime().equals("on")) {
    		gui.add(true);
    	} else {
    		gui.add(false);
    	}
    	if (ConfigProp.getFacetedDataPlatform().equals("on")) {
    		gui.add(true);
    	} else {
    		gui.add(false);
    	}

    	return gui.toJSONString();
    }

}
