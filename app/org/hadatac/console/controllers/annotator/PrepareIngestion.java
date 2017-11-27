package org.hadatac.console.controllers.annotator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URLEncoder;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.State;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.console.models.AssignOptionForm;
import org.hadatac.console.models.SelectScopeForm;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.annotator.FileProcessing;
import org.hadatac.console.controllers.annotator.routes;
import org.hadatac.console.models.SysUser;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.data.Form;
import play.mvc.*;
import play.mvc.Result;

public class PrepareIngestion extends Controller {

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result create(String file_name, String da_uri) {

		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					routes.PrepareIngestion.create(file_name,da_uri).url()));
		}

		final String kbPrefix = ConfigProp.getKbPrefix();
		String ownerEmail = "";
		DataAcquisition da = null;
		DataFile file = null;

		try {
			file_name = URLEncoder.encode(file_name, "UTF-8");
		} catch (Exception e) {
			System.out.println("[ERROR] encoding file name");
		}

		ownerEmail = AuthApplication.getLocalUser(session()).getEmail();
		file = DataFile.findByName(ownerEmail, file_name);
		if (file == null) {
			return ok(prepareIngestion.render(file_name, da, "[ERROR] Could not update file records with new DA information"));
		}
		System.out.println("DataFile's Dataset URI : [" + file.getDatasetUri() + "]");

		// Load associated DA
		if (da_uri != null && !da_uri.equals("")) {
			da = DataAcquisition.findByUri(ValueCellProcessing.replacePrefixEx(da_uri));
			System.out.println("Global scope: [" + da.getGlobalScopeUri() + "]  hasScope: " + da.hasScope());

			if (da != null) {
				return ok(prepareIngestion.render(file_name, da, "DA associated with file has been retrieved"));
			} else {
				String message = "[ERROR] Could not load assigned DA from DA's URI : " + da_uri;
				return badRequest(message);
			}
		}

		// OR create a new DA if the file is not associated with any existing DA

		String da_label = "";
		String new_da_uri = "";

		if (!file_name.startsWith("DA-")) {
			da_label = "DA-" + file_name;
		} else {
			da_label = file_name;
		}
		da_label = da_label.replace(".csv","").replace(".","").replace("+","-");
		new_da_uri = kbPrefix + da_label;

		da = new DataAcquisition();
		da.setTriggeringEvent(TriggeringEvent.INITIAL_DEPLOYMENT);
		da.setLabel(da_label);
		da.setUri(ValueCellProcessing.replacePrefixEx(new_da_uri));

		SysUser user = SysUser.findByEmail(ownerEmail);
		if (user == null) {
			System.out.println("The specified owner email " + ownerEmail + " is not a valid user!");
		} else {
			da.setOwnerUri(user.getUri());
			da.setPermissionUri(user.getUri());
		}

		da.save();

		// save DA
		try {
			da.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		} catch (CommandException e) {
			//System.out.println("[Warning] Creating new Data Acquisition: error from PrepareIngestion's saveToLabKey()");
		}

		file.setDataAcquisitionUri(da.getUri());
		file.save();

		return ok(prepareIngestion.render(file_name, da, "New data acquisition has been created to support file ingestion"));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postCreate(String file_name, String da_uri) {
		return create(file_name, da_uri);
	}
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result reconfigure(String file_name, String da_uri) {
		DataAcquisition dataAcquisition = DataAcquisition.findByUri(da_uri);
		if (null != dataAcquisition) {
			dataAcquisition.setStatus(0);
			dataAcquisition.save();
		}
		return create(file_name, da_uri);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result refine(String file_name, String da_uri, String message) {

		DataAcquisition da = null;

		// Load associated DA
		if (da_uri != null && !da_uri.equals("")) {
			da = DataAcquisition.findByUri(da_uri);
			if (da != null) {
				return ok(prepareIngestion.render(file_name, da, message));
			} else {
				System.out.println("[ERROR] Could not load assigned DA from DA's URI");
			}
		}
		return badRequest("[ERROR] In PrepareIngestion.refine, cannot retrieve DA from provided URI");
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result postRefine(String file_name, String da_uri, String message) {
		return refine(file_name, da_uri, message);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result selectStudy(String file_name, String da_uri) {
		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					routes.PrepareIngestion.selectStudy(file_name,da_uri).url()));
		}

		List<Study> studies = Study.find();

		return ok(selectStudy.render(file_name, da_uri, studies));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result selectScope(String file_name, String da_uri, String std_uri) {
		if (session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
			return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
					routes.PrepareIngestion.selectScope(file_name,da_uri, std_uri).url()));
		}

		String[] fields = null;
		String globalScope = null;
		String globalScopeUri = null;
		List<String> localScope = null;
		List<String> localScopeUri = null;
		String labelsStr = "";
		String path = "";
		String labels = "";

		try {
			file_name = URLEncoder.encode(file_name, "UTF-8");
		} catch (Exception e) {
			System.out.println("[ERROR] encoding file name");
		}

		System.out.println("file <" + file_name + ">");
		path = ConfigProp.getPathUnproc();
		System.out.println("Path: " + path + "  Name: " + file_name);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path + "/" + file_name));
			StringBuilder builder = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				builder.append(line);
				break;
			}
			if(!builder.toString().trim().equals("")) {
				labels = builder.toString();
			}
		} catch (Exception e) {
			System.out.println("Could not process uploaded file.");
		}
		System.out.println("selectScope: labels = <" + labels + ">");
		if (labels != null && !labels.equals("")) {
			fields = FileProcessing.extractFields(labels);
			localScope = new ArrayList<String>();
			localScopeUri = new ArrayList<String>();
			for (String str : fields) {
				localScope.add("no mapping");
				localScopeUri.add("");
			}
			System.out.println("# of fields: " + fields.length);
		}

		Study study = Study.find(std_uri);
		System.out.println("Study uri: " + std_uri);
		System.out.println("StudygetUri(): " + study.getUri());
		System.out.println("Study name: " + study.getLabel());
		List<ObjectCollection> ocList = ObjectCollection.findDomainByStudy(study);
		System.out.println("Collection list size: " + ocList.size());

		return ok(selectScope.render(file_name, da_uri, ocList, Arrays.asList(fields), globalScope, globalScopeUri, localScope, localScopeUri));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result selectDeployment(String file_name, String da_uri) {

		State active = new State(State.ACTIVE);

		List<Deployment> deployments = Deployment.find(active);

		return ok(selectDeployment.render(file_name, da_uri, deployments));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result selectSchema(String file_name, String da_uri) {

		List<DataAcquisitionSchema> schemas = DataAcquisitionSchema.findAll();

		return ok(selectSchema.render(file_name, da_uri, schemas));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processSelectStudy(String file_name, String da_uri) {
		Form<AssignOptionForm> form = Form.form(AssignOptionForm.class).bindFromRequest();
		String message = "";
		AssignOptionForm data = form.get();
		String std_uri = data.getOption();
		//System.out.println("Showing returned option: " + std_uri);

		if (std_uri != null && !std_uri.equals("")) {

			Study std = Study.find(std_uri);
			if (std == null) {
				message = "ERROR - Could not retrieve study from its URI.";
				return refine(file_name, da_uri, message);
			}

			DataAcquisition da = DataAcquisition.findByUri(da_uri);
			if (da == null) {
				message = "ERROR - Could not retrieve Data Acquisition from its URI.";
				return refine(file_name, da_uri, message);
			}

			da.setStudyUri(std_uri);

			try {
				da.save();
				da.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
			} catch (CommandException e) {
			}
			return ok(prepareIngestion.render(file_name, da, "Updated Data Acquisition with deployment information"));
		}

		message = "DA is now associated with study " + std_uri;
		return refine(file_name, da_uri, message);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processSelectScope(String file_name, String da_uri) {
		Form<SelectScopeForm> form = Form.form(SelectScopeForm.class).bindFromRequest();
		String message = "";
		SelectScopeForm data = form.get();
		String globalScopeUri = data.getNewGlobalScopeUri();
		String localScopeUri = data.getNewLocalScopeUri();
		System.out.println("Showing returned GlobalScope: [" + globalScopeUri + "]");
		System.out.println("Showing returned LocalScope: [" + localScopeUri + "]");
		String[] localUriStr = null;
		List<String> localScopeUriList = new ArrayList<String>();
		if (localScopeUri != null) {
			localUriStr = localScopeUri.split(",");
			System.out.println("List of local scope uris:");
			for (int i=0; i < localUriStr.length; i++) {
				localUriStr[i] = localUriStr[i].trim();
				System.out.println("local scope: [" + localUriStr[i] + "]");
			}
			localScopeUriList = Arrays.asList(localUriStr);
		}

		DataAcquisition da = DataAcquisition.findByUri(da_uri);
		if (da == null) {
			message = "ERROR - Could not retrieve Data Acquisition from its URI.";
			return refine(file_name, da_uri, message);
		}

		da.setGlobalScopeUri(globalScopeUri);
		da.setLocalScopeUri(localScopeUriList);

		try {
			da.save();
			da.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		} catch (CommandException e) {
		}

		return ok(prepareIngestion.render(file_name, da, "Updated Data Acquisition with scope information"));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processSelectDeployment(String file_name, String da_uri) {
		Form<AssignOptionForm> form = Form.form(AssignOptionForm.class).bindFromRequest();
		String message = "";
		AssignOptionForm data = form.get();
		String dep_uri = data.getOption();
		//System.out.println("Showing returned option: " + dep_uri);

		if (dep_uri != null && !dep_uri.equals("")) {

			Deployment dep = Deployment.find(dep_uri);
			if (dep == null) {
				message = "ERROR - Could not retrieve Deployment from its URI.";
				return refine(file_name, da_uri, message);
			}

			DataAcquisition da = DataAcquisition.findByUri(da_uri);
			if (da == null) {
				message = "ERROR - Could not retrieve Data Acquisition from its URI.";
				return refine(file_name, da_uri, message);
			}

			da.setDeploymentUri(dep_uri);

			try {
				da.save();
				da.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
			} catch (CommandException e) {
			}
			return ok(prepareIngestion.render(file_name, da, "Updated Data Acquisition with deployment information"));
		}

		message = "DA is now associated with deployment " + dep_uri;
		return refine(file_name, da_uri, message);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result processSelectSchema(String file_name, String da_uri) {
		Form<AssignOptionForm> form = Form.form(AssignOptionForm.class).bindFromRequest();
		String message = "";
		AssignOptionForm data = form.get();
		String das_uri = data.getOption();
		//System.out.println("Showing returned option: " + das_uri);

		if (das_uri != null && !das_uri.equals("")) {

			DataAcquisitionSchema das = DataAcquisitionSchema.find(das_uri);
			if (das == null) {
				message = "ERROR - Could not retrieve Data Acquisition Schema from its URI.";
				return refine(file_name, da_uri, message);
			}

			DataAcquisition da = DataAcquisition.findByUri(da_uri);
			if (da == null) {
				message = "ERROR - Could not retrieve Data Acquisition from its URI.";
				return refine(file_name, da_uri, message);
			}

			da.setSchemaUri(das_uri);

			try {
				da.save();
				da.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
			} catch (CommandException e) {
			}
			return ok(prepareIngestion.render(file_name, da, "Updated Data Acquisition with data acquisition schema information"));
		}

		message = "DA is now associated with data acquisition schema " + das_uri;
		return refine(file_name, da_uri, message);
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result removeAssociation(String file_name, String da_uri, String daComponent) {

		String message = "";
		DataAcquisition da = DataAcquisition.findByUri(da_uri);
		if (da == null) {
			message = "ERROR - Could not retrieve Data Acquisition from its URI.";
			return refine(file_name, da_uri, message);
		}

		switch (daComponent) {

		// removing a study's relationship also removes scope information
		case "Study":  
			da.setStudyUri("");
			da.setGlobalScopeUri("");
			da.setGlobalScopeName("");
			da.setLocalScopeUri(new ArrayList<String>());
			da.setLocalScopeName(new ArrayList<String>());
			break;

		case "Scope":  
			da.setGlobalScopeUri("");
			da.setGlobalScopeName("");
			da.setLocalScopeUri(new ArrayList<String>());
			da.setLocalScopeName(new ArrayList<String>());
			break;

		case "Deployment":  
			da.setDeploymentUri("");
			break;

		case "Schema":  
			da.setSchemaUri("");
		}

		try {
			da.save();
			da.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		} catch (CommandException e) {
		}
		message = "Association with " + daComponent + " removed from the Data Acquisition.";
		return ok(prepareIngestion.render(file_name, da, message));
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public static Result completeDataAcquisition(String file_name, String da_uri) {

		String message = "";
		DataAcquisition da = DataAcquisition.findByUri(da_uri);
		if (da == null) {
			message = "ERROR - Could not retrieve Data Acquisition from its URI.";
			return refine(file_name, da_uri, message);
		}

		da.setStatus(9999);

		try {
			da.save();
			da.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		} catch (CommandException e) {
		}
		message = "Data Acquisition set as complete";
		return ok(prepareIngestion.render(file_name, da, message));
	}

}


