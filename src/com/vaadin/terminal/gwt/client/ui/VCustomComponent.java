/* 
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;

public class VCustomComponent extends SimplePanel implements Container {

    private static final String CLASSNAME = "v-customcomponent";
    private String height;
    private ApplicationConnection client;
    private boolean rendering;
    private String width;
    private RenderSpace renderSpace = new RenderSpace();

    public VCustomComponent() {
        super();
        setStyleName(CLASSNAME);
    }

    public void updateFromUIDL(UIDL uidl, final ApplicationConnection client) {
        rendering = true;
        if (client.updateComponent(this, uidl, true)) {
            rendering = false;
            return;
        }
        this.client = client;

        final UIDL child = uidl.getChildUIDL(0);
        if (child != null) {
            final Paintable p = client.getPaintable(child);
            if (p != getWidget()) {
                if (getWidget() != null) {
                    client.unregisterPaintable((Paintable) getWidget());
                    clear();
                }
                setWidget((Widget) p);
            }
            p.updateFromUIDL(child, client);
        }

        boolean updateDynamicSize = updateDynamicSize();
        if (updateDynamicSize) {
            Scheduler.get().scheduleDeferred(new Command() {
                public void execute() {
                    // FIXME deferred relative size update needed to fix some
                    // scrollbar issues in sampler. This must be the wrong way
                    // to do it. Might be that some other component is broken.
                    client.handleComponentRelativeSize(VCustomComponent.this);

                }
            });
        }

        renderSpace.setWidth(getElement().getOffsetWidth());
        renderSpace.setHeight(getElement().getOffsetHeight());

        /*
         * Needed to update client size if the size of this component has
         * changed and the child uses relative size(s).
         */
        client.runDescendentsLayout(this);

        rendering = false;
    }

    private boolean updateDynamicSize() {
        boolean updated = false;
        if (isDynamicWidth()) {
            int childWidth = Util.getRequiredWidth(getWidget());
            getElement().getStyle().setPropertyPx("width", childWidth);
            updated = true;
        }
        if (isDynamicHeight()) {
            int childHeight = Util.getRequiredHeight(getWidget());
            getElement().getStyle().setPropertyPx("height", childHeight);
            updated = true;
        }

        return updated;
    }

    protected boolean isDynamicWidth() {
        return width == null || width.equals("");
    }

    protected boolean isDynamicHeight() {
        return height == null || height.equals("");
    }

    public boolean hasChildComponent(Widget component) {
        if (getWidget() == component) {
            return true;
        } else {
            return false;
        }
    }

    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        if (hasChildComponent(oldComponent)) {
            clear();
            setWidget(newComponent);
        } else {
            throw new IllegalStateException();
        }
    }

    public void updateCaption(Paintable component, UIDL uidl) {
        // NOP, custom component dont render composition roots caption
    }

    public boolean requestLayout(Set<Paintable> child) {
        // If a child grows in size, it will not necessarily be calculated
        // correctly unless we remove previous size definitions
        if (isDynamicWidth()) {
            getElement().getStyle().setProperty("width", "");
        }
        if (isDynamicHeight()) {
            getElement().getStyle().setProperty("height", "");
        }

        return !updateDynamicSize();
    }

    public RenderSpace getAllocatedSpace(Widget child) {
        return renderSpace;
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        renderSpace.setHeight(getElement().getOffsetHeight());

        if (!height.equals(this.height)) {
            this.height = height;
            if (!rendering) {
                client.handleComponentRelativeSize(getWidget());
            }
        }
    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);
        renderSpace.setWidth(getElement().getOffsetWidth());

        if (!width.equals(this.width)) {
            this.width = width;
            if (!rendering) {
                client.handleComponentRelativeSize(getWidget());
            }
        }
    }

}
