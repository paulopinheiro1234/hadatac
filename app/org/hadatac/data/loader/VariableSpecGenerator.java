package org.hadatac.data.loader;

import org.hadatac.entity.pojo.*;
import org.hadatac.metadata.loader.URIUtils;

import java.util.*;

public class VariableSpecGenerator {

    public static int createVarSpecFromSdd(String sddUri) {
        System.out.println("Inside createVarSpecFromSdd");
        DataAcquisitionSchema sdd = DataAcquisitionSchema.find(sddUri);
        if (sdd != null) {
            System.out.println("  - FOUND Sdd " + sdd.getUri());
            List<DataAcquisitionSchemaAttribute> dasaList = sdd.getAttributes();
            if (dasaList != null && dasaList.size() > 0) {
                System.out.println("  - SIZE ATTRIBUTE LIST " + dasaList.size());
                for (DataAcquisitionSchemaAttribute dasa : dasaList) {
                    System.out.println("     + ATTRIBUTE (1) DASA URI [" + dasa.getUri() + "]");
                    VariableSpec varSpec = new VariableSpec(dasa);
                    System.out.println("     + ATTRIBUTE (2) VAR SPEC [" + varSpec.toString() + "]");
                    String varSpecUri = URIUtils.replacePrefixEx(varSpec.getUri());
                    dasa.setVariableSpec(varSpecUri);
                    System.out.println("     + ATTRIBUTE (3) VAR SPEC URI IN DASA [" + dasa.getVariableSpec() + "]");
                    varSpec.save();
                    System.out.println("     + ATTRIBUTE (4) VAR SPEC SAVED");
                    dasa.saveHasVariableSpec(varSpecUri);
                    System.out.println("     + ATTRIBUTE (5) STEP DASA UPDATED");
                }
                return 0;
            }
        }
        return -1;
    }

}
