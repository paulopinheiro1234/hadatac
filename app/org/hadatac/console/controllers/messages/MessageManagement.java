package org.hadatac.console.controllers.messages;

import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.ResumableUpload;
import org.hadatac.console.models.DataAcquisitionForm;
import org.hadatac.console.models.OneStringFieldForm;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
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
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.data.api.STRStore;
import org.hadatac.data.loader.mqtt.MqttMessageAnnotation;
import org.hadatac.data.loader.mqtt.MqttMessageWorker;
import org.hadatac.data.loader.mqtt.MqttSubscribe;
import org.hadatac.data.loader.http.HttpMessageAnnotation;
import org.hadatac.data.loader.http.HttpMessageWorker;
import org.hadatac.data.loader.http.HttpSubscribe;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.MessageTopic;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.entity.pojo.User;
import org.hadatac.entity.pojo.UserGroup;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ApiUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.State;
import org.hadatac.utils.Templates;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class MessageManagement extends Controller {

    @Inject
	private FormFactory formFactory;
    	
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String da_uri, int offset) {

    	// get an updated list of stream
    	List<STR> results = STRStore.getInstance().findCachedOpenStreams();
    	
    	// get an updated list of studies
        List<String> studyIdList = Study.findIds();
    	return ok(messageManagement.render(dir, filename, da_uri, offset, studyIdList, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String da_uri, int offset) {
        return index(dir, filename, da_uri, offset);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result refreshCache(String dir, String filename, String da_uri, int offset) {
    	// get an updated list of stream
    	STRStore.getInstance().refreshStore();
        return index(dir, filename, da_uri, offset);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postRefreshCache(String dir, String filename, String da_uri, int offset) {
        return refreshCache(dir, filename, da_uri, offset);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result browseTopics(String dir, String filename, String da_uri, int offset, String stream_uri) {

    	STR stream;
    	try {
    		stream_uri = URLDecoder.decode(stream_uri, "utf-8");
    		stream = STRStore.getInstance().findCachedByUri(stream_uri);
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
        	return badRequest("error decoding [" + stream_uri + "]");
    	}
        
    	return ok(topicManagement.render(dir, filename, da_uri, offset, stream));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postBrowseTopics(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return browseTopics(dir, filename, da_uri, offset, stream_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result subscribe(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	
    	// retrieve stream
    	String uri = null;
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
        	return badRequest("error decoding [" + stream_uri + "]");
    	}
    	STR stream = STRStore.getInstance().findCachedByUri(uri);

        // call operation
        if (stream != null) {
        	if (stream.getMessageProtocol() != null && stream.getMessageProtocol().equals(STR.MQTT)) {
        		MqttMessageAnnotation.subscribeMessageStream(stream);
        	} else if (stream.getMessageProtocol() != null && stream.getMessageProtocol().equals(STR.HTTP)) {
        		HttpMessageAnnotation.subscribeMessageStream(stream);
        	} else {
                return badRequest("Message Protocol is neither MQTT or HTTP");
        	}
        	//MessageAnnotationSubscribe subscription = new MessageAnnotationSubscribe();
        	//subscription.exec(stream);
        }
        
        return index(dir, filename, da_uri, offset);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postSubscribe(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return subscribe(dir, filename, da_uri, offset, stream_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result unsubscribe(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	
    	// retrieve stream
    	String uri = null;
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
    	STR stream = STRStore.getInstance().findCachedByUri(uri);

        // call action
        if (stream != null) {
        	if (stream.getMessageProtocol() != null && stream.getMessageProtocol().equals(STR.MQTT)) {
        		MqttMessageAnnotation.unsubscribeMessageStream(stream);
        	} else if (stream.getMessageProtocol() != null && stream.getMessageProtocol().equals(STR.HTTP)) {
        		HttpMessageAnnotation.unsubscribeMessageStream(stream);
        	} else {
                return badRequest("Message Protocol is neither MQTT or HTTP");
        	}
        }
        
        return index(dir, filename, da_uri, offset);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postUnsubscribe(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return unsubscribe(dir, filename, da_uri, offset, stream_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result close(String dir, String filename, String da_uri, int offset, String stream_uri) {

    	// retrieve stream
    	String uri = null;
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
    	STR stream = STRStore.getInstance().findCachedByUri(uri);

    	// call action
        if (stream != null) {
        	if (stream.getMessageProtocol() != null && stream.getMessageProtocol().equals(STR.MQTT)) {
        		MqttMessageAnnotation.closeMessageStream(stream);
        	} else if (stream.getMessageProtocol() != null && stream.getMessageProtocol().equals(STR.HTTP)) {
        		HttpMessageAnnotation.closeMessageStream(stream);
        	} else {
                return badRequest("Message Protocol is neither MQTT or HTTP");
        	}
        }

        return index(dir, filename, da_uri, offset);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postClose(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return close(dir, filename, da_uri, offset, stream_uri);
    }

    /*
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result deleteData(String dir, String filename, String da_uri, int offset, String stream_uri) {

    	// retrieve stream
    	String uri = null;
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
    	MessageStream stream = MessageStream.find(uri);

    	// call action
    	if (stream != null) {
    	}

    	return ok(deleteData.render(dir, filename, da_uri, offset, stream));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postDeleteData(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return deleteData(dir, filename, da_uri, offset, stream_uri);
    }
    */

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result checkAnnotationLog(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	if (stream_uri == null) {
    		stream_uri = "";
    	}
    	String uri = null;
    	STR stream = null;
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
        	System.out.println("looking log for [" + uri + "]");
        	stream = STRStore.getInstance().findCachedByUri(uri);
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
    	if (stream == null || stream.getMessageLogger() == null || stream.getMessageLogger().getLog() == null) {
            return ok(annotation_log.render(Feedback.print(Feedback.WEB, ""), 
                    routes.MessageManagement.index(dir, filename, da_uri, offset).url()));
    	}
        return ok(annotation_log.render(Feedback.print(Feedback.WEB, 
                stream.getMessageLogger().getLog()), 
                routes.MessageManagement.index(dir, filename, da_uri, offset).url()));
    }
 
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result testConnection(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	
    	String uri = null;
    	String streamName = "";
        List<String> results = new ArrayList<String>();
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
        	STR stream = STRStore.getInstance().findCachedByUri(uri);
            //STR stream = STR.findByUri(uri);
            if (stream != null) {
            	streamName = stream.getLabel();
                results = MqttSubscribe.testConnection(stream);
            }
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
        
    	return ok(testConnection.render(dir, filename, da_uri, offset, uri, streamName, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postTestConnection(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return testConnection(dir, filename, da_uri, offset, stream_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result testTopics(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	
    	String streamName = "";
        List<String> results = new ArrayList<String>();
        List<String> specified = new ArrayList<String>();
    	try {
    		stream_uri = URLDecoder.decode(stream_uri, "utf-8");
            //STR stream = STR.findByUri(stream_uri);
        	STR stream = STRStore.getInstance().findCachedByUri(stream_uri);
            if (stream != null) {
            	streamName = stream.getLabel();
            	results = MqttSubscribe.testTopics(stream);
            	Collections.sort(results);
            }
            List<MessageTopic> topics = stream.getTopicsList();
            if (topics != null) {
            	for (MessageTopic tpc : topics) {
            		specified.add(tpc.getLabel());
            	}
            }
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
        
    	return ok(testTopics.render(dir, filename, da_uri, offset, stream_uri, streamName, results, specified));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postTestTopics(String dir, String filename, String da_uri, int offset, String topic_uri) {
        return testTopics(dir, filename, da_uri, offset, topic_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result testLabels(String dir, String filename, String da_uri, int offset, String stream_uri, String topic_uri) {
    	
    	String tpc_uri = null;
    	String streamName = "";
    	String topicLabel = "";
        List<String> results = new ArrayList<String>();
        List<String> specified = new ArrayList<String>();
    	try {
    		tpc_uri = URLDecoder.decode(topic_uri, "utf-8");
            MessageTopic topic = MessageTopic.find(tpc_uri);
            if (topic != null) {
            	topicLabel = topic.getLabel();
            	STR stream = topic.getStream();
            	if (stream != null) {
            		streamName = stream.getLabel();
            		results = MqttSubscribe.testLabels(stream,topic);
            	}
            	Collections.sort(results);
            	if (stream != null && stream.getSchema() != null) {
            		for (DataAcquisitionSchemaAttribute dasa : stream.getSchema().getAttributes()) {
            			specified.add(dasa.getLabel().toLowerCase());
            		}
            	}
            }
    	} catch (Exception e) {
        	System.out.println("error decoding [" + topic_uri + "]");
    	}
    	return ok(testLabels.render(dir, filename, da_uri, offset, tpc_uri, streamName, topicLabel, results, specified,
    			routes.MessageManagement.index(dir, filename, da_uri, offset).url()));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postTestLabels(String dir, String filename, String da_uri, int offset, String stream_uri, String topic_uri) {
        return testLabels(dir, filename, da_uri, offset, stream_uri, topic_uri);
    }

}
