package com.vaadin.event;

import java.io.Serializable;
import java.lang.reflect.Method;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.ui.Component;
import com.vaadin.ui.Component.Event;

/**
 * 
 * Click event fired by a {@link Component} implementing
 * {@link com.vaadin.data.Container} interface. ItemClickEvents happens
 * on an {@link Item} rendered somehow on terminal. Event may also contain a
 * specific {@link Property} on which the click event happened.
 * 
 * ClickEvents are rather terminal dependent events. Correct values in event
 * details cannot be guaranteed.
 * 
 * EXPERIMENTAL FEATURE, user input is welcome
 * 
 * @since 5.3
 * 
 *        TODO extract generic super class/interfaces if we implement some other
 *        click events.
 */
@SuppressWarnings("serial")
public class ItemClickEvent extends Event implements Serializable {
    public static final int BUTTON_LEFT = MouseEventDetails.BUTTON_LEFT;
    public static final int BUTTON_MIDDLE = MouseEventDetails.BUTTON_MIDDLE;
    public static final int BUTTON_RIGHT = MouseEventDetails.BUTTON_RIGHT;

    private MouseEventDetails details;
    private Item item;
    private Object itemId;
    private Object propertyId;

    public ItemClickEvent(Component source, Item item, Object itemId,
            Object propertyId, MouseEventDetails details) {
        super(source);
        this.details = details;
        this.item = item;
        this.itemId = itemId;
        this.propertyId = propertyId;
    }

    /**
     * Gets the item on which the click event occurred.
     * 
     * @return item which was clicked
     */
    public Item getItem() {
        return item;
    }

    /**
     * Gets a possible identifier in source for clicked Item
     * 
     * @return
     */
    public Object getItemId() {
        return itemId;
    }

    /**
     * Returns property on which click event occurred. Returns null if source
     * cannot be resolved at property leve. For example if clicked a cell in
     * table, the "column id" is returned.
     * 
     * @return a property id of clicked property or null if click didn't occur
     *         on any distinct property.
     */
    public Object getPropertyId() {
        return propertyId;
    }

    public int getButton() {
        return details.getButton();
    }

    public int getClientX() {
        return details.getClientX();
    }

    public int getClientY() {
        return details.getClientY();
    }

    public boolean isDoubleClick() {
        return details.isDoubleClick();
    }

    public boolean isAltKey() {
        return details.isAltKey();
    }

    public boolean isCtrlKey() {
        return details.isCtrlKey();
    }

    public boolean isMetaKey() {
        return details.isMetaKey();
    }

    public boolean isShiftKey() {
        return details.isShiftKey();
    }

    public static final Method ITEM_CLICK_METHOD;

    static {
        try {
            ITEM_CLICK_METHOD = ItemClickListener.class.getDeclaredMethod(
                    "itemClick", new Class[] { ItemClickEvent.class });
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException();
        }
    }

    public interface ItemClickListener extends Serializable {
        public void itemClick(ItemClickEvent event);
    }

    /**
     * Components implementing
     * 
     * @link {@link Container} interface may support emitting
     *       {@link ItemClickEvent}s.
     */
    public interface ItemClickSource extends Serializable {
        /**
         * Register listener to handle ItemClickEvents.
         * 
         * Note! Click listeners are rather terminal dependent features.
         * 
         * This feature is EXPERIMENTAL
         * 
         * @param listener
         *            ItemClickListener to be registered
         */
        public void addListener(ItemClickListener listener);

        /**
         * Removes ItemClickListener.
         * 
         * @param listener
         */
        public void removeListener(ItemClickListener listener);
    }

}