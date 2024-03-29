/* 
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal;

import java.io.Serializable;
import java.util.Map;

import com.vaadin.terminal.StreamVariable.StreamingStartEvent;
import com.vaadin.terminal.gwt.client.ApplicationConnection;

/**
 * This interface defines the methods for painting XML to the UIDL stream.
 * 
 * @author Vaadin Ltd.
 * @version
 * @VERSION@
 * @since 3.0
 */
public interface PaintTarget extends Serializable {

    /**
     * Prints single XMLsection.
     * 
     * Prints full XML section. The section data is escaped from XML tags and
     * surrounded by XML start and end-tags.
     * 
     * @param sectionTagName
     *            the name of the tag.
     * @param sectionData
     *            the scetion data.
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addSection(String sectionTagName, String sectionData)
            throws PaintException;

    /**
     * Prints element start tag of a paintable section. Starts a paintable
     * section using the given tag. The PaintTarget may implement a caching
     * scheme, that checks the paintable has actually changed or can a cached
     * version be used instead. This method should call the startTag method.
     * <p>
     * If the Paintable is found in cache and this function returns true it may
     * omit the content and close the tag, in which case cached content should
     * be used.
     * </p>
     * 
     * @param paintable
     *            the paintable to start.
     * @param tag
     *            the name of the start tag.
     * @return <code>true</code> if paintable found in cache, <code>false</code>
     *         otherwise.
     * @throws PaintException
     *             if the paint operation failed.
     * @see #startTag(String)
     * @since 3.1
     */
    public boolean startTag(Paintable paintable, String tag)
            throws PaintException;

    /**
     * Paints a component reference as an attribute to current tag. This method
     * is meant to enable component interactions on client side. With reference
     * the client side component can communicate directly to other component.
     * 
     * Note! This was experimental api and got replaced by
     * {@link #addAttribute(String, Paintable)} and
     * {@link #addVariable(VariableOwner, String, Paintable)}.
     * 
     * @param paintable
     *            the Paintable to reference
     * @param referenceName
     * @throws PaintException
     * 
     * @since 5.2
     * @deprecated use {@link #addAttribute(String, Paintable)} or
     *             {@link #addVariable(VariableOwner, String, Paintable)}
     *             instead
     */
    @Deprecated
    public void paintReference(Paintable paintable, String referenceName)
            throws PaintException;

    /**
     * Prints element start tag.
     * 
     * <pre>
     * Todo:
     * Checking of input values
     * </pre>
     * 
     * @param tagName
     *            the name of the start tag.
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void startTag(String tagName) throws PaintException;

    /**
     * Prints element end tag.
     * 
     * If the parent tag is closed before every child tag is closed an
     * PaintException is raised.
     * 
     * @param tagName
     *            the name of the end tag.
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void endTag(String tagName) throws PaintException;

    /**
     * Adds a boolean attribute to component. Atributes must be added before any
     * content is written.
     * 
     * @param name
     *            the Attribute name.
     * @param value
     *            the Attribute value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addAttribute(String name, boolean value) throws PaintException;

    /**
     * Adds a integer attribute to component. Atributes must be added before any
     * content is written.
     * 
     * @param name
     *            the Attribute name.
     * @param value
     *            the Attribute value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addAttribute(String name, int value) throws PaintException;

    /**
     * Adds a resource attribute to component. Atributes must be added before
     * any content is written.
     * 
     * @param name
     *            the Attribute name
     * @param value
     *            the Attribute value
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addAttribute(String name, Resource value) throws PaintException;

    /**
     * Adds details about {@link StreamVariable} to the UIDL stream. Eg. in web
     * terminals Receivers are typically rendered for the client side as URLs,
     * where the client side implementation can do an http post request.
     * <p>
     * The urls in UIDL message may use Vaadin specific protocol. Before
     * actually using the urls on the client side, they should be passed via
     * {@link ApplicationConnection#translateVaadinUri(String)}.
     * <p>
     * Note that in current terminal implementation StreamVariables are cleaned
     * from the terminal only when:
     * <ul>
     * <li>a StreamVariable with same name replaces an old one
     * <li>the variable owner is no more attached
     * <li>the developer signals this by calling
     * {@link StreamingStartEvent#disposeStreamVariable()}
     * </ul>
     * Most commonly a component developer can just ignore this issue, but with
     * strict memory requirements and lots of StreamVariables implementations
     * that reserve a lot of memory this may be a critical issue.
     * 
     * @param owner
     *            the ReceiverOwner that can track the progress of streaming to
     *            the given StreamVariable
     * @param name
     *            an identifying name for the StreamVariable
     * @param value
     *            the StreamVariable to paint
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addVariable(VariableOwner owner, String name,
            StreamVariable value) throws PaintException;

    /**
     * Adds a long attribute to component. Atributes must be added before any
     * content is written.
     * 
     * @param name
     *            the Attribute name.
     * @param value
     *            the Attribute value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addAttribute(String name, long value) throws PaintException;

    /**
     * Adds a float attribute to component. Atributes must be added before any
     * content is written.
     * 
     * @param name
     *            the Attribute name.
     * @param value
     *            the Attribute value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addAttribute(String name, float value) throws PaintException;

    /**
     * Adds a double attribute to component. Atributes must be added before any
     * content is written.
     * 
     * @param name
     *            the Attribute name.
     * @param value
     *            the Attribute value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addAttribute(String name, double value) throws PaintException;

    /**
     * Adds a string attribute to component. Atributes must be added before any
     * content is written.
     * 
     * @param name
     *            the Boolean attribute name.
     * @param value
     *            the Boolean attribute value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addAttribute(String name, String value) throws PaintException;

    /**
     * TODO
     * 
     * @param name
     * @param value
     * @throws PaintException
     */
    public void addAttribute(String name, Map<?, ?> value)
            throws PaintException;

