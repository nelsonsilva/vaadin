/*
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.event.MouseEvents.ClickListener;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.terminal.gwt.client.ui.VEmbedded;

/**
 * Component for embedding external objects.
 * 
 * @author Vaadin Ltd.
 * @version
 * @VERSION@
 * @since 3.0
 */
@SuppressWarnings("serial")
@ClientWidget(VEmbedded.class)
public class Embedded extends AbstractComponent {

    private static final String CLICK_EVENT = VEmbedded.CLICK_EVENT_IDENTIFIER;

    /**
     * General object type.
     */
    public static final int TYPE_OBJECT = 0;

    /**
     * Image types.
     */
    public static final int TYPE_IMAGE = 1;

    /**
     * Browser ("iframe") type.
     */
    public static final int TYPE_BROWSER = 2;

    /**
     * Type of the object.
     */
    private int type = TYPE_OBJECT;

    /**
     * Source of the embedded object.
     */
    private Resource source = null;

    /**
     * Generic object attributes.
     */
    private String mimeType = null;

    private String standby = null;

    /**
     * Hash of object parameters.
     */
    private final Map<String, String> parameters = new HashMap<String, String>();

    /**
     * Applet or other client side runnable properties.
     */
    private String codebase = null;

    private String codetype = null;

    private String classId = null;

    private String archive = null;

    /**
     * Creates a new empty Embedded object.
     */
    public Embedded() {
    }

    /**
     * Creates a new empty Embedded object with caption.
     * 
     * @param caption
     */
    public Embedded(String caption) {
        setCaption(caption);
    }

    /**
     * Creates a new Embedded object whose contents is loaded from given
     * resource. The dimensions are assumed if possible. The type is guessed
     * from resource.
     * 
     * @param caption
     * @param source
     *            the Source of the embedded object.
     */
    public Embedded(String caption, Resource source) {
        setCaption(caption);
        setSource(source);
    }

    /**
     * Invoked when the component state should be painted.
     */
    @Override
    public void paintContent(PaintTarget target) throws PaintException {

        switch (type) {
        case TYPE_IMAGE:
            target.addAttribute("type", "image");
            break;
        case TYPE_BROWSER:
            target.addAttribute("type", "browser");
            break;
        default:
            break;
        }

        if (getSource() != null) {
            target.addAttribute("src", getSource());
        }

        if (mimeType != null && !"".equals(mimeType)) {
            target.addAttribute("mimetype", mimeType);
        }
        if (classId != null && !"".equals(classId)) {
            target.addAttribute("classid", classId);
        }
        if (codebase != null && !"".equals(codebase)) {
            target.addAttribute("codebase", codebase);
        }
        if (codetype != null && !"".equals(codetype)) {
            target.addAttribute("codetype", codetype);
        }
        if (standby != null && !"".equals(standby)) {
            target.addAttribute("standby", standby);
        }
        if (archive != null && !"".equals(archive)) {
            target.addAttribute("archive", archive);
        }

        // Params
        for (final Iterator<String> i = getParameterNames(); i.hasNext();) {
            target.startTag("embeddedparam");
            final String key = i.next();
            target.addAttribute("name", key);
            target.addAttribute("value", getParameter(key));
            target.endTag("embeddedparam");
        }
    }

    /**
     * Sets an object parameter. Parameters are optional information, and they
     * are passed to the instantiated object. Parameters are are stored as name
     * value pairs. This overrides the previous value assigned to this
     * parameter.
     * 
     * @param name
     *            the name of the parameter.
     * @param value
     *            the value of the parameter.
     */
    public void setParameter(String name, String value) {
        parameters.put(name, value);
        requestRepaint();
    }

    /**
     * Gets the value of an object parameter. Parameters are optional
     * information, and they are passed to the instantiated object. Parameters
     * are are stored as name value pairs.
     * 
     * @return the Value of parameter or null if not found.
     */
    public String getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * Removes an object parameter from the list.
     * 
     * @param name
     *            the name of the parameter to remove.
     */
    public void removeParameter(String name) {
        parameters.remove(name);
        requestRepaint();
    }

