/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.Util;
import com.vaadin.terminal.gwt.client.ValueMap;
import com.vaadin.terminal.gwt.client.ui.dd.VAbstractDropHandler;
import com.vaadin.terminal.gwt.client.ui.dd.VAcceptCallback;
import com.vaadin.terminal.gwt.client.ui.dd.VDragAndDropManager;
import com.vaadin.terminal.gwt.client.ui.dd.VDragEvent;
import com.vaadin.terminal.gwt.client.ui.dd.VDropHandler;
import com.vaadin.terminal.gwt.client.ui.dd.VHasDropHandler;
import com.vaadin.terminal.gwt.client.ui.dd.VTransferable;
import com.vaadin.terminal.gwt.client.ui.dd.VDragAndDropManager.DragEventType;

/**
 * 
 */
public class VTree extends FlowPanel implements Paintable, VHasDropHandler {

    public static final String CLASSNAME = "v-tree";

    public static final String ITEM_CLICK_EVENT_ID = "itemClick";

    private Set<String> selectedIds = new HashSet<String>();
    private ApplicationConnection client;
    private String paintableId;
    private boolean selectable;
    private boolean isMultiselect;
    private String currentMouseOverKey;

    private final HashMap<String, TreeNode> keyToNode = new HashMap<String, TreeNode>();

    /**
     * This map contains captions and icon urls for actions like: * "33_c" ->
     * "Edit" * "33_i" -> "http://dom.com/edit.png"
     */
    private final HashMap<String, String> actionMap = new HashMap<String, String>();

    private boolean immediate;

    private boolean isNullSelectionAllowed = true;

    private boolean disabled = false;

    private boolean readonly;

    private boolean rendering;

    private int dragModes;

    private VAbstractDropHandler dropHandler;

    public VTree() {
        super();
        setStyleName(CLASSNAME);
    }

    private void updateActionMap(UIDL c) {
        final Iterator it = c.getChildIterator();
        while (it.hasNext()) {
            final UIDL action = (UIDL) it.next();
            final String key = action.getStringAttribute("key");
            final String caption = action.getStringAttribute("caption");
            actionMap.put(key + "_c", caption);
            if (action.hasAttribute("icon")) {
                // TODO need some uri handling ??
                actionMap.put(key + "_i", client.translateVaadinUri(action
                        .getStringAttribute("icon")));
            }
        }

    }

    public String getActionCaption(String actionKey) {
        return actionMap.get(actionKey + "_c");
    }

    public String getActionIcon(String actionKey) {
        return actionMap.get(actionKey + "_i");
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        // Ensure correct implementation and let container manage caption
        if (client.updateComponent(this, uidl, true)) {
            return;
        }

        rendering = true;

        this.client = client;

        if (uidl.hasAttribute("partialUpdate")) {
            handleUpdate(uidl);
            rendering = false;
            return;
        }

        paintableId = uidl.getId();

        immediate = uidl.hasAttribute("immediate");

        disabled = uidl.getBooleanAttribute("disabled");
        readonly = uidl.getBooleanAttribute("readonly");

        isNullSelectionAllowed = uidl.getBooleanAttribute("nullselect");

        clear();
        for (final Iterator i = uidl.getChildIterator(); i.hasNext();) {
            final UIDL childUidl = (UIDL) i.next();
            if ("actions".equals(childUidl.getTag())) {
                updateActionMap(childUidl);
                continue;
            } else if ("dh".equals(childUidl.getTag())) {
                updateDropHandler(childUidl);
                continue;
            }
            final TreeNode childTree = new TreeNode();
            if (childTree.ie6compatnode != null) {
                this.add(childTree);
            }
            childTree.updateFromUIDL(childUidl, client);
            if (childTree.ie6compatnode == null) {
                this.add(childTree);
            }
        }
        final String selectMode = uidl.getStringAttribute("selectmode");
        selectable = !"none".equals(selectMode);
        isMultiselect = "multi".equals(selectMode);

        selectedIds = uidl.getStringArrayVariableAsSet("selected");

        if (uidl.hasAttribute("dragModes")) {
            dragModes = uidl.getIntAttribute("dragModes");
        }

        rendering = false;

    }

