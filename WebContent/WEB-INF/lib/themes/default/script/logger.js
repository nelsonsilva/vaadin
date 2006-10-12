// ==========================================================================
//
// Javascript functions for logging and debug
// (c) IT Mill Ltd
//
// ==========================================================================

function MillstoneLogger() {
	this.logWindow = null;
}

//
// Default event handler used to log unhandled events
//
MillstoneLogger.prototype.defaultEventHandler = function(ev) {
	var e = Millstone.commons.isNull(ev) ? window.event : ev;
	this.log("<span style=\"color: blue;\">[defaultHandler]"+Millstone.commons.eventToString(e)+"</span>");
}

//
// Log an event using specified color
//
MillstoneLogger.prototype.logEvent = function(ev,color) {
	var e = Millstone.commons.isNull(ev) ? window.event : ev;
	this.log(Millstone.commons.eventToString(e),color);
}

//
// Log an message from given source, using specified color
//
MillstoneLogger.prototype.log = function(message,color,source) {
	if (!this.logWindow || this.logWindow.closed) {
		this.logWindow = window.open("","LOG");
		this.logWindow.document.write("<h1>Log started "+ (new Date()) +"</h1>");
	}

	if (!Millstone.commons.isNull(color)) {
		this.logWindow.document.write("<nobr><code>");	
	}else {
		this.logWindow.document.write("<nobr><code>");	
	}
	if (!Millstone.commons.isNull(source)) {
		this.logWindow.document.write("["+source+"] ");
	}
	this.logWindow.document.write(message+"</code></nobr><br />");

	this.logWindow.scrollTo(0,64500);
}