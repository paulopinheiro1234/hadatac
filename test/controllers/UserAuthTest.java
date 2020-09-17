package controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.SysUser;
import org.junit.Test;

import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http.RequestBuilder;
import play.test.Helpers;
import play.test.WithApplication;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;

import java.util.Map;
import java.util.HashMap;


public class UserAuthTest extends WithApplication{

	public static Application app;

	@Override
	protected Application provideApplication() {
	    return new GuiceApplicationBuilder().build();
	  }

	@Test
	public void testRegisterUser() {

	}

	@Test
	public void testUnregisterUser() {

	}
   // These were removed because they are guarented to fail on any server with at least 1 user.
   // The selenium login test has a similar test that can be configured by the user and will provide an avenue to pass
	/*
	@Test
	public void testSignupUnregisteredUser() {
		String email = "test@gmail.com";
		if (SysUser.existsSolr()) {
			assertFalse(UserManagement.isPreRegistered(email));
		}
	}

	@Test
	public void testSignupRegisteredUser() {
		String email = "zcjasonliang@gmail.com";
		Map<String, String> data = new HashMap<String, String>();
		data.put("email", email);
		data.put("name", "Test");
		data.put("password", "password");
		data.put("repeatPassword", "password");

		if (SysUser.existsSolr()) {
			assertTrue(UserManagement.isPreRegistered(email));
		}

		RequestBuilder request = Helpers.fakeRequest()
				.method("POST")
				.uri("/hadatac/signup")
				.bodyForm(data);

		int status = Helpers.route(request).status();
		if (status != SEE_OTHER) {
			assertTrue(status == OK);
		}
	}
	*/
	@Test
	public void testRemoveUser() {

	}
}
