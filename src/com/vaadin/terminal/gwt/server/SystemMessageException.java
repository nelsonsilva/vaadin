/*
@ITMillApache2LicenseForJavaFiles@
 */
package com.vaadin.terminal.gwt.server;

@SuppressWarnings("serial")
public class SystemMessageException extends RuntimeException {

    /**
     * Cause of the method exception
     */
    private Throwable cause;

    /**
     * Constructs a new <code>SystemMessageException</code> with the specified
     * detail message.
     * 
     * @param msg
     *            the detail message.
     */
    public SystemMessageException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new <code>SystemMessageException</code> with the specified
     * detail message and cause.
     * 
     * @param msg
     *            the detail message.
     * @param cause
     *            the cause of the exception.
     */
    public SystemMessageException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs a new <code>SystemMessageException</code> from another
     * exception.
     * 
     * @param cause
     *            the cause of the exception.
     */
    public SystemMessageException(Throwable cause) {
        this.cause = cause;
    }

    /**
     * @see java.lang.Throwable#getCause()
     */
    @Override
    public Throwable getCause() {
        return cause;
    }

}