package org.hadatac.utils;

import org.hadatac.data.loader.DataContext;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.PermissionsContext;

public class Repository {

	public static final String METADATA = "metadata";
	public static final String DATA     = "data";	
	
    public static boolean operational(String repository) {
    	if (repository.equals(METADATA)) {
    		return ((MetadataContext.playTotalTriples() != -1) && 
    				(PermissionsContext.playTotalTriples() != -1));
    	} else if (repository.equals(DATA)) {
        	 return ((DataContext.playTotalDataCollections() != -1)&&
                     (DataContext.playTotalMeasurements() != -1));
    		
    	}
    	return false;
    }

}	
	
