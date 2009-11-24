package com.vaadin.terminal.gwt.server;

/**
 * TODO Document me!
 * 
 * @author peholmst
 * 
 */
public interface Constants {

    static final String NOT_PRODUCTION_MODE_INFO = ""
            + "=================================================================\n"
            + "Vaadin is running in DEBUG MODE.\nAdd productionMode=true to web.xml "
            + "to disable debug features.\nTo show debug window, add ?debug to "
            + "your application URL.\n"
            + "=================================================================";

    static final String WARNING_XSRF_PROTECTION_DISABLED = ""
            + "===========================================================\n"
            + "WARNING: Cross-site request forgery protection is disabled!\n"
            + "===========================================================";
    
    static final String URL_PARAMETER_RESTART_APPLICATION = "restartApplication";
    static final String URL_PARAMETER_CLOSE_APPLICATION = "closeApplication";
    static final String URL_PARAMETER_REPAINT_ALL = "repaintAll";
    static final String URL_PARAMETER_THEME = "theme";

    static final String SERVLET_PARAMETER_DEBUG = "Debug";
    static final String SERVLET_PARAMETER_PRODUCTION_MODE = "productionMode";
    static final String SERVLET_PARAMETER_DISABLE_XSRF_PROTECTION = "disable-xsrf-protection";

    // Configurable parameter names
    static final String PARAMETER_VAADIN_RESOURCES = "Resources";

    static final int DEFAULT_BUFFER_SIZE = 32 * 1024;

    static final int MAX_BUFFER_SIZE = 64 * 1024;

    static final String AJAX_UIDL_URI = "/UIDL";

    final String THEME_DIRECTORY_PATH = "VAADIN/themes/";

    static final int DEFAULT_THEME_CACHETIME = 1000 * 60 * 60 * 24;

    static final String WIDGETSET_DIRECTORY_PATH = "VAADIN/widgetsets/";

    // Name of the default widget set, used if not specified in web.xml
    static final String DEFAULT_WIDGETSET = "com.vaadin.terminal.gwt.DefaultWidgetSet";

    // Widget set parameter name
    static final String PARAMETER_WIDGETSET = "widgetset";

    static final String ERROR_NO_WINDOW_FOUND = "No window found. Did you remember to setMainWindow()?";

    static final String DEFAULT_THEME_NAME = "reindeer";

    static final String INVALID_SECURITY_KEY_MSG = "Invalid security key.";

}
