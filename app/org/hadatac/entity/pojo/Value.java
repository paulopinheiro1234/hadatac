package org.hadatac.entity.pojo;

public class Value {

	private String value;
    private String valueClass;

    public Value(String value) {
    	this(value, null);
    }

    public Value(String value, String valueClass) {
    	this.value = value;
    	this.valueClass = valueClass;
    }

	public String getValue() {
		return value;
	}

	public void setValue(String value) { this.value = value; }

	public String getValueClass() {
		return valueClass;
	}

	public void setValueClass(String valueClass) { this.valueClass = valueClass; }

	public String toString() { return value; }

}
