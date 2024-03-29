/*
@VaadinApache2LicenseForJavaFiles@
 */
package com.vaadin.terminal.gwt.server;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.MimeResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import javax.portlet.StateAwareResponse;
import javax.servlet.http.HttpSessionBindingListener;
import javax.xml.namespace.QName;

import com.vaadin.Application;
import com.vaadin.terminal.ApplicationResource;
import com.vaadin.ui.Root;

/**
 * TODO Write documentation, fix JavaDoc tags.
 * 
 * This is automatically registered as a {@link HttpSessionBindingListener} when
 * {@link PortletSession#setAttribute()} is called with the context as value.
 * 
 * @author peholmst
 */
@SuppressWarnings("serial")
public class PortletApplicationContext2 extends AbstractWebApplicationContext {

    private static final Logger logger = Logger
            .getLogger(PortletApplicationContext2.class.getName());

    protected Map<Application, Set<PortletListener>> portletListeners = new HashMap<Application, Set<PortletListener>>();

    protected transient PortletSession session;
    protected transient PortletConfig portletConfig;

    protected HashMap<String, Application> portletWindowIdToApplicationMap = new HashMap<String, Application>();

    private transient PortletResponse response;

    private final Map<String, QName> eventActionDestinationMap = new HashMap<String, QName>();
    private final Map<String, Serializable> eventActionValueMap = new HashMap<String, Serializable>();

    private final Map<String, String> sharedParameterActionNameMap = new HashMap<String, String>();
    private final Map<String, String> sharedParameterActionValueMap = new HashMap<String, String>();

    public File getBaseDirectory() {
        String resultPath = session.getPortletContext().getRealPath("/");
        if (resultPath != null) {
            return new File(resultPath);
        } else {
            try {
                final URL url = session.getPortletContext().getResource("/");
                return new File(url.getFile());
            } catch (final Exception e) {
                // FIXME: Handle exception
                logger.log(
                        Level.INFO,
                        "Cannot access base directory, possible security issue "
                                + "with Application Server or Servlet Container",
                        e);
            }
        }
        return null;
    }

    protected PortletCommunicationManager getApplicationManager(
            Application application) {
        PortletCommunicationManager mgr = (PortletCommunicationManager) applicationToAjaxAppMgrMap
                .get(application);

        if (mgr == null) {
            // Creates a new manager
            mgr = createPortletCommunicationManager(application);
            applicationToAjaxAppMgrMap.put(application, mgr);
        }
        return mgr;
    }

    protected PortletCommunicationManager createPortletCommunicationManager(
            Application application) {
        return new PortletCommunicationManager(application);
    }

    public static PortletApplicationContext2 getApplicationContext(
            PortletSession session) {
        Object cxattr = session.getAttribute(PortletApplicationContext2.class
                .getName());
        PortletApplicationContext2 cx = null;
        // can be false also e.g. if old context comes from another
        // classloader when using
        // <private-session-attributes>false</private-session-attributes>
        // and redeploying the portlet - see #7461
        if (cxattr instanceof PortletApplicationContext2) {
            cx = (PortletApplicationContext2) cxattr;
        }
        if (cx == null) {
            cx = new PortletApplicationContext2();
            session.setAttribute(PortletApplicationContext2.class.getName(), cx);
        }
        if (cx.session == null) {
            cx.session = session;
        }
        return cx;
    }

    @Override
    protected void removeApplication(Application application) {
        super.removeApplication(application);
        // values() is backed by map, removes the key-value pair from the map
        portletWindowIdToApplicationMap.values().remove(application);
    }

    protected void addApplication(Application application,
            String portletWindowId) {
        applications.add(application);
        portletWindowIdToApplicationMap.put(portletWindowId, application);
    }

    public Application getApplicationForWindowId(String portletWindowId) {
        return portletWindowIdToApplicationMap.get(portletWindowId);
    }

    public PortletSession getPortletSession() {
        return session;
    }

    public PortletConfig getPortletConfig() {
        return portletConfig;
    }

    public void setPortletConfig(PortletConfig config) {
        portletConfig = config;
    }

