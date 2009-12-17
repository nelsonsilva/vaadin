package com.vaadin.tests.server.applicationservlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

import com.vaadin.terminal.gwt.server.AbstractApplicationServlet;
import com.vaadin.terminal.gwt.server.ApplicationServlet;

public class TestStaticFilesLocation extends TestCase {

    ApplicationServlet servlet;

    private Method getStaticFilesLocationMethod;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        servlet = new ApplicationServlet();

        // Workaround to avoid calling init and creating servlet config
        Field f = AbstractApplicationServlet.class
                .getDeclaredField("applicationProperties");
        f.setAccessible(true);
        f.set(servlet, new Properties());

        getStaticFilesLocationMethod = AbstractApplicationServlet.class
                .getDeclaredMethod(
                        "getStaticFilesLocation",
                        new Class[] { javax.servlet.http.HttpServletRequest.class });
        getStaticFilesLocationMethod.setAccessible(true);

    }

    public class DummyServletConfig implements ServletConfig {

        // public DummyServletConfig(Map<String,String> initParameters, )
        public String getInitParameter(String name) {
            // TODO Auto-generated method stub
            return null;
        }

        public Enumeration getInitParameterNames() {
            return new Enumeration<Object>() {

                public boolean hasMoreElements() {
                    return false;
                }

                public Object nextElement() {
                    return null;
                }
            };
        }

        public ServletContext getServletContext() {
            // TODO Auto-generated method stub
            return null;
        }

        public String getServletName() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public void testWidgetSetLocation() throws Exception {
        String location;

        /* SERVLETS */
        // http://dummy.host:8080/contextpath/servlet
        // should return /contextpath
        location = testLocation("http://dummy.host:8080", "/contextpath",
                "/servlet", "");
        assertEquals("/contextpath", location);

        // http://dummy.host:8080/servlet
        // should return ""
        location = testLocation("http://dummy.host:8080", "", "/servlet", "");
        assertEquals("", location);

        // http://dummy.host/contextpath/servlet/extra/stuff
        // should return /contextpath
        location = testLocation("http://dummy.host", "/contextpath",
                "/servlet", "/extra/stuff");
        assertEquals("/contextpath", location);

        // http://dummy.host/context/path/servlet/extra/stuff
        // should return /context/path
        location = testLocation("http://dummy.host", "/context/path",
                "/servlet", "/extra/stuff");
        assertEquals("/context/path", location);

        /* Include requests */
        location = testIncludedLocation("http://my.portlet.server", "/user",
                "/tmpservletlocation1", "");
        assertEquals("Wrong widgetset location", "/user", location);

    }

    private String testLocation(String base, String contextPath,
            String servletPath, String pathInfo) throws Exception {

        HttpServletRequest request = createNonIncludeRequest(base, contextPath,
                servletPath, pathInfo);
        // Set request into replay mode
        replay(request);

        String location = (String) getStaticFilesLocationMethod.invoke(servlet,
                request);
        return location;
    }

    private String testIncludedLocation(String base, String portletContextPath,
            String servletPath, String pathInfo) throws Exception {

        HttpServletRequest request = createIncludeRequest(base,
                portletContextPath, servletPath, pathInfo);
        // Set request into replay mode
        replay(request);

        String location = (String) getStaticFilesLocationMethod.invoke(servlet,
                request);
        return location;
    }

    private HttpServletRequest createIncludeRequest(String base,
            String realContextPath, String realServletPath, String pathInfo)
            throws Exception {
        HttpServletRequest request = createRequest(base, "", "", pathInfo);
        expect(request.getAttribute("javax.servlet.include.context_path"))
                .andReturn(realContextPath).anyTimes();
        expect(request.getAttribute("javax.servlet.include.servlet_path"))
                .andReturn(realServletPath).anyTimes();
        expect(
                request
                        .getAttribute(AbstractApplicationServlet.REQUEST_VAADIN_STATIC_FILE_PATH))
                .andReturn(null).anyTimes();

        return request;
    }

    private HttpServletRequest createNonIncludeRequest(String base,
            String realContextPath, String realServletPath, String pathInfo)
            throws Exception {
        HttpServletRequest request = createRequest(base, realContextPath,
                realServletPath, pathInfo);
        expect(request.getAttribute("javax.servlet.include.context_path"))
                .andReturn(null).anyTimes();
        expect(request.getAttribute("javax.servlet.include.servlet_path"))
                .andReturn(null).anyTimes();
        expect(
                request
                        .getAttribute(ApplicationServlet.REQUEST_VAADIN_STATIC_FILE_PATH))
                .andReturn(null).anyTimes();

        return request;
    }

    /**
     * Creates a HttpServletRequest mock using the supplied parameters.
     * 
     * @param base
     *            The base url, e.g. http://localhost:8080
     * @param contextPath
     *            The context path where the application is deployed, e.g.
     *            /mycontext
     * @param servletPath
     *            The servlet path to the servlet we are testing, e.g. /myapp
     * @param pathInfo
     *            Any text following the servlet path in the request, not
     *            including query parameters, e.g. /UIDL/
     * @return A mock HttpServletRequest object useful for testing
     * @throws MalformedURLException
     */
    private HttpServletRequest createRequest(String base, String contextPath,
            String servletPath, String pathInfo) throws MalformedURLException {
        URL url = new URL(base + contextPath + pathInfo);
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.isSecure()).andReturn(
                url.getProtocol().equalsIgnoreCase("https")).anyTimes();
        expect(request.getServerName()).andReturn(url.getHost()).anyTimes();
        expect(request.getServerPort()).andReturn(url.getPort()).anyTimes();
        expect(request.getRequestURI()).andReturn(url.getPath()).anyTimes();
        expect(request.getContextPath()).andReturn(contextPath).anyTimes();
        expect(request.getPathInfo()).andReturn(pathInfo).anyTimes();
        expect(request.getServletPath()).andReturn(servletPath).anyTimes();

        return request;
    }

}
