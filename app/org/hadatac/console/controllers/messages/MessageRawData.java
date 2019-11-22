package org.hadatac.console.controllers.messages;

import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.ResumableUpload;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
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
import org.hadatac.data.loader.mqtt.Subscribe;
import org.hadatac.entity.pojo.MessageStream;
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

public class MessageRawData extends Controller {

	Map<String, MessageStream> streamCache = new HashMap<String, MessageStream>();
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	String results = null;
    	String uri = null;
    	try {
    		uri = URLDecoder.decode(stream_uri, "UTF-8");
    	} catch (Exception e) {
    		results = "Error decoding stream uri";
    	}
    	if (uri != null) {
    		MessageStream stream = null;
    		if (streamCache.containsKey(uri)) {
    			stream = streamCache.get(uri);
    		} else {
    			stream = MessageStream.find(uri);
    			if (stream != null) {
    				streamCache.put(uri, stream);
    			}
    		}
    		if (stream == null) {
    			results = "Could not find Stream [" + uri + "]";
    		} else {
    			results = "Found [" + uri + "] at " + stream.getIP() + ":" + stream.getPort() + "\n";
    			results = results + Subscribe.exec(stream);
    		}
    	}
        return ok(messageRawData.render(dir, filename, da_uri, offset, stream_uri, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return index(dir, filename, da_uri, offset, stream_uri);
    }


}
