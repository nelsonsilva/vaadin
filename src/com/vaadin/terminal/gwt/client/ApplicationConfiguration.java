package com.vaadin.terminal.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.vaadin.terminal.gwt.client.ui.VUnknownComponent;

public class ApplicationConfiguration {

    // can only be inited once, to avoid multiple-entrypoint-problem
    private static WidgetSet initedWidgetSet;

    private String id;
    private String themeUri;
    private String pathInfo;
    private String appUri;
    private JavaScriptObject versionInfo;
    private String windowName;
    private String communicationErrorCaption;
    private String communicationErrorMessage;
    private String communicationErrorUrl;
    private boolean useDebugIdInDom = true;
    private boolean usePortletURLs = false;
    private String portletUidlURLBase;

    private HashMap<String, String> unknownComponents;

    private Class<? extends Paintable>[] classes = new Class[1024];

    private String windowId;

    private static ArrayList<ApplicationConnection> unstartedApplications = new ArrayList<ApplicationConnection>();
    private static ArrayList<ApplicationConnection> runningApplications = new ArrayList<ApplicationConnection>();

    public boolean usePortletURLs() {
        return usePortletURLs;
    }

    public String getPortletUidlURLBase() {
        return portletUidlURLBase;
    }

    public String getRootPanelId() {
        return id;
    }

