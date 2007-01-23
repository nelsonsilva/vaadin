// ==========================================================================
//
// Javascript functions for component focus handling.
// (c) IT Mill Ltd
//
// ==========================================================================


/** Declare our own namespace. 
 * 
 * All globals should be defined in this namespace.
 *
 */
if (typeof itmill == 'undefined') itmill = new Object();
if (typeof itmill.html == 'undefined') itmill.html = new Object();

itmill.html.FocusableUtils = function(eventManager) {
	// Id of focused element variable of window
	this.windowFocusVariableInputId;
	
	// Mapping from focusid ==> element
	this.focusIdToElementIdMap = new Object();

    if (typeof eventManager != 'undefined') {
       	eventManager.registerLoadCallback(this.initFocusableInputs);
		eventManager.registerLoadCallback(this.focusCurrentElement);
	} else {
		alert("Failed to initialize focus handlers");
	}
}

itmill.html.FocusableUtils.prototype.elementFocused = function(el) {
	
	if (itmill.html.utils.focusable.windowFocusVariableInputId && el && el['FOCUSID']) {
		itmill.html.utils.setVarById(this.windowFocusVariableInputId,el['FOCUSID']);
		if (itmill.html.utils.commons.isDebug()) {
			itmill.html.utils.logger.log("Focus to element: "+el['FOCUSID']+". "+ itmill.html.utils.commons.toString(el));
		}
	}

}

itmill.html.FocusableUtils.prototype.elementBlurred = function(el) {
	if (this.windowFocusVariableInputId) {
		itmill.html.utils.setVarById(this.windowFocusVariableInputId,'-1');
	}
}


itmill.html.FocusableUtils.prototype.focusCurrentElement = function() {
	var focusId = itmill.html.utils.getVarById(itmill.html.utils.focusable.windowFocusVariableInputId);
	if (itmill.html.utils.commons.isDebug()) {
		itmill.html.utils.logger.log("Current focus is: "+focusId +" = "+ itmill.html.utils.focusable.focusIdToElementIdMap[focusId]);
	}
	var elementId = itmill.html.utils.focusable.focusIdToElementIdMap[focusId];
	if (elementId) {
		var el = document.getElementById(elementId);
		if (el != null && (!el.disabled) && el.focus) {
			el.focus();
		}
	}            
}


itmill.html.FocusableUtils.prototype.handleComponentFocus = function() {
    var e = window.event;
    var source = itmill.html.utils.commons.getEventSource(e);
	itmill.html.utils.focusable.elementFocused(source);
}


itmill.html.FocusableUtils.prototype.initFocusableInputs = function() {
	var inputs=document.getElementsByTagName('*');
	if (itmill.html.utils.commons.isDebug()) {
		itmill.html.utils.logger.log("Initialize focushandlers: "+inputs.length);	
	}
	for (var i=0;i<inputs.length;i++) {
	    if (inputs[i]['FOCUSID']) {
			if (itmill.html.utils.commons.isDebug()) {
				itmill.html.utils.logger.log("Focus handler: "+inputs[i]['FOCUSID']+": "+ itmill.html.utils.commons.toString(inputs[i]));	
			}
	        itmill.html.utils.focusable.focusIdToElementIdMap[inputs[i]['FOCUSID']] = inputs[i].id;
	        itmill.html.utils.events.addEventHandler(inputs[i], "focus", itmill.html.utils.focusable.handleComponentFocus);
		}
	}		
}


