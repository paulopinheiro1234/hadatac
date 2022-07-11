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

	//public static final String AUTOANNOTATOR_CONFIG_FILE = "autoccsv.config";

	//public static final String GUI_CONFIG_FILE = "gui.config";

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

	public static String getBaseURL() {
		return ConfigFactory.load().getString("hadatac.console.host_deploy");
	}

	public static String getBasePrefix() {
		return ConfigFactory.load().getString("hadatac.community.ont_prefix");
	}

	public static String getKbPrefix() {
		return ConfigFactory.load().getString("hadatac.community.ont_prefix") + "-kb:";
	}

	public static String getPageTitle() {
		return ConfigFactory.load().getString("hadatac.community.pagetitle");
	}
	public static String getShortName() {
		return ConfigFactory.load().getString("hadatac.community.shortname");
	}
	public static String getFullName() {
		return ConfigFactory.load().getString("hadatac.community.fullname");
	}
	public static String getDescription() {
		return ConfigFactory.load().getString("hadatac.community.description");
	}

	public static String getTmp() {
		return ConfigFactory.load().getString("hadatac.autoccsv.path_tmp");
	}

	public static String getLogs() {
		return ConfigFactory.load().getString("hadatac.autoccsv.path_logs");
	}

	public static String getTemplateFileName() {
		return ConfigFactory.load().getString("hadatac.autoccsv.template_file_name");
		//return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "template_file_name");
	}

	public static String getPathUnproc() {
	    if (CollectionUtil.isSandboxMode()) {
	    	return ConfigFactory.load().getString("hadatac.autoccsv.path_unproc") + Sandbox.SUFFIX;
	        //return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_unproc") + Sandbox.SUFFIX;
	    }
	    return ConfigFactory.load().getString("hadatac.autoccsv.path_unproc");
		//return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_unproc");
	}

	public static String getPathProc() {
	    if (CollectionUtil.isSandboxMode()) {
	    	return ConfigFactory.load().getString("hadatac.autoccsv.path_proc") + Sandbox.SUFFIX;
	        //return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_proc") + Sandbox.SUFFIX;
	    }
	    return ConfigFactory.load().getString("hadatac.autoccsv.path_proc");
	    //return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_proc");
	}

	public static String getPathMedia() {
	    return Paths.get(getPathProc(), "/" + MEDIA_FOLDER + "/").toString();
	}

	public static String getPathDataDictionary() {
        return getPathWorking();
    }

	public static String getPathWorking() {
        // This normalizes the config path (removes trailing /, allows speical path chars such as ..)
        // We should probably do this everywhere
        String path = Paths.get(ConfigFactory.load().getString("hadatac.autoccsv.path_working")).normalize().toString();

	    if (CollectionUtil.isSandboxMode()) {
	    	return path + Sandbox.SUFFIX;
	        //return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_working") + Sandbox.SUFFIX;
	    }
	    return path;
		//return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "path_working");
	}
	public static String getAuto() {
		if(System.getProperty("hadatac.autoccsv.auto") != null && !System.getProperty("hadatac.autoccsv.auto").equals(ConfigFactory.load().getString("hadatac.autoccsv.auto"))){
			return System.getProperty("hadatac.autoccsv.auto");
		}
		return ConfigFactory.load().getString("hadatac.autoccsv.auto");
	}

	public static String getDefaultOwnerEmail() {
		return ConfigFactory.load().getString("hadatac.autoccsv.default_owner_email");
        //return getPropertyValue(AUTOANNOTATOR_CONFIG_FILE, "default_owner_email");
    }

	public static String getFacetedDataUnit() {
		return ConfigFactory.load().getString("hadatac.gui.faceted_data_unit");
        //return getPropertyValue(GUI_CONFIG_FILE, "faceted_data_unit");
    }

	public static String getFacetedDataTime() {
		return ConfigFactory.load().getString("hadatac.gui.faceted_data_time");
        //return getPropertyValue(GUI_CONFIG_FILE, "faceted_data_time");
    }

	public static String getFacetedDataSpace() {
		return ConfigFactory.load().getString("hadatac.gui.faceted_data_space");
        //return getPropertyValue(GUI_CONFIG_FILE, "faceted_data_space");
    }

	public static String getFacetedDataPlatform() {
		return ConfigFactory.load().getString("hadatac.gui.faceted_data_platform");
        //return getPropertyValue(GUI_CONFIG_FILE, "faceted_data_platform");
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

    public static String getTestUsername() {
        return ConfigFactory.load().getString("hadatac.test.user_name");
    }

    public static boolean getSignUpBool() {
        if(ConfigFactory.load().getString("hadatac.test.sign_up").toLowerCase().equals("true")){
           return true;
        }
        else{
           return false;
        }
    }

    public static String getTestUserPass() {
        return ConfigFactory.load().getString("hadatac.test.user_password");
    }

    public static String getWebDriverPath() {
        return ConfigFactory.load().getString("hadatac.test.web_driver_path");
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
