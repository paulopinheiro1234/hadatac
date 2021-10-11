package org.hadatac.console.providers;

import org.hadatac.console.providers.MyUsernamePasswordAuthProvider;

public class MyUsernamePasswordAuthUser extends MyUsernamePasswordAuthProvider {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final String name;
    private final transient String password;
    private final String email;

    public MyUsernamePasswordAuthUser(final MyUsernamePasswordAuthProvider signup) {
        this.password = signup.getPassword();
        this.email = signup.getEmail();
        this.name = signup.getName();
    }

    /**
     * Used for password reset only - do not use this to signup a user!
     * @param password
     */
    public MyUsernamePasswordAuthUser(final String password, String email, String name) {
        this.password = password;
        this.email = email;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}