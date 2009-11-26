package com.vaadin.terminal.gwt.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.terminal.gwt.client.Paintable;

public abstract class ClickEventHandler implements ClickHandler,
        DoubleClickHandler, ContextMenuHandler, MouseUpHandler {

    private HandlerRegistration clickHandlerRegistration;
    private HandlerRegistration doubleClickHandlerRegistration;
    private HandlerRegistration mouseUpHandlerRegistration;
    private HandlerRegistration contextMenuHandlerRegistration;

    protected String clickEventIdentifier;
    protected Paintable paintable;
    private ApplicationConnection client;

    public ClickEventHandler(Paintable paintable, String clickEventIdentifier) {
        this.paintable = paintable;
        this.clickEventIdentifier = clickEventIdentifier;
    }

    public void handleEventHandlerRegistration(ApplicationConnection client) {
        this.client = client;
        // Handle registering/unregistering of click handler depending on if
        // server side listeners have been added or removed.
        if (hasEventListener()) {
            if (clickHandlerRegistration == null) {
                clickHandlerRegistration = registerHandler(this, ClickEvent
                        .getType());
                mouseUpHandlerRegistration = registerHandler(this, MouseUpEvent
                        .getType());
                contextMenuHandlerRegistration = registerHandler(this,
                        ContextMenuEvent.getType());
                doubleClickHandlerRegistration = registerHandler(this,
                        DoubleClickEvent.getType());
            }
        } else {
            if (clickHandlerRegistration != null) {
                // Remove existing handlers
                clickHandlerRegistration.removeHandler();
                doubleClickHandlerRegistration.removeHandler();
                mouseUpHandlerRegistration.removeHandler();
                contextMenuHandlerRegistration.removeHandler();

                contextMenuHandlerRegistration = null;
                mouseUpHandlerRegistration = null;
                doubleClickHandlerRegistration = null;
                clickHandlerRegistration = null;

            }
        }

    }

    protected abstract <H extends EventHandler> HandlerRegistration registerHandler(
            final H handler, DomEvent.Type<H> type);

    protected ApplicationConnection getApplicationConnection() {
        return client;
    }

    public boolean hasEventListener() {
        return getApplicationConnection().hasEventListeners(paintable,
                clickEventIdentifier);
    }

    public void onClick(ClickEvent event) {
        if (hasEventListener()) {
            fireClick(event.getNativeEvent());
        }
    }

    protected void fireClick(NativeEvent event) {
        ApplicationConnection client = getApplicationConnection();
        String pid = getApplicationConnection().getPid(paintable);

        MouseEventDetails mouseDetails = new MouseEventDetails(event);

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("mouseDetails", mouseDetails.serialize());
        client.updateVariable(pid, clickEventIdentifier, parameters, true);

    }

    public void onContextMenu(ContextMenuEvent event) {
        if (hasEventListener()) {
            // Prevent showing the browser's context menu when there is a right
            // click listener.
            event.preventDefault();
        }

    }

    public void onMouseUp(MouseUpEvent event) {
        // TODO For perfect accuracy we should check that a mousedown has
        // occured on this element before this mouseup and that no mouseup
        // has occured anywhere after that.
        if (hasEventListener()) {
            if (event.getNativeButton() != NativeEvent.BUTTON_LEFT) {
                // "Click" with right or middle button
                fireClick(event.getNativeEvent());

            }
        }
    }

    public void onDoubleClick(DoubleClickEvent event) {
        if (hasEventListener()) {
            fireClick(event.getNativeEvent());
        }
    }

}