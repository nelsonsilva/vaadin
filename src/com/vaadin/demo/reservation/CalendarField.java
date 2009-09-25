/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.demo.reservation;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.demo.reservation.gwt.client.ui.VCalendarField;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.DateField;

// TODO send one month at a time, do lazyLoading
// TODO check date limit when updating variables
// TODO Allow item selection
@SuppressWarnings("serial")
@ClientWidget(VCalendarField.class)
public class CalendarField extends DateField implements Container.Viewer {

    private static final String TAGNAME = "calendarfield";

    private Date minDate;
    private Date maxDate;

    private Container dataSource;
    private Object itemStyleNamePropertyId;
    private Object itemStartPropertyId;
    private Object itemEndPropertyId;
    private Object itemTitlePropertyId;
    private Object itemDescriptionPropertyId;
    private Object itemNotimePropertyId;

    public CalendarField() {
        super();
        init();
    }

    public CalendarField(Property dataSource) throws IllegalArgumentException {
        super(dataSource);
        init();
    }

    public CalendarField(String caption, Date value) {
        super(caption, value);
        init();
    }

    public CalendarField(String caption, Property dataSource) {
        super(caption, dataSource);
        init();
    }

    public CalendarField(String caption) {
        super(caption);
        init();
    }

    /*
     * Gets the components UIDL tag string. Don't add a JavaDoc comment here, we
     * use the default documentation from implemented interface.
     */
    @Override
    public String getTag() {
        return TAGNAME;
    }

    public void init() {
        super.setResolution(RESOLUTION_HOUR);

    }

    /**
     * Sets the resolution of the CalendarField. Only RESOLUTION_DAY and
     * RESOLUTION_HOUR are supported.
     * 
     * @param resolution
     *            the resolution to set.
     * @see com.vaadin.ui.DateField#setResolution(int)
     */
    @Override
    public void setResolution(int resolution) {
        if (resolution != RESOLUTION_DAY && resolution != RESOLUTION_HOUR) {
            throw new IllegalArgumentException();
        }
        super.setResolution(resolution);
    }

    public void setMinimumDate(Date date) {
        minDate = date;
        requestRepaint();
    }

    public Date getMinimumDate() {
        return minDate;
    }

    public void setMaximumDate(Date date) {
        maxDate = date;
        requestRepaint();
    }

    public Date getMaximumDate() {
        return maxDate;
    }

    public Container getContainerDataSource() {
        return dataSource;
    }

    public void setContainerDataSource(Container newDataSource) {
        if (newDataSource == null || checkDataSource(newDataSource)) {
            dataSource = newDataSource;
        } else {
            // TODO error message
            throw new IllegalArgumentException();
        }
        requestRepaint();
    }

    private boolean checkDataSource(Container dataSource) {

        // Check old propertyIds
        if (itemEndPropertyId != null) {
            final Class<?> c = dataSource.getType(itemEndPropertyId);
            if (!Date.class.isAssignableFrom(c)) {
                itemEndPropertyId = null;
            }
        }
        if (itemNotimePropertyId != null) {
            final Class<?> c = dataSource.getType(itemNotimePropertyId);
            if (!Boolean.class.isAssignableFrom(c)) {
                itemNotimePropertyId = null;
            }
        }
        if (itemStartPropertyId != null) {
            final Class<?> c = dataSource.getType(itemStartPropertyId);
            if (Date.class.isAssignableFrom(c)) {
                // All we _really_ need is one date
                return true;
            } else {
                itemStartPropertyId = null;
            }
        }
        // We need at least one Date
        final Collection<?> ids = dataSource.getContainerPropertyIds();
        for (final Iterator<?> it = ids.iterator(); it.hasNext();) {
            final Object id = it.next();
            final Class<?> c = dataSource.getType(id);
            if (Date.class.isAssignableFrom(c)) {
                itemStartPropertyId = id;
                return true;
            }
        }

        return false;
    }

    public Object getItemStyleNamePropertyId() {
        return itemStyleNamePropertyId;
    }

    public void setItemStyleNamePropertyId(Object propertyId) {
        itemStyleNamePropertyId = propertyId;
    }

    public Object getItemStartPropertyId() {
        return itemStartPropertyId;
    }

