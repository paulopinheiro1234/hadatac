package org.hadatac.console.service;

import org.hadatac.console.models.SysUser;
import play.Application;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.service.UserServicePlugin;
import com.google.inject.Inject;

public class MyUserServicePlugin extends UserServicePlugin {

	@Inject
	public MyUserServicePlugin(final Application app) {
		super(app);
	}

	@Override
	public Object save(final AuthUser authUser) {
		return saveSolr(authUser);
	}
	
	public Object saveSolr(final AuthUser authUser) {
		final boolean isLinked = SysUser.existsByAuthUserIdentity(authUser);
		if (!isLinked) {
			return SysUser.create(authUser).getId();
		} else {
			// we have this user already, so return null
			return null;
		}
	}

	@Override
	public Object getLocalIdentity(final AuthUserIdentity identity) {
		// For production: Caching might be a good idea here...
		// ...and dont forget to sync the cache when users get deactivated/deleted
		final SysUser u = SysUser.findByAuthUserIdentity(identity);
		if(u != null) {
			return u.getId();
		} else {
			return null;
		}
	}

	@Override
	public AuthUser merge(final AuthUser newUser, final AuthUser oldUser) {
		if (!oldUser.equals(newUser)) {
			SysUser.merge(oldUser, newUser);
		}
		return oldUser;
	}

	@Override
	public AuthUser link(final AuthUser oldUser, final AuthUser newUser) {
		SysUser.addLinkedAccount(oldUser, newUser);
		return newUser;
	}
	
	@Override
	public AuthUser update(final AuthUser knownUser) {
		// User logged in again, bump last login date
		SysUser.setLastLoginDate(knownUser);
		return knownUser;
	}

}
