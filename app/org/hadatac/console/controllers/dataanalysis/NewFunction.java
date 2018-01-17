package org.hadatac.console.controllers.dataanalysis;

import org.hadatac.console.views.html.dataanalysis.*;

import java.util.List;
import java.util.Map;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.indicators.routes;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.models.IndicatorForm;
import org.hadatac.console.models.SysUser;
import org.hadatac.entity.pojo.Aggregate;
import org.hadatac.entity.pojo.Attribute;
import org.hadatac.entity.pojo.Characteristic;
import org.hadatac.entity.pojo.Entity;
import org.hadatac.entity.pojo.Indicator;
import org.hadatac.entity.pojo.Unit;
import org.hadatac.entity.pojo.facet.EntityCharacteristic;
import org.hadatac.metadata.loader.ValueCellProcessing;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class NewFunction extends Controller {
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index() {
		final SysUser sysUser = AuthApplication.getLocalUser(session());
		
    	// may need addressing
    	Indicator indicator = new Indicator();
    	List<Attribute> attributes = Attribute.find();
    	List<Unit> units = Unit.find();
    	List<Aggregate> aggregates = Aggregate.find();
    	EntityCharacteristic entityCharacteristic = EntityCharacteristic.create(sysUser.getUri());
    	return ok(newFunction.render(indicator, entityCharacteristic, aggregates));
    }
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex() {
    	return index();
    }
	
	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processForm() {
    	final SysUser sysUser = AuthApplication.getLocalUser(session());
	
        Form<IndicatorForm> form = Form.form(IndicatorForm.class).bindFromRequest();
        IndicatorForm data = form.get();
        
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }
        
		// store new values
		String newURI = ValueCellProcessing.replacePrefixEx(data.getNewUri());
		if (newURI == null || newURI.equals("")) {
	            return badRequest("[ERROR] New URI cannot be empty.");
		}
		String newLabel = data.getNewLabel();
		String newComment = data.getNewComment();
	
	        // insert current state of the STD
		Indicator ind = new Indicator(DynamicFunctions.replacePrefixWithURL(newURI),
				      newLabel,
				      newComment);
		
		// insert the new indicator content inside of the triplestore
		ind.save();
		
		// update/create new indicator in LabKey
		int nRowsAffected = ind.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
		if (nRowsAffected <= 0) {
		    return badRequest("Failed to insert new indicator to LabKey!\n");
		}
		
		
		return ok(newFunctionConfirm.render(ind));
    }
}
