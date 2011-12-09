/*
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.ui;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Iterator;

import com.vaadin.data.Property;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.client.ui.VCustomComponent;

/**
 * A {@link Field} whose UI content can be constructed by the user, enabling the
 * creation of e.g. form fields by composing Vaadin components. Customization of
 * both the visual presentation and the logic of the field is possible.
 * 
 * Subclasses must implement {@link #getType()} and {@link #createContent()}.
 * 
 * Most custom fields can simply compose a user interface that calls the methods
 * {@link #setInternalValue(Object)} and {@link #getInternalValue()} when
 * necessary.
 * 
 * It is also possible to override {@link #commit()},
 * {@link #setPropertyDataSource(Property)} and other logic of the field.
 * 
 * @since 7.0
 */
@ClientWidget(VCustomComponent.class)
public abstract class CustomField<T> extends AbstractField<T> implements
        ComponentContainer {

    /**
     * The root component implementing the custom component.
     */
    private Component root = null;

    /**
     * Constructs a new custom field.
     * 
     * <p>
     * The component is implemented by wrapping the methods of the composition
     * root component given as parameter. The composition root must be set
     * before the component can be used.
     * </p>
     */
    public CustomField() {
        // expand horizontally by default
        setWidth(100, UNITS_PERCENTAGE);
    }

    /**
     * Constructs the content and notifies it that the {@link CustomField} is
     * attached to a window.
     * 
     * @see com.vaadin.ui.Component#attach()
     */
    @Override
    public void attach() {
        root = getContent();
        super.attach();
        getContent().setParent(this);
        getContent().attach();

        fireComponentAttachEvent(getContent());
    }

    /**
     * Notifies the content that the {@link CustomField} is detached from a
     * window.
     * 
     * @see com.vaadin.ui.Component#detach()
     */
    @Override
    public void detach() {
        super.detach();
        getContent().detach();
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        if (getContent() == null) {
            throw new IllegalStateException(
                    "Content component or layout of the field must be set before the "
                            + getClass().getName() + " can be painted");
        }

        getContent().paint(target);
    }

    /**
     * Returns the content of the
     * 
     * @return
     */
    protected Component getContent() {
        if (null == root) {
            root = createContent();
        }
        return root;
    }

    /**
     * Create the content component or layout for the field. Subclasses of
     * {@link CustomField} should implement this method.
     * 
     * Note that this method is called when the CustomField is attached to a
     * layout or when {@link #getContent()} is called explicitly for the first
     * time. It is only called once for a {@link CustomField}.
     * 
     * @return
     */
    protected abstract Component createContent();

    private void requestContentRepaint() {
        if (getParent() == null) {
            // skip repaint - not yet attached
            return;
        }
        if (getContent() instanceof ComponentContainer) {
            ((ComponentContainer) getContent()).requestRepaintAll();
        } else {
            getContent().requestRepaint();
        }
    }

    // Size related methods
    // TODO might not be necessary to override but following the pattern from
    // AbstractComponentContainer

    @Override
    public void setHeight(float height, int unit) {
        super.setHeight(height, unit);
        requestContentRepaint();
    }

    @Override
    public void setWidth(float height, int unit) {
        super.setWidth(height, unit);
        requestContentRepaint();
    }

    // ComponentContainer methods

    private class ComponentIterator implements Iterator<Component>,
            Serializable {
        boolean first = getContent() != null;

        public boolean hasNext() {
            return first;
        }

        public Component next() {
            first = false;
            return getContent();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public Iterator<Component> getComponentIterator() {
        return new ComponentIterator();
    }

    public int getComponentCount() {
        return (null != getContent()) ? 1 : 0;
    }

    public void requestRepaintAll() {
        requestRepaint();

        requestContentRepaint();
    }

    /**
     * Fires the component attached event. This should be called by the
     * addComponent methods after the component have been added to this
     * container.
     * 
     * @param component
     *            the component that has been added to this container.
     */
    protected void fireComponentAttachEvent(Component component) {
        fireEvent(new ComponentAttachEvent(this, component));
    }

    // TODO remove these methods when ComponentContainer interface is cleaned up

    public void addComponent(Component c) {
        throw new UnsupportedOperationException();
    }

    public void removeComponent(Component c) {
        throw new UnsupportedOperationException();
    }

    public void removeAllComponents() {
        throw new UnsupportedOperationException();
    }

    public void replaceComponent(Component oldComponent, Component newComponent) {
        throw new UnsupportedOperationException();
    }

    public void moveComponentsFrom(ComponentContainer source) {
        throw new UnsupportedOperationException();
    }

    private static final Method COMPONENT_ATTACHED_METHOD;

    static {
        try {
            COMPONENT_ATTACHED_METHOD = ComponentAttachListener.class
                    .getDeclaredMethod("componentAttachedToContainer",
                            new Class[] { ComponentAttachEvent.class });
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException(
                    "Internal error finding methods in CustomField");
        }
    }

    public void addListener(ComponentAttachListener listener) {
        addListener(ComponentContainer.ComponentAttachEvent.class, listener,
                COMPONENT_ATTACHED_METHOD);
    }

    public void removeListener(ComponentAttachListener listener) {
        removeListener(ComponentContainer.ComponentAttachEvent.class, listener,
                COMPONENT_ATTACHED_METHOD);
    }

    public void addListener(ComponentDetachListener listener) {
        // content never detached
    }

    public void removeListener(ComponentDetachListener listener) {
        // content never detached
    }

}
