package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;

public class OriginalVariableSpec extends VariableSpec {

	private STR str;

	private DataAcquisitionSchemaAttribute dasa;

	public OriginalVariableSpec(VariableSpec variableSpec) {
		super(variableSpec);
    }

    public STR getSTR() {
    	return str;
    }

	public void setSTR(STR str) {
		this.str = str;
	}

	public DataAcquisitionSchemaAttribute getDASA() {
		return dasa;
	}

	public void setDASA(DataAcquisitionSchemaAttribute dasa) {
		this.dasa = dasa;
	}

	public List<String> getDataFileNames() {
		List<String> dfList = new ArrayList<String>();
		String strUri = str.getUri();
		if (str == null) {
			return dfList;
		}
		List<DataFile> dataFileList = DataFile.findByDataAcquisition(strUri);
		if (dataFileList == null || dataFileList.size() <= 0) {
			return dfList;
		}
		for (DataFile df : dataFileList) {
			dfList.add(df.getBaseName());
		}
		return dfList;
	}

}
