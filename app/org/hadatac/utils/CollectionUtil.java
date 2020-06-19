package org.hadatac.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.console.controllers.sandbox.Sandbox;
import org.hadatac.entity.pojo.OperationMode;

import com.typesafe.config.ConfigFactory;

public class CollectionUtil {
 
    // private variables
    private static CollectionUtil single_instance = null; 
	private static Map<String, String> configCache = null;

	private CollectionUtil() { 
    	configCache = new HashMap<String,String>();
		initConfigCache();
    } 
  
    // static method to create instance of Singleton class 
    public static CollectionUtil getInstance() { 
        if (single_instance == null) {
            single_instance = new CollectionUtil(); 
        }
        return single_instance; 
    } 
    
    public Map<String, String> getInstanceConfigCache() {
    	return configCache;
    }
    
	public static Map<String, String> getConfigCache() {
		return CollectionUtil.getInstance().getInstanceConfigCache();
	}
	
    public enum Collection {
        // data and auxiliary data 
        DATA_COLLECTION ("/sdc"), 
        DATA_ACQUISITION ("/measurement"),
        METADATA_AQUISITION ("/data_acquisitions"),
        SA_ACQUISITION ("/schema_attributes"),
        CONSOLE_STORE ("/console_store"),
        STUDIES ("/studies"),
        ANALYTES ("/analytes"),
        ANNOTATION_LOG ("/annotation_log"),
        CSV_DATASET ("/csv"),
        OPERATION_MODE ("/operation_mode"),
        NAMESPACE ("/namespace"),
        URI_GENERATOR ("/uri_generator"),
        STUDY_ACQUISITION ("/studies"),
        METADATA_DA ("/data_acquisitions"),
        ANALYTES_ACQUISITION ("/analytes"),
        SCHEMA_ATTRIBUTES ("/schema_attributes"),
        
        // triplestore
        METADATA_SPARQL ("/store/query"),
        METADATA_UPDATE ("/store/update"),
        METADATA_GRAPH ("/store/data"),

        // users 
        AUTHENTICATE_USERS ("/users"),
        AUTHENTICATE_ACCOUNTS ("/linked_account"),
        AUTHENTICATE_ROLES ("/security_role"),
        AUTHENTICATE_TOKENS ("/token_action"),
        AUTHENTICATE_PERMISSIONS ("/user_permission"),

        // permissions
        PERMISSIONS_SPARQL ("/store_users/query"),
        PERMISSIONS_UPDATE ("/store_users/update"),
        PERMISSIONS_GRAPH ("/store_users/data")
        ;
        
        private final String collectionString;

        private Collection(String collectionString) {
            this.collectionString = collectionString;
        }
        
        public String get() {
            return collectionString;
        }
    }
    
    public static String getCollectionName(String collection) {
        if (Arrays.asList(
                Collection.OPERATION_MODE.get(),
                Collection.AUTHENTICATE_ACCOUNTS.get(), 
                Collection.AUTHENTICATE_USERS.get(), 
                Collection.AUTHENTICATE_ROLES.get(),
                Collection.AUTHENTICATE_TOKENS.get(),
                Collection.AUTHENTICATE_PERMISSIONS.get(),
                Collection.PERMISSIONS_SPARQL.get(),
                Collection.PERMISSIONS_UPDATE.get(),
                Collection.PERMISSIONS_GRAPH.get()).contains(collection)) {
            return collection;
        }
        
        if (isSandboxMode()) {
            if (Arrays.asList(
                    Collection.METADATA_SPARQL.get(), 
                    Collection.METADATA_UPDATE.get(), 
                    Collection.METADATA_GRAPH.get()).contains(collection)) {
                return collection.replace("store", "store" + Sandbox.SUFFIX);
            } else {
                return collection + Sandbox.SUFFIX;
            }
        }

        return collection;
    }
    
    public void initConfigCache() {
    	configCache = new HashMap<String, String>();
    	configCache.put("hadatac.solr.triplestore", ConfigFactory.load().getString("hadatac.solr.triplestore"));
    	configCache.put("hadatac.solr.users",ConfigFactory.load().getString("hadatac.solr.users"));
    	configCache.put("hadatac.solr.permissions",ConfigFactory.load().getString("hadatac.solr.permissions"));
    	configCache.put("hadatac.solr.data", ConfigFactory.load().getString("hadatac.solr.data"));
    }
    
    public static boolean isSandboxMode() {
        List<OperationMode> modes = OperationMode.findAll();
        if (modes.size() > 0) {
            OperationMode mode = modes.get(0);
            if (mode.getOperationMode().equals(OperationMode.SANDBOX)) {
                return true;
            }
        }
        
        return false;
    }

    public static String getCollectionPath(Collection collection) {
        String collectionName = null;
        switch (collection) {
        case METADATA_SPARQL:
        case METADATA_UPDATE:
        case METADATA_GRAPH :          
            collectionName = getConfigCache().get("hadatac.solr.triplestore") + getCollectionName(collection.get());
        break;
        case AUTHENTICATE_USERS:
        case AUTHENTICATE_ACCOUNTS: 
        case AUTHENTICATE_ROLES: 
        case AUTHENTICATE_TOKENS:
        case AUTHENTICATE_PERMISSIONS: 
            collectionName = getConfigCache().get("hadatac.solr.users") + collection.get();
        break;
        case PERMISSIONS_SPARQL:
        case PERMISSIONS_UPDATE:
        case PERMISSIONS_GRAPH :       
            collectionName = getConfigCache().get("hadatac.solr.permissions") + collection.get();
        break;
        case DATA_COLLECTION:
        case DATA_ACQUISITION:
        case METADATA_AQUISITION:
        case SA_ACQUISITION :
        case CONSOLE_STORE:
        case STUDIES:
        case ANALYTES:
        case ANNOTATION_LOG:
        case OPERATION_MODE:
        case NAMESPACE:
        case CSV_DATASET:
        case URI_GENERATOR :
        case STUDY_ACQUISITION:
        case METADATA_DA:
        case ANALYTES_ACQUISITION:
        case SCHEMA_ATTRIBUTES:        
            collectionName = getConfigCache().get("hadatac.solr.data") + getCollectionName(collection.get());
        break;
        }
        
        return collectionName;
    }
}
