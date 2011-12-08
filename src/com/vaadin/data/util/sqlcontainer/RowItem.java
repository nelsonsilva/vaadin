/*
@ITMillApache2LicenseForJavaFiles@
 */
package com.vaadin.data.util.sqlcontainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * RowItem represents one row of a result set obtained from a QueryDelegate.
 * 
 * Note that depending on the QueryDelegate in use this does not necessarily map
 * into an actual row in a database table.
 */
public final class RowItem implements Item {
    private static final long serialVersionUID = -6228966439127951408L;
    private SQLContainer container;
    private RowId id;
    private Collection<ColumnProperty> properties;

    /**
     * Prevent instantiation without required parameters.
     */
    @SuppressWarnings("unused")
    private RowItem() {
    }

    public RowItem(SQLContainer container, RowId id,
            Collection<ColumnProperty> properties) {
        if (container == null) {
            throw new IllegalArgumentException("Container cannot be null.");
        }
        if (id == null) {
            throw new IllegalArgumentException("Row ID cannot be null.");
        }
        this.container = container;
        this.properties = properties;
        /* Set this RowItem as owner to the properties */
        if (properties != null) {
            for (ColumnProperty p : properties) {
                p.setOwner(this);
            }
        }
        this.id = id;
    }

    public Property<?> getItemProperty(Object id) {
        if (id instanceof String && id != null) {
            for (ColumnProperty cp : properties) {
                if (id.equals(cp.getPropertyId())) {
                    return cp;
                }
            }
        }
        return null;
    }

    public Collection<?> getItemPropertyIds() {
        Collection<String> ids = new ArrayList<String>(properties.size());
        for (ColumnProperty cp : properties) {
            ids.add(cp.getPropertyId());
        }
        return Collections.unmodifiableCollection(ids);
    }

    /**
     * Adding properties is not supported. Properties are generated by
     * SQLContainer.
     */
    public boolean addItemProperty(Object id, Property property)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Removing properties is not supported. Properties are generated by
     * SQLContainer.
     */
    public boolean removeItemProperty(Object id)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public RowId getId() {
        return id;
    }

    public SQLContainer getContainer() {
        return container;
    }

    public boolean isModified() {
        if (properties != null) {
            for (ColumnProperty p : properties) {
                if (p.isModified()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append("ID:");
        s.append(getId().toString());
        for (Object propId : getItemPropertyIds()) {
            s.append("|");
            s.append(propId.toString());
            s.append(":");
            Object value = getItemProperty(propId).getValue();
            s.append((null != value) ? value.toString() : null);
        }
        return s.toString();
    }

    public void commit() {
        if (properties != null) {
            for (ColumnProperty p : properties) {
                p.commit();
            }
        }
    }
}