    /**
     * Gets the embedded object parameter names.
     * 
     * @return the Iterator of parameters names.
     */
    public Iterator<String> getParameterNames() {
        return parameters.keySet().iterator();
    }

    /**
     * This attribute specifies the base path used to resolve relative URIs
     * specified by the classid, data, and archive attributes. When absent, its
     * default value is the base URI of the current document.
     * 
     * @return the code base.
     */
    public String getCodebase() {
        return codebase;
    }

    /**
     * Gets the MIME-Type of the code.
     * 
     * @return the MIME-Type of the code.
     */
    public String getCodetype() {
        return codetype;
    }

    /**
     * Gets the MIME-Type of the object.
     * 
     * @return the MIME-Type of the object.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * This attribute specifies a message that a user agent may render while
     * loading the object's implementation and data.
     * 
     * @return The text displayed when loading
     */
    public String getStandby() {
        return standby;
    }

    /**
     * This attribute specifies the base path used to resolve relative URIs
     * specified by the classid, data, and archive attributes. When absent, its
     * default value is the base URI of the current document.
     * 
     * @param codebase
     *            The base path
     */
    public void setCodebase(String codebase) {
        if (codebase != this.codebase
                || (codebase != null && !codebase.equals(this.codebase))) {
            this.codebase = codebase;
            requestRepaint();
        }
    }

    /**
     * This attribute specifies the content type of data expected when
     * downloading the object specified by classid. This attribute is optional
     * but recommended when classid is specified since it allows the user agent
     * to avoid loading information for unsupported content types. When absent,
     * it defaults to the value of the type attribute.
     * 
     * @param codetype
     *            the codetype to set.
     */
    public void setCodetype(String codetype) {
        if (codetype != this.codetype
                || (codetype != null && !codetype.equals(this.codetype))) {
            this.codetype = codetype;
            requestRepaint();
        }
    }

    /**
     * Sets the mimeType, the MIME-Type of the object.
     * 
     * @param mimeType
     *            the mimeType to set.
     */
    public void setMimeType(String mimeType) {
        if (mimeType != this.mimeType
                || (mimeType != null && !mimeType.equals(this.mimeType))) {
            this.mimeType = mimeType;
            if ("application/x-shockwave-flash".equals(mimeType)) {
                /*
                 * Automatically add wmode transparent as we use lots of
                 * floating layers in Vaadin. If developers need better flash
                 * performance, they can override this value programmatically
                 * back to "window" (the defautl).
                 */
                if (getParameter("wmode") == null) {
                    setParameter("wmode", "transparent");
                }
            }
            requestRepaint();
        }
    }

    /**
     * This attribute specifies a message that a user agent may render while
     * loading the object's implementation and data.
     * 
     * @param standby
     *            The text to display while loading
     */
    public void setStandby(String standby) {
        if (standby != this.standby
                || (standby != null && !standby.equals(this.standby))) {
            this.standby = standby;
            requestRepaint();
        }
    }

    /**
     * This attribute may be used to specify the location of an object's
     * implementation via a URI.
     * 
     * @return the classid.
     */
    public String getClassId() {
        return classId;
    }

    /**
     * This attribute may be used to specify the location of an object's
     * implementation via a URI.
     * 
     * @param classId
     *            the classId to set.
     */
    public void setClassId(String classId) {
        if (classId != this.classId
                || (classId != null && !classId.equals(this.classId))) {
            this.classId = classId;
            requestRepaint();
        }
    }

    /**
     * Gets the resource contained in the embedded object.
     * 
     * @return the Resource
     */
    public Resource getSource() {
        return source;
    }

