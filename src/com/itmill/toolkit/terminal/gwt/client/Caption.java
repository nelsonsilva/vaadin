package com.itmill.toolkit.terminal.gwt.client;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class Caption extends HTML {
	
	private Widget owner;

	public Caption(Widget component)  {
		owner = component;
		setStyleName("i-caption");
	}
	
	public void updateCaption(UIDL uidl) {
		String c = uidl.getStringAttribute("caption");
		// TODO Description and error messages
		if (c == null) {
		} else {
			setText(c);
		}		
		setVisible(!uidl.getBooleanAttribute("invisible"));
	}
	
	public static boolean isNeeded(UIDL uidl) {
		if (uidl.getStringAttribute("caption") != null) return true;
		// TODO Description and error messages
		return false;
	}
	
	/**
	 * Returns Widget (most likely Paintable) for which this Caption
	 * belongs to.
	 * 
	 * @return owner Widget
	 */
	public Widget getOwner() {
		return owner;
	}
}