    public void addPortletListener(Application app, PortletListener listener) {
        Set<PortletListener> l = portletListeners.get(app);
        if (l == null) {
            l = new LinkedHashSet<PortletListener>();
            portletListeners.put(app, l);
        }
        l.add(listener);
    }

    public void removePortletListener(Application app, PortletListener listener) {
        Set<PortletListener> l = portletListeners.get(app);
        if (l != null) {
            l.remove(listener);
        }
    }

    public void firePortletRenderRequest(Application app, Root root,
            RenderRequest request, RenderResponse response) {
        Set<PortletListener> listeners = portletListeners.get(app);
        if (listeners != null) {
            for (PortletListener l : listeners) {
                l.handleRenderRequest(request, new RestrictedRenderResponse(
                        response), root);
            }
        }
    }

    public void firePortletActionRequest(Application app, Root root,
            ActionRequest request, ActionResponse response) {
        String key = request.getParameter(ActionRequest.ACTION_NAME);
        if (eventActionDestinationMap.containsKey(key)) {
            // this action request is only to send queued portlet events
            response.setEvent(eventActionDestinationMap.get(key),
                    eventActionValueMap.get(key));
            // cleanup
            eventActionDestinationMap.remove(key);
            eventActionValueMap.remove(key);
        } else if (sharedParameterActionNameMap.containsKey(key)) {
            // this action request is only to set shared render parameters
            response.setRenderParameter(sharedParameterActionNameMap.get(key),
                    sharedParameterActionValueMap.get(key));
            // cleanup
            sharedParameterActionNameMap.remove(key);
            sharedParameterActionValueMap.remove(key);
        } else {
            // normal action request, notify listeners
            Set<PortletListener> listeners = portletListeners.get(app);
            if (listeners != null) {
                for (PortletListener l : listeners) {
                    l.handleActionRequest(request, response, root);
                }
            }
        }
    }

    public void firePortletEventRequest(Application app, Root root,
            EventRequest request, EventResponse response) {
        Set<PortletListener> listeners = portletListeners.get(app);
        if (listeners != null) {
            for (PortletListener l : listeners) {
                l.handleEventRequest(request, response, root);
            }
        }
    }

    public void firePortletResourceRequest(Application app, Root root,
            ResourceRequest request, ResourceResponse response) {
        Set<PortletListener> listeners = portletListeners.get(app);
        if (listeners != null) {
            for (PortletListener l : listeners) {
                l.handleResourceRequest(request, response, root);
            }
        }
    }

    public interface PortletListener extends Serializable {

        public void handleRenderRequest(RenderRequest request,
                RenderResponse response, Root root);

        public void handleActionRequest(ActionRequest request,
                ActionResponse response, Root root);

        public void handleEventRequest(EventRequest request,
                EventResponse response, Root root);

        public void handleResourceRequest(ResourceRequest request,
                ResourceResponse response, Root root);
    }

    /**
     * This is for use by {@link AbstractApplicationPortlet} only.
     * 
     * TODO cleaner implementation, now "semi-static"!
     * 
     * @param mimeResponse
     */
    void setResponse(PortletResponse response) {
        this.response = response;
    }

    @Override
    public String generateApplicationResourceURL(ApplicationResource resource,
            String mapKey) {
        if (response instanceof MimeResponse) {
            ResourceURL resourceURL = ((MimeResponse) response)
                    .createResourceURL();
            final String filename = resource.getFilename();
            if (filename == null) {
                resourceURL.setResourceID("APP/" + mapKey + "/");
            } else {
                resourceURL.setResourceID("APP/" + mapKey + "/"
                        + urlEncode(filename));
            }
            return resourceURL.toString();
        } else {
            // in a background thread or otherwise outside a request
            // TODO exception ??
            return null;
        }
    }

    /**
     * Creates a new action URL.
     * 
     * @param action
     * @return action URL or null if called outside a MimeRequest (outside a
     *         UIDL request or similar)
     */
    public PortletURL generateActionURL(String action) {
        PortletURL url = null;
        if (response instanceof MimeResponse) {
            url = ((MimeResponse) response).createActionURL();
            url.setParameter("javax.portlet.action", action);
        } else {
            return null;
        }
        return url;
    }

