package com.itmill.toolkit.terminal.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Utility class for fetching CSS properties from DOM StyleSheets JS object.
 */
public class CSSRule {

    private final String selector;
    private JavaScriptObject rules = null;

    public CSSRule(String selector) {
        this.selector = selector;
        fetchRule(selector);
    }

    // TODO how to find the right LINK-element? We should probably give the
    // stylesheet a name.
    private native void fetchRule(final String selector)
    /*-{
        this.@com.itmill.toolkit.terminal.gwt.client.CSSRule::rules = @com.itmill.toolkit.terminal.gwt.client.CSSRule::searchForRule(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)($doc.styleSheets[1], selector);
    }-*/;

    /*
     * Loops through all current style rules and collects all matching to
     * 'rules' array. The array is reverse ordered (last one found is first).
     */
    private static native JavaScriptObject searchForRule(
            JavaScriptObject sheet, final String selector)
    /*-{
    if(!$doc.styleSheets)
        return null;
        
    selector = selector.toLowerCase();
    
    var allMatches = [];
    
    var theRules = new Array();
    if (sheet.cssRules)
        theRules = sheet.cssRules
    else if (sheet.rules)
        theRules = sheet.rules
        
        var j = theRules.length;
        for(var i=0; i<j; i++) {
            var r = theRules[i];
            if(r.type == 3) {
                allMatches.unshift(@com.itmill.toolkit.terminal.gwt.client.CSSRule::searchForRule(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;)(r.styleSheet, selector));
            } else if(r.type == 1) {
                var selectors = r.selectorText.toLowerCase().split(",");
                var n = selectors.length;
                for(var m=0; m<n; m++) {
                    if(selectors[m].replace(/^\s+|\s+$/g, "") == selector) {
                        allMatches.unshift(r);
                        break; // No need to loop other selectors for this rule
                    }
                }
            }
        }
        
        return allMatches;
    }-*/;

    /**
     * Returns a specific property value from this CSS rule.
     * @param propertyName
     * @return
     */
    public native String getPropertyValue(final String propertyName)
    /*-{  
        for(var i=0; i<this.@com.itmill.toolkit.terminal.gwt.client.CSSRule::rules.length; i++){
            var value = this.@com.itmill.toolkit.terminal.gwt.client.CSSRule::rules[i].style[propertyName];
            if(value)
                return value;
        }
        return null;
    }-*/;

    public String getSelector() {
        return selector;
    }

}
