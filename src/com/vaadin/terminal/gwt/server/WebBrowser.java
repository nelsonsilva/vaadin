/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.server;

import java.util.Locale;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import com.vaadin.terminal.Terminal;

@SuppressWarnings("serial")
public class WebBrowser implements Terminal {

    private int screenHeight = 0;
    private int screenWidth = 0;
    private String browserApplication = null;
    private Locale locale;
    private String address;
    private boolean secureConnection;

    /**
     * There is no default-theme for this terminal type.
     * 
     * @return Allways returns null.
     */
    public String getDefaultTheme() {
        return null;
    }

    /**
     * Get the height of the users display in pixels.
     * 
     */
    public int getScreenHeight() {
        return screenHeight;
    }

    /**
     * Get the width of the users display in pixels.
     * 
     */
    public int getScreenWidth() {
        return screenWidth;
    }

    /**
     * Get the browser user-agent string.
     * 
     * @return
     */
    public String getBrowserApplication() {
        return browserApplication;
    }

    void updateBrowserProperties(HttpServletRequest request) {
        locale = request.getLocale();
        address = request.getRemoteAddr();
        secureConnection = request.isSecure();

        final String agent = request.getHeader("user-agent");
        if (agent != null) {
            browserApplication = agent;
        }

        final String sw = request.getParameter("sw");
        if (sw != null) {
            final String sh = request.getParameter("sh");
            try {
                screenHeight = Integer.parseInt(sh);
                screenWidth = Integer.parseInt(sw);
            } catch (final NumberFormatException e) {
                screenHeight = screenWidth = 0;
            }
        }
    }

    // TODO: This method depends on the Portlet API.
    void updateBrowserProperties(PortletRequest request) {
        locale = request.getLocale();
        address = null;
        secureConnection = request.isSecure();

        final String agent = request.getProperty("user-agent");
        if (agent != null) {
            browserApplication = agent;
        }

        final String sw = request.getParameter("sw");
        if (sw != null) {
            final String sh = request.getParameter("sh");
            try {
                screenHeight = Integer.parseInt(sh);
                screenWidth = Integer.parseInt(sw);
            } catch (final NumberFormatException e) {
                screenHeight = screenWidth = 0;
            }
        }
    }

    /**
     * Get the IP-address of the web browser. If the application is running
     * inside a portlet, this method will return null.
     * 
     * @return IP-address in 1.12.123.123 -format
     */
    public String getAddress() {
        return address;
    }

    /** Get the default locate of the browser. */
    public Locale getLocale() {
        return locale;
    }

    /** Is the connection made using HTTPS? */
    public boolean isSecureConnection() {
        return secureConnection;
    }

}
