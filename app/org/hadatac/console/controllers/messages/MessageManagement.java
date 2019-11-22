package org.hadatac.console.controllers.messages;

import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.ResumableUpload;

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

import org.hadatac.console.views.html.messages.*;
import org.hadatac.entity.pojo.ObjectAccessSpec;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.MessageStream;
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

public class MessageManagement extends Controller {

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String da_uri, int offset) {
        List<MessageStream> results = MessageStream.find();
        return ok(messageManagement.render(dir, filename, da_uri, offset, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String da_uri, int offset) {
        return index(dir, filename, da_uri, offset);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result start(String dir, String filename, String da_uri, int offset, String stream_uri) {

        List<MessageStream> results = MessageStream.find();
    	return ok(messageManagement.render(dir, filename, da_uri, offset, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postStart(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return start(dir, filename, da_uri, offset, stream_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result stop(String dir, String filename, String da_uri, int offset, String stream_uri) {

        List<MessageStream> results = MessageStream.find();
    	return ok(messageManagement.render(dir, filename, da_uri, offset, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postStop(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return start(dir, filename, da_uri, offset, stream_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result view(String dir, String filename, String da_uri, int offset, String stream_uri) {

        List<MessageStream> results = MessageStream.find();
    	return ok(messageManagement.render(dir, filename, da_uri, offset, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postView(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return start(dir, filename, da_uri, offset, stream_uri);
    }

}