    /**
     * Gets the application base URI. Using this other than as the download
     * action URI can cause problems in Portlet 2.0 deployments.
     * 
     * @return application base URI
     */
    public String getApplicationUri() {
        return appUri;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getThemeUri() {
        return themeUri;
    }

    public void setAppId(String appId) {
        id = appId;
    }

    public void setInitialWindowName(String name) {
        windowName = name;
    }

    public String getInitialWindowName() {
        return windowName;
    }

    public JavaScriptObject getVersionInfoJSObject() {
        return versionInfo;
    }

    public String getCommunicationErrorCaption() {
        return communicationErrorCaption;
    }

    public String getCommunicationErrorMessage() {
        return communicationErrorMessage;
    }

    public String getCommunicationErrorUrl() {
        return communicationErrorUrl;
    }

    private native void loadFromDOM()
    /*-{

        var id = this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::id;
        if($wnd.vaadin.vaadinConfigurations && $wnd.vaadin.vaadinConfigurations[id]) {
            var jsobj = $wnd.vaadin.vaadinConfigurations[id];
            var uri = jsobj.appUri;
            if(uri != null && uri[uri.length -1] != "/") {
                uri = uri + "/";
            }
            this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::appUri = uri;
            this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::pathInfo = jsobj.pathInfo;
            this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::themeUri = jsobj.themeUri;
            if(jsobj.windowName) {
                this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::windowName = jsobj.windowName;
            }
            if('useDebugIdInDom' in jsobj && typeof(jsobj.useDebugIdInDom) == "boolean") {
                this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::useDebugIdInDom = jsobj.useDebugIdInDom;
            }
            if(jsobj.versionInfo) {
                this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::versionInfo = jsobj.versionInfo;
            }
            if(jsobj.comErrMsg) {
                this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::communicationErrorCaption = jsobj.comErrMsg.caption;
                this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::communicationErrorMessage = jsobj.comErrMsg.message;
                this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::communicationErrorUrl = jsobj.comErrMsg.url;
            }
            if (jsobj.usePortletURLs) {
                this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::usePortletURLs = jsobj.usePortletURLs;
            }
            if (jsobj.portletUidlURLBase) {
                this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::portletUidlURLBase = jsobj.portletUidlURLBase;
            }
        } else {
            $wnd.alert("Vaadin app failed to initialize: " + this.id);
        }

     }-*/;

    /**
     * Inits the ApplicationConfiguration by reading the DOM and instantiating
     * ApplicationConnections accordingly. Call {@link #startNextApplication()}
     * to actually start the applications.
     * 
     * @param widgetset
     *            the widgetset that is running the apps
     */
    public static void initConfigurations(WidgetSet widgetset) {
        String wsname = widgetset.getClass().getName();
        String module = GWT.getModuleName();
        int lastdot = module.lastIndexOf(".");
        String base = module.substring(0, lastdot);
        String simpleName = module.substring(lastdot + 1);
        // if (!wsname.startsWith(base) || !wsname.endsWith(simpleName)) {
        // // WidgetSet module name does not match implementation name;
        // // probably inherited WidgetSet with entry-point. Skip.
        // GWT.log("Ignored init for " + wsname + " when starting " + module,
        // null);
        // return;
        // }

        if (initedWidgetSet != null) {
            // Something went wrong: multiple widgetsets inited
            String msg = "Tried to init " + widgetset.getClass().getName()
                    + ", but " + initedWidgetSet.getClass().getName()
                    + " was already inited.";
            ApplicationConnection.getConsole().log(msg);
        }
        initedWidgetSet = widgetset;
        ArrayList<String> appIds = new ArrayList<String>();
        loadAppIdListFromDOM(appIds);

        for (Iterator<String> it = appIds.iterator(); it.hasNext();) {
            String appId = it.next();
            ApplicationConfiguration appConf = getConfigFromDOM(appId);
            ApplicationConnection a = new ApplicationConnection(widgetset,
                    appConf);
            unstartedApplications.add(a);
        }
    }

    /**
     * Starts the next unstarted application. The WidgetSet should call this
     * once to start the first application; after that, each application should
     * call this once it has started. This ensures that the applications are
     * started synchronously, which is neccessary to avoid session-id problems.
     * 
     * @return true if an unstarted application was found
     */
    public static boolean startNextApplication() {
        if (unstartedApplications.size() > 0) {
            ApplicationConnection a = unstartedApplications.remove(0);
            a.start();
            runningApplications.add(a);
            return true;
        } else {
            return false;
        }
    }

    public static List<ApplicationConnection> getRunningApplications() {
        return runningApplications;
    }

    private native static void loadAppIdListFromDOM(ArrayList<String> list)
    /*-{
         var j;
         for(j in $wnd.vaadin.vaadinConfigurations) {
             list.@java.util.Collection::add(Ljava/lang/Object;)(j);
         }
     }-*/;

    public static ApplicationConfiguration getConfigFromDOM(String appId) {
        ApplicationConfiguration conf = new ApplicationConfiguration();
        conf.setAppId(appId);
        conf.loadFromDOM();
        return conf;
    }

    public native String getServletVersion()
    /*-{
        return this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::versionInfo.vaadinVersion;
    }-*/;

    public native String getApplicationVersion()
    /*-{
        return this.@com.vaadin.terminal.gwt.client.ApplicationConfiguration::versionInfo.applicationVersion;
    }-*/;

    public boolean useDebugIdInDOM() {
        return useDebugIdInDom;
    }

    public Class<? extends Paintable> getWidgetClassByEncodedTag(String tag) {
        try {
            int parseInt = Integer.parseInt(tag);
            return classes[parseInt];
        } catch (Exception e) {
            // component was not present in mappings
            return VUnknownComponent.class;
        }
    }

    public void addComponentMappings(ValueMap valueMap, WidgetSet widgetSet) {
        JsArrayString keyArray = valueMap.getKeyArray();
        for (int i = 0; i < keyArray.length(); i++) {
            String key = keyArray.get(i).intern();
            int value = valueMap.getInt(key);
            classes[value] = widgetSet.getImplementationByClassName(key);
            if (classes[value] == VUnknownComponent.class) {
                if (unknownComponents == null) {
                    unknownComponents = new HashMap<String, String>();
                }
                unknownComponents.put("" + value, key);
            } else if (key == "com.vaadin.ui.Window") {
                windowId = "" + value;
            }
        }
    }

    /**
     * @return the integer value that is used to code top level windows
     *         "com.vaadin.ui.Window"
     */
    String getEncodedWindowTag() {
        return windowId;
    }

    String getUnknownServerClassNameByEncodedTagName(String tag) {
        if (unknownComponents != null) {
            return unknownComponents.get(tag);
        }
        return null;
    }
}
