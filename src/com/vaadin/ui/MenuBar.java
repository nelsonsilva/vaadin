package com.vaadin.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.gwt.client.ui.VMenuBar;

/**
 * <p>
 * A class representing a horizontal menu bar. The menu can contain MenuItem
 * objects, which in turn can contain more MenuBars. These sub-level MenuBars
 * are represented as vertical menu.
 * </p>
 */
@SuppressWarnings("serial")
@ClientWidget(VMenuBar.class)
public class MenuBar extends AbstractComponent {

    // Items of the top-level menu
    private final List<MenuItem> menuItems;

    // Number of items in this menu
    private static int numberOfItems = 0;

    private boolean collapseItems;
    private Resource submenuIcon;
    private MenuItem moreItem;

    /** Tag is the UIDL element name for client-server communications. */
    @Override
    public java.lang.String getTag() {
        return "menubar";
    }

    /** Paint (serialise) the component for the client. */
    @Override
    public void paintContent(PaintTarget target) throws PaintException {

        // Superclass writes any common attributes in the paint target.
        super.paintContent(target);

        // Stack for list iterators
        Stack<Iterator<MenuItem>> iteratorStack = new Stack<Iterator<MenuItem>>();

        target.startTag("options");

        if (submenuIcon != null) {
            target.addAttribute("submenuIcon", submenuIcon);
        }

        target.addAttribute("collapseItems", collapseItems);

        if (collapseItems) {
            target.startTag("moreItem");
            target.addAttribute("text", moreItem.getText());
            if (moreItem.getIcon() != null) {
                target.addAttribute("icon", moreItem.getIcon());
            }
            target.endTag("moreItem");
        }

        target.endTag("options");
        target.startTag("items");

        Iterator<MenuItem> itr = menuItems.iterator();

        // This generates the tree from the contents of the menu
        while (itr.hasNext()) {

            MenuItem item = itr.next();

            target.startTag("item");

            target.addAttribute("text", item.getText());
            target.addAttribute("id", item.getId());

            Command command = item.getCommand();
            if (command != null) {
                target.addAttribute("command", true);
            } else {
                target.addAttribute("command", false);
            }

            Resource icon = item.getIcon();
            if (icon != null) {
                target.addAttribute("icon", icon);
            }

            if (item.hasChildren()) {
                iteratorStack.push(itr); // For later use

                // Go through the children
                itr = item.getChildren().iterator();
            } else {
                target.endTag("item"); // Item had no children, end description
            }

            // The end submenu. More than one submenu may end at once.
            while (!itr.hasNext() && !iteratorStack.empty()) {
                itr = iteratorStack.pop();
                target.endTag("item");
            }

        }

        target.endTag("items");
    }

    /** Deserialize changes received from client. */
    @Override
    public void changeVariables(Object source, Map variables) {
        Stack<MenuItem> items = new Stack<MenuItem>();
        boolean found = false;

        if (variables.containsKey("clickedId")) {

            Integer clickedId = (Integer) variables.get("clickedId");
            Iterator<MenuItem> itr = getItems().iterator();
            while (itr.hasNext()) {
                items.push(itr.next());
            }

            MenuItem tmpItem = null;

            // Go through all the items in the menu
            while (!found && !items.empty()) {
                tmpItem = items.pop();
                found = (clickedId.intValue() == tmpItem.getId());

                if (tmpItem.hasChildren()) {
                    itr = tmpItem.getChildren().iterator();
                    while (itr.hasNext()) {
                        items.push(itr.next());
                    }
                }

            }// while

            // If we got the clicked item, launch the command.
            if (found) {
                tmpItem.getCommand().menuSelected(tmpItem);
            }
        }// if
    }// changeVariables

    /**
     * Constructs an empty, horizontal menu
     */
    public MenuBar() {
        menuItems = new ArrayList<MenuItem>();
        setCollapse(true);
        setMoreMenuItem(null);
    }

    /**
     * Add a new item to the menu bar. Command can be null, but a caption must
     * be given.
     * 
     * @param caption
     *            the text for the menu item
     * @param command
     *            the command for the menu item
     * @throws IllegalArgumentException
     */
    public MenuBar.MenuItem addItem(String caption, MenuBar.Command command) {
        return addItem(caption, null, command);
    }

    /**
     * Add a new item to the menu bar. Icon and command can be null, but a
     * caption must be given.
     * 
     * @param caption
     *            the text for the menu item
     * @param icon
     *            the icon for the menu item
     * @param command
     *            the command for the menu item
     * @throws IllegalArgumentException
     */
    public MenuBar.MenuItem addItem(String caption, Resource icon,
            MenuBar.Command command) {
        if (caption == null) {
            throw new IllegalArgumentException("caption cannot be null");
        }
        MenuItem newItem = new MenuItem(caption, icon, command);
        menuItems.add(newItem);
        requestRepaint();

        return newItem;

    }

