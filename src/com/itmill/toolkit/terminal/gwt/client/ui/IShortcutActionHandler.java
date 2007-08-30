package com.itmill.toolkit.terminal.gwt.client.ui;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.user.client.ui.KeyboardListener;
import com.itmill.toolkit.terminal.gwt.client.ApplicationConnection;
import com.itmill.toolkit.terminal.gwt.client.UIDL;

/**
 * A helper class to implement keyboard shorcut handling. Keeps
 * a list of owners actions and fires actions to server. User class
 * needs to delegate keyboard events to handleKeyboardEvents function.
 * 
 * @author IT Mill ltd
 */
public class IShortcutActionHandler {
	private ArrayList actions = new ArrayList();
	private ApplicationConnection client;
	private String paintableId;
	
	private IShortcutActionHandler() {}
	/**
	 * 
	 * @param pid Paintable id
	 * @param c reference to application connections
	 */
	public IShortcutActionHandler(String pid, ApplicationConnection c) {
		paintableId = pid;
		client = c;
	}

	/**
	 * Updates list of actions this handler listens to.
	 * 
	 * @param c UIDL snippet containing actions
	 */
	public void updateActionMap(UIDL c) {
		actions.clear();
		Iterator it = c.getChildIterator();
		while(it.hasNext()) {
			UIDL action = (UIDL) it.next();
			
			int[] modifiers = null;
			if(action.hasAttribute("mk"))
				modifiers = action.getIntArrayAttribute("mk");
			
			ShortcutKeyCombination kc = new ShortcutKeyCombination(
					action.getIntAttribute("kc"),
					modifiers);
			String key = action.getStringAttribute("key");
			String caption = action.getStringAttribute("caption");
			actions.add(new IShortcutAction(key,kc, caption));
		}
	}

	/**
	 * This method compares given key code and modifier keys to
	 * internal list of actions. If matching action is found it 
	 * is fired.
	 * 
	 * @param keyCode character typed
	 * @param modifiers modifier keys (bitmask like in {@link KeyboardListener})
	 */
	public void handleKeyboardEvent(char keyCode, int modifiers) {
		ShortcutKeyCombination kc = 
			new ShortcutKeyCombination(keyCode, modifiers);
		Iterator it = actions.iterator();
		while(it.hasNext()) {
			IShortcutAction a = (IShortcutAction) it.next();
			if(a.getShortcutCombination().equals(kc)) {
				client.updateVariable(paintableId, "action", a.getKey(), true);
				break;
			}
		}
	}
	
}

class ShortcutKeyCombination {
	
	public static final int SHIFT = 16;
	public static final int CTRL = 17;
	public static final int ALT = 18;
	
	
	
	char keyCode = 0;
	private int modifiersMask;
	
	public ShortcutKeyCombination() {
	}
	
	ShortcutKeyCombination(char kc, int modifierMask) {
		keyCode = kc;
		this.modifiersMask = modifierMask;
	}
	
	ShortcutKeyCombination(int kc, int[] modifiers) {
		keyCode = (char) kc;
		keyCode = Character.toUpperCase(keyCode);
		
		this.modifiersMask = 0;
		if(modifiers != null) {
			for (int i = 0; i < modifiers.length; i++) {
				switch (modifiers[i]) {
				case ALT:
					modifiersMask = modifiersMask | KeyboardListener.MODIFIER_ALT;
					break;
				case CTRL:
					modifiersMask = modifiersMask | KeyboardListener.MODIFIER_CTRL;
					break;
				case SHIFT:
					modifiersMask = modifiersMask | KeyboardListener.MODIFIER_SHIFT;
					break;
				default:
					break;
				}
			}
		}
	}
	
	public boolean equals(ShortcutKeyCombination other) {
		if( this.keyCode == other.keyCode &&
				this.modifiersMask == other.modifiersMask)
			return true;
		return false;
	}
}

class IShortcutAction {

	private ShortcutKeyCombination sc;
	private String caption;
	private String key;

	public IShortcutAction(String key, ShortcutKeyCombination sc, String caption) {
		this.sc = sc;
		this.key = key;
		this.caption = caption;
	}
	
	public ShortcutKeyCombination getShortcutCombination() {
		return sc;
	}
	
	public String getCaption() {
		return caption;
	}
	
	public String getKey() {
		return key;
	}

}

