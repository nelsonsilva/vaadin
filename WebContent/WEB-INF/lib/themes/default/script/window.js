// Window component javascript


function MillstoneWindowUtils() {

}

MillstoneWindowUtils.prototype.onload = function() {
	// Invoke all registered listeners	    
	if (typeof Millstone.events != 'undefined') {    
		Millstone.events.loadCallback();
	}
}

MillstoneWindowUtils.prototype.onunload = function() {    
	// Invoke all registered listeners	    
	if (typeof Millstone.events != 'undefined') {    
		Millstone.events.unloadCallback();
	}
}

MillstoneWindowUtils.prototype.onsubmit = function() {
	// Invoke all registered listeners	    
	if (typeof Millstone.events != 'undefined') {    
		Millstone.events.submitCallback();
	}
}

MillstoneWindowUtils.prototype.onresize = function() {    
	// Invoke all registered listeners	    
	if (typeof Millstone.events != 'undefined') {    
		Millstone.events.unloadCallback();
	}
}