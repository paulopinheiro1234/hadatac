package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;

public class OriginalVariable extends Variable {

	private STR str;

	private DataAcquisitionSchemaAttribute dasa;

	public OriginalVariable(Variable variable) {
		super(variable);
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

}
