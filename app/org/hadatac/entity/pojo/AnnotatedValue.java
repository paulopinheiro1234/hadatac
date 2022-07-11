package org.hadatac.entity.pojo;

public class AnnotatedValue {

	private String value;
    private String valueClass;
    private VariableSpec variableSpec;

    public AnnotatedValue(String value) {
    	this(value, null, null);
    }

    public AnnotatedValue(String value, String valueClass) { this(value, valueClass, null); }

	public AnnotatedValue(String value, String valueClass, VariableSpec variableSpec) {
    	this.value = value;
    	this.valueClass = valueClass;
    	this.variableSpec = variableSpec;
    }

	public String getValue() {
		return value;
	}

	public void setValue(String value) { this.value = value; }

	public String getValueClass() {
		return valueClass;
	}

	public void setValueClass(String valueClass) { this.valueClass = valueClass; }

	public VariableSpec getVariable() {
		return variableSpec;
	}

	public void setVariable(VariableSpec variableSpec) { this.variableSpec = variableSpec; }

	public String toString() { return value; }

}
