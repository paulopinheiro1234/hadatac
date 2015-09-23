package org.hadatac.utils;

import play.Play;

public class Collections {

    // data and auxiliary data 
    public static final String DATA_COLLECTION             = "/sdc";
	public static final String DATA_ACQUISITION            = "/measurement";
	public static final String CONSOLE_STORE               = "/console_store";
	public static final String URI_GENERATOR               = "/uri_generator";
    
    // triplestore
	public static final String METADATA_SPARQL             = "/store/sparql";
	public static final String METADATA_GRAPH              = "/store/rdf-graph-store";
    
    // users 
	public static final String AUTHENTICATE_USERS          = "/users";
	public static final String AUTHENTICATE_ACCOUNTS       = "/linked_account";
	public static final String AUTHENTICATE_ROLES          = "/security_role";
	public static final String AUTHENTICATE_TOKENS         = "/token_action";
	public static final String AUTHENTICATE_PERMISSIONS    = "/user_permission";
   
    // permissions
	public static final String PERMISSIONS_SPARQL          = "/store_users/sparql";
	public static final String PERMISSIONS_GRAPH           = "/store_users/rdf-graph-store";
    
	public static String getCollectionsName(String request) {
	
		String collectionName = null;
		switch (request) {
            case DATA_COLLECTION:
            case DATA_ACQUISITION:
            case CONSOLE_STORE:
            case URI_GENERATOR :           collectionName = Play.application().configuration().getString("hadatac.solr.data") + request;
                                           break;
            case METADATA_SPARQL:
            case METADATA_GRAPH :          collectionName = Play.application().configuration().getString("hadatac.solr.triplestore") + request;
                                           break;
            case AUTHENTICATE_USERS:
            case AUTHENTICATE_ACCOUNTS: 
            case AUTHENTICATE_ROLES: 
            case AUTHENTICATE_TOKENS:
            case AUTHENTICATE_PERMISSIONS: collectionName = Play.application().configuration().getString("hadatac.solr.permissions") + request;
                                           break;
            case PERMISSIONS_SPARQL:
            case PERMISSIONS_GRAPH :       collectionName = Play.application().configuration().getString("hadatac.solr.permissions") + request;
                                           break;
		}
		return collectionName;
	}

}
