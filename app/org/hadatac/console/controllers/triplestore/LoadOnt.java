package org.hadatac.console.controllers.triplestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.pac4j.play.java.Secure;
import play.libs.Files.TemporaryFile;
import play.mvc.*;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Http.MultipartFormData.FilePart;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.entity.pojo.NameSpace;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;


public class LoadOnt extends Controller {

    public static final String LAST_LOADED_NAMESPACE = "/last-loaded-namespaces-properties";

    @Inject
    private FormFactory formFactory;
    @Inject
    Application application;

    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result loadOnt(String oper, Http.Request request) {

        List<String> cacheList = new ArrayList<String>();
        File folder = new File(NameSpaces.CACHE_PATH);

        try {
            // if the directory does not exist, create it
            if (!folder.exists()) {
                System.out.println("creating directory: " + NameSpaces.CACHE_PATH);
                try{
                    folder.mkdir();
                } catch(SecurityException se){
                    System.out.println("Failed to create directory.");
                }
                System.out.println("DIR created");
            }

            String name = "";
            if (folder.listFiles() != null) {
                System.out.println("folder.listFiles:" + folder.listFiles());
                for (final File fileEntry : folder.listFiles()) {
                    if (!fileEntry.isDirectory()) {
                        name = fileEntry.getName();
                        if (name.startsWith(NameSpaces.CACHE_PREFIX)) {
                            name = name.substring(NameSpaces.CACHE_PREFIX.length());
                            cacheList.add(name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ok(loadOnt.render(oper, cacheList, NameSpaces.getInstance().getOntologyList(),application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postLoadOnt(String oper, Http.Request request) {
        return loadOnt(oper, request);
    }

    public static String playLoadOntologies(String oper) {
        NameSpaces.getInstance();
        MetadataContext metadata = new
                MetadataContext("user",
                "password",
                ConfigFactory.load().getString("hadatac.solr.triplestore"),
                false);
        return metadata.loadOntologies(Feedback.WEB, oper);
    }

    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result eraseCache(Http.Request request) {
        List<String> cacheList = new ArrayList<String>();
        File folder = new File(NameSpaces.CACHE_PATH);
        String name = "";

        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                name = fileEntry.getName();
                if (name.startsWith(NameSpaces.CACHE_PREFIX)) {
                    fileEntry.delete();
                }
            }
        }

        return ok(loadOnt.render("init", cacheList, NameSpaces.getInstance().getOntologyList(),application.getUserEmail(request)));
    }

    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result reloadNamedGraphFromRemote(String abbreviation) {
        NameSpace ns = NameSpaces.getInstance().getNamespaces().get(abbreviation);
        ns.deleteTriples();

        String url = ns.getURL();
        if (!url.isEmpty()) {
            ns.loadTriples(url, true);
        }
        ns.updateNumberOfLoadedTriples();
        ns.updateFromTripleStore();

        return redirect(routes.LoadOnt.loadOnt("init"));
    }

    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result reloadNamedGraphFromCache(String abbreviation) {
        NameSpace ns = NameSpaces.getInstance().getNamespaces().get(abbreviation);
        ns.deleteTriples();

        String filePath = NameSpaces.CACHE_PATH + "copy" + "-" + ns.getAbbreviation().replace(":", "");
        File localFile = new File(filePath);
        if (localFile.exists()) {
            ns.loadTriples(filePath, false);
            ns.updateNumberOfLoadedTriples();
            ns.updateFromTripleStore();
        } else {
            return badRequest("No cache for this namespace!");
        }

        return redirect(routes.LoadOnt.loadOnt("init"));
    }

    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result deleteNamedGraph(String abbreviation) {
        NameSpace ns = NameSpaces.getInstance().getNamespaces().get(abbreviation);
        ns.deleteTriples();
        ns.updateNumberOfLoadedTriples();
        ns.updateFromTripleStore();

        return redirect(routes.LoadOnt.loadOnt("init"));
    }

    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result deleteAllNamedGraphs() {
        for (NameSpace ns : NameSpaces.getInstance().getNamespaces().values()) {
            ns.deleteTriples();
            ns.updateNumberOfLoadedTriples();
            ns.updateFromTripleStore();
        }

        return redirect(routes.LoadOnt.loadOnt("init"));
    }

    @SuppressWarnings("unchecked")
    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result saveNamespaces(Http.Request request) {
        int original_size = NameSpace.findAll().size();

        Form form = formFactory.form().bindFromRequest(request);
        Map<String, String> data = form.rawData();

        NameSpace.deleteAll();
        for (int i = 0; i < Math.max(original_size, data.size() / 3); ++i) {
            if (!data.containsKey("nsAbbrev" + String.valueOf(i + 1))) {
                continue;
            }

            NameSpace ns = new NameSpace();
            ns.setAbbreviation(data.get("nsAbbrev" + String.valueOf(i + 1)));
            ns.setName(data.get("nsName" + String.valueOf(i + 1)));
            ns.setMimeType(data.get("nsType" + String.valueOf(i + 1)));
            ns.setURL(data.get("nsURL" + String.valueOf(i + 1)));
            ns.save();
        }
        NameSpaces.getInstance().reload();

        return redirect(routes.LoadOnt.loadOnt("init"));
    }

    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    @BodyParser.Of(value = BodyParser.MultipartFormData.class)
    public Result importNamespaces(Http.Request request) {
        System.out.println("importNamespaces is called");
        String name_last_loaded_namespace = "";
        FilePart uploadedfile = request.body().asMultipartFormData().getFile("ttl");
        if (uploadedfile != null) {
            TemporaryFile  temporaryFile = (TemporaryFile) uploadedfile.getRef();
            File file = temporaryFile.path().toFile();
            name_last_loaded_namespace = uploadedfile.getFilename();
            FileInputStream inputStream;
            try {
                inputStream = new FileInputStream(file);
                List<NameSpace> namespaces = NameSpaces.loadFromFile(inputStream);
                inputStream.close();

                NameSpace.deleteAll();
                for (NameSpace ns : namespaces) {
                    ns.save();
                }
            } catch (Exception e) {
                return badRequest("Could not find uploaded file");
            }
        } else {
            return badRequest("Error uploading file. Please try again.");
        }
        NameSpaces.getInstance().reload();

        // save the name of the last uploaded namespace
        File lastloadedfile = new File(ConfigProp.getPathWorking() + LAST_LOADED_NAMESPACE);
        try {
            FileOutputStream lastLoadedOutputStream = new FileOutputStream(lastloadedfile);
            System.out.println("Name last loaded prop file: " + lastLoadedOutputStream);
            lastLoadedOutputStream.write(name_last_loaded_namespace.getBytes());
            lastLoadedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return redirect(routes.LoadOnt.loadOnt("init"));
    }

    public static String getNameLastLoadedNamespace() {
        File lastloadedfile = new File(ConfigProp.getPathWorking() + LAST_LOADED_NAMESPACE);
        String name_last_loaded_namespace = "";
        try {
            FileInputStream inputStream = new FileInputStream(lastloadedfile);
            name_last_loaded_namespace = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            inputStream.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return name_last_loaded_namespace;
    }


    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result exportNamespaces() {
        String path = ConfigProp.getPathWorking();
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(path + "/namespaces.properties");
        try {
            List<String> lines = new ArrayList<String>();

            for (NameSpace ns : NameSpaces.getInstance().getOntologyList()) {
                lines.add(ns.getAbbreviation() + "=" + String.join(",", Arrays.asList(
                        ns.getName(), ns.getMimeType(), ns.getURL())));
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(String.join("\n", lines).getBytes());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ok(file);
    }
}
