package org.hadatac.metadata.loader;

public interface RDFContext {

    public Long totalTriples();	

    public Long loadLocalFile(int mode, String filePath, String contentType);
	
}	
	
