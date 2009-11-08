package com.vaadin.terminal.gwt.client.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.ContainerResizedListener;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class VMenuBar extends Widget implements Paintable,
        CloseHandler<PopupPanel>, ContainerResizedListener {

    /** Set the CSS class name to allow styling. */
    public static final String CLASSNAME = "v-menubar";

    /** For server connections **/
    protected String uidlId;
    protected ApplicationConnection client;

    protected final VMenuBar hostReference = this;
    protected String submenuIcon = null;
    protected CustomMenuItem moreItem = null;
    protected VMenuBar collapsedRootItems;

    // Construct an empty command to be used when the item has no command
    // associated
    protected static final Command emptyCommand = null;

    /** Widget fields **/
    protected boolean subMenu;
    protected ArrayList<CustomMenuItem> items;
    protected Element containerElement;
    protected VOverlay popup;
    protected VMenuBar visibleChildMenu;
    protected VMenuBar parentMenu;
    protected CustomMenuItem selected;

    public VMenuBar() {
        // Create an empty horizontal menubar
        this(false);
    }

    public VMenuBar(boolean subMenu) {
        super();
        setElement(DOM.createDiv());

        items = new ArrayList<CustomMenuItem>();
        popup = null;
        visibleChildMenu = null;

        Element table = DOM.createTable();
        Element tbody = DOM.createTBody();
        DOM.appendChild(getElement(), table);
        DOM.appendChild(table, tbody);

        if (!subMenu) {
            setStyleName(CLASSNAME);
            Element tr = DOM.createTR();
            DOM.appendChild(tbody, tr);
            containerElement = tr;
        } else {
            setStyleName(CLASSNAME + "-submenu");
            containerElement = tbody;
        }
        this.subMenu = subMenu;

        sinkEvents(Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT);
    }

    /**
     * This method must be implemented to update the client-side component from
     * UIDL data received from server.
     * 
     * This method is called when the page is loaded for the first time, and
     * every time UI changes in the component are received from the server.
     */
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        // This call should be made first. Ensure correct implementation,
        // and let the containing layout manage caption, etc.
        if (client.updateComponent(this, uidl, true)) {
            return;
        }

        // For future connections
        this.client = client;
        uidlId = uidl.getId();

        // Empty the menu every time it receives new information
        if (!getItems().isEmpty()) {
            clearItems();
        }

        UIDL options = uidl.getChildUIDL(0);

        if (options.hasAttribute("submenuIcon")) {
            submenuIcon = client.translateVaadinUri(uidl.getChildUIDL(0)
                    .getStringAttribute("submenuIcon"));
        } else {
            submenuIcon = null;
        }

        if (uidl.hasAttribute("width")) {
            UIDL moreItemUIDL = options.getChildUIDL(0);
            StringBuffer itemHTML = new StringBuffer();

            if (moreItemUIDL.hasAttribute("icon")) {
                itemHTML.append("<img src=\""
                        + client.translateVaadinUri(moreItemUIDL
                                .getStringAttribute("icon")) + "\" class=\""
                        + Icon.CLASSNAME + "\" alt=\"\" />");
            }
            itemHTML.append(moreItemUIDL.getStringAttribute("text"));

            moreItem = new CustomMenuItem(itemHTML.toString(), emptyCommand);
            collapsedRootItems = new VMenuBar(true);
            moreItem.setSubMenu(collapsedRootItems);
            moreItem.addStyleName(CLASSNAME + "-more-menuitem");
        }

        UIDL uidlItems = uidl.getChildUIDL(1);
        Iterator<Object> itr = uidlItems.getChildIterator();
        Stack<Iterator<Object>> iteratorStack = new Stack<Iterator<Object>>();
        Stack<VMenuBar> menuStack = new Stack<VMenuBar>();
        VMenuBar currentMenu = this;

        while (itr.hasNext()) {
            UIDL item = (UIDL) itr.next();
            CustomMenuItem currentItem = null;

            String itemText = item.getStringAttribute("text");
            final int itemId = item.getIntAttribute("id");

            boolean itemHasCommand = item.getBooleanAttribute("command");

            // Construct html from the text and the optional icon
            StringBuffer itemHTML = new StringBuffer();

            if (item.hasAttribute("icon")) {
                itemHTML.append("<img src=\""
                        + client.translateVaadinUri(item
                                .getStringAttribute("icon")) + "\" class=\""
                        + Icon.CLASSNAME + "\" alt=\"\" />");
            }

            itemHTML.append(itemText);

            // Add submenu indicator
            if (item.getChildCount() > 0) {
                // FIXME For compatibility reasons: remove in version 7
                String bgStyle = "";
                if (submenuIcon != null) {
                    bgStyle = " style=\"background-image: url(" + submenuIcon
                            + "); text-indent: -999px; width: 1em;\"";
                }
                itemHTML
                        .append("<span class=\"" + CLASSNAME
                                + "-submenu-indicator\"" + bgStyle
                                + ">&#x25B6;</span>");
            }

            Command cmd = null;

            if (itemHasCommand) {
                // Construct a command that fires onMenuClick(int) with the
                // item's id-number
                cmd = new Command() {
                    public void execute() {
                        hostReference.onMenuClick(itemId);
                    }
                };
            }

            currentItem = currentMenu.addItem(itemHTML.toString(), cmd);

            if (item.getChildCount() > 0) {
                menuStack.push(currentMenu);
                iteratorStack.push(itr);
                itr = item.getChildIterator();
                currentMenu = new VMenuBar(true);
                currentItem.setSubMenu(currentMenu);
            }

            while (!itr.hasNext() && !iteratorStack.empty()) {
                itr = iteratorStack.pop();
                currentMenu = menuStack.pop();
            }
        }// while

        iLayout();

    }// updateFromUIDL

    /**
     * This is called by the items in the menu and it communicates the
     * information to the server
     * 
     * @param clickedItemId
     *            id of the item that was clicked
     */
    public void onMenuClick(int clickedItemId) {
        // Updating the state to the server can not be done before
        // the server connection is known, i.e., before updateFromUIDL()
        // has been called.
        if (uidlId != null && client != null) {
            // Communicate the user interaction parameters to server. This call
            // will initiate an AJAX request to the server.
            client.updateVariable(uidlId, "clickedId", clickedItemId, true);
        }
    }

    /** Widget methods **/

    /**
     * Returns a list of items in this menu
     */
    public List<CustomMenuItem> getItems() {
        return items;
    }

    /**
     * Remove all the items in this menu
     */
    public void clearItems() {
        Element e = getContainingElement();
        while (DOM.getChildCount(e) > 0) {
            DOM.removeChild(e, DOM.getChild(e, 0));
        }
        items.clear();
    }

    /**
     * Returns the containing element of the menu
     * 
     * @return
     */
    public Element getContainingElement() {
        return containerElement;
    }

    /**
     * Returns a new child element to add an item to
     * 
     * @param index
     *            the index in which point to add a new element in a submenu. -1
     *            will add the new element as the last child (append)
     * 
     * @return
     */
    public Element getNewChildElement(int index) {
        if (subMenu) {
            Element tr = DOM.createTR();
            if (index == -1) {
                DOM.appendChild(getContainingElement(), tr);
            } else {
                DOM.insertChild(getContainingElement(), tr, index);
            }
            return tr;
        } else {
            return getContainingElement();
        }
    }

    /**
     * Add a new item to this menu
     * 
     * @param html
     *            items text
     * @param cmd
     *            items command
     * @return the item created
     */
    public CustomMenuItem addItem(String html, Command cmd) {
        CustomMenuItem item = new CustomMenuItem(html, cmd);
        addItem(item);
        return item;
    }

    /**
     * Add a new item to this menu
     * 
     * @param item
     */
    public void addItem(CustomMenuItem item) {
        if (items.contains(item)) {
            return;
        }
        DOM.appendChild(getNewChildElement(-1), item.getElement());
        item.setParentMenu(this);
        item.setSelected(false);
        items.add(item);
    }

    public void addItem(CustomMenuItem item, int index) {
        if (items.contains(item)) {
            return;
        }
        if (subMenu) {
            DOM.appendChild(getNewChildElement(index), item.getElement());
        } else {
            DOM.insertChild(getNewChildElement(-1), item.getElement(), index);
        }
        item.setParentMenu(this);
        item.setSelected(false);
        items.add(index, item);
    }

    /**
     * Remove the given item from this menu
     * 
     * @param item
     */
    public void removeItem(CustomMenuItem item) {
        if (items.contains(item)) {
            int index = items.indexOf(item);
            Element container = getContainingElement();

            DOM.removeChild(container, DOM.getChild(container, index));
            items.remove(index);
        }
    }

    /*
     * @see
     * com.google.gwt.user.client.ui.Widget#onBrowserEvent(com.google.gwt.user
     * .client.Event)
     */
    @Override
    public void onBrowserEvent(Event e) {
        super.onBrowserEvent(e);

        Element targetElement = DOM.eventGetTarget(e);
        CustomMenuItem targetItem = null;
        for (int i = 0; i < items.size(); i++) {
            CustomMenuItem item = items.get(i);
            if (DOM.isOrHasChild(item.getElement(), targetElement)) {
                targetItem = item;
            }
        }

        if (targetItem != null) {
            switch (DOM.eventGetType(e)) {

            case Event.ONCLICK:
                itemClick(targetItem);
                break;

            case Event.ONMOUSEOVER:
                itemOver(targetItem);
                break;

            case Event.ONMOUSEOUT:
                itemOut(targetItem);
                break;
            }
        }
    }

    /**
     * When an item is clicked
     * 
     * @param item
     */
    public void itemClick(CustomMenuItem item) {
        if (item.getCommand() != null) {
            setSelected(null);

            if (visibleChildMenu != null) {
                visibleChildMenu.hideChildren();
            }

            hideParents();
            DeferredCommand.addCommand(item.getCommand());

        } else {
            if (item.getSubMenu() != null
                    && item.getSubMenu() != visibleChildMenu) {
                setSelected(item);
                showChildMenu(item);
            }
        }
    }

    /**
     * When the user hovers the mouse over the item
     * 
     * @param item
     */
    public void itemOver(CustomMenuItem item) {
        setSelected(item);

        boolean menuWasVisible = visibleChildMenu != null;

        if (menuWasVisible && visibleChildMenu != item.getSubMenu()) {
            popup.hide();
        }

        if (item.getSubMenu() != null && (parentMenu != null || menuWasVisible)
                && visibleChildMenu != item.getSubMenu()) {
            showChildMenu(item);
        }
    }

    /**
     * When the mouse is moved away from an item
     * 
     * @param item
     */
    public void itemOut(CustomMenuItem item) {
        if (visibleChildMenu != item.getSubMenu() || visibleChildMenu == null) {
            hideChildMenu(item);
            setSelected(null);
        }
    }

    /**
     * Shows the child menu of an item. The caller must ensure that the item has
     * a submenu.
     * 
     * @param item
     */
    public void showChildMenu(CustomMenuItem item) {
        popup = new VOverlay(true, false, true);
        popup.setWidget(item.getSubMenu());
        popup.addCloseHandler(this);
        int left = 0;
        int top = 0;
        if (subMenu) {
            left = item.getParentMenu().getAbsoluteLeft()
                    + item.getParentMenu().getOffsetWidth();
            top = item.getAbsoluteTop();
        } else {
            left = item.getAbsoluteLeft();
            top = item.getParentMenu().getAbsoluteTop()
                    + item.getParentMenu().getOffsetHeight();
        }
        popup.setPopupPosition(left, top);

        item.getSubMenu().onShow();
        visibleChildMenu = item.getSubMenu();
        item.getSubMenu().setParentMenu(this);

        popup.show();

        if (left + popup.getOffsetWidth() >= RootPanel.getBodyElement()
                .getOffsetWidth()) {
            if (subMenu) {
                left = item.getParentMenu().getAbsoluteLeft()
                        - popup.getOffsetWidth();
            } else {
                left = RootPanel.getBodyElement().getOffsetWidth()
                        - popup.getOffsetWidth();
                ApplicationConnection.getConsole().log("" + left);
            }
            popup.setPopupPosition(left, top);
        }
    }

    /**
     * Hides the submenu of an item
     * 
     * @param item
     */
    public void hideChildMenu(CustomMenuItem item) {
        if (visibleChildMenu != null
                && !(visibleChildMenu == item.getSubMenu())) {
            popup.hide();

        }
    }

    /**
     * When the menu is shown.
     */
    public void onShow() {
        // remove possible previous selection
        if (selected != null) {
            selected.setSelected(false);
            selected = null;
        }
    }

    /**
     * Recursively hide all child menus
     */
    public void hideChildren() {
        if (visibleChildMenu != null) {
            visibleChildMenu.hideChildren();
            popup.hide();
        }
    }

    /**
     * Recursively hide all parent menus
     */
    public void hideParents() {

        if (visibleChildMenu != null) {
            popup.hide();
            setSelected(null);
        }

        if (getParentMenu() != null) {
            getParentMenu().hideParents();
        }
    }

    /**
     * Returns the parent menu of this menu, or null if this is the top-level
     * menu
     * 
     * @return
     */
    public VMenuBar getParentMenu() {
        return parentMenu;
    }

    /**
     * Set the parent menu of this menu
     * 
     * @param parent
     */
    public void setParentMenu(VMenuBar parent) {
        parentMenu = parent;
    }

    /**
     * Returns the currently selected item of this menu, or null if nothing is
     * selected
     * 
     * @return
     */
    public CustomMenuItem getSelected() {
        return selected;
    }

    /**
     * Set the currently selected item of this menu
     * 
     * @param item
     */
    public void setSelected(CustomMenuItem item) {
        // If we had something selected, unselect
        if (item != selected && selected != null) {
            selected.setSelected(false);
        }
        // If we have a valid selection, select it
        if (item != null) {
            item.setSelected(true);
        }

        selected = item;
    }

    /**
     * Listener method, fired when this menu is closed
     */
    public void onClose(CloseEvent<PopupPanel> event) {
        hideChildren();
        if (event.isAutoClosed()) {
            hideParents();
        }
        visibleChildMenu = null;
        popup = null;

    }

    /**
     * 
     * A class to hold information on menu items
     * 
     */
    private class CustomMenuItem extends UIObject implements HasHTML {

        protected String html = null;
        protected Command command = null;
        protected VMenuBar subMenu = null;
        protected VMenuBar parentMenu = null;

        public CustomMenuItem(String html, Command cmd) {
            setElement(DOM.createTD());

            setHTML(html);
            setCommand(cmd);
            setSelected(false);

            addStyleName("menuitem");
        }

        public void setSelected(boolean selected) {
            if (selected) {
                addStyleDependentName("selected");
            } else {
                removeStyleDependentName("selected");
            }
        }

        /*
         * setters and getters for the fields
         */

        public void setSubMenu(VMenuBar subMenu) {
            this.subMenu = subMenu;
        }

        public VMenuBar getSubMenu() {
            return subMenu;
        }

        public void setParentMenu(VMenuBar parentMenu) {
            this.parentMenu = parentMenu;
        }

        public VMenuBar getParentMenu() {
            return parentMenu;
        }

        public void setCommand(Command command) {
            this.command = command;
        }

        public Command getCommand() {
            return command;
        }

        public String getHTML() {
            return html;
        }

        public void setHTML(String html) {
            this.html = html;
            DOM.setInnerHTML(getElement(), html);
        }

        public String getText() {
            return html;
        }

        public void setText(String text) {
            setHTML(text);

        }
    }

    /**
     * @author Jouni Koivuviita / IT Mill Ltd.
     */

    public void iLayout() {
        // Only collapse if there is more than one item in the root menu and the
        // menu has an explicit size
        if ((getItems().size() > 1 || collapsedRootItems.getItems().size() > 0)
                && getElement().getStyle().getProperty("width") != null) {

            // Measure the width of the "more" item
            final boolean morePresent = getItems().contains(moreItem);
            addItem(moreItem);
            final int moreItemWidth = moreItem.getOffsetWidth();
            if (!morePresent) {
                removeItem(moreItem);
            }

            // Measure available space
            int availableWidth = getElement().getClientWidth();
            final int rootWidth = getElement().getFirstChildElement()
                    .getOffsetWidth();
            int diff = availableWidth - rootWidth;

            removeItem(moreItem);

            if (diff < 0) {
                // Too many items: collapse last items from root menu
                final int widthNeeded = moreItemWidth - diff;
                int widthReduced = 0;

                while (widthReduced < widthNeeded && getItems().size() > 0) {
                    // Move last root menu item to collapsed menu
                    CustomMenuItem collapse = getItems().get(
                            getItems().size() - 1);
                    widthReduced += collapse.getOffsetWidth();
                    removeItem(collapse);
                    collapsedRootItems.addItem(collapse, 0);
                }
            } else if (collapsedRootItems.getItems().size() > 0) {
                // Space available for items: expand first items from collapsed
                // menu
                int widthAvailable = diff + moreItemWidth;
                int widthGrowth = 0;

                while (widthAvailable > widthGrowth) {
                    // Move first item from collapsed menu to the root menu
                    CustomMenuItem expand = collapsedRootItems.getItems()
                            .get(0);
                    collapsedRootItems.removeItem(expand);
                    addItem(expand);
                    widthGrowth += expand.getOffsetWidth();
                    if (collapsedRootItems.getItems().size() > 0) {
                        widthAvailable -= moreItemWidth;
                    }
                    if (widthGrowth > widthAvailable) {
                        removeItem(expand);
                        collapsedRootItems.addItem(expand, 0);
                    } else {
                        widthAvailable = diff;
                    }
                }
            }
            if (collapsedRootItems.getItems().size() > 0) {
                addItem(moreItem);
            }
        }
    }

}
