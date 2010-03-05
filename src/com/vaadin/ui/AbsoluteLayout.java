/* 
@ITMillApache2LicenseForJavaFiles@
 */
package com.vaadin.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.terminal.gwt.client.ui.VAbsoluteLayout;

/**
 * AbsoluteLayout is a layout implementation that mimics html absolute
 * positioning.
 * 
 */
@SuppressWarnings("serial")
@ClientWidget(VAbsoluteLayout.class)
public class AbsoluteLayout extends AbstractLayout {

    private static final String CLICK_EVENT = VAbsoluteLayout.CLICK_EVENT_IDENTIFIER;

    private Collection<Component> components = new LinkedHashSet<Component>();
    private Map<Component, ComponentPosition> componentToCoordinates = new HashMap<Component, ComponentPosition>();

    public AbsoluteLayout() {
        setSizeFull();
    }

    public Iterator<Component> getComponentIterator() {
        return components.iterator();
    }

    public void replaceComponent(Component oldComponent, Component newComponent) {
        ComponentPosition position = getPosition(oldComponent);
        removeComponent(oldComponent);
        addComponent(newComponent);
        componentToCoordinates.put(newComponent, position);
    }

    @Override
    public void addComponent(Component c) {
        components.add(c);
        super.addComponent(c);
        requestRepaint();
    }

    @Override
    public void removeComponent(Component c) {
        components.remove(c);
        super.removeComponent(c);
        requestRepaint();
    }

    public void addComponent(Component c, String cssPosition) {
        addComponent(c);
        getPosition(c).setCSSString(cssPosition);
    }

    public ComponentPosition getPosition(Component component) {
        if (componentToCoordinates.containsKey(component)) {
            return componentToCoordinates.get(component);
        } else {
            ComponentPosition coords = new ComponentPosition();
            componentToCoordinates.put(component, coords);
            return coords;
        }
    }

    /**
     * TODO symmetric getters and setters for fields to make this simpler to use
     * in generic java tools
     * 
     */
    public class ComponentPosition implements Serializable {

        private int zIndex = -1;
        private float topValue = -1;
        private float rightValue = -1;
        private float bottomValue = -1;
        private float leftValue = -1;

        private int topUnits;
        private int rightUnits;
        private int bottomUnits;
        private int leftUnits;

        /**
         * Sets the position attributes using CSS syntax. Example usage:
         * 
         * <code><pre>
         * setCSSString("top:10px;left:20%;z-index:16;");
         * </pre></code>
         * 
         * @param css
         */
        public void setCSSString(String css) {
            String[] cssProperties = css.split(";");
            for (int i = 0; i < cssProperties.length; i++) {
                String[] keyValuePair = cssProperties[i].split(":");
                String key = keyValuePair[0].trim();
                if (key.equals("")) {
                    continue;
                }
                if (key.equals("z-index")) {
                    zIndex = Integer.parseInt(keyValuePair[1]);
                } else {
                    String value;
                    if (keyValuePair.length > 1) {
                        value = keyValuePair[1].trim();
                    } else {
                        value = "";
                    }
                    String unit = value.replaceAll("[0-9\\.]+", "");
                    if (!unit.equals("")) {
                        value = value.substring(0, value.indexOf(unit)).trim();
                    }
                    float v = Float.parseFloat(value);
                    int unitInt = parseCssUnit(unit);
                    if (key.equals("top")) {
                        topValue = v;
                        topUnits = unitInt;
                    } else if (key.equals("right")) {
                        rightValue = v;
                        rightUnits = unitInt;
                    } else if (key.equals("bottom")) {
                        bottomValue = v;
                        bottomUnits = unitInt;
                    } else if (key.equals("left")) {
                        leftValue = v;
                        leftUnits = unitInt;
                    }
                }
            }
            requestRepaint();
        }

        private int parseCssUnit(String string) {
            for (int i = 0; i < UNIT_SYMBOLS.length; i++) {
                if (UNIT_SYMBOLS[i].equals(string)) {
                    return i;
                }
            }
            return 0; // defaults to px (eg. top:0;)
        }

        public String getCSSString() {
            String s = "";
            if (topValue >= 0) {
                s += "top:" + topValue + UNIT_SYMBOLS[topUnits] + ";";
            }
            if (rightValue >= 0) {
                s += "right:" + rightValue + UNIT_SYMBOLS[rightUnits] + ";";
            }
            if (bottomValue >= 0) {
                s += "bottom:" + bottomValue + UNIT_SYMBOLS[bottomUnits] + ";";
            }
            if (leftValue >= 0) {
                s += "left:" + leftValue + UNIT_SYMBOLS[leftUnits] + ";";
            }
            if (zIndex >= 0) {
                s += "z-index:" + zIndex + ";";
            }
            return s;
        }

        public void setTop(float topValue, int topUnits) {
            validateLength(topValue, topUnits);
            this.topValue = topValue;
            this.topUnits = topUnits;
            requestRepaint();
        }

