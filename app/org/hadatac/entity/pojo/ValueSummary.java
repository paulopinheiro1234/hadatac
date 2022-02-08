package org.hadatac.entity.pojo;

public class ValueSummary {

	private Value value;
    private int frequency;

    public ValueSummary(Value value) {
    	this.value = value;
    	this.frequency = 0;
    }

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) { this.value = value; }

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) { this.frequency = frequency; }

	public String toString() {
    	if (value != null) {
			return value.getValue();
		}
    	return "";
    }

}
