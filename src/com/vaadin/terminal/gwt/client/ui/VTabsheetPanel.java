/* 
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.Util;

/**
 * A panel that displays all of its child widgets in a 'deck', where only one
 * can be visible at a time. It is used by
 * {@link com.vaadin.terminal.gwt.client.ui.VTabsheet}.
 * 
 * This class has the same basic functionality as the GWT DeckPanel
 * {@link com.google.gwt.user.client.ui.DeckPanel}, with the exception that it
 * doesn't manipulate the child widgets' width and height attributes.
 */
public class VTabsheetPanel extends ComplexPanel {

    private Widget visibleWidget;
    private TouchScrollDelegate touchScrollDelegate;

    /**
     * Creates an empty tabsheet panel.
     */
    public VTabsheetPanel() {
        setElement(DOM.createDiv());
        sinkEvents(Event.TOUCHEVENTS);
        addDomHandler(new TouchStartHandler() {
            public void onTouchStart(TouchStartEvent event) {
                /*
                 * All container elements needs to be scrollable by one finger.
                 * Update the scrollable element list of touch delegate on each
                 * touch start.
                 */
                NodeList<Node> childNodes = getElement().getChildNodes();
                Element[] elements = new Element[childNodes.getLength()];
                for (int i = 0; i < elements.length; i++) {
                    elements[i] = (Element) childNodes.getItem(i);
                }
                getTouchScrollDelegate().setElements(elements);
                getTouchScrollDelegate().onTouchStart(event);
            }
        }, TouchStartEvent.getType());
    }

    protected TouchScrollDelegate getTouchScrollDelegate() {
        if (touchScrollDelegate == null) {
            touchScrollDelegate = new TouchScrollDelegate();
        }
        return touchScrollDelegate;

    }

    /**
     * Adds the specified widget to the deck.
     * 
     * @param w
     *            the widget to be added
     */
    @Override
    public void add(Widget w) {
        Element el = createContainerElement();
        DOM.appendChild(getElement(), el);
        super.add(w, el);
    }

    private Element createContainerElement() {
        Element el = DOM.createDiv();
        DOM.setStyleAttribute(el, "position", "absolute");
        DOM.setStyleAttribute(el, "overflow", "auto");
        hide(el);
        return el;
    }

    /**
     * Gets the index of the currently-visible widget.
     * 
     * @return the visible widget's index
     */
    public int getVisibleWidget() {
        return getWidgetIndex(visibleWidget);
    }

    /**
     * Inserts a widget before the specified index.
     * 
     * @param w
     *            the widget to be inserted
     * @param beforeIndex
     *            the index before which it will be inserted
     * @throws IndexOutOfBoundsException
     *             if <code>beforeIndex</code> is out of range
     */
    public void insert(Widget w, int beforeIndex) {
        Element el = createContainerElement();
        DOM.insertChild(getElement(), el, beforeIndex);
        super.insert(w, el, beforeIndex, false);
    }

    @Override
    public boolean remove(Widget w) {
        Element child = w.getElement();
        Element parent = null;
        if (child != null) {
            parent = DOM.getParent(child);
        }
        final boolean removed = super.remove(w);
        if (removed) {
            if (visibleWidget == w) {
                visibleWidget = null;
            }
            if (parent != null) {
                DOM.removeChild(getElement(), parent);
            }
        }
        return removed;
    }

    /**
     * Shows the widget at the specified index. This causes the currently-
     * visible widget to be hidden.
     * 
     * @param index
     *            the index of the widget to be shown
     */
    public void showWidget(int index) {
        checkIndexBoundsForAccess(index);
        Widget newVisible = getWidget(index);
        if (visibleWidget != newVisible) {
            if (visibleWidget != null) {
                hide(DOM.getParent(visibleWidget.getElement()));
            }
            visibleWidget = newVisible;
            unHide(DOM.getParent(visibleWidget.getElement()));
        }
    }

    private void hide(Element e) {
        DOM.setStyleAttribute(e, "visibility", "hidden");
        DOM.setStyleAttribute(e, "top", "-100000px");
        DOM.setStyleAttribute(e, "left", "-100000px");
    }

    private void unHide(Element e) {
        DOM.setStyleAttribute(e, "top", "0px");
        DOM.setStyleAttribute(e, "left", "0px");
        DOM.setStyleAttribute(e, "visibility", "");
    }

    public void fixVisibleTabSize(int width, int height, int minWidth) {
        if (visibleWidget == null) {
            return;
        }

        boolean dynamicHeight = false;

        if (height < 0) {
            height = visibleWidget.getOffsetHeight();
            dynamicHeight = true;
        }
        if (width < 0) {
            width = visibleWidget.getOffsetWidth();
        }
        if (width < minWidth) {
            width = minWidth;
        }

        Element wrapperDiv = (Element) visibleWidget.getElement()
                .getParentElement();

        // width first
        getElement().getStyle().setPropertyPx("width", width);
        wrapperDiv.getStyle().setPropertyPx("width", width);

        if (dynamicHeight) {
            // height of widget might have changed due wrapping
            height = visibleWidget.getOffsetHeight();
        }
        // v-tabsheet-tabsheetpanel height
        getElement().getStyle().setPropertyPx("height", height);

        // widget wrapper height
        wrapperDiv.getStyle().setPropertyPx("height", height);
        runWebkitOverflowAutoFix();
    }

    public void runWebkitOverflowAutoFix() {
        if (visibleWidget != null) {
            Util.runWebkitOverflowAutoFix(DOM.getParent(visibleWidget
                    .getElement()));
        }

    }

    public void replaceComponent(Widget oldComponent, Widget newComponent) {
        boolean isVisible = (visibleWidget == oldComponent);
        int widgetIndex = getWidgetIndex(oldComponent);
        remove(oldComponent);
        insert(newComponent, widgetIndex);
        if (isVisible) {
            showWidget(widgetIndex);
        }
    }
}
