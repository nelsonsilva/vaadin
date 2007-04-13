/** Register namespace using OpenAjax Hub if it is available */
if (typeof OpenAjax != 'undefined') {
 	OpenAjax.registerLibrary("ITMillToolkit40", "http://toolkit.itmill.com/", "4.0");
	OpenAjax.registerGlobals("ITMillToolkit40", ["itmill"]);
}

/** Declare our own namespace. 
 * All globals should be defined in this namespace.
 *
 */
if (typeof itmill == 'undefined') itmill = new Object();


/** List of themes */
itmill.themes = new Object();

/** List of clients */
itmill.clients = new Array();

/** Creates new itmill.toolkit ajax client.
 *  @param windowElementNode Reference to element that will contain the 
 * 				application window.
 *  @param servletUrl Base URL to server-side ajax adapter.
 *  @param clientRoot Base URL to client-side ajax adapter resources.
 *  @constructor
 *
 *  @author IT Mill Ltd.
 */
itmill.Client = function(windowElementNode, servletUrl, clientRoot, waitElement) {
    this.clientId = itmill.clients.length;
    itmill.clients.push(this);
    
    this._browserDetect();

	// Store parameters
	this.mainWindowElement = windowElementNode;
	if (this.mainWindowElement == null) {
		alert("Invalid window element. Ajax client not properly initialized.");
	}
	// make main element look like Paintable so that some event listeners can't get
	// reference to client
	this.mainWindowElement.varMap = new Object();
	this.mainWindowElement.client = this;
	this.itmtkMainWindow = window;
	
	this.ajaxAdapterServletUrl = servletUrl;
	if (this.ajaxAdapterServletUrl == null) {
		alert("Invalid servlet URL. Ajax client not properly initialized.");
	}

	// Wait element is shown during the ajax requests
	this.waitElement = waitElement;

	// Root of the client scripts
	if (clientRoot == null)
		this.clientRoot = "";
	else (clientRoot.length > 0)
		this.clientRoot = clientRoot + (clientRoot.match('/$') ? "" : "/" );	

	// Debugging is disabled by default
	this.debugEnabled = false;
    
    if(window.location.hash.indexOf("profiling") > 0) {
        this.profilingEnabled = true;
    }

	// Initialize variableChangeQueue
	this.variableStates = new Object();
	
	// Create empty renderers list
	this.renderers = new Object(); 

	// Create windows list (for native style windows)
	this.documents = new Object(); 
	this.windows = new Object(); 
	
	this.windowOrder = new Array();
	
	// Remove all eventListeners on window.unload
	with (this) {
		addEventListener(window,"unload", function () {
			var removed =  removeAllEventListeners(document);
			if (window.eventMap) {
				for (var t in window.eventMap) {
					var i = window.eventMap[t].length;
					while (i--) {
						client.removeEventListener(window,t,window.eventMap[t][i]);
						removed++;
					}
				}
				window.eventMap = null;
			}
			
			debug("Removed " + removed + " event listeners.");
			// TODO close all windows
			debug("Removed " + unregisterAllLayoutFunctions()+ " layout functions.");
			
			window.png = null;
		});
		var client = this;
		var func = function() {
			client.resizeTimeout=null;
			client.processAllLayoutFunctions()
		};
		
		addEventListener(window,"resize", function () {	
			if (client.resizeTimeout) clearTimeout(client.resizeTimeout);				
			client.resizeTimeout = setTimeout(func,500);
		});
		
	}
	
	// TODO remove global
	window.png = function(img) {
       var src = img.src;
        if (!src || src.indexOf("pixel.gif")>0) return;
        if (src.indexOf(".png")<1) return
        if (!itmill.wb.isIE6) return;
        
        var w = img.width||16; // def width 16, hidden icons fail otherwise
        var h = img.height||16;
        
        img.onload = null;
        img.src = clientRoot + "pixel.gif";
        img.style.height = h+"px";
        img.style.width = w+"px";
        img.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+src+"', sizingMethod='crop');";               
	}
	
}

/** Start the ajax client.
 *  Sends the initial request to server.
 *
 *  @author IT Mill Ltd.
 */
itmill.Client.prototype.start = function() {
	if (this.debugEnabled) {
		this.debug("Starting Ajax client");
	} 
	// Send initial request
	this.processVariableChanges(true);
}

/**
 * This function runs various browser detections.
 * 
 * After this we can check for variables like itmill.wb.isIE or itmill.wb.isMac
 */
itmill.Client.prototype._browserDetect = function() {
	if(!itmill.wb) {
		itmill.wb = new itmill.WebBrowser();
	}
}

itmill.Client.prototype.warn = function (message, folded, extraStyle, html) {

	// Check if we are in debug mode
	if (this.debugEnabled)	{
        console.warn(message);
    }
}
/** Write debug message to debug window.
 *
 *  @param message The message to be written
 *  @param folded True if the message should be foldable and folded to default,
 *			false or missing otherwise.
 *
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.debug = function (message, folded, extraStyle, html) {
	// Check if we are in debug mode
	if ((typeof console) != 'undefined' && this.debugEnabled)	{ 
        console.log(message);
    }
}

/** Write object properties to console.
 *
 *  @param obj The object that is debugged.
 *  @param level The recursion level that the properties are inspected.
 *
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.debugObject = function (obj,level) {
	this.debug(this.printObject(obj,level),true,null,true);
}

/** Write error message to debug window.
 *
 *  @param message The message to be written
 *  @param causeException Exception that caused this error.
 *
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.error = function (message, causeException) {

	// Check if we are in debug mode
	if (!this.debugEnabled)	{ return; }

    console.error(message);
    console.dir(causeException);
    console.trace();
}

/** Creates new XMLHttpRequest object.
 *
 *  NOTE: The return type of this function is platform dependent.
 *
 *  @return New XMLHttpRequest or XMLHTTP (ActiveXObject) instance
 *  @type XMLHttpRequest | ActiveXObject
 *
 *  @author IT Mill Ltd.
 */
itmill.Client.prototype.getXMLHttpRequest = function () {

	var req = false;
  	
	if(window.XMLHttpRequest) {
	
		// Native XMLHttpRequest object
		try {
			req = new XMLHttpRequest();
		} catch(e) {
			req = false;
		}
		    
    } else if(window.ActiveXObject) {
    	
    	// IE/Windows ActiveX version
		try {
			req = new ActiveXObject("Msxml2.XMLHTTP");
		} catch(e) {
			try {
				req = new ActiveXObject("Microsoft.XMLHTTP");
			} catch(e) {
				req = false;
			}
		}
	}
	return req;
}

/** Loads a document using XMLHttpRequest object and returns it as text.
 *
 *  @param url The URL of document.		
 *  @skipCache If true, does not use cached documents (or cache this result).
 *	
 *  @author IT Mill Ltd.
 * 
 */
itmill.Client.prototype.loadDocument = function (url,skipCache) {

	if (!skipCache) {
		if (!this.loadcache) this.loadcache = new Object();
		var cached = this.loadcache["_"+url];
		if (cached != null) {
			this.debug(url + " loaded from cache.");
			return cached;
		}
	}
	
	var x = this.getXMLHttpRequest();
	x.open("GET",url, false);
	x.send(null);
	var response = x.responseText; 
	if (x.status != 200) {
		this.error("Could not load (status 200) " + url);
		return null;
	}
	delete x;
	
	if (!skipCache) {
		this.loadcache["_"+url] = response;
	}
	
	if (response) {
		this.debug(url + " loaded.");
	} else {
		this.debug("Could not load " + url);
	}
	return response;
}

