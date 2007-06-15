// ==========================================================================
//
// Javascript functions for logging and debug
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

itmill.html.Logger = function() {
	this.logWindow = null;
}

//
// Default event handler used to log unhandled events
//
itmill.html.Logger.prototype.defaultEventHandler = function(ev) {
	var e = itmill.html.utils.commons.isNull(ev) ? window.event : ev;
	this.log("<span style=\"color: blue;\">[defaultHandler]"+itmill.html.utils.commons.eventToString(e)+"</span>");
}

//
// Log an event using specified color
//
itmill.html.Logger.prototype.logEvent = function(ev,color) {
	var e = itmill.html.utils.commons.isNull(ev) ? window.event : ev;
	this.log(itmill.html.utils.commons.eventToString(e),color);
}

//
// Log an message from given source, using specified color
//
itmill.html.Logger.prototype.log = function(message,color,source) {
	if (!this.logWindow || this.logWindow.closed) {
		this.logWindow = window.open("","LOG");
		this.logWindow.document.write("<h1>Log started "+ (new Date()) +"</h1>");
	}

	if (!itmill.html.utils.commons.isNull(color)) {
		this.logWindow.document.write("<nobr><code>");	
	}else {
		this.logWindow.document.write("<nobr><code>");	
	}
	if (!itmill.html.utils.commons.isNull(source)) {
		this.logWindow.document.write("["+source+"] ");
	}
	this.logWindow.document.write(message+"</code></nobr><br />");

	this.logWindow.scrollTo(0,64500);
}