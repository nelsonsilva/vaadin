/*
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.ObjectElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.DomEvent.Type;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.VConsole;
import com.vaadin.terminal.gwt.client.VTooltip;

public class VEmbedded extends HTML implements Paintable {
    public static final String CLICK_EVENT_IDENTIFIER = "click";

    private static String CLASSNAME = "v-embedded";

    private String height;
    private String width;
    private Element browserElement;

    private String type;

    private ApplicationConnection client;

    private final ClickEventHandler clickEventHandler = new ClickEventHandler(
            this, CLICK_EVENT_IDENTIFIER) {

        @Override
        protected <H extends EventHandler> HandlerRegistration registerHandler(
                H handler, Type<H> type) {
            return addDomHandler(handler, type);
        }

    };

    public VEmbedded() {
        setStyleName(CLASSNAME);
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        if (client.updateComponent(this, uidl, true)) {
            return;
        }
        this.client = client;

        boolean clearBrowserElement = true;

        clickEventHandler.handleEventHandlerRegistration(client);

        if (uidl.hasAttribute("type")) {
            type = uidl.getStringAttribute("type");
            if (type.equals("image")) {
                addStyleName(CLASSNAME + "-image");
                Element el = null;
                boolean created = false;
                NodeList<Node> nodes = getElement().getChildNodes();
                if (nodes != null && nodes.getLength() == 1) {
                    Node n = nodes.getItem(0);
                    if (n.getNodeType() == Node.ELEMENT_NODE) {
                        Element e = (Element) n;
                        if (e.getTagName().equals("IMG")) {
                            el = e;
                        }
                    }
                }
                if (el == null) {
                    setHTML("");
                    el = DOM.createImg();
                    created = true;
                    DOM.sinkEvents(el, Event.ONLOAD);
                }

                // Set attributes
                Style style = el.getStyle();
                String w = uidl.getStringAttribute("width");
                if (w != null) {
                    style.setProperty("width", w);
                } else {
                    style.setProperty("width", "");
                }
                String h = uidl.getStringAttribute("height");
                if (h != null) {
                    style.setProperty("height", h);
                } else {
                    style.setProperty("height", "");
                }
                DOM.setElementProperty(el, "src", getSrc(uidl, client));

                if (created) {
                    // insert in dom late
                    getElement().appendChild(el);
                }

                /*
                 * Sink tooltip events so tooltip is displayed when hovering the
                 * image.
                 */
                sinkEvents(VTooltip.TOOLTIP_EVENTS);

            } else if (type.equals("browser")) {
                addStyleName(CLASSNAME + "-browser");
                if (browserElement == null) {
                    setHTML("<iframe width=\"100%\" height=\"100%\" frameborder=\"0\""
                            + " allowTransparency=\"true\" src=\"\""
                            + " name=\"" + uidl.getId() + "\"></iframe>");
                    browserElement = DOM.getFirstChild(getElement());
                }
                DOM.setElementAttribute(browserElement, "src",
                        getSrc(uidl, client));
                clearBrowserElement = false;
            } else {
                VConsole.log("Unknown Embedded type '" + type + "'");
            }
        } else if (uidl.hasAttribute("mimetype")) {
            final String mime = uidl.getStringAttribute("mimetype");
            if (mime.equals("application/x-shockwave-flash")) {
                // Handle embedding of Flash
                addStyleName(CLASSNAME + "-flash");
                setHTML(createFlashEmbed(uidl));

            } else if (mime.equals("image/svg+xml")) {
                addStyleName(CLASSNAME + "-svg");
                String data;
                Map<String, String> parameters = getParameters(uidl);
                if (parameters.get("data") == null) {
                    data = getSrc(uidl, client);
                } else {
                    data = "data:image/svg+xml," + parameters.get("data");
                }
                setHTML("");
                ObjectElement obj = Document.get().createObjectElement();
                obj.setType(mime);
                obj.setData(data);
                if (width != null) {
                    obj.getStyle().setProperty("width", "100%");
                }
                if (height != null) {
                    obj.getStyle().setProperty("height", "100%");
                }
                if (uidl.hasAttribute("classid")) {
                    obj.setAttribute("classid",
                            uidl.getStringAttribute("classid"));
                }
                if (uidl.hasAttribute("codebase")) {
                    obj.setAttribute("codebase",
                            uidl.getStringAttribute("codebase"));
                }
                if (uidl.hasAttribute("codetype")) {
                    obj.setAttribute("codetype",
                            uidl.getStringAttribute("codetype"));
                }
                if (uidl.hasAttribute("archive")) {
                    obj.setAttribute("archive",
                            uidl.getStringAttribute("archive"));
                }
                if (uidl.hasAttribute("standby")) {
                    obj.setAttribute("standby",
                            uidl.getStringAttribute("standby"));
                }
                getElement().appendChild(obj);

            } else {
                VConsole.log("Unknown Embedded mimetype '" + mime + "'");
            }
        } else {
            VConsole.log("Unknown Embedded; no type or mimetype attribute");
        }

        if (clearBrowserElement) {
            browserElement = null;
        }
    }

    /**
     * Creates the Object and Embed tags for the Flash plugin so it works
     * cross-browser
     * 
     * @param uidl
     *            The UIDL
     * @return Tags concatenated into a string
     */
    private String createFlashEmbed(UIDL uidl) {
        /*
         * To ensure cross-browser compatibility we are using the twice-cooked
         * method to embed flash i.e. we add a OBJECT tag for IE ActiveX and
         * inside it a EMBED for all other browsers.
         */

        StringBuilder html = new StringBuilder();

        // Start the object tag
        html.append("<object ");

        /*
         * Add classid required for ActiveX to recognize the flash. This is a
         * predefined value which ActiveX recognizes and must be the given
         * value. More info can be found on
         * http://kb2.adobe.com/cps/415/tn_4150.html. Allow user to override
         * this by setting his own classid.
         */
        if (uidl.hasAttribute("classid")) {
            html.append("classid=\""
                    + Util.escapeAttribute(uidl.getStringAttribute("classid"))
                    + "\" ");
        } else {
            html.append("classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" ");
        }

        /*
         * Add codebase required for ActiveX and must be exactly this according
         * to http://kb2.adobe.com/cps/415/tn_4150.html to work with the above
         * given classid. Again, see more info on
         * http://kb2.adobe.com/cps/415/tn_4150.html. Limiting Flash version to
         * 6.0.0.0 and above. Allow user to override this by setting his own
         * codebase
         */
        if (uidl.hasAttribute("codebase")) {
            html.append("codebase=\""
                    + Util.escapeAttribute(uidl.getStringAttribute("codebase"))
                    + "\" ");
        } else {
            html.append("codebase=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0\" ");
        }

        // Add width and height
        html.append("width=\"" + Util.escapeAttribute(width) + "\" ");
        html.append("height=\"" + Util.escapeAttribute(height) + "\" ");
        html.append("type=\"application/x-shockwave-flash\" ");

        // Codetype
        if (uidl.hasAttribute("codetype")) {
            html.append("codetype=\""
                    + Util.escapeAttribute(uidl.getStringAttribute("codetype"))
                    + "\" ");
        }

        // Standby
        if (uidl.hasAttribute("standby")) {
            html.append("standby=\""
                    + Util.escapeAttribute(uidl.getStringAttribute("standby"))
                    + "\" ");
        }

        // Archive
        if (uidl.hasAttribute("archive")) {
            html.append("archive=\""
                    + Util.escapeAttribute(uidl.getStringAttribute("archive"))
                    + "\" ");
        }

        // End object tag
        html.append(">");

        // Ensure we have an movie parameter
        Map<String, String> parameters = getParameters(uidl);
        if (parameters.get("movie") == null) {
            parameters.put("movie", getSrc(uidl, client));
        }

        // Add parameters to OBJECT
        for (String name : parameters.keySet()) {
            html.append("<param ");
            html.append("name=\"" + Util.escapeAttribute(name) + "\" ");
            html.append("value=\"" + Util.escapeAttribute(parameters.get(name))
                    + "\" ");
            html.append("/>");
        }

        // Build inner EMBED tag
        html.append("<embed ");
        html.append("src=\"" + Util.escapeAttribute(getSrc(uidl, client))
                + "\" ");
        html.append("width=\"" + Util.escapeAttribute(width) + "\" ");
        html.append("height=\"" + Util.escapeAttribute(height) + "\" ");
        html.append("type=\"application/x-shockwave-flash\" ");

        // Add the parameters to the Embed
        for (String name : parameters.keySet()) {
            html.append(Util.escapeAttribute(name));
            html.append("=");
            html.append("\"" + Util.escapeAttribute(parameters.get(name))
                    + "\"");
        }

        // End embed tag
        html.append("></embed>");

        // End object tag
        html.append("</object>");

        return html.toString();
    }

    /**
     * Returns a map (name -> value) of all parameters in the UIDL.
     * 
     * @param uidl
     * @return
     */
    private static Map<String, String> getParameters(UIDL uidl) {
        Map<String, String> parameters = new HashMap<String, String>();

        Iterator<Object> childIterator = uidl.getChildIterator();
        while (childIterator.hasNext()) {

            Object child = childIterator.next();
            if (child instanceof UIDL) {

                UIDL childUIDL = (UIDL) child;
                if (childUIDL.getTag().equals("embeddedparam")) {
                    String name = childUIDL.getStringAttribute("name");
                    String value = childUIDL.getStringAttribute("value");
                    parameters.put(name, value);
                }
            }

        }

        return parameters;
    }

    /**
     * Helper to return translated src-attribute from embedded's UIDL
     * 
     * @param uidl
     * @param client
     * @return
     */
    private String getSrc(UIDL uidl, ApplicationConnection client) {
        String url = client.translateVaadinUri(uidl.getStringAttribute("src"));
        if (url == null) {
            return "";
        }
        return url;
    }

    @Override
    public void setWidth(String width) {
        this.width = width;
        if (isDynamicHeight()) {
            int oldHeight = getOffsetHeight();
            super.setWidth(width);
            int newHeight = getOffsetHeight();
            /*
             * Must notify parent if the height changes as a result of a width
             * change
             */
            if (oldHeight != newHeight) {
                Util.notifyParentOfSizeChange(this, false);
            }
        } else {
            super.setWidth(width);
        }

    }

    private boolean isDynamicWidth() {
        return width == null || width.equals("");
    }

    private boolean isDynamicHeight() {
        return height == null || height.equals("");
    }

    @Override
    public void setHeight(String height) {
        this.height = height;
        super.setHeight(height);
    }

    @Override
    protected void onDetach() {
        if (BrowserInfo.get().isIE()) {
            // Force browser to fire unload event when component is detached
            // from the view (IE doesn't do this automatically)
            if (browserElement != null) {
                DOM.setElementAttribute(browserElement, "src",
                        "javascript:false");
            }
        }
        super.onDetach();
    }

    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);
        if (DOM.eventGetType(event) == Event.ONLOAD) {
            if ("image".equals(type)) {
                updateElementDynamicSizeFromImage();
            }
            Util.notifyParentOfSizeChange(this, true);
        }

        client.handleTooltipEvent(event, this);
    }

    /**
     * Updates the size of the embedded component's element if size is
     * undefined. Without this embeddeds containing images will remain the wrong
     * size in certain cases (e.g. #6304).
     */
    private void updateElementDynamicSizeFromImage() {
        if (isDynamicWidth()) {
            getElement().getStyle().setWidth(
                    getElement().getFirstChildElement().getOffsetWidth(),
                    Unit.PX);
        }
        if (isDynamicHeight()) {
            getElement().getStyle().setHeight(
                    getElement().getFirstChildElement().getOffsetHeight(),
                    Unit.PX);
        }
    }

}
