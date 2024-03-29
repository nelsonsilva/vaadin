/* 
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ServletPortletHelper.ApplicationClassException;

/**
 * This servlet connects a Vaadin Application to Web.
 * 
 * @author Vaadin Ltd.
 * @version
 * @VERSION@
 * @since 5.0
 */

@SuppressWarnings("serial")
public class ApplicationServlet extends AbstractApplicationServlet {

    // Private fields
    private Class<? extends Application> applicationClass;

    /**
     * Called by the servlet container to indicate to a servlet that the servlet
     * is being placed into service.
     * 
     * @param servletConfig
     *            the object containing the servlet's configuration and
     *            initialization parameters
     * @throws javax.servlet.ServletException
     *             if an exception has occurred that interferes with the
     *             servlet's normal operation.
     */
    @Override
    public void init(javax.servlet.ServletConfig servletConfig)
            throws javax.servlet.ServletException {
        super.init(servletConfig);

        // Loads the application class using the same class loader
        // as the servlet itself

        try {
            applicationClass = ServletPortletHelper.getApplicationClass(
                    servletConfig.getInitParameter("application"),
                    servletConfig.getInitParameter(Application.ROOT_PARAMETER),
                    getClassLoader());
        } catch (ApplicationClassException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected Application getNewApplication(HttpServletRequest request)
            throws ServletException {

        // Creates a new application instance
        try {
            final Application application = getApplicationClass().newInstance();

            return application;
        } catch (final IllegalAccessException e) {
            throw new ServletException("getNewApplication failed", e);
        } catch (final InstantiationException e) {
            throw new ServletException("getNewApplication failed", e);
        } catch (ClassNotFoundException e) {
            throw new ServletException("getNewApplication failed", e);
        }
    }

    @Override
    protected Class<? extends Application> getApplicationClass()
            throws ClassNotFoundException {
        return applicationClass;
    }
}