    private void updateTreeRelatedDragData(VDragEvent drag) {

        currentMouseOverKey = findCurrentMouseOverKey(drag.getElementOver());

        drag.getDropDetails().put("itemIdOver", currentMouseOverKey);

        if (currentMouseOverKey != null) {
            String detail = getDropDetail(drag.getCurrentGwtEvent());
            Boolean overTreeNode = null;
            if (!keyToNode.get(currentMouseOverKey).isLeaf()
                    && "Center".equals(detail)) {
                overTreeNode = true;
            }
            drag.getDropDetails().put("itemIdOverIsNode", overTreeNode);
            drag.getDropDetails().put("detail", detail);

        }
    }

    private String findCurrentMouseOverKey(Element elementOver) {
        TreeNode treeNode = null;
        Element curEl = elementOver;
        while (curEl != null) {
            try {
                EventListener eventListener = Event.getEventListener(curEl);
                if (eventListener != null) {
                    // found a widget
                    if (eventListener instanceof TreeNode) {
                        treeNode = (TreeNode) eventListener;
                    }
                    break;
                } else {
                    curEl = (Element) curEl.getParentElement();
                }
            } catch (Exception e) {
                ApplicationConnection.getConsole().log(e.getMessage());
                e.printStackTrace();
            }
        }
        return treeNode == null ? null : treeNode.key;
    }

    private void updateDropHandler(UIDL childUidl) {
        if (dropHandler == null) {
            dropHandler = new VAbstractDropHandler() {

                @Override
                public void dragEnter(VDragEvent drag) {
                    updateTreeRelatedDragData(drag);
                    super.dragEnter(drag);
                }

                @Override
                protected void dragAccepted(final VDragEvent drag) {
                }

                @Override
                public void dragOver(final VDragEvent currentDrag) {
                    final Object oldIdOver = currentDrag.getDropDetails().get(
                            "itemIdOver");
                    final String oldDetail = (String) currentDrag
                            .getDropDetails().get("detail");

                    updateTreeRelatedDragData(currentDrag);
                    final String detail = getDropDetail(currentDrag
                            .getCurrentGwtEvent());
                    boolean nodeHasChanged = (currentMouseOverKey != null && currentMouseOverKey != oldIdOver)
                            || (oldIdOver != null);
                    boolean detailHasChanded = !detail.equals(oldDetail);

                    if (nodeHasChanged || detailHasChanded) {
                        ApplicationConnection.getConsole().log(
                                "Change in Transferable " + currentMouseOverKey
                                        + " " + detail);
                        VAcceptCallback accpectedCb = new VAcceptCallback() {
                            public void handleResponse(ValueMap responseData) {
                                if (responseData == null // via client
                                        // side
                                        // validation
                                        || responseData.containsKey("accepted")) {
                                    keyToNode.get(currentMouseOverKey)
                                            .emphasis(detail);
                                }
                            }
                        };
                        if (validateOnServer()) {
                            VDragAndDropManager.get().visitServer(
                                    DragEventType.OVER, accpectedCb);

                        } else {
                            if (validates(currentDrag)) {
                                accpectedCb.handleResponse(null);
                            } else {
                                keyToNode.get(currentMouseOverKey).emphasis(
                                        null);
                            }
                            if (oldIdOver != null
                                    && oldIdOver != currentMouseOverKey) {
                                keyToNode.get(oldIdOver).emphasis(null);
                            }
                        }
                    }

                }

                @Override
                public void dragLeave(VDragEvent drag) {
                    cleanUp();
                }

                private void cleanUp() {
                    if (currentMouseOverKey != null) {
                        keyToNode.get(currentMouseOverKey).emphasis(null);
                        currentMouseOverKey = null;
                    }
                }

                @Override
                public boolean drop(VDragEvent drag) {
                    cleanUp();
                    return super.drop(drag);
                }

                @Override
                public Paintable getPaintable() {
                    return VTree.this;
                }

                public ApplicationConnection getApplicationConnection() {
                    return client;
                }

            };
        }
        dropHandler.updateRules(childUidl);
    }

    public String getDropDetail(NativeEvent event) {
        TreeNode treeNode = keyToNode.get(currentMouseOverKey);
        // TODO no scroll support
        int offsetHeight = treeNode.nodeCaptionDiv.getOffsetHeight();
        int absoluteTop = treeNode.getAbsoluteTop();
        int clientY = event.getClientY();
        int fromTop = clientY - absoluteTop;

        String detail;
        float percentageFromTop = (fromTop / (float) offsetHeight);
        if (percentageFromTop < 0.2) {
            detail = "Top";
        } else if (percentageFromTop > 0.8) {
            detail = "Bottom";
        } else {
            detail = "Center";
        }

        return detail;

    }

