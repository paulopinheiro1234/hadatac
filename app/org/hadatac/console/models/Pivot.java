package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

public class Pivot {
	public Pivot parent;
	public List<Pivot> children;

	public String field;
	public String value;
	public String tooltip;
	public int count;

	public Pivot() {
		parent = null;
		children = new ArrayList<Pivot>();
	}

	public void addChild(Pivot child) {
		child.parent = this;
		children.add(child);
	}

	public void setNullParent() {
		parent = null;
		for (Pivot child : children) {
			child.setNullParent();
		}
	}
	
	public int recomputeStats() {
		if (children.isEmpty()) {
			return count;
		}
		
		int cnt = 0;
		for (Pivot child : children) {
			cnt += child.recomputeStats();
		}
		
		count = cnt;
		
		return count;
	}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof Pivot) && (((Pivot)o).value.equals(this.value))) {
			return true;
		} else {
			return false;
		}
	}
}
