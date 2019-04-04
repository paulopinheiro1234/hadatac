package org.hadatac.data.loader;

import java.lang.String;

import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.VirtualColumn;


public class VirtualColumnGenerator extends BaseGenerator {

    final String kbPrefix = ConfigProp.getKbPrefix();

    public VirtualColumnGenerator(DataFile dataFile) {
        super(dataFile);
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
        return ref.trim().replace(" ", "");
    }

    private String getGroundingLabel(Record rec) {
        return rec.getValueByColumnName(mapCol.get("groundingLabel"));
    }

    public VirtualColumn createVirtualColumn(Record record) throws Exception {
  
        // Skip the study row in the SSD sheet
        //System.out.println("VirtualColumnGenerator: row's type: [" + this.getTypeUri(record) + "]   [" + URIUtils.replacePrefix("hasco:Study") + "]");
        if (this.getTypeUri(record).equals("hasco:Study")) {
            return null;
        }
        
        this.studyUri = getStudyUri(record);
        if (this.studyUri == null || this.studyUri.equals("")) {
            logger.printExceptionByIdWithArgs("SSD_00003", this.getTypeUri(record));
            return null;
        }
            
        String SOCReference = getSOCReference(record);
        if (SOCReference == null || SOCReference.equals("")) {
            logger.printException("SSD_00004");
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
