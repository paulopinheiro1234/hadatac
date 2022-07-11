package org.hadatac.data.loader;

import java.lang.String;

import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.VirtualColumn;


public class VirtualColumnGenerator extends BaseGenerator {

    public VirtualColumnGenerator(DataFile dataFile) {
        super(dataFile);
    }

    @Override
    public void initMapping() {
        mapCol.clear();
        mapCol.put("sheet", "sheet");
        mapCol.put("typeUri", "type");
        mapCol.put("hascoTypeUri", "hascoType");
        mapCol.put("hasSOCReference", "hasSOCReference");
        mapCol.put("studyUri", "isMemberOf");
        mapCol.put("groundingLabel", "groundingLabel");
    }

    private String getTypeUri(Record rec) {
        return rec.getValueByColumnName(mapCol.get("typeUri"));
    }

    private String getStudyUri(Record rec) {
//        return rec.getValueByColumnName(mapCol.get("studyUri"));
        String studyUri = URIUtils.replacePrefixEx(rec.getValueByColumnName(mapCol.get("studyUri")));
        return (studyUri);
    }

    private String getSOCReference(Record rec) {
        String ref = rec.getValueByColumnName(mapCol.get("hasSOCReference"));
        return ref.trim().replace(" ", "");
    }

    private String getGroundingLabel(Record rec) {
        return rec.getValueByColumnName(mapCol.get("groundingLabel"));
    }

    public VirtualColumn createVirtualColumn(Record record) throws Exception {
  
        // Skip if type URI, study URI and SOC Reference are all blank
    	String typeUri = this.getTypeUri(record);
        this.studyUri = this.getStudyUri(record);
        String SOCReference = this.getSOCReference(record);
        //System.out.println("createVirtualColumn: typeUri [" + typeUri + "] StudyUri [" + studyUri + "] SOCReference [" + SOCReference + "]");
        if ((typeUri == null || typeUri.equals("")) && 
        	(this.studyUri == null || this.studyUri.equals("")) && 
        	(SOCReference == null || SOCReference.equals(""))) {
        	return null;
        }
        
        // Skip the study row in the SSD sheet
        if (typeUri.equals("hasco:Study")) {
            return null;
        }
    	
        // generate error if only study URI is missing
        if (this.studyUri == null || this.studyUri.equals("")) {
            logger.printExceptionByIdWithArgs("SSD_00003", this.getTypeUri(record));
            return null;
        }
            
        // generate error if only SOC reference is missing
        if (SOCReference == null || SOCReference.equals("")) {
            logger.printException("SSD_00004");
            return null;
        }
            
        VirtualColumn vc = new VirtualColumn(
                getStudyUri(record),
                getGroundingLabel(record),
                SOCReference);

        return vc;
    }   
        
    @Override
    public void preprocess() throws Exception {}

    @Override
    public HADatAcThing createObject(Record rec, int rowNumber, String selector) throws Exception {
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
