/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.itmill.toolkit.ui;

import java.util.Iterator;

import com.itmill.toolkit.terminal.PaintException;
import com.itmill.toolkit.terminal.PaintTarget;
import com.itmill.toolkit.terminal.Sizeable;

/**
 * SplitPanel.
 * 
 * <code>SplitPanel</code> is a component container, that can contain two
 * components (possibly containers) which are split by divider element.
 * 
 * @author IT Mill Ltd.
 * @version
 * @VERSION@
 * @since 5.0
 */
public class SplitPanel extends AbstractLayout {

    /* Predefined orientations ***************************************** */

    /**
     * Components are to be layed out vertically.
     */
    public static int ORIENTATION_VERTICAL = 0;

    /**
     * Components are to be layed out horizontally.
     */
    public static int ORIENTATION_HORIZONTAL = 1;

    private Component firstComponent;

    private Component secondComponent;

    /**
     * Orientation of the layout.
     */
    private int orientation;

    private int pos = 50;

    private int posUnit = UNITS_PERCENTAGE;

    /**
     * Creates a new split panel. The orientation of the panels is
     * <code>ORIENTATION_VERTICAL</code>.
     */
    public SplitPanel() {
        orientation = ORIENTATION_VERTICAL;
        setSizeFull();
    }

    /**
     * Create a new split panels. The orientation of the panel is given as
     * parameters.
     * 
     * @param orientation
     *                the Orientation of the layout.
     */
    public SplitPanel(int orientation) {
        this.orientation = orientation;
        setSizeFull();
    }

    /**
     * Gets the component UIDL tag.
     * 
     * @return the Component UIDL tag as string.
     */
    public String getTag() {
        if (orientation == ORIENTATION_HORIZONTAL) {
            return "hsplitpanel";
        } else {
            return "vsplitpanel";
        }
    }

    /**
     * Add a component into this container. The component is added to the right
     * or under the previous component.
     * 
     * @param c
     *                the component to be added.
     */
    public void addComponent(Component c) {
        if (firstComponent == null) {
            firstComponent = c;
        } else if (secondComponent == null) {
            secondComponent = c;
        } else {
            throw new UnsupportedOperationException(
                    "Split panel can contain only two components");
        }
        super.addComponent(c);
        requestRepaint();
    }

    public void setFirstComponent(Component c) {
        if (firstComponent != null) {
            // detach old
            removeComponent(firstComponent);
        }
        firstComponent = c;
        super.addComponent(c);
    }

    public void setSecondComponent(Component c) {
        if (secondComponent != null) {
            // detach old
            removeComponent(c);
        }
        secondComponent = c;
        super.addComponent(c);
    }

    /**
     * Removes the component from this container.
     * 
     * @param c
     *                the component to be removed.
     */
    public void removeComponent(Component c) {
        super.removeComponent(c);
        if (c == firstComponent) {
            firstComponent = null;
        } else {
            secondComponent = null;
        }
        requestRepaint();
    }

    /**
     * Gets the component container iterator for going trough all the components
     * in the container.
     * 
     * @return the Iterator of the components inside the container.
     */
    public Iterator getComponentIterator() {
        return new Iterator() {
            int i = 0;

            public boolean hasNext() {
                if (i < (firstComponent == null ? 0 : 1)
                        + (secondComponent == null ? 0 : 1)) {
                    return true;
                }
                return false;
            }

            public Object next() {
                if (!hasNext()) {
                    return null;
                }
                i++;
                if (i == 1) {
                    return firstComponent == null ? secondComponent
                            : firstComponent;
                } else if (i == 2) {
                    return secondComponent;
                }
                return null;
            }

            public void remove() {
                if (i == 1) {
                    if (firstComponent != null) {
                        setFirstComponent(null);
                        i = 0;
                    } else {
                        setSecondComponent(null);
                    }
                } else if (i == 2) {
                    setSecondComponent(null);
                }
            }
        };
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
        super.paintContent(target);

        final String position = pos + UNIT_SYMBOLS[posUnit];

        target.addAttribute("position", position);

        if (firstComponent != null) {
            firstComponent.paint(target);
        } else {
            (new OrderedLayout()).paint(target);
        }
        if (secondComponent != null) {
            secondComponent.paint(target);
        } else {
            (new OrderedLayout()).paint(target);
        }
    }

    /**
     * Gets the orientation of the container.
     * 
     * @return the Value of property orientation.
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Set the orientation of the container.
     * 
     * @param orientation
     *                the New value of property orientation.
     */
    public void setOrientation(int orientation) {

        // Checks the validity of the argument
        if (orientation < ORIENTATION_VERTICAL
                || orientation > ORIENTATION_HORIZONTAL) {
            throw new IllegalArgumentException();
        }

        this.orientation = orientation;
        requestRepaint();
    }

    /* Documented in superclass */
    public void replaceComponent(Component oldComponent, Component newComponent) {
        if (oldComponent == firstComponent) {
            setFirstComponent(newComponent);
        } else if (oldComponent == secondComponent) {
            setSecondComponent(secondComponent);
        }
        requestRepaint();
    }

    /**
     * Moves the position of the splitter.
     * 
     * @param pos
     *                the new size of the first region in persentage
     */
    public void setSplitPosition(int pos) {
        setSplitPosition(pos, UNITS_PERCENTAGE);
    }

    /**
     * Moves the position of the splitter with given position and unit.
     * 
     * @param pos
     *                size of the first region
     * @param unit
     *                the unit (from {@link Sizeable}) in which the size is
     *                given.
     */
    public void setSplitPosition(int pos, int unit) {
        this.pos = pos;
        posUnit = unit;
    }

}