    /**
     * Sends a portlet event to the indicated destination.
     * 
     * Internally, an action may be created and opened, as an event cannot be
     * sent directly from all types of requests.
     * 
     * The event destinations and values need to be kept in the context until
     * sent. Any memory leaks if the action fails are limited to the session.
     * 
     * Event names for events sent and received by a portlet need to be declared
     * in portlet.xml .
     * 
     * @param root
     *            a window in which a temporary action URL can be opened if
     *            necessary
     * @param name
     *            event name
     * @param value
     *            event value object that is Serializable and, if appropriate,
     *            has a valid JAXB annotation
     */
    public void sendPortletEvent(Root root, QName name, Serializable value)
            throws IllegalStateException {
        if (response instanceof MimeResponse) {
            String actionKey = "" + System.currentTimeMillis();
            while (eventActionDestinationMap.containsKey(actionKey)) {
                actionKey = actionKey + ".";
            }
            PortletURL actionUrl = generateActionURL(actionKey);
            if (actionUrl != null) {
                eventActionDestinationMap.put(actionKey, name);
                eventActionValueMap.put(actionKey, value);
                throw new RuntimeException(
                        "Root.open has not yet been implemented");
                // root.open(new ExternalResource(actionUrl.toString()));
            } else {
                // this should never happen as we already know the response is a
                // MimeResponse
                throw new IllegalStateException(
                        "Portlet events can only be sent from a portlet request");
            }
        } else if (response instanceof StateAwareResponse) {
            ((StateAwareResponse) response).setEvent(name, value);
        } else {
            throw new IllegalStateException(
                    "Portlet events can only be sent from a portlet request");
        }
    }

    /**
     * Sets a shared portlet parameter.
     * 
     * Internally, an action may be created and opened, as shared parameters
     * cannot be set directly from all types of requests.
     * 
     * The parameters and values need to be kept in the context until sent. Any
     * memory leaks if the action fails are limited to the session.
     * 
     * Shared parameters set or read by a portlet need to be declared in
     * portlet.xml .
     * 
     * @param root
     *            a window in which a temporary action URL can be opened if
     *            necessary
     * @param name
     *            parameter identifier
     * @param value
     *            parameter value
     */
    public void setSharedRenderParameter(Root root, String name, String value)
            throws IllegalStateException {
        if (response instanceof MimeResponse) {
            String actionKey = "" + System.currentTimeMillis();
            while (sharedParameterActionNameMap.containsKey(actionKey)) {
                actionKey = actionKey + ".";
            }
            PortletURL actionUrl = generateActionURL(actionKey);
            if (actionUrl != null) {
                sharedParameterActionNameMap.put(actionKey, name);
                sharedParameterActionValueMap.put(actionKey, value);
                throw new RuntimeException(
                        "Root.open has not yet been implemented");
                // root.open(new ExternalResource(actionUrl.toString()));
            } else {
                // this should never happen as we already know the response is a
                // MimeResponse
                throw new IllegalStateException(
                        "Shared parameters can only be set from a portlet request");
            }
        } else if (response instanceof StateAwareResponse) {
            ((StateAwareResponse) response).setRenderParameter(name, value);
        } else {
            throw new IllegalStateException(
                    "Shared parameters can only be set from a portlet request");
        }
    }

    /**
     * Sets the portlet mode. This may trigger a new render request.
     * 
     * Portlet modes used by a portlet need to be declared in portlet.xml .
     * 
     * @param root
     *            a window in which the render URL can be opened if necessary
     * @param portletMode
     *            the portlet mode to switch to
     * @throws PortletModeException
     *             if the portlet mode is not allowed for some reason
     *             (configuration, permissions etc.)
     */
    public void setPortletMode(Root root, PortletMode portletMode)
            throws IllegalStateException, PortletModeException {
        if (response instanceof MimeResponse) {
            PortletURL url = ((MimeResponse) response).createRenderURL();
            url.setPortletMode(portletMode);
            throw new RuntimeException("Root.open has not yet been implemented");
            // root.open(new ExternalResource(url.toString()));
        } else if (response instanceof StateAwareResponse) {
            ((StateAwareResponse) response).setPortletMode(portletMode);
        } else {
            throw new IllegalStateException(
                    "Portlet mode can only be changed from a portlet request");
        }
    }
}
