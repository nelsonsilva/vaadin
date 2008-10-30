/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.itmill.toolkit.terminal.gwt.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.itmill.toolkit.terminal.gwt.client.RenderInformation.FloatSize;

public class Util {

    /**
     * Helper method for debugging purposes.
     * 
     * Stops execution on firefox browsers on a breakpoint.
     * 
     */
    public static native void browserDebugger()
    /*-{
        if($wnd.console)
            debugger;
    }-*/;

    /**
     * Called when the size of one or more widgets have changed during
     * rendering. Finds parent container and notifies them of the size change.
     * 
     * @param widgets
     */
    public static void componentSizeUpdated(Set<Widget> widgets) {
        if (widgets.isEmpty()) {
            return;
        }

        Map<Container, Set<Paintable>> childWidgets = new HashMap<Container, Set<Paintable>>();

        for (Widget widget : widgets) {
            // ApplicationConnection.getConsole().log(
            // "Widget " + Util.getSimpleName(widget) + " size updated");
            Widget parent = widget.getParent();
            while (parent != null && !(parent instanceof Container)) {
                parent = parent.getParent();
            }
            if (parent != null) {
                Set<Paintable> set = childWidgets.get(parent);
                if (set == null) {
                    set = new HashSet<Paintable>();
                    childWidgets.put((Container) parent, set);
                }
                set.add((Paintable) widget);
            }
        }

        Set<Widget> parentChanges = new HashSet<Widget>();
        for (Container parent : childWidgets.keySet()) {
            if (!parent.requestLayout(childWidgets.get(parent))) {
                parentChanges.add((Widget) parent);
            }
        }

        componentSizeUpdated(parentChanges);
    }

    public static float parseRelativeSize(String size) {
        if (size == null || !size.endsWith("%")) {
            return -1;
        }

        try {
            return Float.parseFloat(size.substring(0, size.length() - 1));
        } catch (Exception e) {
            ClientExceptionHandler.displayError(
                    "Unable to parse relative size", e);
        }

        return -1;
    }

    /**
     * Returns closest parent Widget in hierarchy that implements Container
     * interface
     * 
     * @param component
     * @return closest parent Container
     */
    public static Container getLayout(Widget component) {
        Widget parent = component.getParent();
        while (parent != null && !(parent instanceof Container)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            assert ((Container) parent).hasChildComponent(component);

            return (Container) parent;
        }
        return null;
    }

    /**
     * Detects if current browser is IE.
     * 
     * @deprecated use BrowserInfo class instead
     * 
     * @return true if IE
     */
    public static boolean isIE() {
        return BrowserInfo.get().isIE();
    }

    /**
     * Detects if current browser is IE6.
     * 
     * @deprecated use BrowserInfo class instead
     * 
     * @return true if IE6
     */
    public static boolean isIE6() {
        return BrowserInfo.get().isIE6();
    }

    /**
     * @deprecated use BrowserInfo class instead
     * @return
     */
    public static boolean isIE7() {
        return BrowserInfo.get().isIE7();
    }

    /**
     * @deprecated use BrowserInfo class instead
     * @return
     */
    public static boolean isFF2() {
        return BrowserInfo.get().isFF2();
    }

    private static final Element escapeHtmlHelper = DOM.createDiv();

    /**
     * Converts html entities to text.
     * 
     * @param html
     * @return escaped string presentation of given html
     */
    public static String escapeHTML(String html) {
        DOM.setInnerText(escapeHtmlHelper, html);
        return DOM.getInnerHTML(escapeHtmlHelper);
    }

    /**
     * Adds transparent PNG fix to image element; only use for IE6.
     * 
     * @param el
     *            IMG element
     * @param blankImageUrl
     *            URL to transparent one-pixel gif
     */
    public native static void addPngFix(Element el, String blankImageUrl)
    /*-{
        el.attachEvent("onload", function() {
            var src = el.src;
            if (src.indexOf(".png")<1) return;
            var w = el.width||16; 
            var h = el.height||16;
            el.src =blankImageUrl;
            el.style.height = h+"px";
            el.style.width = w+"px";
            el.style.padding = "0px";
            el.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+src+"', sizingMethod='crop')";  
        },false);
    }-*/;

    /**
     * Clones given element as in JavaScript.
     * 
     * Deprecate this if there appears similar method into GWT someday.
     * 
     * @param element
     * @param deep
     *            clone child tree also
     * @return
     */
    public static native Element cloneNode(Element element, boolean deep)
    /*-{
        return element.cloneNode(deep);
    }-*/;

    public static int measureHorizontalPadding(Element element, int paddingGuess) {
        String originalWidth = DOM.getStyleAttribute(element, "width");
        int originalOffsetWidth = element.getOffsetWidth();
        int widthGuess = (originalOffsetWidth - paddingGuess);
        if (widthGuess < 1) {
            widthGuess = 1;
        }
        DOM.setStyleAttribute(element, "width", widthGuess + "px");
        int padding = element.getOffsetWidth() - widthGuess;

        DOM.setStyleAttribute(element, "width", originalWidth);
        return padding;
    }