    public void setItemStartPropertyId(Object propertyId) {
        // TODO nullcheck for property id
        if (dataSource != null
                && !Date.class.isAssignableFrom(dataSource.getType(propertyId))) {
            // TODO error message
            throw new IllegalArgumentException();
        }
        itemStartPropertyId = propertyId;
    }

    public Object getItemEndPropertyId() {
        return itemEndPropertyId;
    }

    public void setItemEndPropertyId(Object propertyId) {
        // TODO nullcheck for property id
        if (dataSource != null
                && !Date.class.isAssignableFrom(dataSource.getType(propertyId))) {
            // TODO error message
            throw new IllegalArgumentException();
        }
        itemEndPropertyId = propertyId;
    }

    public Object getItemTitlePropertyId() {
        return itemTitlePropertyId;
    }

    public void setItemTitlePropertyId(Object propertyId) {
        itemTitlePropertyId = propertyId;
    }

    public Object getItemDescriptionPropertyId() {
        return itemDescriptionPropertyId;
    }

    public void setItemDescriptionPropertyId(Object propertyId) {
        itemDescriptionPropertyId = propertyId;
    }

    public Object getitemNotimePropertyId() {
        return itemNotimePropertyId;
    }

    public void setItemNotimePropertyId(Object propertyId) {
        // TODO nullcheck for property id
        if (dataSource != null
                && !Boolean.class.isAssignableFrom(dataSource
                        .getType(propertyId))) {
            // TODO error message
            throw new IllegalArgumentException();
        }
        itemNotimePropertyId = propertyId;
    }

    /**
     * Paints the content of this component.
     * 
     * @param target
     *            the Paint Event.
     * @throws PaintException
     *             if the paint operation failed.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        if (minDate != null) {
            target.addAttribute("min", String.valueOf(minDate.getTime()));
        }
        if (maxDate != null) {
            target.addAttribute("max", String.valueOf(maxDate.getTime()));
        }

        if (dataSource != null) {
            target.startTag("items");

            // TODO send one month now, the rest via lazyloading
            int month = new Date().getMonth();
            final Object value = getValue();
            if (value != null && value instanceof Date) {
                month = ((Date) value).getMonth();
            }

            for (final Iterator<?> it = dataSource.getItemIds().iterator(); it
                    .hasNext();) {
                final Object itemId = it.next();
                final Item item = dataSource.getItem(itemId);
                Property p = item.getItemProperty(itemStartPropertyId);
                Date start = (Date) p.getValue();
                Date end = start; // assume same day
                if (itemEndPropertyId != null) {
                    p = item.getItemProperty(itemEndPropertyId);
                    end = (Date) p.getValue();
                    if (end == null) {
                        end = start;
                    } else if (end.before(start)) {
                        final Date tmp = start;
                        start = end;
                        end = tmp;
                    }
                }

                // TODO half-done lazyloading logic (hence broken)

                if (start != null) {
                    if ((start.getMonth() <= month || end.getMonth() >= month)) {
                        target.startTag("item");
                        // TODO different id?
                        target.addAttribute("id", itemId.hashCode());
                        if (itemStyleNamePropertyId != null) {
                            p = item.getItemProperty(itemStyleNamePropertyId);
                            final String styleName = (String) p.getValue();
                            target.addAttribute("styleName", styleName);
                        }
                        SimpleDateFormat sdf = new SimpleDateFormat(
                                "d MM yyyy HH:mm:ss Z");

                        target.addAttribute("Z", start.getTimezoneOffset());

                        target.addAttribute("start", "" + sdf.format(start));

                        if (end != start) {
                            target.addAttribute("end", "" + sdf.format(end));
                        }
                        if (itemTitlePropertyId != null) {
                            p = item.getItemProperty(itemTitlePropertyId);
                            final Object val = p.getValue();
                            if (val != null) {
                                target.addAttribute("title", val.toString());
                            }
                        }
                        if (itemDescriptionPropertyId != null) {
                            p = item.getItemProperty(itemDescriptionPropertyId);
                            final Object val = p.getValue();
                            if (val != null) {
                                target.addAttribute("description", val
                                        .toString());
                            }
                        }
                        if (itemNotimePropertyId != null) {
                            p = item.getItemProperty(itemNotimePropertyId);
                            final Object val = p.getValue();
                            if (val != null) {
                                target.addAttribute("notime", ((Boolean) val)
                                        .booleanValue());
                            }
                        }

                        target.endTag("item");
                    }
                }
            }

            target.endTag("items");
        }
    }
}
