package com.itmill.toolkit.demo.colorpicker.gwt.client;

import com.google.gwt.user.client.ui.Widget;
import com.itmill.toolkit.demo.colorpicker.gwt.client.ui.IColorPicker;
import com.itmill.toolkit.terminal.gwt.client.DefaultWidgetSet;
import com.itmill.toolkit.terminal.gwt.client.UIDL;

public class WidgetSet extends DefaultWidgetSet {
	/** Creates a widget according to its class name. */
    public Widget createWidget(UIDL uidl) {
    	String className = resolveWidgetTypeName(uidl);
    	if ("com.itmill.toolkit.demo.colorpicker.gwt.client.ui.IColorPicker"
    			.equals(className))
    		return new IColorPicker();

    	// Let the DefaultWidgetSet handle creation of default widgets
    	return super.createWidget(uidl);
    }

    /** Resolves UIDL tag name to class name. */
    protected String resolveWidgetTypeName(UIDL uidl) {
    	String tag = uidl.getTag();
    	if ("colorpicker".equals(tag))
    		return "com.itmill.toolkit.demo.colorpicker.gwt.client.ui.IColorPicker";

    	// Let the DefaultWidgetSet handle resolution of default widgets
    	return super.resolveWidgetTypeName(uidl);
    }
}
