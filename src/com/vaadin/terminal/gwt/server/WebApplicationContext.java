/*
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.server;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext;

/**
 * Web application context for Vaadin applications.
 * 
 * This is automatically added as a {@link HttpSessionBindingListener} when
 * added to a {@link HttpSession}.
 *
 * @author IT Mill Ltd.
 * @version
 * @VERSION@
 * @since 3.1
 */
@SuppressWarnings("serial")
public class WebApplicationContext implements ApplicationContext,
        HttpSessionBindingListener, Serializable {

    protected List<TransactionListener> listeners = Collections
            .synchronizedList(new LinkedList<TransactionListener>());

    protected transient HttpSession session;

    protected final HashSet<Application> applications = new HashSet<Application>();

    protected WebBrowser browser = new WebBrowser();

    protected HashMap<Application, CommunicationManager> applicationToAjaxAppMgrMap = new HashMap<Application, CommunicationManager>();

    /**
     * Creates a new Web Application Context.
     *
     */
    WebApplicationContext() {

    }

    /**
     * Gets the application context base directory.
     *
     * @see com.vaadin.service.ApplicationContext#getBaseDirectory()
     */
    public File getBaseDirectory() {
        final String realPath = ApplicationServlet.getResourcePath(session
                .getServletContext(), "/");
        if (realPath == null) {
            return null;
        }
        return new File(realPath);
    }

    /**
     * Gets the http-session application is running in.
     *
     * @return HttpSession this application context resides in.
     */
    public HttpSession getHttpSession() {
        return session;
    }

    /**
     * Gets the applications in this context.
     *
     * @see com.vaadin.service.ApplicationContext#getApplications()
     */
    public Collection<Application> getApplications() {
        return Collections.unmodifiableCollection(applications);
    }

    /**
     * Gets the application context for an HttpSession.
     *
     * @param session
     *            the HTTP session.
     * @return the application context for HttpSession.
     */
    static public WebApplicationContext getApplicationContext(
            HttpSession session) {
        WebApplicationContext cx = (WebApplicationContext) session
                .getAttribute(WebApplicationContext.class.getName());
        if (cx == null) {
            cx = new WebApplicationContext();
            session.setAttribute(WebApplicationContext.class.getName(), cx);
        }
        if (cx.session == null) {
            cx.session = session;
        }
        return cx;
    }

    /**
     * Adds a transaction listener to this context. The transaction listener is
     * called before and after each each HTTP request related to this session
     * except when serving static resources.
     *
     *
     * @see com.vaadin.service.ApplicationContext#addTransactionListener(com.vaadin.service.ApplicationContext.TransactionListener)
     */
    public void addTransactionListener(TransactionListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a transaction listener from this context. The transaction
     * listener is called before and after each each HTTP request related to
     * this session except when serving static resources.
     *
     * @see com.vaadin.service.ApplicationContext#removeTransactionListener(com.vaadin.service.ApplicationContext.TransactionListener)
     */
    public void removeTransactionListener(TransactionListener listener) {
        listeners.remove(listener);

    }

    /**
     * Sends a notification that a transaction is starting.
     *
     * @param application
     *            The application associated with the transaction.
     * @param request
     *            the HTTP request that triggered the transaction.
     */
    protected void startTransaction(Application application,
            HttpServletRequest request) {
        synchronized (listeners) {
            for (TransactionListener listener : listeners) {
                listener.transactionStart(application, request);
            }
        }
    }

    /**
     * Sends a notification that a transaction has ended.
     *
     * @param application
     *            The application associated with the transaction.
     * @param request
     *            the HTTP request that triggered the transaction.
     */
    protected void endTransaction(Application application,
            HttpServletRequest request) {
        LinkedList<Exception> exceptions = null;

        synchronized (listeners) {
            for (TransactionListener listener : listeners) {
                try {
                    listener.transactionEnd(application, request);
                } catch (final RuntimeException t) {
                    if (exceptions == null) {
                        exceptions = new LinkedList<Exception>();
                    }
                    exceptions.add(t);
                }
            }
        }

        // If any runtime exceptions occurred, throw a combined exception
        if (exceptions != null) {
            final StringBuffer msg = new StringBuffer();
            for (final Iterator i = exceptions.iterator(); i.hasNext();) {
                final RuntimeException e = (RuntimeException) i.next();
                if (msg.length() == 0) {
                    msg.append("\n\n--------------------------\n\n");
                }
                msg.append(e.getMessage() + "\n");
                final StringWriter trace = new StringWriter();
                e.printStackTrace(new PrintWriter(trace, true));
                msg.append(trace.toString());
            }
            throw new RuntimeException(msg.toString());
        }
    }

    protected void removeApplication(Application application) {
        applications.remove(application);
        applicationToAjaxAppMgrMap.remove(application);
    }

    protected void addApplication(Application application) {
        applications.add(application);
    }

    /**
     * @see javax.servlet.http.HttpSessionBindingListener#valueBound(HttpSessionBindingEvent)
     */
    public void valueBound(HttpSessionBindingEvent arg0) {
        // We are not interested in bindings
    }

    /**
     * @see javax.servlet.http.HttpSessionBindingListener#valueUnbound(HttpSessionBindingEvent)
     */
    public void valueUnbound(HttpSessionBindingEvent event) {
        // If we are going to be unbound from the session, the session must be
        // closing
        try {
            while (!applications.isEmpty()) {
                final Application app = applications.iterator().next();
                app.close();
                removeApplication(app);
            }
        } catch (Exception e) {
            // This should never happen but is possible with rare
            // configurations (e.g. robustness tests). If you have one
            // thread doing HTTP socket write and another thread trying to
            // remove same application here. Possible if you got e.g. session
            // lifetime 1 min but socket write may take longer than 1 min.
            // FIXME: Handle exception
            System.err.println("Could not remove application, leaking memory.");
            e.printStackTrace();
        }
    }

    /**
     * Get the web browser associated with this application context.
     *
     * Because application context is related to the http session and server
     * maintains one session per browser-instance, each context has exactly one
     * web browser associated with it.
     *
     * @return
     */
    public WebBrowser getBrowser() {
        return browser;
    }

    /**
     * Gets communication manager for an application.
     *
     * If this application has not been running before, a new manager is
     * created.
     *
     * @param application
     * @return CommunicationManager
     */
    protected CommunicationManager getApplicationManager(
            Application application, AbstractApplicationServlet servlet) {
        CommunicationManager mgr = applicationToAjaxAppMgrMap.get(application);

        if (mgr == null) {
            // Creates new manager
            mgr = new CommunicationManager(application, servlet);
            applicationToAjaxAppMgrMap.put(application, mgr);
        }
        return mgr;
    }

}
