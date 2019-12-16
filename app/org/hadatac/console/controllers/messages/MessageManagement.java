package org.hadatac.console.controllers.messages;

import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.ResumableUpload;

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
import org.hadatac.data.loader.mqtt.MessageAnnotation;
import org.hadatac.data.loader.mqtt.MessageAnnotationSubscribe;
import org.hadatac.data.loader.mqtt.MessageWorker;
import org.hadatac.data.loader.mqtt.Subscribe;
import org.hadatac.entity.pojo.ObjectAccessSpec;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.MessageStream;
import org.hadatac.entity.pojo.MessageTopic;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.entity.pojo.User;
import org.hadatac.entity.pojo.UserGroup;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
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
    	String clientId = "";
    	if (MessageWorker.getInstance() != null && MessageWorker.getInstance().currentClient != null) {
    		clientId = MessageWorker.getInstance().currentClient.getClientId();
    	}        return ok(messageManagement.render(dir, filename, da_uri, offset, clientId, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String da_uri, int offset) {
        return index(dir, filename, da_uri, offset);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result initiate(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	
    	String uri = null;
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
        MessageStream stream = MessageStream.find(uri);
        if (stream != null) {
            MessageAnnotation.initiateMessageStream(stream);
        }
        
        List<MessageStream> results = MessageStream.find();
    	String clientId = "";
    	if (MessageWorker.getInstance() != null && MessageWorker.getInstance().currentClient != null) {
    		clientId = MessageWorker.getInstance().currentClient.getClientId();
    	}    	return ok(messageManagement.render(dir, filename, da_uri, offset, clientId, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postInitiate(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return initiate(dir, filename, da_uri, offset, stream_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result subscribe(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	
    	String uri = null;
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
        MessageStream stream = MessageStream.find(uri);
        if (stream != null) {
            MessageAnnotation.subscribeMessageStream(stream);
        }
        
        List<MessageStream> results = MessageStream.find();
    	String clientId = "";
    	if (MessageWorker.getInstance() != null && MessageWorker.getInstance().currentClient != null) {
    		clientId = MessageWorker.getInstance().currentClient.getClientId();
    	}    	return ok(messageManagement.render(dir, filename, da_uri, offset, clientId, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postSubscribe(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return subscribe(dir, filename, da_uri, offset, stream_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result unsubscribe(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	
    	String uri = null;
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
        MessageStream stream = MessageStream.find(uri);
        if (stream != null) {
            MessageAnnotation.unsubscribeMessageStream(stream);
        }
        
        List<MessageStream> results = MessageStream.find();
    	String clientId = "";
    	if (MessageWorker.getInstance() != null && MessageWorker.getInstance().currentClient != null) {
    		clientId = MessageWorker.getInstance().currentClient.getClientId();
    	}    	return ok(messageManagement.render(dir, filename, da_uri, offset, clientId, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postUnsubscribe(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return unsubscribe(dir, filename, da_uri, offset, stream_uri);
    }

    public static void stopStream(MessageStream stream) {
        if (stream != null) {
            MessageAnnotation.stopMessageStream(stream);
        }
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result stop(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	String uri = null;
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}

    	MessageStream stream = MessageStream.find(uri);
    	stopStream(stream);

    	List<MessageStream> results = MessageStream.find();
    	String clientId = "";
    	if (MessageWorker.getInstance() != null && MessageWorker.getInstance().currentClient != null) {
    		clientId = MessageWorker.getInstance().currentClient.getClientId();
    	}    	return ok(messageManagement.render(dir, filename, da_uri, offset, clientId, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postStop(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return stop(dir, filename, da_uri, offset, stream_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result view(String dir, String filename, String da_uri, int offset, String stream_uri) {

        List<MessageStream> results = MessageStream.find();
    	String clientId = "";
    	if (MessageWorker.getInstance() != null && MessageWorker.getInstance().currentClient != null) {
    		clientId = MessageWorker.getInstance().currentClient.getClientId();
    	}    	return ok(messageManagement.render(dir, filename, da_uri, offset, clientId, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postView(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return view(dir, filename, da_uri, offset, stream_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result checkAnnotationLog(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	if (stream_uri == null) {
    		stream_uri = "";
    	}
    	String uri = null;
    	MessageStream stream = null;
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
        	System.out.println("looking log for [" + uri + "]");
            stream = MessageStream.find(uri);
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
    	if (stream == null || stream.getLogger() == null || stream.getLogger().getLog() == null) {
            return ok(annotation_log.render(Feedback.print(Feedback.WEB, ""), 
                    routes.MessageManagement.index(dir, filename, da_uri, offset).url()));
    	}
        return ok(annotation_log.render(Feedback.print(Feedback.WEB, 
                stream.getLogger().getLog()), 
                routes.MessageManagement.index(dir, filename, da_uri, offset ).url()));
    }
 
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result listTopics(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	
    	String uri = null;
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
        MessageStream stream = MessageStream.find(uri);
        
        List<MessageTopic> topics = null;
        topics = MessageTopic.findByStream(uri);
        
    	return ok(topicManagement.render(dir, filename, da_uri, offset, stream, topics));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postListTopics(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return listTopics(dir, filename, da_uri, offset, stream_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result testConnection(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	
    	String uri = null;
    	String streamLabel = "";
        List<String> results = new ArrayList<String>();
    	try {
    		uri = URLDecoder.decode(stream_uri, "utf-8");
            MessageStream stream = MessageStream.find(uri);
            if (stream != null) {
            	streamLabel = stream.getName();
                results = Subscribe.testConnection(stream);
            }
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
        
    	return ok(testConnection.render(dir, filename, da_uri, offset, uri, streamLabel, results));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postTestConnection(String dir, String filename, String da_uri, int offset, String stream_uri) {
        return testConnection(dir, filename, da_uri, offset, stream_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result testTopics(String dir, String filename, String da_uri, int offset, String stream_uri) {
    	
    	String streamLabel = "";
        List<String> results = new ArrayList<String>();
        List<String> specified = new ArrayList<String>();
    	try {
    		stream_uri = URLDecoder.decode(stream_uri, "utf-8");
            MessageStream stream = MessageStream.find(stream_uri);
            if (stream != null) {
            	streamLabel = stream.getName();
            	results = Subscribe.testTopics(stream);
            	Collections.sort(results);
            }
            List<MessageTopic> topics = MessageTopic.findByStream(stream_uri);
            if (topics != null) {
            	for (MessageTopic tpc : topics) {
            		specified.add(tpc.getLabel());
            	}
            }
    	} catch (Exception e) {
        	System.out.println("error decoding [" + stream_uri + "]");
    	}
        
    	return ok(testTopics.render(dir, filename, da_uri, offset, stream_uri, streamLabel, results, specified));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postTestTopics(String dir, String filename, String da_uri, int offset, String topic_uri) {
        return testTopics(dir, filename, da_uri, offset, topic_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result testLabels(String dir, String filename, String da_uri, int offset, String topic_uri) {
    	
    	String tpc_uri = null;
    	String streamLabel = "";
    	String topicLabel = "";
        List<String> results = new ArrayList<String>();
        List<String> specified = new ArrayList<String>();
    	try {
    		tpc_uri = URLDecoder.decode(topic_uri, "utf-8");
            MessageTopic topic = MessageTopic.find(tpc_uri);
            if (topic != null) {
            	topicLabel = topic.getLabel();
            	MessageStream stream = topic.getStream();
            	if (stream != null) {
            		streamLabel = stream.getName();
            		results = Subscribe.testLabels(stream,topic);
            	}
            	Collections.sort(results);
            	if (topic.getStreamSpec() != null && topic.getStreamSpec().getSchema() != null) {
            		DataAcquisitionSchema sdd = topic.getStreamSpec().getSchema();
            		for (DataAcquisitionSchemaAttribute dasa : sdd.getAttributes()) {
            			specified.add(dasa.getLabel().toLowerCase());
            		}
            	}
            }
    	} catch (Exception e) {
        	System.out.println("error decoding [" + topic_uri + "]");
    	}
        
    	return ok(testLabels.render(dir, filename, da_uri, offset, tpc_uri, streamLabel, topicLabel, results, specified));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postTestLabels(String dir, String filename, String da_uri, int offset, String topic_uri) {
        return testLabels(dir, filename, da_uri, offset, topic_uri);
    }

}