    public static int measureHorizontalBorder(Element element) {
        int borders;
        if (BrowserInfo.get().isIE()) {
            String width = element.getStyle().getProperty("width");
            String height = element.getStyle().getProperty("height");

            int offsetWidth = element.getOffsetWidth();
            int offsetHeight = element.getOffsetHeight();
            if (BrowserInfo.get().isIE6()) {
                if (offsetHeight < 1) {
                    offsetHeight = 1;
                }
                if (offsetWidth < 1) {
                    offsetWidth = 10;
                }
                element.getStyle().setPropertyPx("height", offsetHeight);
            }
            element.getStyle().setPropertyPx("width", offsetWidth);

            borders = element.getOffsetWidth()
                    - element.getPropertyInt("clientWidth");

            element.getStyle().setProperty("width", width);
            if (BrowserInfo.get().isIE6()) {
                element.getStyle().setProperty("height", height);
            }
        } else {
            borders = element.getOffsetWidth()
                    - element.getPropertyInt("clientWidth");
        }
        assert borders >= 0;

        return borders;
    }

    public static int measureVerticalBorder(Element element) {
        int borders = element.getOffsetHeight()
                - element.getPropertyInt("clientHeight");
        assert borders >= 0;
        return borders;
    }

    public static int measureMarginLeft(Element element) {
        return element.getAbsoluteLeft()
                - element.getParentElement().getAbsoluteLeft();
    }

    public static void setWidthExcludingPadding(Element element,
            int requestedWidth, int paddingGuess) {

        int widthGuess = requestedWidth - paddingGuess;
        if (widthGuess < 0) {
            widthGuess = 0;
        }

        DOM.setStyleAttribute(element, "width", widthGuess + "px");
        int captionOffsetWidth = DOM.getElementPropertyInt(element,
                "offsetWidth");

        int actualPadding = captionOffsetWidth - widthGuess;

        if (actualPadding != paddingGuess) {
            DOM.setStyleAttribute(element, "width", requestedWidth
                    - actualPadding + "px");

        }

    }

    public static String getSimpleName(Widget widget) {
        if (widget == null) {
            return "(null)";
        }

        String name = widget.getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public static void setFloat(Element element, String value) {
        if (BrowserInfo.get().isIE()) {
            DOM.setStyleAttribute(element, "styleFloat", value);
        } else {
            DOM.setStyleAttribute(element, "cssFloat", value);
        }
    }

    private static int detectedScrollbarSize = -1;

    public static int getNativeScrollbarSize() {
        if (detectedScrollbarSize < 0) {
            Element scroller = DOM.createDiv();
            scroller.getStyle().setProperty("width", "50px");
            scroller.getStyle().setProperty("height", "50px");
            scroller.getStyle().setProperty("overflow", "scroll");
            scroller.getStyle().setProperty("position", "absolute");
            scroller.getStyle().setProperty("marginLeft", "-5000px");
            RootPanel.getBodyElement().appendChild(scroller);
            detectedScrollbarSize = scroller.getOffsetWidth()
                    - scroller.getPropertyInt("clientWidth");
            assert detectedScrollbarSize != 0;
            RootPanel.getBodyElement().removeChild(scroller);

        }
        return detectedScrollbarSize;
    }

    /**
     * Run workaround for webkits overflow auto issue.
     * 
     * See: our buh #2138 and https://bugs.webkit.org/show_bug.cgi?id=21462
     * 
     * @param elem
     *            with overflow auto
     */
    public static void runWebkitOverflowAutoFix(final Element elem) {
        // add max version if fix landes sometime to webkit
        if (BrowserInfo.get().getWebkitVersion() > 0) {
            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    // Dough, safari scoll auto means actually just a moped
                    elem.getStyle().setProperty("overflow", "hidden");
                    (new Timer() {
                        @Override
                        public void run() {
                            elem.getStyle().setProperty("overflow", "auto");
                        }
                    }).schedule(1);
                }
            });
        }

    }

    /**
     * Parses the UIDL parameter and fetches the relative size of the component.
     * If a dimension is not specified as relative it will return -1. If the
     * UIDL does not contain width or height specifications this will return
     * null.
     * 
     * @param uidl
     * @return
     */
    public static FloatSize parseRelativeSize(UIDL uidl) {
        boolean hasAttribute = false;
        String w = "";
        String h = "";
        if (uidl.hasAttribute("width")) {
            hasAttribute = true;
            w = uidl.getStringAttribute("width");
        }
        if (uidl.hasAttribute("height")) {
            hasAttribute = true;
            h = uidl.getStringAttribute("height");
        }

        if (!hasAttribute) {
            return null;
        }

        float relativeWidth = Util.parseRelativeSize(w);
        float relativeHeight = Util.parseRelativeSize(h);

        FloatSize relativeSize = new FloatSize(relativeWidth, relativeHeight);
        return relativeSize;

    }

    public static boolean isCached(UIDL uidl) {
        return uidl.getBooleanAttribute("cached");
    }
}
