package org.hadatac.data.loader.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.hadatac.data.loader.MeasurementGenerator;
import org.hadatac.entity.pojo.STR;

import play.data.FormFactory;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;

import org.hadatac.data.loader.http.HttpMessageWorker;
import org.hadatac.utils.ApiUtil;


public class HttpSubscribe extends Controller {
    
    private STR                  stream;
	private boolean 			 quietMode;
    private String               plainPayload;
    private long                 totalMessages;
    private int                  ingestedMessages;
    private int					 partialCounter;
    private MeasurementGenerator gen;
    
    public static void exec(STR stream, MeasurementGenerator generator) {

        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit((Runnable) () -> {
        
        	try {
        		new HttpSubscribe(stream, generator);
        	    TimeUnit.MILLISECONDS.sleep(300);
        	} catch(Exception e) {
        		System.out.println("excep " + e);
        		e.printStackTrace();
        	}      

        }); 
        
        HttpMessageWorker.getInstance().addExecutor(stream.getLabel(), executor);
        
    }

    public HttpSubscribe() {
    	this.quietMode = false;
    }
    
    public void setStream(STR stream) {
    	this.stream    = stream;    	
    	this.totalMessages = stream.getTotalMessages();
    	this.ingestedMessages = Math.toIntExact(stream.getIngestedMessages());
    	this.partialCounter = 0;
    	
    }
    
    public int setGenerator(MeasurementGenerator gen) {
    	if (stream == null || stream.getUri() == null || stream.getUri().isEmpty()) {
    		return -1;
    	}
    	this.gen = gen;
    	HttpMessageWorker.getInstance().addStreamGenerator(stream.getUri(), gen);
    	return 0;
    }
    
    public HttpSubscribe(STR stream, MeasurementGenerator generator) {
    	this.stream    = stream;
    	this.quietMode = false;

    	HttpMessageWorker.getInstance().addStreamGenerator(stream.getUri(), generator);
    	this.gen = generator;
    	
    	this.totalMessages = stream.getTotalMessages();
    	this.ingestedMessages = Math.toIntExact(stream.getIngestedMessages());
    	this.partialCounter = 0;

    }
    
	@BodyParser.Of(BodyParser.Json.class)
    public Result messageArrived(String topic, String streamUri) {
        if(streamUri == null){
            return badRequest(ApiUtil.createResponse("No stream specified", false));
        }
        System.out.println("[HTTPManagement] Setting stream for " + streamUri);

        RequestBody body = request().body();
        plainPayload = play.libs.Json.asciiStringify(body.asJson());
        System.out.println("[HTTPManagement] body: [" + plainPayload + "]");

        totalMessages = totalMessages + 1;
		stream.setTotalMessages(totalMessages);
		stream.setIngestedMessages(ingestedMessages);
		partialCounter = partialCounter + 1;
		if (partialCounter >= 1) {
			partialCounter = 0;
			System.out.println("Received " + totalMessages + " messages. Ingested " + ingestedMessages + " messages.");

            try {
                gen.commitObjectsToSolr(gen.getObjects());
            } catch (Exception e) {
                e.printStackTrace();                
                gen.getLogger().printException(gen.getErrorMsg(e));
            }
			stream.save();
		}

		try {
			if (HttpMessageWorker.processMessage(stream.getUri(), topic, plainPayload, ingestedMessages) != null) {
				ingestedMessages = ingestedMessages + 1;
				stream.setIngestedMessages(ingestedMessages);
			}
		} catch (Exception e) {
			e.printStackTrace();
            return internalServerError(ApiUtil.createResponse("Error parsing HTTP resquest body", false));
		}
		
		if (Thread.currentThread().isInterrupted()) {
			//System.out.println("Thread INTERRUPTED");
		} else {
			//System.out.println("Thread not interrrupted");
		}

        return ok(ApiUtil.createResponse("{}", true));
	}

}

