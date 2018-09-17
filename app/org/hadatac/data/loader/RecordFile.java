package org.hadatac.data.loader;

import java.io.File;
import java.util.List;

public interface RecordFile {
	public List<Record> getRecords();
	
	public List<String> getHeaders();
	
	public File getFile();
	
	public boolean isValid();
	
    public int getNumberOfSheets();

	public String getSheetName();
}
