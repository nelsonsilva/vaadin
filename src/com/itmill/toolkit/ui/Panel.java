/* *************************************************************************
 
 IT Mill Toolkit 

 Development of Browser User Interfaces Made Easy

 Copyright (C) 2000-2006 IT Mill Ltd
 
 *************************************************************************

 This product is distributed under commercial license that can be found
 from the product package on license.pdf. Use of this product might 
 require purchasing a commercial license from IT Mill Ltd. For guidelines 
 on usage, see licensing-guidelines.html

 *************************************************************************
 
 For more information, contact:
 
 IT Mill Ltd                           phone: +358 2 4802 7180
 Ruukinkatu 2-4                        fax:   +358 2 4802 7181
 20540, Turku                          email:  info@itmill.com
 Finland                               company www: www.itmill.com
 
 Primary source for information and releases: www.itmill.com

 ********************************************************************** */

package com.itmill.toolkit.ui;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import com.itmill.toolkit.event.Action;
import com.itmill.toolkit.event.ShortcutAction;
import com.itmill.toolkit.event.Action.Handler;
import com.itmill.toolkit.terminal.KeyMapper;
import com.itmill.toolkit.terminal.PaintException;
import com.itmill.toolkit.terminal.PaintTarget;
import com.itmill.toolkit.terminal.Scrollable;
import com.itmill.toolkit.terminal.Sizeable;

/**
 * Panel - a simple single component container.
 * 
 * @author IT Mill Ltd.
 * @version
 * @VERSION@
 * @since 3.0
 */
