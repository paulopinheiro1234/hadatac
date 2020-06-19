package org.hadatac.console.controllers.annotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLEncoder;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import javax.inject.Inject;

import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.State;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.console.models.AssignOptionForm;
import org.hadatac.console.models.SelectScopeForm;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.annotator.FileProcessing;
import org.hadatac.console.controllers.annotator.routes;
import org.hadatac.console.models.SysUser;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.URIUtils;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.data.Form;
import play.mvc.*;
import play.mvc.Result;
import play.data.FormFactory;

public class PrepareIngestion extends Controller {

    @Inject
    private FormFactory formFactory;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result create(String dir, String fileId, String da_uri) {
        final String kbPrefix = ConfigProp.getKbPrefix();
        String ownerEmail = AuthApplication.getLocalUser(session()).getEmail();
        STR da = null;

        DataFile dataFile = DataFile.findByIdAndEmail(fileId, ownerEmail);
        if (dataFile == null) {
            return badRequest("[ERROR] Could not update file records with new DA information");
        }
        
        System.out.println("DataFile's Dataset URI : [" + dataFile.getDatasetUri() + "]");

        // Load associated DA
        if (da_uri != null && !da_uri.equals("")) {
            da = STR.findByUri(URIUtils.replacePrefixEx(da_uri));
            //System.out.println("Row scope: [" + da.getRowScopeUri() + "]  hasScope: " + da.hasScope());

            if (da != null) {
                return ok(prepareIngestion.render(dir, fileId, da, "DA associated with file has been retrieved"));
            } else {
                String message = "[ERROR] Could not load assigned DA from DA's URI : " + da_uri;
                return badRequest(message);
            }
        }

        // OR create a new DA if the file is not associated with any existing DA
        
        String da_label = "";
        String new_da_uri = "";

        if (!dataFile.getPureFileName().startsWith("DA-")) {
            da_label = "DA-" + dataFile.getPureFileName();
        } else {
            da_label = dataFile.getPureFileName();
        }
        da_label = da_label.replace(".csv","").replace(".","").replace("+","-");
        new_da_uri = kbPrefix + da_label;

        da = new STR();
        da.setTriggeringEvent(TriggeringEvent.INITIAL_DEPLOYMENT);
        da.setLabel(da_label);
        da.setUri(URIUtils.replacePrefixEx(new_da_uri));

        SysUser user = SysUser.findByEmail(ownerEmail);
        if (user == null) {
            System.out.println("The specified owner email " + ownerEmail + " is not a valid user!");
        } else {
            da.setOwnerUri(user.getUri());
            da.setPermissionUri(user.getUri());
        }

        da.saveToSolr();

        dataFile.setDataAcquisitionUri(da.getUri());
        dataFile.save();

        return ok(prepareIngestion.render(dir, fileId, da, "New data acquisition has been created to support file ingestion"));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postCreate(String dir, String fileId, String da_uri) {
        return create(dir, fileId, da_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result reconfigure(String dir, String fileId, String da_uri) {
        STR dataAcquisition = STR.findByUri(da_uri);
        if (null != dataAcquisition) {
            dataAcquisition.setStatus(0);
            dataAcquisition.save();
        }
        return create(dir, fileId, da_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result refine(String dir, String fileId, String da_uri, String message) {

        STR da = null;

        // Load associated DA
        if (da_uri != null && !da_uri.equals("")) {
            da = STR.findByUri(da_uri);
            if (da != null) {
                return ok(prepareIngestion.render(dir, fileId, da, message));
            } else {
                System.out.println("[ERROR] Could not load assigned DA from DA's URI");
            }
        }
        return badRequest("[ERROR] In PrepareIngestion.refine, cannot retrieve DA from provided URI");
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postRefine(String dir, String fileId, String da_uri, String message) {
        return refine(dir, fileId, da_uri, message);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result selectStudy(String dir, String fileId, String da_uri) {

        List<Study> studies = Study.find();

        return ok(selectStudy.render(dir, fileId, da_uri, studies));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result selectScope(String dir, String fileId, String da_uri, String std_uri) {

        String[] fields = null;
        String rowScope = null;
        String rowScopeUri = null;
        List<String> cellScope = null;
        List<String> cellScopeUri = null;
        String labels = "";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(DataFile.findById(fileId).getAbsolutePath()));
            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                builder.append(line);
                break;
            }
            if(!builder.toString().trim().equals("")) {
                labels = builder.toString();
            }
        } catch (Exception e) {
            System.out.println("Could not process uploaded file.");
        }
        System.out.println("selectScope: labels = <" + labels + ">");
        if (labels != null && !labels.equals("")) {
            fields = FileProcessing.extractFields(labels);
            cellScope = new ArrayList<String>();
            cellScopeUri = new ArrayList<String>();
            for (String str : fields) {
                cellScope.add("no mapping");
                cellScopeUri.add("");
            }
            System.out.println("# of fields: " + fields.length);
        }

        Study study = Study.find(std_uri);
        System.out.println("Study uri: " + study.getUri());
        System.out.println("Study name: " + study.getLabel());
        List<ObjectCollection> ocList = ObjectCollection.findDomainByStudyUri(std_uri);
        System.out.println("Collection list size: " + ocList.size());

        return ok(selectScope.render(dir, fileId, da_uri, ocList, Arrays.asList(fields), 
                rowScope, rowScopeUri, cellScope, cellScopeUri));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result selectDeployment(String dir, String fileId, String da_uri) {

        State active = new State(State.ACTIVE);

        List<Deployment> deployments = Deployment.find(active);

        return ok(selectDeployment.render(dir, fileId, da_uri, deployments));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result selectSchema(String dir, String fileId, String da_uri) {

        List<DataAcquisitionSchema> schemas = DataAcquisitionSchema.findAll();

        return ok(selectSchema.render(dir, fileId, da_uri, schemas));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processSelectStudy(String dir, String fileId, String da_uri) {
        Form<AssignOptionForm> form = formFactory.form(AssignOptionForm.class).bindFromRequest();
        String message = "";
        AssignOptionForm data = form.get();
        String std_uri = data.getOption();
        //System.out.println("Showing returned option: " + std_uri);

        if (std_uri != null && !std_uri.equals("")) {

            Study std = Study.find(std_uri);
            if (std == null) {
                message = "ERROR - Could not retrieve study from its URI.";
                return refine(dir, fileId, da_uri, message);
            }

            STR da = STR.findByUri(da_uri);
            if (da == null) {
                message = "ERROR - Could not retrieve Stream Specification from its URI.";
                return refine(dir, fileId, da_uri, message);
            }

            da.setStudyUri(std_uri);

            da.saveToSolr();

            return ok(prepareIngestion.render(dir, fileId, da, "Updated Stream Specification with deployment information"));
        }

        message = "DA is now associated with study " + std_uri;
        return refine(dir, fileId, da_uri, message);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processSelectScope(String dir, String fileId, String da_uri) {
        Form<SelectScopeForm> form = formFactory.form(SelectScopeForm.class).bindFromRequest();
        String message = "";
        SelectScopeForm data = form.get();
        String rowScopeUri = data.getNewRowScopeUri();
        String cellScopeUri = data.getNewCellScopeUri();
        System.out.println("Showing returned RowScope: [" + rowScopeUri + "]");
        System.out.println("Showing returned CellScope: [" + cellScopeUri + "]");
        String[] cellUriStr = null;
        List<String> cellScopeUriList = new ArrayList<String>();
        if (cellScopeUri != null) {
            cellUriStr = cellScopeUri.split(",");
            System.out.println("List of cell scope uris:");
            for (int i=0; i < cellUriStr.length; i++) {
                cellUriStr[i] = cellUriStr[i].trim();
                System.out.println("cell scope: [" + cellUriStr[i] + "]");
            }
            cellScopeUriList = Arrays.asList(cellUriStr);
        }

        STR da = STR.findByUri(da_uri);
        if (da == null) {
            message = "ERROR - Could not retrieve Stream Specification from its URI.";
            return refine(dir, fileId, da_uri, message);
        }

        //da.setRowScopeUri(rowScopeUri);
        da.setCellScopeUri(cellScopeUriList);

        da.saveToSolr();

        return ok(prepareIngestion.render(dir, fileId, da, "Updated Stream Specification with scope information"));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processSelectDeployment(String dir, String fileId, String da_uri) {
        Form<AssignOptionForm> form = formFactory.form(AssignOptionForm.class).bindFromRequest();
        String message = "";
        AssignOptionForm data = form.get();
        String dep_uri = data.getOption();
        //System.out.println("Showing returned option: " + dep_uri);

        if (dep_uri != null && !dep_uri.equals("")) {

            Deployment dep = Deployment.find(dep_uri);
            if (dep == null) {
                message = "ERROR - Could not retrieve Deployment from its URI.";
                return refine(dir, fileId, da_uri, message);
            }

            STR da = STR.findByUri(da_uri);
            if (da == null) {
                message = "ERROR - Could not retrieve Stream Specification from its URI.";
                return refine(dir, fileId, da_uri, message);
            }

            da.setDeploymentUri(dep_uri);

            da.saveToSolr();
            
            return ok(prepareIngestion.render(dir, fileId, da, "Updated Stream Specification with deployment information"));
        }

        message = "DA is now associated with deployment " + dep_uri;
        return refine(dir, fileId, da_uri, message);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processSelectSchema(String dir, String fileId, String da_uri) {
        Form<AssignOptionForm> form = formFactory.form(AssignOptionForm.class).bindFromRequest();
        String message = "";
        AssignOptionForm data = form.get();
        String das_uri = data.getOption();
        //System.out.println("Showing returned option: " + das_uri);

        if (das_uri != null && !das_uri.equals("")) {

            DataAcquisitionSchema das = DataAcquisitionSchema.find(das_uri);
            if (das == null) {
                message = "ERROR - Could not retrieve Stream Specification Schema from its URI.";
                return refine(dir, fileId, da_uri, message);
            }

            STR da = STR.findByUri(da_uri);
            if (da == null) {
                message = "ERROR - Could not retrieve Stream Specification from its URI.";
                return refine(dir, fileId, da_uri, message);
            }

            da.setSchemaUri(das_uri);

            da.saveToSolr();
            
            return ok(prepareIngestion.render(dir, fileId, da, "Updated Stream Specification with data acquisition schema information"));
        }

        message = "DA is now associated with data acquisition schema " + das_uri;
        return refine(dir, fileId, da_uri, message);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result removeAssociation(String dir, String fileId, String da_uri, String daComponent) {

        String message = "";
        STR da = STR.findByUri(da_uri);
        if (da == null) {
            message = "ERROR - Could not retrieve Stream Specification from its URI.";
            return refine(dir, fileId, da_uri, message);
        }

        switch (daComponent) {

        // removing a study's relationship also removes scope information
        case "Study":  
            da.setStudyUri("");
            //da.setRowScopeUri("");
            //da.setRowScopeName("");
            da.setCellScopeUri(new ArrayList<String>());
            da.setCellScopeName(new ArrayList<String>());
            break;

        case "Scope":  
            //da.setRowScopeUri("");
            //da.setRowScopeName("");
            da.setCellScopeUri(new ArrayList<String>());
            da.setCellScopeName(new ArrayList<String>());
            break;

        case "Deployment":  
            da.setDeploymentUri("");
            break;

        case "Schema":  
            da.setSchemaUri("");
        }

        da.saveToSolr();
            
        message = "Association with " + daComponent + " removed from the Stream Specification.";
        return ok(prepareIngestion.render(dir, fileId, da, message));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result completeDataAcquisition(String dir, String fileId, String da_uri) {
        String message = "";
        STR da = STR.findByUri(da_uri);
        if (da == null) {
            message = "ERROR - Could not retrieve Stream Specification from its URI.";
            return refine(dir, fileId, da_uri, message);
        }

        da.setStatus(9999);
        
        da.saveToSolr();
        
        message = "Stream Specification set as complete";
        return ok(prepareIngestion.render(dir, fileId, da, message));
    }
}


