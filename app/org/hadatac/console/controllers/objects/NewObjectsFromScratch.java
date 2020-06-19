package org.hadatac.console.controllers.objects;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;

import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.ObjectCollectionType;
import org.hadatac.entity.pojo.StudyObjectType;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.objects.*;
import org.hadatac.console.models.NewObjectsFromScratchForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.AuthApplication;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class NewObjectsFromScratch extends Controller {

    static final long MAX_OBJECTS = 1000;
    static final long LENGTH_CODE = 6;

    @Inject
    private FormFactory formFactory;

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String da_uri, String std_uri, String oc_uri, int page) {

        try {
            std_uri = URLDecoder.decode(std_uri, "utf-8");
            oc_uri = URLDecoder.decode(oc_uri, "utf-8");
        } catch (UnsupportedEncodingException e) {
            std_uri = "";
            oc_uri = "";
        }

        Study study = Study.find(std_uri);
        ObjectCollection oc = ObjectCollection.find(oc_uri);

        List<StudyObjectType> typeList = StudyObjectType.find();

        if (typeList == null) {
            System.out.println("new objects: type list is null");
        } else {
            for (StudyObjectType type : typeList) {
                System.out.println("new objects: type = [" + type.getUri() + "]");
            }
        }

        return ok(newObjectsFromScratch.render(dir, filename, da_uri, study, oc, typeList, page));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String dir, String filename, String da_uri, String std_uri, String oc_uri, int page) {
        return index(dir, filename, da_uri, std_uri, oc_uri, page);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processForm(String dir, String filename, String da_uri, String std_uri, String oc_uri, int page) {
        final SysUser sysUser = AuthApplication.getLocalUser(session());

        Study std = Study.find(std_uri);
        ObjectCollection oc = ObjectCollection.find(oc_uri);

        Form<NewObjectsFromScratchForm> form = formFactory.form(NewObjectsFromScratchForm.class).bindFromRequest();
        NewObjectsFromScratchForm data = form.get();

        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }


        long quantity = Long.parseLong(data.getNewQuantity());
        if (quantity > MAX_OBJECTS) {
            return badRequest("Your request has exceeded MAX_OBJECTS!");
        }
        long nextId = std.getLastId() + 1;
        //System.out.println("nextId : " + nextId);
        if (quantity > 0) {
            std.increaseLastId(quantity);
        }

        // Fixed values
        String newType = null;
        if (data.getNewType() == null || data.getNewType().equals("")) {
            return badRequest("[ERROR] New type cannot be empty.");
        } else {
            newType = URIUtils.replacePrefixEx(data.getNewType());
        }
        String newObjectCollectionUri = URIUtils.replacePrefixEx(oc_uri);
        String newLabelPrefix = data.getNewLabelPrefix();

        // Variable values
        String newURI = null;
        String newLabel = null;
        String newComment = null;

        // Object object
        StudyObject obj = null;

        if (quantity > 0) {
            for (int i=0; i < quantity; i++) {

                ObjectCollectionType ocType = ObjectCollectionType.find(oc.getTypeUri());
                newURI = oc.getUri().replace("OC-",ocType.getAcronym() + "-") + "-" + formattedCounter(nextId);
                if (newLabelPrefix == null || newLabelPrefix.equals("")) {
                    newLabel = Long.toString(nextId);
                } else {
                    newLabel = newLabelPrefix + " " + nextId;
                }
                newComment = newLabel;

                // insert current state of the OBJ
                obj = new StudyObject(newURI,
                        newType,
                        "", // Original ID
                        newLabel,
                        newObjectCollectionUri,
                        newComment,
                        new ArrayList<String>(),
                        new ArrayList<String>(),
                        new ArrayList<String>()
                        );

                // insert the new OC content inside of the triplestore regardless of any change -- the previous content has already been deleted
                obj.saveToTripleStore();

                oc.getObjectUris().add(obj.getUri());
                nextId++;

            }
        }
        String message = "A total of " + quantity + " new object(s) have been Generated";
        return ok(objectConfirm.render(message, dir, filename, da_uri, std_uri, oc_uri, page));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processScopeForm(String dir, String filename, String da_uri, String std_uri, String oc_uri, int page) {
        try {
            oc_uri = URLDecoder.decode(oc_uri, "utf-8");
        } catch (UnsupportedEncodingException e) {
            oc_uri = "";
        }
        
        final SysUser sysUser = AuthApplication.getLocalUser(session());

        Study std = Study.find(std_uri);
        ObjectCollection oc = ObjectCollection.find(oc_uri);

        Form<NewObjectsFromScratchForm> form = formFactory.form(
                NewObjectsFromScratchForm.class).bindFromRequest();
        NewObjectsFromScratchForm data = form.get();

        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        // store new values
        System.out.println("============================ NEW OBJECTS =======================");
        System.out.println("Study: [" + std.getUri() + "]");
        System.out.println("OC: [" + oc.getUri() + "]");
        System.out.println("type: [" + data.getNewType() + "]");
        System.out.println("UseDomain : " + data.getUseDomain());
        System.out.println("UseSpace : " + data.getUseSpace());
        System.out.println("UseTime : " + data.getUseTime());
        System.out.println("Multiplier : " + data.getNewMultiplier());

        boolean useDomain = (data.getUseDomain() != null);
        boolean useSpace = (data.getUseSpace() != null);
        boolean useTime = (data.getUseTime() != null);
        long multiplier = Long.parseLong(data.getNewMultiplier());

        if (multiplier > MAX_OBJECTS) {
            return badRequest("Requested number of objects exceeeds MAX OBJECTS");
        }

        String newLabelPrefix = data.getNewLabelPrefix();
        boolean newLabelQualifier = (data.getNewLabelQualifier() != null);
        long nextId = std.getLastId() + 1;
        long quantity = 0;

        // Fixed values
        String newType = null;
        if (data.getNewType() == null || data.getNewType().equals("")) {
            return badRequest("[ERROR] New type cannot be empty.");
        } else {
            newType = URIUtils.replacePrefixEx(data.getNewType());
        }
        String newObjectCollectionUri = URIUtils.replacePrefixEx(oc_uri);

        // Variable values
        String newURI = null;
        String newLabel = null;
        String newComment = null;

        // Object object
        StudyObject obj = null;
        List<String> genObjs = new ArrayList<String>();

        List<ObjectCollection> allObjectCollections = new ArrayList<ObjectCollection>();

        if (multiplier > 0) {

            // Combine selected object collections
            if (useDomain) {
                if (useSpace) {
                    if (useTime) {
                        // DOMAIN + SPACE + TIME
                        //System.out.println("Selection: DOMAIN + SPACE + TIME");
                        allObjectCollections.add(oc.getHasScope());
                        allObjectCollections.addAll(oc.getSpaceScopes());
                        allObjectCollections.addAll(oc.getTimeScopes());

                    } else {
                        // DOMAIN + SPACE
                        //System.out.println("Selection: DOMAIN + SPACE");
                        allObjectCollections.add(oc.getHasScope());
                        allObjectCollections.addAll(oc.getSpaceScopes());

                    }
                } else {
                    if (useTime) {
                        // DOMAIN + TIME
                        //System.out.println("Selection: DOMAIN + TIME");
                        allObjectCollections.add(oc.getHasScope());
                        allObjectCollections.addAll(oc.getTimeScopes());

                    } else {
                        // DOMAIN
                        //System.out.println("Selection: DOMAIN");
                        allObjectCollections.add(oc.getHasScope());

                    }
                }

            } else if (useSpace) {

                if (useTime) {
                    // SPACE and TIME
                    System.out.println("Selection: SPACE + TIME");
                    allObjectCollections.addAll(oc.getSpaceScopes());
                    allObjectCollections.addAll(oc.getTimeScopes());

                } else {
                    // SPACE
                    System.out.println("Selection: SPACE");
                    allObjectCollections.addAll(oc.getSpaceScopes());

                }
            } else if (useTime) {
                // TIME
                System.out.println("Selection: TIME");
                allObjectCollections.addAll(oc.getTimeScopes());
            }

            // Generate the cartesian product of the scope objects
            List<StudyObject[]> newListArray = new ArrayList<StudyObject[]>();		
            for (ObjectCollection collection : allObjectCollections) {
                int j = 0;
                StudyObject[] newArray = new StudyObject[collection.getObjects().size()]; 
                for (StudyObject stdObj : collection.getObjects()) {
                    newArray[j] = stdObj;
                    j++;
                }
                newListArray.add(newArray);
            }
            StudyObject[][] newMatrix = newListArray.toArray(new StudyObject[newListArray.size()][]);

            List<String> genCombinations = new ArrayList<String>();
            List<List<StudyObject>> genCombinationsOS = new ArrayList<List<StudyObject>>();
            cartesianProduct(newMatrix, 0, new StudyObject[newMatrix.length], genCombinations, genCombinationsOS);

            System.out.println("GENERATED COMBINATIONS");
            for (List<StudyObject> soCol : genCombinationsOS) {
                for (int i=0; i < multiplier; i++) {
                    ObjectCollectionType ocType = ObjectCollectionType.find(oc.getTypeUri());
                    newURI = oc.getUri().replace("OC-",ocType.getAcronym() + "-") + "-" + formattedCounter(nextId);
                    if (newLabelPrefix == null || newLabelPrefix.equals("")) {
                        newLabel = Long.toString(nextId);
                    } else {
                        newLabel = newLabelPrefix + " " + nextId;
                    }
                    if (newLabelQualifier) {
                        newLabel += ":";
                        for (StudyObject so : soCol) {
                            newLabel += " " + so.getLabel();
                        }
                    }
                    newComment = newLabel;

                    List<String> scopeUris = new ArrayList<String>();
                    List<String> timeScopeUris = new ArrayList<String>();
                    List<String> spaceScopeUris = new ArrayList<String>();
                    for (StudyObject so : soCol) {
                        scopeUris.add(so.getUri());
                        // TODO: needs to add to timeScopeUris and spaceScopeUris too
                        System.out.println("   - Scope: [" + so.getUri() + "]");
                    }

                    // insert current state of the OBJ
                    obj = new StudyObject(newURI,
                            newType,
                            "", // Original ID
                            newLabel,
                            newObjectCollectionUri,
                            newComment,
                            scopeUris,
                            timeScopeUris,
                            spaceScopeUris
                            );

                    // insert the new OC content inside of the triplestore regardless of any change -- the previous content has already been deleted
                    obj.saveToTripleStore();

                    oc.getObjectUris().add(obj.getUri());

                    nextId++;
                    genObjs.add(newLabel);
                }
            }

        }
        quantity = genObjs.size();
        String message = "Total objects created: " + quantity;
        if (quantity > 0) {
            std.increaseLastId(quantity);
        }

        return ok(objectConfirm.render(message, dir, filename, da_uri, std_uri, oc_uri, page));
    }

    private static void cartesianProduct(StudyObject[][] arr, int level, StudyObject[] cp, List<String> gens, List<List<StudyObject>> genOS) {
        if (level == arr.length) {
            List<StudyObject> response = new ArrayList<StudyObject>();
            String message = "";
            for (StudyObject so : cp) {
                response.add(so);
                message += so.getLabel() + " ";
            }
            gens.add(message);
            genOS.add(response);
            return;
        }
        for (int i = 0; i < arr[level].length; i++) {
            cp[level] = arr[level][i];
            cartesianProduct(arr, level + 1, cp, gens, genOS);
        }
    }    

    static private String formattedCounter (long value) {
        String strLong = Long.toString(value).trim();
        for (int i=strLong.length(); i < LENGTH_CODE; i++) {
            strLong = "0" + strLong;
        }
        return strLong;
    }

}
