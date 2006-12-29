// ==========================================================================
//
// Javascript functions for component focus handling.
// (c) IT Mill Ltd
//
// ==========================================================================


MillstoneFocusableUtils.prototype.elementFocused = function(el) {
	
	if (Millstone.focusable.windowFocusVariableInputId && el && el['FOCUSID']) {
		Millstone.setVarById(this.windowFocusVariableInputId,el['FOCUSID']);
		if (Millstone.commons.isDebug()) {
			Millstone.logger.log("Focus to element: "+el['FOCUSID']+". "+ Millstone.commons.toString(el));
		}
	}

}

MillstoneFocusableUtils.prototype.elementBlurred = function(el) {
	if (this.windowFocusVariableInputId) {
		Millstone.setVarById(this.windowFocusVariableInputId,'-1');
	}
}


MillstoneFocusableUtils.prototype.focusCurrentElement = function() {
	var focusId = Millstone.getVarById(Millstone.focusable.windowFocusVariableInputId);
	if (Millstone.commons.isDebug()) {
		Millstone.logger.log("Current focus is: "+focusId +" = "+ Millstone.focusable.focusIdToElementIdMap[focusId]);
	}
	var elementId = Millstone.focusable.focusIdToElementIdMap[focusId];
	if (elementId) {
		var el = document.getElementById(elementId);
		if (el != null && (!el.disabled) && el.focus) {
			el.focus();
		}
	}            
}


MillstoneFocusableUtils.prototype.handleComponentFocus = function() {
    var e = window.event;
    var source = Millstone.commons.getEventSource(e);
	Millstone.focusable.elementFocused(source);
}


MillstoneFocusableUtils.prototype.initFocusableInputs = function() {
	var inputs=document.getElementsByTagName('*');
	if (Millstone.commons.isDebug()) {
		Millstone.logger.log("Initialize focushandlers: "+inputs.length);	
	}
	for (var i=0;i<inputs.length;i++) {
	    if (inputs[i]['FOCUSID']) {
			if (Millstone.commons.isDebug()) {
				Millstone.logger.log("Focus handler: "+inputs[i]['FOCUSID']+": "+ Millstone.commons.toString(inputs[i]));	
			}
	        Millstone.focusable.focusIdToElementIdMap[inputs[i]['FOCUSID']] = inputs[i].id;
	        Millstone.events.addEventHandler(inputs[i], "focus", Millstone.focusable.handleComponentFocus);
		}
	}		
}

function MillstoneFocusableUtils(eventManager) {
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



