/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.itmill.toolkit.terminal.gwt.client.ui;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollListener;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.itmill.toolkit.terminal.gwt.client.ApplicationConnection;
import com.itmill.toolkit.terminal.gwt.client.BrowserInfo;
import com.itmill.toolkit.terminal.gwt.client.Container;
import com.itmill.toolkit.terminal.gwt.client.Paintable;
import com.itmill.toolkit.terminal.gwt.client.RenderSpace;
import com.itmill.toolkit.terminal.gwt.client.UIDL;
import com.itmill.toolkit.terminal.gwt.client.Util;

/**
 * "Sub window" component.
 * 
 * TODO update position / scroll position / size to client
 * 
 * @author IT Mill Ltd
 */
public class IWindow extends IToolkitOverlay implements Container,
        ScrollListener {

    private static final int MIN_HEIGHT = 60;

    private static final int MIN_WIDTH = 80;

    private static Vector<IWindow> windowOrder = new Vector<IWindow>();

    public static final String CLASSNAME = "i-window";

    /**
     * Pixels used by inner borders and paddings horizontally (calculated only
     * once)
     */
    private int borderWidth = -1;

    /**
     * Pixels used by inner borders and paddings vertically (calculated only
     * once)
     */
    private int borderHeight = -1;

    private static final int STACKING_OFFSET_PIXELS = 15;

    public static final int Z_INDEX = 10000;

    private Paintable layout;

    private Element contents;

    private Element header;

    private Element footer;

    private Element resizeBox;

    private final ScrollPanel contentPanel = new ScrollPanel();

    private boolean dragging;

    private int startX;

    private int startY;

    private int origX;

    private int origY;

    private boolean resizing;

    private int origW;

    private int origH;

    private Element closeBox;

    protected ApplicationConnection client;

    private String id;

    ShortcutActionHandler shortcutHandler;

    /** Last known positionx read from UIDL or updated to application connection */
    private int uidlPositionX = -1;

    /** Last known positiony read from UIDL or updated to application connection */
    private int uidlPositionY = -1;

    private boolean modal = false;

    private boolean resizable = true;

    private Element modalityCurtain;
    private Element draggingCurtain;

    private Element headerText;

    private boolean readonly;

    private RenderSpace renderSpace = new RenderSpace(0, 0, true);

    private String width;

    private String height;

    public IWindow() {
        super(false, false, true); // no autohide, not modal, shadow
        // Different style of shadow for windows
        setShadowStyle("window");

        final int order = windowOrder.size();
        setWindowOrder(order);
        windowOrder.add(this);
        constructDOM();
        setPopupPosition(order * STACKING_OFFSET_PIXELS, order
                * STACKING_OFFSET_PIXELS);
        contentPanel.addScrollListener(this);
    }

    private void bringToFront() {
        int curIndex = windowOrder.indexOf(this);
        if (curIndex + 1 < windowOrder.size()) {
            windowOrder.remove(this);
            windowOrder.add(this);
            for (; curIndex < windowOrder.size(); curIndex++) {
                windowOrder.get(curIndex).setWindowOrder(curIndex);
            }
        }
    }

    /**
     * Returns true if window is the topmost window
     * 
     * @return
     */
    private boolean isActive() {
        return windowOrder.lastElement().equals(this);
    }

    public void setWindowOrder(int order) {
        setZIndex(order + Z_INDEX);
    }

    @Override
    protected void setZIndex(int zIndex) {
        super.setZIndex(zIndex);
        if (modal) {
            DOM.setStyleAttribute(modalityCurtain, "zIndex", "" + zIndex);
        }
    }

    protected void constructDOM() {
        setStyleName(CLASSNAME);

        header = DOM.createDiv();
        DOM.setElementProperty(header, "className", CLASSNAME + "-outerheader");
        headerText = DOM.createDiv();
        DOM.setElementProperty(headerText, "className", CLASSNAME + "-header");
        contents = DOM.createDiv();
        DOM.setElementProperty(contents, "className", CLASSNAME + "-contents");
        footer = DOM.createDiv();
        DOM.setElementProperty(footer, "className", CLASSNAME + "-footer");
        resizeBox = DOM.createDiv();
        DOM
                .setElementProperty(resizeBox, "className", CLASSNAME
                        + "-resizebox");
        closeBox = DOM.createDiv();
        DOM.setElementProperty(closeBox, "className", CLASSNAME + "-closebox");
        DOM.appendChild(footer, resizeBox);

        DOM.sinkEvents(getElement(), Event.ONLOSECAPTURE);
        DOM.sinkEvents(closeBox, Event.ONCLICK);
        DOM.sinkEvents(contents, Event.ONCLICK);

        final Element wrapper = DOM.createDiv();
        DOM.setElementProperty(wrapper, "className", CLASSNAME + "-wrap");

        final Element wrapper2 = DOM.createDiv();
        DOM.setElementProperty(wrapper2, "className", CLASSNAME + "-wrap2");

        DOM.sinkEvents(wrapper, Event.ONKEYDOWN);

        DOM.appendChild(wrapper2, closeBox);
        DOM.appendChild(wrapper2, header);
        DOM.appendChild(header, headerText);
        DOM.appendChild(wrapper2, contents);
        DOM.appendChild(wrapper2, footer);
        DOM.appendChild(wrapper, wrapper2);
        DOM.appendChild(super.getContainerElement(), wrapper);

        sinkEvents(Event.MOUSEEVENTS);

        setWidget(contentPanel);

    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        id = uidl.getId();
        this.client = client;

        // Workaround needed for Testing Tools (GWT generates window DOM
        // slightly different in different browsers).
        DOM.setElementProperty(closeBox, "id", id + "_window_close");

        if (uidl.hasAttribute("invisible")) {
            hide();
            return;
        }

        if (!uidl.hasAttribute("cached")) {
            if (uidl.getBooleanAttribute("modal") != modal) {
                setModal(!modal);
            }
            if (!isAttached()) {
                show();
            }
        }

        if (client.updateComponent(this, uidl, false)) {
            return;
        }

        if (uidl.getBooleanAttribute("resizable") != resizable) {
            setResizable(!resizable);
        }

        if (isReadOnly() != uidl.getBooleanAttribute("readonly")) {
            setReadOnly(!isReadOnly());
        }

        // Initialize the position form UIDL
        try {
            final int positionx = uidl.getIntVariable("positionx");
            final int positiony = uidl.getIntVariable("positiony");
            if (positionx >= 0 && positiony >= 0) {
                setPopupPosition(positionx, positiony);
            }
        } catch (final IllegalArgumentException e) {
            // Silently ignored as positionx and positiony are not required
            // parameters
        }

        if (uidl.hasAttribute("caption")) {
            setCaption(uidl.getStringAttribute("caption"), uidl
                    .getStringAttribute("icon"));
        }

        boolean showingUrl = false;
        int childIndex = 0;
        UIDL childUidl = uidl.getChildUIDL(childIndex++);
        while ("open".equals(childUidl.getTag())) {
            // TODO multiple opens with the same target will in practice just
            // open the last one - should we fix that somehow?
            final String parsedUri = client.translateToolkitUri(childUidl
                    .getStringAttribute("src"));
            if (!childUidl.hasAttribute("name")) {
                final Frame frame = new Frame();
                DOM.setStyleAttribute(frame.getElement(), "width", "100%");
                DOM.setStyleAttribute(frame.getElement(), "height", "100%");
                DOM.setStyleAttribute(frame.getElement(), "border", "0px");
                frame.setUrl(parsedUri);
                contentPanel.setWidget(frame);
                showingUrl = true;
            } else {
                final String target = childUidl.getStringAttribute("name");
                Window.open(parsedUri, target, "");
            }
            childUidl = uidl.getChildUIDL(childIndex++);
        }

        final Paintable lo = client.getPaintable(childUidl);
        if (layout != null) {
            if (layout != lo) {
                // remove old
                client.unregisterPaintable(layout);
                contentPanel.remove((Widget) layout);
                // add new
                if (!showingUrl) {
                    contentPanel.setWidget((Widget) lo);
                }
                layout = lo;
            }
        } else if (!showingUrl) {
            contentPanel.setWidget((Widget) lo);
            layout = lo;
        }
        lo.updateFromUIDL(childUidl, client);

        // If no explicit width is specified, calculate natural width for window
        // and set it explicitly
        if (!uidl.hasAttribute("width")) {
            final int naturalWidth = getElement().getOffsetWidth();
            setWidth(naturalWidth + "px");
        }

        // we may have actions and notifications
        if (uidl.getChildCount() > 1) {
            final int cnt = uidl.getChildCount();
            for (int i = 1; i < cnt; i++) {
                childUidl = uidl.getChildUIDL(i);
                if (childUidl.getTag().equals("actions")) {
                    if (shortcutHandler == null) {
                        shortcutHandler = new ShortcutActionHandler(id, client);
                    }
                    shortcutHandler.updateActionMap(childUidl);
                } else if (childUidl.getTag().equals("notifications")) {
                    // TODO needed? move ->
                    for (final Iterator it = childUidl.getChildIterator(); it
                            .hasNext();) {
                        final UIDL notification = (UIDL) it.next();
                        String html = "";
                        if (notification.hasAttribute("icon")) {
                            final String parsedUri = client
                                    .translateToolkitUri(notification
                                            .getStringAttribute("icon"));
                            html += "<img src=\"" + parsedUri + "\" />";
                        }
                        if (notification.hasAttribute("caption")) {
                            html += "<h1>"
                                    + notification
                                            .getStringAttribute("caption")
                                    + "</h1>";
                        }
                        if (notification.hasAttribute("message")) {
                            html += "<p>"
                                    + notification
                                            .getStringAttribute("message")
                                    + "</p>";
                        }

                        final String style = notification.hasAttribute("style") ? notification
                                .getStringAttribute("style")
                                : null;
                        final int position = notification
                                .getIntAttribute("position");
                        final int delay = notification.getIntAttribute("delay");
                        new INotification(delay).show(html, position, style);
                    }
                }
            }

        }

        // setting scrollposition must happen after children is rendered
        contentPanel.setScrollPosition(uidl.getIntVariable("scrollTop"));
        contentPanel.setHorizontalScrollPosition(uidl
                .getIntVariable("scrollLeft"));

        // Center this window on screen if requested
        // This has to be here because we might not know the content size before
        // everything is painted into the window
        if (uidl.getBooleanAttribute("center")) {
            center();
        }

        updateShadowSizeAndPosition();
    }

    private void setReadOnly(boolean readonly) {
        this.readonly = readonly;
        if (readonly) {
            DOM.setStyleAttribute(closeBox, "display", "none");
        } else {
            DOM.setStyleAttribute(closeBox, "display", "");
        }
    }

    private boolean isReadOnly() {
        return readonly;
    }

    @Override
    public void show() {
        if (modal) {
            showModalityCurtain();
        }
        super.show();

        setFF2CaretFixEnabled(true);
        fixFF3OverflowBug();
    }

    /** Disable overflow auto with FF3 to fix #1837. */
    private void fixFF3OverflowBug() {
        if (BrowserInfo.get().isFF3()) {
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    DOM.setStyleAttribute(getElement(), "overflow", "");
                }
            });
        }
    }

    /**
     * Fix "missing cursor" browser bug workaround for FF2 in Windows and Linux.
     * 
     * Calling this method has no effect on other browsers than the ones based
     * on Gecko 1.8
     * 
     * @param enable
     */
    private void setFF2CaretFixEnabled(boolean enable) {
        if (BrowserInfo.get().isFF2()) {
            if (enable) {
                DeferredCommand.addCommand(new Command() {
                    public void execute() {
                        DOM.setStyleAttribute(getElement(), "overflow", "auto");
                    }
                });
            } else {
                DOM.setStyleAttribute(getElement(), "overflow", "");
            }
        }
    }

    @Override
    public void hide() {
        if (modal) {
            hideModalityCurtain();
        }
        super.hide();
    }

    private void setModal(boolean modality) {
        modal = modality;
        if (modal) {
            modalityCurtain = DOM.createDiv();
            DOM.setElementProperty(modalityCurtain, "className", CLASSNAME
                    + "-modalitycurtain");
            if (isAttached()) {
                showModalityCurtain();
                bringToFront();
            } else {
                DeferredCommand.addCommand(new Command() {
                    public void execute() {
                        // modal window must on top of others
                        bringToFront();
                    }
                });
            }
        } else {
            if (modalityCurtain != null) {
                if (isAttached()) {
                    hideModalityCurtain();
                }
                modalityCurtain = null;
            }
        }
    }

    private void showModalityCurtain() {
        if (BrowserInfo.get().isFF2()) {
            DOM.setStyleAttribute(modalityCurtain, "height", DOM
                    .getElementPropertyInt(RootPanel.getBodyElement(),
                            "offsetHeight")
                    + "px");
            DOM.setStyleAttribute(modalityCurtain, "position", "absolute");
        }
        DOM.setStyleAttribute(modalityCurtain, "zIndex", ""
                + (windowOrder.indexOf(this) + Z_INDEX));
        DOM.appendChild(RootPanel.getBodyElement(), modalityCurtain);
    }

    private void hideModalityCurtain() {
        DOM.removeChild(RootPanel.getBodyElement(), modalityCurtain);
    }

    /*
     * Shows (or hides) an empty div on top of all other content; used when
     * resizing or moving, so that iframes (etc) do not steal event.
     */
    private void showDraggingCurtain(boolean show) {
        if (show && draggingCurtain == null) {

            setFF2CaretFixEnabled(false); // makes FF2 slow

            draggingCurtain = DOM.createDiv();
            DOM.setStyleAttribute(draggingCurtain, "position", "absolute");
            DOM.setStyleAttribute(draggingCurtain, "top", "0px");
            DOM.setStyleAttribute(draggingCurtain, "left", "0px");
            DOM.setStyleAttribute(draggingCurtain, "width", "100%");
            DOM.setStyleAttribute(draggingCurtain, "height", "100%");
            DOM.setStyleAttribute(draggingCurtain, "zIndex", ""
                    + IToolkitOverlay.Z_INDEX);

            DOM.appendChild(RootPanel.getBodyElement(), draggingCurtain);
        } else if (!show && draggingCurtain != null) {

            setFF2CaretFixEnabled(true); // makes FF2 slow

            DOM.removeChild(RootPanel.getBodyElement(), draggingCurtain);
            draggingCurtain = null;
        }

    }

    private void setResizable(boolean resizability) {
        resizable = resizability;
        if (resizability) {
            DOM.setElementProperty(resizeBox, "className", CLASSNAME
                    + "-resizebox");
        } else {
            DOM.setElementProperty(resizeBox, "className", CLASSNAME
                    + "-resizebox " + CLASSNAME + "-resizebox-disabled");
        }
    }

    @Override
    public void setPopupPosition(int left, int top) {
        super.setPopupPosition(left, top);
        if (left != uidlPositionX && client != null) {
            client.updateVariable(id, "positionx", left, false);
            uidlPositionX = left;
        }
        if (top != uidlPositionY && client != null) {
            client.updateVariable(id, "positiony", top, false);
            uidlPositionY = top;
        }
    }

    public void setCaption(String c) {
        setCaption(c, null);
    }

    public void setCaption(String c, String icon) {
        String html = Util.escapeHTML(c);
        if (icon != null) {
            icon = client.translateToolkitUri(icon);
            html = "<img src=\"" + icon + "\" class=\"i-icon\" />" + html;
        }
        DOM.setInnerHTML(headerText, html);
    }

    @Override
    protected Element getContainerElement() {
        // in GWT 1.5 this method is used in PopupPanel constructor
        if (contents == null) {
            return super.getContainerElement();
        }
        return contents;
    }

    @Override
    public void onBrowserEvent(final Event event) {
        final int type = event.getTypeInt();

        if (type == Event.ONKEYDOWN && shortcutHandler != null) {
            shortcutHandler.handleKeyboardEvent(event);
            return;
        }

        final Element target = DOM.eventGetTarget(event);

        // Handle window caption tooltips
        if (client != null && DOM.isOrHasChild(header, target)) {
            client.handleTooltipEvent(event, this);
        }

        if (resizing || resizeBox == target) {
            onResizeEvent(event);
            event.cancelBubble(true);
        } else if (target == closeBox) {
            if (type == Event.ONCLICK) {
                onCloseClick();
                event.cancelBubble(true);
            }
        } else if (dragging || !DOM.isOrHasChild(contents, target)) {
            onDragEvent(event);
            event.cancelBubble(true);
        } else if (type == Event.ONCLICK) {
            // clicked inside window, ensure to be on top
            if (!isActive()) {
                bringToFront();
            }
        }
    }

    private void onCloseClick() {
        client.updateVariable(id, "close", true, true);
    }

    private void onResizeEvent(Event event) {
        if (resizable) {
            switch (event.getTypeInt()) {
            case Event.ONMOUSEDOWN:
                if (!isActive()) {
                    bringToFront();
                }
                showDraggingCurtain(true);
                if (BrowserInfo.get().isIE()) {
                    DOM.setStyleAttribute(resizeBox, "visibility", "hidden");
                }
                resizing = true;
                startX = event.getScreenX();
                startY = event.getScreenY();
                origW = getElement().getOffsetWidth();
                origH = getElement().getOffsetHeight();
                DOM.setCapture(getElement());
                event.preventDefault();
                break;
            case Event.ONMOUSEUP:
                showDraggingCurtain(false);
                if (BrowserInfo.get().isIE()) {
                    DOM.setStyleAttribute(resizeBox, "visibility", "");
                }
                resizing = false;
                DOM.releaseCapture(getElement());
                setSize(event, true);
                break;
            case Event.ONLOSECAPTURE:
                showDraggingCurtain(false);
                if (BrowserInfo.get().isIE()) {
                    DOM.setStyleAttribute(resizeBox, "visibility", "");
                }
                resizing = false;
            case Event.ONMOUSEMOVE:
                if (resizing) {
                    setSize(event, false);
                    event.preventDefault();
                }
                break;
            default:
                event.preventDefault();
                break;
            }
        }
    }

    private void setSize(Event event, boolean updateVariables) {
        int w = event.getScreenX() - startX + origW;
        if (w < MIN_WIDTH) {
            w = MIN_WIDTH;
        }

        int h = event.getScreenY() - startY + origH;
        if (h < MIN_HEIGHT) {
            h = MIN_HEIGHT;
        }

        setWidth(w + "px");
        setHeight(h + "px");

        if (updateVariables) {
            // sending width back always as pixels, no need for unit
            client.updateVariable(id, "width", w, false);
            client.updateVariable(id, "height", h, false);
        }

        // Update child widget dimensions
        if (client != null) {
            client.handleComponentRelativeSize((Widget) layout);
            client.runDescendentsLayout((HasWidgets) layout);
        }
    }

    @Override
    /*
     * Width is set to the out-most element (i-window).
     * 
     * This function should never be called with percentage values (it will
     * throw an exception)
     */
    public void setWidth(String width) {
        this.width = width;
        if (!isAttached()) {
            return;
        }
        if (width != null && !"".equals(width)) {
            int pixelWidth;
            // Convert non-pixel values to pixels
            if (width.indexOf("px") < 0) {
                DOM.setStyleAttribute(getElement(), "width", width);
                pixelWidth = getElement().getOffsetWidth();
                width = pixelWidth + "px";
            }
            if (BrowserInfo.get().isIE6()) {
                getElement().getStyle().setProperty("overflow", "hidden");
            }
            getElement().getStyle().setProperty("width", width);

            pixelWidth = getElement().getOffsetWidth() - borderWidth;

            renderSpace.setWidth(pixelWidth);

            // IE6 needs the actual inner content width on the content element,
            // otherwise it won't wrap the content properly (no scrollbars
            // appear, content flows out of window)
            if (BrowserInfo.get().isIE6()) {
                DOM.setStyleAttribute(contentPanel.getElement(), "width",
                        pixelWidth + "px");
            }
            updateShadowSizeAndPosition();
        }
    }

    @Override
    /*
     * Height is set to the out-most element (i-window).
     * 
     * This function should never be called with percentage values (it will
     * throw an exception)
     */
    public void setHeight(String height) {
        this.height = height;
        if (!isAttached()) {
            return;
        }
        if (height != null && !"".equals(height)) {
            DOM.setStyleAttribute(getElement(), "height", height);
            int pixels = getElement().getOffsetHeight() - getExtraHeight();
            if (pixels < 0) {
                pixels = 0;
            }
            renderSpace.setHeight(pixels);
            height = pixels + "px";
            contentPanel.getElement().getStyle().setProperty("height", height);
            updateShadowSizeAndPosition();

        }
    }

    private int extraH = 0;

    private int getExtraHeight() {
        extraH = header.getOffsetHeight() + footer.getOffsetHeight();
        return extraH;
    }

    private void onDragEvent(Event event) {
        switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEDOWN:
            if (!isActive()) {
                bringToFront();
            }
            showDraggingCurtain(true);
            dragging = true;
            startX = DOM.eventGetScreenX(event);
            startY = DOM.eventGetScreenY(event);
            origX = DOM.getAbsoluteLeft(getElement());
            origY = DOM.getAbsoluteTop(getElement());
            DOM.setCapture(getElement());
            DOM.eventPreventDefault(event);
            break;
        case Event.ONMOUSEUP:
            dragging = false;
            showDraggingCurtain(false);
            DOM.releaseCapture(getElement());
            break;
        case Event.ONLOSECAPTURE:
            showDraggingCurtain(false);
            dragging = false;
            break;
        case Event.ONMOUSEMOVE:
            if (dragging) {
                final int x = DOM.eventGetScreenX(event) - startX + origX;
                final int y = DOM.eventGetScreenY(event) - startY + origY;
                setPopupPosition(x, y);
                DOM.eventPreventDefault(event);
            }
            break;
        default:
            break;
        }
    }

    @Override
    public boolean onEventPreview(Event event) {
        if (dragging) {
            onDragEvent(event);
            return false;
        } else if (resizing) {
            onResizeEvent(event);
            return false;
        } else if (modal) {
            // return false when modal and outside window
            final Element target = event.getTarget().cast();
            if (!DOM.isOrHasChild(getElement(), target)) {
                return false;
            }
        }
        return true;
    }

    public void onScroll(Widget widget, int scrollLeft, int scrollTop) {
        client.updateVariable(id, "scrollTop", scrollTop, false);
        client.updateVariable(id, "scrollLeft", scrollLeft, false);
    }

    @Override
    public void addStyleDependentName(String styleSuffix) {
        // IWindow's getStyleElement() does not return the same element as
        // getElement(), so we need to override this.
        setStyleName(getElement(), getStylePrimaryName() + "-" + styleSuffix,
                true);
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        // Calculate space required by window borders, so we can accurately
        // calculate space for content

        // IE (IE6 especially) requires some magic tricks to pull the border
        // size correctly (remember that we want to accomodate for paddings as
        // well)
        if (BrowserInfo.get().isIE()) {
            DOM.setStyleAttribute(contents, "width", "7000px");
            DOM.setStyleAttribute(contentPanel.getElement(), "width", "7000px");
            int contentWidth = DOM.getElementPropertyInt(contentPanel
                    .getElement(), "offsetWidth");
            contentWidth = DOM.getElementPropertyInt(contentPanel.getElement(),
                    "offsetWidth");
            final int windowWidth = DOM.getElementPropertyInt(getElement(),
                    "offsetWidth");
            DOM.setStyleAttribute(contentPanel.getElement(), "width", "");
            DOM.setStyleAttribute(contents, "width", "");

            borderWidth = windowWidth - contentWidth;
        }

        // Standards based browsers get away with it a little easier :)
        else {
            final int contentWidth = DOM.getElementPropertyInt(contentPanel
                    .getElement(), "offsetWidth");
            final int windowWidth = DOM.getElementPropertyInt(getElement(),
                    "offsetWidth");
            borderWidth = windowWidth - contentWidth;
        }

        setWidth(width);
        setHeight(height);

    }

    public RenderSpace getAllocatedSpace(Widget child) {
        if (child == layout) {
            return renderSpace;
        } else {
            // Exception ??
            return null;
        }
    }

    public boolean hasChildComponent(Widget component) {
        if (component == layout) {
            return true;
        } else {
            return false;
        }
    }

    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        contentPanel.setWidget(newComponent);
    }

    public boolean requestLayout(Set<Paintable> child) {
        updateShadowSizeAndPosition();
        return true;
    }

    public void updateCaption(Paintable component, UIDL uidl) {
        // NOP, window has own caption, layout captio not rendered
    }

}
