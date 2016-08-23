package org.hadatac.entity.pojo;

import java.util.List;

public class Characteristic {
	private String uri;
	private String label;
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public static Characteristic find(String uri) {
		return null;
	}
	
	public static List<Characteristic> find(List<MeasurementType> list) {
		
		return null;
	}
}
