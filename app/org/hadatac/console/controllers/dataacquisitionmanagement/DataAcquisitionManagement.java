package org.hadatac.console.controllers.dataacquisitionmanagement;

import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionmanagement.routes;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.ResumableUpload;
import org.hadatac.console.models.DataAcquisitionForm;
import org.hadatac.console.models.SysUser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.FormFactory;

import org.hadatac.console.views.html.dataacquisitionmanagement.*;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.entity.pojo.User;
import org.hadatac.entity.pojo.UserGroup;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.State;
import org.hadatac.utils.Templates;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DataAcquisitionManagement extends Controller {

    @Inject
    private FormFactory formFactory;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(int stateId) {
        List<STR> results = null;
        State state = new State(stateId);
        final SysUser user = AuthApplication.getLocalUser(session());
        if (user.isDataManager()) {
            results = STR.findAll(state);
        } else {
            String ownerUri = UserManagement.getUriByEmail(user.getEmail());
            results = STR.find(ownerUri, state);
        }

        for (STR dataAcquisition : results) {
            dataAcquisition.setSchemaUri(URIUtils.replaceNameSpaceEx(dataAcquisition.getSchemaUri()));
        }
        results.sort(new Comparator<STR>() {
            @Override
            public int compare(STR lhs, STR rhs) {
                return lhs.getUri().compareTo(rhs.getUri());
            }
        });

        return ok(dataAcquisitionManagement.render(state, results, user.isDataManager()));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(int stateId) {
        return index(stateId);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result newDataAcquisition() {

        final SysUser sysUser = AuthApplication.getLocalUser(session());

        Map<String, String> nameList = new HashMap<String, String>();
        List<User> groups = UserGroup.find();
        for (User group : groups) {
            nameList.put(group.getUri(), group.getName());
        }
        
        for (String uri : User.getUserURIs()) {
            nameList.put(uri, uri);
        }

        return ok(newDataAcquisition.render(
        		Study.find(),
        		DataAcquisitionSchema.findAll(),
                Deployment.find(new State(State.ACTIVE)),
                nameList,
                User.getUserEmails(), 
                sysUser.isDataManager()));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postNewDataAcquisition() {
        return newDataAcquisition();
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processForm() {
        final SysUser sysUser = AuthApplication.getLocalUser(session());

        Form<DataAcquisitionForm> form = formFactory.form(DataAcquisitionForm.class).bindFromRequest();
        DataAcquisitionForm data = form.get();

        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }
        if (null != STR.findByUri(data.getNewDataAcquisitionUri())) {
            return badRequest("Data acquisition with this uri already exists!");
        }

        STR da = new STR();
        da.setUri(data.getNewDataAcquisitionUri());
        da.setNumberDataPoints(0);
        da.setSchemaUri(data.getNewSchema());
        da.setTriggeringEvent(TriggeringEvent.INITIAL_DEPLOYMENT);
        da.setParameter(data.getNewParameter());
        if (sysUser.isDataManager()) {
            da.setOwnerUri(data.getNewOwner());
        }
        da.setPermissionUri(data.getNewPermission());
        DateTimeFormatter isoFormat = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm a");
        da.setStartedAt(isoFormat.parseDateTime(data.getNewStartDate()));
        da.save();

        return redirect(routes.DataAcquisitionManagement.index(State.ACTIVE));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result generateSTRFileFromForm(String dir) {
        final SysUser sysUser = AuthApplication.getLocalUser(session());

        Form<DataAcquisitionForm> form = formFactory.form(DataAcquisitionForm.class).bindFromRequest();
        DataAcquisitionForm data = form.get();

        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        String pathUnproc = ConfigProp.getPathUnproc();

        String filename = "STR-" + data.getDaName() + ".csv";
        DataFile dataFile = DataFile.findByNameAndStatus(filename, DataFile.UNPROCESSED);
        if (dataFile != null && dataFile.existsInFileSystem(pathUnproc)) {
            return badRequest(
                    "<a style=\"color:#cc3300; font-size: x-large;\">A file with this name already exists!</a>");
        }

        File file = new File(pathUnproc + "/" + filename);
        // Create headers
        List<String> headers = new ArrayList<String>();
        List<String> row = new ArrayList<String>();

        headers.add(Templates.DASTUDYID);
        row.add(data.getStudyId());

        headers.add(Templates.DATAACQUISITIONNAME);
        row.add(data.getDaName());
        
        headers.add(Templates.DATADICTIONARYNAME);
        row.add(data.getSddName());
        
        headers.add(Templates.DEPLOYMENTURI);
        row.add(data.getDeploymentUri());
        
        headers.add(Templates.CELLSCOPE);
        row.add(data.getCellScope());
        
        headers.add(Templates.OWNEREMAIL);
        row.add(data.getOwnerEmail());
        
        headers.add(Templates.PERMISSIONURI);
        row.add(data.getPermissionUri());
        
        try {
            FileUtils.writeStringToFile(file, String.join(",", headers) + "\n", "utf-8", true);
            FileUtils.writeStringToFile(file, String.join(",", row) + "\n", "utf-8", true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataFile = DataFile.create(filename, "", AuthApplication.getLocalUser(session()).getEmail(), DataFile.FREEZED);

        return redirect(org.hadatac.console.controllers.annotator.routes.AutoAnnotator.index(dir, "."));
    }
}
