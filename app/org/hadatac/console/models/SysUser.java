package org.hadatac.console.models;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.TokenAction.Type;
import play.Play;
import play.data.validation.Constraints;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.noggit.JSONUtil;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hadatac.entity.pojo.User;

import org.hadatac.utils.Collections;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */

public class SysUser implements Subject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Long id;
	
	@Field("id")
	private String id_s;
	
	@Field("uri")
	private String uri;

	@Constraints.Email
	@Field("email")
	private String email;

	@Field("name")
	private String name;
	
	@Field("first_name")
	private String firstName;
	
	@Field("last_name")
	private String lastName;

	@Field("last_login")
	private String lastLogin;

	@Field("active")
	private boolean active;

	@Field("email_validated")
	private boolean emailValidated;

	private Instant lastLogin_j;
	
	private List<SecurityRole> roles;

	private List<LinkedAccount> linkedAccounts;

	private List<UserPermission> permissions;
	
	public SysUser() {
		roles = new ArrayList<SecurityRole>();
	}

	public String getId() {
		return id_s;
	}
	public void setId(String id) {
		this.id_s = id;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri; 
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email; 
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name; 
	}
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName; 
	}
	
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName; 
	}
	
	public String getLastLogin() {
		return this.lastLogin_j.toString();
	}
	
	public void setLastLogin(String lastLogin) {
		lastLogin_j = Instant.parse(lastLogin);
	}
	
	public boolean getActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active; 
	}
	
	public boolean getEmailValidated() {
		return emailValidated;
	}
	public void setEmailValidated(boolean emailValidated) {
		this.emailValidated = emailValidated;
	}
	
	public List<LinkedAccount> getLinkedAccounts() {
		return linkedAccounts;
	}
	public void setLinkedAccounts(List<LinkedAccount> linkedAccounts) {
		this.linkedAccounts = linkedAccounts;
	}
	
	public List<String> getSecurityRoleId() {
		List<String> list = new ArrayList<String>();
		Iterator<SecurityRole> roleIterator = roles.iterator();
		while (roleIterator.hasNext()) {
			SecurityRole role = roleIterator.next();
			list.add(role.id_s);
		}
		return list;
	}
	
	@Field("security_role_id")
	public void setSecurityRoleId(List<String> list) {
		Iterator<String> listIterator = list.iterator();
		while (listIterator.hasNext()) {
			String id = listIterator.next();
			SecurityRole role = new SecurityRole();
			role.id_s = id;
			roles.add(role);
		}
	}
	
	public boolean isDataManager() {
		SecurityRole target = SecurityRole.findByRoleNameSolr(AuthApplication.DATA_MANAGER_ROLE);
		for(SecurityRole r : roles) {
			if(r.id_s.equals(target.id_s)){
				return true;
			}
		}
		return false;
	}
	
	public boolean isEmailValidated() {
		return emailValidated;
	}
	
	public void addSecurityRole(String role_name) {
		SecurityRole new_role = SecurityRole.findByRoleNameSolr(role_name);
		boolean isRoleExisted = false;
		Iterator<SecurityRole> iterRoles = roles.iterator();
		while (iterRoles.hasNext()) {
			SecurityRole role = iterRoles.next();
			if(role.id_s.equals(new_role.id_s)){
				isRoleExisted = true;
			}
		}
		if(!isRoleExisted){
			roles.add(new_role);
		}
	}
	
	public void removeSecurityRole(String role_name) {
		SecurityRole new_role = SecurityRole.findByRoleNameSolr(role_name);
		Iterator<SecurityRole> iterRoles = roles.iterator();
		while (iterRoles.hasNext()) {
			SecurityRole role = iterRoles.next();
			if(role.id_s.equals(new_role.id_s)){
				iterRoles.remove();
			}
		}
	}
	
	public List<String> getUserPermissionId() {
		List<String> list = new ArrayList<String>();
		Iterator<UserPermission> permissionIterator = permissions.iterator();
		while (permissionIterator.hasNext()) {
			UserPermission permission = permissionIterator.next();
			list.add(permission.id_s);
		}
		return list;
	}
	
	@Field("user_permission_id")
	public void setUserPermissionId(List<String> list) {
		Iterator<String> listIterator = list.iterator();
		while (listIterator.hasNext()) {
			String id = listIterator.next();
			UserPermission permission = new UserPermission();
			permission.id_s = id;
			permissions.add(permission);
		}
	}

	@Override
	public String getIdentifier() {
		return Long.toString(this.id);
	}

	@Override
	public List<? extends Role> getRoles() {
		return this.roles;
	}

	@Override
	public List<? extends Permission> getPermissions() {
		return this.permissions;
	}

	public static boolean existsByAuthUserIdentity(
			final AuthUserIdentity identity) {
		return existsByAuthUserIdentitySolr(identity);
	}
	
	public static boolean existsByAuthUserIdentitySolr(
			final AuthUserIdentity identity) {
		final List<SysUser> users;
		if (identity instanceof UsernamePasswordAuthUser) {
			users = getUsernamePasswordAuthUserFindSolr((UsernamePasswordAuthUser) identity);
		} else {
			users = getAuthUserFindSolr(identity);
		}
		return !users.isEmpty();
	}
	
	private static List<SysUser> getAuthUserFindSolr(
		final AuthUserIdentity identity) {
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_USERS).build();
		String query = "active:true AND provider_user_id:" + identity.getId() + " AND provider_key:" + identity.getProvider();
    	SolrQuery solrQuery = new SolrQuery(query);
    	List<SysUser> users = new ArrayList<SysUser>();
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			Iterator<SolrDocument> i = list.iterator();
			while (i.hasNext()) {
				SysUser user = convertSolrDocumentToUser(i.next());
				users.add(user);
			}
		} catch (Exception e) {
			System.out.println("[ERROR] User.getAuthUserFindSolr - Exception message: " + e.getMessage());
		}
    	
    	return users;
	}

	public static SysUser findByAuthUserIdentity(final AuthUserIdentity identity) {
		return findByAuthUserIdentitySolr(identity);
	}
	
	public static SysUser findByAuthUserIdentitySolr(final AuthUserIdentity identity) {
		if (identity == null) {
			return null;
		}
		if (identity instanceof UsernamePasswordAuthUser) {
			return findByUsernamePasswordIdentitySolr((UsernamePasswordAuthUser) identity);
		} else {
			List<SysUser> users = getAuthUserFindSolr(identity); 
			if (users.size() == 1) {
				return users.get(0);
			} else {
				return null;
			}
		}
	}

	public static SysUser findByUsernamePasswordIdentity(
			final UsernamePasswordAuthUser identity) {
		return findByUsernamePasswordIdentitySolr(identity);
	}
	
	public static SysUser findByUsernamePasswordIdentitySolr(
			final UsernamePasswordAuthUser identity) {
		List<SysUser> users = getUsernamePasswordAuthUserFindSolr(identity);
		if (users.size() == 1) {
			return users.get(0);
		} else {
			return null;
		}
	}
	
	public static SysUser findByIdSolr(final String id) {
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_USERS).build();
		
    	SolrQuery solrQuery = new SolrQuery("id:" + id);
    	SysUser user = null;
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() == 1) {
				user = convertSolrDocumentToUser(list.get(0));
			}
		} catch (Exception e) {
			System.out.println("[ERROR] TokenAction.findByTokenSolr - Exception message: " + e.getMessage());
		}
    	
    	return user;
	}

	private static List<SysUser> getUsernamePasswordAuthUserFindSolr(
			final UsernamePasswordAuthUser identity) {
		return getEmailUserFindSolr(identity.getEmail(), identity.getProvider());
	}

	public void merge(final SysUser otherUser) {
		mergeSolr(otherUser);
	}
	
	public void mergeSolr(final SysUser otherUser) {
		for (final LinkedAccount acc : otherUser.linkedAccounts) {
			this.linkedAccounts.add(LinkedAccount.create(acc));
		}
		// do all other merging stuff here - like resources, etc.

		// deactivate the merged user that got added to this one
		otherUser.active = false;
		this.save();
		otherUser.save();
	}
	
	public static SysUser create(final AuthUser authUser, String uri) {
		final SysUser sys_user = new SysUser();
		
		sys_user.roles.add(SecurityRole
				.findByRoleNameSolr(AuthApplication.DATA_OWNER_ROLE));
		sys_user.permissions = new ArrayList<UserPermission>();
		sys_user.active = true;
		sys_user.lastLogin = Instant.now().toString();
		sys_user.linkedAccounts = java.util.Collections.singletonList(LinkedAccount
				.create(authUser));

		if (authUser instanceof EmailIdentity) {
			final EmailIdentity identity = (EmailIdentity) authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			sys_user.email = identity.getEmail();
			sys_user.emailValidated = false;
		}

		if (authUser instanceof NameIdentity) {
			final NameIdentity identity = (NameIdentity) authUser;
			final String name = identity.getName();
			if (name != null) {
				sys_user.name = name;
			}
		}
		
		if (authUser instanceof FirstLastNameIdentity) {
		  final FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
		  final String firstName = identity.getFirstName();
		  final String lastName = identity.getLastName();
		  if (firstName != null) {
			  sys_user.firstName = firstName;
		  }
		  if (lastName != null) {
			  sys_user.lastName = lastName;
		  }
		}
		
		sys_user.id_s = UUID.randomUUID().toString();
		
		if (!SysUser.existsSolr()) {
			sys_user.roles.add(SecurityRole
					.findByRoleNameSolr(AuthApplication.DATA_MANAGER_ROLE));
			sys_user.emailValidated = true;
			
			String admin_uri = "http://localhost/users#admin";
			User user = new User();
			user.setName(sys_user.name);
			user.setEmail(sys_user.email);
			user.setUri(admin_uri);
			
			if(null == uri){
				sys_user.uri = admin_uri;
			}
			else{
				sys_user.uri = uri;
			}
			user.save();
			sys_user.save();
			
			return sys_user;
		}

		if(null == uri) {
			sys_user.uri = "";
		}
		else {
			sys_user.uri = uri;
		}
		sys_user.save();
		
		return sys_user;
	}
	
	public static boolean existsSolr() {
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_USERS).build();
    	SolrQuery solrQuery = new SolrQuery("*:*");
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() > 0) {
				return true;
			}
		} catch (Exception e) {
			System.out.println("[ERROR] User.existsSolr - Exception message: " + e.getMessage());
		}
    	
    	return false;
	}

	public static SysUser create(final AuthUser authUser) {
		final SysUser sys_user = new SysUser();
		
		sys_user.roles.add(SecurityRole
				.findByRoleNameSolr(org.hadatac.console.controllers.AuthApplication.DATA_OWNER_ROLE));
		sys_user.permissions = new ArrayList<UserPermission>();
		sys_user.active = true;
		sys_user.lastLogin = Instant.now().toString();
		sys_user.linkedAccounts = java.util.Collections.singletonList(LinkedAccount
				.create(authUser));

		if (authUser instanceof EmailIdentity) {
			final EmailIdentity identity = (EmailIdentity) authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			sys_user.email = identity.getEmail();
			sys_user.emailValidated = false;
		}

		if (authUser instanceof NameIdentity) {
			final NameIdentity identity = (NameIdentity) authUser;
			final String name = identity.getName();
			if (name != null) {
				sys_user.name = name;
			}
		}
		
		if (authUser instanceof FirstLastNameIdentity) {
		  final FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
		  final String firstName = identity.getFirstName();
		  final String lastName = identity.getLastName();
		  if (firstName != null) {
			  sys_user.firstName = firstName;
		  }
		  if (lastName != null) {
			  sys_user.lastName = lastName;
		  }
		}
		
		sys_user.id_s = UUID.randomUUID().toString();
		
		User user = new User();
		user.setName(sys_user.name);
		user.setEmail(sys_user.email);
		
		if (SysUser.existsSolr() == false) {
			sys_user.roles.add(SecurityRole
					.findByRoleNameSolr(org.hadatac.console.controllers.AuthApplication.DATA_MANAGER_ROLE));
			sys_user.emailValidated = true;
			user.setUri("http://localhost/users#admin");
		}

		user.save();
		sys_user.save();
		
		return sys_user;
	}
	
	public void save() {
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_USERS).build();
        
        try {
			solrClient.addBean(this);
			solrClient.commit();
			solrClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        Iterator<LinkedAccount> i = linkedAccounts.iterator();
        while (i.hasNext()) {
        	LinkedAccount account = i.next();
        	account.user = this;
        	account.save();
        }
	}
	
	public int delete() {
		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.users") 
					+ Collections.AUTHENTICATE_USERS).build();
			UpdateResponse response = solr.deleteById(this.id_s);
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] SysUser.delete() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] SysUser.delete() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] SysUser.delete() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}

	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
		mergeSolr(oldUser, newUser);
	}
	
	public static void mergeSolr(final AuthUser oldUser, final AuthUser newUser) {
		SysUser.findByAuthUserIdentitySolr(oldUser).merge(
				SysUser.findByAuthUserIdentitySolr(newUser));
	}

	public Set<String> getProviders() {
		final Set<String> providerKeys = new HashSet<String>(
				this.linkedAccounts.size());
		for (final LinkedAccount acc : this.linkedAccounts) {
			providerKeys.add(acc.providerKey);
		}
		return providerKeys;
	}

	public static void addLinkedAccount(final AuthUser oldUser,
			final AuthUser newUser) {
		final SysUser u = SysUser.findByAuthUserIdentity(oldUser);
		if (null != u) {
			u.linkedAccounts.add(LinkedAccount.create(newUser));
			u.save();
		}
	}

	public static void setLastLoginDate(final AuthUser knownUser) {
		final SysUser u = SysUser.findByAuthUserIdentity(knownUser);
		if (null != u) {
			u.lastLogin = Instant.now().toString();
			u.save();
		}
	}

	public static SysUser findByEmail(final String email) {
		return findByEmailSolr(email);
	}
	
	public static SysUser findByEmailSolr(final String email) {
		List<SysUser> users = getEmailUserFindSolr(email);
		if (users.size() == 1) {
			return users.get(0);
		} else {
			return null;
		}
	}

	private static List<SysUser> getEmailUserFindSolr(final String email) {
		return getEmailUserFindSolr(email, "");
	}
	
	private static List<SysUser> getEmailUserFindSolr(final String email, final String providerKey) {
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_USERS).build();
		String query = "email:" + email + " AND active:true";
    	SolrQuery solrQuery = new SolrQuery(query);
    	List<SysUser> users = new ArrayList<SysUser>();
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			Iterator<SolrDocument> i = list.iterator();
			while (i.hasNext()) {
				SysUser user = convertSolrDocumentToUser(i.next());
				users.add(user);
				if (!providerKey.isEmpty()) {
					LinkedAccount account = LinkedAccount.findByProviderKeySolr(user, providerKey);
					if (account == null) {
						users.remove(user);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("[ERROR] User.getEmailUserFindSolr - Exception message: " + e.getMessage());
		}
    	
    	return users;
	}
	
	public static String outputAsJson() {
		SolrClient solrClient = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.users") 
				+ Collections.AUTHENTICATE_USERS).build();
		String query = "*:*";
    	SolrQuery solrQuery = new SolrQuery(query);
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList docs = queryResponse.getResults();
			return JSONUtil.toJSON(docs);
		} catch (Exception e) {
			System.out.println("[ERROR] SysUser.outputAsJson - Exception message: " + e.getMessage());
		}
    	
    	return "";
	}

	public LinkedAccount getAccountByProvider(final String providerKey) {
		return LinkedAccount.findByProviderKey(this, providerKey);
	}
	
	public LinkedAccount getAccountByProviderSolr(final String providerKey) {
		return LinkedAccount.findByProviderKeySolr(this, providerKey);
	}

	public static void verify(final SysUser unverified) {
		// You might want to wrap this into a transaction
		unverified.emailValidated = true;
		unverified.save();
		TokenAction.deleteByUser(unverified, Type.EMAIL_VERIFICATION);
	}

	public void changePassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		changePasswordSolr(authUser, create);
	}
	
	public void changePasswordSolr(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		LinkedAccount a = this.getAccountByProviderSolr(authUser.getProvider());
		if (a == null) {
			if (create) {
				a = LinkedAccount.create(authUser);
				a.user = this;
			} else {
				throw new RuntimeException(
						"Account not enabled for password usage");
			}
		}
		a.providerUserId = authUser.getHashedPassword();
		a.save();
	}

	public void resetPassword(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		// You might want to wrap this into a transaction
		resetPasswordSolr(authUser, create);
	}
	
	public void resetPasswordSolr(final UsernamePasswordAuthUser authUser,
			final boolean create) {
		// You might want to wrap this into a transaction
		this.changePassword(authUser, create);
		TokenAction.deleteByUserSolr(this, Type.PASSWORD_RESET);
	}
	
	private static SysUser convertSolrDocumentToUser(SolrDocument doc) {
		SysUser user = new SysUser();
		user.id_s = doc.getFieldValue("id").toString();
		user.uri = doc.getFieldValue("uri").toString();
		user.email = doc.getFieldValue("email").toString();
		user.name = doc.getFieldValue("name").toString();
		user.firstName = doc.getFieldValue("first_name").toString();
		user.lastName = doc.getFieldValue("last_name").toString();
		if (null == doc.getFieldValue("last_login")) {
			user.setLastLogin(Instant.now().toString());
		}
		else {
			user.setLastLogin(doc.getFieldValue("last_login").toString());
		}
		user.active = Boolean.parseBoolean(doc.getFieldValue("active").toString());
		user.emailValidated = Boolean.parseBoolean(doc.getFieldValue("email_validated").toString());
		
		user.roles = new ArrayList<SecurityRole>();
		Iterator<Object> i = doc.getFieldValues("security_role_id").iterator();
		while (i.hasNext()) {
			SecurityRole role = SecurityRole.findByIdSolr(i.next().toString());
			if (null != role) {
				user.roles.add(role);
			}
		}
		
		user.permissions = new ArrayList<UserPermission>();
		if (doc.getFieldValues("user_permission_id") != null) {
			i = doc.getFieldValues("user_permission_id").iterator();
			while (i.hasNext()) {
				user.permissions.add(UserPermission.findByIdSolr(i.next().toString()));
			}
		}
		
		user.linkedAccounts = LinkedAccount.findByIdSolr(user);
		
		return user;
	}
}
