package org.hadatac.entity.pojo;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class AnnotatedValueSummary {

	public static String GREATER_THAN = "GT";
	public static String GREATER_OR_EQUAL_THAN = "GE";
	public static String EQUAL = "EQ";
	public static String LESS_OR_EQUAL_THAN = "LE";
	public static String LESS_THAN = "LT";

	private AnnotatedValue avalue;
    private int frequency;
    private List<Pair<String,String>> constraints;

	public AnnotatedValueSummary(String value) {
		AnnotatedValue aval = new AnnotatedValue(value);
		this.avalue = aval;
		this.frequency = 0;
	}

	public AnnotatedValueSummary(AnnotatedValue avalue) {
    	this.avalue = avalue;
    	this.frequency = 0;
    }

	public AnnotatedValue getAnnotatedValue() {
		return avalue;
	}

	public void setAnnotatedValue(AnnotatedValue avalue) { this.avalue = avalue; }

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) { this.frequency = frequency; }

	public String toString() {
    	if (avalue != null) {
			return avalue.getValue();
		}
    	return "";
    }

    private static void addCodeIntoAlignment(Alignment alignment, String code, String label, String codeValueClass) {
		if (code != null && !code.equals("")) {
			if (alignment.getCode(codeValueClass) == null) {
				List<String> newEntry = new ArrayList<String>();
				newEntry.add(code);
				newEntry.add(label);
				alignment.addCode(codeValueClass, newEntry);
			}
		}
	}

	public static Map<String, AnnotatedValueSummary> categorizeAgeInMonths(Alignment alignment, Map<String, AnnotatedValueSummary> variableSummary) {
		Map<String, AnnotatedValueSummary> categVS = new HashMap<String, AnnotatedValueSummary>();
		for (Map.Entry<String, AnnotatedValueSummary> entry: variableSummary.entrySet()) {
			System.out.println("     value: " + entry.getKey() + " freq: " + entry.getValue().getFrequency());
			try {
				int rawVal = Integer.parseInt(entry.getKey());
				AnnotatedValueSummary avs;
				if (rawVal < 12) {
					avs = categVS.get("205");
					if (avs == null) {
						avs = new AnnotatedValueSummary("205");
						avs.getAnnotatedValue().setValueClass("http://purl.org/twc/EXAMPLE_000005");
						avs.setFrequency(0);
						addCodeIntoAlignment(alignment, "205", "<12 months", "http://purl.org/twc/EXAMPLE_000005");
					}
					avs.setFrequency(avs.getFrequency() + entry.getValue().getFrequency());
					categVS.put(avs.getAnnotatedValue().getValue(),avs);
				} else if (rawVal >= 12 && rawVal <= 24) {
					avs = categVS.get("206");
					if (avs == null) {
						avs = new AnnotatedValueSummary("206");
						avs.getAnnotatedValue().setValueClass("http://purl.org/twc/EXAMPLE_000006");
						avs.setFrequency(0);
						addCodeIntoAlignment(alignment, "206", "12-24 months", "http://purl.org/twc/EXAMPLE_000006");
					}
					avs.setFrequency(avs.getFrequency() + entry.getValue().getFrequency());
					categVS.put(avs.getAnnotatedValue().getValue(),avs);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return categVS;
	}

	public static Map<String, AnnotatedValueSummary> categorizeAgeInYears(Alignment alignment, Map<String, AnnotatedValueSummary> variableSummary) {
		Map<String, AnnotatedValueSummary> categVS = new HashMap<String, AnnotatedValueSummary>();
		for (Map.Entry<String, AnnotatedValueSummary> entry: variableSummary.entrySet()) {
			System.out.println("     value: " + entry.getKey() + " freq: " + entry.getValue().getFrequency());
			try {
				int rawVal = Integer.parseInt(entry.getKey());
				AnnotatedValueSummary avs;
				if (rawVal < 20) {
					avs = categVS.get("201");
					if (avs == null) {
						avs = new AnnotatedValueSummary("201");
						avs.getAnnotatedValue().setValueClass("http://purl.org/twc/EXAMPLE_000001");
						avs.setFrequency(0);
						addCodeIntoAlignment(alignment, "201", "<20 years", "http://purl.org/twc/EXAMPLE_000001");
					}
					avs.setFrequency(avs.getFrequency() + entry.getValue().getFrequency());
					categVS.put(avs.getAnnotatedValue().getValue(),avs);
				} else if (rawVal >= 20 && rawVal <= 39) {
					avs = categVS.get("202");
					if (avs == null) {
						avs = new AnnotatedValueSummary("202");
						avs.getAnnotatedValue().setValueClass("http://purl.org/twc/EXAMPLE_000002");
						avs.setFrequency(0);
						addCodeIntoAlignment(alignment, "202", "20-39 years", "http://purl.org/twc/EXAMPLE_000002");
					}
					avs.setFrequency(avs.getFrequency() + entry.getValue().getFrequency());
					categVS.put(avs.getAnnotatedValue().getValue(),avs);
				} else if (rawVal >= 40 && rawVal <= 59) {
					avs = categVS.get("203");
					if (avs == null) {
						avs = new AnnotatedValueSummary("203");
						avs.getAnnotatedValue().setValueClass("http://purl.org/twc/EXAMPLE_000003");
						avs.setFrequency(0);
						addCodeIntoAlignment(alignment, "203", "40-59 years", "http://purl.org/twc/EXAMPLE_000003");
					}
					avs.setFrequency(avs.getFrequency() + entry.getValue().getFrequency());
					categVS.put(avs.getAnnotatedValue().getValue(),avs);
				} else if (rawVal >= 60) {
					avs = categVS.get("204");
					if (avs == null) {
						avs = new AnnotatedValueSummary("204");
						avs.getAnnotatedValue().setValueClass("http://purl.org/twc/EXAMPLE_000004");
						avs.setFrequency(0);
						addCodeIntoAlignment(alignment, "204", "60+ years", "http://purl.org/twc/EXAMPLE_000004");
					}
					avs.setFrequency(avs.getFrequency() + entry.getValue().getFrequency());
					categVS.put(avs.getAnnotatedValue().getValue(),avs);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return categVS;
	}

	public static Map<String, Map<String, AnnotatedValueSummary>> categorizeNonCategorical(Alignment alignment, List<Variable> vars, Map<String, Map<String, AnnotatedValueSummary>> summary) {
		System.out.println("inside categorizeNonCategorical");
		Map<String, Map<String, AnnotatedValueSummary>> categorizedSummary = new HashMap<String, Map<String, AnnotatedValueSummary>>();
		Map<String, AnnotatedValueSummary> categorizedVariable;

		Map<String, Variable> varsMap = new HashMap<String, Variable>();
		for (Variable variable : vars) {
			//System.out.println("avail vars: " + variable.toString());
			varsMap.put(variable.toString(), variable);
		}

		for (Map.Entry<String, Map<String, AnnotatedValueSummary>> entry: summary.entrySet()) {
			//System.out.println("key: " + entry.getKey() + " value: " + entry.getValue());

			// get Variable from variable's key
			Variable var = varsMap.get(entry.getKey());
			categorizedVariable = entry.getValue();

			// handle age variables
			if (var != null && var.getAttributeListStr().equals("http://semanticscience.org/resource/SIO_001013")) {
				System.out.println("AGE Variable: " + var.toString() + "   unit: " + var.getUnitStr());

				// age in years
				if (var.getUnitStr().equals("http://semanticscience.org/resource/SIO_000428")) {
					categorizedVariable = categorizeAgeInYears(alignment, entry.getValue());

				// age in months
				} else if (var.getUnitStr().equals("http://semanticscience.org/resource/SIO_000429")) {
					categorizedVariable = categorizeAgeInMonths(alignment, entry.getValue());
				}

			}

			categorizedSummary.put(var.toString(), categorizedVariable);
		}


		return categorizedSummary;
	}

}