        public void setRight(float rightValue, int rightUnits) {
            validateLength(rightValue, rightUnits);
            this.rightValue = rightValue;
            this.rightUnits = rightUnits;
            requestRepaint();
        }

        public void setBottom(float bottomValue, int units) {
            validateLength(bottomValue, units);
            this.bottomValue = bottomValue;
            bottomUnits = units;
            requestRepaint();
        }

        public void setLeft(float leftValue, int units) {
            validateLength(leftValue, units);
            this.leftValue = leftValue;
            leftUnits = units;
            requestRepaint();
        }

        public void setZIndex(int zIndex) {
            this.zIndex = zIndex;
            requestRepaint();
        }

        public void setTopValue(float topValue) {
            validateLength(topValue, topUnits);
            this.topValue = topValue;
            requestRepaint();
        }

        public float getTopValue() {
            return topValue;
        }

        /**
         * @return the rightValue
         */
        public float getRightValue() {
            return rightValue;
        }

        /**
         * @param rightValue
         *            the rightValue to set
         */
        public void setRightValue(float rightValue) {
            validateLength(rightValue, rightUnits);
            this.rightValue = rightValue;
            requestRepaint();
        }

        /**
         * @return the bottomValue
         */
        public float getBottomValue() {
            return bottomValue;
        }

        /**
         * @param bottomValue
         *            the bottomValue to set
         */
        public void setBottomValue(float bottomValue) {
            validateLength(bottomValue, bottomUnits);
            this.bottomValue = bottomValue;
            requestRepaint();
        }

        /**
         * @return the leftValue
         */
        public float getLeftValue() {
            return leftValue;
        }

        /**
         * @param leftValue
         *            the leftValue to set
         */
        public void setLeftValue(float leftValue) {
            validateLength(leftValue, leftUnits);
            this.leftValue = leftValue;
            requestRepaint();
        }

        /**
         * @return the topUnits
         */
        public int getTopUnits() {
            return topUnits;
        }

        /**
         * @param topUnits
         *            the topUnits to set
         */
        public void setTopUnits(int topUnits) {
            validateLength(topValue, topUnits);
            this.topUnits = topUnits;
            requestRepaint();
        }

        /**
         * @return the rightUnits
         */
        public int getRightUnits() {
            return rightUnits;
        }

        /**
         * @param rightUnits
         *            the rightUnits to set
         */
        public void setRightUnits(int rightUnits) {
            validateLength(rightValue, rightUnits);
            this.rightUnits = rightUnits;
            requestRepaint();
        }

        /**
         * @return the bottomUnits
         */
        public int getBottomUnits() {
            return bottomUnits;
        }

        /**
         * @param bottomUnits
         *            the bottomUnits to set
         */
        public void setBottomUnits(int bottomUnits) {
            validateLength(bottomValue, bottomUnits);
            this.bottomUnits = bottomUnits;
            requestRepaint();
        }

        /**
         * @return the leftUnits
         */
        public int getLeftUnits() {
            return leftUnits;
        }

        /**
         * @param leftUnits
         *            the leftUnits to set
         */
        public void setLeftUnits(int leftUnits) {
            validateLength(leftValue, leftUnits);
            this.leftUnits = leftUnits;
            requestRepaint();
        }

        /**
         * @return the zIndex
         */
        public int getZIndex() {
            return zIndex;
        }

    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        for (Component component : components) {
            target.startTag("cc");
            target.addAttribute("css", getPosition(component).getCSSString());
            component.paint(target);
            target.endTag("cc");
        }
    }

    private static void validateLength(float topValue, int topUnits2) {
        // TODO throw on invalid value

    }

    @Override
    public void changeVariables(Object source, Map variables) {
        super.changeVariables(source, variables);
        if (variables.containsKey(CLICK_EVENT)) {
            fireClick((Map<String, Object>) variables.get(CLICK_EVENT));
        }

    }

    private void fireClick(Map<String, Object> parameters) {
        MouseEventDetails mouseDetails = MouseEventDetails
                .deSerialize((String) parameters.get("mouseDetails"));
        Component childComponent = (Component) parameters.get("component");

        fireEvent(new LayoutClickEvent(this, mouseDetails, childComponent));
    }

    /**
     * Add a click listener to the layout. The listener is called whenever the
     * user clicks inside the layout. Also when the click targets a component
     * inside the Panel, provided the targeted component does not prevent the
     * click event from propagating.
     * 
     * The child component that was clicked is included in the
     * {@link LayoutClickEvent}.
     * 
     * Use {@link #removeListener(LayoutClickListener)} to remove the listener.
     * 
     * @param listener
     *            The listener to add
     */
    public void addListener(LayoutClickListener listener) {
        addListener(CLICK_EVENT, LayoutClickEvent.class, listener,
                LayoutClickListener.clickMethod);
    }

    /**
     * Remove a click listener from the layout. The listener should earlier have
     * been added using {@link #addListener(LayoutClickListener)}.
     * 
     * @param listener
     *            The listener to remove
     */
    public void removeListener(LayoutClickListener listener) {
        removeListener(CLICK_EVENT, LayoutClickEvent.class, listener);
    }

}
