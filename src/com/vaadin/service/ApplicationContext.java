/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.service;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;

import com.vaadin.Application;

/**
 * <code>ApplicationContext</code> provides information about the running
 * context of the application. Each context is shared by all applications that
 * are open for one user. In web-environment this corresponds to HttpSession.
 * 
 * @author IT Mill Ltd.
 * @version
 * @VERSION@
 * @since 3.1
 */
public interface ApplicationContext extends Serializable {

    /**
     * Returns application context base directory.
     * 
     * Typically an application is deployed in a such way that is has
     * application directory. For web applications this directory is the root
     * directory of the web applications. In some cases application might not
     * have application directory (for example web applications running inside
     * of war).
     * 
     * @return The application base directory
     */
    public File getBaseDirectory();

    /**
     * Gets the applications in this context.
     * 
     * Gets all applications in this context. Each application context contains
     * all applications that are open for one user.
     * 
     * @return Collection containing all applications in this context
     */
    public Collection getApplications();

    /**
     * Adds transaction listener to this context.
     * 
     * @param listener
     *            the listener to be added.
     * @see TransactionListener
     */
    public void addTransactionListener(TransactionListener listener);

    /**
     * Removes transaction listener from this context.
     * 
     * @param listener
     *            the listener to be removed.
     * @see TransactionListener
     */
    public void removeTransactionListener(TransactionListener listener);

    /**
     * Interface for listening the application transaction events.
     * Implementations of this interface can be used to listen all transactions
     * between the client and the application.
     * 
     */
    public interface TransactionListener extends Serializable {

        /**
         * Invoked at the beginning of every transaction.
         * 
         * @param application
         *            the Application object.
         * @param transactionData
         *            the Data identifying the transaction.
         */
        public void transactionStart(Application application,
                Object transactionData);

        /**
         * Invoked at the end of every transaction.
         * 
         * @param applcation
         *            the Application object.
         * @param transactionData
         *            the Data identifying the transaction.
         */
        public void transactionEnd(Application application,
                Object transactionData);

    }
}