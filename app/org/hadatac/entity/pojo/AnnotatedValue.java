package org.hadatac.entity.pojo;

public class AnnotatedValue {

	private String value;
    private String valueClass;
    private Variable variable;

    public AnnotatedValue(String value) {
    	this(value, null, null);
    }

    public AnnotatedValue(String value, String valueClass) { this(value, valueClass, null); }

	public AnnotatedValue(String value, String valueClass, Variable variable) {
    	this.value = value;
    	this.valueClass = valueClass;
    	this.variable = variable;
    }

	public String getValue() {
		return value;
	}

	public void setValue(String value) { this.value = value; }

	public String getValueClass() {
		return valueClass;
	}

	public void setValueClass(String valueClass) { this.valueClass = valueClass; }

	public Variable getVariable() {
		return variable;
	}

	public void setVariable(Variable variable) { this.variable = variable; }

	public String toString() { return value; }

}
