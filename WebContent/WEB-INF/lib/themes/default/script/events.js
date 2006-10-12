// ==========================================================================
//
// Functions for handling Javascript events.
// (c) IT Mill Ltd
//
// ==========================================================================

// Constructor
function MillstoneEventUtils() {
	this.specialKeys = new Object();
	this.specialKeys[0] = "NONE";
	this.specialKeys[8] = "BACKSPACE";
	this.specialKeys[9] = "TAB";
	this.specialKeys[13] = "ENTER";
	this.specialKeys[16] = "SHIFT";
	this.specialKeys[17] = "CTRL";
	this.specialKeys[18] = "ALT";
	this.specialKeys[27] = "ESCAPE";
	this.specialKeys[33] = "PAGEUP";
	this.specialKeys[34] = "PAGEDOWN";
	this.specialKeys[35] = "END";
	this.specialKeys[36] = "HOME";
	this.specialKeys[37] = "LEFT";
	this.specialKeys[38] = "UP";
	this.specialKeys[39] = "RIGHT";
	this.specialKeys[40] = "DOWN";	
	this.specialKeys[45] = "INSERT";
	this.specialKeys[46] = "DELETE";

  this.loadCallback=function () { }; // do nothing
  this.unloadCallback=function () { }; // do nothing
  this.submitCallback=function () { }; // do nothing
}

//
// Add an event handler for given event
//
MillstoneEventUtils.prototype.addEventHandler = function(obj,eventType,callback) {
	if (!Millstone.commons.isNull(obj)) {	
		if (Millstone.commons.isDebug()) { Millstone.logger.log("Added event handler '"+ callback +"' for '"+eventType + "' to "+Millstone.commons.toString(obj)); }
		if (obj['on'+eventType] == null) {
			obj['on'+eventType] = callback;
		} else {
			obj['on'+eventType] = (obj['on'+eventType]).andThen(callback);		
		}
		return true;
	} else {
		return false;
	}
}

//
// Remove an event handler for given event
//
MillstoneEventUtils.prototype.removeEventHandler = function(obj,eventType,callback) {
	if (Millstone.commons.isNull(obj)) {
		return;
	}
		
	if (Millstone.commons.isDebug()) { Millstone.logger.log("Removed event handler: "+eventType + " from "+Millstone.commons.toString(obj)); }
	if (obj.addEventListener) {
		obj.removeEventListener(eventType,callback, false);
		return true;
	} else if (obj.attachEvent) {
		var r = obj.detachEvent("on"+eventType,callback);
		return r;
	} else {
		return false;
	}
}



// ==========================================================================
//
// A cross-browser event object wrapper to make event handling easier
//
// ==========================================================================
function MillstoneCommonEvent(eventObject) {
	this.originalEvent = Millstone.commons.isNull(eventObject) ? window.event : eventObject;
	this.eventType = this.originalEvent.type;
    if (document.all) {
        this.keyCode = this.originalEvent.keyCode;
    } else {
 	    this.keyCode = this.originalEvent.which;
	}
}

// Returns the key code
//
MillstoneCommonEvent.prototype.getKey = function() {
 	return this.keyCode;	
}

// Returns the type of event.
//
MillstoneCommonEvent.prototype.getEventType = function() {
	return this.eventType;
}

// Returns the key code as character
//
MillstoneCommonEvent.prototype.getChar = function() {
	String.fromCharCode(this.getKey());
}

// Get name of the key pressed. 
// This is something like 'ALT-K', 'F1' or  'ESC'
//
MillstoneCommonEvent.prototype.getKeyName = function() {
	// Find out the name of the key pressed
	var keyName = '';
	var key = this.getKey();

    // Get name for key from map
    keyName = Millstone.events.specialKeys[key];
	if (keyName != null) {
		return keyName;	
	}
    // Function keys (Note: F1 handled by 'handleHelp' function)
    if (key >=112 && key <=123) {
    	var f = key-111; // Normalize, so that value 2 maps to 'F2'    	
		keyName =  "F"+f;
	} 
	
	// Normal keys
	else {
		keyName = String.fromCharCode(key);
	}
	
	// Handle modifiers
    
	// ALT-<key>
    if (this.originalEvent.altKey) {
	    keyName = "ALT-"+keyName;
    }

	// CTRL-<key>
    else if (this.originalEvent.ctrlKey) {
	    keyName = "CTRL-"+keyName;
    }

	// SHIFT-<key>
    else if (this.originalEvent.shiftKey) {
	    keyName = "SHIFT-"+keyName;
    }
        
    return keyName;
}

// Get the source object of this event
//
MillstoneCommonEvent.prototype.getSource = function() {
    var source;
    if (this.originalEvent == null) {
    	source = null;    	
    } else if (this.originalEvent.target && typeof this.originalEvent.target != 'undefined') {
    	source = this.originalEvent.target;
    } else if (this.originalEvent.srcElement && typeof this.originalEvent.srcElement != 'undefined') {
    	source = this.originalEvent.srcElement;
    } else {
    	source = window;
    }
    return source;
}

// Format event to string
// 
MillstoneCommonEvent.prototype.toString = function() {
   return "Event '"+this.getEventType()+"' ("+this.getKey()+",'"+this.getKeyName()+"') from '"+Millstone.commons.toString(this.getSource())+"'";
}

// Cancels the event and returns false.
//
MillstoneCommonEvent.prototype.cancelEvent = function() {
	this.originalEvent.returnValue = false;
 	if (document.all) this.originalEvent.keyCode = 0;
	this.originalEvent.cancelBubble = true;
	if (Millstone.commons.isDebug()) { Millstone.logger.log("CancelEvent: "+this.toString(),"red"); }	
   	return false;
}

// ==========================================================================
//
// Window load and unload callback manager
// 
// ==========================================================================

Function.prototype.andThen=function(g) {
  var f=this;
  return function() {
    f();g();
  }
}


MillstoneEventUtils.prototype.registerLoadCallback = function(callbackFunction) {
    this.loadCallback=(this.loadCallback).andThen(callbackFunction);
  }

MillstoneEventUtils.prototype.registerUnloadCallback = function(callbackFunction) {
    this.unloadCallback=(this.unloadCallback).andThen(callbackFunction);
  }
  
MillstoneEventUtils.prototype.registerSubmitCallback = function(callbackFunction) {
    this.submitCallback = (this.submitCallback).andThen(callbackFunction);
  }

