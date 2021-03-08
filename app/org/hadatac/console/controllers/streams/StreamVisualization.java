package org.hadatac.console.controllers.streams;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import org.hadatac.console.views.html.streams.*;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.DeploymentQueries;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.models.TripleDocument;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.utils.State;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import javax.inject.Inject;


public class StreamVisualization extends Controller {
    @Inject
    Application application;

    //private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private Date minDate;
    private Date maxDate;
    private boolean is9Pixel = false;
    private boolean is25by20 = false;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String dir, String filename, String da_uri, Http.Request request) {

        try {
            da_uri = URLDecoder.decode(da_uri, "UTF-8");
        } catch (Exception e) {
        }
        if (da_uri == null || da_uri.isEmpty()) {
            return ok(genericVisualization.render(dir, filename, da_uri, "[]", "", "", application.getUserEmail(request)));
        }
        List<Measurement> measurements = Measurement.findByDataAcquisitionUri(da_uri);
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
        System.out.println("Stream Visualization: da_uri=" + da_uri);
        System.out.println("Stream Visualization: size measurements =" + measurements.size());
        //for (Measurement m : measurements) {
        //	System.out.println("Stream Visualization: " +  sdf.format(m.getTimestamp()) + " " + m.getCharacteristicUris().get(0) + "  " + m.getValue());
        //}
        if (measurements == null) {
            return ok(genericVisualization.render(dir, filename, da_uri, "[]", "", "",application.getUserEmail(request)));
        }
        String json = generateJSON(measurements);
        //System.out.println("Min Date: " + sdf.format(minDate));
        //System.out.println("Max Date: " + sdf.format(maxDate));
        if (is9Pixel) {
            return ok(tof9PixelPodImageVisualization.render(dir, filename, da_uri, generateJSON(measurements), sdf.format(minDate), sdf.format(maxDate),application.getUserEmail(request)));
        }
        if (is25by20) {
            return ok(tof25by20ImageVisualization.render(dir, filename, da_uri, generateJSON(measurements), sdf.format(minDate), sdf.format(maxDate),application.getUserEmail(request)));
        }
        return ok(genericVisualization.render(dir, filename, da_uri, generateJSON(measurements), sdf.format(minDate), sdf.format(maxDate),application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String dir, String filename, String da_uri, Http.Request request) {
        return index(dir, filename, da_uri, request);
    }

    private String generateJSON(List<Measurement> measurements) {
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT-05:00"));
        minDate = new Date(Long.MAX_VALUE);
        maxDate = new Date(0);
        String json = "[";
        for (int i = 0; i < measurements.size(); i++) {
            if (measurements.get(i).getCharacteristicUris().get(0).equals("http://hadatac.org/ont/lesa#DetectorDistance9Pixel15DegreeImage") ||
                    measurements.get(i).getCharacteristicUris().get(0).equals("http://hadatac.org/ont/lesa#DetectorDistance25By20Image")) {

                String values = "";

                if (measurements.get(i).getCharacteristicUris().get(0).equals("http://hadatac.org/ont/lesa#DetectorDistance9Pixel15DegreeImage")) {
                    is9Pixel = true;
                    values = measurements.get(i).getValue();
                };
                if (measurements.get(i).getCharacteristicUris().get(0).equals("http://hadatac.org/ont/lesa#DetectorDistance25By20Image")) {
                    is25by20 = true;
                    System.out.println("Measurement Value: " + measurements.get(i).getValue());
                    values = measurements.get(i).getValue().replace("];]", "]]");

                };

                Date tmpDate = measurements.get(i).getTimestamp();
                if (tmpDate.before(minDate)) {
                    minDate = tmpDate;
                }
                if (tmpDate.after(maxDate)) {
                    maxDate = tmpDate;
                }
                json = json + "{";
                json = json + "\"date\":\"" + sdf.format(tmpDate) + "\",";
                json = json + "\"value\":\"" + values + "\"";
                json = json + "}";
                if (i < measurements.size() - 1) {
                    json = json + ",";
                }
            }
        }
        json = json + "]";
        return json;
    }

}
