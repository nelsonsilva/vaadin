package com.itmill.toolkit.demo.reservation;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Iterator;

import com.itmill.toolkit.data.Container;
import com.itmill.toolkit.data.Item;
import com.itmill.toolkit.data.Property;
import com.itmill.toolkit.data.util.IndexedContainer;
import com.itmill.toolkit.terminal.PaintException;
import com.itmill.toolkit.terminal.PaintTarget;
import com.itmill.toolkit.terminal.Sizeable;
import com.itmill.toolkit.ui.AbstractComponent;

public class GoogleMap extends AbstractComponent implements Sizeable,
        Container.Viewer {
    private String TAG_MARKERS = "markers";
    private String TAG_MARKER = "marker";
    private int width = 400;
    private int height = 300;
    private int zoomLevel = 15;
    private Point2D.Double mapCenter;

    private Container dataSource;
    private Object itemMarkerHtmlPropertyId = new Object();
    private Object itemMarkerXPropertyId = new Object();
    private Object itemMarkerYPropertyId = new Object();

    public String getTag() {
        return "googlemap";
    }

    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        if (null != mapCenter) {
            target.addAttribute("centerX", mapCenter.getX());
            target.addAttribute("centerY", mapCenter.getY());
        }
        target.addAttribute("zoom", zoomLevel);
        target.addAttribute("width", width);
        target.addAttribute("height", height);

        if (dataSource != null) {
            target.startTag(TAG_MARKERS);
            Collection itemIds = dataSource.getItemIds();
            for (Iterator it = itemIds.iterator(); it.hasNext();) {
                Object itemId = it.next();
                Item item = dataSource.getItem(itemId);
                Property p = item.getItemProperty(getItemMarkerXPropertyId());
                Double x = (Double) (p != null ? p.getValue() : null);
                p = item.getItemProperty(getItemMarkerYPropertyId());
                Double y = (Double) (p != null ? p.getValue() : null);
                if (x == null || y == null) {
                    continue;
                }
                target.startTag(TAG_MARKER);
                target.addAttribute("x", x.doubleValue());
                target.addAttribute("y", y.doubleValue());
                p = item.getItemProperty(getItemMarkerHtmlPropertyId());
                String h = (String) (p != null ? p.getValue() : null);
                target.addAttribute("html", h);
                target.endTag(TAG_MARKER);
            }
            target.endTag(TAG_MARKERS);
        }
    }

    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = zoomLevel;
        requestRepaint();
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    // Sizeable methods:

    public int getHeight() {
        return height;
    }

    public int getHeightUnits() {
        return Sizeable.UNITS_PIXELS;
    }

    public int getWidth() {
        return width;
    }

    public int getWidthUnits() {
        return Sizeable.UNITS_PIXELS;
    }

    public void setHeight(int height) {
        this.height = height;
        requestRepaint();
    }

    public void setHeightUnits(int units) {
        throw new UnsupportedOperationException();
    }

    public void setWidth(int width) {
        this.width = width;
        requestRepaint();
    }

    public void setWidthUnits(int units) {
        throw new UnsupportedOperationException();
    }

    public void setMapCenter(Point2D.Double center) {
        mapCenter = center;
    }

    public Point2D.Double getMapCenter() {
        return mapCenter;
    }

    // Container.Viewer methods:

    public Container getContainerDataSource() {
        return dataSource;
    }

    public void setContainerDataSource(Container newDataSource) {

        dataSource = newDataSource;

        requestRepaint();
    }

    // Item methods

    public Object getItemMarkerHtmlPropertyId() {
        return itemMarkerHtmlPropertyId;
    }

    public void setItemMarkerHtmlPropertyId(Object itemMarkerHtmlPropertyId) {
        this.itemMarkerHtmlPropertyId = itemMarkerHtmlPropertyId;
        requestRepaint();
    }

    public Object getItemMarkerXPropertyId() {
        return itemMarkerXPropertyId;
    }

    public void setItemMarkerXPropertyId(Object itemMarkerXPropertyId) {
        this.itemMarkerXPropertyId = itemMarkerXPropertyId;
        requestRepaint();
    }

    public Object getItemMarkerYPropertyId() {
        return itemMarkerYPropertyId;
    }

    public void setItemMarkerYPropertyId(Object itemMarkerYPropertyId) {
        this.itemMarkerYPropertyId = itemMarkerYPropertyId;
        requestRepaint();
    }

    // Marker add

    public Object addMarker(String html, Point2D.Double location) {
        if (location == null) {
            throw new IllegalArgumentException("Location must be non-null");
        }
        if (dataSource == null) {
            initDataSource();
        }
        Object markerId = dataSource.addItem();
        if (markerId == null) {
            return null;
        }
        Item marker = dataSource.getItem(markerId);
        Property p = marker.getItemProperty(getItemMarkerXPropertyId());
        p.setValue(new Double(location.x));
        p = marker.getItemProperty(getItemMarkerYPropertyId());
        p.setValue(new Double(location.y));
        p = marker.getItemProperty(getItemMarkerHtmlPropertyId());
        p.setValue(html);

        requestRepaint();

        return markerId;
    }

    public void removeMarker(Object markerId) {
        if (dataSource != null) {
            dataSource.removeItem(markerId);
            requestRepaint();
        }
    }

    public Item getMarkerItem(Object markerId) {
        if (dataSource != null) {
            return dataSource.getItem(markerId);
        } else {
            return null;
        }
    }

    // dataSource init helper:
    private void initDataSource() {
        dataSource = new IndexedContainer();
        dataSource.addContainerProperty(itemMarkerHtmlPropertyId, String.class,
                null);
        dataSource.addContainerProperty(itemMarkerXPropertyId, Double.class,
                new Double(0));
        dataSource.addContainerProperty(itemMarkerYPropertyId, Double.class,
                new Double(0));
    }

    public void clear() {
        setContainerDataSource(null);
    }

    public void setSizeFull() {
        // TODO Auto-generated method stub

    }

    public void setSizeUndefined() {
        // TODO Auto-generated method stub

    }
}