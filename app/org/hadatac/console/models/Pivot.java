package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;


public class Pivot {
	public Pivot parent;
	public List<Pivot> children;

	public String field;
	public String value;
	public int count;

	public Pivot() {
		parent = null;
		children = new ArrayList<Pivot>();
	}

	public void addChild(Pivot child) {
		child.parent = this;
		if (!children.contains(child)) {
			children.add(child);
		}
	}

	public void findByField(String field, List<Pivot> pivots) {
		for (Pivot child : children) {
			child.findByField(field, pivots);
			if (null != this.field && this.field.equals(field)) {
				pivots.add(child);
			}
		}
	}
	
	public Pivot findByValue(Pivot targetPivot, String value) {
		if (this.value.equals(value)) {
			if (!targetPivot.children.contains(this)) {
				Pivot pivot = new Pivot();
				pivot.field = this.field;
				pivot.value = this.value;
				pivot.count = this.count;
				pivot.children = null;
				targetPivot.addChild(pivot);
			}
		} else {
			for (Pivot child : children) {
				//Pivot potential
				for (Pivot target_child : targetPivot.children) {
					child.findByValue(target_child, value);
				}
			}
		}
		return new Pivot();
	}

	public Pivot tracePivot() {
		System.out.println("tracePivot value: " + this.value);
		System.out.println("tracePivot parent: " + this.parent);
		System.out.println("tracePivot field: " + this.field);
		if (null == parent) {
			return this;
		}
		Pivot new_parent = new Pivot();
		new_parent.field = parent.field;
		new_parent.value = parent.value;
		new_parent.addChild(this);

		return new_parent.tracePivot();
	}

	public void setNullParent() {
		parent = null;
		for (Pivot child : children) {
			child.setNullParent();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		
		if (null == this || null == o) {
			return false;
		}

		if (!(o instanceof Pivot)) {
			return false;
		}

		Pivot c = (Pivot) o;

		return (field == c.field && value == c.value);
	}
}
