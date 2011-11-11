/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.vaadin.Application;
import com.vaadin.ui.RootLayout;

/**
 * This servlet connects a Vaadin Application to Web.
 * 
 * @author IT Mill Ltd.
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

        // Gets the application class name
        final String applicationClassName = servletConfig
                .getInitParameter("application");
        if (applicationClassName == null) {
            String rootLayoutParam = servletConfig
                    .getInitParameter("rootLayout");

            // Validate the parameter value
            verifyRootLayoutClass(rootLayoutParam);

            // Application can be used if a valid rootLayout is defined
            applicationClass = Application.class;
            return;
        }

        try {
            applicationClass = (Class<? extends Application>) getClassLoader()
                    .loadClass(applicationClassName);
        } catch (final ClassNotFoundException e) {
            throw new ServletException("Failed to load application class: "
                    + applicationClassName);
        }
    }

    private void verifyRootLayoutClass(String className)
            throws ServletException {
        if (className == null) {
            throw new ServletException(
                    "rootLayout servlet parameter not defined");
        }

        // Check that the root layout class can be found
        try {
            Class<?> rootLayoutClass = getClassLoader().loadClass(className);
            if (!RootLayout.class.isAssignableFrom(rootLayoutClass)) {
                throw new ServletException(className
                        + " does not implement RootLayout");
            }
            // Try finding a default constructor, else throw exception
            rootLayoutClass.getConstructor();
        } catch (ClassNotFoundException e) {
            throw new ServletException("rootLayout class could not be loaded",
                    e);
        } catch (SecurityException e) {
            throw new ServletException("Could not access rootLayout class", e);
        } catch (NoSuchMethodException e) {
            throw new ServletException(className
                    + " doesn't have a public no-args constructor");
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