    private void handleUpdate(UIDL uidl) {
        final TreeNode rootNode = keyToNode.get(uidl
                .getStringAttribute("rootKey"));
        if (rootNode != null) {
            if (!rootNode.getState()) {
                // expanding node happened server side
                rootNode.setState(true, false);
            }
            rootNode.renderChildNodes(uidl.getChildIterator());
        }
    }

    public void setSelected(TreeNode treeNode, boolean selected) {
        if (selected) {
            if (!isMultiselect) {
                while (selectedIds.size() > 0) {
                    final String id = selectedIds.iterator().next();
                    final TreeNode oldSelection = keyToNode.get(id);
                    if (oldSelection != null) {
                        // can be null if the node is not visible (parent
                        // collapsed)
                        oldSelection.setSelected(false);
                    }
                    selectedIds.remove(id);
                }
            }
            treeNode.setSelected(true);
            selectedIds.add(treeNode.key);
        } else {
            if (!isNullSelectionAllowed) {
                if (!isMultiselect || selectedIds.size() == 1) {
                    return;
                }
            }
            selectedIds.remove(treeNode.key);
            treeNode.setSelected(false);
        }

        client.updateVariable(paintableId, "selected", selectedIds
                .toArray(new String[selectedIds.size()]), immediate);
    }

    public boolean isSelected(TreeNode treeNode) {
        return selectedIds.contains(treeNode.key);
    }

    protected class TreeNode extends SimplePanel implements ActionOwner {

        public static final String CLASSNAME = "v-tree-node";

        String key;

        private String[] actionKeys = null;

        private boolean childrenLoaded;

        private Element nodeCaptionDiv;

        protected Element nodeCaptionSpan;

        private FlowPanel childNodeContainer;

        private boolean open;

        private Icon icon;

        private Element ie6compatnode;

        private Event mouseDownEvent;

        public TreeNode() {
            constructDom();
            sinkEvents(Event.ONCLICK | Event.ONDBLCLICK | Event.MOUSEEVENTS
                    | Event.ONCONTEXTMENU);
        }

        public void emphasis(String string) {
            // ApplicationConnection.getConsole().log("OUTLINE" + string);
            Style style = nodeCaptionDiv.getStyle();
            String top = "Top".equals(string) ? "2px solid green" : "";
            String bottom = "Bottom".equals(string) ? "2px solid green" : "";
            String bg = "Center".equals(string) ? "green" : "";

            style.setProperty("borderTop", top);
            style.setProperty("borderBottom", bottom);
            style.setBackgroundColor(bg);

        }

        @Override
        public void onBrowserEvent(Event event) {
            super.onBrowserEvent(event);
            if (disabled) {
                return;
            }
            final int type = DOM.eventGetType(event);
            final Element target = DOM.eventGetTarget(event);
            if (client.hasEventListeners(VTree.this, ITEM_CLICK_EVENT_ID)
                    && target == nodeCaptionSpan
                    && (type == Event.ONDBLCLICK || type == Event.ONMOUSEUP)) {
                fireClick(event);
            }
            if (type == Event.ONCLICK) {
                if (getElement() == target || ie6compatnode == target) {
                    // state change
                    toggleState();
                } else if (!readonly && target == nodeCaptionSpan) {
                    // caption click = selection change && possible click event
                    toggleSelection();
                }
                DOM.eventCancelBubble(event, true);
            } else if (type == Event.ONCONTEXTMENU) {
                showContextMenu(event);
            }

            if (dragModes != 0 || dropHandler != null) {
                if (type == Event.ONMOUSEDOWN) {
                    if (nodeCaptionDiv.isOrHasChild(event.getTarget())) {
                        ApplicationConnection.getConsole().log(
                                "TreeNode m down");
                        event.preventDefault(); // prevent text selection
                        mouseDownEvent = event;
                    }
                } else if (type == Event.ONMOUSEMOVE
                        || type == Event.ONMOUSEOUT) {

                    if (mouseDownEvent != null) {
                        ApplicationConnection.getConsole().log(
                                "TreeNode drag start " + event.getType());
                        // start actual drag on slight move when mouse is down
                        VTransferable t = new VTransferable();
                        t.setComponent(VTree.this);
                        t.setItemId(key);
                        VDragEvent drag = VDragAndDropManager.get().startDrag(
                                t, mouseDownEvent, true);

                        drag.createDragImage(nodeCaptionDiv, true);
                        event.stopPropagation();

                        mouseDownEvent = null;
                    }
                } else if (type == Event.ONMOUSEUP) {
                    mouseDownEvent = null;
                }
                if (type == Event.ONMOUSEOVER) {
                    ApplicationConnection.getConsole().log(
                            "Treenode mouse over");
                    mouseDownEvent = null;
                    currentMouseOverKey = key;
                    event.stopPropagation();
                }

            }
        }

