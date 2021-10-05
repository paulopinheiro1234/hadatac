package org.hadatac.console.controllers.annotator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.annotator.routes;
import org.hadatac.console.http.ResumableUpload;
import org.hadatac.console.models.AssignOptionForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.data.loader.AnnotationWorker;
import org.hadatac.data.loader.CSVRecordFile;
import org.hadatac.data.loader.GeneratorChain;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.data.loader.SpreadsheetRecordFile;
import org.hadatac.console.views.html.*;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.User;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.FileManager;
import org.hadatac.utils.NameSpace;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.typesafe.config.ConfigException.Null;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.pac4j.play.java.Secure;
import play.twirl.api.Html;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Http.MultipartFormData.FilePart;
import play.libs.Files.DelegateTemporaryFile;


public class AutoAnnotator extends Controller {

   @Inject
   FormFactory formFactory;
   @Inject
    Application application;

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result index(String dir, String dest, Http.Request request) {
      final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

      String newDir = Paths.get(dir, dest).normalize().toString();

      List<DataFile> procFiles = null;
      List<DataFile> unprocFiles = null;
      List<String> studyURIs = new ArrayList<String>();

      String pathProc = ConfigProp.getPathProc();
      String pathUnproc = ConfigProp.getPathUnproc();

      List<String> folders = DataFile.findFolders(Paths.get(pathProc, newDir).toString(), true);
      if (!"/".equals(newDir)) {
         folders.add(0, "..");
      }

      if (user.isDataManager()) {
         procFiles = DataFile.findInDir(newDir, DataFile.PROCESSED);

         unprocFiles = DataFile.findInDir("", DataFile.UNPROCESSED);
         unprocFiles.addAll(DataFile.findInDir("", DataFile.FREEZED));

         String basePath = newDir;
         if (basePath.startsWith("/")) {
            basePath = basePath.substring(1, basePath.length());
         }

         DataFile.includeUnrecognizedFiles(Paths.get(pathProc, newDir).toString(),
         basePath, procFiles, user.getEmail(), DataFile.PROCESSED);

         DataFile.includeUnrecognizedFiles(pathUnproc, "",
         unprocFiles, user.getEmail(), DataFile.UNPROCESSED);
      } else {
         procFiles = DataFile.findInDir(newDir, user.getEmail(), DataFile.PROCESSED);

         unprocFiles = DataFile.findInDir("", user.getEmail(), DataFile.UNPROCESSED);
         unprocFiles.addAll(DataFile.findInDir("", user.getEmail(), DataFile.FREEZED));
      }

      DataFile.filterNonexistedFiles(pathProc, procFiles);
      DataFile.filterNonexistedFiles(pathUnproc, unprocFiles);

      for (DataFile dataFile : procFiles) {
         if (!dataFile.getStudyUri().isEmpty() && !studyURIs.contains(dataFile.getStudyUri())) {
            studyURIs.add(dataFile.getStudyUri());
         }
      }

      studyURIs.sort(new Comparator<String>() {
         @Override
         public int compare(String o1, String o2) {
            return o1.compareTo(o2);
         }
      });

      boolean bStarted = false;
      if (ConfigProp.getAuto().equals("on")) {
         bStarted = true;
      }

      unprocFiles.sort(new Comparator<DataFile>() {
         @Override
         public int compare(DataFile d1, DataFile d2) {
            return d1.getFileName().compareTo(d2.getFileName());
         }
      });

      procFiles.sort(new Comparator<DataFile>() {
         @Override
         public int compare(DataFile d1, DataFile d2) {
            return d1.getFileName().compareTo(d2.getFileName());
         }
      });

      return ok(autoAnnotator.render(newDir, folders, unprocFiles, procFiles, studyURIs, bStarted, user.isDataManager(), user.getEmail()));
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result postIndex(String dir, String dest,Http.Request request) {
      return index(dir, dest,request);
   }

   @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
   public Result assignFileOwner(String dir, String ownerEmail, String fileId, Http.Request request) {
      final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

      DataFile dataFile = null;
      if (user.isDataManager()) {
         dataFile = DataFile.findById(fileId);
      } else {
         dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
      }

      if (null == dataFile) {
         return badRequest("You do NOT have the permission to operate this file!");
      }

      return ok(assignOption.render(User.getUserEmails(),
      routes.AutoAnnotator.processOwnerForm(dir, ownerEmail, fileId),
      "Owner",
      "Selected File",
      dataFile.getFileName(),user.getEmail()));
   }

   @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
   public Result postAssignFileOwner(String dir, String ownerEmail, String fileId,Http.Request request) {
      return assignFileOwner(dir, ownerEmail, fileId, request);
   }

   @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
   public Result processOwnerForm(String dir, String ownerEmail, String fileId,Http.Request request) {
      Form<AssignOptionForm> form = formFactory.form(AssignOptionForm.class).bindFromRequest(request);
      AssignOptionForm data = form.get();

      if (form.hasErrors()) {
         System.out.println("HAS ERRORS");
         return badRequest(assignOption.render(User.getUserEmails(),
         routes.AutoAnnotator.processOwnerForm(dir, ownerEmail, fileId),
         "Owner",
         "Selected File",
         fileId,application.getUserEmail(request)));
      } else {
         DataFile file = DataFile.findByIdAndEmail(fileId, ownerEmail);
         if (file == null) {
            file = new DataFile(fileId);
            file.setOwnerEmail(AuthApplication.getLocalUser(application.getUserEmail(request)).getEmail());
            file.setStatus(DataFile.UNPROCESSED);
            file.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
         }
         file.setOwnerEmail(data.getOption());
         file.save();
         return redirect(routes.AutoAnnotator.index(dir, "."));
      }
   }

   @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
   public Result assignDataAcquisition(String dir, String dataAcquisitionUri, String fileId,Http.Request request) {
      final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

      DataFile dataFile = null;
      if (user.isDataManager()) {
         dataFile = DataFile.findById(fileId);
      } else {
         dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
      }

      if (null == dataFile) {
         return badRequest("You do NOT have the permission to operate this file!");
      }

      List<String> dataAcquisitionURIs = new ArrayList<String>();
      STR.findAll().forEach((da) -> dataAcquisitionURIs.add(
      URIUtils.replaceNameSpaceEx(da.getUri())));

      return ok(assignOption.render(dataAcquisitionURIs,
      routes.AutoAnnotator.processDataAcquisitionForm(dir, dataAcquisitionUri, fileId),
      "Stream Specification",
      "Selected File",
      dataFile.getFileName(),application.getUserEmail(request)));
   }

   @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
   public Result postAssignDataAcquisition(String dir, String dataAcquisitionUri, String fileId, Http.Request request) {
      return assignDataAcquisition(dir, dataAcquisitionUri, fileId, request);
   }

   @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
   public Result processDataAcquisitionForm(String dir, String dataAcquisitionUri, String fileId, Http.Request request) {
      Form<AssignOptionForm> form = formFactory.form(AssignOptionForm.class).bindFromRequest(request);
      AssignOptionForm data = form.get();

      List<String> dataAcquisitionURIs = new ArrayList<String>();
      STR.findAll().forEach((da) -> dataAcquisitionURIs.add(
      URIUtils.replaceNameSpaceEx(da.getUri())));

      if (form.hasErrors()) {
         System.out.println("HAS ERRORS");
         return badRequest(assignOption.render(dataAcquisitionURIs,
         routes.AutoAnnotator.processDataAcquisitionForm(dir, dataAcquisitionUri, fileId),
         "Stream Specification",
         "Selected File",
         fileId,application.getUserEmail(request)));
      } else {
         DataFile file = DataFile.findById(fileId);
         if (file == null) {
            file = new DataFile("Unknown_File.csv");
            file.setOwnerEmail(AuthApplication.getLocalUser(application.getUserEmail(request)).getEmail());
            file.setStatus(DataFile.UNPROCESSED);
            file.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
         }
         file.setDataAcquisitionUri(URIUtils.replacePrefixEx(data.getOption()));
         file.save();
         return redirect(routes.AutoAnnotator.index(dir, "."));
      }
   }

   @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
   public Result toggleAutoAnnotator(String dir) {
      if (ConfigProp.getAuto().equals("on")) {
         //ConfigProp.setPropertyValue("hadatac.conf", "autoccsv.auto", "off");
         System.setProperty("hadatac.autoccsv.auto", "off");
         System.out.println("Turning auto-annotation off");
      }
      else {
         //ConfigProp.setPropertyValue("hadatac.conf", "autoccsv.auto", "on");
         System.setProperty("hadatac.autoccsv.auto", "on");
         System.out.println("Turning auto-annotation on");
      }

      return redirect(routes.AutoAnnotator.index(dir, "."));
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result downloadTemplates(String dir,Http.Request request) {
      return ok(download_templates.render(dir,application.getUserEmail(request)));
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result postDownloadTemplates(String dir) {
      return postDownloadTemplates(dir);
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result checkAnnotationLog(String dir, String fileId, Http.Request request) {
      DataFile dataFile = DataFile.findById(fileId);
      if (fileId == null) {
         fileId = "";
      }
      if (DataFile.findById(fileId) == null ||
      DataFile.findById(fileId).getLogger() == null) {
         return ok(annotation_log.render(Feedback.print(Feedback.WEB, ""),
         routes.AutoAnnotator.index(dir, ".").url(),application.getUserEmail(request)));
      }
      return ok(annotation_log.render(Feedback.print(Feedback.WEB,
      DataFile.findById(fileId).getLogger().getLog()),
      routes.AutoAnnotator.index(dir, ".").url(),application.getUserEmail(request)));
   }


   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result checkErrorDictionary(Http.Request request) {
      InputStream inputStream = getClass().getClassLoader()
      .getResourceAsStream("error_dictionary.json");
      String jsonText = "";
      try {
         jsonText = IOUtils.toString(inputStream, "UTF-8");
      } catch (IOException e) {
         e.printStackTrace();
      }

      return ok(error_dictionary.render(jsonText, routes.AutoAnnotator.index("/", ".").url(),application.getUserEmail(request)));
   }

   public Result getAnnotationStatus(String fileId) {
      DataFile dataFile = DataFile.findById(fileId);
      Map<String, Object> result = new HashMap<String, Object>();

      if (dataFile == null) {
         result.put("File Id", fileId);
         result.put("Status", "Unknown");
         result.put("Error", "The file with the specified id cannot be retrieved. "
         + "Please provide a valid file id.");
      } else {
         result.put("File Name", dataFile.getFileName());
         result.put("Status", dataFile.getStatus());
         result.put("Submission Time", dataFile.getSubmissionTime());
         result.put("Completion Time", dataFile.getCompletionTime());
         result.put("Owner Email", dataFile.getOwnerEmail());
         result.put("Log", dataFile.getLog());
      }

      return ok(Json.toJson(result));
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result moveDataFile(String dir, String fileId,Http.Request request) {
      final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

      DataFile dataFile = null;
      if (user.isDataManager()) {
         dataFile = DataFile.findById(fileId);
      } else {
         dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
      }

      if (null == dataFile) {
         return badRequest("You do NOT have t		        object.setLogger(new AnnotationLogger(object, SolrUtils.getFieldValue(doc, \"log_str\").toString()));\n" +
         "he permission to operate this file!");
      }

      String pathProc = ConfigProp.getPathProc();
      String pathUnproc = ConfigProp.getPathUnproc();
      File file = new File(dataFile.getAbsolutePath());

      if (dataFile.getPureFileName().startsWith("DA-")) {
         Measurement.deleteFromSolr(dataFile.getDatasetUri());
         NameSpace.deleteTriplesByNamedGraph(URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri()));
      } else {
         deleteAddedTriples(file, dataFile);
      }

      dataFile.resetForUnprocessed();

      File destFolder = new File(pathUnproc);
      if (!destFolder.exists()){
         destFolder.mkdirs();
      }
      file.renameTo(new File(destFolder + "/" + dataFile.getStorageFileName()));
      file.delete();

      dataFile.getLogger().addLine(Feedback.println(Feedback.WEB,
      String.format("[OK] Moved file %s to unprocessed folder", dataFile.getPureFileName())));
      dataFile.save();

      return redirect(routes.AutoAnnotator.index(dir, "."));
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result moveDataFileToWorking(String fileId,Http.Request request) {
      final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

      DataFile dataFile = null;
      if (user.isDataManager()) {
         dataFile = DataFile.findByIdAndStatus(fileId, DataFile.UNPROCESSED);
      } else {
         dataFile = DataFile.findByIdAndOwnerEmailAndStatus(
         fileId, user.getEmail(), DataFile.UNPROCESSED);
      }

      if (null == dataFile) {
         return badRequest("You do NOT have the permission to operate this file!");
      }

      if (dataFile.existsInFileSystem(ConfigProp.getPathWorking())) {
         return badRequest("<a style=\"color:#cc3300; font-size: x-large;\">A file with this name already exists!</a>");
      }

      File file = new File(dataFile.getAbsolutePath());
      File destFolder = new File(ConfigProp.getPathWorking());
      if (!destFolder.exists()) {
         destFolder.mkdirs();
      }
      file.renameTo(new File(destFolder.getPath() + "/" + dataFile.getStorageFileName()));
      file.delete();

      dataFile.getLogger().resetLog();
      dataFile.setDir("");
      dataFile.setStatus(DataFile.WORKING);
      dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
      dataFile.save();

      return redirect(routes.AutoAnnotator.index("/", "."));
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result activateDataFile(String dir, String fileId, Http.Request request) {
      final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
      DataFile dataFile = null;
      if (user.isDataManager()) {
         dataFile = DataFile.findById(fileId);
      } else {
         dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
      }

      if (null == dataFile) {
         return badRequest("You do NOT have the permission to operate this file!");
      }

      dataFile.setStatus(DataFile.UNPROCESSED);
      dataFile.getLogger().resetLog();
      dataFile.save();

      return redirect(routes.AutoAnnotator.index(dir, "."));
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result deleteDataFile(String dir, String fileId, Http.Request request) {
      final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

      DataFile dataFile = null;
      if (user.isDataManager()) {
         dataFile = DataFile.findById(fileId);
      } else {
         dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
      }

      if (null == dataFile) {
         return badRequest("You do NOT have the permission to operate this file!");
      }

      File file = new File(dataFile.getAbsolutePath());

      if (dataFile.getPureFileName().startsWith("DA-")) {
         Measurement.deleteFromSolr(dataFile.getDatasetUri());
         NameSpace.deleteTriplesByNamedGraph(URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri()));
      } else {
         try {
            deleteAddedTriples(file, dataFile);
         } catch (Exception e) {
            System.out.print("Can not delete triples ingested by " + dataFile.getFileName() + " ..");
            file.delete();
            dataFile.delete();

            return redirect(routes.AutoAnnotator.index(dir, "."));
         }
      }
      file.delete();
      dataFile.delete();

      return redirect(routes.AutoAnnotator.index(dir, "."));
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result deleteDataFileOnly(String dir, String fileId, Http.Request request) {
      final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

      DataFile dataFile = null;
      if (user.isDataManager()) {
         dataFile = DataFile.findById(fileId);
      } else {
         dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
      }

      if (null == dataFile) {
         return badRequest("You do NOT have the permission to operate this file!");
      }

      File file = new File(dataFile.getAbsolutePath());
      file.delete();
      dataFile.delete();

      return redirect(routes.AutoAnnotator.index(dir, "."));
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public static void deleteAddedTriples(File file, DataFile dataFile) {
      System.out.println("Deleting the added triples from the moving file ...");

      if (!dataFile.attachFile(file)) {
         return;
      }

      GeneratorChain chain = AnnotationWorker.getGeneratorChain(dataFile);

      if (chain != null) {
         chain.delete();
      }
   }

   @Secure(authorizers = Constants.FILE_VIEWER_EDITOR_ROLE)
   public Result downloadDataFile(String fileId,Http.Request request) {
      final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));

      DataFile dataFile = null;
      dataFile = DataFile.findByIdAndEmail(fileId, null);

      if (null == dataFile) {
         return badRequest("Invalid file id!");
      }

      if (!user.isDataManager()) {
         if ( (dataFile.getOwnerEmail() != null && !dataFile.getOwnerEmail().equalsIgnoreCase(user.getEmail()))
                 && !dataFile.getViewerEmails().contains(user.getEmail())
                 && !dataFile.getEditorEmails().contains(user.getEmail())) {
            return badRequest("You do NOT have the permission to download this file!");
         }
      }

      return ok(new File(dataFile.getAbsolutePath())).withHeader("Content-disposition", String.format("attachment; filename=%s", dataFile.getFileName()));
   }

   // access to media files does no require ownership verification
   public Result downloadMediaFile(String mediaFileName) {
      //System.out.println("MediaFile: " + Paths.get(ConfigProp.getPathProc(), "media", mediaFileName.replace("file://", "")).toString());
      return ok(new File(Paths.get(ConfigProp.getPathProc(), "media", mediaFileName.replace("file://", "")).toString()));
   }

   @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
   public Result deleteFolder(String dir,Http.Request request) {
      List<DataFile> dfs = DataFile.findInDir(dir,DataFile.PROCESSED);
      int totFiles;
      if (dfs == null) {
         totFiles = 0;
      } else {
         totFiles = dfs.size();
      }

      List<String> folders = DataFile.findFolders(dir, false);
      boolean noSubFolders = (folders.size() == 0 || (folders.size() == 1 && folders.get(0) != null && folders.get(0).equals("..")));
      String statusMsg;
      if (noSubFolders) {
         statusMsg = "Folder can be deleted because it has no sub-folders.";
      } else {
         statusMsg = "Folder cannot be deleted becuase it has sub-folders.";
      }

      return ok(deleteFolder.render(dir, !noSubFolders, totFiles, statusMsg,application.getUserEmail(request)));
   }

   @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
   public Result postDeleteFolder(String dir,Http.Request request) {
      return deleteFolder(dir,request);
   }

   @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
   public Result processDeleteFolder(String dir) {
      List<DataFile> dfs = DataFile.findInDir(dir, DataFile.PROCESSED);
      for (DataFile df : dfs) {
         File file = new File(df.getAbsolutePath());
         System.out.println(df.getAbsolutePath() + "  " + df.getStorageFileName());

         if (df.getPureFileName().startsWith("DA-")) {
            Measurement.deleteFromSolr(df.getDatasetUri());
            NameSpace.deleteTriplesByNamedGraph(URIUtils.replacePrefixEx(df.getDataAcquisitionUri()));
         } else {
            try {
               deleteAddedTriples(file, df);
            } catch (Exception e) {
               System.out.print("Can not delete triples ingested by " + df.getFileName() + " ..");
               file.delete();
               df.delete();

               return redirect(routes.AutoAnnotator.index(dir, "."));
            }
         }
         file.delete();
         df.delete();
      }
      DataFile folder = new DataFile(dir);
      File folderFile = new File(folder.getAbsolutePath());
      System.out.println(folder.getFileName());
      System.out.println(folderFile.getAbsolutePath() + "  " + folderFile.getName());
      if (folderFile.exists()) {
         try {
            folderFile.delete();
            folder.delete();
         } catch (Exception e) {
            System.out.print("Can not delete folder " + dir + " itself");
            return redirect(routes.AutoAnnotator.index(dir, "."));
         }
      }
      return redirect(routes.AutoAnnotator.index(dir, ".."));
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result saveDataFile(Http.Request request) {
      FilePart uploadedfile = request.body().asMultipartFormData().getFile("file");

      JSONParser parser = new JSONParser();
      JSONObject params = null;
      try {
         String metadata = URLDecoder.decode(uploadedfile.getFilename(), "utf-8");
         params = (JSONObject)parser.parse(metadata);
      } catch (Exception e) {
         e.printStackTrace();
      }

      String fileId = (String)params.get("fileId");

      if (uploadedfile != null) {
         final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
         DataFile dataFile = DataFile.findByIdAndEmail(fileId, user.getEmail());
         if (null == dataFile) {
            return ok("<a style=\"color:#cc3300; font-size: large;\">This file can be modified only by its owner!</a>");
         }

         File file = new File(dataFile.getAbsolutePath());
         if (!file.exists()) {
            return ok("<a style=\"color:#cc3300; font-size: large;\">Could not find this file on records!</a>");
         }

         InputStream fileInputStream;
         try {
            DelegateTemporaryFile tf = (DelegateTemporaryFile)uploadedfile.getRef();
            tf.moveFileTo(file, true);
            /*
            fileInputStream = new FileInputStream((File)uploadedfile.getRef());
            System.out.println("Opened input stream");
            byte[] byteFile = IOUtils.toByteArray(fileInputStream);
            System.out.println("convert file");
            FileUtils.writeByteArrayToFile(file, byteFile);
            System.out.println("write file");
            fileInputStream.close();
            System.out.println("Save completed");
            */
         } catch (Exception e) {
            return ok("<a style=\"color:#cc3300; font-size: large;\">Error uploading file. Please try again.</a>");
         }

         return ok("<a style=\"color:#008000; font-size: large;\">File successfully saved!</a>");
      } else {
         return ok("<a style=\"color:#cc3300; font-size: large;\">Error uploading file. Please try again.</a>");
      }
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result uploadDataFileByChunking(
   String resumableChunkNumber,
   String resumableChunkSize,
   String resumableCurrentChunkSize,
   String resumableTotalSize,
   String resumableType,
   String resumableIdentifier,
   String resumableFilename,
   String resumableRelativePath,
   Http.Request request) {
      if (ResumableUpload.uploadFileByChunking(request,
      ConfigProp.getPathUnproc())) {
         //This Chunk has been Uploaded.
         return ok("Uploaded.");
      } else {
         return status(HttpStatus.SC_NOT_FOUND);
      }
   }

   @Secure(authorizers = Constants.DATA_OWNER_ROLE)
   public Result postUploadDataFileByChunking(
   String resumableChunkNumber,
   String resumableChunkSize,
   String resumableCurrentChunkSize,
   String resumableTotalSize,
   String resumableType,
   String resumableIdentifier,
   String resumableFilename,
   String resumableRelativePath,
   Http.Request request) {

      Path path = Paths.get(resumableFilename);
      if (path == null) {
         return badRequest("<a style=\"color:#cc3300; font-size: x-large;\">Could not get file path!</a>");
      }

      String fileName = path.getFileName().toString();

      if (ResumableUpload.postUploadFileByChunking(request, ConfigProp.getPathUnproc())) {
         DataFile dataFile = DataFile.create(
         fileName, "", AuthApplication.getLocalUser(application.getUserEmail(request)).getEmail(),
         DataFile.UNPROCESSED);

         String originalPath = Paths.get(ConfigProp.getPathUnproc(), dataFile.getPureFileName()).toString();
         File file = new File(originalPath);

         String newPath = originalPath.replace(
         "/" + dataFile.getPureFileName(),
         "/" + dataFile.getStorageFileName());
         file.renameTo(new File(newPath));
         file.delete();

         return(ok("Upload finished"));
      } else {
         return(ok("Upload"));
      }
   }
}
