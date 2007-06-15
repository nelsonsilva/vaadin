// Window component javascript


/** Declare our own namespace. 
 * 
 * All globals should be defined in this namespace.
 *
 */
if (typeof itmill == 'undefined') itmill = new Object();
if (typeof itmill.html == 'undefined') itmill.html = new Object();

itmill.html.WindowUtils = function() {

}

itmill.html.WindowUtils.prototype.onload = function() {
	// Invoke all registered listeners	    
	if (typeof itmill.html.utils.events != 'undefined') {    
		itmill.html.utils.events.loadCallback();
	}
}

itmill.html.WindowUtils.prototype.onunload = function() {    
	// Invoke all registered listeners	    
	if (typeof itmill.html.utils.events != 'undefined') {    
		itmill.html.utils.events.unloadCallback();
	}
}

itmill.html.WindowUtils.prototype.onsubmit = function() {
	// Invoke all registered listeners	    
	if (typeof itmill.html.utils.events != 'undefined') {    
		itmill.html.utils.events.submitCallback();
	}
}

itmill.html.WindowUtils.prototype.onresize = function() {    
	// Invoke all registered listeners	    
	if (typeof itmill.html.utils.events != 'undefined') {    
		itmill.html.utils.events.unloadCallback();
	}
}