        private void fireClick(Event evt) {
            // non-immediate iff an immediate select event is going to happen
            boolean imm = !immediate
                    || !selectable
                    || (!isNullSelectionAllowed && isSelected() && selectedIds
                            .size() == 1);
            MouseEventDetails details = new MouseEventDetails(evt);
            client.updateVariable(paintableId, "clickedKey", key, false);
            client.updateVariable(paintableId, "clickEvent",
                    details.toString(), imm);
        }

        private void toggleSelection() {
            if (selectable) {
                VTree.this.setSelected(this, !isSelected());
            }
        }

        private void toggleState() {
            setState(!getState(), true);
        }

        protected void constructDom() {
            // workaround for a very weird IE6 issue #1245
            if (BrowserInfo.get().isIE6()) {
                ie6compatnode = DOM.createDiv();
                setStyleName(ie6compatnode, CLASSNAME + "-ie6compatnode");
                DOM.setInnerText(ie6compatnode, " ");
                DOM.appendChild(getElement(), ie6compatnode);

                DOM.sinkEvents(ie6compatnode, Event.ONCLICK);
            }

            nodeCaptionDiv = DOM.createDiv();
            DOM.setElementProperty(nodeCaptionDiv, "className", CLASSNAME
                    + "-caption");
            Element wrapper = DOM.createDiv();
            nodeCaptionSpan = DOM.createSpan();
            DOM.appendChild(getElement(), nodeCaptionDiv);
            DOM.appendChild(nodeCaptionDiv, wrapper);
            DOM.appendChild(wrapper, nodeCaptionSpan);

            childNodeContainer = new FlowPanel();
            childNodeContainer.setStylePrimaryName(CLASSNAME + "-children");
            setWidget(childNodeContainer);
        }

        public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
            setText(uidl.getStringAttribute("caption"));
            key = uidl.getStringAttribute("key");

            keyToNode.put(key, this);

            if (uidl.hasAttribute("al")) {
                actionKeys = uidl.getStringArrayAttribute("al");
            }

            if (uidl.getTag().equals("node")) {
                if (uidl.getChildCount() == 0) {
                    childNodeContainer.setVisible(false);
                } else {
                    renderChildNodes(uidl.getChildIterator());
                    childrenLoaded = true;
                }
            } else {
                addStyleName(CLASSNAME + "-leaf");
            }
            addStyleName(CLASSNAME);
            if (uidl.hasAttribute("style")) {
                addStyleName(CLASSNAME + "-" + uidl.getStringAttribute("style"));
                Widget.setStyleName(nodeCaptionDiv, CLASSNAME + "-caption-"
                        + uidl.getStringAttribute("style"), true);
                childNodeContainer.addStyleName(CLASSNAME + "-children-"
                        + uidl.getStringAttribute("style"));
            }

            if (uidl.getBooleanAttribute("expanded") && !getState()) {
                setState(true, false);
            }

            if (uidl.getBooleanAttribute("selected")) {
                setSelected(true);
                // ensure that identifier is in selectedIds array (this may be a
                // partial update)
                selectedIds.add(key);
            }

            if (uidl.hasAttribute("icon")) {
                if (icon == null) {
                    icon = new Icon(client);
                    DOM.insertBefore(DOM.getFirstChild(nodeCaptionDiv), icon
                            .getElement(), nodeCaptionSpan);
                }
                icon.setUri(uidl.getStringAttribute("icon"));
            } else {
                if (icon != null) {
                    DOM.removeChild(DOM.getFirstChild(nodeCaptionDiv), icon
                            .getElement());
                    icon = null;
                }
            }

