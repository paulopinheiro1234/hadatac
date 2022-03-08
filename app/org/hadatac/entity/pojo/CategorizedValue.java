package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategorizedValue {

	public static String GREATER_THAN = "GT";
	public static String GREATER_OR_EQUAL_THAN = "GE";
	public static String EQUAL = "EQ";
	public static String LESS_OR_EQUAL_THAN = "LE";
	public static String LESS_THAN = "LT";

    private static void addCodeIntoAlignment(Alignment alignment, String code, String label, String codeValueClass) {
		if (code != null && codeValueClass != null && !code.equals("")) {
			if (alignment.getCode(codeValueClass) == null) {
				List<String> newEntry = new ArrayList<String>();
				newEntry.add(code);
				newEntry.add(label);
				alignment.addCode(codeValueClass, newEntry);
			}
		}
	}

	public static AnnotatedValue categorizeAgeInMonths(AnnotatedValue origValue, Alignment alignment) {
		AnnotatedValue newValue = null;

		int intValue = -1;
		try{
			intValue = Integer.parseInt(origValue.getValue());
		} catch (Exception e) {
			//e.printStackTrace();
			return null;
		}

		if (intValue > -1 && intValue < 12) {
			newValue = new AnnotatedValue("205", "http://purl.org/twc/EXAMPLE_000005");
			addCodeIntoAlignment(alignment, "205", "<12 months", "http://purl.org/twc/EXAMPLE_000005");
		} else if (intValue >= 12 && intValue <= 24) {
			newValue = new AnnotatedValue("206", "http://purl.org/twc/EXAMPLE_000006");
			addCodeIntoAlignment(alignment, "206", "12-24 months", "http://purl.org/twc/EXAMPLE_000006");
		}
		newValue.setVariable(origValue.getVariable());

		return newValue;
	}

	public static AnnotatedValue categorizeAgeInYears(AnnotatedValue origValue, Alignment alignment) {
		AnnotatedValue newValue = null;

		int intValue = -1;
		try{
			intValue = Integer.parseInt(origValue.getValue());
		} catch (Exception e) {
			//e.printStackTrace();
			return null;
		}

		if (intValue > -1 && intValue < 20) {
			newValue = new AnnotatedValue("201", "http://purl.org/twc/EXAMPLE_000001");
			addCodeIntoAlignment(alignment, "201", "<20 years", "http://purl.org/twc/EXAMPLE_000001");
		} else if (intValue >= 20 && intValue <= 39) {
			newValue = new AnnotatedValue("202", "http://purl.org/twc/EXAMPLE_000002");
			addCodeIntoAlignment(alignment, "202", "20-39 years", "http://purl.org/twc/EXAMPLE_000002");
		} else if (intValue >= 20 && intValue <= 39) {
			newValue = new AnnotatedValue("203", "http://purl.org/twc/EXAMPLE_000003");
			addCodeIntoAlignment(alignment, "203", "20-39 years", "http://purl.org/twc/EXAMPLE_000003");
		} else if (intValue >= 40 && intValue <= 59) {
			newValue = new AnnotatedValue("204", "http://purl.org/twc/EXAMPLE_000004");
			addCodeIntoAlignment(alignment, "204", "40-59 years", "http://purl.org/twc/EXAMPLE_000004");
		} else if (intValue >= 60) {
			newValue = new AnnotatedValue("205", "http://purl.org/twc/EXAMPLE_000005");
			addCodeIntoAlignment(alignment, "205", "60+ years", "http://purl.org/twc/EXAMPLE_000005");
		}
		newValue.setVariable(origValue.getVariable());

		return newValue;
	}

	public static AnnotatedValue categorize(AnnotatedValue origValue, Alignment alignment) {
		//System.out.println("inside categorize");
		AnnotatedValue categorized = null;

		// handle age variables
		if (origValue != null && origValue.getVariable() != null) {
			if (origValue.getVariable().getAttributeListStr().equals("http://semanticscience.org/resource/SIO_001013")) {
				String unitStr = origValue.getVariable().getUnitStr();
				//System.out.println("AGE Variable: " + origValue.getVariable().toString() + "   unit: " + unitStr);

				// age in years
				if (unitStr != null && unitStr.equals("http://semanticscience.org/resource/SIO_000428")) {
					categorized = categorizeAgeInYears(origValue, alignment);

				// age in months
				} else if (unitStr != null && unitStr.equals("http://semanticscience.org/resource/SIO_000429")) {
					categorized = categorizeAgeInMonths(origValue, alignment);
				}

			}
		}

		if (categorized == null) {
			return origValue;
		}
		return categorized;
	}

}
