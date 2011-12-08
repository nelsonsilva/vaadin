/* 
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import com.google.gwt.user.client.DOM;

/**
 * This class represents a password field.
 * 
 * @author Vaadin Ltd.
 * 
 */
public class VPasswordField extends VTextField {

    public VPasswordField() {
        super(DOM.createInputPassword());
    }

}