/** Loads a CustomLayout using the XMLHttpRequest object, performs url rewrite,
 *  and returns it as text.
 *  Currently rewrites:
 *    src=|codebase=|code=|background=|usemap=|lowsrc=|href= and url()
 *  @param url The name of the CustomLayout.		
 *  @skipCache If true, does not use cached documents (or cache this result).
 *	
 *  @author IT Mill Ltd.
 * 
 */
itmill.Client.prototype.loadCustomLayout = function (style,skipCache) {
	if (!skipCache) {
		// check if it's cached
		if (!this.loadcache) this.loadcache = new Object();
		var cached = this.loadcache["_"+style];
		if (cached != null) {
			this.debug(style + " loaded from cache.");
			return cached;
		}
	}
	
	// not in cache, get from url
	var renderer = this.findRenderer("customlayout",style);
	var theme = renderer.theme;
	var layoutBase = theme.root + "layout/";
	var url = layoutBase + style + ".html";
	var x = this.getXMLHttpRequest();
	x.open("GET",url, false);
	x.send(null);
	if (x.status != 200) {
		delete x;
		this.debug("Could not find CustomLayout " + style);
		return null;
	}
	text = x.responseText;
	delete x;
	if (!text) {
		this.debug("Empty CustomLayout " + style + " loaded from " + url);
		return null;
	}
	
	var attrs = ["","codebase"];
	// Replace src=
	for (var i=0;i<attrs.length;i++) {
		var attr = attrs[i];
	 	text = text.replace(/(src=|codebase=|code=|background=|usemap=|lowsrc=|href=)(['"])(?!\/)(?!http:)(?!https:)(?:.\/.\/)?(\S+\2)/gi,"$1$2"+layoutBase+"$3");
	  	text = text.replace(/(src=|codebase=|code=|background=|usemap=|lowsrc=|href=)(?!['"])(?!\/)(?!http:)(?!https:)(?:.\/.\/)?(\S+)/gi,"$1"+layoutBase+"$2");
  	}
  	// Replace url()
  	text = text.replace(/(url\(\s*)(['"])(?!\/)(?!http:)(?!https:)(?:.\/.\/)?(\S+\2\s*\))/gi,"$1$2"+layoutBase+"$3");
  	text = text.replace(/(url\(\s*)(?!['"])(?!\/)(?!http:)(?!https:)(?:.\/.\/)?(\S+\s*\))/gi,"$1"+layoutBase+"$2");
 	// Replace token ././ with base url 
	text = text.replace(/\.\/\.\//g,layoutBase);
	
	if (!skipCache) {
		this.loadcache["_"+style] = text;
	}
	
	if (text) {
		this.debug(style + " loaded from " + url);
	} else {
		this.debug("Could not load " + style + " from " + url);
	}
	return text;
}


/** Registers new renderer function to ajax client.
 *	
 *  @param theme Theme instance where the renderer belongs to.
 *  @param tag UIDL Tag-name that this renderer supports.
 *  @param componentStyle The style attribute of component that this renderer supports.
 *  @param renderFunction Function that is performs the rendering of the UIDL.
 *	@return Newly created renderer object instance.
 *  @type Object
 *
 *  @author IT Mill Ltd.
 * 
 */
itmill.Client.prototype.registerRenderer = function (theme, tag, componentStyle, renderFunction) {

	if (renderFunction == null) {
		alert("Theme error: Invalid renderer function registered for '"+tag+(componentStyle == null ? "" : "." + componentStyle)+"'");
	}

	// Find previous (parent) renderer
	var parentRenderer = this.findRenderer(tag,componentStyle);
	
	// Create new renderer information object
	var renderer = new Object; 
	renderer.match = tag + (componentStyle == null ? "" : "__" + componentStyle);
	renderer.doc = document;
	renderer.client = this;
	renderer.theme = theme;
	renderer.tag = tag;
	renderer.componentStyle = componentStyle;
	renderer.renderFunction = renderFunction;
	renderer.parentRenderer = parentRenderer;
	
	// This replaces the previous (parent) renderer
	this.renderers[renderer.match] = renderer;

	// We return the created renderer object
	this.debug("Registered renderer for "+tag +(componentStyle == null ? "" : " (" + componentStyle+")")+"");
	return renderer;

}


/** Unregisters all renderers in client.
 *
 *  @author IT Mill Ltd.
 * 
 */
itmill.Client.prototype.unregisterAllRenderers = function () {

	// We just create new, empty rederer map.
	this.renderers = new Object();

}

/** Create new response listener for the HTTPRequest object.
 *  This creates new function reference that is used to
 *  process the server response in httpRequest.onreadystatechange.
 *
 *  @param client Reference to this client instance.
 *
 *  @author IT Mill Ltd.
 * 
 */
itmill.Client.prototype.createRequestChangeListener = function(client, req) {
	
    return (function() {
        try {
            // wrap everything in try-catch to get better user experience
            // in case of session timeout, server error etc
            if (req.readyState != 4 || typeof req.status == 'undefined' || req.status == null)
                return;
        
            // Check status code
            if (req.status != 200) {
                console.error(req);
                req.onreadystatechange = new Function();
                delete req;
                return;
            }
    
            // Get updates
            var updates = req.responseXML;
            // responseXML should be null if not valid XML, but sadly not, validate
            // by detecting count of changes element
            if (!updates || updates.getElementsByTagName("changes").length == 0) {
                // check if response was a redirect instruction (application end)
                if(updates.getElementsByTagName("redirect").length > 0) {
                    var redirect = updates.getElementsByTagName("redirect")[0];
                    window.location = redirect.getAttribute("url");
                    return;
                } else {
                    // something unexpected returned as uidl
                    throw("Invalid UIDL response");
                }
            }
            
            // Debug request load time
            if (client.debugEnabled) {
                console.timeEnd("UIDL loaded in ");
                // Firebug can show traffic so no need to show responce, 
                // uncomment if you need to see uidl in other browsers
                //client.debug("UIDL Changes: \n"+req.responseText,true);
            }
            
            // Clean up 
            client.variableStates = new Object();
            req.onreadystatechange = new Function();
            delete req;
            
            // Process the updates
            try {
                if (updates.normalize) updates.normalize();
            } catch (e) {
                if (client.debugEnabled) {
                    client.debug("normalize() FAILED");
                }
            }
            client.processUpdates(updates); 
            client.requestStartTime = -1;

        } catch(e) {
            // If server no not respond or content not uidl -> reload window
            console.error("Bad UIDL response:" + e);
            console.log(req.responseText);
            req.onreadystatechange = new Function();
            delete req;
            return;
        }
	});
}

/** Send pending variable changes to server.
 *
 *  This function sends all pending (non-immediate) variable changes to the 
 *  server and registers callback to render process the server response.
 *
 *  @param repaintAll True if full window UIDL should be requested from server.
 *  @param nowait True if the wait-window should not be shown
 *
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.processVariableChanges = function (repaintAll,nowait) {
	
	if (this.waitElement&&!nowait) {
		this.waitElement.style.display = "inline";
	}
	
	// Request start time
    if(this.debugEnabled) {
        console.time("UIDL loaded in ");
    }
	
	// Build variable change query string
	var changes = (repaintAll ? "repaintAll=1" : "");
	for (var i in this.variableStates) {
		changes  += (changes==""?"":"&") + 
		i + "=" + encodeURIComponent(this.variableStates[i]);
	}
	
	// Build up request URL
    var url = this.ajaxAdapterServletUrl;
    
    // Use get parameters for Nokia Reindeer, other browsers use post
    var useGetParams = false;
    if (navigator.appName=="Netscape"&&navigator.appVersion=="7.0") useGetParams=true;
    
    // Get requests require unique requestid to avoid caching
    if (useGetParams)
    	url += "?requestid=" + Math.random() + "&" + changes; 
    
     // Run the HTTP request
	this.debug("Send variable changes: " + url);
	var activeRequest = this.getXMLHttpRequest();
    // Create callback for request state changes
    var changeListener = this.createRequestChangeListener(this,activeRequest);  
	activeRequest.onreadystatechange = changeListener;
	activeRequest.open(useGetParams?"GET":"POST",url, true);
	activeRequest.setRequestHeader('Content-Type',
                      'application/x-www-form-urlencoded; charset=UTF-8');
	activeRequest.send(useGetParams?null:changes);
}

/** Get first child element in given parent.
 *
 *  @param parent The parent element
 *
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.getFirstChildElement = function (parent) {
	/*
	if (parent == null || parent.childNodes == null) {
		return null;
	}
	for (var j=0; j<parent.childNodes.length; j++) {  			
		var n = parent.childNodes.item(j);
		if (n.nodeType == Node.ELEMENT_NODE) {
			return n;
		}
	}
	*/
	try {
		var child = parent.firstChild;
		while (child) {
			if (child.nodeType == Node.ELEMENT_NODE) {
				return child;
			}
			child = child.nextSibling;
		}
	} catch (e) {
	}
	
	return null;
	
	
}


/** Initializes new native browser window.
 *  Creates a document element and initializes it to
 *  to contain a window component.
 *
 *  @param win The window to be initialized.
 *  @param name IT Mill Toolkit name of the window to be initialized.
 *  @return reference to div in document that should contain the window.
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.initializeNewWindow = function (win,uidl,theme) {

	if (win == null) {
		return null;
	}
	
	// Special handling for framewindows
	var framewindow = uidl.nodeName == "framewindow";	
	var name = uidl.getAttribute("name");	
	var caption = uidl.getAttribute("caption")||"";	
	if (this.debugEnabled) {
			this.debug("Initializing new "+(framewindow?"frame-":"")+"window '"+name+"' (PID="+uidl.getAttribute("id")+")");
	}    

	// Create HTML content
	var winElementId = "itmtk-window";
	var html="";
	if (framewindow) {
		html = this.createFramesetHtml(uidl,theme)
	} else {
		html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"+
				"<HTML><HEAD id=\"html-head\"><TITLE>"+caption+"</TITLE>";

    // Add stylesheets if safari
    	if (typeof navigator.vendor != 'undefined' && navigator.vendor.indexOf('Apple') >= 0)
		for (var i=0; i<this.mainDocument.styleSheets.length; i++) {	
			var ss = this.mainDocument.styleSheets[i];	
			if (typeof ss.href != 'undefined') 
				html += "<link href='" + ss.href + "' type='text/css' rel='stylesheet' />"
		}
		
		html +="</HEAD>\n"+"<BODY STYLE=\" overflow: auto; border: none; margin: 0px; padding: 0px;\" class='itmtk'>"+
		"<div id=\""+winElementId+"\" class=\"window\"></div><\/BODY><\/HTML>\n";			
	}
	win.document.open();
    win.document.write(html);
    win.document.close();
    win.document.ownerWindow = win;
    
    // TODO ILLEGAL global
    win.document.renderUIDL = function(uidl,currentNode) {
    	this.client.renderUIDL(uidl,currentNode);
    }
    win.document.client = this;
    with (this) {
		addEventListener(win,"unload", function () {
			try {
				removeAllEventListeners(win.document);
				removeAllEventListeners(win);
				unregisterAllLayoutFunctions(win.document);
			} catch (e) {
				// IGNORED
			}
		});
		var client = this;
		addEventListener(win,"resize", function () {			
			try {
				setTimeout(function() {client.processAllLayoutFunctions()},1);
			} catch (e) {
				// IGNORED
			}
		});
		
	}
	
	// Add stylesheets for others than safari
    if (!framewindow && !(typeof navigator.vendor != 'undefined' && navigator.vendor.indexOf('Apple') >= 0)) {
		for (var si in this.mainDocument.styleSheets) {	
			var ss = this.mainDocument.styleSheets[si];	
			var nss = win.document.createElement('link');
			nss.rel = 'stylesheet';
			nss.type = 'text/css';
			nss.media = ss.media;
			nss.href = ss.href;
			if (ss.href != null)
				win.document.getElementById('html-head').appendChild(nss);
		}
	}
		
    // Register it to client
	this.registerWindow(name, win, win.document);
	
	// Add unregister callback
	var client = this;
	win.onunload = function() { 
		client.unregisterWindow(name); 
		win.onunload = null;
	}
	
	// Ensure the name
	win.itmtkWindowName = name;
	
	// Assign the current node into that window
	var winElement = win.document.getElementById("itmtk-window");
	if (framewindow) {
		winElement = win.document.getElementById(uidl.getAttribute("id"));
	}	
	if (winElement == null && this.debugEnabled) {
			this.warn("Window element not found!");
	}
	win.document.itmtkWindowElement = winElement;
	
	if (!win.png) {
		var clientRoot = this.clientRoot;
		// PNG loading support in IE
		win.png = function(img) {
                var ua = navigator.userAgent.toLowerCase();
                if (ua.indexOf("windows")<0) return;
                var msie = ua.indexOf("msie");
                if (msie < 0) return;
                var v = parseInt(ua.substring(msie+5,msie+6));
                if (!v || v < 5 || v > 6) return;
                
                var src = img.src;
                var w = img.width;
                var h = img.height;
                
                if (src && src.indexOf(clientRoot+"pixel.gif")>0) return;
                
                img.onload = null;
                img.src = clientRoot + "pixel.gif";
                img.style.height = h+"px";
                img.style.width = w+"px";
                img.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+src+"', sizingMethod='crop');";               
        }
	}
	
	// Return the content element
	return winElement;
}

/** Recursively create frameset html for FrameWindow initialization.
 *
 *  @param win The window to be initialized.
 *  @param name IT Mill Toolkit name of the window to be initialized.
 *  @return reference to div in document that should contain the window.
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.createFramesetHtml = function(uidl,theme) {

	if (uidl == null) {
		return "";
	}
	var cols = uidl.getAttribute("cols");
	var rows = uidl.getAttribute("rows");
	var caption = uidl.getAttribute("caption")||"";

	// Open frameset	
	var html = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html><head><title>"+caption+"</title></head><frameset ";
	if (cols) {
		html += "cols=\""+cols+"\"";
	} else if (rows) {
		html += "rows=\""+rows+"\"";
	}
	html += " id=\""+uidl.getAttribute("id")+"\"";
	html += " >";

	// Sub-frames / -framesets
    for (var i=0; i<uidl.childNodes.length; i++) {
		var n = uidl.childNodes.item(i); 
		if (n.nodeType == Node.ELEMENT_NODE) {
			if (n.nodeName == "frameset") {
				html += this.createFramesetHtml(n);
			} else if (n.nodeName == "frame") {
				var name =n.getAttribute("name");
				var src = n.getAttribute("src");
				html += "<frame id=\""+name+"\" name=\""+name+"\"";
				
				if (src && src.indexOf("theme://")==0) {
					src = (theme?theme.root:"themes/") + src.substring(8);
					html += " src=\""+src+"\" ";
				}
				
				html += "/>";
			}
		}
	}
	
	// Close frameset	
	html += "</frameset></html>";
	return html;
}

/** Unregisters and closes a window.
 
 *  @param windowName Name of the window
 *
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.unregisterWindow = function (windowName) {
	if (this.debugEnabled) {
		this.debug("Unregistering window '"+windowName+"'");
	}
	
	var doc = this.documents[windowName];
	var win = this.windows[windowName];
	
	if (doc) {
		this.documents[windowName] = null;
		try {
			//if (win.location) win.location.href = "about:blank";
			this.windows[windowName] = null;
			win.close();
			doc.ownerWindow = null;
		} catch (e) {
			if (this.debugEnabled) {
				this.error("Exception when closing window '"+windowName+"'. Continuing...",e);
			}
		}
	} else if (this.debugEnabled) {
		this.debug("Failed to unregister '"+windowName+"'. Window not found.");
	}
}

/** Registers new window .
 *  This enabled to client update the components by id in this window.
 
 *  @param windowName Name of the window
 *  @param doc The document element of window.
 *
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.registerWindow = function (windowName,win,doc) {
	if (win != null && doc != null && windowName != null) {	
		doc.itmtkWindowName = windowName;
		this.documents[windowName] = doc;
		this.windows[windowName] = win;
		if (this.debugEnabled) {
			this.debug("Registered new window '"+windowName+"'");
		}
	}
}

/** Find a paintable by id.
 *  Searcher all windows for given id and returns the element
 *  or null if not found.
 
 *  @param paintableId Id to look for.
 *
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.findPaintableById = function (paintableId) {

	if (this.documents == null) {
		return null;
	}
	for (var name in this.documents) {  			
		var d = this.documents[name];
		var win = this.windows[name];
		
		try {
			if (win && win.location && this.itmtkMainWindow.location.href != win.location.href) {
				// referencing external url, we cannot access, but no problem:
				// it can't contain the paitable either.
				if (this.debugEnabled) {
					this.debug("Window: '"+name+"' referencing external URL and can NOT contain Paintable '"+paintableId+"'");
				}	
				continue;
			}
		} catch (e) {
			this.debug("Exception while examining window.location, assuming ext url.");
			continue;
		}

		try {
			if (d != null && win && !win.closed) {
				var el  = d.getElementById(paintableId);
				if (el != null) {
					if (this.debugEnabled) {
						var isMain = el.ownerDocument == this.mainDocument? "main":" child";
						this.debug("Paintable '"+paintableId+"' found in "+isMain+"-window: '"+d.itmtkWindowName+"'");
					}			
					return el;
				} else {
					if (this.debugEnabled) {
						this.debug("Window: '"+d.itmtkWindowName+"' does NOT contain Paintable '"+paintableId+"'");
					}			
				}
			}
		} catch (e) {			
			if (this.debugEnabled) {
				this.error("Exception when accessing window '"+name+"'. Closing and continuing...",e);
			}
			this.unregisterWindow(name);
		}			
	}
	if (this.debugEnabled) {
		// this may be normal, if user makes quick clicks and 
		// updates to other elements are still on their way
		this.warn("Paintable '"+paintableId+"' NOT found in ANY of current windows.");
	}	
	return null;
}

/** Process UIDL updates from server.
 *
 *  Renders user interface changes. The registered renderers
 *  are then used to render the changes to correct location.
 *
 *  @param updates Updates UIDL updates from server.
 *
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.processUpdates = function (updates) {
	if (this.debugEnabled) {
        console.group("Changes from server");
        console.time("All changes in");
	}
    if(this.profilingEnabled)
        console.profile("Changes profiling");

	try {
		// Iterate through the received changes
		var changes = updates.getElementsByTagName("change");
		var cLen = changes.length;
		this._focusedElementRendered = false;
		for (var i=0; i<cLen; i++) {
			// Render start time
            if (this.debugEnabled) {
                console.time("Change");
            }
			var change = changes.item(i);
			var paintableId = change.getAttribute("pid");
			if(paintableId == this.focusedElement)
				this._focusedElementRendered = true;
			var windowName = change.getAttribute("windowname");
			var invisible = (change.getAttribute("visible") == "false");
			var changeContent = this.getFirstChildElement(change);
			invisible = invisible || (changeContent && changeContent.getAttribute("invisible") == "true");
			var paintableName = (changeContent!= null?changeContent.nodeName:"(unknown)");
	
			if (this.debugEnabled) {
				console.group("Change "+i+" Id='"+paintableId+"' Paintable='"+paintableName+"'");
			}
					
			// Get the containing element from all current windows
			var currentNode = this.findPaintableById(paintableId);
			
			// Use window element by default for windows
			if (currentNode == null && changeContent != null) {
			
				if (
					this.mainDocument == null
					|| changeContent.nodeName == "framewindow"
					|| ( changeContent.getAttribute("style") && changeContent.getAttribute("style") == "native" )
				) {
					var winName = changeContent.getAttribute("name");				
					
					if (this.mainDocument == null) {
						// Initialize the main document/window 
						currentNode = this.mainWindowElement;
						currentNode.ownerDocument.ownerWindow = window;
						this.registerWindow(winName, window, currentNode.ownerDocument);
						this.mainDocument = currentNode.ownerDocument;
						this.mainDocument.isMainDocument = "true";
					} else {						
						// Open a new window if no document was found						
						var limit = new Date().getTime() + (1000*3);
						var height = itmill.themes.Base.prototype.getVariableElement(changeContent,"integer", "height").getAttribute("value");
						var width = itmill.themes.Base.prototype.getVariableElement(changeContent,"integer", "width").getAttribute("value");
						var features = "location=no,menubar=no,status=no,toolbar=no,resizable=yes,scrollbars=yes";
						if (height > 0 && width > 0) features += ",height=" + height + ",width=" + width;
						var win = window.open("about:blank",winName,features);
						while (new Date().getTime() < limit) {
							try  {
								var url = win.location.href;
								break;
							} catch (e) {
								// IE slow sometimes, buzy-loop for permission ( TODO better solution? )
								this.debug("Permission denied for window "+winName+", retrying.");
							} 
						}
						try {
							var url = win.location.href;
						} catch (e) {
							console.error("Could not open window:" + winName);
							win = window.open("about:blank",winName);
						}
						
						currentNode  = this.initializeNewWindow(win,changeContent);			
					}
				}
			}
			
			
			if (invisible && currentNode) {
				// Special hiding procesedure for windows
				if (windowName != null || changeContent.nodeName == "window") {
					if(currentNode && currentNode.className.indexOf("native") < 0 ) {
						// if div window
						currentNode.TkWindow.cleanUp();
						currentNode.parentNode.removeChild(currentNode);
					} else {
						// native window
						this.unregisterWindow(windowName);					
					}
				} else {
					// Hide invisble components
					currentNode.style.display = "none";
				}
			} else {
				// Make sure we are visible
				if (currentNode) currentNode.style.display = "";
			}
			
			if (currentNode == null && changeContent && changeContent.nodeName == "window") {
				// new style div window, set currentNode to mainDocument root
				currentNode = this.mainWindowElement;
			}

			if(currentNode) {
				// Process all uidl nodes inside a change
				var uidl = change.firstChild;
				while (uidl) {
					if (uidl.nodeType == Node.ELEMENT_NODE) {
						if (!currentNode) {
							currentNode = this.createPaintableElement(uidl);
						}
						if (currentNode.ownerDocument.renderUIDL) {
							currentNode.ownerDocument.renderUIDL(uidl,currentNode);
						} else { 
							this.renderUIDL(uidl,currentNode);
						}
					}
					uidl = uidl.nextSibling;
				}
			} else {
				if (this.debugEnabled)
					// warn only (this may be normal behaviour if user is closes window
					// and update for it is still on its way etc...)
					console.info("No paint target found, ignoring change")
			}
			
			if (this.debugEnabled) {
                console.info("Change " + i + " Id='"+ paintableId+ "'. Paintable='"+ paintableName +"' rendered");
                console.timeEnd("Change");
                console.groupEnd();
			}
		}
		
		// all updates are now in, check for meta tagas
		var meta = updates.getElementsByTagName("meta");
		if(meta[0]) {
			var metaEl = meta[0];
			var focusEl = metaEl.getElementsByTagName("focus");
			if(focusEl && focusEl[0]) {
				this.setFocus(focusEl[0].getAttribute("pid"));
			}
		} else if (this._focusedElementRendered) {
			// update didn't contain focus information, set the one that is
			// most recently set if it was re-rendered
			this.setFocus(this._focusedPID);
		}
	} catch (e) {
		// Print out the exception
		if (this.debugEnabled) {
        	console.error("Could not process changes: "+e.message);
            console.error(e);
 		} else {
        	console.error("Could not process changes: "+e.message);
            console.error(e);
			alert("Failed to process all changes. \n Please enable debug logging to get detailed error description");
		}
	}
	
	this.processAllLayoutFunctions();
	
    if (this.debugEnabled) {
        console.timeEnd("All changes in");
        if(this.profilingEnabled)
            console.profileEnd("Changes profiling");
        console.groupEnd();
	} else {
        if(this.profilingEnabled)
            console.profileEnd("Changes profiling");
    }
	if (this.waitElement) {
		this.waitElement.style.display = "none";
	}

}

/** Render the given UIDL to target.
 *
 *  If no renderer is specified the the internal renderer registry is 
 *  looked up for matching renderer.
 *
 *  @param uidl The UIDL node that is rendered.
 *  @param target The targer element where the result should be appended.
 *  @param renderer The specific renderer instance that should be used (optional)
 *  @return This function returns whatever the utilized renderer returns.
 *
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.renderUIDL = function (uidl, target, renderer, doublebuffer) {

	// Text nodes
	if (uidl.nodeType == Node.TEXT_NODE) {
		var text = target.ownerDocument.createTextNode(uidl.nodeValue);
		target.appendChild(text);
		return null;
	}

	// Sanity check
	if (uidl == null || uidl.nodeType != Node.ELEMENT_NODE) return;

	// Render the UIDL using the given renderer  
	if (renderer != null) {
	
		// Invoke renderer and return whatever it returns.
		
		// Function argument pass-through: 
		// Create arguments array and add all callers extra parameters 
		// to the end of arguments.
		var args = new Array();
		args.push(renderer);
		args.push(uidl);
		args.push(target);
		for(var i=3; i<arguments.length; i++) {
            args.push(arguments[i]);
        }
        try {      
			var res = renderer.renderFunction.apply(this,args);
			return res;
		} catch (e) {
			// Print out the exception
        	this.error("Could not render "+ uidl.nodeName +" using '"+ renderer.theme.themeName +"'", e);
        	return;
		}
		
		
	} else {
	
		// Lookup for renderer
		var style = uidl.getAttribute("style");
		var tag = uidl.nodeName;
		
		
		// Render standard uidl formatting
		if (tag == "b" || tag == "br" || tag == "i" || tag == 'li' || tag == 'u' ||
			tag ==  'ul' || tag == 'h1' || tag == 'h2' || tag == 'h3' || tag == 'h4' ||
			tag ==  'h5' ||  tag == 'h6') {
			var elem = target.ownerDocument.createElement(tag);
			target.appendChild(elem);
			var retval = null;
			for (var j=0; j<uidl.childNodes.length; j++) 
				var retval = this.renderUIDL(uidl.childNodes.item(j), elem, renderer, doublebuffer);
        	return retval;
        }
				
		// Render pre uidl formatting
		if (tag == 'pre') {
			var elem = target.ownerDocument.createElement(tag);
			target.appendChild(elem);
			elem.style.whiteSpace='pre';
			for (var j=0; j<uidl.childNodes.length; j++) { 
				try {
					var text = uidl.childNodes.item(j).nodeValue;
					text = text.replace(/(^\r)?\n/g, '\r\n');
					elem.appendChild(target.ownerDocument.createTextNode(text));
				} catch (e) {}
			}
        	return null;
        }
				
		var renderer = this.findRenderer(tag,style);

		// Render the UIDL using the found renderer  
		if (renderer != null) {
			
			// Function argument pass-through: 
			// Create arguments array and add all callers extra parameters 
			// to the end of arguments.
			var args = new Array();
			args[args.length] = uidl;
			args[args.length] = target;
			args[args.length] = renderer;
			for(var i=3; i<arguments.length; i++) {
	            args[args.length] = arguments[i];
	        }
			return this.renderUIDL.apply(this,args);
		}
	}

	// If no renderer is specified, render the UIDL as-is to console
    console.error("No renderer spesified");
    console.dirxml(uidl);
    
	return null;
}

/** Search the internal renderer registry for matching renderer. 
 *
 *  The matching process first looks up for exact tag and componentStyle
 *  match, but if no renderer is found it uses only the tag name matching.
 *  If still no renderer is found returns null.
 *
 *  @param tag UIDL tag name.
 *  @param componentStyle The style attribute of the component (optional)
 *  @return A matching renderer instance
 *  @type Object
 *	
 *  @author IT Mill Ltd.
 * 
 */
itmill.Client.prototype.findRenderer = function (tag, componentStyle) {
	var renderer = null;
	var rendererId = tag + (componentStyle == null ? "" : "__" + componentStyle);

	// Try to find with specific style
	if (componentStyle != null) {
        renderer = this.renderers[rendererId];
	}
	
	// Try to find a renderer only using tag
	if (renderer == null) {
		renderer = this.renderers[tag];
	}
	
	return renderer;
}

/** Returns given XML as text.
 *
 *  @param xml The XML node to be rendered as HTML.
 *  @return The XML as text
 *  @type String
 *	
 *  @author IT Mill Ltd.
 * 
 */
itmill.Client.prototype.getXMLtext = function(xml) {
    var h = new Array();
    h.push("<");
    h.push(xml.nodeName);
	if (xml.attributes.length > 0)
		for(var i=0; i<xml.attributes.length; i++) {
		var a = xml.attributes.item(i);
        h.push(" ");
        h.push(a.name);
        h.push('="');
        h.push(a.value);
        h.push('"');
	}
    h.push(">");
	if (xml.hasChildNodes())
	for (var i=0; i<xml.childNodes.length; i++) { 
		var c = xml.childNodes.item(i);
		if (c.nodeType == Node.ELEMENT_NODE) {
            h.push(this.getXMLtext(c));
		} else if (c.nodeType == Node.TEXT_NODE && c.data != null) {
            h.push(c.data);
		}
	}
    h.push("</");
    h.push(xml.nodeName);
    h.push(">");
    return h.join("");
}

/** Send a change variable event to server.
 *
 *	Changes a variable value and if 'immediate' is true invokes the
 *	processVariableChanges function.
 *
 *  @param name The name of the variable to change.
 *  @param value New value of the variable.
 *  @param immediate True if the variable change should immediately propagate to server.
 *  @param nowait True if the wait-window should not be shown
 * 
 *  @author IT Mill Ltd.
 * 
 */
itmill.Client.prototype.changeVariable = function (name, value, immediate, nowait) {
	this.debug("variableChange('" + name + "', '" + value + "', " + immediate + ");");

	this.variableStates[name] = value;
	
	if (immediate) 
		this.processVariableChanges(false,nowait);
}

/** Create new containing element for a paintable (component).
 *
 *  This function creates new containing element for a single 
 *  paintable object, typically component.
 *
 *  @param uidl The UIDL node of the paintable.
 *  @param target The target node where the new containing element should be 
 *			appended to as child.
 *  @return The newly created element node.
 *  @type Node
 *
 *  @author IT Mill Ltd.
 * 
 */
itmill.Client.prototype.createPaintableElement = function (uidl, target) {

	// Create DIV as container to right document.
	var div = this.createElement("div", target);

	// Append to parent, if 'target' parameter specified
	if (target != null) {
		target.appendChild(div);
	}
	
	// Add ID attribute	
	var pid = uidl.getAttribute("id");
	if (target != null && pid != null) {
		div.setAttribute("id",pid);
	}
    
    // Create a varMap has for elements variables
    div.varMap = new Object();
	
	// add reference to client for use in event listeners
	div.client = this;
	
	// Set visibility
	var invisible = uidl.getAttribute("invisible");
	if (target != null && invisible == "true") {
		div.style.display = "none";
	} else {
		div.style.display = "";
	}
	// Return reference to newly created div
	return div;	
}


/** Assigns given CSS class to element.
 *  Cross-browser function for assigning CSS class attribute to
 *  an element.
 *  @param element The element where the class should be applied.
 *  @param className The CSS class name to apply.
 *  @return element.
 *  @type Node
 * 
 *  @author IT Mill Ltd.
 *
 */
itmill.Client.prototype.setElementClassName = function(element,className) {
	if (element == null) { return; }
		element.style.className = className;
}

/**
 *   Add event listener function to an element.
 *  
 *   @param element      The element
 *   @param type         Type of event to listen for [click|mouseover|mouseout|...]
 *   @param func         The function to call. Called with single parameter: event.
 *   @param id			 A unique id for the function (element-scope) for easy removal (optional).
 *  
 *   @return the listener function added
 */
itmill.Client.prototype.addEventListener = function(element,type,func,id) {
	if (element.addEventListener) {
		element.addEventListener(type, func, false);
		
	} else if (element.attachEvent) {
		element.attachEvent("on" + type, func);
		
	} else {
		element['on'+type] =  func;
	}
		
	//  TODO add only to paintable?
	
	// ID-to-type map
	if (!element.eventIDMap) element.eventIDMap = new Object();
	element.eventIDMap[id] = type;
	
	if (!element.eventMap) element.eventMap = new Object();
	if (!element.eventMap[id||type]) element.eventMap[id||type] = new Array();
	element.eventMap[id||type][element.eventMap[id||type].length] = func;
	
	return func;
}
/**
 *   Remove event listener function from a element. The parameters should match addEventListener()
 *  
 *   @param element      The element
 *   @param type         Type of event to listen for [click|mouseover|mouseout|...]
 *   @param func         The listener function to remove.
 *   @param id			 Id of the function to remove (optional).
 *  
 */
itmill.Client.prototype.removeEventListener = function(element,type,func,id) {
	
	if(id) {
	
		if (element.eventMap && element.eventMap[id]) {
			func = element.eventMap[id][0];
			type = element.eventIDMap[id];
		}
		
	} else {
		
		if (element.eventMap && element.eventMap[type]) {
			for (var f in element.eventMap[type]) {
				if (element.eventMap[type][f]==func) {
					element.eventMap[type][f] = null;
					break;
				}
			}
		}
		
	}
	
	if (element.removeEventListener) {
			element.removeEventListener(type, func, false);
			
		} else if (element.detachEvent) {
			element.detachEvent("on" + type, func);
			
		} else {
			element['on'+type] =  null;
		}
		
}

/**
 *   Remove all event listener functions from a element.
 *  
 *   @param element      The element
 *   @param type         Type of event to listen for [click|mouseover|mouseout|...]
 *   @param func         The listener function to remove.
 *  
 */
 itmill.Client.prototype.removeAllEventListeners = function(element) {
 	var removed = 0;
	if (element.eventMap) {
		for (var t in element.eventMap) {
			var i = element.eventMap[t].length;
			while (i--) {
				this.removeEventListener(element,t,element.eventMap[t][i]);
				removed++;
			}
		}
		
		element.eventMap = null;
	}
	// TODO eventMAp -> paintable & only get DIV:s
	var childs = element.getElementsByTagName("*");
	if (childs) {
		var i = childs.length;
		while (i--) {
			element = childs[i];
			if (element.eventMap) {
				for (var t in element.eventMap) {
					var j = element.eventMap[t].length;
					while (j--) {
						this.removeEventListener(element,t,element.eventMap[t][j]);
						removed++;
					}
				}
				
				element.eventMap = null;
			}
		}
	}
	
	return removed;
}

itmill.Client.prototype.registerLayoutFunction = function (paintableElement,func) {
	if (!paintableElement || !func) {
		this.error("Invalid layout function registration; paintableElement:"+paintableElement+" func:"+func);
		return;
	}
	
	var pid = paintableElement.id;
	if (!pid) {
		this.error("Register layout function; paintableElement pid not found!" + paintableElement);
		return;
	}
	
	if (!this.layoutFunctionsOrder) {
		this.layoutFunctionsOrder = new Object();
		this.layoutFunctions = new Array();
	}
		
	var idx = this.layoutFunctionsOrder[pid];
	if (typeof(idx) == "undefined") idx = this.layoutFunctions.length;
	
	this.layoutFunctionsOrder[pid] = idx;
	this.layoutFunctions[idx] = func;

	this.debug("Registered layout function for ("+paintableElement.nodeName+") pid " + pid + " as number " + idx);
}
itmill.Client.prototype.unregisterLayoutFunction = function (paintableElement) {
	if (!paintableElement) {
		this.error("unregisterLayoutFunction(): NULL paintableElement!");
		return false;
	}
	
	if (!this.layoutFunctionsOrder) {
		// no functions at all
		return false;
	}
	
	var pid = paintableElement.id;
	if (!pid) {
		this.error("unregisterLayoutFunction(): paintableElement pid not found!" + paintableElement);
		return false;
	}
	
	var idx = this.layoutFunctionsOrder[pid];
	if (typeof(idx) == "undefined") {
		// no registered function
		return false;
	}
	
	this.layoutFunctions[idx] = null;
	delete this.layoutFunctionsOrder[pid];

	this.debug("Unregistered layout function " + pid);

	return true;
}
itmill.Client.prototype.unregisterAllLayoutFunctions = function (paintableElement) {
	var removed = 0;
	if (!paintableElement) {
		removed = (this.layoutFunctions?this.layoutFunctions.length:0);
		this.layoutFunctions = null;
		this.layoutFunctionsOrder = null;
		this.debug("Unregistered ALL layout functions!");
		return removed;
	}
	
	
	
	if (paintableElement.id) {
		if (this.unregisterLayoutFunction(paintableElement)) removed++
	}
	var cn = paintableElement.getElementsByTagName("div");
	if (cn) {
		var len = cn.length;
		for (var i=0;i<len;i++) {
			if (cn[i].id) {
				if (this.unregisterLayoutFunction(cn[i])) removed++
			}
		}
	}
	
	return removed;
}
itmill.Client.prototype.processAllLayoutFunctions = function() {
	if (this.layoutFunctions) {
		this.debug("Processing layout functions...");
		var lf = this.layoutFunctions;
		var lfo = this.layoutFunctionsOrder;
		var cnt = 0;
		for (var pid in lfo) {
			var idx = lfo[pid];
			var func = lf[idx];
			try {
				func();
				cnt++;
			} catch (e) {
				this.error("Layout function "+pid+" failed; "+ e + " Removing.");
				delete this.layoutFunctionsOrder[pid];
				this.layoutFunctions[idx] = null;
			}
		}
		this.debug("...processed " + cnt + " successfully");
	}
}


/** Returns a cross-browser object with useful event properties.
 * 
 * @deprecated use same function in itmill.lib
 * 
 * e: 					the ('raw') event  
 * type:				event type
 * target:				the target element
 * targetX:				X-position of the target element
 * targetY:				Y-position of the target element
 * key:					pressed key character
 * alt:					true if ALT -key was held
 * shift:				true if SHIFT -key was held
 * ctrl:				true if CTRL -key was held
 * rightclick:			true if the right mousebutton was clicked, or ctrl held while clicking
 * mouseX:				X-position of the mouse
 * mouseY:				Y-position of the mouse
 *
 *  @param e			The event, null for window.event (IE)
 *
 *	@return Properties object.  
 */
itmill.Client.prototype.getEvent = function(e) {
	var props = new Object()

	if (!e) var e = window.event;
	props.e = e;
	props.type = e.type;
	
	var targ;	
	if (e.target) { 
		targ = e.target;
	} else if (e.srcElement) { 
		targ = e.srcElement;
	}
	if (targ.nodeType == 3) {
		targ = targ.parentNode;
	}
	props.target = targ;
	var p = this.getElementPosition(targ);
	props.targetX = p.x;
	props.targetY = p.y;
	
	var code;
	if (e.keyCode) {
	 code = e.keyCode;
	} else if (e.which) {
		code = e.which;
	}
	if (code) {
		props.key = String.fromCharCode(code);
	}
	
	props.alt = e.altKey;
	props.ctrl = e.ctrlKey;
	props.shift = e.shiftKey;
	
	var rightclick;
	if (e.which) {
		rightclick = (e.which == 3 || (props.ctrl));
	} else if (e.button) {
		rightclick = (e.button == 2|| (props.ctrl));
	}
	props.rightclick = rightclick;
	
	if (e.pageX || e.pageY) 	{
		props.mouseX = e.pageX;
		props.mouseY = e.pageY;
	} else if (e.clientX || e.clientY) 	{
		props.mouseX = e.clientX + document.body.scrollLeft
			+ document.documentElement.scrollLeft;
		props.mouseY = e.clientY + document.body.scrollTop
			+ document.documentElement.scrollTop;
	}
	
	props.stop = function() {
		e.cancelBubble = true;
		if (e.stopPropagation) e.stopPropagation();
		if (e.preventDefault) e.preventDefault();
		return false;
	}
	
	return props;
}

/**
 * This method should be called when element receives focus. Client stores reference to
 * currently focused element PID, so it can restore focus if element gets re-rendered
 */
itmill.Client.prototype.setFocusedElement = function(el) {
	if(el && el.id) {
		this._focusedPID = el.id;
	}
}

itmill.Client.prototype.setFocus = function(pid) {
	var el = this.findPaintableById(pid);
	if(el) {
		// run custom focus handler if one exists, else focus paintable div
		if(el._onfocus)
			el._onfocus();
		else
			el.focus();
	}
}

/**
 * Helper method for Themes that returns htmlElments closest parent that is Paintable 
 * (DIV && has -"varMap" property) or null if not found
 *
 * TODO This function is in totally wrong place, should be in html-helper-lib.js 
 * or something
 */
 itmill.Client.prototype.getPaintable = function(el) {
 	while(typeof el.varMap == "undefined" && el.parentNode != el.ownerDocument) {
 		el = el.parentNode;
 	}
 	if(el.varMap)
 		return el;
	else 
		return null;
}

/**
 *  TODO This function is in totally wrong place, should be in html-helper-lib.js
 */
itmill.Client.prototype.getElementPosition = function(element) {
	var props = new Object();
// TODO scroll offsets testing in IE
	var obj = element;
	var x = obj.offsetLeft + (obj.scrollLeft||0);
	var y = obj.offsetTop + (obj.scrollTop||0);
	if (obj.parentNode||obj.offsetParent) {
		while (obj.offsetParent||obj.parentNode) {
            obj = obj.offsetParent||obj.parentNode;
			if (obj.nodeName == "TBODY") continue;
			x += (obj.offsetLeft||0) - (obj.scrollLeft||0);
			y += (obj.offsetTop||0) - (obj.scrollTop||0);
		}
	} else if (obj.x) {
		x += obj.x;
		y += obj.y;
	}
	props.x = x;
	props.y = y;
	props.h = element.offsetHeight;
	props.w = element.offsetWidth;
	
	return props;
}

/** Prints objects properties into separate window.
 *
 *  @obj Object to be printed
 *  @level recursion level
 * @deprecated TO BE REMOVED DUE FIREBUG IS OUR DEFAULT DEBUGGING METHOD NOWDAYS
 */
itmill.Client.prototype.debugObjectWindow = function(obj,level) {

	// Default level
	if (level == null) {
		level = 2;
	}
	
	//print into string
	var str = this.printObject(obj,level);
	
	// open a window for debug
	var win = window.open("", "ms_ajax_debug");
	win.document.open();
	win.document.write("<html><body>"+str+"+</body></html>");
	win.document.close();
}

/** 
 * Print a object instace as html string.
 * Prints (recursively) the objects properties into a html table.
 *  
 * @param obj Object to be printed
 * @param level recursion level
 * @deprecated TO BE REMOVED DUE FIREBUG IS OUR DEFAULT DEBUGGING METHOD NOWDAYS
 */
itmill.Client.prototype.printObject = function(obj,level) {
	if (level == null || level < 1) {
		level = 1;
	}

	var str = "<table border=\"0\"><tr><td colspan=\"3\">Object: "+obj+"<hr /></td></tr>";
	try {	
		for (var prop in obj) { 
			str += "<tr><td valign=\"top\">"+prop+"</td><td valign=\"top\"> = </td><td valign=\"top\">";
			try { 
				if (typeof obj[prop] == "object" && level > 1) {
					str += this.printObject(obj[prop],level-1);
				} else {
					str += obj[prop];
				}
			} catch(ignored) {
				str += "[EVAL FAILED]";
			} 
			str += "</td></tr>";
		}
	} catch (e) {
		str += "<tr><td colspan=\"3\">[Failed to list object properties: "+e.message+"]</td></tr>"
	}
	str += "</table>";
	return str;
}

/**
 * One client has only one context menu object. This is called by hanglers which
 * want to populate and show it.
 * 
 * @return {itmill.ui.ContextMenu} Returns clients context menu object
 */
itmill.Client.prototype.getContextMenu = function() {
	if(!this.contextMenu) {
		this.contextMenu = new itmill.ui.ContextMenu();
		this.contextMenu.appendTo(this.mainWindowElement);
	}
	return this.contextMenu;
}

/**
 * Method to register shorcut key handler
 * 
 * @param shortcut Shortcut object to be added
 */
itmill.Client.prototype.addShortcutHandler = function(shortcut) {
	// TODO
	// this should find the right container element where shortcuts should be hooked
	// now prototyping by hooking all events to document
	var body = document.body;
	if(!body.shortcutMap) {
		body.shortcutMap = new Array();
	}
	body.shortcutMap.push(shortcut);
	// TODO remove this, containers that catch key clicks should do this on render phase
	if(!body.shortcutEventAdded) {
		console.log("adding shortcut event handler");
		this.addEventListener(body,"keydown",itmill.ui._shortcutHandler);
		body.shortcutEventAdded = true;
	}
}

/** Createsa text node to the same document as target.
 *  If target is null or not given the document reference is 
 *  used instead.
 *  
 *  @target Target element
 *  @text Textnode content
 */
itmill.Client.prototype.createTextNode = function(text, target) {
	if (target != null && target.ownerDocument != null) {
		return target.ownerDocument.createTextNode(text);
	} else {
		return document.createTextNode(text);
	}
}

/** Creates a element node to the same document as target.
 *  If target is null or not given the document reference is 
 *  used instead.
 *  
 *  @nodeName Element nodeName
 *  @text Textnode content
 */
itmill.Client.prototype.createElement = function(nodeName, target) {
	
	if (target != null && target.ownerDocument != null) {
		return target.ownerDocument.createElement(nodeName);
	} else {
		return document.createElement(nodeName);
	}
}

/**
 * This is a class that provides browser detection and some
 * crossbrowser functions needed in various components.
 * 
 * TODO this is in  wrong file
 */
itmill.WebBrowser = function() {
	if(document.all && !window.opera) {
		this.isIE = true;
		if(window.XMLHttpRequest)
			this.isIE7 = true;
		else
			this.isIE6 = true;
	}
	if(window.opera)
		this.isOpera = true;
	var agent = navigator.userAgent;
	if(agent.indexOf("Webkit") > 0 ) {
		this.isWebkit = true;
	} 

	if(agent.indexOf("Mac") > 0 ) {
		this.isMac = true;
	}
	if(agent.indexOf("Linux") > 0 ) {
		this.isLinux = true;
	}
	// TODO: Refine this, assumes FF if not opera, IE or webkit
	if(!this.isIE && !this.isOpera && !this.isWebkit)
		this.isFF = true;
}

itmill.WebBrowser.prototype.getWindowWidth = function() {
	return document.documentElement.clientWidth;
}

itmill.WebBrowser.prototype.getWindowHeight = function() {
	if(self.innerHeight) {
		return self.innerHeight;
	}
	else if(window.opera) {
		return document.documentElement.scrollHeight;
	} else {
		return document.documentElement.clientHeight;
	}
}

/* 
 * Various static general purpose functions that don't 
 * belong to client or theme are stored in itmill.lib.
 */
itmill.lib = new Object(); // create namespace
 
 /** Returns a cross-browser object with useful event properties.
 * 
 * @deprecated use same function in itmill.lib
 * 
 * e: 					the ('raw') event  
 * type:				event type
 * target:				the target element
 * targetX:				X-position of the target element
 * targetY:				Y-position of the target element
 * key:					pressed key character
 * alt:					true if ALT -key was held
 * shift:				true if SHIFT -key was held
 * ctrl:				true if CTRL -key was held
 * rightclick:			true if the right mousebutton was clicked, or ctrl held while clicking
 * mouseX:				X-position of the mouse
 * mouseY:				Y-position of the mouse
 *
 *  @param e			The event, null for window.event (IE)
 *
 *	@return Properties object.  
 */
itmill.lib.getEvent = function(e) {
	var props = new Object()

	if (!e) var e = window.event;
	props.e = e;
	props.type = e.type;
	
	var targ;	
	if (e.target) { 
		targ = e.target;
	} else if (e.srcElement) { 
		targ = e.srcElement;
	}
	if (targ.nodeType == 3) {
		targ = targ.parentNode;
	}
	props.target = targ;
	var p = itmill.lib.getElementPosition(targ);
	props.targetX = p.x;
	props.targetY = p.y;
	
	var code;
	if (e.keyCode) {
	 code = e.keyCode;
	} else if (e.which) {
		code = e.which;
	}
	if (code) {
		props.key = String.fromCharCode(code);
	}
	
	props.alt = e.altKey;
	props.ctrl = e.ctrlKey;
	props.shift = e.shiftKey;
	
	var rightclick;
	if (e.which) {
		rightclick = (e.which == 3 || (props.ctrl));
	} else if (e.button) {
		rightclick = (e.button == 2|| (props.ctrl));
	}
	props.rightclick = rightclick;
	
	if (e.pageX || e.pageY) 	{
		props.mouseX = e.pageX;
		props.mouseY = e.pageY;
	} else if (e.clientX || e.clientY) 	{
		props.mouseX = e.clientX + document.body.scrollLeft
			+ document.documentElement.scrollLeft;
		props.mouseY = e.clientY + document.body.scrollTop
			+ document.documentElement.scrollTop;
	}
	
	props.stop = function() {
		e.cancelBubble = true;
		if (e.stopPropagation) e.stopPropagation();
		if (e.preventDefault) e.preventDefault();
		return false;
	}
	
	return props;
}

itmill.lib.getElementPosition = function(element) {
	var props = new Object();
// TODO scroll offsets testing in IE
	var obj = element;
	var x = obj.offsetLeft + (obj.scrollLeft||0);
	var y = obj.offsetTop + (obj.scrollTop||0);
	if (obj.parentNode||obj.offsetParent) {
		while (obj.offsetParent||obj.parentNode) {
            obj = obj.offsetParent||obj.parentNode;
			if (obj.nodeName == "TBODY") continue;
			x += (obj.offsetLeft||0) - (obj.scrollLeft||0);
			y += (obj.offsetTop||0) - (obj.scrollTop||0);
		}
	} else if (obj.x) {
		x += obj.x;
		y += obj.y;
	}
	props.x = x;
	props.y = y;
	props.h = element.offsetHeight;
	props.w = element.offsetWidth;
	
	return props;
}


 /**
  * Utility function for event handlers that returns a client object
  * that owns element.
  * 
  * Iterates through html elements until finds a paintable element and returns
  * client reference from it.
  * 
  * @param htmlElement element to be inspected, usually got from evt.target
  * @return Client object for this element
  * 
  */
 itmill.lib.getClient = function(htmlElement) {
 	var pntbl = itmill.lib.getPaintable(htmlElement);
 	return pntbl ? pntbl.client : null;
 }
 
 /**
 * Helper method  that returns htmlElments closest parent that is Paintable 
 * (DIV && has -"varMap" property) or null if not found
 *
 * @param el html element
 * @return Paintable div element
 */
 itmill.lib.getPaintable = function(el) {
 	while(typeof el.varMap == "undefined" && el.parentNode != el.ownerDocument) {
 		el = el.parentNode;
 	}
 	if(el.varMap)
 		return el;
	else 
		return null;
} 
 
 /**
 * Returns a ...Base.TkWindow object that contains given HTMLElement object  or null
 * if not found
 */
 itmill.lib.getTkWindow = function(el) {
 	while(typeof el.TkWindow == "undefined" && el.parentNode != el.ownerDocument) {
 		el = el.parentNode;
 	}
 	if(el.TkWindow)
 		return el.TkWindow;
	else 
		return null;
}
 
 
/** Class that implements inheritance mechanism for themes */

itmill.Class = function() {};

itmill.Class.prototype.construct = function() {};

itmill.Class.extend = function(def) {
	
	 var classDef = function() {
        if (arguments[0] !== itmill.Class) { this.construct.apply(this, arguments); }
    };
    
    var proto = new this(itmill.Class);
    var superClass = this.prototype;
    
    for (var n in def) {
        var item = def[n];  
        if (item instanceof Function) item.$ = superClass;
        proto[n] = item;
    }

    classDef.prototype = proto;
    
    //Give this new class the same static extend method    
    classDef.extend = this.extend;        
    return classDef;

}

/*
 * Add indexOf funtion to Array objects in case of lousy browser (IE or Safari)
 */
if(!Array.prototype.indexOf) {
	Array.prototype.indexOf = function(value) {
		for(var i = 0; i < this.length; i++) {
 			if(this[i] == value) {
 				return i;
 			}
 		}
 		return -1;
 	}
}

