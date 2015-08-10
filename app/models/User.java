package models;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;

import models.TokenAction.Type;
import play.data.format.Formats;
import play.data.validation.Constraints;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.*;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */

public class User extends AppModel implements Subject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Long id;
	
	@Field("id")
	public String id_s;

	@Constraints.Email
	@Field("email")
	public String email;

	@Field("name")
	public String name;
	
	@Field("first_name")
	public String firstName;
	
	@Field("last_name")
	public String lastName;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date lastLogin;
	
	public DateTime lastLogin_j;

	@Field("active")
	public boolean active;

	@Field("email_validated")
	public boolean emailValidated;

	public List<SecurityRole> roles;

	public List<LinkedAccount> linkedAccounts;

	public List<UserPermission> permissions;
	
	public String getLastLogin() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(this.lastLogin_j);
	}
	
	@Field("last_login")
	public void setLastLogin(String lastLogin) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		lastLogin_j = formatter.parseDateTime(lastLogin);
	}
	
	public List<String> getSecurityRoleId() {
		System.out.println("! User.getSecurityRoleId");
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
	public String getIdentifier()
	{
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
		final List<User> users;
		if (identity instanceof UsernamePasswordAuthUser) {
			users = getUsernamePasswordAuthUserFindSolr((UsernamePasswordAuthUser) identity);
		} else {
			users = getAuthUserFindSolr(identity);
		}
		return !users.isEmpty();
	}
	
	private static List<User> getAuthUserFindSolr(
			final AuthUserIdentity identity) {
		SolrClient solrClient = new HttpSolrClient("http://localhost:8983/solr/user");
		String query = "active:true AND provider_user_id:" + identity.getId() + " AND provider_key:" + identity.getProvider();
    	SolrQuery solrQuery = new SolrQuery(query);
    	List<User> users = new ArrayList<User>();
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			Iterator<SolrDocument> i = list.iterator();
			while (i.hasNext()) {
				User user = convertSolrDocumentToUser(i.next());
				users.add(user);
			}
		} catch (Exception e) {
			System.out.println("[ERROR] User.getAuthUserFindSolr - Exception message: " + e.getMessage());
		}
    	
    	return users;
	}

	public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
		return findByAuthUserIdentitySolr(identity);
	}
	
	public static User findByAuthUserIdentitySolr(final AuthUserIdentity identity) {
		if (identity == null) {
			return null;
		}
		if (identity instanceof UsernamePasswordAuthUser) {
			return findByUsernamePasswordIdentitySolr((UsernamePasswordAuthUser) identity);
		} else {
			List<User> users = getAuthUserFindSolr(identity); 
			if (users.size() == 1) {
				return users.get(0);
			} else {
				return null;
			}
		}
	}

	public static User findByUsernamePasswordIdentity(
			final UsernamePasswordAuthUser identity) {
		return findByUsernamePasswordIdentitySolr(identity);
	}
	
	public static User findByUsernamePasswordIdentitySolr(
			final UsernamePasswordAuthUser identity) {
		List<User> users = getUsernamePasswordAuthUserFindSolr(identity);
		if (users.size() == 1) {
			return users.get(0);
		} else {
			return null;
		}
	}
	
	public static User findByIdSolr(final String id) {
		SolrClient solrClient = new HttpSolrClient("http://localhost:8983/solr/users");
    	SolrQuery solrQuery = new SolrQuery("id:" + id);
    	User user = null;
    	
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

	private static List<User> getUsernamePasswordAuthUserFindSolr(
			final UsernamePasswordAuthUser identity) {
		return getEmailUserFindSolr(identity.getEmail(), identity.getProvider());
	}

	public void merge(final User otherUser) {
		mergeSolr(otherUser);
	}
	
	public void mergeSolr(final User otherUser) {
		for (final LinkedAccount acc : otherUser.linkedAccounts) {
			this.linkedAccounts.add(LinkedAccount.create(acc));
		}
		// do all other merging stuff here - like resources, etc.

		// deactivate the merged user that got added to this one
		otherUser.active = false;
		this.save();
		otherUser.save();
	}

	public static User create(final AuthUser authUser) {
		final User user = new User();
		user.roles = Collections.singletonList(SecurityRole
				.findByRoleNameSolr(controllers.AuthApplication.USER_ROLE));
		user.permissions = new ArrayList<UserPermission>();
		// user.permissions.add(UserPermission.findByValue("printers.edit"));
		user.active = true;
		user.lastLogin = new Date();
		user.linkedAccounts = Collections.singletonList(LinkedAccount
				.create(authUser));

		if (authUser instanceof EmailIdentity) {
			final EmailIdentity identity = (EmailIdentity) authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			user.email = identity.getEmail();
			user.emailValidated = false;
		}

		if (authUser instanceof NameIdentity) {
			final NameIdentity identity = (NameIdentity) authUser;
			final String name = identity.getName();
			if (name != null) {
				user.name = name;
			}
		}
		
		if (authUser instanceof FirstLastNameIdentity) {
		  final FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
		  final String firstName = identity.getFirstName();
		  final String lastName = identity.getLastName();
		  if (firstName != null) {
		    user.firstName = firstName;
		  }
		  if (lastName != null) {
		    user.lastName = lastName;
		  }
		}
		
		user.id_s = UUID.randomUUID().toString();

		user.save();
		// user.saveManyToManyAssociations("roles");
		// user.saveManyToManyAssociations("permissions");
		return user;
	}
	
	public void save() {
		SolrClient solrClient = new HttpSolrClient("http://localhost:8983/solr/users");
        
        try {
			solrClient.addBean(this);
			solrClient.commit();
			solrClient.close();
		} catch (Exception e) {
			System.out.println("[ERROR] User.save - Exception message: " + e.getMessage());
		}
        
        Iterator<LinkedAccount> i = linkedAccounts.iterator();
        while (i.hasNext()) {
        	LinkedAccount account = i.next();
        	account.user = this;
        	account.save();
        }
	}

	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
		mergeSolr(oldUser, newUser);
	}
	
	public static void mergeSolr(final AuthUser oldUser, final AuthUser newUser) {
		User.findByAuthUserIdentitySolr(oldUser).merge(
				User.findByAuthUserIdentitySolr(newUser));
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
		final User u = User.findByAuthUserIdentity(oldUser);
		u.linkedAccounts.add(LinkedAccount.create(newUser));
		u.save();
	}

	public static void setLastLoginDate(final AuthUser knownUser) {
		final User u = User.findByAuthUserIdentity(knownUser);
		u.lastLogin = new Date();
		u.save();
	}

	public static User findByEmail(final String email) {
		return findByEmailSolr(email);
	}
	
	public static User findByEmailSolr(final String email) {
		List<User> users = getEmailUserFindSolr(email);
		if (users.size() == 1) {
			return users.get(0);
		} else {
			return null;
		}
	}

	private static List<User> getEmailUserFindSolr(final String email) {
		return getEmailUserFindSolr(email, "");
	}
	
	private static List<User> getEmailUserFindSolr(final String email, final String providerKey) {
		SolrClient solrClient = new HttpSolrClient("http://localhost:8983/solr/users");
		String query = "email:" + email + " AND active:true";
    	SolrQuery solrQuery = new SolrQuery(query);
    	List<User> users = new ArrayList<User>();
    	
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			Iterator<SolrDocument> i = list.iterator();
			while (i.hasNext()) {
				User user = convertSolrDocumentToUser(i.next());
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

	public LinkedAccount getAccountByProvider(final String providerKey) {
		return LinkedAccount.findByProviderKey(this, providerKey);
	}
	
	public LinkedAccount getAccountByProviderSolr(final String providerKey) {
		return LinkedAccount.findByProviderKeySolr(this, providerKey);
	}

	public static void verify(final User unverified) {
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
	
	private static User convertSolrDocumentToUser(SolrDocument doc) {
		User user = new User();
		user.id_s = doc.getFieldValue("id").toString();
		user.email = doc.getFieldValue("email").toString();
		user.name = doc.getFieldValue("name").toString();
		user.firstName = doc.getFieldValue("first_name").toString();
		user.lastName = doc.getFieldValue("last_name").toString();
		user.setLastLogin(doc.getFieldValue("last_login").toString());
		user.active = Boolean.parseBoolean(doc.getFieldValue("active").toString());
		user.emailValidated = Boolean.parseBoolean(doc.getFieldValue("email_validated").toString());
		
		user.roles = new ArrayList<SecurityRole>();
		Iterator<Object> i = doc.getFieldValues("security_role_id").iterator();
		while (i.hasNext()) {
			user.roles.add(SecurityRole.findByIdSolr(i.next().toString()));
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
