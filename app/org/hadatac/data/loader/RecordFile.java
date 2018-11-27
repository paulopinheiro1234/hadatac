package org.hadatac.data.loader;

import java.io.File;
import java.util.List;

public interface RecordFile {
	public List<Record> getRecords();
	
	public List<String> getHeaders();
	
	public File getFile();
	
	public String getFileName();
	
	public boolean isValid();
	
    public int getNumberOfSheets();
    
    public int getNumberOfRows();

	public String getSheetName();
}
