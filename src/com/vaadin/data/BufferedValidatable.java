/* 
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.data;

import java.io.Serializable;

/**
 * <p>
 * This interface defines the combination of <code>Validatable</code> and
 * <code>Buffered</code> interfaces. The combination of the interfaces defines
 * if the invalid data is committed to datasource.
 * </p>
 * 
 * @author Vaadin Ltd.
 * @version
 * @VERSION@
 * @since 3.0
 */
public interface BufferedValidatable extends Buffered, Validatable,
        Serializable {

    /**
     * Tests if the invalid data is committed to datasource. The default is
     * <code>false</code>.
     */
    public boolean isInvalidCommitted();

    /**
     * Sets if the invalid data should be committed to datasource. The default
     * is <code>false</code>.
     */
    public void setInvalidCommitted(boolean isCommitted);
}