            if (BrowserInfo.get().isIE6() && isAttached()) {
                fixWidth();
            }
        }

        public boolean isLeaf() {
            return getStyleName().contains("leaf");
        }

        private void setState(boolean state, boolean notifyServer) {
            if (open == state) {
                return;
            }
            if (state) {
                if (!childrenLoaded && notifyServer) {
                    client.updateVariable(paintableId, "requestChildTree",
                            true, false);
                }
                if (notifyServer) {
                    client.updateVariable(paintableId, "expand",
                            new String[] { key }, true);
                }
                addStyleName(CLASSNAME + "-expanded");
                childNodeContainer.setVisible(true);

            } else {
                removeStyleName(CLASSNAME + "-expanded");
                childNodeContainer.setVisible(false);
                if (notifyServer) {
                    client.updateVariable(paintableId, "collapse",
                            new String[] { key }, true);
                }
            }
            open = state;

            if (!rendering) {
                Util.notifyParentOfSizeChange(VTree.this, false);
            }
        }

        private boolean getState() {
            return open;
        }

        private void setText(String text) {
            DOM.setInnerText(nodeCaptionSpan, text);
        }

        private void renderChildNodes(Iterator i) {
            childNodeContainer.clear();
            childNodeContainer.setVisible(true);
            while (i.hasNext()) {
                final UIDL childUidl = (UIDL) i.next();
                // actions are in bit weird place, don't mix them with children,
                // but current node's actions
                if ("actions".equals(childUidl.getTag())) {
                    updateActionMap(childUidl);
                    continue;
                }
                final TreeNode childTree = new TreeNode();
                if (ie6compatnode != null) {
                    childNodeContainer.add(childTree);
                }
                childTree.updateFromUIDL(childUidl, client);
                if (ie6compatnode == null) {
                    childNodeContainer.add(childTree);
                }
            }
            childrenLoaded = true;
        }

        public boolean isChildrenLoaded() {
            return childrenLoaded;
        }

        public Action[] getActions() {
            if (actionKeys == null) {
                return new Action[] {};
            }
            final Action[] actions = new Action[actionKeys.length];
            for (int i = 0; i < actions.length; i++) {
                final String actionKey = actionKeys[i];
                final TreeAction a = new TreeAction(this, String.valueOf(key),
                        actionKey);
                a.setCaption(getActionCaption(actionKey));
                a.setIconUrl(getActionIcon(actionKey));
                actions[i] = a;
            }
            return actions;
        }

        public ApplicationConnection getClient() {
            return client;
        }

        public String getPaintableId() {
            return paintableId;
        }

        /**
         * Adds/removes Vaadin specific style name. This method ought to be
         * called only from VTree.
         * 
         * @param selected
         */
        protected void setSelected(boolean selected) {
            // add style name to caption dom structure only, not to subtree
            setStyleName(nodeCaptionDiv, "v-tree-node-selected", selected);
        }

        protected boolean isSelected() {
            return VTree.this.isSelected(this);
        }

        public void showContextMenu(Event event) {
            if (!readonly && !disabled) {
                if (actionKeys != null) {
                    int left = event.getClientX();
                    int top = event.getClientY();
                    top += Window.getScrollTop();
                    left += Window.getScrollLeft();
                    client.getContextMenu().showAt(this, left, top);
                }
                event.cancelBubble(true);
                event.preventDefault();
            }
        }

        /*
         * We need to fix the width of TreeNodes so that the float in
         * ie6compatNode does not wrap (see ticket #1245)
         */
        private void fixWidth() {
            nodeCaptionDiv.getStyle().setProperty("styleFloat", "left");
            nodeCaptionDiv.getStyle().setProperty("display", "inline");
            nodeCaptionDiv.getStyle().setProperty("marginLeft", "0");
            final int captionWidth = ie6compatnode.getOffsetWidth()
                    + nodeCaptionDiv.getOffsetWidth();
            setWidth(captionWidth + "px");
        }

        @Override
        public void onAttach() {
            super.onAttach();
            if (ie6compatnode != null) {
                fixWidth();
            }
        }
    }

    public VDropHandler getDropHandler() {
        return dropHandler;
    }
}
