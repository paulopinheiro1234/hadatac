package org.hadatac.console.controllers.dataacquisitionmanagement;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import play.data.FormFactory;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionmanagement.routes;
import org.hadatac.console.models.DataAcquisitionForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.dataacquisitionmanagement.*;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.entity.pojo.User;
import org.hadatac.entity.pojo.UserGroup;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class EditDataAcquisition extends Controller {

    @Inject
    private FormFactory formFactory;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String uri, boolean bChangeParam) {

        final SysUser sysUser = AuthApplication.getLocalUser(session());
        try {
            if (uri != null) {
                uri = URLDecoder.decode(uri, "UTF-8");
            } else {
                uri = "";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (!uri.equals("")) {
            STR dataAcquisition = STR.findByUri(uri);
            if (null == dataAcquisition) {
                return badRequest("Invalid data acquisition URI!");
            }

            Map<String, String> nameList = new HashMap<String, String>();
            User user = User.find(dataAcquisition.getOwnerUri());
            if(null != user){
                if(user.getUri() != dataAcquisition.getPermissionUri()){
                    nameList.put(user.getUri(), user.getName());
                }
                List<User> groups = UserGroup.find();
                for (User group : groups) {
                    nameList.put(group.getUri(), group.getName());
                }
            }

            Map<String, String> mapSchemas = new HashMap<String, String>();
            List<DataAcquisitionSchema> schemas = DataAcquisitionSchema.findAll();
            for (DataAcquisitionSchema schema : schemas) {
                mapSchemas.put(schema.getUri(), URIUtils.replaceNameSpaceEx(schema.getUri()));
            }

            return ok(editDataAcquisition.render(dir, filename, dataAcquisition, nameList, 
                    User.getUserURIs(), mapSchemas, sysUser.isDataManager(), bChangeParam));
        }

        return badRequest("Invalid data acquisition URI!");
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String uri, boolean bChangeParam) {
        return index(dir, filename, uri, bChangeParam);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processForm(String dir, String filename, String acquisitionUri, boolean bChangeParam) {
        final SysUser sysUser = AuthApplication.getLocalUser(session());

        Form<DataAcquisitionForm> form = formFactory.form(DataAcquisitionForm.class).bindFromRequest();
        DataAcquisitionForm data = form.get();
        List<String> changedInfos = new ArrayList<String>();

        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        STR da = STR.findByUri(acquisitionUri);
        if (null != data.getNewDataAcquisitionUri()) {
            if (!data.getNewDataAcquisitionUri().equals("")) {
                if (null != STR.findByUri(data.getNewDataAcquisitionUri())) {
                    return badRequest("Data acquisition with this uri already exists!");
                }

                // Create new data acquisition
                da.setUri(data.getNewDataAcquisitionUri());
                da.setNumberDataPoints(0);
                da.setTriggeringEvent(TriggeringEvent.CHANGED_CONFIGURATION);
                da.setParameter(data.getNewParameter());

                DateTimeFormatter isoFormat = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm a");
                da.setStartedAt(isoFormat.parseDateTime(data.getNewStartDate()));

                if (!data.getNewEndDate().equals("")) {
                    isoFormat = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm a");
                    da.setEndedAt(isoFormat.parseDateTime(data.getNewEndDate()));
                }  

                da.saveToSolr();

                return ok(main.render("Results,", "", new Html("<h3>" 
                        + String.format("Content have been inserted in Table \"DataAcquisition\"") 
                        + "</h3>")));
            }
        }

        // Update current data acquisition
        if (bChangeParam) {
            if (da.getParameter() == null || !da.getParameter().equals(data.getNewParameter())) {
                da.setParameter(data.getNewParameter());
                changedInfos.add(data.getNewParameter());
            }
        }
        else {
            if (sysUser.isDataManager()) {
                if (da.getOwnerUri() == null || !da.getOwnerUri().equals(data.getNewOwner())) {
                    da.setOwnerUri(data.getNewOwner());
                    changedInfos.add(data.getNewOwner());
                }
            }
            if (da.getPermissionUri() == null || !da.getPermissionUri().equals(data.getNewPermission())) {
                da.setPermissionUri(data.getNewPermission());
                changedInfos.add(data.getNewPermission());
            }
            if (da.getSchemaUri() == null || !da.getSchemaUri().equals(data.getNewSchema())) {
                da.setSchemaUri(data.getNewSchema());
                changedInfos.add(data.getNewSchema());
            }
        }

        if (!changedInfos.isEmpty()) {
            da.save();
        }

        return ok(editDataAcquisitionConfirm.render(dir, filename, da, changedInfos, sysUser.isDataManager()));
    }
}
