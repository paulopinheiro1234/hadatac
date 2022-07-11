package org.hadatac.entity.pojo;

import java.util.*;

public class HarmonizedVariableSpec extends VariableSpec {

	private List<VariableSpec> sourceList;

	public HarmonizedVariableSpec(List<VariableSpec> sourceList) {
		super(sourceList);
		sourceList.sort(new Comparator<VariableSpec>() {
			@Override
			public int compare(VariableSpec o1, VariableSpec o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		this.sourceList = sourceList;
		List<String> roles = new ArrayList<String>();
		for (VariableSpec var : sourceList) {
			if (!roles.contains(var.getRole())) {
				roles.add(var.getRole());
			}
		}
		if (roles.size() == 1) {
			this.setRole(roles.get(0));
		} else if (roles.size() > 1) {
			boolean first = true;
			String newRole = "";
			for (String role : roles) {
				if (first) {
					first = false;
				} else {
					newRole = newRole + "+";
				}
				newRole = newRole + role;
			}
			this.setRole(newRole);
		}
	}

	public List<VariableSpec> getSourceList() {
		return sourceList;
	}

	public void setSourceList(List<VariableSpec> sourceList) {
		this.sourceList = sourceList;
	}

	public void addSource(VariableSpec newVar) {
		this.sourceList.add(newVar);
	}

	public static String concatenatedKeys(List<VariableSpec> sourceList) {
		if (sourceList == null) {
			return null;
		}
		sourceList.sort(new Comparator<VariableSpec>() {
			@Override
			public int compare(VariableSpec o1, VariableSpec o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		if (sourceList.size() == 1) {
			return sourceList.get(0).toString();
		} else if (sourceList.size() > 1) {
			boolean first = true;
			String finalKey = "";
			for (VariableSpec var : sourceList) {
				if (first) {
					first = false;
				} else {
					finalKey = finalKey + "+";
				}
				finalKey = finalKey + var.toString();
			}
			return finalKey;
		}
		return null;

	}

	public static boolean harmonizable(VariableSpec v1, VariableSpec v2) {
		if (v1.getEntityStr() != null && v2.getEntityStr() != null && !v1.getEntityStr().equals(v2.getEntityStr())) {
			return false;
		}
		if (v1.getAttributeList() != null && v2.getAttributeList() != null) {
			if (v1.getAttributeList().size() != v2.getAttributeList().size()) {
				return false;
			}
			for (int ind = 0; ind < v1.getAttributeList().size(); ind++) {
				if (!v1.getAttributeList().get(ind).getUri().equals(v2.getAttributeList().get(ind).getUri())) {
					return false;
				}
			}

		}
		if (v1.getUnitStr() != null && v2.getUnitStr() != null && !v1.getUnitStr().equals(v2.getUnitStr())) {
			return false;
		}
		if (v1.getTimeStr() != null && v2.getTimeStr() != null && !v1.getTimeStr().equals(v2.getTimeStr())) {
			return false;
		}
		return true;
	}

	public String sourceToString() {
		//System.out.println("inside sourceToString()");
		String auxstr = "";
		for (VariableSpec source : sourceList) {
			auxstr = auxstr + source.toString() + "<br>";
		}
		return auxstr;
	}

	public String datafileToString() {
		//System.out.println("inside datafileToString()");
		String auxstr = "";
		for (VariableSpec source : sourceList) {
			if (source instanceof OriginalVariableSpec) {
				String strUri = ((OriginalVariableSpec) source).getSTR().getUri();
				List<DataFile> dataFileList = DataFile.findByDataAcquisition(strUri);
				String fileNameList = "";
				if (dataFileList != null && dataFileList.size() > 0) {
					if (dataFileList.size() > 1) {
						fileNameList = "[";
						for (DataFile df : dataFileList) {
							fileNameList = fileNameList + df.getBaseName() + "<br>";
						}
						fileNameList = fileNameList + "] ";
					} else {
						fileNameList = dataFileList.get(0).getBaseName() + "<br>";

					}
				}
				auxstr = auxstr + fileNameList + "<br>";
			} else {
				auxstr = auxstr + "<br>";

			}
		}
		return auxstr;
	}

	public String dasaToString(int index) {
		//System.out.println("inside dasaToString()");
		String auxstr = "";
		int i = 1;
		for (VariableSpec source : sourceList) {
			if (source instanceof OriginalVariableSpec && ((OriginalVariableSpec) source).getDASA() != null) {
				auxstr = auxstr + " <button id=\'srcBtn" + (index + i++) + "\'>" +((OriginalVariableSpec) source).getDASA().getLabel() + "</button><br>";
			} else {
				auxstr = auxstr + "<br>";

			}
		}
		return auxstr;
	}

	public static HarmonizedVariableSpec selectFromList(List<HarmonizedVariableSpec> list, String key) {
		for (HarmonizedVariableSpec var : list) {
			if (var.toString().equals(key)) {
				return var;
			}
		}
		return null;
	}

}