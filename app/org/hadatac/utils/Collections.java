package org.hadatac.utils;

import play.Play;

public class Collections {

    // data and auxiliary data 
        public static final String DATA_COLLECTION             = "/sdc";
	public static final String DATA_ACQUISITION            = "/measurement";
	public static final String METADATA_AQUISITION	       = "/data_acquisitions";
	public static final String SA_ACQUISITION              = "/schema_attributes";
	public static final String CONSOLE_STORE               = "/console_store";
	public static final String STUDIES		       = "/studies";
	public static final String ANALYTES		       = "/analytes";
	public static final String ANNOTATION_LOG              = "/annotation_log";
	public static final String CSV_DATASET                 = "/csv";
	public static final String LABKEY_CREDENTIAL           = "/labkey";
	public static final String URI_GENERATOR               = "/uri_generator";
	public static final String STUDY_ACQUISITION           = "/studies/select";
	public static final String SAMPLES_ACQUISITION         = "/samples/select";
	public static final String METADATA_DA		       = "/data_acquisitions/select";
	public static final String SUBJECTS_ACQUISITION	       = "/subjects/select";
	public static final String ANALYTES_ACQUISITION        = "/analytes/select";
	public static final String SCHEMA_ATTRIBUTES           = "/schema_attributes/select";
    
    // triplestore
	public static final String METADATA_SPARQL             = "/store/query";
	public static final String METADATA_UPDATE             = "/store/update";
	public static final String METADATA_GRAPH              = "/store/data";
    
    // users 
	public static final String AUTHENTICATE_USERS          = "/users";
	public static final String AUTHENTICATE_ACCOUNTS       = "/linked_account";
	public static final String AUTHENTICATE_ROLES          = "/security_role";
	public static final String AUTHENTICATE_TOKENS         = "/token_action";
	public static final String AUTHENTICATE_PERMISSIONS    = "/user_permission";
   
    // permissions
	public static final String PERMISSIONS_SPARQL          = "/store_users/query";
	public static final String PERMISSIONS_UPDATE          = "/store_users/update";
	public static final String PERMISSIONS_GRAPH           = "/store_users/data";
    
	public static String getCollectionsName(String request) {
	
		String collectionName = null;
		switch (request) {
            case DATA_COLLECTION:
            case DATA_ACQUISITION:
            case METADATA_AQUISITION:
            case SA_ACQUISITION :
            case CONSOLE_STORE:
            case URI_GENERATOR :           collectionName = Play.application().configuration().getString("hadatac.solr.data") + request;
                                           break;
            case METADATA_SPARQL:
            case METADATA_UPDATE:
            case METADATA_GRAPH :          collectionName = Play.application().configuration().getString("hadatac.solr.triplestore") + request;
                                           break;
            case AUTHENTICATE_USERS:
            case AUTHENTICATE_ACCOUNTS: 
            case AUTHENTICATE_ROLES: 
            case AUTHENTICATE_TOKENS:
            case AUTHENTICATE_PERMISSIONS: collectionName = Play.application().configuration().getString("hadatac.solr.data") + request;
                                           break;
            case PERMISSIONS_SPARQL:
            case PERMISSIONS_UPDATE:
            case PERMISSIONS_GRAPH :       collectionName = Play.application().configuration().getString("hadatac.solr.permissions") + request;
                                           break;
            case STUDY_ACQUISITION:
            case SUBJECTS_ACQUISITION:
            case SAMPLES_ACQUISITION:
            case ANALYTES_ACQUISITION:
            case METADATA_DA:
            case STUDIES:
            case SCHEMA_ATTRIBUTES:        collectionName = Play.application().configuration().getString("hadatac.solr.data") + request;
            							   break;
                                           
		}
		return collectionName;
	}

}
