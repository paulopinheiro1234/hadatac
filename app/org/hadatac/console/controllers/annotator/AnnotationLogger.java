package org.hadatac.console.controllers.annotator;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.MessageTopic;
import org.hadatac.entity.pojo.STR;
import org.hadatac.utils.Feedback;


public class AnnotationLogger {

    private String log = "";
    private DataFile parent = null;
    private STR stream = null;
    private MessageTopic topic = null;
    
    public AnnotationLogger(DataFile parent) {
        this.parent = parent;
    }
    
    public AnnotationLogger(STR stream) {
        this.stream = stream;
    }
    
    public AnnotationLogger(MessageTopic topic) {
        this.topic = topic;
    }
    
	public AnnotationLogger(DataFile parent, String log) {
	    this.parent = parent;
	    this.log = log;
	}
	
	public AnnotationLogger(STR stream, String log) {
	    this.stream = stream;
	    this.log = log;
	}
	
	public AnnotationLogger(MessageTopic topic, String log) {
	    this.topic = topic;
	    this.log = log;
	}
	
	public String getLog() {
		return log;
	}
	
	public void setLog(String log) {
		this.log = log;
		if (parent != null) {
			parent.save();
		}
		if (stream != null) {
			stream.save();
		}
		if (topic != null) {
			topic.save();
		}
	}
	
	public void resetLog() {
	    log = "";
		if (parent != null) {
			parent.save();
		}
		if (stream != null) {
			stream.save();
		}
		if (topic != null) {
			topic.save();
		}
	}
	
	public void addLine(String newLine) {
	    log += (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()) + " " + newLine;
		if (parent != null) {
			parent.save();
		}
		if (stream != null) {
			stream.save();
		}
		if (topic != null) {
			topic.save();
		}
	}
	
	public void printExceptionById(String id) {
	    printException(id + ": " + ErrorDictionary.getDetailById(id));
    }
	
	public void printExceptionByIdWithArgs(String id, Object... args) {
        printException(id + ": " + String.format(ErrorDictionary.getDetailById(id), args));
    }
	
	public void printException(Exception exception) {
	    addLine(Feedback.println(Feedback.WEB, "[ERROR] " + exception.getMessage()));
    }
    
    public void printException(String message) {
        addLine(Feedback.println(Feedback.WEB, "[ERROR] " + message));
    }
    
    public void printWarningById(String id) {
        printWarning(id + ": " + ErrorDictionary.getDetailById(id));
    }
    
    public void printWarningByIdWithArgs(String id, Object... args) {
        printWarning(id + ": " + String.format(ErrorDictionary.getDetailById(id), args));
    }
    
    public void printWarning(String message) {
        addLine(Feedback.println(Feedback.WEB, "[WARNING] " + message));
    }
    
    public void println(String message) {
        addLine(Feedback.println(Feedback.WEB, "[LOG] " + message));
    }
}