    /**
     * Add an item before some item. If the given item does not exist the item
     * is added at the end of the menu. Icon and command can be null, but a
     * caption must be given.
     * 
     * @param caption
     *            the text for the menu item
     * @param icon
     *            the icon for the menu item
     * @param command
     *            the command for the menu item
     * @param itemToAddBefore
     *            the item that will be after the new item
     * @throws IllegalArgumentException
     */
    public MenuBar.MenuItem addItemBefore(String caption, Resource icon,
            MenuBar.Command command, MenuBar.MenuItem itemToAddBefore) {
        if (caption == null) {
            throw new IllegalArgumentException("caption cannot be null");
        }

        MenuItem newItem = new MenuItem(caption, icon, command);
        if (menuItems.contains(itemToAddBefore)) {
            int index = menuItems.indexOf(itemToAddBefore);
            menuItems.add(index, newItem);

        } else {
            menuItems.add(newItem);
        }

        requestRepaint();

        return newItem;
    }

    /**
     * Returns a list with all the MenuItem objects in the menu bar
     * 
     * @return a list containing the MenuItem objects in the menu bar
     */
    public List<MenuItem> getItems() {
        return menuItems;
    }

    /**
     * Remove first occurrence the specified item from the main menu
     * 
     * @param item
     *            The item to be removed
     */
    public void removeItem(MenuBar.MenuItem item) {
        if (item != null) {
            menuItems.remove(item);
        }
        requestRepaint();
    }

    /**
     * Empty the menu bar
     */
    public void removeItems() {
        menuItems.clear();
        requestRepaint();
    }

    /**
     * Returns the size of the menu.
     * 
     * @return The size of the menu
     */
    public int getSize() {
        return menuItems.size();
    }

    /**
     * Set the icon to be used if a sub-menu has children. Defaults to null;
     * 
     * @param icon
     */
    public void setSubmenuIcon(Resource icon) {
        submenuIcon = icon;
        requestRepaint();
    }

    /**
     * Get the icon used for sub-menus. Returns null if no icon is set.
     * 
     * @return
     */
    public Resource getSubmenuIcon() {
        return submenuIcon;
    }

    /**
     * Enable or disable collapsing top-level items. Top-level items will
     * collapse to if there is not enough room for them. Items that don't fit
     * will be placed under the "More" menu item.
     * 
     * Collapsing is enabled by default.
     * 
     * @param collapse
     */
    public void setCollapse(boolean collapse) {
        collapseItems = collapse;
        requestRepaint();
    }

    /**
     * Collapsing is enabled by default.
     * 
     * @return true if the top-level items will be collapsed
     */
    public boolean getCollapse() {
        return collapseItems;
    }

    /**
     * Set the item that is used when collapsing the top level menu. All
     * "overflowing" items will be added below this. The item command will be
     * ignored. If set to null, the default item with the "More" text is be
     * used.
     * 
     * @param item
     */
    public void setMoreMenuItem(MenuItem item) {
        if (item != null) {
            moreItem = item;
        } else {
            moreItem = new MenuItem("More", null, null);
        }
        requestRepaint();
    }

    /**
     * Get the MenuItem used as the collapse menu item.
     * 
     * @return
     */
    public MenuItem getMoreMenuItem() {
        return moreItem;
    }

    /**
     * This interface contains the layer for menu commands of the
     * {@link com.vaadin.ui.MenuBar} class. It's method will fire when the user
     * clicks on the containing {@link com.vaadin.ui.MenuBar.MenuItem}. The
     * selected item is given as an argument.
     */
    public interface Command extends Serializable {
        public void menuSelected(MenuBar.MenuItem selectedItem);
    }

    /**
     * A composite class for menu items and sub-menus. You can set commands to
     * be fired on user click by implementing the
     * {@link com.vaadin.ui.MenuBar.Command} interface. You can also add
     * multiple MenuItems to a MenuItem and create a sub-menu.
     * 
     */
    public class MenuItem implements Serializable {

        /** Private members * */
        private final int itsId;
        private Command itsCommand;
        private String itsText;
        private List<MenuItem> itsChildren;
        private Resource itsIcon;
        private MenuItem itsParent;

        /**
         * Constructs a new menu item that can optionally have an icon and a
         * command associated with it. Icon and command can be null, but a
         * caption must be given.
         * 
         * @param text
         *            The text associated with the command
         * @param command
         *            The command to be fired
         * @throws IllegalArgumentException
         */
        public MenuItem(String caption, Resource icon, MenuBar.Command command) {
            if (caption == null) {
                throw new IllegalArgumentException("caption cannot be null");
            }
            itsId = ++numberOfItems;
            itsText = caption;
            itsIcon = icon;
            itsCommand = command;
        }

