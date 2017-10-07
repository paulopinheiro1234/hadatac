package org.hadatac.console.http;

import play.Play;

public class ConfigUtils {
	public static String getKbPrefix() {
		return Play.application().configuration().getString("hadatac.community.ont_prefix") + "-kb:";
	}
}
