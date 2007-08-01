package com.itmill.toolkit.terminal.gwt.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.itmill.toolkit.terminal.gwt.client.ApplicationConnection;
import com.itmill.toolkit.terminal.gwt.client.Paintable;
import com.itmill.toolkit.terminal.gwt.client.UIDL;

public class ILink extends HTML implements Paintable, ClickListener {
	
	public static final String CLASSNAME = "i-link";
	
	private static final int BORDER_STYLE_DEFAULT = 0;
	private static final int BORDER_STYLE_MINIMAL = 1;
	private static final int BORDER_STYLE_NONE = 2;
	
	private String src;
	
	private String text;
	
	private String target;
	
	private int borderStyle = BORDER_STYLE_DEFAULT;
	
	private boolean enabled;

	private boolean readonly;

	private int width;

	private int height;
	
	public ILink() {
		super();
		addClickListener(this);
	}
	
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

//		if (client.updateComponent(this, uidl, true))
//			return;
		
		enabled = uidl.hasAttribute("disabled") ? false : true;
		readonly = uidl.hasAttribute("disabled") ? true : false;
		
		if(uidl.hasAttribute("target"))
			target = uidl.getStringAttribute(target);
		if(uidl.hasAttribute("src")) {
			//TODO theme soure
			src = uidl.getStringAttribute("src");
		}
		
		height = uidl.hasAttribute("height") ? uidl.getIntAttribute("height") : -1;
		width = uidl.hasAttribute("width") ? uidl.getIntAttribute("width") : -1;
		
		DOM.setInnerHTML(getElement(), uidl.getStringAttribute("caption"));
	}

	public void onClick(Widget sender) {
		if(enabled) {
			if(target == null)
				target = "_blank";
			String features;
			switch (borderStyle) {
			case BORDER_STYLE_NONE:
				features = "menubar=no,location=no,status=no";
				break;
			case BORDER_STYLE_MINIMAL:
				features = "menubar=yes,location=no,status=no";
				break;
			default:
				features = "menubar=yes,location=yes,scrollbars=yes,status=yes";
				break;
			}
			if(width > 0 || height > 0) {
				features += ",resizable=no";
				if(width > 0)
					features += ",width=" + width;
				if(height > 0)
					features += ",height=" + height;
			} else {
				features += ",resizable=yes";			
			}
			Window.open(src, target, features);
		}
	}
	
}
