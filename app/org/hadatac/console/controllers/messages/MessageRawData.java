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
import org.hadatac.data.loader.mqtt.MqttSubscribe;
import org.hadatac.entity.pojo.MessageTopic;
import org.hadatac.entity.pojo.STR;
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

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String da_uri, int offset, String stream_uri, String topic_uri, String postAction, boolean show) {
		STR stream = null;
		MessageTopic topic = null;
    	List<String> results = new ArrayList<String>();
    	String str_uri = null;
    	String tpc_uri = null;
    	String topic_label = null;
    	if (postAction == null) {
    		postAction = routes.MessageManagement.index(dir, filename, da_uri, offset, show).url();
    	}
    	if (stream_uri == null) {
    		results.add("No stream URI provided");
            return ok(messageRawData.render(dir, filename, da_uri, offset, "", "", "", results, postAction, show));
    	}
    	try {
    		str_uri = URLDecoder.decode(stream_uri, "UTF-8");
    		stream = STR.findByUri(str_uri);
    	} catch (Exception e) {
    		str_uri = "";
    		results.add("Error decoding/loading stream uri");
    	}
    	if (topic_uri == null) {
    		tpc_uri = "";
    	} else {
    		try {
    			tpc_uri = URLDecoder.decode(topic_uri, "UTF-8");
        		topic = MessageTopic.find(tpc_uri);
    		} catch (Exception e) {
    			tpc_uri = "";
    			results.add("Error decoding/loading topic uri");
    		}
    	}
		if (stream == null) {
			results.add("Could not find stream");
		} else if (topic == null) {
			results.add("Found Stream at " + stream.getMessageIP() + ":" + stream.getMessagePort());
			List<String> tempList = MqttSubscribe.exec(stream, null, MqttSubscribe.SUBSCRIBE_BATCH);
			System.out.println("TEMPLIST SIZE = " + tempList.size());
			results.addAll(tempList);
		} else {
			results.add("Found Stream at " + stream.getMessageIP() + ":" + stream.getMessagePort() + " with topic " + stream.getLabel());
			results.add("Found Topic " + topic.getLabel());
			topic_label = topic.getLabel();
			results.addAll(MqttSubscribe.exec(stream, topic, MqttSubscribe.SUBSCRIBE_BATCH));
		}
        return ok(messageRawData.render(dir, filename, da_uri, offset, str_uri, tpc_uri, topic_label, results, postAction, show));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String da_uri, int offset, String stream_uri, String topic_uri, String postAction, boolean show) {
        return index(dir, filename, da_uri, offset, stream_uri, topic_uri, postAction, show);
    }

}
