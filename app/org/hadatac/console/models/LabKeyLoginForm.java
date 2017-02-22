package org.hadatac.console.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;

import org.jasypt.util.text.BasicTextEncryptor;

import play.libs.F.Option;
import play.mvc.QueryStringBindable;

public class LabKeyLoginForm implements QueryStringBindable<LabKeyLoginForm> {
    public String user_name = "";
    public String password = "";
    private BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    
    public LabKeyLoginForm(){
    	Properties prop = new Properties();
		try {
			InputStream is = getClass().getClassLoader().getResourceAsStream("labkey.config");
			prop.load(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	textEncryptor.setPassword(prop.getProperty("encryption_key"));
    }
    
    public String getUserName() {
    	return user_name;
    }
    
    public String getPassword() {
    	return password;
    }

	@Override
	public Option<LabKeyLoginForm> bind(String key, Map<String, String[]> data) {
		if (data.containsKey(key + ".user") && data.containsKey(key + ".password")) {
        	String user_name_temp = data.get(key + ".user")[0];
        	String password_temp = data.get(key + ".password")[0];
			if(!user_name_temp.isEmpty()){
				user_name = textEncryptor.decrypt(user_name_temp);
				password = textEncryptor.decrypt(password_temp);
			}
            return Option.<LabKeyLoginForm>Some(this);
        } else {
        	return Option.<LabKeyLoginForm>None();
        }
	}

	@Override
	public String javascriptUnbind() {
		return null;
	}

	@Override
	public String unbind(String key) {
		String user_name_encry = "";
		String password_encry = "";
		if(!user_name.isEmpty()){
			user_name_encry = textEncryptor.encrypt(user_name);
			try {
				user_name_encry = URLEncoder.encode(user_name_encry, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			password_encry = textEncryptor.encrypt(password);
			try {
				password_encry = URLEncoder.encode(password_encry, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return key + ".user=" + user_name_encry + "&" + key + ".password=" + password_encry;
	}
}
