package org.hadatac.data.loader;

public interface Record {
	
	public String getValueByColumnName(String colomnName);
	
	public String getValueByColumnIndex(int index);
	
	public int size();
}