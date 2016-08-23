package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

public class Pivot {
	public List<Pivot> children;
	
	public String field;
	public String value;
	public int count;
	
	public Pivot() {
		children = new ArrayList<Pivot>();
	}
}
