/* 
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;

public class VProgressIndicator extends Widget implements Paintable {

    private static final String CLASSNAME = "v-progressindicator";
    Element wrapper = DOM.createDiv();
    Element indicator = DOM.createDiv();
    private ApplicationConnection client;
    private final Poller poller;
    private boolean indeterminate = false;
    private boolean pollerSuspendedDueDetach;
    private int interval;

    public VProgressIndicator() {
        setElement(DOM.createDiv());
        getElement().appendChild(wrapper);
        setStyleName(CLASSNAME);
        wrapper.appendChild(indicator);
        indicator.setClassName(CLASSNAME + "-indicator");
        wrapper.setClassName(CLASSNAME + "-wrapper");
        poller = new Poller();
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        this.client = client;
        if (!uidl.getBooleanAttribute("cached")) {
            poller.cancel();
        }
        if (client.updateComponent(this, uidl, true)) {
            return;
        }

        indeterminate = uidl.getBooleanAttribute("indeterminate");

        if (indeterminate) {
            String basename = CLASSNAME + "-indeterminate";
            VProgressIndicator.setStyleName(getElement(), basename, true);
            VProgressIndicator.setStyleName(getElement(), basename
                    + "-disabled", uidl.getBooleanAttribute("disabled"));
        } else {
            try {
                final float f = Float.parseFloat(uidl
                        .getStringAttribute("state"));
                final int size = Math.round(100 * f);
                DOM.setStyleAttribute(indicator, "width", size + "%");
            } catch (final Exception e) {
            }
        }

        if (!uidl.getBooleanAttribute("disabled")) {
            interval = uidl.getIntAttribute("pollinginterval");
            poller.scheduleRepeating(interval);
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        if (pollerSuspendedDueDetach) {
            poller.scheduleRepeating(interval);
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        if (interval > 0) {
            poller.cancel();
            pollerSuspendedDueDetach = true;
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            poller.cancel();
        }
    }

    class Poller extends Timer {

        @Override
        public void run() {
            if (!client.hasActiveRequest()
                    && Util.isAttachedAndDisplayed(VProgressIndicator.this)) {
                client.sendPendingVariableChanges();
            }
        }

    }

}
