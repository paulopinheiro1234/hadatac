package org.hadatac.data.loader;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public interface RecordFile {
	public List<Record> getRecords();
	
	public List<String> getHeaders();
	
	public File getFile();
	
	public boolean isValid();
	
    public int getSheetNumber();
}