    /**
     * Gets the type of the embedded object.
     * <p>
     * This can be one of the following:
     * <ul>
     * <li>TYPE_OBJECT <i>(This is the default)</i>
     * <li>TYPE_IMAGE
     * </ul>
     * </p>
     * 
     * @return the type.
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the object source resource. The dimensions are assumed if possible.
     * The type is guessed from resource.
     * 
     * @param source
     *            the source to set.
     */
    public void setSource(Resource source) {
        if (source != null && !source.equals(this.source)) {
            this.source = source;
            final String mt = source.getMIMEType();

            if (mimeType == null) {
                mimeType = mt;
            }

            if (mt.equals("image/svg+xml")) {
                type = TYPE_OBJECT;
            } else if ((mt.substring(0, mt.indexOf("/"))
                    .equalsIgnoreCase("image"))) {
                type = TYPE_IMAGE;
            } else {
                // Keep previous type
            }
            requestRepaint();
        }
    }

    /**
     * Sets the object type.
     * <p>
     * This can be one of the following:
     * <ul>
     * <li>TYPE_OBJECT <i>(This is the default)</i>
     * <li>TYPE_IMAGE
     * <li>TYPE_BROWSER
     * </ul>
     * </p>
     * 
     * @param type
     *            the type to set.
     */
    public void setType(int type) {
        if (type != TYPE_OBJECT && type != TYPE_IMAGE && type != TYPE_BROWSER) {
            throw new IllegalArgumentException("Unsupported type");
        }
        if (type != this.type) {
            this.type = type;
            requestRepaint();
        }
    }

    /**
     * This attribute may be used to specify a space-separated list of URIs for
     * archives containing resources relevant to the object, which may include
     * the resources specified by the classid and data attributes. Preloading
     * archives will generally result in reduced load times for objects.
     * Archives specified as relative URIs should be interpreted relative to the
     * codebase attribute.
     * 
     * @return Space-separated list of URIs with resources relevant to the
     *         object
     */
    public String getArchive() {
        return archive;
    }

    /**
     * This attribute may be used to specify a space-separated list of URIs for
     * archives containing resources relevant to the object, which may include
     * the resources specified by the classid and data attributes. Preloading
     * archives will generally result in reduced load times for objects.
     * Archives specified as relative URIs should be interpreted relative to the
     * codebase attribute.
     * 
     * @param archive
     *            Space-separated list of URIs with resources relevant to the
     *            object
     */
    public void setArchive(String archive) {
        if (archive != this.archive
                || (archive != null && !archive.equals(this.archive))) {
            this.archive = archive;
            requestRepaint();
        }
    }

    /**
     * Add a click listener to the component. The listener is called whenever
     * the user clicks inside the component. Depending on the content the event
     * may be blocked and in that case no event is fired.
     * 
     * Use {@link #removeListener(ClickListener)} to remove the listener.
     * 
     * @param listener
     *            The listener to add
     */
    public void addListener(ClickListener listener) {
        addListener(CLICK_EVENT, ClickEvent.class, listener,
                ClickListener.clickMethod);
    }

    /**
     * Remove a click listener from the component. The listener should earlier
     * have been added using {@link #addListener(ClickListener)}.
     * 
     * @param listener
     *            The listener to remove
     */
    public void removeListener(ClickListener listener) {
        removeListener(CLICK_EVENT, ClickEvent.class, listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.AbstractComponent#changeVariables(java.lang.Object,
     * java.util.Map)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        if (variables.containsKey(CLICK_EVENT)) {
            fireClick((Map<String, Object>) variables.get(CLICK_EVENT));
        }

    }

    /**
     * Notifies click-listeners that a mouse click event has occurred.
     * 
     * @param parameters
     */
    private void fireClick(Map<String, Object> parameters) {
        MouseEventDetails mouseDetails = MouseEventDetails
                .deSerialize((String) parameters.get("mouseDetails"));

        fireEvent(new ClickEvent(this, mouseDetails));
    }

}
