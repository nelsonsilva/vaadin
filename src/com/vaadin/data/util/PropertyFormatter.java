/*
@ITMillApache2LicenseForJavaFiles@
 */
package com.vaadin.data.util;

import com.vaadin.data.Property;

/**
 * Formatting proxy for a {@link Property}.
 * 
 * <p>
 * This class can be used to implement formatting for any type of Property
 * datasources. The idea is to connect this as proxy between UI component and
 * the original datasource.
 * </p>
 * 
 * <p>
 * For example <code>
 * <pre>textfield.setPropertyDataSource(new PropertyFormatter(property) {
            public String format(Object value) {
                return ((Double) value).toString() + "000000000";
            }

            public Object parse(String formattedValue) throws Exception {
                return Double.parseDouble(formattedValue);
            }

        });</pre></code> adds formatter for Double-typed property that extends
 * standard "1.0" notation with more zeroes.
 * </p>
 * 
 * @author IT Mill Ltd.
 * @since 5.3.0
 */
@SuppressWarnings("serial")
public abstract class PropertyFormatter extends AbstractProperty implements Property.Viewer,
        Property.ValueChangeListener, Property.ReadOnlyStatusChangeListener {

    /** Datasource that stores the actual value. */
    Property dataSource;

    /**
     * Construct a new {@code PropertyFormatter} that is not connected to any
     * data source. Call {@link #setPropertyDataSource(Property)} later on to
     * attach it to a property.
     * 
     */
    protected PropertyFormatter() {
    }

    /**
     * Construct a new formatter that is connected to given data source. Calls
     * {@link #format(Object)} which can be a problem if the formatter has not
     * yet been initialized.
     * 
     * @param propertyDataSource
     *            to connect this property to.
     */
    public PropertyFormatter(Property propertyDataSource) {

        setPropertyDataSource(propertyDataSource);
    }

    /**
     * Gets the current data source of the formatter, if any.
     * 
     * @return the current data source as a Property, or <code>null</code> if
     *         none defined.
     */
    public Property getPropertyDataSource() {
        return dataSource;
    }

    /**
     * Sets the specified Property as the data source for the formatter.
     * 
     * 
     * <p>
     * Remember that new data sources getValue() must return objects that are
     * compatible with parse() and format() methods.
     * </p>
     * 
     * @param newDataSource
     *            the new data source Property.
     */
    public void setPropertyDataSource(Property newDataSource) {

        boolean readOnly = false;
        String prevValue = null;

        if (dataSource != null) {
            if (dataSource instanceof Property.ValueChangeNotifier) {
                ((Property.ValueChangeNotifier) dataSource)
                        .removeListener(this);
            }
            if (dataSource instanceof Property.ReadOnlyStatusChangeListener) {
                ((Property.ReadOnlyStatusChangeNotifier) dataSource)
                        .removeListener(this);
            }
            readOnly = isReadOnly();
            prevValue = toString();
        }

        dataSource = newDataSource;

        if (dataSource != null) {
            if (dataSource instanceof Property.ValueChangeNotifier) {
                ((Property.ValueChangeNotifier) dataSource).addListener(this);
            }
            if (dataSource instanceof Property.ReadOnlyStatusChangeListener) {
                ((Property.ReadOnlyStatusChangeNotifier) dataSource)
                        .addListener(this);
            }
        }

        if (isReadOnly() != readOnly) {
            fireReadOnlyStatusChange();
        }
        String newVal = toString();
        if ((prevValue == null && newVal != null)
                || (prevValue != null && !prevValue.equals(newVal))) {
            fireValueChange();
        }
    }

    /* Documented in the interface */
    public Class getType() {
        return String.class;
    }

    /**
     * Get the formatted value.
     * 
     * @return If the datasource returns null, this is null. Otherwise this is
     *         String given by format().
     */
    public Object getValue() {
        return toString();
    }

    /**
     * Get the formatted value.
     * 
     * @return If the datasource returns null, this is null. Otherwise this is
     *         String given by format().
     */
    @Override
    public String toString() {
        if (dataSource == null || dataSource.getValue() == null) {
            return null;
        }
        return format(dataSource.getValue());
    }

    /** Reflects the read-only status of the datasource. */
    @Override
    public boolean isReadOnly() {
        return dataSource == null ? false : dataSource.isReadOnly();
    }

    /**
     * This method must be implemented to format the values received from
     * DataSource.
     * 
     * @param value
     *            Value object got from the datasource. This is guaranteed to be
     *            non-null and of the type compatible with getType() of the
     *            datasource.
     * @return
     */
    abstract public String format(Object value);

    /**
     * Parse string and convert it to format compatible with datasource.
     * 
     * The method is required to assure that parse(format(x)) equals x.
     * 
     * @param formattedValue
     *            This is guaranteed to be non-null string.
     * @return Non-null value compatible with datasource.
     * @throws Exception
     *             Any type of exception can be thrown to indicate that the
     *             conversion was not succesful.
     */
    abstract public Object parse(String formattedValue) throws Exception;

    /**
     * Sets the Property's read-only mode to the specified status.
     * 
     * @param newStatus
     *            the new read-only status of the Property.
     */
    @Override
    public void setReadOnly(boolean newStatus) {
        if (dataSource != null) {
            dataSource.setReadOnly(newStatus);
        }
    }

    public void setValue(Object newValue) throws ReadOnlyException,
            ConversionException {
        if (dataSource == null) {
            return;
        }
        if (newValue == null) {
            if (dataSource.getValue() != null) {
                dataSource.setValue(null);
                fireValueChange();
            }
        } else {
            try {
                dataSource.setValue(parse(newValue.toString()));
                if (!newValue.equals(toString())) {
                    fireValueChange();
                }
            } catch (ConversionException e) {
                throw e;
            } catch (Exception e) {
                throw new ConversionException(e);
            }
        }
    }

    /**
     * Listens for changes in the datasource.
     * 
     * This should not be called directly.
     */
    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
        fireValueChange();
    }

    /**
     * Listens for changes in the datasource.
     * 
     * This should not be called directly.
     */
    public void readOnlyStatusChange(
            com.vaadin.data.Property.ReadOnlyStatusChangeEvent event) {
        fireReadOnlyStatusChange();
    }

}