public class Panel extends AbstractComponentContainer implements Sizeable,
        Scrollable, ComponentContainer.ComponentAttachListener,
        ComponentContainer.ComponentDetachListener, Action.Container {

    public static final String STYLE_LIGHT = "light";

    public static final String STYLE_EMPHASIZE = "emphasize";

    /**
     * Height of the layout. Set to -1 for undefined height.
     */
    private int height = -1;

    /**
     * Height unit.
     * 
     * @see com.itmill.toolkit.terminal.Sizeable.UNIT_SYMBOLS;
     */
    private int heightUnit = UNITS_PIXELS;

    /**
     * Width of the layout. Set to -1 for undefined width.
     */
    private int width = -1;

    /**
     * Width unit.
     * 
     * @see com.itmill.toolkit.terminal.Sizeable.UNIT_SYMBOLS;
     */
    private int widthUnit = UNITS_PIXELS;

    /**
     * Layout of the panel.
     */
    private Layout layout;

    /**
     * Scroll X position.
     */
    private int scrollOffsetX = 0;

    /**
     * Scroll Y position.
     */
    private int scrollOffsetY = 0;

    /**
     * Scrolling mode.
     */
    private boolean scrollable = false;

    /** List of action handlers */
    private LinkedList actionHandlers = null;

    /** Action mapper */
    private KeyMapper actionMapper = null;

    /**
     * Creates a new empty panel. Ordered layout is used.
     */
    public Panel() {
        setLayout(null);
    }

    /**
     * Creates a new empty panel with given layout. Layout must be non-null.
     * 
     * @param layout
     *                the layout used in the panel.
     */
    public Panel(Layout layout) {
        setLayout(layout);
    }

    /**
     * Creates a new empty panel with caption. Default layout is used.
     * 
     * @param caption
     *                the caption used in the panel.
     */
    public Panel(String caption) {
        this(caption, null);
    }

    /**
     * Creates a new empty panel with caption.
     * 
     * @param caption
     *                the caption of the panel.
     * @param layout
     *                the layout used in the panel.
     */
    public Panel(String caption, Layout layout) {
        this(layout);
        setCaption(caption);
    }

    /**
     * Gets the current layout of the panel.
     * 
     * @return the Current layout of the panel.
     */
    public Layout getLayout() {
        return layout;
    }

    /**
     * Sets the layout of the panel. All the components are moved to new layout.
     * 
     * @param layout
     *                the New layout of the panel.
     */
    public void setLayout(Layout layout) {

        // Only allow non-null layouts
        if (layout == null) {
            layout = new OrderedLayout();
            // Force margins by default
            layout.setMargin(true);
        }

        // Sets the panel to be parent for the layout
        layout.setParent(this);
        dependsOn(layout);

        // If panel already contains a layout, move the contents to new one
        // and detach old layout from the panel
        if (this.layout != null) {
            layout.moveComponentsFrom(this.layout);
            removeDirectDependency(this.layout);
            this.layout.setParent(null);
        }

        // Removes the event listeners from the old layout
        if (this.layout != null) {
            this.layout
                    .removeListener((ComponentContainer.ComponentAttachListener) this);
            this.layout
                    .removeListener((ComponentContainer.ComponentDetachListener) this);
        }

        // Sets the new layout
        this.layout = layout;

        // Adds the event listeners for new layout
        layout.addListener((ComponentContainer.ComponentAttachListener) this);
        layout.addListener((ComponentContainer.ComponentDetachListener) this);
    }

    /**
     * Paints the content of this component.
     * 
     * @param target
     *                the Paint Event.
     * @throws PaintException
     *                 if the paint operation failed.
     */
    public void paintContent(PaintTarget target) throws PaintException {
        layout.paint(target);

        // Add size info as variables
        if (getHeight() > -1) {
            target.addVariable(this, "height", getHeight()
                    + UNIT_SYMBOLS[getHeightUnits()]);
        }
        if (getWidth() > -1) {
            target.addVariable(this, "width", getWidth()
                    + UNIT_SYMBOLS[getWidthUnits()]);
        }

        if (isScrollable()) {
            target.addVariable(this, "scrollleft", getScrollOffsetX());
            target.addVariable(this, "scrolldown", getScrollOffsetY());
        }

        if (actionHandlers != null && !actionHandlers.isEmpty()) {
            target.addVariable(this, "action", "");
            target.startTag("actions");

            for (Iterator ahi = actionHandlers.iterator(); ahi.hasNext();) {
                Action[] aa = ((Action.Handler) ahi.next()).getActions(null,
                        this);
                if (aa != null) {
                    for (int ai = 0; ai < aa.length; ai++) {
                        Action a = aa[ai];
                        target.startTag("action");
                        String akey = actionMapper.key(aa[ai]);
                        target.addAttribute("key", akey);
                        if (a.getCaption() != null) {
                            target.addAttribute("caption", a.getCaption());
                        }
                        if (a.getIcon() != null) {
                            target.addAttribute("icon", a.getIcon());
                        }
                        if (a instanceof ShortcutAction) {
                            ShortcutAction sa = (ShortcutAction) a;
                            target.addAttribute("kc", sa.getKeyCode());
                            int[] modifiers = sa.getModifiers();
                            if (modifiers != null) {
                                String[] smodifiers = new String[modifiers.length];
                                for (int i = 0; i < modifiers.length; i++) {
                                    smodifiers[i] = String
                                            .valueOf(modifiers[i]);
                                }
                                target.addAttribute("mk", smodifiers);
                            }
                        }
                        target.endTag("action");
                    }
                }
            }
            target.endTag("actions");
        }
    }

    /**
     * Gets the component UIDL tag.
     * 
     * @return the Component UIDL tag as string.
     */
    public String getTag() {
        return "panel";
    }

    /**
     * Adds the component into this container.
     * 
     * @param c
     *                the component to be added.
     * @see com.itmill.toolkit.ui.AbstractComponentContainer#addComponent(com.itmill.toolkit.ui.Component)
     */
    public void addComponent(Component c) {
        layout.addComponent(c);
        // No repaint request is made as we except the underlying container to
        // request repaints
    }

    /**
     * Removes the component from this container.
     * 
     * @param c
     *                The component to be added.
     * @see com.itmill.toolkit.ui.AbstractComponentContainer#removeComponent(com.itmill.toolkit.ui.Component)
     */
    public void removeComponent(Component c) {
        layout.removeComponent(c);
        // No repaint request is made as we except the underlying container to
        // request repaints
    }

    /**
     * Gets the component container iterator for going trough all the components
     * in the container.
     * 
     * @return the Iterator of the components inside the container.
     * @see com.itmill.toolkit.ui.ComponentContainer#getComponentIterator()
     */
    public Iterator getComponentIterator() {
        return layout.getComponentIterator();
    }

    /**
     * Called when one or more variables handled by the implementing class are
     * changed.
     * 
     * @see com.itmill.toolkit.terminal.VariableOwner#changeVariables(Object,
     *      Map)
     */
    public void changeVariables(Object source, Map variables) {
        super.changeVariables(source, variables);

        // Get new size
        Integer newWidth = (Integer) variables.get("width");
        Integer newHeight = (Integer) variables.get("height");
        if (newWidth != null && newWidth.intValue() != getWidth()) {
            setWidth(newWidth.intValue());
            // ensure units as we are reading pixels
            setWidthUnits(UNITS_PIXELS);

        }
        if (newHeight != null && newHeight.intValue() != getHeight()) {
            setHeight(newHeight.intValue());
            // ensure units as we are reading pixels
            setHeightUnits(UNITS_PIXELS);
        }

        // Scrolling
        Integer newScrollX = (Integer) variables.get("scrollleft");
        Integer newScrollY = (Integer) variables.get("scrolldown");
        if (newScrollX != null && newScrollX.intValue() != getScrollOffsetX()) {
            setScrollOffsetX(newScrollX.intValue());
        }
        if (newScrollY != null && newScrollY.intValue() != getScrollOffsetY()) {
            setScrollOffsetY(newScrollY.intValue());
        }

        // Actions
        if (variables.containsKey("action")) {
            String key = (String) variables.get("action");
            Action action = (Action) actionMapper.get(key);
            if (action != null && actionHandlers != null) {
                for (Iterator i = actionHandlers.iterator(); i.hasNext();) {
                    ((Action.Handler) i.next())
                            .handleAction(action, this, this);
                }
            }
        }

    }

    /* Scrolling functionality */

    /* Documented in interface */
    public int getScrollOffsetX() {
        return scrollOffsetX;
    }

    /* Documented in interface */
    public int getScrollOffsetY() {
        return scrollOffsetY;
    }

    /* Documented in interface */
    public boolean isScrollable() {
        return scrollable;
    }

    /* Documented in interface */
    public void setScrollable(boolean isScrollingEnabled) {
        if (scrollable != isScrollingEnabled) {
            scrollable = isScrollingEnabled;
            requestRepaint();
        }
    }

    /* Documented in interface */
    public void setScrollOffsetX(int pixelsScrolledLeft) {
        if (pixelsScrolledLeft < 0) {
            throw new IllegalArgumentException(
                    "Scroll offset must be at least 0");
        }
        if (scrollOffsetX != pixelsScrolledLeft) {
            scrollOffsetX = pixelsScrolledLeft;
            requestRepaint();
        }
    }

    /* Documented in interface */
    public void setScrollOffsetY(int pixelsScrolledDown) {
        if (pixelsScrolledDown < 0) {
            throw new IllegalArgumentException(
                    "Scroll offset must be at least 0");
        }
        if (scrollOffsetY != pixelsScrolledDown) {
            scrollOffsetY = pixelsScrolledDown;
            requestRepaint();
        }
    }

    /* Documented in superclass */
    public void replaceComponent(Component oldComponent, Component newComponent) {

        layout.replaceComponent(oldComponent, newComponent);
    }

    /**
     * A new component is attached to container.
     * 
     * @see com.itmill.toolkit.ui.ComponentContainer.ComponentAttachListener#componentAttachedToContainer(com.itmill.toolkit.ui.ComponentContainer.ComponentAttachEvent)
     */
    public void componentAttachedToContainer(ComponentAttachEvent event) {
        if (event.getContainer() == layout) {
            fireComponentAttachEvent(event.getAttachedComponent());
        }
    }

    /**
     * A component has been detached from container.
     * 
     * @see com.itmill.toolkit.ui.ComponentContainer.ComponentDetachListener#componentDetachedFromContainer(com.itmill.toolkit.ui.ComponentContainer.ComponentDetachEvent)
     */
    public void componentDetachedFromContainer(ComponentDetachEvent event) {
        if (event.getContainer() == layout) {
            fireComponentDetachEvent(event.getDetachedComponent());
        }
    }

    /**
     * Notifies the component that it is connected to an application.
     * 
     * @see com.itmill.toolkit.ui.Component#attach()
     */
    public void attach() {
        super.attach();
        if (layout != null) {
            layout.attach();
        }
    }

    /**
     * Notifies the component that it is detached from the application.
     * 
     * @see com.itmill.toolkit.ui.Component#detach()
     */
    public void detach() {
        if (layout != null) {
            layout.detach();
        }
    }

    /**
     * Removes all components from this container.
     * 
     * @see com.itmill.toolkit.ui.ComponentContainer#removeAllComponents()
     */
    public void removeAllComponents() {
        layout.removeAllComponents();
    }

    public void addActionHandler(Handler actionHandler) {
        if (actionHandler != null) {

            if (actionHandlers == null) {
                actionHandlers = new LinkedList();
                actionMapper = new KeyMapper();
            }

            if (!actionHandlers.contains(actionHandler)) {
                actionHandlers.add(actionHandler);
                requestRepaint();
            }
        }

    }

    /**
     * Removes an action handler.
     * 
     * @see com.itmill.toolkit.event.Action.Container#removeActionHandler(Action.Handler)
     */
    public void removeActionHandler(Action.Handler actionHandler) {

        if (actionHandlers != null && actionHandlers.contains(actionHandler)) {

            actionHandlers.remove(actionHandler);

            if (actionHandlers.isEmpty()) {
                actionHandlers = null;
                actionMapper = null;
            }

            requestRepaint();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.itmill.toolkit.terminal.Sizeable#getHeight()
     */
    public int getHeight() {
        return height;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.itmill.toolkit.terminal.Sizeable#getHeightUnits()
     */
    public int getHeightUnits() {
        return heightUnit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.itmill.toolkit.terminal.Sizeable#getWidth()
     */
    public int getWidth() {
        return width;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.itmill.toolkit.terminal.Sizeable#getWidthUnits()
     */
    public int getWidthUnits() {
        return widthUnit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.itmill.toolkit.terminal.Sizeable#setHeight(int)
     */
    public void setHeight(int height) {
        this.height = height;
        requestRepaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.itmill.toolkit.terminal.Sizeable#setHeightUnits(int)
     */
    public void setHeightUnits(int units) {
        heightUnit = units;
        requestRepaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.itmill.toolkit.terminal.Sizeable#setSizeFull()
     */
    public void setSizeFull() {
        height = 100;
        width = 100;
        heightUnit = UNITS_PERCENTAGE;
        widthUnit = UNITS_PERCENTAGE;
        requestRepaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.itmill.toolkit.terminal.Sizeable#setSizeUndefined()
     */
    public void setSizeUndefined() {
        height = -1;
        width = -1;
        heightUnit = UNITS_PIXELS;
        widthUnit = UNITS_PIXELS;
        requestRepaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.itmill.toolkit.terminal.Sizeable#setWidth(int)
     */
    public void setWidth(int width) {
        this.width = width;
        requestRepaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.itmill.toolkit.terminal.Sizeable#setWidthUnits(int)
     */
    public void setWidthUnits(int units) {
        widthUnit = units;
        requestRepaint();
    }

}
