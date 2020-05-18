package org.hadatac.data.loader.http;

import java.lang.String;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import org.hadatac.data.loader.MeasurementGenerator;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.DataFile;

public class HttpMessageAnnotation {
	
	public HttpMessageAnnotation() {}

    /********************************************************************************
     *                            STREAM MANAGEMENT                                 *
     ********************************************************************************/
    
    public static void subscribeMessageStream(STR stream) {
    	if (stream == null || !stream.getMessageStatus().equals(STR.SUSPENDED)) {
    		return;
    	}    	
		stream.getMessageLogger().resetLog();
		stream.getMessageLogger().println(String.format("Subscribing message stream: %s", stream.getMessageName()));
    	DataFile archive;
    	if (stream.getMessageArchiveId() == null || stream.getMessageArchiveId().isEmpty()) {
            Date date = new Date();
    		String fileName = "DA-" + stream.getMessageName().replaceAll("/","_").replaceAll(".", "_") + ".json";
    		archive = DataFile.create(fileName, "" , "", DataFile.PROCESSED);
            archive.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date));
            archive.save();
    		stream.setMessageArchiveId(archive.getId());
            stream.getMessageLogger().println(String.format("Creating archive datafile " + fileName + " with id " + archive.getId()));
    		stream.save();
    	} else {
    		archive = DataFile.findById(stream.getMessageArchiveId());
            stream.getMessageLogger().println("Reusing archive datafile with id " + stream.getMessageArchiveId());
    	}
            
        stream.getMessageLogger().println(String.format("Message stream <%s> has labels <%s>", stream.getLabel(), stream.getHeaders().toString()));
        
        MeasurementGenerator gen = new MeasurementGenerator(MeasurementGenerator.MSGMODE, null, stream, stream.getSchema(), null);
        if (!gen.getStudyUri().isEmpty()) {
            gen.setNamedGraphUri(gen.getStudyUri());
        }        
		try {
			gen.preprocess();
		} catch (Exception e1) {
			stream.getMessageLogger().println("Error with MeasurementGenerator inside MessageAnnotation: " + e1.toString());
		}
		HttpMessageWorker.getInstance().addStream(stream);
        HttpMessageWorker.getInstance().addStreamGenerator(stream.getUri(), gen);
        HTTPSubscribe.exec(stream, gen);
        stream.setMessageStatus(STR.ACTIVE);
		stream.getMessageLogger().println(String.format("Message stream %s is active.", stream.getMessageName()));
		stream.save();

    }
    
    public static void unsubscribeMessageStream(STR stream) {
    	if (!stream.getMessageStatus().equals(STR.ACTIVE)) {
    		return;
    	}
    	System.out.println("Unsubscribing message stream: " + stream.getMessageName());
		stream.getMessageLogger().resetLog();
		stream.getMessageLogger().println(String.format("Unsubscribing message stream: %s", stream.getMessageName()));
		if (!HttpMessageWorker.getInstance().executorsMap.containsKey(stream.getMessageName())) {
			stream.getMessageLogger().println("Could not stop message stream: " + stream.getMessageName() + ". Reason: currentClient is null");
		} else {
	        stream.setMessageStatus(STR.SUSPENDED);        
	        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	        String endTime = isoFormat.format(new Date());
	        stream.setEndedAtXsdWithMillis(endTime);
			stream.getMessageLogger().println(String.format("Stopped processing of message stream: %s", stream.getMessageName()));
			HttpMessageWorker.getInstance().removeStream(stream.getUri());
			HttpMessageWorker.getInstance().removeStreamGenerator(stream.getUri());
    	}
		stream.save();
    }
        
}
