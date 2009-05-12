package com.vaadin.terminal.gwt.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.Application;

@SuppressWarnings("serial")
public class ApplicationRunnerServlet extends AbstractApplicationServlet {

    /**
     * The name of the application class currently used. Only valid within one
     * request.
     */
    String applicationClassName = "";

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
    }

    @Override
    protected void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        applicationClassName = getApplicationRunnerApplicationClassName(request);
        super.service(request, response);
        applicationClassName = "";

    }

    @Override
    URL getApplicationUrl(HttpServletRequest request)
            throws MalformedURLException {
        URL url = super.getApplicationUrl(request);

        String path = url.toString();
        path += applicationClassName;
        path += "/";

        return new URL(path);
    }

    @Override
    protected Application getNewApplication(HttpServletRequest request)
            throws ServletException {

        // Creates a new application instance
        try {
            Class<?> applicationClass = getClassLoader().loadClass(
                    applicationClassName);
            final Application application = (Application) applicationClass
                    .newInstance();

            return application;
        } catch (final IllegalAccessException e) {
            throw new ServletException(e);
        } catch (final InstantiationException e) {
            throw new ServletException(e);
        } catch (final ClassNotFoundException e) {
            throw new ServletException(
                    new InstantiationException(
                            "Failed to load application class: "
                                    + applicationClassName));
        }

    }

    private String getApplicationRunnerApplicationClassName(
            HttpServletRequest request) {
        return getApplicationRunnerURIs(request).applicationClassname;
    }

    private static class URIS {
        String widgetsetPath;
        String applicationURI;
        String context;
        String runner;
        String applicationClassname;

    }

    /**
     * Parses application runner URIs.
     * 
     * If request URL is e.g.
     * http://localhost:8080/itmill/run/com.vaadin.demo.Calc then
     * <ul>
     * <li>context=itmill</li>
     * <li>Runner servlet=run</li>
     * <li>Toolkit application=com.vaadin.demo.Calc</li>
     * </ul>
     * 
     * @param request
     * @return string array containing widgetset URI, application URI and
     *         context, runner, application classname
     */
    private static URIS getApplicationRunnerURIs(HttpServletRequest request) {
        final String[] urlParts = request.getRequestURI().toString().split(
                "\\/");
        String context = null;
        String runner = null;
        URIS uris = new URIS();
        String applicationClassname = null;
        String contextPath = request.getContextPath();
        if (urlParts[1].equals(contextPath.replaceAll("\\/", ""))) {
            // class name comes after web context and runner application
            context = urlParts[1];
            runner = urlParts[2];
            if (urlParts.length == 3) {
                throw new IllegalArgumentException("No application specified");
            }
            applicationClassname = urlParts[3];

            uris.widgetsetPath = "/" + context;
            uris.applicationURI = "/" + context + "/" + runner + "/"
                    + applicationClassname;
            uris.context = context;
            uris.runner = runner;
            uris.applicationClassname = applicationClassname;
        } else {
            // no context
            context = "";
            runner = urlParts[1];
            if (urlParts.length == 2) {
                throw new IllegalArgumentException("No application specified");
            }
            applicationClassname = urlParts[2];

            uris.widgetsetPath = "/";
            uris.applicationURI = "/" + runner + "/" + applicationClassname;
            uris.context = context;
            uris.runner = runner;
            uris.applicationClassname = applicationClassname;
        }
        return uris;
    }

    // @Override
    @Override
    protected Class getApplicationClass() throws ClassNotFoundException {
        // TODO use getClassLoader() ?
        return getClass().getClassLoader().loadClass(applicationClassName);
    }

    @Override
    String getRequestPathInfo(HttpServletRequest request) {
        String path = request.getPathInfo();
        if (path == null) {
            return null;
        }

        path = path.substring(1 + applicationClassName.length());
        return path;
    }

    @Override
    String getWidgetsetLocation(HttpServletRequest request) {
        URIS uris = getApplicationRunnerURIs(request);
        String widgetsetPath = uris.widgetsetPath;
        if (widgetsetPath.equals("/")) {
            widgetsetPath = "";
        }

        return widgetsetPath;
    }

}