    /**
     * Adds a Paintable type attribute. On client side the value will be a
     * terminal specific reference to corresponding component on client side
     * implementation.
     * 
     * @param name
     *            the name of the attribute
     * @param value
     *            the Paintable to be referenced on client side
     * @throws PaintException
     */
    public void addAttribute(String name, Paintable value)
            throws PaintException;

    /**
     * Adds a string type variable.
     * 
     * @param owner
     *            the Listener for variable changes.
     * @param name
     *            the Variable name.
     * @param value
     *            the Variable initial value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addVariable(VariableOwner owner, String name, String value)
            throws PaintException;

    /**
     * Adds a int type variable.
     * 
     * @param owner
     *            the Listener for variable changes.
     * @param name
     *            the Variable name.
     * @param value
     *            the Variable initial value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addVariable(VariableOwner owner, String name, int value)
            throws PaintException;

    /**
     * Adds a long type variable.
     * 
     * @param owner
     *            the Listener for variable changes.
     * @param name
     *            the Variable name.
     * @param value
     *            the Variable initial value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addVariable(VariableOwner owner, String name, long value)
            throws PaintException;

    /**
     * Adds a float type variable.
     * 
     * @param owner
     *            the Listener for variable changes.
     * @param name
     *            the Variable name.
     * @param value
     *            the Variable initial value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addVariable(VariableOwner owner, String name, float value)
            throws PaintException;

    /**
     * Adds a double type variable.
     * 
     * @param owner
     *            the Listener for variable changes.
     * @param name
     *            the Variable name.
     * @param value
     *            the Variable initial value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addVariable(VariableOwner owner, String name, double value)
            throws PaintException;

    /**
     * Adds a boolean type variable.
     * 
     * @param owner
     *            the Listener for variable changes.
     * @param name
     *            the Variable name.
     * @param value
     *            the Variable initial value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addVariable(VariableOwner owner, String name, boolean value)
            throws PaintException;

    /**
     * Adds a string array type variable.
     * 
     * @param owner
     *            the Listener for variable changes.
     * @param name
     *            the Variable name.
     * @param value
     *            the Variable initial value.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addVariable(VariableOwner owner, String name, String[] value)
            throws PaintException;

    /**
     * Adds a Paintable type variable. On client side the variable value will be
     * a terminal specific reference to corresponding component on client side
     * implementation. When updated from client side, terminal will map the
     * client side component reference back to a corresponding server side
     * reference.
     * 
     * @param owner
     *            the Listener for variable changes
     * @param name
     *            the name of the variable
     * @param value
     *            the initial value of the variable
     * 
     * @throws PaintException
     *             if the paint oparation fails
     */
    public void addVariable(VariableOwner owner, String name, Paintable value)
            throws PaintException;

    /**
     * Adds a upload stream type variable.
     * 
     * @param owner
     *            the Listener for variable changes.
     * @param name
     *            the Variable name.
     * 
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addUploadStreamVariable(VariableOwner owner, String name)
            throws PaintException;

    /**
     * Prints single XML section.
     * <p>
     * Prints full XML section. The section data must be XML and it is
     * surrounded by XML start and end-tags.
     * </p>
     * 
     * @param sectionTagName
     *            the tag name.
     * @param sectionData
     *            the section data to be printed.
     * @param namespace
     *            the namespace.
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addXMLSection(String sectionTagName, String sectionData,
            String namespace) throws PaintException;

    /**
     * Adds UIDL directly. The UIDL must be valid in accordance with the
     * UIDL.dtd
     * 
     * @param uidl
     *            the UIDL to be added.
     * @throws PaintException
     *             if the paint operation failed.
     */
    public void addUIDL(java.lang.String uidl) throws PaintException;

    /**
     * Adds text node. All the contents of the text are XML-escaped.
     * 
     * @param text
     *            the Text to add
     * @throws PaintException
     *             if the paint operation failed.
     */
    void addText(String text) throws PaintException;

    /**
     * Adds CDATA node to target UIDL-tree.
     * 
     * @param text
     *            the Character data to add
     * @throws PaintException
     *             if the paint operation failed.
     * @since 3.1
     */
    void addCharacterData(String text) throws PaintException;

    public void addAttribute(String string, Object[] keys);

    /**
     * @return the "tag" string used in communication to present given
     *         {@link Paintable} type. Terminal may define how to present
     *         paintable.
     */
    public String getTag(Paintable paintable);

    /**
     * @return true if a full repaint has been requested. E.g. refresh in a
     *         browser window or such.
     */
    public boolean isFullRepaint();
}
