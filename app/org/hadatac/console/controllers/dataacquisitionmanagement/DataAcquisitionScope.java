package org.hadatac.console.controllers.dataacquisitionmanagement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.annotator.FileProcessing;
import org.hadatac.console.controllers.dataacquisitionmanagement.routes;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.dataacquisitionmanagement.*;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DataAcquisitionScope extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result create(String dir, String fileId, String da_uri) {

        STR da = null;
        String ownerEmail = AuthApplication.getLocalUser(session()).getEmail();

        // Load associated DA
        DataFile dataFile = DataFile.findByIdAndEmail(fileId, ownerEmail);
        if (dataFile == null) {
            return badRequest("[ERROR] Could not update file records with new DA information");
        }

        // Load associated DA
        if (da_uri != null && !da_uri.equals("")) {
            da = STR.findByUri(URIUtils.replacePrefixEx(da_uri));

            if (da == null) {
                String message = "[ERROR] Could not load assigned DA from DA's URI : " + da_uri;
                return badRequest(message);
            }
        }

        String[] fields = null;
        String globalScope = null;
        String globalScopeUri = null;
        List<String> localScope = null;
        List<String> localScopeUri = null;
        String labels = "";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(dataFile.getAbsolutePath()));
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
            localScope = new ArrayList<String>();
            localScopeUri = new ArrayList<String>();
            for (String str : fields) {
                localScope.add("no mapping");
                localScopeUri.add("");
            }
            //System.out.println("# of fields: " + fields.length);
        }

        List<ObjectCollection> ocList = ObjectCollection.findDomainByStudyUri(da.getStudyUri());
        //System.out.println("Collection list size: " + ocList.size());

        return ok(editScope.render(dir, fileId, da_uri, ocList, Arrays.asList(fields), globalScope, globalScopeUri, localScope, localScopeUri));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postCreate(String dir, String fileId, String da_uri) {
        return create(dir, fileId, da_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result view(String dir, String fileId, String da_uri) {

        STR da = null;
        
        // Load associated DA
        String ownerEmail = AuthApplication.getLocalUser(session()).getEmail();
        DataFile dataFile = DataFile.findByIdAndEmail(fileId, ownerEmail);
        if (dataFile == null) {
            return badRequest("[ERROR] Could not update file records with new DA information");
        }

        // Load associated DA
        if (da_uri != null && !da_uri.equals("")) {
            da = STR.findByUri(URIUtils.replacePrefixEx(da_uri));

            if (da == null) {
                String message = "[ERROR] Could not load assigned DA from DA's URI : " + da_uri;
                return badRequest(message);
            }
        }

        List<String> cellScopeUri = da.getCellScopeUri();
        System.out.println("Size Cell Scope URI: " + cellScopeUri.size());
        for (String str : cellScopeUri) {
            System.out.println("  - uri : " + str);
        }
        List<String> cellScopeName = da.getCellScopeName();
        System.out.println("Size Cell Scope URI: " + cellScopeName.size());
        for (String str : cellScopeName) {
            System.out.println("  - name : " + str);
        }

        return ok(viewScope.render(dir, fileId, da_uri, cellScopeName, cellScopeUri));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postView(String dir, String fileId, String da_uri) {
        return view(dir, fileId, da_uri);
    }

}
