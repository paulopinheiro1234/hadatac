package org.hadatac;

public class Constants {
    public static final String FLASH_MESSAGE_KEY = "message";
    public static final String FLASH_ERROR_KEY = "error";
    public static final String DATA_OWNER_ROLE = "data_owner";
    public static final String DATA_MANAGER_ROLE = "data_manager";
    public static final String FILE_VIEWER_EDITOR_ROLE = "file_viewer_editor";
    public static final String EMAIL_TEMPLATE_FALLBACK_LANGUAGE = "en";
    public static final String SETTING_KEY_MAIL="mail";
    public static final String SETTING_KEY_VERIFICATION_LINK_SECURE = SETTING_KEY_MAIL
            + "." + "verificationLink.secure";
    public static final String SETTING_KEY_PASSWORD_RESET_LINK_SECURE = SETTING_KEY_MAIL
            + "." + "passwordResetLink.secure";
    public static final String SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET = "loginAfterPasswordReset";
}
