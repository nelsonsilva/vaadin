package com.itmill.toolkit.terminal.gwt.client.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.itmill.toolkit.terminal.gwt.client.ApplicationConnection;
import com.itmill.toolkit.terminal.gwt.client.Paintable;
import com.itmill.toolkit.terminal.gwt.client.UIDL;
import com.itmill.toolkit.terminal.gwt.client.Util;

/**
 * TODO dump GWT's Tree implementation and use Toolkit 4 style
 * TODO update node close/opens to server (even if no content fetch is needed)
 * 
 * DOM structure
 *
 */
public class ITree extends Tree implements Paintable {
	
	public static final String CLASSNAME = "i-tree";

	Set selectedIds = new HashSet();
	ApplicationConnection client;
	String paintableId;
	private boolean selectable;
	private boolean multiselect;
	
	private HashMap keyToNode = new HashMap();
	
	/**
	 * This map contains captions and icon urls for 
	 * actions like:
	 *   * "33_c" -> "Edit"
	 *   * "33_i" -> "http://dom.com/edit.png"
	 */
	private HashMap actionMap = new HashMap();

	private boolean immediate;

	
	public ITree() {
		super();
		setStyleName(CLASSNAME);
	}
	
	private void updateActionMap(UIDL c) {
		Iterator it = c.getChildIterator();
		while(it.hasNext()) {
			UIDL action = (UIDL) it.next();
			String key = action.getStringAttribute("key");
			String caption = action.getStringAttribute("caption");
			actionMap.put(key + "_c", caption);
			if(action.hasAttribute("icon")) {
				// TODO need some uri handling ??
				actionMap.put(key + "_i", action.getStringAttribute("icon"));
			}
		}
		
	}
	
	public String getActionCaption(String actionKey) {
		return (String) actionMap.get(actionKey + "_c");
	}
	
	public String getActionIcon(String actionKey) {
		return (String) actionMap.get(actionKey + "_i");
	}


	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		// Ensure correct implementation and let container manage caption
		if (client.updateComponent(this, uidl, true))
			return;
		
		this.client = client;
		
		if(uidl.hasAttribute("partialUpdate")) {
			handleUpdate(uidl);
			return;
		}
		
		this.paintableId = uidl.getId();
		
		this.immediate = uidl.hasAttribute("immediate");
		
		clear();
		for (Iterator i = uidl.getChildIterator(); i.hasNext();) {
			UIDL childUidl = (UIDL)i.next();
			if("actions".equals(childUidl.getTag())){
				updateActionMap(childUidl);
				continue;
			}
			TreeNode childTree = new TreeNode();
			addItem(childTree);
			childTree.updateFromUIDL(childUidl, client);
		}
		String selectMode = uidl.getStringAttribute("selectmode");
		selectable = selectMode != null;
		multiselect = "multi".equals(selectMode);
		
		addTreeListener(new TreeListener() {
		
			public void onTreeItemStateChanged(TreeItem item) {
				if (item instanceof TreeNode) {
					TreeNode tn = (TreeNode) item;
					if(item.getState()) {
						if(!tn.isChildrenLoaded()) {
							String key = tn.key;
							ITree.this.client.updateVariable(paintableId, "expand", new String[] {key}, true);
						}
					} else {
						// TODO collapse
					}
				}
			}
		
			public void onTreeItemSelected(TreeItem item) {
				TreeNode n = ((TreeNode) item);
				if (!selectable) return;
				String key = n.key;
				if (key != null) {
					if(selectedIds.contains(key) && multiselect) {
						selectedIds.remove(key);
						n.setISelected(false);
					} else {
						if (!multiselect) {
							selectedIds.clear();
						}
						selectedIds.add(key);
						n.setISelected(true);
					}
					ITree.this.client.updateVariable(ITree.this.paintableId, "selected", selectedIds.toArray(), immediate);
				}
			}
		
		});
		
		selectedIds = uidl.getStringArrayVariableAsSet("selected");
		
	}
	
	private void handleUpdate(UIDL uidl) {
		TreeNode rootNode = (TreeNode) keyToNode.get(uidl.getStringAttribute("rootKey"));
		if(rootNode != null) {
			rootNode.renderChildNodes(uidl.getChildIterator());
		}
		
	}

	private class TreeNode extends TreeItem implements IActionOwner {
		
		String key;
		
		boolean isLeaf = false;
		
		private String[] actionKeys = null;

		private boolean childrenLoaded;
		
		public TreeNode() {
			super();
			attachContextMenuEvent(getElement());
		}
		
		public void remove() {
			Util.removeContextMenuEvent(getElement());
			super.remove();
		}

		public void setSelected(boolean selected) {
			if(!selected && !ITree.this.multiselect) {
				this.setISelected(false);
			}
			super.setSelected(selected);
		}

		public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
			this.setText(uidl.getStringAttribute("caption"));
			key = uidl.getStringAttribute("key");
			
			keyToNode.put(key, this);
			
			if(uidl.hasAttribute("al"))
				actionKeys = uidl.getStringArrayAttribute("al");
			
			if(uidl.getTag().equals("node")) {
				isLeaf = false;
				if(uidl.getChidlCount() == 0) {
					TreeNode childTree = new TreeNode();
					childTree.setText("Loading...");
					childrenLoaded = false;
					this.addItem(childTree);
				} else {
					renderChildNodes(uidl.getChildIterator());
				}
			} else {
				isLeaf = true;
			}
			
			if(uidl.getBooleanAttribute("expanded") && !getState()) {
				setState(true);
			}
			
			setSelected(uidl.getBooleanAttribute("selected"));
			
		}
		
		private void renderChildNodes(Iterator i) {
			removeItems();
			while (i.hasNext()) {
				UIDL childUidl = (UIDL)i.next();
				if("actions".equals(childUidl.getTag())) {
					updateActionMap(childUidl);
					continue;
				}
				TreeNode childTree = new TreeNode();
				this.addItem(childTree);
				childTree.updateFromUIDL(childUidl, client);
			}
			childrenLoaded = true;
		}
		
		public boolean isChildrenLoaded() {
			return childrenLoaded;
		}

		public IAction[] getActions() {
			if(actionKeys == null)
				return new IAction[] {};
			IAction[] actions = new IAction[actionKeys.length];
			for (int i = 0; i < actions.length; i++) {
				String actionKey = actionKeys[i];
				ITreeAction a = new ITreeAction(this, String.valueOf(key), actionKey);
				a.setCaption(getActionCaption(actionKey));
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
		 * Adds/removes IT Mill Toolkit spesific style name.
		 * (GWT treenode does not support multiselects)
		 * 
		 * @param selected
		 */
		public void setISelected(boolean selected) {
			setStyleName(getElement(), "i-tree-node-selected", selected);
		}
		
		public void showContextMenu(Event event) {
			if(actionKeys != null) {
				int left = DOM.eventGetClientX(event);
				int top = DOM.eventGetClientY(event);
				top += Window.getScrollTop();
				left += Window.getScrollLeft();
				client.getContextMenu().showAt(this, left, top);
			}
			DOM.eventCancelBubble(event, true);
		}
		
		private native void attachContextMenuEvent(Element el) /*-{
			var node = this;
			el.oncontextmenu = function(e) {
				if(!e)
					e = $wnd.event;
				node.@com.itmill.toolkit.terminal.gwt.client.ui.ITree.TreeNode::showContextMenu(Lcom/google/gwt/user/client/Event;)(e);
				return false;
			};
		}-*/;
		
	}
}