        /**
         * Checks if the item has children (if it is a sub-menu).
         * 
         * @return True if this item has children
         */
        public boolean hasChildren() {
            return itsChildren != null;
        }

        /**
         * Add a new item inside this item, thus creating a sub-menu. Command
         * can be null, but a caption must be given.
         * 
         * @param caption
         *            the text for the menu item
         * @param command
         *            the command for the menu item
         */
        public MenuBar.MenuItem addItem(String caption, MenuBar.Command command) {
            return addItem(caption, null, command);
        }

        /**
         * Add a new item inside this item, thus creating a sub-menu. Icon and
         * command can be null, but a caption must be given.
         * 
         * @param caption
         *            the text for the menu item
         * @param icon
         *            the icon for the menu item
         * @param command
         *            the command for the menu item
         */
        public MenuBar.MenuItem addItem(String caption, Resource icon,
                MenuBar.Command command) {
            if (caption == null) {
                throw new IllegalArgumentException("caption cannot be null");
            }

            if (itsChildren == null) {
                itsChildren = new ArrayList<MenuItem>();
            }

            MenuItem newItem = new MenuItem(caption, icon, command);

            // The only place where the parent is set
            newItem.setParent(this);
            itsChildren.add(newItem);

            requestRepaint();

            return newItem;
        }

        /**
         * Add an item before some item. If the given item does not exist the
         * item is added at the end of the menu. Icon and command can be null,
         * but a caption must be given.
         * 
         * @param caption
         *            the text for the menu item
         * @param icon
         *            the icon for the menu item
         * @param command
         *            the command for the menu item
         * @param itemToAddBefore
         *            the item that will be after the new item
         * 
         */
        public MenuBar.MenuItem addItemBefore(String caption, Resource icon,
                MenuBar.Command command, MenuBar.MenuItem itemToAddBefore) {

            MenuItem newItem = null;

            if (hasChildren() && itsChildren.contains(itemToAddBefore)) {
                int index = itsChildren.indexOf(itemToAddBefore);
                newItem = new MenuItem(caption, icon, command);
                newItem.setParent(this);
                itsChildren.add(index, newItem);

            } else {
                newItem = addItem(caption, icon, command);
            }

            requestRepaint();

            return newItem;
        }

        /**
         * For the associated command.
         * 
         * @return The associated command, or null if there is none
         */
        public Command getCommand() {
            return itsCommand;
        }

        /**
         * Gets the objects icon.
         * 
         * @return The icon of the item, null if the item doesn't have an icon
         */
        public Resource getIcon() {
            return itsIcon;
        }

        /**
         * For the containing item. This will return null if the item is in the
         * top-level menu bar.
         * 
         * @return The containing {@link com.vaadin.ui.MenuBar.MenuItem} , or
         *         null if there is none
         */
        public MenuBar.MenuItem getParent() {
            return itsParent;
        }

        /**
         * This will return the children of this item or null if there are none.
         * 
         * @return List of children items, or null if there are none
         */
        public List<MenuItem> getChildren() {
            return itsChildren;
        }

        /**
         * Gets the objects text
         * 
         * @return The text
         */
        public java.lang.String getText() {
            return itsText;
        }

        /**
         * Returns the number of children.
         * 
         * @return The number of child items
         */
        public int getSize() {
            return itsChildren.size();
        }

        /**
         * Get the unique identifier for this item.
         * 
         * @return The id of this item
         */
        public int getId() {
            return itsId;
        }

        /**
         * Set the command for this item. Set null to remove.
         * 
         * @param command
         *            The MenuCommand of this item
         */
        public void setCommand(MenuBar.Command command) {
            itsCommand = command;
        }

        /**
         * Sets the icon. Set null to remove.
         * 
         * @param icon
         *            The icon for this item
         */
        public void setIcon(Resource icon) {
            itsIcon = icon;
            requestRepaint();
        }

        /**
         * Set the text of this object.
         * 
         * @param text
         *            Text for this object
         */
        public void setText(java.lang.String text) {
            if (text != null) {
                itsText = text;
            }
            requestRepaint();
        }

        /**
         * Remove the first occurrence of the item.
         * 
         * @param item
         *            The item to be removed
         */
        public void removeChild(MenuBar.MenuItem item) {
            if (item != null && itsChildren != null) {
                itsChildren.remove(item);
                if (itsChildren.isEmpty()) {
                    itsChildren = null;
                }
            }
            requestRepaint();
        }

        /**
         * Empty the list of children items.
         */
        public void removeChildren() {
            if (itsChildren != null) {
                itsChildren.clear();
                itsChildren = null;
            }
            requestRepaint();
        }

        /**
         * Set the parent of this item. This is called by the addItem method.
         * 
         * @param parent
         *            The parent item
         */
        protected void setParent(MenuBar.MenuItem parent) {
            itsParent = parent;
        }

    }// class MenuItem

}// class MenuBar
