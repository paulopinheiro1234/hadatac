package org.hadatac.entity.pojo;

public class AnnotatedGroupSummary {

	private AnnotatedGroup agroup;
    private int frequency;

	public AnnotatedGroupSummary(AnnotatedGroup agroup) {
    	this.agroup = agroup;
    	this.frequency = 0;
    }

	public AnnotatedGroup getAnnotatedGroup() {
		return agroup;
	}

	public void setAnnotatedGroup(AnnotatedGroup agroup) { this.agroup = agroup; }

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) { this.frequency = frequency; }

	public void increaseFrequency() { this.frequency = this.frequency + 1; }

	public String toString() {
    	if (agroup != null) {
			return agroup.toString() + " (freq): " + frequency;
		}
    	return "";
    }

}
