// ==========================================================================
//
// Common functions for other Millstone Javascript Modules.
// (c) IT Mill Ltd
//
// ==========================================================================


// Constructor
function MillstoneCommonUtils() {
	this.debug = false;
	this.loadedScripts = new Object();
}

// Check for null object
//
MillstoneCommonUtils.prototype.isDebug = function () {
	return this.debug;
}

// Check for null object
//
MillstoneCommonUtils.prototype.isNull =  function(obj) {
	return (obj == null || typeof(obj) == 'undefined');
}

// Format event to string
//
MillstoneCommonUtils.prototype.eventToString =  function(e) {
	return "Event: "+e.type +" from "+Millstone.commons.toString(this.getEventSource(e));
}

// Format object to string
//
MillstoneCommonUtils.prototype.toString = function (obj, longFormat, indent) {
	if (!obj || typeof(obj) == 'undefined') {
		return "Object: "+obj;
	}
	var tab = longFormat?"\t":"";
	if (typeof(indent) != 'undefined')
		tab += indent;
	var str = tab+"Object[tag="+obj['tagName']+",id="+obj['id']+"]";
	if (!longFormat) return str;
	for (var prop in obj) {
		if (obj[prop] != null)
			str += "\n"+tab + prop + "("+(typeof(obj[prop])) +") = '"+ obj[prop];
		if (typeof(obj[prop]) == 'object' && obj[prop] != null) 
			str += toString(obj[prop], false, tab);
	}
	return str+"'\n";
}


// Format object to string
//
MillstoneCommonUtils.prototype.toHTML = function (obj, longFormat, indent) {
	if (!obj || typeof(obj) == 'undefined') {
		return "<b>Object: "+obj+"</b>";
	}
	var tab = longFormat?"&nbsp;&nbsp;&nbsp;":"";
	if (typeof(indent) != 'undefined')
		tab += indent;
	var str = tab+"<b>Object[tag="+obj['tagName']+",id="+obj['id']+"]</b>";
	if (!longFormat) return str;
	for (var prop in obj) {
		if (obj[prop] != null)
			str += "<br /><nobr>" +tab +"<b>"+ prop + "</b>("+(typeof(obj[prop])) +") = '"+ escape(obj[prop]) +"'</nobr>";
		if (typeof(obj[prop]) == 'object' && obj[prop] != null) 
			str += toString(obj[prop], false, tab);
	}
	return str+ "<br />";
}

//
// Extract the event source object from event object
//
MillstoneCommonUtils.prototype.getEventSource = function (ev) {
    var source;
    if (ev == null) {
    	return null;    	
    } else if (ev.target && typeof ev.target != 'undefined') {
    	source = ev.target;
    } else if (ev.srcElement && typeof ev.srcElement != 'undefined') {
    	source = ev.srcElement;
    } else {
    	source = window;
    }
    return source;
}

//
// Cancel an event by setting the state of event object
// 
MillstoneCommonUtils.prototype.cancelKeyEvent = function (ev) {
	var e = this.isNull(ev) ? window.event : ev;
 	e.keyCode = 0;
	e.returnValue = false;
	e.cancelBubble = true;
	if (this.isDebug()) { Millstone.logger.log("[cancelKeyEvent] "+e.type + " from "+this.toString(this.getEventSource(e)),"red"); }	
   	return false;
}

// Extract keycode from event object
//
MillstoneCommonUtils.prototype.getKeyCode = function (e) {
    var key = 0;
    if (document.all) {
        key = e.keyCode;
    } else if (document.layers)
 	    key = e.which;
 	return key;
}

// Ensures that given javascript library is loaded
//
MillstoneCommonUtils.prototype.loadLibraryOnce = function(src) {
	var loaded = this.loadedScripts[src];	
	if (this.isNull(loaded)) {
		var str = '<scr'+'ipt language="Javascript" src="' + src + '">var iefix=1;</scr'+'ipt>';
		document.write(str);
		this.loadedScripts[src] = new Object();
	}
}
