/* 
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.ui;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.client.ui.VProgressIndicator;

/**
 * <code>ProgressIndicator</code> is component that shows user state of a
 * process (like long computing or file upload)
 * 
 * <code>ProgressIndicator</code> has two mainmodes. One for indeterminate
 * processes and other (default) for processes which progress can be measured
 * 
 * May view an other property that indicates progress 0...1
 * 
 * @author Vaadin Ltd.
 * @version
 * @VERSION@
 * @since 4
 */
@SuppressWarnings("serial")
@ClientWidget(VProgressIndicator.class)
public class ProgressIndicator extends AbstractField<Number> implements
        Property.Viewer, Property.ValueChangeListener {

    /**
     * Content mode, where the label contains only plain text. The getValue()
     * result is coded to XML when painting.
     */
    public static final int CONTENT_TEXT = 0;

    /**
     * Content mode, where the label contains preformatted text.
     */
    public static final int CONTENT_PREFORMATTED = 1;

    private boolean indeterminate = false;

    private Property dataSource;

    private int pollingInterval = 1000;

    /**
     * Creates an a new ProgressIndicator.
     */
    public ProgressIndicator() {
        setPropertyDataSource(new ObjectProperty<Float>(new Float(0),
                Float.class));
    }

    /**
     * Creates a new instance of ProgressIndicator with given state.
     * 
     * @param value
     */
    public ProgressIndicator(Float value) {
        setPropertyDataSource(new ObjectProperty<Float>(value, Float.class));
    }

    /**
     * Creates a new instance of ProgressIndicator with stae read from given
     * datasource.
     * 
     * @param contentSource
     */
    public ProgressIndicator(Property contentSource) {
        setPropertyDataSource(contentSource);
    }

    /**
     * Sets the component to read-only. Readonly is not used in
     * ProgressIndicator.
     * 
     * @param readOnly
     *            True to enable read-only mode, False to disable it.
     */
    @Override
    public void setReadOnly(boolean readOnly) {
        if (dataSource == null) {
            throw new IllegalStateException("Datasource must be se");
        }
        dataSource.setReadOnly(readOnly);
    }

    /**
     * Is the component read-only ? Readonly is not used in ProgressIndicator -
     * this returns allways false.
     * 
     * @return True if the component is in read only mode.
     */
    @Override
    public boolean isReadOnly() {
        if (dataSource == null) {
            throw new IllegalStateException("Datasource must be se");
        }
        return dataSource.isReadOnly();
    }

    /**
     * Paints the content of this component.
     * 
     * @param target
     *            the Paint Event.
     * @throws PaintException
     *             if the Paint Operation fails.
     */
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        target.addAttribute("indeterminate", indeterminate);
        target.addAttribute("pollinginterval", pollingInterval);
        target.addAttribute("state", getValue().toString());
    }

    /**
     * Gets the value of the ProgressIndicator. Value of the ProgressIndicator
     * is Float between 0 and 1.
     * 
     * @return the Value of the ProgressIndicator.
     * @see com.vaadin.ui.AbstractField#getValue()
     */
    @Override
    public Number getValue() {
        if (dataSource == null) {
            throw new IllegalStateException("Datasource must be set");
        }
        // TODO conversions to eliminate cast
        return (Number) dataSource.getValue();
    }

    /**
     * Sets the value of the ProgressIndicator. Value of the ProgressIndicator
     * is the Float between 0 and 1.
     * 
     * @param newValue
     *            the New value of the ProgressIndicator.
     * @see com.vaadin.ui.AbstractField#setValue()
     */
    @Override
    public void setValue(Object newValue) {
        if (dataSource == null) {
            throw new IllegalStateException("Datasource must be set");
        }
        dataSource.setValue(newValue);
    }

    /**
     * @see com.vaadin.ui.AbstractField#toString()
     * @deprecated use the data source value instead of toString()
     */
    @Deprecated
    @Override
    public String toString() {
        throw new UnsupportedOperationException(
                "Use Property.getValue() instead of ProgressIndicator.toString()");
    }

    /**
     * @see com.vaadin.ui.AbstractField#getType()
     */
    @Override
    public Class<? extends Number> getType() {
        if (dataSource == null) {
            throw new IllegalStateException("Datasource must be set");
        }
        return dataSource.getType();
    }

    /**
     * Gets the viewing data-source property.
     * 
     * @return the datasource.
     * @see com.vaadin.ui.AbstractField#getPropertyDataSource()
     */
    @Override
    public Property getPropertyDataSource() {
        return dataSource;
    }

    /**
     * Sets the property as data-source for viewing.
     * 
     * @param newDataSource
     *            the new data source.
     * @see com.vaadin.ui.AbstractField#setPropertyDataSource(com.vaadin.data.Property)
     */
    @Override
    public void setPropertyDataSource(Property newDataSource) {
        // Stops listening the old data source changes
        if (dataSource != null
                && Property.ValueChangeNotifier.class
                        .isAssignableFrom(dataSource.getClass())) {
            ((Property.ValueChangeNotifier) dataSource).removeListener(this);
        }

        // Sets the new data source
        dataSource = newDataSource;

        // Listens the new data source if possible
        if (dataSource != null
                && Property.ValueChangeNotifier.class
                        .isAssignableFrom(dataSource.getClass())) {
            ((Property.ValueChangeNotifier) dataSource).addListener(this);
        }
    }

    /**
     * Gets the mode of ProgressIndicator.
     * 
     * @return true if in indeterminate mode.
     */
    public boolean getContentMode() {
        return indeterminate;
    }

    /**
     * Sets wheter or not the ProgressIndicator is indeterminate.
     * 
     * @param newValue
     *            true to set to indeterminate mode.
     */
    public void setIndeterminate(boolean newValue) {
        indeterminate = newValue;
        requestRepaint();
    }

    /**
     * Gets whether or not the ProgressIndicator is indeterminate.
     * 
     * @return true to set to indeterminate mode.
     */
    public boolean isIndeterminate() {
        return indeterminate;
    }

    /**
     * Sets the interval that component checks for progress.
     * 
     * @param newValue
     *            the interval in milliseconds.
     */
    public void setPollingInterval(int newValue) {
        pollingInterval = newValue;
        requestRepaint();
    }

    /**
     * Gets the interval that component checks for progress.
     * 
     * @return the interval in milliseconds.
     */
    public int getPollingInterval() {
        return pollingInterval;
    }

}
