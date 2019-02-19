package org.hadatac.data.loader;

import java.lang.String;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.VirtualColumn;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.console.controllers.annotator.AnnotationLog;

public class VirtualColumnGenerator extends BaseGenerator {

    final String kbPrefix = ConfigProp.getKbPrefix();
    String annotationFileName = "";

    public VirtualColumnGenerator(RecordFile file, String annotationFileName) {
        super(file);
        String str = file.getFile().getName().replaceAll("SSD-", "");
        this.annotationFileName = annotationFileName;
    }

    @Override
    public void initMapping() {
        mapCol.clear();
        mapCol.put("sheet", "sheet");
        mapCol.put("typeUri", "type");
        mapCol.put("hasSOCReference", "hasSOCReference");
        mapCol.put("studyUri", "isMemberOf");
        mapCol.put("groundingLabel", "groundingLabel");
    }

    private String getTypeUri(Record rec) {
        return rec.getValueByColumnName(mapCol.get("typeUri"));
    }

    private String getStudyUri(Record rec) {
        return rec.getValueByColumnName(mapCol.get("studyUri"));
    }

    private String getSOCReference(Record rec) {
        String ref = rec.getValueByColumnName(mapCol.get("hasSOCReference"));
        return ref.trim().replace(" ", "").replace("_", "-");
    }

    private String getGroundingLabel(Record rec) {
        return rec.getValueByColumnName(mapCol.get("groundingLabel"));
    }

    public VirtualColumn createVirtualColumn(Record record) throws Exception {
  
        // Skip the study row in the SSD sheet
        System.out.println("VirtualColumnGenerator: row's type: [" + this.getTypeUri(record) + "]   [" + URIUtils.replacePrefix("hasco:Study") + "]");
        if (this.getTypeUri(record).equals("hasco:Study")) {
            return null;
        }
        
        this.studyUri = getStudyUri(record);
        if (this.studyUri == null || this.studyUri.equals("")) {
            AnnotationLog.printException("VirtualColumnGenerator: no studyUri provided for generator with typeUri [" + this.getTypeUri(record) +"]", annotationFileName);
            return null;
        }
            
        String SOCReference = getSOCReference(record);
        if (SOCReference == null || SOCReference.equals("")) {
            AnnotationLog.printException("VirtualColumnGenerator: no SOCReference provided for generator", annotationFileName);
            return null;
        }
            
        VirtualColumn vc = new VirtualColumn(
                getStudyUri(record),
                getGroundingLabel(record),
                getSOCReference(record));

        return vc;
    }   
        
    @Override
    public void preprocess() throws Exception {}

    @Override
    public HADatAcThing createObject(Record rec, int rowNumber) throws Exception {
        return createVirtualColumn(rec);
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in SSDGenerator: " + e.getMessage();
    }

    @Override
    public String getTableName() {
        // TODO Auto-generated method stub
        return null;
    }
}
