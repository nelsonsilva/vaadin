/*
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.DomEvent.Type;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.Container;
import com.vaadin.terminal.gwt.client.Focusable;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.RenderSpace;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.ui.ShortcutActionHandler.ShortcutActionHandlerOwner;

/**
 *
 */
public class VView extends SimplePanel implements Container, ResizeHandler,
        Window.ClosingHandler, ShortcutActionHandlerOwner, Focusable {

    public static final String FRAGMENT_VARIABLE = "fragment";

    private static final String CLASSNAME = "v-view";

    public static final String NOTIFICATION_HTML_CONTENT_NOT_ALLOWED = "useplain";

    private String theme;

    private Paintable layout;

    private final LinkedHashSet<VWindow> subWindows = new LinkedHashSet<VWindow>();

    private String id;

    private ShortcutActionHandler actionHandler;

    /** stored width for IE resize optimization */
    private int width;

    /** stored height for IE resize optimization */
    private int height;

    private ApplicationConnection connection;

    /**
     * We are postponing resize process with IE. IE bugs with scrollbars in some
     * situations, that causes false onWindowResized calls. With Timer we will
     * give IE some time to decide if it really wants to keep current size
     * (scrollbars).
     */
    private Timer resizeTimer;

    private int scrollTop;

    private int scrollLeft;

    private boolean rendering;

    private boolean scrollable;

    private boolean immediate;

    private boolean resizeLazy = false;

    /**
     * Attribute name for the lazy resize setting .
     */
    public static final String RESIZE_LAZY = "rL";

    /**
     * Reference to the parent frame/iframe. Null if there is no parent (i)frame
     * or if the application and parent frame are in different domains.
     */
    private Element parentFrame;

    private HandlerRegistration historyHandlerRegistration;

    /**
     * The current URI fragment, used to avoid sending updates if nothing has
     * changed.
     */
    private String currentFragment;

    /**
     * Listener for URI fragment changes. Notifies the server of the new value
     * whenever the value changes.
     */
    private final ValueChangeHandler<String> historyChangeHandler = new ValueChangeHandler<String>() {
        public void onValueChange(ValueChangeEvent<String> event) {
            String newFragment = event.getValue();

            // Send the new fragment to the server if it has changed
            if (!newFragment.equals(currentFragment) && connection != null) {
                currentFragment = newFragment;
                connection.updateVariable(id, FRAGMENT_VARIABLE, newFragment,
                        true);
            }
        }
    };

    private ClickEventHandler clickEventHandler = new ClickEventHandler(this,
            VPanel.CLICK_EVENT_IDENTIFIER) {

        @Override
        protected <H extends EventHandler> HandlerRegistration registerHandler(
                H handler, Type<H> type) {
            return addDomHandler(handler, type);
        }
    };

    private VLazyExecutor delayedResizeExecutor = new VLazyExecutor(200,
            new ScheduledCommand() {
                public void execute() {
                    windowSizeMaybeChanged(Window.getClientWidth(),
                            Window.getClientHeight());
                }

            });

    public VView() {
        super();
        setStyleName(CLASSNAME);

        // Allow focusing the view by using the focus() method, the view
        // should not be in the document focus flow
        getElement().setTabIndex(-1);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        historyHandlerRegistration = History
                .addValueChangeHandler(historyChangeHandler);
        currentFragment = History.getToken();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        historyHandlerRegistration.removeHandler();
        historyHandlerRegistration = null;
    }

    /**
     * Called when the window might have been resized.
     * 
     * @param newWidth
     *            The new width of the window
     * @param newHeight
     *            The new height of the window
     */
    protected void windowSizeMaybeChanged(int newWidth, int newHeight) {
        boolean changed = false;
        if (width != newWidth) {
            width = newWidth;
            changed = true;
            VConsole.log("New window width: " + width);
        }
        if (height != newHeight) {
            height = newHeight;
            changed = true;
            VConsole.log("New window height: " + height);
        }
        if (changed) {
            VConsole.log("Running layout functions due to window resize");
            connection.runDescendentsLayout(VView.this);
            Util.runWebkitOverflowAutoFix(getElement());

            sendClientResized();
        }
    }

    public String getTheme() {
        return theme;
    }

    /**
     * Used to reload host page on theme changes.
     */
    private static native void reloadHostPage()
    /*-{
         $wnd.location.reload();
     }-*/;

    /**
     * Evaluate the given script in the browser document.
     * 
     * @param script
     *            Script to be executed.
     */
    private static native void eval(String script)
    /*-{
      try {
         if (script == null) return;
         $wnd.eval(script);
      } catch (e) {
      }
    }-*/;

    /**
     * Returns true if the body is NOT generated, i.e if someone else has made
     * the page that we're running in. Otherwise we're in charge of the whole
     * page.
     * 
     * @return true if we're running embedded
     */
    public boolean isEmbedded() {
        return !getElement().getOwnerDocument().getBody().getClassName()
                .contains(ApplicationConnection.GENERATED_BODY_CLASSNAME);
    }

    public void updateFromUIDL(final UIDL uidl, ApplicationConnection client) {
        rendering = true;

        id = uidl.getId();
        boolean firstPaint = connection == null;
        connection = client;

        immediate = uidl.hasAttribute("immediate");
        resizeLazy = uidl.hasAttribute(RESIZE_LAZY);
        String newTheme = uidl.getStringAttribute("theme");
        if (theme != null && !newTheme.equals(theme)) {
            // Complete page refresh is needed due css can affect layout
            // calculations etc
            reloadHostPage();
        } else {
            theme = newTheme;
        }
        if (uidl.hasAttribute("style")) {
            setStyleName(getStylePrimaryName() + " "
                    + uidl.getStringAttribute("style"));
        }

        clickEventHandler.handleEventHandlerRegistration(client);

        if (!isEmbedded() && uidl.hasAttribute("caption")) {
            // only change window title if we're in charge of the whole page
            com.google.gwt.user.client.Window.setTitle(uidl
                    .getStringAttribute("caption"));
        }

        // Process children
        int childIndex = 0;

        // Open URL:s
        boolean isClosed = false; // was this window closed?
        while (childIndex < uidl.getChildCount()
                && "open".equals(uidl.getChildUIDL(childIndex).getTag())) {
            final UIDL open = uidl.getChildUIDL(childIndex);
            final String url = client.translateVaadinUri(open
                    .getStringAttribute("src"));
            final String target = open.getStringAttribute("name");
            if (target == null) {
                // source will be opened to this browser window, but we may have
                // to finish rendering this window in case this is a download
                // (and window stays open).
                Scheduler.get().scheduleDeferred(new Command() {
                    public void execute() {
                        goTo(url);
                    }
                });
            } else if ("_self".equals(target)) {
                // This window is closing (for sure). Only other opens are
                // relevant in this change. See #3558, #2144
                isClosed = true;
                goTo(url);
            } else {
                String options;
                if (open.hasAttribute("border")) {
                    if (open.getStringAttribute("border").equals("minimal")) {
                        options = "menubar=yes,location=no,status=no";
                    } else {
                        options = "menubar=no,location=no,status=no";
                    }

                } else {
                    options = "resizable=yes,menubar=yes,toolbar=yes,directories=yes,location=yes,scrollbars=yes,status=yes";
                }

                if (open.hasAttribute("width")) {
                    int w = open.getIntAttribute("width");
                    options += ",width=" + w;
                }
                if (open.hasAttribute("height")) {
                    int h = open.getIntAttribute("height");
                    options += ",height=" + h;
                }

                Window.open(url, target, options);
            }
            childIndex++;
        }
        if (isClosed) {
            // don't render the content, something else will be opened to this
            // browser view
            rendering = false;
            return;
        }

        // Draw this application level window
        UIDL childUidl = uidl.getChildUIDL(childIndex);
        final Paintable lo = client.getPaintable(childUidl);

        if (layout != null) {
            if (layout != lo) {
                // remove old
                client.unregisterPaintable(layout);
                // add new
                setWidget((Widget) lo);
                layout = lo;
            }
        } else {
            setWidget((Widget) lo);
            layout = lo;
        }

        layout.updateFromUIDL(childUidl, client);
        if (!childUidl.getBooleanAttribute("cached")) {
            updateParentFrameSize();
        }

        // Save currently open subwindows to track which will need to be closed
        final HashSet<VWindow> removedSubWindows = new HashSet<VWindow>(
                subWindows);

        // Handle other UIDL children
        while ((childUidl = uidl.getChildUIDL(++childIndex)) != null) {
            String tag = childUidl.getTag().intern();
            if (tag == "actions") {
                if (actionHandler == null) {
                    actionHandler = new ShortcutActionHandler(id, client);
                }
                actionHandler.updateActionMap(childUidl);
            } else if (tag == "execJS") {
                String script = childUidl.getStringAttribute("script");
                eval(script);
            } else if (tag == "notifications") {
                for (final Iterator<?> it = childUidl.getChildIterator(); it
                        .hasNext();) {
                    final UIDL notification = (UIDL) it.next();
                    VNotification.showNotification(client, notification);
                }
            } else {
                // subwindows
                final Paintable w = client.getPaintable(childUidl);
                if (subWindows.contains(w)) {
                    removedSubWindows.remove(w);
                } else {
                    subWindows.add((VWindow) w);
                }
                w.updateFromUIDL(childUidl, client);
            }
        }

        // Close old windows which where not in UIDL anymore
        for (final Iterator<VWindow> rem = removedSubWindows.iterator(); rem
                .hasNext();) {
            final VWindow w = rem.next();
            client.unregisterPaintable(w);
            subWindows.remove(w);
            w.hide();
        }

        if (uidl.hasAttribute("focused")) {
            // set focused component when render phase is finished
            Scheduler.get().scheduleDeferred(new Command() {
                public void execute() {
                    final Paintable toBeFocused = uidl.getPaintableAttribute(
                            "focused", connection);

                    /*
                     * Two types of Widgets can be focused, either implementing
                     * GWT HasFocus of a thinner Vaadin specific Focusable
                     * interface.
                     */
                    if (toBeFocused instanceof com.google.gwt.user.client.ui.Focusable) {
                        final com.google.gwt.user.client.ui.Focusable toBeFocusedWidget = (com.google.gwt.user.client.ui.Focusable) toBeFocused;
                        toBeFocusedWidget.setFocus(true);
                    } else if (toBeFocused instanceof Focusable) {
                        ((Focusable) toBeFocused).focus();
                    } else {
                        VConsole.log("Could not focus component");
                    }
                }
            });
        }

        // Add window listeners on first paint, to prevent premature
        // variablechanges
        if (firstPaint) {
            Window.addWindowClosingHandler(this);
            Window.addResizeHandler(this);
        }

        onResize();

        // finally set scroll position from UIDL
        if (uidl.hasVariable("scrollTop")) {
            scrollable = true;
            scrollTop = uidl.getIntVariable("scrollTop");
            DOM.setElementPropertyInt(getElement(), "scrollTop", scrollTop);
            scrollLeft = uidl.getIntVariable("scrollLeft");
            DOM.setElementPropertyInt(getElement(), "scrollLeft", scrollLeft);
        } else {
            scrollable = false;
        }

        // Safari workaround must be run after scrollTop is updated as it sets
        // scrollTop using a deferred command.
        if (BrowserInfo.get().isSafari()) {
            Util.runWebkitOverflowAutoFix(getElement());
        }

        scrollIntoView(uidl);

        if (uidl.hasAttribute(FRAGMENT_VARIABLE)) {
            currentFragment = uidl.getStringAttribute(FRAGMENT_VARIABLE);
            if (!currentFragment.equals(History.getToken())) {
                History.newItem(currentFragment, true);
            }
        } else {
            // Initial request for which the server doesn't yet have a fragment
            // (and haven't shown any interest in getting one)
            currentFragment = History.getToken();

            // Include current fragment in the next request
            client.updateVariable(id, FRAGMENT_VARIABLE, currentFragment, false);
        }

        rendering = false;
    }

    /**
     * Tries to scroll paintable referenced from given UIDL snippet to be
     * visible.
     * 
     * @param uidl
     */
    void scrollIntoView(final UIDL uidl) {
        if (uidl.hasAttribute("scrollTo")) {
            Scheduler.get().scheduleDeferred(new Command() {
                public void execute() {
                    final Paintable paintable = uidl.getPaintableAttribute(
                            "scrollTo", connection);
                    ((Widget) paintable).getElement().scrollIntoView();
                }
            });
        }
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        int type = DOM.eventGetType(event);
        if (type == Event.ONKEYDOWN && actionHandler != null) {
            actionHandler.handleKeyboardEvent(event);
            return;
        } else if (scrollable && type == Event.ONSCROLL) {
            updateScrollPosition();
        }
    }

    /**
     * Updates scroll position from DOM and saves variables to server.
     */
    private void updateScrollPosition() {
        int oldTop = scrollTop;
        int oldLeft = scrollLeft;
        scrollTop = DOM.getElementPropertyInt(getElement(), "scrollTop");
        scrollLeft = DOM.getElementPropertyInt(getElement(), "scrollLeft");
        if (connection != null && !rendering) {
            if (oldTop != scrollTop) {
                connection.updateVariable(id, "scrollTop", scrollTop, false);
            }
            if (oldLeft != scrollLeft) {
                connection.updateVariable(id, "scrollLeft", scrollLeft, false);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.gwt.event.logical.shared.ResizeHandler#onResize(com.google
     * .gwt.event.logical.shared.ResizeEvent)
     */
    public void onResize(ResizeEvent event) {
        onResize();
    }

    /**
     * Called when a resize event is received.
     */
    private void onResize() {
        /*
         * IE (pre IE9 at least) will give us some false resize events due to
         * problems with scrollbars. Firefox 3 might also produce some extra
         * events. We postpone both the re-layouting and the server side event
         * for a while to deal with these issues.
         * 
         * We may also postpone these events to avoid slowness when resizing the
         * browser window. Constantly recalculating the layout causes the resize
         * operation to be really slow with complex layouts.
         */
        boolean lazy = resizeLazy || BrowserInfo.get().isIE8();

        if (lazy) {
            delayedResizeExecutor.trigger();
        } else {
            windowSizeMaybeChanged(Window.getClientWidth(),
                    Window.getClientHeight());
        }
    }

    /**
     * Send new dimensions to the server.
     */
    private void sendClientResized() {
        connection.updateVariable(id, "height", height, false);
        connection.updateVariable(id, "width", width, immediate);
    }

    public native static void goTo(String url)
    /*-{
       $wnd.location = url;
     }-*/;

    public void onWindowClosing(Window.ClosingEvent event) {
        // Change focus on this window in order to ensure that all state is
        // collected from textfields
        // TODO this is a naive hack, that only works with text fields and may
        // cause some odd issues. Should be replaced with a decent solution, see
        // also related BeforeShortcutActionListener interface. Same interface
        // might be usable here.
        VTextField.flushChangesFromFocusedTextField();
    }

    private final RenderSpace myRenderSpace = new RenderSpace() {
        private int excessHeight = -1;
        private int excessWidth = -1;

        @Override
        public int getHeight() {
            return getElement().getOffsetHeight() - getExcessHeight();
        }

        private int getExcessHeight() {
            if (excessHeight < 0) {
                detectExcessSize();
            }
            return excessHeight;
        }

        private void detectExcessSize() {
            // TODO define that iview cannot be themed and decorations should
            // get to parent element, then get rid of this expensive and error
            // prone function
            final String overflow = getElement().getStyle().getProperty(
                    "overflow");
            getElement().getStyle().setProperty("overflow", "hidden");
            if (BrowserInfo.get().isIE()
                    && getElement().getPropertyInt("clientWidth") == 0) {
                // can't detect possibly themed border/padding width in some
                // situations (with some layout configurations), use empty div
                // to measure width properly
                DivElement div = Document.get().createDivElement();
                div.setInnerHTML("&nbsp;");
                div.getStyle().setProperty("overflow", "hidden");
                div.getStyle().setProperty("height", "1px");
                getElement().appendChild(div);
                excessWidth = getElement().getOffsetWidth()
                        - div.getOffsetWidth();
                getElement().removeChild(div);
            } else {
                excessWidth = getElement().getOffsetWidth()
                        - getElement().getPropertyInt("clientWidth");
            }
            excessHeight = getElement().getOffsetHeight()
                    - getElement().getPropertyInt("clientHeight");

            getElement().getStyle().setProperty("overflow", overflow);
        }

        @Override
        public int getWidth() {
            if (connection.getConfiguration().isStandalone()) {
                return getElement().getOffsetWidth() - getExcessWidth();
            }

            // If not running standalone, there might be multiple Vaadin apps
            // that won't shrink with the browser window as the components have
            // calculated widths (#3125)

            // Find all Vaadin applications on the page
            ArrayList<String> vaadinApps = new ArrayList<String>();
            loadAppIdListFromDOM(vaadinApps);

            // Store original styles here so they can be restored
            ArrayList<String> originalDisplays = new ArrayList<String>(
                    vaadinApps.size());

            String ownAppId = connection.getConfiguration().getRootPanelId();

            // Set display: none for all Vaadin apps
            for (int i = 0; i < vaadinApps.size(); i++) {
                String appId = vaadinApps.get(i);
                Element targetElement;
                if (appId.equals(ownAppId)) {
                    // Only hide the contents of current application
                    targetElement = ((Widget) layout).getElement();
                } else {
                    // Hide everything for other applications
                    targetElement = Document.get().getElementById(appId);
                }
                Style layoutStyle = targetElement.getStyle();

                originalDisplays.add(i, layoutStyle.getDisplay());
                layoutStyle.setDisplay(Display.NONE);
            }

            int w = getElement().getOffsetWidth() - getExcessWidth();

            // Then restore the old display style before returning
            for (int i = 0; i < vaadinApps.size(); i++) {
                String appId = vaadinApps.get(i);
                Element targetElement;
                if (appId.equals(ownAppId)) {
                    targetElement = ((Widget) layout).getElement();
                } else {
                    targetElement = Document.get().getElementById(appId);
                }
                Style layoutStyle = targetElement.getStyle();
                String originalDisplay = originalDisplays.get(i);

                if (originalDisplay.length() == 0) {
                    layoutStyle.clearDisplay();
                } else {
                    layoutStyle.setProperty("display", originalDisplay);
                }
            }

            return w;
        }

        private int getExcessWidth() {
            if (excessWidth < 0) {
                detectExcessSize();
            }
            return excessWidth;
        }

        @Override
        public int getScrollbarSize() {
            return Util.getNativeScrollbarSize();
        }
    };

    private native static void loadAppIdListFromDOM(ArrayList<String> list)
    /*-{
         var j;
         for(j in $wnd.vaadin.vaadinConfigurations) {
            list.@java.util.Collection::add(Ljava/lang/Object;)(j);
         }
     }-*/;

    public RenderSpace getAllocatedSpace(Widget child) {
        return myRenderSpace;
    }

    public boolean hasChildComponent(Widget component) {
        return (component != null && component == layout);
    }

    public void replaceChildComponent(Widget oldComponent, Widget newComponent) {
        // TODO This is untested as no layouts require this
        if (oldComponent != layout) {
            return;
        }

        setWidget(newComponent);
        layout = (Paintable) newComponent;
    }

    public boolean requestLayout(Set<Paintable> child) {
        /*
         * Can never propagate further and we do not want need to re-layout the
         * layout which has caused this request.
         */
        updateParentFrameSize();

        // layout size change may affect its available space (scrollbars)
        connection.handleComponentRelativeSize((Widget) layout);

        return true;

    }

    private void updateParentFrameSize() {
        if (parentFrame == null) {
            return;
        }

        int childHeight = Util.getRequiredHeight(getWidget().getElement());
        int childWidth = Util.getRequiredWidth(getWidget().getElement());

        parentFrame.getStyle().setPropertyPx("width", childWidth);
        parentFrame.getStyle().setPropertyPx("height", childHeight);
    }

    private static native Element getParentFrame()
    /*-{
        try {
            var frameElement = $wnd.frameElement;
            if (frameElement == null) {
                return null;
            }
            if (frameElement.getAttribute("autoResize") == "true") {
                return frameElement;
            }
        } catch (e) {
        }
        return null;
    }-*/;

    public void updateCaption(Paintable component, UIDL uidl) {
        // NOP Subwindows never draw caption for their first child (layout)
    }

    /**
     * Return an iterator for current subwindows. This method is meant for
     * testing purposes only.
     * 
     * @return
     */
    public ArrayList<VWindow> getSubWindowList() {
        ArrayList<VWindow> windows = new ArrayList<VWindow>(subWindows.size());
        for (VWindow widget : subWindows) {
            windows.add(widget);
        }
        return windows;
    }

    public void init(String rootPanelId,
            ApplicationConnection applicationConnection) {
        DOM.sinkEvents(getElement(), Event.ONKEYDOWN | Event.ONSCROLL);

        // iview is focused when created so element needs tabIndex
        // 1 due 0 is at the end of natural tabbing order
        DOM.setElementProperty(getElement(), "tabIndex", "1");

        RootPanel root = RootPanel.get(rootPanelId);

        // Remove the v-app-loading or any splash screen added inside the div by
        // the user
        root.getElement().setInnerHTML("");
        // For backwards compatibility with static index pages only.
        // No longer added by AbstractApplicationServlet/Portlet
        root.removeStyleName("v-app-loading");

        root.add(this);

        if (applicationConnection.getConfiguration().isStandalone()) {
            // set focus to iview element by default to listen possible keyboard
            // shortcuts. For embedded applications this is unacceptable as we
            // don't want to steal focus from the main page nor we don't want
            // side-effects from focusing (scrollIntoView).
            getElement().focus();
        }

        parentFrame = getParentFrame();
    }

    public ShortcutActionHandler getShortcutActionHandler() {
        return actionHandler;
    }

    public void focus() {
        getElement().focus();
    }

}
