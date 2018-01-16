package controllers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.providers.MyUsernamePasswordAuthProvider.MySignup;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;

import play.data.Form;
import play.mvc.Http.Context;
import play.mvc.Http.RequestBuilder;
import play.test.Helpers;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.SEE_OTHER;

import java.util.Map;
import java.util.HashMap;

import play.test.FakeApplication;

public class UserAuthTest {
	public static FakeApplication app;

	@BeforeClass
	public static void startApp() {
	    app = Helpers.fakeApplication(Helpers.inMemoryDatabase());
	    Helpers.start(app);
	}

	@AfterClass
	public static void stopApp() {
	    Helpers.stop(app);
	}
	
	@Test
	public void testRegisterUser() {
		
	}
	
	@Test
	public void testUnregisterUser() {
		
	}
	
	@Test
	public void testSignupUnregisteredUser() {
		MySignup signup = new MySignup();
		signup.email = "test@gmail.com";
		signup.name = "Test";
		signup.password = "password";
		signup.repeatPassword = signup.password;
		
		Form<MySignup> filledForm = Form.form(MySignup.class).fill(signup);
		if (SysUser.existsSolr()) {
			assertFalse(UserManagement.isPreRegistered(filledForm.get().email));
		}
	}
	
	@Test
	public void testSignupRegisteredUser() {
		String email = "test@gmail.com";
		Map<String, String> data = new HashMap<String, String>();
		data.put("email", email);
		data.put("name", "Test");
		data.put("password", "password");
		data.put("repeatPassword", "password");
		
		if (SysUser.existsSolr()) {
			assertFalse(UserManagement.isPreRegistered(email));
		}
		
		RequestBuilder request = Helpers.fakeRequest()
				.method("POST")
				.uri("/hadatac/signup")
				.bodyForm(data);
		
		int status = UsernamePasswordAuthProvider.handleSignup(new Context(request)).status();
		if (status != SEE_OTHER) {
			assertTrue(status == OK);
		}
	}
	
	@Test
	public void testRemoveUser() {
		
	}
}
