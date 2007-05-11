if(document.all && !window.opera) {
	Node = new Object();
	Node.ELEMENT_NODE = 1;
	Node.ATTRIBUTE_NODE = 2;
	Node.TEXT_NODE = 3;
	Node.CDATA_SECTION_NODE = 4;
	Node.ENTITY_REFERENCE_NODE = 5;
	Node.ENTITY_NODE = 6;
	Node.PROCESSING_INSTRUCTION_NODE = 7;
	Node.COMMENT_NODE = 8;
	Node.DOCUMENT_NODE = 9;
	Node.DOCUMENT_TYPE_NODE = 10;
	Node.DOCUMENT_FRAGMENT_NODE = 11;
	Node.NOTATION_NODE = 12;
}

/** Base theme class extends ITMillToolkitClient.Theme */
itmill.themes.Base = itmill.Class.extend( {

	
/** Constructor
 *  @param themeRoot The base URL for theme resources.
 *  @constructor
*/
construct : function(themeRoot) {
	this.themeName = "base";

	// Store the the root URL
	this.root = themeRoot;
},

/** Register all renderers to a ajax client.
 *
 * @param client The ajax client instance.
 */
registerTo : function(client) {

	// This hides all integer, string, etc. variables
	client.registerRenderer(this,"integer",null,function() {});
	client.registerRenderer(this,"string",null,function() {});
	client.registerRenderer(this,"boolean",null,function() {});
	// and special tags
	client.registerRenderer(this,"description",null,function() {});
	client.registerRenderer(this,"error",null,function() {});
	client.registerRenderer(this,"actions",null,function() {});
		
	
	// Register renderer functions
	client.registerRenderer(this,"component",null,this.renderComponent);
	client.registerRenderer(this,"label",null,this.renderLabel);
	client.registerRenderer(this,"data",null,this.renderData);
	client.registerRenderer(this,"pre",null,this.renderData);
	client.registerRenderer(this,"link",null,this.renderLink);
	client.registerRenderer(this,"button",null,this.renderButton);
	client.registerRenderer(this,"textfield",null,this.renderTextField);
	client.registerRenderer(this,"datefield",null,this.renderDateField);
	client.registerRenderer(this,"datefield","calendar",this.renderDateFieldCalendar);
	client.registerRenderer(this,"select",null,this.renderSelect);
	client.registerRenderer(this,"select","optiongroup",this.renderSelectOptionGroup);
	client.registerRenderer(this,"select","horizontalOptiongroup",this.renderSelectOptionGroup);
	client.registerRenderer(this,"select","twincol",this.renderSelectTwincol);
	client.registerRenderer(this,"upload",null,this.renderUpload);
	client.registerRenderer(this,"embedded",null,this.renderEmbedded);

	client.registerRenderer(this,"framewindow",null,this.renderFramewindow);
	client.registerRenderer(this,"open",null,this.renderOpen);
	
	client.registerRenderer(this,"panel",null,this.renderPanel);
	client.registerRenderer(this,"orderedlayout",null,this.renderOrderedLayout);
	client.registerRenderer(this,"customlayout",null,this.renderCustomLayout);
	client.registerRenderer(this,"gridlayout",null,this.renderGridLayout);
	client.registerRenderer(this,"tabsheet",null,this.renderTabSheet);
    client.registerRenderer(this,"progressindicator",null,this.renderProgressIndicator);

	client.registerRenderer(this,"table",null,this.renderScrollTable);
    client.registerRenderer(this,"table","paging",this.renderPagingTable);
    client.registerRenderer(this,"table","list",this.renderPagingTable);
	client.registerRenderer(this,"tree",null,this.renderTree);
	client.registerRenderer(this,"tree","coolmenu",this.renderTreeMenu);

	client.registerRenderer(this,"window",null,this.renderWindow);
    
    // Usually functions here are run so that "this" means client, but
    // some of functions are run from themes Scope and need client reference
    this.client = client;
},


/* 
#### DOM functions ########################################################
*/

createElementTo : function (target, tagName, cssClass) {

	if (target == null) return null;

	// Create the requested element
	var e = target.ownerDocument.createElement(tagName);
	
	// Set CSS class if specified
	if (cssClass) {
		this.setCSSClass(e,cssClass);	
	}
	
	// Append to parent
	target.appendChild(e);
	
	return e;
},

/**
 * All uri's grabbed from uidl, should be filtered through this function, which
 * makes them browsers compatible. In uidl we may have uri's that start with
 * 'theme://' that are converted for browsers.
 */
parseResourceUri : function (iconUrl) {
	if (iconUrl && iconUrl.indexOf("theme://") == 0) {
    	iconUrl = (this.iconRoot != null ? this.iconRoot : this.root) 
    				+ iconUrl.substring(8);
    }
	return iconUrl
},

createTextNodeTo : function (target,text) {

	// Sanity check
	if (text == null || target == null) return null;

	var tn = target.ownerDocument.createTextNode(text);

	// Append to parent
	target.appendChild(tn);
		
	return tn;
},

getFirstElement : function(parent, elementName) {
	if (parent && parent.childNodes) {
		for (var i=0;i<parent.childNodes.length;i++) {
			if (parent.childNodes[i].nodeName == elementName) {
				return parent.childNodes[i];
			}
		}
	}
	return null;
},

getFirstTextNode : function(parent) {
	if (parent == null || parent.childNodes == null) return null;
	
	var cns = parent.childNodes;
	var len = cns.length;
	for (var i=0; i<len; i++) {
		var child = cns[i];
		if (child.nodeType == Node.TEXT_NODE) {
			return child;
		}
	}
	
},

/**
 *   Removes all children of an element an element.
 *  
 *   @param element      Remove children of this element. 
 *  
 *   @return the element with children removed
 */
removeAllChildNodes : function(element) {
	this.client.removeAllEventListeners(element);
	while (element.childNodes&&element.childNodes.length > 0) {
		element.removeChild(element.childNodes[0]);
	}
	return element;
},

getElementContent : function(parent, elementName) {
	if (elementName != null) {
		// Find element and return its content		
		var n = this.getFirstElement(parent,elementName);
		if (n == null) return null;
		var tn = this.getFirstTextNode(n);
		if (tn != null && tn.data != null) {
			return tn.data;
		}
		return "";	
	} else {
		// If no element name is given return
		// content of parent
		var tn = this.getFirstTextNode(parent);
		if (tn != null && tn.data != null) {
			return tn.data;
		}
		return "";	
	}
},

getChildElements : function(parent, tagName) {
	
	if (parent == null || parent.childNodes == null || tagName == null) return null;

	// Iterate all child nodes
	var res = new Array();
	for (var i=0; i < parent.childNodes.length; i++) {
		var n = parent.childNodes[i];
		if (n.nodeType == Node.ELEMENT_NODE && n.nodeName == tagName) {
			res[res.length++] = n;
		}
	}
	return res;	
},

nodeToString : function(node, deep) {

	if (node == null) {
		return "";
	} else if (node.nodeType == Node.TEXT_NODE) {
		// Render text nodes.
		if (node.data) {
			return node.data;
		} else {
			return "";
		}
	
	} else if (node.nodeType == Node.ELEMENT_NODE) {	
		
		// Renderer element nodes.
		var txt = "<" + node.nodeName;
		if (node.attributes.length > 0)
			for(var i=0; i<node.attributes.length; i++) {
			var a = node.attributes.item(i);
			txt += " " + a.name + "=\"" + a.value+"\"";
		}
		if (deep && node.childNodes != null && node.childNodes.length >0) {
			txt += ">";
			for (var i=0; i<node.childNodes.length; i++) { 
				var c = node.childNodes.item(i);
				txt += this.nodeToString(c,deep);			
			}
			txt += "</"+node.nodeName+">";  
		} else {
			txt += "/>";  
		}
	  	return txt;
	  }
	  
	  return ""+node.nodeName + "-node";
},

createInputElementTo : function(target,type,className,focusid) {
	
	var input = null;
	var appendInEnd = false;
	if (document.all && !window.opera) {
		// IE only
		input = this.createElementTo(target,"<input type='"+type+"'>");
	} else {
		// Other browsers
        input = document.createElement("input");
        input.type = type;
        appendInEnd = true;
	}
	
	// Assign class
	if (className != null && className != "") {
		this.setCSSClass(input,className);
	}
	
	if (focusid) input.focusid = focusid;

	if (appendInEnd)  target.appendChild(input);

	return input;
},

/* 
#### CSS functions ###################################################### 
*/

addCSSClass : function(element, className) {
	if (element == null) return element;
	if (element.className) {
		var classArray = element.className.split(" ");
		for (var i in classArray) {
			if (classArray[i]==className) {
				// allready in className
				return element;
			}
		} 
	}
	element.className = (element.className?element.className:"") + " " + className;
	return element;	
},

removeCSSClass : function(element, className) {
	if (element == null) return element;
	var classArray = new Array();
	if (element.className) {
		classArray = element.className.split(" ");
	}
	var newArray = new Array();
	for (var i in classArray) {
		if (classArray[i]!=className) {
			newArray[newArray.length] = classArray[i];
		}
	} 
	element.className = newArray.join(" ");
	return element;	
},

toggleCSSClass : function(element, className) {
	if (element == null) return element;

	var classArray = new Array();
	if (element.className) {
		classArray = element.className.split(" ");
	}
	for (var i=0;i<classArray.length;i++) {
		if (classArray[i]==className) {
			this.removeCSSClass(element, className);
			return;
		}
	}	
	this.addCSSClass(element, className);
	
	return element;	
},

setCSSClass : function(element, className) {
	if (element == null) return element;
	element.className = className;
	return element;	
},

setCSSDefaultClass : function(renderer,element,uidl) {
	if (element == null) return element;
	var cn = this.styleToCSSClass(renderer.tag,uidl.getAttribute("style"));
	element.className = cn;
	return element;	
},

styleToCSSClass : function(prefix,style) {

	var s = "";
	if (prefix != null) {
		s = prefix;
	}
  	if (style != null) {
  		if (s.length > 0) {
  			s = s + "-";
  		}
  		s = s + style + " " + prefix;
  	}
  	return s
},

/* 
#### Generic JS helpers ##################################################
*/

/**
 *   Check if integer list contains a number.
 *  
 *   @param list         Comma separated list of integers
 *   @param number       Number to be tested
 *  
 *   @return true iff the number can be found in the list
 */
listContainsInt : function(list,number) {
  if (!list) return false;
  var a = list.split(",");

  for (var i = 0;i<a.length;i++) {
    if (a[i] == number) return true;
  }
  
  return false;
},

/** Add number to integer list, if it does not exit before.
 *  
 *  
 *  @param list         Comma separated list of integers
 *  @param number       Number to be added
 *  
 *  @return new list
 */
listAddInt : function(list,number) {

  if (this.listContainsInt(list,number)) 
    return list;
    
  if (list == "") return number;
  else return list + "," + number;
},

/** Remove number from integer list.
 *  
 *  @param list         Comma separated list of integers
 *  @param number       Number to be removed
 *  
 *  @return new list
 */
listRemoveInt : function(list,number) {
	if (!list) return "";
	retval = "";
	var a = list.split(',');

	for (var i=0;i<a.length;i++) {
		if (a[i] != number) {
  			if (i == 0) retval += a[i];
  			else retval += "," + a[i];
    	}
  	}
	return retval;
},

/* 
#### Variable helpers #############################################
*/

/**
 * Fetches reference to elements variable
 *
 * @param paintable Element which variable is to be fetched
 * @param varId variables id or name
 */
getVar : function(paintable, varId) {
    return paintable.varMap[varId];
}, 

/**
 * Tells client that variable has changed
 *
 * @param paintable Element which variable is to be changed
 * @param varId variables id or name
 * @param value value to be stored
 * @param immediate flag if variable change should be sent to server immediatedly
 */
updateVar : function(client,variable, immediate) {
    client.changeVariable(variable.id, variable.value, immediate);
},

/**
 * Creates local variable to paintable from uidl
 * @param paintable Paintable element where variable is stored
 * @param variableUidl uidl fraction where variable is parsed
 * @return varId
 */
createVarFromUidl : function(paintable, variableUidl) {
    if (!variableUidl) {
        return null;
    }
    var variable = new Object();
    variable.id = variableUidl.getAttribute("id");
    variable.name = variableUidl.getAttribute("name");
    variable.type = variableUidl.nodeName;
    // TODO arrays could be handled as real js arrays and lot of old functions below could be removed
    if (variable.type == "array") {
        variable.value = new Array();
        var items = variableUidl.getElementsByTagName("ai");
        if (items != null) {
            for (var i=0; i < items.length; i++) {
                var v = this.getFirstTextNode(items[i]); 
                if (v != null && v.data != null) {
                    variable.value.push(v.data);
                }
            } 
        }
        variable.id = "array:" + variable.id;
    } else if (variable.type == "string") {
        var node = this.getFirstTextNode(variableUidl);
        variable.value = (node?node.data:"");
    } else if (variable.type == "boolean") {
        if (variableUidl.getAttribute("value") == "true") {
            variable.value = true;
        } else { 
            variable.value = false;
        }
    } else {
        variable.value = variableUidl.getAttribute("value");
    }
    // variable can be fetched with both id and name
    paintable.varMap[variable.id] = variable;
    paintable.varMap[variable.name] = variable;
    return variable;
},


// variable helpers below are deprecated, move tovards storing variables in paintables varMap object
// and function above

getVariableElement : function(uidl,type,name) {

	if (uidl == null) return;
	
	var nodes = this.getChildElements(uidl,type);
	if (nodes != null) {
		for (var i=0; i < nodes.length; i++) {
			if (nodes[i].getAttribute("name") == name) {				
				return nodes[i];
			}
		}
	}
	return null;	
},

createVariableElementTo : function(target,variableElement) {
	if (!variableElement) {
		return null;
	}
	var input = this.createInputElementTo(target,"hidden");
	input.variableId = variableElement.getAttribute("id");
	input.variableName = variableElement.getAttribute("name");
	if (variableElement.nodeName == "array") {
	    input.variableId = "array:"+input.variableId;
		input.value = this.arrayToList(variableElement);
	} else if (variableElement.nodeName == "string") {
		var node = this.getFirstTextNode(variableElement);
		input.value = (node?node.data:"");
	} else {
		input.value = variableElement.getAttribute("value");
	}
	return input;
},

getVariableElementValue : function(variableElement) {
	if ( variableElement == null) {
		return null;
	}
	
	if (variableElement.nodeName == "array") {
		return this.arrayToList(variableElement);
	} else if (variableElement.nodeName == "string") {
		var node = this.getFirstTextNode(variableElement);
		return (node?node.data:"");
	} else {
		return variableElement.getAttribute("value");
	}
	return null;
},

setVariable : function(client, variableNode, newValue, immediate) {
	if (variableNode == null) return;
	variableNode.value = newValue;
	client.changeVariable(variableNode.variableId, newValue, immediate);
},

addArrayVariable : function(client, variableNode, newValue, immediate) {
	if (variableNode == null) return;
	variableNode.value = this.listAddInt(variableNode.value,newValue);
	client.changeVariable(variableNode.variableId, variableNode.value, immediate);
},

toggleArrayVariable : function(client, variableNode, value, immediate) {
	if (variableNode == null) return;
	if (this.listContainsInt(variableNode.value,value)) {
		variableNode.value = this.listRemoveInt(variableNode.value,value);
	} else {
		variableNode.value = this.listAddInt(variableNode.value,value);
	}
	client.changeVariable(variableNode.variableId, variableNode.value, immediate);
},

removeArrayVariable : function(client, variableNode, value, immediate) {
	if (variableNode == null) return;
	variableNode.value = this.listRemoveInt(variableNode.value,value);
	client.changeVariable(variableNode.variableId, variableNode.value, immediate);
},

arrayToList : function(arrayVariableElement) {

  var list = "";
  if (arrayVariableElement == null || arrayVariableElement.childNodes == null) return list;
  
  var items = arrayVariableElement.getElementsByTagName("ai");
  if (items == null) return list;
  
  for (var i=0; i <items.length;i++) {
  	var v = this.getFirstTextNode(items[i]); 
  	if (v != null && v.data != null) {
  		if (list.length >0) list += ",";
  		list += v.data;
  	}
  }	
  
  return list;
},


/* 
#### Generic component functions #############################################
*/
renderChildNodes : function(renderer, uidl, to) {
	for (var i=0; i<uidl.childNodes.length; i++) {
		var child = uidl.childNodes.item(i);
		if (child.nodeType == Node.ELEMENT_NODE) {
			renderer.client.renderUIDL(child,to);
		} else if (child.nodeType == Node.TEXT_NODE) {
			to.appendChild(to.ownerDocument.createTextNode(child.data));
		}
	}
},

applyWidthAndHeight : function(uidl,target) {
	if (target == null || uidl == null) return;

	// Width
	var widthEl = this.getVariableElement(uidl,"integer","width");
	if (widthEl) {
		var w = widthEl.getAttribute("value");
		if (w > 0) {
			target.style.width = ""+w+"px";
		}
	}
	
	// Height
	var heightEl = this.getVariableElement(uidl,"integer","height");
	if (heightEl) {
		var h = heightEl.getAttribute("value");
		if (h > 0) {
			target.style.height = ""+h+"px";
		}
	}	
},

createPaintableElement : function (renderer, uidl, target,layoutInfo) {

	// And create DIV as container
	var div = null;
	var pid = uidl.getAttribute("id");
	if(pid && pid == renderer.client._focusedPID)
		renderer.client._focusedElementRendered = true;
	var li = layoutInfo||target.layoutInfo;
	if (pid != null && target.getAttribute("id") == pid){
		div = target;
        // this a repaint, remove old listeners to avoid memory leaks
        renderer.client.warn("Removed " + renderer.client.removeAllEventListeners(div) + " event listeners.");
        renderer.client.warn("Removed " + renderer.client.unregisterAllLayoutFunctions(div) + " layout functions.");
	} else {
		//TODO: Remove this if the statement below works.
		// div = renderer.theme.createElementTo(target,"div");
		div = renderer.client.createPaintableElement(uidl,target);
	}
	div.layoutInfo = li;
	div.innerHTML = "";
	if (li&&li.captionNode) {
		// caption placed elsewhere (form); see renderDefaultComponentHeader()
		li.captionNode.innerHTML = "";
	}
		
	// Assign CSS class
	this.setCSSDefaultClass(renderer,div,uidl);
	if ("true"==uidl.getAttribute("disabled")) {
		this.addCSSClass(div,"disabled");
	}
	if (this.getFirstElement(uidl,"error")) {
		this.addCSSClass(div,"error");
	}
	return div;	
},

renderDefaultComponentHeader : function(renderer, uidl, target, layoutInfo) {
	var theme = renderer.theme;
	var doc = renderer.doc;
	var client = renderer.client;

	var captionText = uidl.getAttribute("caption");
	var error = theme.getFirstElement(uidl,"error");
	var description = theme.getFirstElement(uidl,"description");
	var icon = uidl.getAttribute("icon");
	
	if (!captionText && !error && !description && !icon) {
		return null;
	}
	
	if (!layoutInfo) {
		layoutInfo = target.layoutInfo;
	}
	
	// If layout info contains caption node, use it as caption position
	if (layoutInfo != null && layoutInfo.captionNode) {
		target = layoutInfo.captionNode;
		target.innerHTML = "";
	}
	
	// Caption container	
	var caption = this.createElementTo(target,"div");
	
	// Create debug-mode UIDL printing button
	if (renderer.client.debugEnabled) {
		var uidlDebug = this.createElementTo(caption,"div","uidl");
        uidlDebug.uidl = uidl;
		client.addEventListener(uidlDebug,"click", function (e) {
            if(window.confirm("Print components UIDL to console?")) {
                var event = client.getEvent(e);
                console.info("Printing components UIDL");
                console.dirxml(event.target.uidl);
            }
		});
	}
	
	if (captionText||error||description||icon) {
		//this.addCSSClass(caption,"caption clickable");
	} else {
		return caption;
	}
	
	
	var iconUrl = uidl.getAttribute("icon");
		
	if (iconUrl) {
    	if (iconUrl.indexOf("theme://") == 0) {
    		iconUrl = (theme.iconRoot != null ? theme.iconRoot : theme.root) 
    					+ iconUrl.substring(8);
    	}
		var icon = this.createElementTo(caption,"img","icon");
		icon.src = iconUrl;
	}
	
	
	// Caption text
	this.createTextNodeTo(caption,captionText);
	this.setCSSClass(caption,"caption");
	
	var errorIcon;
	if (error) {
		this.addCSSClass(caption,"clickable");
		var icon = this.createElementTo(caption,"img","icon");
		icon.src = theme.root+"img/icon/error-mini.gif";
		if (iconUrl) {
			/* overlay icon */
			this.setCSSClass(icon,"overlay error");
		} else {
			this.setCSSClass(icon,"error");
		}
		errorIcon = icon;
	} else if (description) {
	
		if(!captionText) this.addCSSClass(caption,"hide");
		
		var icon = this.createElementTo(caption,"img","icon description");
		icon.src = theme.root+"img/icon/info-mini.gif";
		if (iconUrl) {
			/* overlay icon */
			this.setCSSClass(icon,"overlay description");
			this.removeCSSClass(caption,"hide");
		} 
	}
	var popupTarget = (captionText || iconUrl || error)?caption:target;
	if (error||description) {
		if(error) {
			popupTarget._descriptionHTML = '<span class="error">' + client.getXMLtext(error) + "</span>";
		} else {
			popupTarget._descriptionHTML = client.getXMLtext(description);
		}
		this.client.addEventListener(popupTarget, "mouseover",this._onDescriptionMouseOver);
		this.client.addEventListener(popupTarget, "mouseout",this._onDescriptionMouseOut);
	}
	return caption;
},

_onDescriptionMouseOver : function(e) {
	var evt = itmill.lib.getEvent(e);
	
	var trg = evt.target;
	while(!trg._descriptionHTML && trg.parentNode)
		trg = trg.parentNode;
	
	if(trg._descriptionHTML) {
		var client = itmill.lib.getClient(trg);
		if(client) {
			trg._popuptimeout = window.setTimeout(function() {
				var tt = client.getTooltip();
				tt.showTooltip(trg._descriptionHTML, evt);
			}, 800);
		}
	}
},

_onDescriptionMouseOut : function(e) {
	var evt = itmill.lib.getEvent(e);
	var trg = evt.target;
	while(!trg._descriptionHTML && trg.parentNode)
		trg = trg.parentNode;
	if(trg._descriptionHTML) {
		window.clearTimeout(trg._popuptimeout);
		var client = itmill.lib.getClient(trg);
		var tt = client.getTooltip();
		tt._hide();
	}
},


/*
#### Generic event handlers ######################################################
*/
addAddClassListener : function(theme,client,element,event,className,target,current) {
	client.addEventListener(element,event, function(e) {
			if (current) {
				if (current.length) {
					var length = current.length;
					while (length--) {
						theme.removeCSSClass(current[length],className);
						delete current[length];
					}
				} else {
					for (e in current) {
						theme.removeCSSClass(current[e],className);
						delete current[e];						
					}
				}
			}
			theme.addCSSClass((target?target:element),className);
			if (current) {
				current[current.length] = (target?target:element);
			}
		}
	);
},

addRemoveClassListener : function(theme,client,element,event,className,target) {
	this.client.addEventListener(element,event, function(e) {
			theme.removeCSSClass((target?target:element),className);
		}
	);
},

addToggleClassListener : function(theme,client,element,event,className,target) {
	this.client.addEventListener(element,event, function(e) {
			theme.toggleCSSClass((target?target:element),className);
		}
	);
},

addStopListener : function(theme,client,element,event) {
	this.client.addEventListener(element, event, function(e) { 
			var evt = client.getEvent(e);
			evt.stop();
			return false;
		}
	);
},

addSetVarListener : function(theme,client,element,event,variable,key,immediate) {
	this.client.addEventListener(element,event, function(e) {
			var value = "";
			if (typeof(key)=="string") {
				value = key;
			} else if (key.type=="checkbox"||key.type=="radio") {
				value = key.checked;
			} else if (key.type=="select-multiple") {
				var s = new Array();
				for (var i = 0; i < key.options.length; i++) {
					if (key.options[i].selected) {
						s[s.length] = key.options[i].value;
					}
				}		
				value = s.join(',');		
			} else {
				value = key.value;
			}
			if (typeof(variable) == "string") {
				client.changeVariable(variable,value,immediate);
			} else {
				theme.setVariable(client,variable,value,immediate);
			}
		}
	);
},

addRemoveVarListener : function(theme,client,element,event,variable,key,immediate) {
	client.addEventListener(element,event, function(e) {
			theme.removeArrayVariable(client,variable,key,immediate);
		}
	);
},

addAddVarListener : function(theme,client,element,event,variable,key,immediate) {
	client.addEventListener(element,event, function(e) {
			theme.addArrayVariable(client,variable,key,immediate);
		}
	);
},

addToggleVarListener : function(theme,client,element,event,variable,key,immediate) {
	client.addEventListener(element,event, function(e) {
			theme.toggleArrayVariable(client,variable,key,immediate);
		}
	);
},

addExpandNodeListener : function(theme,client,img,event,subnodes,expandVariable,collapseVariable,key,immediate,target) {
		client.addEventListener((target?target:img), event, function(e) { 
				if (img.expanded == "true") {
					theme.removeArrayVariable(client,expandVariable,key,false);
					theme.addArrayVariable(client,collapseVariable,key,immediate);
					img.src = theme.root + "img/tree/off.gif";
					img.expanded = "false";
				} else {
					theme.removeArrayVariable(client,collapseVariable,key,false);
					theme.addArrayVariable(client,expandVariable,key,immediate || 
						!img.expanded || !subnodes.childNodes || subnodes.childNodes.length <= 0);
					img.src = theme.root + "img/tree/on.gif";
					img.expanded = "true";
				}
			}
		);
},

addTogglePopupListener : function(theme,client,element,event,popup,delay,defWidth,popupAt,blocker) {
	client.addEventListener(element,(event=="rightclick"?"mouseup":event), function(e) {
			var evt = client.getEvent(e);
			if (event=="rightclick"&&!evt.rightclick) return;
			if (evt.target.nodeName == "INPUT" || evt.target.nodeName == "SELECT") return;
            if (evt.alt) return;
            if (popupAt) {
            	var p = client.getElementPosition(popupAt);
 				theme.togglePopup(popup,p.x,(p.y+p.h),(delay?delay:0),(defWidth?defWidth:100),blocker);
            } else {
				theme.togglePopup(popup,evt.mouseX,evt.mouseY,(delay?delay:0),(defWidth?defWidth:100),blocker);
			}
			evt.stop();
		}
	);
},

addShowPopupListener : function(theme,client,element,event,popup,delay,defWidth) {
	client.addEventListener(element,(event=="rightclick"?"click":event), function(e) {
			var evt = client.getEvent(e);
			if (event=="rightclick"&&!evt.rightclick) return;

			theme.showPopup(client,popup,evt.mouseX,evt.mouseY,(delay?delay:0),(defWidth?defWidth:100));
			evt.stop();
		}
	);
},


/**
* Adds a hidden button with a tabindex; adds .over to hoverTarget when focused
*/
addTabtoHandlers : function(client,theme,target,hoverTarget,tabindex,defaultButton) {
	
	var div = document.createElement("div");
	div.style.padding = "0px";
	div.style.margin = "0px";
	div.style.width = "0px";
	div.style.height = "0px";
	div.style.overflow = "hidden";

	var b = this.createInputElementTo(div,(defaultButton?"submit":"button"));

	if (tabindex) b.tabIndex = tabindex;

	client.addEventListener(b,"focus", function() {
		theme.addCSSClass(hoverTarget,"over");
	});
	client.addEventListener(b,"blur", function() {
		theme.removeCSSClass(hoverTarget,"over");
	});
	b.onfocus = theme._updateFocusedToClient;
	target.appendChild(div);
    return b;
},

_updateFocusedToClient : function() {
	var client = itmill.clients[0];
	var pntbl = client.getPaintable(this);
	client.setFocusedElement(pntbl);
},

/*
#### Component renderers ######################################################
*/
renderComponent : function(renderer,uidl,target,layoutInfo) {

	// Create containing element
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
	
	// Render default header
	renderer.theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
	
	// Render children to div
	renderer.theme.renderChildNodes(renderer, uidl, div);
},

renderWindow : function(renderer,uidl,target,layoutInfo) {

	if(!uidl.getAttribute("main") && ! (uidl.getAttribute("style") && uidl.getAttribute("style") == "native")) {
		if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
		if(target.TkWindow) {
            target.TkWindow.cleanUp();
        }
		var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
		var w = parseInt(renderer.theme.getVariableElementValue(renderer.theme.getVariableElement(uidl,"integer","width")));
		var h = parseInt(renderer.theme.getVariableElementValue(renderer.theme.getVariableElement(uidl,"integer","height")));
		var x = parseInt(renderer.theme.getVariableElementValue(renderer.theme.getVariableElement(uidl,"integer","positionx")));
		var y = parseInt(renderer.theme.getVariableElementValue(renderer.theme.getVariableElement(uidl,"integer","positiony")));
		var cap = uidl.getAttribute("caption");
		var modality = uidl.getAttribute("style") == "modal";
		
		// stack new windows from upper left corner
        if (!x || x < 0) {
		    x = (renderer.client.windowOrder.length + 1) * 20;
        }
        if (!y || y < 0) {
		    var y = (renderer.client.windowOrder.length + 1) * 20;
        }
		
		var tkWin = new itmill.themes.Base.TkWindow({
			title: cap,
			width: w,
			height: h,
			posX: x,
			posY: y,
			constrainToBrowser: true,
			parentNode: div,
			modal: modality});

		div.TkWindow = tkWin;
		renderer.theme.createVarFromUidl(div,renderer.theme.getVariableElement(uidl,"boolean","close"));
		
		// this is needed to remove window modality
		if(!modality) {
            tkWin.setModal(false);
        }
		renderer.theme.renderChildNodes(renderer,uidl,tkWin.childTarget);
		return;
	}
	
	// rest is for "native"  windows or main window
	
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);

	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
	
	var theme = renderer.theme;
	
	// If theme is changed, reload window
	var currentTheme = div.itmtkTheme;
	div.itmtkTheme = uidl.getAttribute("theme");
	if (typeof currentTheme != 'undefined' && div.itmtkTheme != currentTheme)
		window.location.href = window.location.href;
	
    // Render children to div
	theme.renderChildNodes(renderer, uidl, div);
	
	// Apply width and height
	theme.applyWidthAndHeight(uidl,div);
	
	// Focusing
	var focused = theme.getVariableElement(uidl,"string","focused");
	var focusid = theme.getVariableElementValue(focused);
	if (focusid) { 
		var found = false;
		var els = div.getElementsByTagName("input");
		var len = (els?els.length:0);
		for (var i=0;i<len;i++) {
			var el = els[i];
			if (focusid == el["focusid"]) {
				el.focus();
				found = true;
				break;
			}
		}
		if (!found) {
			els = div.getElementsByTagName("select");
			var len = (els?els.length:0);
			for (var i=0;i<len;i++) {
				var el = els[i];
				if (focusid == el["focusid"]) {
					el.focus();
					found = true;
					break;
				}
			}		
		}
		if (!found) {
			els = div.getElementsByTagName("textarea");
			var len = (els?els.length:0);
			for (var i=0;i<len;i++) {
				var el = els[i];
				if (focusid == el["focusid"]) {
					el.focus();
					found = true;
					break;
				}
			}		
		}
	}
},

renderOpen : function(renderer,uidl,target,layoutInfo) {
	var theme = renderer.theme;
 	
 	var src = uidl.getAttribute("src");
 	var name = uidl.getAttribute("name");
 	
 	if (name) {
 		window.open(src,name);
 	} else {
 		// first overcome IE prob by hiding scrollbars from containing div
 		if(document.all && !window.opera)
 			target.style.overflow = "hidden";
 		var div = theme.createPaintableElement(renderer,uidl,target,layoutInfo);
 		div.innerHTML = "<IFRAME name=\""+name+"\" id=\""+name+"\" width=100% height=100% style=\"border:none;margin:0px;padding:0px;background:none;\" src=\""+src+"\"></IFRAME>";
	}
},

renderFramewindow : function(renderer,uidl,target,layoutInfo) {	
	var theme = renderer.theme;
	var client = renderer.client;
	
	// TODO: Should we unregister all previous child windows?
	
	// We just reinitialize the window
	var win = target.ownerDocument.ownerWindow;
	client.initializeNewWindow(win,uidl,theme);
},

renderCustomLayout : function(renderer,uidl,target,layoutInfo) {
	// Shortcuts
	var theme = renderer.theme;
	var client = renderer.client;
	// Get style
    var style = uidl.getAttribute("style");    
    if (style == null) return null;
    // Load layout
    var text = renderer.client.loadCustomLayout(style,false);
    if (text == null) {
    	client.debug("CustomLayout " + style + " NOT FOUND");
    	return null; 
    }
 
	// Create containing element
	var main = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);		
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
    
    // Save locations from uidl
    var locations = new Object();
    var unused = new Object();
    var cN = uidl.childNodes;
    var len = cN.length;
 	for (var j=0; j < len; j++) {
          var c = cN.item(j);
          if (c.nodeType == Node.ELEMENT_NODE 
          		&& c.nodeName == "location" 
          		&& c.getAttribute("name")) {
          		locations[c.getAttribute("name")] = c;
          		unused[c.getAttribute("name")] = c;
          }
    }   
 
 	// Containing div, render layout
    var n = theme.createElementTo(main, "div");
    n.setAttribute("id",uidl.getAttribute("id"));
    n.innerHTML=text;

    // Make non-live copy of location divs
    var liveDivs = n.getElementsByTagName("div");
    var divs = [];
    for (var i=0; i<liveDivs.length; i++) {
    	var div = liveDivs[i]
      	if (div.getAttribute("location") == null) continue;
    	divs[divs.length] = div;
    }
    // Render component for each location
	for (var i=0; i<divs.length; i++) {
		var div = divs[i];
		var name = div.getAttribute("location");      
		// clear content
		client.removeAllEventListeners(div);
		div.innerHTML = "";
		var c = locations[name];
		if (c) {   
			delete unused[name];
			var lcn = c.childNodes;
			for (var k=0; k<lcn.length; k++) {
				// find component in uidl and render
				var cc = lcn[k]; 
				if (cc.nodeType == Node.ELEMENT_NODE) {					
					var newNode = renderer.client.renderUIDL(cc,div);				
	                break;
				}
			}
        } else {
			// no such location in uidl
        	client.warn("Location " + name + " NOT USED in CustomLayout " + style);
		}
	}

	// Report missing locations
    for (var k in unused) {
    	client.error("Location " + k + " NOT FOUND in CustomLayout " + style);
    }

},

renderOrderedLayout : function(renderer,uidl,target,layoutInfo) {
	// Shortcuts
	var theme = renderer.theme;
	
	// Create containing element
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);		
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

	// Render all children to table
	var vertical = uidl.getAttribute("orientation") != "horizontal";
	var table = null;
	var tr = null;
	var td = null;
	var style = uidl.getAttribute("style");
	var form = style == "form";
	
	for (var i=0; i<uidl.childNodes.length; i++) {
		var childUIDL = uidl.childNodes.item(i);
		td = null;
		if (childUIDL.nodeType == Node.ELEMENT_NODE) {
		
			// Ensure TABLE and TR
			if (tr == null || vertical) {
				if (table == null) {
					table = renderer.theme.createElementTo(div,"table","orderedlayout");
                    //table.width="100%";                    
					renderer.theme.addCSSClass(table,"layout");
					table = renderer.theme.createElementTo(table,"tbody","layout");
				}
				tr = renderer.theme.createElementTo(table,"tr","layout");
			}
			
			// Create extra TD for form style captions
			var layoutInfo = null;
			if (form) {
			 	layoutInfo = new Object()
				td = renderer.theme.createElementTo(tr,"td","layout");
				layoutInfo.captionNode = td;
			}
			
			// Force new TD for each child rendered
			td = renderer.theme.createElementTo(tr,"td","layout");			
			
			// Render the component to TD
			renderer.client.renderUIDL(childUIDL,td, null, layoutInfo);
			
		}
	}			
},

renderGridLayout : function(renderer,uidl,target,layoutInfo) {
	// NOTE TODO indenting might be off
	// Shortcuts
	var theme = renderer.theme;
		
	var h = uidl.getAttribute("h");	
	var w = uidl.getAttribute("w");
	// Create component element
	var div = theme.createPaintableElement(renderer,uidl,target,layoutInfo);
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
	//var width = (div.offsetWidth||div.clientWidth)-20;
	//var px = Math.floor(width/parseInt(w));
	
	var table = theme.createElementTo(div,"table", "layout");
	table = renderer.theme.createElementTo(table,"tbody","layout");
	var tr = null;
	var td = null;
	for (var y=0; y<uidl.childNodes.length; y++) {
		var rowUidl = uidl.childNodes[y];
				
		if (rowUidl.nodeType == Node.ELEMENT_NODE || rowUidl.nodeName == "gr") {
		
			tr = theme.createElementTo(table,"tr","layout");
			tr.style.verticalAlign = "top";
			td = null;
			
			for (var x=0; x<rowUidl.childNodes.length; x++) {
				var cellUidl = rowUidl.childNodes[x];				
				
				// Add colspan and rowspan
				if (cellUidl.nodeType == Node.ELEMENT_NODE && cellUidl.nodeName == "gc") {							
					// Create new TD for each child rendered
					td = renderer.theme.createElementTo(tr,"td","layout");
										
					var w = cellUidl.getAttribute('w');
					var h = cellUidl.getAttribute('h');							
					//var cont = renderer.theme.createElementTo(td,"div");
					//cont.style.width = ((w?w:1)*px)+"px";
					if (w != null) {
						td.setAttribute('colSpan',w);
					}
					if (h != null) {
						td.setAttribute('rowSpan',h);
					}					
					// Render the component(s) to TD
					if (cellUidl.childNodes != null && cellUidl.childNodes.length >0) {
						var len = cellUidl.childNodes.length;
						for (var c=0;c<len;c++) {
							var el = cellUidl.childNodes[c];
							if (el.nodeType == Node.ELEMENT_NODE) {
								renderer.client.renderUIDL(el,td);
							}
						}
						//cont.style.width = "";
					}
				}
			}
		}
	}
},

renderPanel : function(renderer,uidl,target,layoutInfo) {
			// Supports styles "light" and "none"

			// Shortcuts
			var theme = renderer.theme;
			
			var style = uidl.getAttribute("style");
			
			var borderStyle = "panelborder";
			
			// Create component element
			var outer = theme.createPaintableElement(renderer,uidl,target,layoutInfo);
			if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
			
            if ("none"!=style) {
			    theme.addCSSClass(div,"outset");
            }
			if ("light"==style) {
				theme.addCSSClass(div,"light");
				//borderStyle += "light";
			}
			
			// Create extra DIV for visual layout
			var div = theme.createElementTo(outer,"div");
            if ("none"!=style) {
			    theme.setCSSClass(div,borderStyle);
            }

			// Create default header
			var caption = theme.renderDefaultComponentHeader(renderer,uidl,div);
			theme.addCSSClass(caption,"panelcaption");
            if ("light"==style) {
				theme.addCSSClass(caption,"panelcaptionlight");
			}
			
			// If no actual caption, remove description popup listener
			if(caption && caption.className.indexOf("hide") > -1) {
				client.removeEventListener(div,undefined,null,"descriptionPopup");
			}

			// Create content DIV
			var content = theme.createElementTo(div,"div");
			theme.setCSSClass(content,"content");

            // Apply width and height
            theme.applyWidthAndHeight(uidl,outer);
            
            // TODO looks ugly, refactor
            var w = parseInt(theme.getVariableElement(uidl,"integer","width").getAttribute("value"));
            var h = parseInt(theme.getVariableElement(uidl,"integer","height").getAttribute("value"));
            if (h && h > 0) {
            	var ch = (caption?caption.offsetHeight:0);
                content.style.height = (outer.offsetHeight - ch - 14) + "px";
            }
            if (w && w > 0 && !window.XMLHttpRequest) {
                // fix width for IE 6
                // offsetwidht - scrollbar + border
                content.style.width = (content.offsetWidth - 14) + "px";
            }
 			
			// Render children to div
			theme.renderChildNodes(renderer, uidl, content);

},

/** under development
 * this should be easyly modified not to use polling in case "comet" is implemented
 */ 
renderProgressIndicator : function(renderer,uidl,target,layoutInfo) {
    // TODO try to mess intervals
    // Create container element
    var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
    var id = div.id;
    var interval = uidl.getAttribute("pollinginterval");
    
    var appId = this.clientId;

    var f = function() {
        // poll server for changes
        itmill.clients[appId].processVariableChanges(false,false);
        if(null == document.getElementById(id)) {
            //if progressindicator is removed, clear this interval
            clearInterval(renderer.theme.intervals[id]);
        }
    };
    if(!renderer.theme.intervals) {
        renderer.theme.intervals = new Object();
    }
    if(renderer.theme.intervals[id]) {
        // remove old interval
        clearInterval(renderer.theme.intervals[id]);
    }
    renderer.theme.intervals[id] = setInterval(f,interval);
    
    if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
    // Create default header
    var caption = renderer.theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
    var indeterminate = ("true" == uidl.getAttribute("indeterminate"));
    var state = uidl.getAttribute("state");
    if(indeterminate) {
        div.state = 0;
        var chars = ['|', '/', '-','\\', '|','/','-', '\\'];
        div.ipiStateChange = function() {
            // this will change divs char constantly | / - | \ -
            // we this particular div exists (and not redrawn due it has new timer)
            if(document.getElementById(id) && document.getElementById(id) == div) {
                // change state
                div.state++;
                // set new state
                div.innerHTML = chars[div.state%8];
                setTimeout(div.ipiStateChange,700);
            }
        }
        // start indeterminate indicator
        div.ipiStateChange();
    } else {
        var widthPros = Math.round(state*100);
        div.innerHTML = "<div style=\"border:1px solid red;\"><div style=\"width:"+widthPros+"%;background-color:red;\"><br/></div></div>";
    }
},

renderTabSheet : function(renderer,uidl,target,layoutInfo) {

			var theme = renderer.theme;
			
			// Create container element
			var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
			if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

			// Create default header
			var caption = renderer.theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
			
			// If no actual caption, remove description popup listener
			if(caption && caption.className.indexOf("hide") > -1) {
				client.removeEventListener(div,undefined,null,"descriptionPopup");
			}
			
			//  Render tabs
			var tabs = theme.createElementTo(div,"div","tabs");
			var varId = theme.getVariableElement(uidl,"string","selected").getAttribute("id");
			
			var tabNodes = theme.getChildElements(uidl,"tabs");
			if (tabNodes != null && tabNodes.length >0)  tabNodes = theme.getChildElements(tabNodes[0],"tab");
			var selectedTabNode = null;
			if (tabNodes != null && tabNodes.length >0) {
				for (var i=0; i< tabNodes.length;i++) {
					var tabNode = tabNodes[i];
					var tab = theme.createElementTo(tabs,"div");
					var key = tabNode.getAttribute("key");
					var iconUrl =  tabNode.getAttribute("icon");
					if (iconUrl && iconUrl.indexOf("theme://") == 0) {
		   				iconUrl = (theme.iconRoot != null ? theme.iconRoot : theme.root) 
		    				+ iconUrl.substring(8);
					}		
					if (tabNode.getAttribute("selected") == "true") {
						theme.addCSSClass(tab,"tab-on inline");
						selectedTabNode = tabNode;
					} else if (tabNode.getAttribute("disabled") == "true" 
								|| uidl.getAttribute("disabled") == "true"
								|| uidl.getAttribute("readonly") == "true") {
						theme.setCSSClass(tab,"tab disabled inline");
					} else {
						theme.setCSSClass(tab,"tab clickable inline");
						theme.addAddClassListener(theme,this,tab,"mouseover","over",tab);
						theme.addRemoveClassListener(theme,this,tab,"mouseout","over",tab);
						theme.addSetVarListener(theme,this,tab,"click",varId,key,true);
					}
					// Extra div in tab
					tab = theme.createElementTo(tab,"div","caption border pad inline");
					
					// Icon
					if (iconUrl) {
						tab.innerHTML = "<IMG src=\""+iconUrl+"\" class=\"icon\" />" + tabNode.getAttribute("caption");
					} else {
						tab.innerHTML = tabNode.getAttribute("caption");
					}
				
				}
			}
			
			// Render content (IE renderbug need three)
			var content = theme.createElementTo(div,"div","outset");
			content = theme.createElementTo(content,"div","border");
			content = theme.createElementTo(content,"div","content");
			if (selectedTabNode != null) {
				theme.renderChildNodes(renderer,selectedTabNode, content);
			}
},


renderTree : function(renderer,uidl,target,layoutInfo) {
			
	var theme = renderer.theme;
	
	// Create container element
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

	// Get tree attributes
	var style = uidl.getAttribute("style");
	var immediate = ("true" == uidl.getAttribute("immediate"));
	var disabled = ("true" == uidl.getAttribute("disabled"));
	var readonly = ("true" == uidl.getAttribute("readonly"));
	var selectMode = uidl.getAttribute("selectmode");
	var selectable = selectMode == "multi" || selectMode == "single";
	var selected;
	if (selectable) {
		selected = new Object();
	}
	var selectionVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","selected"));
	var expandVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","expand"));
	var collapseVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","collapse"));

	var actions = null;
	var actionVar = null;
	var alNode = theme.getFirstElement(uidl,"actions")
	if (alNode) {
		actionVar = theme.createVarFromUidl(div,theme.getVariableElement(alNode,"string","action"));
		actions = new Object();
		var ak = alNode.getElementsByTagName("action");
		for (var i=0;i<ak.length;i++) {
			actions[ak[i].getAttribute("key")] = {
				caption: ak[i].getAttribute("caption"),
				icon: theme.parseResourceUri(ak[i].getAttribute("icon"))
			};
		}
	}
	div.actions = actions;
	delete alNode;

	// Create default header
	var caption = renderer.theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
	theme.addCSSClass(caption,"treecaption");
	
	// Content DIV
	var content = theme.createElementTo(div,"div","content");
	
	// Tree container (ul)
	var ul = theme.createElementTo(content,"ul","tree");
	
	// Iterate all nodes
	var totalSubNodes = 0;
	for (var i=0; i<uidl.childNodes.length; i++) {
		var childNode = uidl.childNodes[i];
		if (childNode.nodeName == "node" || childNode.nodeName == "leaf")
			totalSubNodes++;
	}
	
	for (var i=0, j=0; i<uidl.childNodes.length; i++) {
		var node = uidl.childNodes[i];
		if (node.nodeName == "node" || node.nodeName == "leaf") {
			var lastItem = (j==totalSubNodes-1);
			theme.renderTreeNode(renderer,node,ul,selectable,selectMode,selected,selectionVariable,expandVariable,collapseVariable,actions,actionVar,immediate,disabled,readonly,lastItem);
			j++;
		} 	
	}
},

renderTreeNode : function(renderer,node,target,selectable,selectMode,selected,selectionVariable,expandVariable,collapseVariable,actions,actionVar,immediate,disabled,readonly,lastItem) {

	// Shortcuts
	var theme = renderer.theme;
	var client = renderer.client;
	
	// Tree node (li)
	var li = theme.createElementTo(target,"li","clickable");
	if(lastItem) theme.addCSSClass(li,"last");
		
	// Expand/Collapse button
	var ECButton = theme.createElementTo(li,"span");
	
	// Possible icon
	var icon = node.getAttribute("icon");
	if (icon) {
        var iconurl = theme.root+icon.split("theme:")[1];
        var img = theme.createElementTo(li,"img","icon");
	    img.src = iconurl;
    }
	
	// Caption
	var cap = theme.createElementTo(li,"span","caption");
	theme.createTextNodeTo(cap,node.getAttribute("caption"));
	
	// Hover effects
	if (!disabled&&!readonly&&selectable) {
		theme.addAddClassListener(theme,client,cap,"mouseover","over",cap);
		theme.addRemoveClassListener(theme,client,cap,"mouseout","over",cap);
	}
	
	// Server-side selection
	var key = node.getAttribute("key");
	if (selectable && node.getAttribute("selected") == "true") {
		theme.addCSSClass(cap,"selected");
		selected[key] = cap;
	}

	// Indicate selection	
	if (theme.listContainsInt(selectionVariable.value,key)) {
		theme.addCSSClass(cap, "selected");
	}

	// Selection listeners
	if (selectable && !disabled) {
		if (!readonly) {		
			if (selectMode == "single") {
				theme.addAddClassListener(theme,client,cap,"click","selected",cap,selected);
				theme.addSetVarListener(theme,client,cap,"click",selectionVariable,key,immediate);
			
			} else if (selectMode == "multi") {	
				theme.addToggleClassListener(theme,client,cap,"click","selected");
				theme.addToggleVarListener(theme,client,cap,"click",selectionVariable,key,immediate);
			
			}
		}
	} 
	
	// Actions
	// and does node have subnodes/leafs?
	
	var hasChildren = false;
	for (var i=0; i<node.childNodes.length; i++) {
		var childNode = node.childNodes[i];
		if(!hasChildren && (childNode.nodeName == "node" || childNode.nodeName == "leaf")) {
			hasChildren = true;
		}
		if (!disabled && !readonly && childNode.nodeName == "al" ) {
			// extract actions that this particular node has
			var actionList = new Array();
			for(var j = 0; j < childNode.childNodes.length; j++) {
				actionList.push(childNode.childNodes[j].firstChild.data);
			}
			li.actionList = actionList;
			li.key = node.getAttribute("key");
			client.addEventListener(li,"contextmenu",theme.treeNodeShowContextMenu);
			if(window.opera)
				client.addEventListener(li,"click",theme.treeNodeShowContextMenu);
			
		}
	}
		
	// Render all sub-nodes
	if (node.nodeName == "node") {
	
		if (hasChildren) {
			theme.addCSSClass(ECButton,"collapse");
			ECButton.expanded = "true";
		} else {
			theme.addCSSClass(ECButton,"expand");
			ECButton.expanded = "false";
		}
		

		if(hasChildren) {
		
			var subtree = theme.createElementTo(li,"ul");
			
			// Loop all subnodes and count actual children
			// (needed to find the last child)
			var totalSubNodes = 0;
			for (var i=0; i<node.childNodes.length; i++) {
				var childNode = node.childNodes[i];
				if (childNode.nodeName == "node" || childNode.nodeName == "leaf")
					totalSubNodes++;
			}
			
			for (var i=0, j=0; i<node.childNodes.length; i++) {
				var childNode = node.childNodes[i];
				if (childNode.nodeName == "node" || childNode.nodeName == "leaf") {
					var lastItem = (j==totalSubNodes-1);
					theme.renderTreeNode(renderer,childNode,subtree,selectable,selectMode,selected,selectionVariable,expandVariable,collapseVariable,actions,actionVar,immediate,disabled,readonly,lastItem);
					j++;
				} 
			}
		
		// Empty handle for event functions
		} else subtree = theme.createElementTo(li,"em");
		
		// Add event listener
		if (!disabled) {
			var target = (selectable && !readonly)? ECButton : li;
			theme.addToggleClassListener(theme,client,target,"mouseup","hidden",subtree);
			theme.addExpandNodeListener(theme,client,ECButton,"mouseup",subtree,expandVariable,collapseVariable,key,immediate,target);
			theme.addStopListener(theme,client,target,"mouseup");
			theme.addStopListener(theme,client,target,"click");
		}
		
	}

},

addExpandNodeListener : function(theme,client,button,event,subnodes,expandVariable,collapseVariable,key,immediate,target) {
		client.addEventListener((target?target:button), event, function(e) { 
				if (button.expanded == "true") {
					theme.removeArrayVariable(client,expandVariable,key,false);
					theme.addArrayVariable(client,collapseVariable,key,immediate);
					theme.removeCSSClass(button,"collapse");
					theme.addCSSClass(button,"expand");
					button.expanded = "false";
				} else {
					theme.removeArrayVariable(client,collapseVariable,key,false);
					theme.addArrayVariable(client,expandVariable,key,immediate || !button.expanded || !subnodes.childNodes || subnodes.childNodes.length<=0);
					theme.removeCSSClass(button,"expand");
					theme.addCSSClass(button,"collapse");
					button.expanded = "true";
				}
			}
		);
},



treeNodeShowContextMenu: function(e) {
	var evt = itmill.Client.prototype.getEvent(e);
	if(evt.rightclick || evt.type == "contextmenu") {
		evt.stop();
		// Build ContextMenu compatible structure form list
		// in li element that contains keys, and ul element that
		// contains variable and captions for action keys

		var client = window.itmill.clients[0];
		var cm = client.getContextMenu();
		var node;
		if(evt.target.actionList) {
			node = evt.target;
		} else {
			node = evt.target.parentNode;
		}
		// event triggers from span element in li element
		var tree = client.getPaintable(node);
		var actions = new Array();
		// actionValue string is sent to server on contextMenu click
		// They comma separated like this: "[listitem],[actionKey]"
		for(var i = 0; i < node.actionList.length; i++) {
			actions.push({
				caption: tree.actions[node.actionList[i]].caption,
				icon: tree.actions[node.actionList[i]].icon,
				actionValue: (node.key + "," + node.actionList[i])
			});
		}
		cm.showContextMenu(actions,evt,tree.varMap.action);
		// disable browsers native context menu
		return false;
	}
},


renderTextField : function(renderer,uidl,target, layoutInfo) {

	var client = renderer.client;
	var theme = renderer.theme;
	var immediate = uidl.getAttribute("immediate") == "true";
	var readonly = uidl.getAttribute("readonly") == "true";
	var multiline = uidl.getAttribute("multiline") == "true";
	var secret = uidl.getAttribute("secret") == "true";
	var cols = uidl.getAttribute("cols");
	var rows = uidl.getAttribute("rows");
	var disabled = uidl.getAttribute("disabled") == "true";
	var focusid = uidl.getAttribute("focusid");
	var tabindex = uidl.getAttribute("tabindex");
	
	// Create containing element
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

	// Render default header
	renderer.theme.renderDefaultComponentHeader(renderer,uidl,div, layoutInfo);
	
	// Create border
	var border = renderer.theme.createElementTo(div,"div","border");
	
	// Create input
	var input = null;
	if (multiline) {
		input = renderer.theme.createElementTo(border,"textarea");	
		input.wrap = "off";	
		if (focusid) {
			input.focusid = focusid;
		}
	} else {
		input = renderer.theme.createInputElementTo(border,(secret?"password":"text"),null,focusid);	
	}
	if (tabindex) input.tabIndex = tabindex;
	if (disabled||readonly) {
		input.disabled = "true";
	} else {
		div.focusableField = input;
		div._onfocus = theme._onFieldFocus;
		input.onfocus = theme._updateFocusedToClient;
	}
	
	// Assign cols and rows
	if (cols >0) {
		if (multiline) {
			input.cols = cols;
		} else {
			input.size = cols;
			input.maxlength = cols;
		}
	}
	if (rows >0) {
		input.rows = rows;
	}
	
	// Find variable node
	var strNode = theme.getVariableElement(uidl,"string","text");
	var inputId = strNode.getAttribute("id");
	input.id = inputId;
	
	// Assign value	
	strNode= theme.getFirstTextNode(strNode);
	if (strNode != null && strNode.data != null) {
			input.value = strNode.data;
	}
		
	// Listener 
	theme.addSetVarListener(theme,client,input,"change",inputId,input,immediate);
},
_onFieldFocus : function() {
	// IE 6 sometimes throws error when trying to move focus onwars
	try {
		this.focusableField.focus();
	} catch(e) {}
},

renderDateField : function(renderer,uidl,target,layoutInfo) {
	// TODO needs simplification
	// - jscalendar supports time! but not resolution?
	// - dynamic .js loading!

	var theme = renderer.theme;

	// Create containing element
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);

	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

	/* Styles:
	*	time	- only time selection (no date)
	*/
	var style = uidl.getAttribute("style");

	var immediate = uidl.getAttribute("immediate") == "true";
	var disabled = uidl.getAttribute("disabled") == "true";
	var readonly = uidl.getAttribute("readonly") == "true";
	
	/* locale, translate UI */
	var locale = uidl.getAttribute("locale")	
	if (locale && !disabled && !readonly) {
		locale = locale.toLowerCase().split("_")[0];
		var lang = renderer.client.loadDocument(theme.root+"ext/jscalendar/lang/calendar-"+locale+".js",false);
		if (lang) {			
			try {
				window.eval(lang);
			} catch (e) {
				this.error("Could not eval DateField lang ("+locale+"):"+e );
			}
		}
	}		
	
	// Render default header
	renderer.theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
		
	var yearVar = theme.getVariableElement(uidl,"integer","year");
    var monthVar = theme.getVariableElement(uidl,"integer","month"); 
    var dayVar = theme.getVariableElement(uidl,"integer","day");
    var hourVar = theme.getVariableElement(uidl,"integer","hour");
    var minVar = theme.getVariableElement(uidl,"integer","min");
    var secVar = theme.getVariableElement(uidl,"integer","sec");
    var msecVar = theme.getVariableElement(uidl,"integer","msec");

	var year = null;
    var month = null;
    var day = null;
    var hour = null;
    var min = null;
    var sec = null;
    var msec = null;
	var text = null;
        
    var inputId = yearVar.getAttribute("id") + "_input";
	var buttonId = yearVar.getAttribute("id") + "_button";
	
    // Assign the value to textfield
    var yearValue = yearVar != null? yearVar.getAttribute("value"): null;
    var monthValue = monthVar != null? monthVar.getAttribute("value"): null;
    var dayValue = dayVar != null? dayVar.getAttribute("value"): null;
    var hourValue = hourVar != null? hourVar.getAttribute("value"): null;
    var minValue = minVar != null? minVar.getAttribute("value"): null;
    var secValue = secVar != null? secVar.getAttribute("value"): null;
    var msecValue = msecVar != null? msecVar.getAttribute("value"): null;
    
    if (style != "time") {
		if (dayValue) {
			// Using calendar - create textfield
		    if (readonly) {
		    	text = theme.createTextNodeTo(div,dayValue+"."+monthValue+"."+yearValue);
		    } else {
		    	text = theme.createInputElementTo(div,"text");
				text.id = inputId;
		    	text.size = "10";
			    if (disabled) {
			    	text.disabled = true;
			    }	            
			    if (yearValue >0 && monthValue >0 && dayValue >0) {
				    text.value = dayValue+"."+monthValue+"."+yearValue;
				} else {
				    text.value ="";
				}
		    }
			
			// Create button
		    var button = theme.createInputElementTo(div,"button","btn clickable");
		    button.id =buttonId;
		    button.inputId = inputId;
		    
		    button.value = "...";
		    if (disabled||readonly) {
		    	button.disabled = true;
		    }
		} else {
			if (yearVar) {
				// Year select
				if (readonly) {
					theme.createTextNodeTo(div,yearValue);
				} else {
			    	var year = theme.createElementTo(div,"select");
			    	year.options[0] = new Option("",-1);
			    	for (var i=0;i<500;i++) {
			    		year.options[i+1] = new Option(i+1900,i+1900);
			    		if (yearValue == (i+1900)) {
			    			year.options[i+1].selected = true;
			    		}
			    	}
				    if (disabled) {
				    	year.disabled = true;
				    }
			    	if (!readonly) theme.addSetVarListener(theme,this,year,"change",yearVar.getAttribute("id"),year,immediate);
		    	}
			}
			if (monthVar) {
				// Month select
				if (readonly) {
					theme.createTextNodeTo(div,"."+monthValue);
				} else {
			    	month = theme.createElementTo(div,"select");
			    	month.options[0] = new Option("",-1);
			    	for (var i=0;i<12;i++) {
			    		month.options[i+1] = new Option(i+1,i+1);
			    		if (monthValue == i+1) {
			    			month.options[i+1].selected = true;
			    		}
			    	}
				    if (disabled) {
				    	month.disabled = true;
				    }
			    	if (!readonly) theme.addSetVarListener(theme,this,month,"change",monthVar.getAttribute("id"),month,immediate);
			    }
			}
		}
	}
    if (hourVar) {
    	if (readonly) {
    		theme.createTextNodeTo(div," "+(hourValue<10?"0"+hourValue:hourValue));
    	} else {
	    	hour = theme.createElementTo(div,"select");
	    	hour.options[0] = new Option("",-1);
	    	for (var i=0;i<24;i++) {
	    		var cap = (i+1<10?"0"+(i+1):(i+1));
	    		if (!minVar) {
	    			// Append anyway, makes it easier to recognize as time
	    			cap = cap + ":00";
	    		}
	    		hour.options[i+1] = new Option(cap,i+1);
	    		if (hourValue == i+1) {
	    			hour.options[i+1].selected = true;
	    		}
	    	}
		    if (disabled) {
		    	hour.disabled = true;
		    }
	    	if (!readonly) theme.addSetVarListener(theme,this,hour,"change",hourVar.getAttribute("id"),hour,immediate);
	    }
    }
    if (minVar) {
    	// Minute select
    	if (readonly) {
    		theme.createTextNodeTo(div,":"+(minValue<10?"0"+minValue:minValue));
    	} else {
	    	theme.createTextNodeTo(div,":");
	    	min = theme.createElementTo(div,"select");
	    	min.options[0] = new Option("",-1);
	    	for (var i=0;i<60;i++) {
	    		min.options[i+1] = new Option((i<10?"0"+(i):(i)),i);
	    		if (minValue == i) {
	    			min.options[i+1].selected = true;
	    		}
	    	}
		    if (disabled) {
		    	min.disabled = true;
		    }
	    	if (!readonly) theme.addSetVarListener(theme,this,min,"change",minVar.getAttribute("id"),min,immediate);
	    }
    }
    if (secVar) {
    	// Second select
    	if (readonly) {
    		theme.createTextNodeTo(div,":"+(secValue<10?"0"+secValue:secValue));
    	} else {
	    	theme.createTextNodeTo(div,":");
	    	sec = theme.createElementTo(div,"select");
	    	sec.options[0] = new Option("",-1);
	    	for (var i=0;i<60;i++) {
	    		sec.options[i+1] = new Option((i<10?"0"+(i):(i)),i);
	    		if (secValue == i) {
	    			sec.options[i+1].selected = true;
	    		}
	    	}
		    if (disabled) {
		    	sec.disabled = true;
		    }
	    	if (!readonly) theme.addSetVarListener(theme,this,sec,"change",secVar.getAttribute("id"),sec,immediate);
	    }
    }
    if (msecVar) {
    	// Millisecond select
    	if (readonly) {
	    		var cap = msecValue;
	    		if (i+1 < 100) {
	    			cap = "0"+cap;
	    		}
	    		if (i+1 < 10) {
	    			cap = "0"+cap;
	    		}
    		theme.createTextNodeTo(div,"."+cap);
    	} else {
	    	theme.createTextNodeTo(div,".");
	    	msec = theme.createElementTo(div,"select");
	    	msec.options[0] = new Option("",-1);
	    	for (var i=0;i<1000;i++) {
	    		var cap = i;
	    		if (i < 100) {
	    			cap = "0"+cap;
	    		}
	    		if (i < 10) {
	    			cap = "0"+cap;
	    		}
	    		msec.options[i+1] = new Option(cap,i);
	    		if (msecValue == i) {
	    			msec.options[i+1].selected = true;
	    		}
	    	}
		    if (disabled) {
		    	msec.disabled = true;
		    }
	    	if (!readonly) theme.addSetVarListener(theme,this,msec,"change",msecVar.getAttribute("id"),msec,immediate);
	    }	    
   }
   
   if (!readonly) {
   		if (msec) theme.addDateFieldNullListener(this,msec,text,msec,sec,min,hour,day,month,year,yearVar,immediate);
   		if (sec) theme.addDateFieldNullListener(this,sec,text,msec,sec,min,hour,day,month,year,yearVar,immediate);
   		if (min) theme.addDateFieldNullListener(this,min,text,msec,sec,min,hour,day,month,year,yearVar,immediate);
   		if (hour) theme.addDateFieldNullListener(this,hour,text,msec,sec,min,hour,day,month,year,yearVar,immediate);
   		if (day) theme.addDateFieldNullListener(this,day,text,msec,sec,min,hour,day,month,year,yearVar,immediate);
   		if (month) theme.addDateFieldNullListener(this,month,text,msec,sec,min,hour,day,month,year,yearVar,immediate);
   		if (year) theme.addDateFieldNullListener(this,year,text,msec,sec,min,hour,day,month,year,yearVar,immediate);
   		 
   }
   
   var nullFunc = function () {
   			// TODO wierd when un-nulling
   			// -> serverside, examine
   			// + nulls in dropdowns!
   			text.value = "";
   			if (msec) {
   				msec.options[0].selected = true;
   				//client.changeVariable(msecVar.getAttribute("id"), -1, false);
   			}
   			if (sec) {
   				sec.options[0].selected = true;
    			//client.changeVariable(secVar.getAttribute("id"), -1, false);
   			}
   			if (min) {
   				min.options[0].selected = true;
   				//client.changeVariable(minVar.getAttribute("id"), -1, false);
   			}
   			if (hour) {
   				hour.options[0].selected = true;
   				//client.changeVariable(hourVar.getAttribute("id"), -1, false);
   			}
   			//client.changeVariable(dayVar.getAttribute("id"), -1, false);
   			//client.changeVariable(monthVar.getAttribute("id"), -1, false);
   			this.changeVariable(yearVar.getAttribute("id"), -1, immediate);
   }

	if (!readonly && !disabled && style != "time" && dayVar) {
		button.onclick = theme.dateFieldShowCalendar;
		button.updateField = theme.dateFieldUpdateFromCalendar;
		text.dayVarId = dayVar.getAttribute("id");
		text.monthVarId = monthVar.getAttribute("id");
		text.yearVarId = yearVar.getAttribute("id");
		text.immediate = immediate;
		text.updateVariables = theme.dateFieldUpdateVariables;
		text.onchange = text.updateVariables;
		text.client = this;
	}
},

dateFieldShowCalendar : function (e) {
	var evt = itmill.Client.prototype.getEvent(e);
	// "this" is triggering element that has inputs id in inputId
	var inputField = evt.target.ownerDocument.getElementById(evt.target.inputId);
	
	// This uses Calendar object directly, DO NOT USE setup() helper methods - it will leak
	var cal = window.calendar;

	var dValue = Date.parseDate(inputField.value, "%d,%m,%Y");

	console.debug("show calendar");
	
	var mustCreate = false;
	if(!cal) {
		// popping up calendar first time for this page on any field
		window.calendar = cal = new Calendar(
			1,
			dValue,
			itmill.themes.Base.prototype.dateFieldUpdateFromCalendar,
			itmill.themes.Base.prototype.dateFieldCloseCalendar
		);
		cal.setDateFormat("%d.%m.%Y");
		mustCreate = true;
	} else {
		// modifying existing calendar Object
		cal.setDate(dValue);
	}
	
	if(mustCreate)
		cal.create();
	
	cal.showAtElement(this);
	cal.triggerElement = this;
	
},

/** Called on Calendars  "close click" */
dateFieldCloseCalendar : function() {
	this.hide();
},

dateFieldUpdateFromCalendar : function (){
	// this is calendar object
	if(this.dateClicked) {
		// we have doped it with triggerElement that points to "..." Button
		var field = document.getElementById(this.triggerElement.inputId);
		field.value = this.date.print(this.dateFormat);
		field.updateVariables();
		this.hide();
	}
},

dateFieldUpdateVariables : function () {
	// "this" must be textField
	if (this.value == null || this.value == "") {
		return;
	}
	var a = this.value.split(".");
	
	this.client.changeVariable(this.dayVarId, a[0], false);
	this.client.changeVariable(this.monthVarId, a[1], false);
	this.client.changeVariable(this.yearVarId, a[2], this.immediate);
	
},

addDateFieldNullListener : function (client,elm,text,msec,sec,min,hour,day,month,year,yearVar,immediate) {
	this.client.addEventListener(elm, "change", function(event) {

		if ( !elm || elm.value != -1) return;


   			if (text) text.value = "";
   			
   			if (msec) {
   				msec.options[0].selected = true;
   				//client.changeVariable(msecVar.getAttribute("id"), -1, false);
   			}
   			if (sec) {
   				sec.options[0].selected = true;
    			//client.changeVariable(secVar.getAttribute("id"), -1, false);
   			}
   			if (min) {
   				min.options[0].selected = true;
   				//client.changeVariable(minVar.getAttribute("id"), -1, false);
   			}
   			if (hour) {
   				hour.options[0].selected = true;
   				//client.changeVariable(hourVar.getAttribute("id"), -1, false);
   			}
   			if (day) {
   				hour.options[0].selected = true;
   				//client.changeVariable(hourVar.getAttribute("id"), -1, false);
   			}
   			if (month) {
   				hour.options[0].selected = true;
   				//client.changeVariable(hourVar.getAttribute("id"), -1, false);
   			}
   			if (year) {
   				hour.options[0].selected = true;
   				//client.changeVariable(hourVar.getAttribute("id"), -1, false);
   			}
   			//client.changeVariable(dayVar.getAttribute("id"), -1, false);
   			//client.changeVariable(monthVar.getAttribute("id"), -1, false);
   			client.changeVariable(yearVar.getAttribute("id"), -1, immediate);
   });
},

renderDateFieldCalendar : function(renderer,uidl,target,layoutInfo) {

	var theme = renderer.theme;
	
	// Create containing element
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
	
	// Render default header
	renderer.theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
	
	// Get attributes
	var immediate = uidl.getAttribute("immediate") == "true";
	var enabled = uidl.getAttribute("enabled") == "true";
	var readonly = uidl.getAttribute("readonly") == "true";
	var yearVar = theme.getVariableElement(uidl,"integer","year");
    var monthVar = theme.getVariableElement(uidl,"integer","month"); 
    var dayVar = theme.getVariableElement(uidl,"integer","day");
	var yearVarId = yearVar.getAttribute("id");
    var monthVarId = monthVar.getAttribute("id"); 
    var dayVarId = dayVar.getAttribute("id");
    var initDate = new Date();
    initDate.setFullYear(yearVar.getAttribute("value"));
    initDate.setMonth(monthVar.getAttribute("value") - 1);
    initDate.setDate(dayVar.getAttribute("value"));
    
 	// Create container DIV
	var calDiv = theme.createElementTo(div,"div");
    var calDivId = uidl.getAttribute("id") + "_cal";
	calDiv.id = calDivId;
   
   // TODO this most likely leaks in IE, refactor like in normal field
   var pid = uidl.getAttribute("id");
   Calendar.setup({
   		firstDay     : 1,
   		date		  : initDate,
   		flat         : calDiv, // ID of the parent element
   		flatCallback : function (cal) {
   			var y = cal.date.getFullYear();
   			var m = cal.date.getMonth() + 1;
   			var d = cal.date.getDate();
   			theme.client.changeVariable(dayVarId, d, false);
   			theme.client.changeVariable(monthVarId, m, false);
   			theme.client.changeVariable(yearVarId, y, immediate);
   		}       
   });
},

renderUpload : function(renderer,uidl,target,layoutInfo) {

	var theme = renderer.theme;
	var client = renderer.client;
	var varNode = theme.getVariableElement(uidl,"uploadstream","stream");
	
	// Create containing element
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
	
	// We don't render header for upload, but use caption for submitting buttons text
	var caption = uidl.getAttribute("caption");
	if(caption == null || caption == "")
		caption = "Send";

	// Unique name for iframes
	var frameName = "upload_"+varNode.getAttribute("id")+"_iframe";
    
    var hIframeContainer = renderer.theme.createElementTo(div,"div");
    hIframeContainer.innerHTML = '<iframe style="height:0px;width:0px;0;margin:0;padding:0;border:0;" name="'+frameName+'"></iframe>'

    iframe = hIframeContainer.firstChild;
    ifr = iframe.contentWindow;

    // Ok. Now we are ready render the actual upload form and 
    // inputs.
    var formContainer = renderer.theme.createElementTo(div,"div");
    formContainer.innerHTML = 
    '<form action="'+client.ajaxAdapterServletUrl +
    '" method="post" enctype="multipart/form-data" target="'+frameName+'">'+
    '<input type="file" name="'+varNode.getAttribute("id")+'" />'+
    '<input type="submit" value="'+caption+'" onclick="javascript:this.form.parentNode.previousSibling.firstChild.submitted = true; this.form.submit(); return false;"/>' +
    '</form>'
    ;
    
    var form = formContainer.firstChild;
    if("true" == uidl.getAttribute("disabled")) {
    	form.firstChild.disabled = true;
    	form.lastChild.onclick = "javascript: return false;";
    	form.lastChild.disabled = true;
    }
    
    form.onsubmit = function() {
        iframe.submitted = true;
    };

    iframe.submitted = false;
	// Attach event listeners for processing the chencges after upload.
	if (document.all && !window.opera) {
		iframe.onreadystatechange = function() {
            if (iframe.submitted == true) {
                iframe.onreadystatechange = null;
                client.processVariableChanges(true);
            }
		};
	} else {
		iframe.onload = function() {
			if (iframe.submitted) {
                iframe.onload = null;
                client.processVariableChanges(true);
			}
		};
	}
},

renderEmbedded : function(renderer,uidl,target,layoutInfo) {

    var theme = renderer.theme;
    
	// Create containing element
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
	
	// Render default header
	renderer.theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
	
	if (uidl.getAttribute("type") == "image") {
	
		// Image mode
		var img = renderer.theme.createElementTo(div,"img","embedded");
		
		// SRC
		var val = uidl.getAttribute("src");			
		
		// Support for theme resources as images
		if (typeof val != 'undefined' && val != null && val.indexOf("theme://") == 0)
    		val = theme.root + val.substring(8);
    	
		if (val != null) img.src = val;
		
		// Width
		val = uidl.getAttribute("width");
		if (val != null && val > 0) img.width = val;
		
		// Height
		val = uidl.getAttribute("height");
		if (val != null && val > 0) img.height = val;
		
		// ALT-attribute
		img.alt = theme.getElementContent(uidl,"description");
	} else if (uidl.getAttribute("mimetype") == "application/x-shockwave-flash") {
	
		var html = "<object ";

		var val = uidl.getAttribute("width");
		if (val) html += " width=\""+val+"\" ";
		val = uidl.getAttribute("height");
		if (val) html += " height=\""+val+"\" ";
		
		html += 'classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" '+
		  'codebase="http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab"> ';

		val = uidl.getAttribute("src");

		// Support for theme resources 
		if (typeof val != 'undefined' && val != null && val.indexOf("theme://") == 0)
    		val = theme.root + val.substring(8);
		
		if (val) 
			html += '<param name="movie" value="'+val+'" /> ';

		var params = theme.getChildElements(uidl,"embeddedparams");
		if (params != null) {
			var len = params.length;
			for (var i=0;i<len;i++) {
				html += "<param name=\""+params[i].getAttribute("name")+"\" value=\""+params[i].getAttribute("name")+"\" />"
			}
		}
		
		html += '<embed ';
		val = uidl.getAttribute("width");
		if (val) html += " width=\""+val+"\" ";
		val = uidl.getAttribute("height");
		if (val) html += " height=\""+val+"\" ";
		val = uidl.getAttribute("src");
		if (val) html += " src=\""+val+"\" ";
		html += 'type="application/x-shockwave-flash" '+
			'pluginspage="http://www.macromedia.com/go/getflashplayer" />'
		html += '</object>';
		
		div.innerHTML = html;		

	} else {
		
		var html = "<object ";
		var val = uidl.getAttribute("src");
	
		// Support for theme resources 
		if (typeof val != 'undefined' && val != null && val.indexOf("theme://") == 0)
    		val = theme.root + val.substring(8);
	
		if (val) html += " data=\""+val+"\" ";
		
		val = uidl.getAttribute("width");
		if (val) html += " width=\""+val+"\" ";
		
		val = uidl.getAttribute("height");
		if (val) html += " height=\""+val+"\" ";
		
		val = uidl.getAttribute("codebase");
		if (val) html += " codebase=\""+val+"\" ";
		
		val = uidl.getAttribute("standby");
		if (val) html += " standby=\""+val+"\" ";
		
		val = uidl.getAttribute("mimetype");
		if (val) html += " type=\""+val+"\" ";
		
		html += ">";
		
		// Add all parameters
		var params = theme.getChildElements(uidl,"embeddedparams");
		if (params != null) {
			var len = params.length;
			for (var i=0;i<len;i++) {
				html += "<param name=\""+params[i].getAttribute("name")+"\" value=\""+params[i].getAttribute("name")+"\" />"
			}
		}
		
		html += "</object>";
		
		div.innerHTML = html;		
	}
},

renderLink : function(renderer,uidl,target,layoutInfo) {
	// Shortcut variables
	var theme = renderer.theme;
	var client = renderer.client;

	var immediate = "true"==uidl.getAttribute("immediate");
	var disabled = "true"==uidl.getAttribute("disabled");
	var readonly = "true"==uidl.getAttribute("readonly");

	var targetName = uidl.getAttribute("name");
	var width = uidl.getAttribute("width");
	var height = uidl.getAttribute("height");
	var border = uidl.getAttribute("border");
	var src = uidl.getAttribute("src");
	if (src && src.indexOf("theme://") == 0) {
		src = theme.root + src.substring(8);
	}	

	// Create containing element
	var div = theme.createPaintableElement(renderer,uidl,target,layoutInfo);
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

	var link = theme.createElementTo(div,"div", "link pad clickable");
	
	if (!disabled&&!readonly) {
		theme.addAddClassListener(theme,client,link,"mouseover","over");
		theme.addRemoveClassListener(theme,client,link,"mouseout","over");
		
		var feat;
		switch (border) {
			case "minimal":
				feat = "menubar=yes,location=no,status=no";
				break;
			case "none":
				feat = "menubar=no,location=no,status=no";
				break;
			default: 
				feat = "menubar=yes,location=yes,scrollbars=yes,status=yes";
				break;
		}
		if (width||height) {
			feat += ",resizable=no";
			feat += (width?",width="+width:"");
			feat += (height?",height="+height:"");
		} else {
			feat += ",resizable=yes";
		}
		theme.addLinkOpenWindowListener(theme,client,div,"click",src,targetName,feat);
	}

	// Render default header
	theme.renderDefaultComponentHeader(renderer,uidl,link);
	

},

addLinkOpenWindowListener : function(theme,client,element,event,url,target,features) {
	client.addEventListener(element,(event=="rightclick"?"click":event), function(e) {
			var evt = client.getEvent(e);
			if (event=="rightclick"&&!evt.rightclick) return;
			if (!target) {
				window.location = url;
			} else {
				var win = window.open(url, target, features);
				win.focus();
			}
		}
	);
},

renderPagingTable : function(renderer,uidl,target,layoutInfo) {
	// Shortcut variables
	var theme = renderer.theme;
	var client = renderer.client;
	
	// Create containing DIV
	var div = theme.createPaintableElement(renderer,uidl,target,layoutInfo);	
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
	
	// Mimic scroll table structure
	div.model = new Object();
	div.model.meta = new Object();
	
	// Clean possible scroll table material that's causing errors when switching 
	// back to scroll table rendering in feature browser (in IE)
	target.colWidths = null;

	// Create default header
	var caption = theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
	
	if ("list"==uidl.getAttribute("style")) {
		theme.removeCSSClass(div,"table-list");
		theme.addCSSClass(div,"list");
		theme.addCSSClass(caption,"listcaption");
	} else theme.addCSSClass(caption,"tablecaption");
	
	// Get table attributes
	var rowheaders = ("true"==uidl.getAttribute("rowheaders"));
	var totalrows = parseInt(uidl.getAttribute("totalrows"));
	var pagelength = parseInt(uidl.getAttribute("pagelength"));
	var rowCount = parseInt(uidl.getAttribute("rows"));
	var firstvisible = theme.getVariableElementValue(theme.getVariableElement(uidl,"integer","firstvisible"))||1;
	var firstvisibleVar = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"integer","firstvisible"));
	var immediate = ("true" == uidl.getAttribute("immediate"));
	var selectMode = uidl.getAttribute("selectmode");
	var selectable = selectMode == "multi" || selectMode == "single";
	var selected; // Selected map
	if (selectable) {
		selected = new Array();
	}
	var selectionVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","selected"));
	var visibleCols = div.model.visiblecols = theme.getFirstElement(uidl,"visiblecolumns");
	var collapseVariable = theme.createVarFromUidl(div,theme.getVariableElement(uidl,"array","collapsedcolumns"));
	var sortcolVar = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"string","sortcolumn"));
	var sortkey = theme.getVariableElementValue(theme.getVariableElement(uidl,"string","sortcolumn"));
	var sortasc = theme.getVariableElement(uidl,"boolean","sortascending");
	var sortascVar = theme.createVariableElementTo(div,sortasc);
	sortasc = sortasc != null && "true"==sortasc.getAttribute("value");
	
	var actions = null;
	var actionVar = null;
	var alNode = theme.getFirstElement(uidl,"actions")
	if (alNode) {
		actionVar = theme.createVarFromUidl(div,theme.getVariableElement(alNode,"string","action"));
		actions = true;
		div.model.meta.actions = new Array();
		var ak = alNode.getElementsByTagName("action");
		for (var i=0;i<ak.length;i++) {
			div.model.meta.actions[ak[i].getAttribute("key")] = ak[i].getAttribute("caption");
		}
	}
	delete alNode;
		
	// Create table for content
	div = theme.createElementTo(div,"div","outset");
	div = theme.createElementTo(div,"div","content border");
	
	var table = theme.createElementTo(div,"table");
	table = theme.createElementTo(table,"tbody");
	table.setAttribute("cellpadding","0");
	table.setAttribute("cellspacing","0");
	var tr = null;
	var td = null;

	
	// Column headers
	var cols = theme.getFirstElement(uidl,"cols");
	if (cols != null) {
		cols = cols.getElementsByTagName("ch");
	}
	if (/*cols != null && cols.length >0*/true) {
		tr = theme.createElementTo(table,"tr","header");
		if (rowheaders) {
			td = theme.createElementTo(tr,"td","empty");
			// TODO enable sorting by rowheader
		}
		for (var i=0; i<cols.length;i++) {
			var sortable = cols[i].getAttribute("sortable");
			td = theme.createElementTo(tr,"td","cheader bg");
			// Sorting
			var key = cols[i].getAttribute("cid");
			if (sortable=="true") {
				theme.addCSSClass(td,"clickable");
				// Sorting always immediate
				theme.addSetVarListener(theme,client,td,"click",sortascVar,(key==sortkey?!sortasc:sortasc),false);
				theme.addSetVarListener(theme,client,td,"click",sortcolVar,key,true);
				
			}
			var ch = cols[i].getAttribute("caption");
			var cap = theme.createElementTo(td,"div","caption");
			theme.createTextNodeTo(cap,ch != null? ch : "");
			if (sortkey==key) {
				theme.addCSSClass(td,sortasc?"asc":"desc");
			}
		}
		
		// Collapsing
		td = theme.createElementTo(tr,"td","cheader scroll bg");
		if (visibleCols) {
			var iconDiv = theme.createElementTo(td,"div","img");
			var icon = theme.createElementTo(iconDiv,"img","icon");
			icon.src = theme.root+"img/table/colsel.gif";
			renderer.client.addEventListener(iconDiv,"click",theme.tableShowColumnSelectMenu);
		}
	}
	delete cols;

	// Table rows
	var rows = theme.getFirstElement(uidl,"rows");
	if (rows != null) {
		rows = theme.getChildElements(rows,"tr");
	}
	if (rows != null && rows.length >0) {
		for (var i=0; i<rows.length;i++) {
			tr = theme.createElementTo(table,"tr");
			// TODO rowheader
			theme.setCSSClass(tr, (i % 2 == 0?"even":"odd"));

			if (selectable) theme.addCSSClass(tr, "clickable");
			var key = rows[i].getAttribute("key");
			tr.key = new Object();
			tr.key = key;
			
			if (selectable&&"true"==rows[i].getAttribute("selected")) {
				theme.addCSSClass(tr, "selected");
				selected[selected.length] = tr;
			}

			if (selectable) {
				if (selectMode == "multi") {
					theme.addToggleClassListener(theme,client,tr,"click","selected");
					theme.addToggleVarListener(theme,client,tr,"click",selectionVariable,key,immediate);
				} else {
					theme.addAddClassListener(theme,client,tr,"click","selected",tr,selected);
					theme.addSetVarListener(theme,client,tr,"click",selectionVariable,key,immediate);
				}
			}

			if (rowheaders) {
				var td = theme.createElementTo(tr,"td","rheader bg");
				var caption = theme.createElementTo(td,"div","caption");
				theme.createTextNodeTo(caption,rows[i].getAttribute("caption"));
			}
			if (rows[i].childNodes != null && rows[i].childNodes.length >0) {
				var al = null; 
				for (var j=0; j<rows[i].childNodes.length;j++) {
					if (rows[i].childNodes[j].nodeName == "al") {
						al = rows[i].childNodes[j];
					} else if (rows[i].childNodes[j].nodeType == Node.ELEMENT_NODE) {
						td = theme.createElementTo(tr,"td");
						renderer.client.renderUIDL(rows[i].childNodes[j],td);
					}
				}
			}
			
			// extract actions that this particular node has
			if(actions) {
				var actionList = new Array();
				for(var j = 0; j < al.childNodes.length; j++) {
					actionList.push(al.childNodes[j].firstChild.data);
				}
				tr.actionList = actionList;
				client.addEventListener(tr,"contextmenu",theme.tableRowShowContextMenu);
				if(window.opera)
					client.addEventListener(tr,"click",theme.tableRowShowContextMenu);	
			}
			
			// SCROLLBAR
			/*
			if (i==0) {
				td = theme.createElementTo(tr,"td", "scroll border");
				// TODO:
				//theme.tableAddScrollEvents(theme,td);
				
				td.setAttribute("rowSpan",rows.length);
				var inner = theme.createElementTo(td,"div", "scroll");
			}
			*/		
		}
	}
	delete rows;
	
	var paging = theme.createElementTo(div,"div","nav pad");
	var button = theme.createElementTo(paging,"div","pad caption inline");
	if (firstvisible > 1) {
		theme.addCSSClass(button,"clickable prev");
		theme.addAddClassListener(theme,client,button,"mouseover","bg");
		theme.addRemoveClassListener(theme,client,button,"mouseout","bg");
		theme.addSetVarListener(theme,client,button,"click",firstvisibleVar,(String) (parseInt(firstvisible)-parseInt(pagelength)),true);
	} else {
		theme.addCSSClass(button,"disabled");
	}
	theme.createTextNodeTo(button,"<<");
	
	button = theme.createElementTo(paging,"div","current pad inline");
	theme.createTextNodeTo(button,firstvisible+" - "+(firstvisible-1+parseInt(rowCount))+ " / " + totalrows);
	
	button = theme.createElementTo(paging,"div","pad caption inline");
	if (parseInt(firstvisible)+parseInt(pagelength)<=parseInt(totalrows)) {
		theme.addCSSClass(button,"clickable next");
		theme.addAddClassListener(theme,client,button,"mouseover","bg");
		theme.addRemoveClassListener(theme,client,button,"mouseout","bg");
		theme.addSetVarListener(theme,client,button,"click",firstvisibleVar, (String) (parseInt(firstvisible)+parseInt(pagelength)),true);
	} else {
		theme.addCSSClass(button,"disabled");
	}
	theme.createTextNodeTo(button,">>");
},

/*
SCROLLTABLE GLOSSARY
hout : header tables container div, id is in form PID69hout
hin : header table (separate table to allow smooth resizing)
cout : content tables container div, id is in form PID69cout
cin : content table
heh && hah : something concerng row headers

aSpacer : spacer above table taking up space for unloaded rows
bSpacer : spacer below table taking up space for unloaded rows

This is to be added soon
To make scrolling smoother and make we will save row data to a local data structure



*/

renderScrollTable : function(renderer,uidl,target,layoutInfo) {
    // TODO colorder too model or straight to div

    // Build a model object of table, that will later be saved to containing div
    // An update, like scrolling, can then compare changes and update only changed parts of final dom
    var model = new Object();
    model.meta = new Object(); // change in here effects total redraw
    model.state = new Object(); // things like first visible row, selections etc
    model.request = new Object(); // firstrow, rows
    
    // TODO consider if really necessary to implement these
    model.headerCache = new Object(); // compare this to original, to detect need for header redraw
    model.bodyCache = new Object(); // compare this to original cell by cell, to detect need for contents redraw
    
	// Shortcut variables
	var theme = renderer.theme;
	var client = renderer.client;
	var colWidths;
    
    var redraw = false;
    var allowUpdate = true;
	if (target.colWidths) {
		if(target.model.tableBody.childNodes.length > 0) {
    	    // we are repainting existing table with rows in it
    	    redraw = true;
			colWidths = target.colWidths;
		} else {
			// table is empty -> delete colWidths to recalc
			target.colWidths = null;
			colWidths = model.colWidths = new Object();
		}
	} else {
		colWidths = model.colWidths = new Object();
	}

    // TODO remove this if possible
	var scrolledLeft = target.scrolledLeft;
    
    // Get attributes
    // TODO remove separate variables and change function to use variables in model object
	var pid        = model.pid     = uidl.getAttribute("id");
	var immediate  = model.meta.immediate = uidl.getAttribute("immediate")||false;
	var selectmode = model.meta.selectmode = uidl.getAttribute("selectmode");
	var cols       = model.meta.cols = parseInt(uidl.getAttribute("cols"));
	var totalrows  = model.meta.totalrows = parseInt(uidl.getAttribute("totalrows"));
	model.meta.disabled = ("true" == uidl.getAttribute("disabled"));
    
	var pagelength = model.meta.pagelength = uidl.getAttribute("pagelength") ? parseInt(uidl.getAttribute("pagelength")) : model.meta.totalrows;
    model.meta.readonly = uidl.getAttribute("readonly") || false;
    model.meta.sizeableW = uidl.getAttribute("width") || false;
    model.meta.sizeableH = uidl.getAttribute("height") || false; 
	model.meta.colheaders = uidl.getAttribute("colheaders")||false;
	var rowheaders = model.meta.rowheaders = uidl.getAttribute("rowheaders")||false;
    model.request.rows = parseInt(uidl.getAttribute("rows"));
    model.request.firstrow = parseInt(uidl.getAttribute("firstrow"));
    model.meta.rowsHasActions = theme.getFirstElement(uidl, "ak") || false;
	var visiblecols= model.visiblecols =  theme.getFirstElement(uidl,"visiblecolumns");
    model.columnorder = theme.getVariableElement(uidl,"array","columnorder");
	var sortkey    = model.meta.sortkey = theme.getVariableElementValue(theme.getVariableElement(uidl,"string","sortcolumn"));
    
    
    
    model.meta.cacheRate = 3; // means times pagelength
    // means threshold when new cache row fetch is instantiated 
    // if 0 fetches only when really needed, 
    // when same as reactRate fetches even if scrolled only one row
    model.meta.cacheReactRate = 2.3; 
    model.meta.cacheSize = Math.ceil(model.meta.cacheRate*model.meta.pagelength); // means times pagelength
    model.meta.cacheReactTh = Math.ceil(model.meta.cacheReactRate*model.meta.pagelength); // means threshold when new cache row fetch is instantiated
	
    // column order
	model.colorder = new Array();
	var fv = model.state.fv = parseInt(theme.getVariableElementValue(theme.getVariableElement(uidl,"integer","firstvisible"))||1);
	if (selectmode != "none") {
		model.selected = new Array();
	}
	
    // In that case only scrolled, determine that and 
    // fork into other function otherwise continue
    
    var div = false;
    // check if this table has been drawn before and this is just a scroll update
    if(redraw) {
        // this will be done by comparing critical parts of model object constructed from uidl and the one stored in target (paintable div)
        for(j in model.meta) {
            if(target.model.meta[j] != model.meta[j] || typeof(target.model.meta[j]) != typeof(model.meta[j])) {
                allowUpdate = false;
                break;
            }
        }
        if (model.request.firstrow == target.model.state.fv) {
            // this is not a scroll event, redraw whole table
            allowUpdate = false;
        }
        if (allowUpdate) {
            console.info("Update existing table");
            div = target;
            theme.scrollTableScrollUpdate(renderer, div, model, uidl);
            return;
        }
    }
    // rest of the function is ideally run only when first time painting table
    if(!div) {
        // Create containing DIV
        var div = theme.createPaintableElement(renderer,uidl,target,layoutInfo);
    }
    // save reference of model object to dom
    div.model = model;
    model.state.firstRendered = model.state.fv
    model.state.lastRendered = model.state.fv + model.request.rows - 1;
    
    
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

	// Variables
    // TODO create these only if redrawing, if updating update values
    
    
    var fvVar = theme.createVarFromUidl(div,theme.getVariableElement(uidl,"integer","firstvisible"));
    // this is a hack to scroll always to top on Safari 2.0.*
    if(navigator.userAgent.indexOf("AppleWebKit/4") > 0 ) {
    	console.warn("Safari don't support scrollTop, scroll up");
    	if(fvVar && fvVar.value > 1) {
    		// need to scroll top on every redraw
        	var fvVar = theme.createVarFromUidl(div,theme.getVariableElement(uidl,"integer","firstvisible"));
			fvVar.value = 1;
			delete target.colWidths;
			theme.updateVar(this, fvVar, true);
			return;
			// now render to the end and then "scrollup" on cache request
    	}
    }
    
    var reqrowsVar = theme.createVarFromUidl(div,theme.getVariableElement(uidl,"integer","reqrows"));
    var reqfirstrowVar = theme.createVarFromUidl(div,theme.getVariableElement(uidl,"integer","reqfirstrow"));


	var ccVar = theme.createVarFromUidl(div,theme.getVariableElement(uidl,"array","collapsedcolumns"));
	var coVar = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","columnorder"));
	var selVar = model.selVar = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","selected"));
	var sortVar = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"string","sortcolumn"));
	var sortasc = theme.getVariableElement(uidl,"boolean","sortascending");
	var sortascVar = theme.createVariableElementTo(div,sortasc);
	sortasc = (sortasc != null && "true"==sortasc.getAttribute("value"));


	// Create default header
	var caption = theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
	theme.addCSSClass(caption, "tablecaption");
	
	
	// column collapsing
	// main div
	var inner  = theme.createElementTo(div,"div","border");

    // TODO check if this is needed
	var offsetLeft = client.getElementPosition(inner).x;
    
    // TODO move building actions object to beginning of the funtion -> redraw if actions change
	// Actions
	var actions = null;
	var actionVar = null;
	var alNode = theme.getFirstElement(uidl,"actions")
	if (alNode) {
		actionVar = theme.createVarFromUidl(div,theme.getVariableElement(alNode,"string","action"));
		actions = new Object();
		var ak = alNode.getElementsByTagName("action");
		for (var i=0;i<ak.length;i++) {
			actions[ak[i].getAttribute("key")] = ak[i].getAttribute("caption");
		}
        model.meta.actions = actions;
	}
	delete alNode;

	inner.innerHTML = "<div id=\""+pid+"status\" class=\"tablestatus\" style=\"display:none;\"></div>";
    inner.innerHTML += '<div class="colsel-container"></div><div class="hcontainer"></div>';
	var vcols = model.vcols = inner.childNodes[1];
	if (visiblecols && ! model.meta.disabled) {
		vcols.innerHTML = "<DIV class=\"colsel\"><div></div></DIV>";
		renderer.client.addEventListener(vcols,"click",theme.tableShowColumnSelectMenu);
	}


	var alignments = model.alignments = new Array();
	
	// headers
	var hout = theme.createElementTo(inner.childNodes[2],"div","bg");
    model.hout = hout; // add reference for later use
	hout.id = pid+"hout";
	hout.style.overflow = "hidden";	
	theme.addCSSClass(hout,"hout");
	var html = "<div><TABLE id=\""+pid+"hin\" class=\"hin\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><TBODY><TR>";	
	if (rowheaders) {
		html += "<td ";
		html += "cid=\"heh\" id=\""+pid+"heh\" class=\"heh\">";
        html += "<img id=\""+pid+"hah\" src=\""+theme.root+"img/table/handle.gif\" class=\"colresizer\" >";
        html += "<div class=\"headerContent\" style=\"";
		html += "\"></div></td>";
	}

	var chs = theme.getFirstElement(uidl, "cols").getElementsByTagName("ch");
	var len = chs.length;
	for (var i=0;i<len;i++) {
		var col = chs[i];
		var cap =  col.getAttribute("caption")||(visiblecols?"":"");
		var sort =  col.getAttribute("sortable");
		var cid =  col.getAttribute("cid");
		var iconUrl =  col.getAttribute("icon");
		if (iconUrl && iconUrl.indexOf("theme://") == 0) {
		    iconUrl = (theme.iconRoot != null ? theme.iconRoot : theme.root) 
		    				+ iconUrl.substring(8);
		}		
		alignments[i] = col.getAttribute("align");		
		model.colorder[i] = cid;
		html += "<TD ";
        var cellClasses = '';
		if (sortkey == cid) {
            html += "sorted=\"true\" ";
            if(sortasc) {
                cellClasses += 'asc ';
            } else {
                cellClasses += 'desc';
            }
		}
        
		html += " class=\""+cellClasses+"\" cid=\""+cid+"\" id=\""+pid+"he"+i+"\" >"
        // add image that is used for col resizing, width set in css
        html += '<img id="'+pid+'ha'+cid+'" src="'+theme.root+'img/table/handle.gif" class="colresizer" />';
		html += '<div class="headerContent';
		if (alignments[i]) {
			switch (alignments[i]) {
				case "e":
					html += " align_right";
					break;
				case "c":
					html += " align_center";
					break;
				default:
			}
		}
		html += '" ';
		html += ">";
        html += (iconUrl?"<img src=\""+iconUrl+"\" class=\"icon\">":"")+cap+"</div></TD>";
	}
	html += '</TR></TBODY></TABLE><div>';
	hout.innerHTML = html;
	if(!model.meta.colheaders) {
        hout.style.display = "none";
        vcols.style.display = "none";
    }
    
	// Render CONTENT
	var cout = model.cout = theme.createElementTo(inner,"div");
	cout.id = pid+"cout";
	theme.addCSSClass(cout,"cout");
	
	// We have set this attribute also in CSS, but due reindeer bug (?)
	// set scrolling in JS also
	cout.style.overflow = "scroll";
	
	// Now we have a very weird bugfix: mac FF has big issues setting scrollbars
	// to right layer. Now that we have set up new scrollbars and possibly under
	// frontmost window, we need to "shake" the frontmost window a bit
	var agent = navigator.userAgent.toLowerCase();
	// try to workaround nasty scrollbar problems with mac firefox
	if(this.windowOrder && this.windowOrder[0] && agent.indexOf("mac") > 0 && agent.indexOf("firefox") > 0 ) {
		var ol = this.windowOrder[client.windowOrder.length -1]._ol;
		ol._div.style.overflow = "auto";
		var fool = ol._div.offsetHeight;
		ol._div.style.overflow = "visible";
		fool = ol._div.offsetHeight;
	}
    
    // create spacer elements and save reference to model (needed for webkit bug)
    // Otherwice we simply use margins
    model.aSpacer = theme.createElementTo(cout,"div");
    model.aSpacer.className = "spacer";

    var d = cout.ownerDocument;
    var table = d.createElement("table");
    table.id = pid + "cin";
    table.className = "cin";
    var tableB = model.tableBody = d.createElement("tbody");
    // get rows from uidl
    var rows = theme.getFirstElement(uidl, "rows");
    // variables used building table
    var tr = null;
    var td = null;
    var tdDiv = null;
    var icon = null;
    
    var df = d.createDocumentFragment();
    // TODO check for optimizations
    var len = rows.childNodes.length; 
	for (var i = 0; i <len;i++) {
        var row = rows.childNodes[i];
		var cap =  row.getAttribute("caption");
		var key =  row.getAttribute("key");
		var seld = row.getAttribute("selected");
		var iconUrl = row.getAttribute("icon");
        tr = d.createElement("tr");
        tr.key = key;
        tr.className = ((i + model.state.fv ) %2==0) ? "even" : "odd";
        if (seld) {
            tr.selected = true;
            tr.className = " selected";
        }
		
		if (rowheaders) {
            td = d.createElement("td"); tdDiv = d.createElement("div");
            td.className = "tablecell";
            tdDiv.className = "cellContent";
			if (iconUrl) {
                icon = d.createElement("img");
                icon.className = "icon";
                icon.src = iconUrl;
                tdDiv.appendChild(icon);
			}
            tdDiv.appendChild(d.createTextNode(row.getAttribute("caption")));
            td.appendChild(tdDiv);
            tr.appendChild(td);
		}	
		var comps = row.childNodes;
		var l = comps.length;
		if (l==0) {
            // add empty cell if no cells exists
            td = d.createElement("td"); tdDiv = d.createElement("div");
            td.className = "tablecell"; tdDiv.className = "cellContent";
            td.appendChild(tdDiv);
            tr.appendChild(td);
		}
		var al = null;
		var colNum = -1;
		for (var j=0;j<l;j++) {
			var comp = comps[j];
            if (comp.nodeName == "al" ) {
                al = comp;
                continue;
            }
			colNum++;
			// Placeholder TD, we'll render the content later
            td = d.createElement("td"); tdDiv = d.createElement("div");
            td.className = "tablecell"; tdDiv.className = "cellContent";
            if (alignments[colNum]) {
                switch (alignments[colNum]) {
                    case "e":
                        td.className += " align_right";
                        break;
                    case "c":
                        td.className += " align_center";
                        break;
                    default:
                }
            }
            // render content
            if(comp.nodeName == 'label' && ! comp.getAttribute("caption") && comp.firstChild && comp.firstChild.data) {
                // skip heavy renderUIDL function if only text
                tdDiv.appendChild(d.createTextNode(comp.firstChild.data));
            } else {
                client.renderUIDL(comp, tdDiv);
            }
            td.appendChild(tdDiv);
            tr.appendChild(td);
		}
        if (al && tr.firstChild && ! model.meta.disabled) {
			// extract actions that this particular node has
			var actionList = new Array();
			for(var j = 0; j < al.childNodes.length; j++) {
				actionList.push(al.childNodes[j].firstChild.data);
			}
			tr.actionList = actionList;
			client.addEventListener(tr,"contextmenu",theme.tableRowShowContextMenu);
			if(window.opera)
				client.addEventListener(tr,"click",theme.tableRowShowContextMenu);
        }
        // selection
        if (model.meta.selectmode != "none"  && ! model.meta.readonly && ! model.meta.disabled) {
            model.selected.push(tr);
            theme.addCSSClass(tr,"clickable");
            theme.addToggleClassListener(theme,client,tr,"mouseover","selectable");
            theme.addToggleClassListener(theme,client,tr,"mouseout","selectable");
            if (selectmode == "multi") {
                theme.addToggleClassListener(theme,client,tr,"click","selected");
                theme.addToggleVarListener(theme,client,tr,"click",selVar,key,immediate);
            } else {
                theme.addAddClassListener(theme,client,tr,"click","selected",tr,model.selected);
                theme.addSetVarListener(theme,client,tr,"click",selVar,key,immediate);
            }
        }
        df.appendChild(tr);
	}
    tableB.appendChild(df);
    table.appendChild(tableB);
	cout.appendChild(table);
    // create spacer elements and save reference to model (needed for webkit bug on table margins)
    model.bSpacer = theme.createElementTo(cout,"div");
    model.bSpacer.className = "spacer";
    

	// Do some initial sizing and scrolling
    
    model.rowheight = table.rows.length ? Math.ceil(table.offsetHeight/table.rows.length) : 22;
    // scroll padding calculations 
    
    if(model.state.fv > 0)
    	var prePad = (model.state.fv - 1) * model.rowheight;
	else
		var prePad = 0;
    // remaining invisible lines * line_height
    var postPad = (model.meta.totalrows-model.state.fv-model.request.rows+1)*model.rowheight;

    // set height defined by sizeable interface
    // TODO refine (works only for pixels atm)
    if(model.meta.sizeableH) {
        var extraH = div.offsetHeight - cout.offsetHeight;
        // snip or grow size from "scrolling" part
        cout.style.height = ( parseInt(model.meta.sizeableH) - extraH ) + "px";
    } else {
        // fix containers height to initial height of table + scrollbar
        // due, some timing issues, tables height is not always stabilized,
        // so calculate using pagelength & rowheight instead of offsetHeight
        cout.style.height = (model.meta.pagelength*model.rowheight+16)+"px";
    }

    model.aSpacer.style.height = prePad + "px";
    model.bSpacer.style.height = postPad + "px";
    
    cout.scrollTop = prePad;
    
	div.recalc = theme.scrollTableRecalc;
 	div.recalc(pid,target);
	cout.scrollLeft = scrolledLeft;
	hout.scrollLeft = scrolledLeft;


    
    // fix width of table component to its initial width if not explicetely set via Sizeable interface
    if(!model.meta.sizeableW) {
        model.state.width = div.style.width = (cout.scrollWidth + 19 ) + "px";
    } else {
        if(model.meta.sizeableW.indexOf("%") < 0) {
            model.state.width = model.meta.sizeableW + "px";
        } else {
            model.state.width = model.meta.sizeableW;
        }
        div.style.width = model.state.width;
    }
    cout.style.width = (parseInt(model.state.width) - 4) + "px";
    hout.style.width = (parseInt(model.state.width) - 4) + "px";
    // ensure browsers don't make any intelligent cell resizing
    hout.firstChild.style.width = "6000px";
    

	var status = target.ownerDocument.getElementById(pid+"status");
    model.status = status;
	var p = client.getElementPosition(hout);
	status.style.marginTop = 35 + "px";
	status.style.marginLeft = Math.round(div.offsetWidth/2 - 75 ) +"px";
    vcols.style.marginLeft = (div.offsetWidth - 22) + "px";

	if(! model.meta.disabled) {
		// add listener only for enabled table
	    theme.scrollTableAddScrollHandler(client,theme,div);
	    theme.scrollTableAddScrollListener(theme,div);
	 	// Column order drag & drop
	 	var hin = target.ownerDocument.getElementById(pid+"hin");
	    var h = hin.getElementsByTagName("td");
	    var dragOrderGroup = new Object();
	    for (var i = 0;i<h.length;i++) { 
	    	var id = h[i].getAttribute("id");  
	    	if (id==pid+"heh") {
		        var handle = target.ownerDocument.getElementById(pid+"hah");
		        if (handle) {
		        	theme.tableAddWidthListeners(client,theme,handle,"heh",div,pid);
		        }
	    	}
	 		if (!id||id.indexOf(pid+"he")<0) {
	            continue;
	        }   
	        var cid = h[i].getAttribute("cid");
	        var handle = target.ownerDocument.getElementById(pid+"ha"+cid);
	        if (handle) {
	        	theme.tableAddWidthListeners(client,theme,handle,cid,div,pid);
	        }
	        if (coVar||sortVar) {
	        	theme.addCSSClass(h[i],"clickable");
	        	theme.addToDragOrderGroup(client,theme,h[i],dragOrderGroup,coVar,sortVar,sortascVar,sortasc);
	        }
	    }
	    
	    var hin = target.ownerDocument.getElementById(pid+"hin");
	    var cin = target.ownerDocument.getElementById(pid+"cin");
	    theme.scrollTableRegisterLF(client,theme,div,inner,cout,hout,cin,hin);
	    
	    // send request to fetch cache rows
	    if (model.meta.totalrows > 0 && model.meta.totalrows > model.state.lastRendered) {
	        reqfirstrowVar.value = model.state.lastRendered + 1;
	        reqrowsVar.value = model.meta.cacheSize;
	        theme.updateVar(client,reqfirstrowVar, false);
	        theme.updateVar(client,reqrowsVar, true);
	    }

	} else {
		// if disabled remove, scrollbars
	    theme.scrollTableRegisterLF(client,theme,div,inner,cout,hout,cin,hin);
		cout.style.overflow = "hidden";
	}
    
},

/**
 * renderScrollTable passes updating component to this funtion if scrolled and only body changes
 * @param target main container div
 * @param model constructed tables model object from uidl
 */
scrollTableScrollUpdate : function(renderer,target, model,uidl) {
    console.info("Updating new rows to existing table");
    var tm = target.model;
    var theme = renderer.theme;
    var d = target.ownerDocument;
    var tableBody = tm.tableBody;
    var colorder = tm.colorder;
    
    /* define function that creates rows */
    var createRow = function(ruidl, odd, selectmode) {
        var row = d.createElement("tr");
        var key = row.key = ruidl.getAttribute("key");
        row.className = (odd) ? "odd" : "even";
        if (ruidl.getAttribute("selected")) {
            row.selected = true;
            row.className += " selected";
        }
        
        if(model.meta.rowheaders) {
            var rhCell = d.createElement("td");
            rhCell.className = "tablecell";
            var cellContent = d.createElement("div");
            cellContent.className = "cellContent";
            cellContent.style.width = (target.colWidths["heh"] - 4) + "px";
            // TODO row icon ???
            cellContent.innerHTML = ruidl.getAttribute("caption");
            rhCell.appendChild(cellContent);
            row.appendChild(rhCell);
        }
        
        // rows nodes
        var comps = ruidl.childNodes;
        var l = comps.length;
        var currentCol = -1;
        var al = null; // rows action listeners
        for (var k=0;k<l;k++) {
            var comp = comps[k];
            if (comp.nodeName == "#text") continue;
            if (comp.nodeName == "al") {
                al = comp;
                continue;
            }
            // create table cell and cellContent div
            var cell = d.createElement("td");
            currentCol++;
            cell.className = "tablecell";
            
            if (tm.alignments[currentCol]) {
                switch (tm.alignments[currentCol]) {
                    case "e":
                        cell.className += " align_right";
                        break;
                    case "c":
                        cell.className += " align_center";
                        break;
                    default:
                }
            }
            
            
            var cellContent = d.createElement("div");
            cellContent.className = "cellContent";
            // table cell shoudn't need explicit size, but due IE bug, explicitely set content divs size
            cellContent.style.width = (target.colWidths[colorder[currentCol]] - 4) + "px";
            cell.appendChild(cellContent);
            // render cell content
            if(comp.nodeName == 'label' && ! comp.getAttribute("caption") && comp.firstChild && comp.firstChild.data) {
                // skip heavy renderUIDL function if only text
                cellContent.appendChild(d.createTextNode(comp.firstChild.data));
            } else {
                renderer.client.renderUIDL(comp, cellContent);
            }
            
            row.appendChild(cell);
        }
        if (al&&row.firstChild) {
        	// extract actions that this particular node has
			var actionList = new Array();
			for(var j = 0; j < al.childNodes.length; j++) {
				actionList.push(al.childNodes[j].firstChild.data);
			}
			row.actionList = actionList;
			renderer.client.addEventListener(row,"contextmenu",theme.tableRowShowContextMenu);
			if(window.opera)
				renderer.client.addEventListener(row,"click",theme.tableRowShowContextMenu);
        }
        // selection
        if (model.meta.selectmode != "none" && ! model.meta.readonly) {
            var client = renderer.client;
            tm.selected.push(row);
            theme.addCSSClass(row,"clickable");
            theme.addToggleClassListener(theme,client,row,"mouseover","selectable");
            theme.addToggleClassListener(theme,client,row,"mouseout","selectable");
            if (selectmode == "multi") {
                theme.addToggleClassListener(theme,client,row,"click","selected");
                theme.addToggleVarListener(theme,client,row,"click",tm.selVar,key,model.meta.immediate);
            } else {
                theme.addAddClassListener(theme,client,row,"click","selected",row,tm.selected);
                theme.addSetVarListener(theme,client,row,"click",tm.selVar,key,model.meta.immediate);
            }
        }
        
        return row;
    } // end defining createRows function

    // get array of received row elements
    var trs = theme.getFirstElement(uidl, "rows").getElementsByTagName("tr");
    
    if (model.request.rows == 0) {
        console.info("No new rows were loaded");
    } else if(model.request.firstrow == tm.state.lastRendered + 1) {
        // if first received row == lastRendered + 1 we have moderate update to end of table
        // -> add received rows to the end of the table and resize bSpacer
        for(var i = 0; i < trs.length; i++) {
            var row = createRow(trs[i], ((model.request.firstrow + i)%2 == 1), tm.meta.selectmode);
            tableBody.appendChild(row);
            tm.bSpacer.style.height = (parseInt(tm.bSpacer.style.height) - tm.rowheight) + "px";
            tm.state.lastRendered++;
        }
        // remove rows from beginning not to put browser to its knees in case of verybigmillionlinetable
        while( tm.meta.cacheSize < (model.state.fv - tm.state.firstRendered) ) {
                tableBody.removeChild(tableBody.firstChild);
                tm.aSpacer.style.height = (parseInt(tm.aSpacer.style.height) + tm.rowheight) + "px";
                tm.state.firstRendered++;
        }
    } else if(model.request.firstrow + model.request.rows == tm.state.firstRendered) {
        // moderate update to beginning of the table
        for(var i = trs.length -1 ; i >= 0; i--) {
            // render a new row
            row = createRow(trs[i], ((tm.state.firstRendered - 1)%2 == 1 ), tm.meta.selectmode);
            tableBody.insertBefore(row, tableBody.firstChild);
            // adjust top margin
            tm.aSpacer.style.height = (parseInt(tm.aSpacer.style.height) - tm.rowheight) + "px";
            // update firstRendered value
            tm.state.firstRendered--;
        }
        // remove rows from the end not to put browser to its knees in case of verybigmillionlinetable
        while( tm.meta.cacheSize < (tm.state.lastRendered - model.state.fv -model.meta.pagelength ) ) {
                tableBody.removeChild(tableBody.lastChild);
                tm.bSpacer.style.height = (parseInt(tm.bSpacer.style.height) + tm.rowheight) + "px";
                tm.state.lastRendered--;
        }
    } else if(model.request.firstrow > tm.state.lastRendered) {
        // big scroll down
        // truncate old tbody and resize aSpacer + bSpacer to fit whole space
        var tmp = d.createElement("tbody");
        tableBody.parentNode.replaceChild(tmp, tableBody);
        tableBody = tm.tableBody = tmp;
        tm.aSpacer.style.height = (
            parseInt(tm.aSpacer.style.height) + 
            tm.rowheight * ( 
                (model.request.firstrow - tm.state.firstRendered)
                ) ) + "px";
        // set bSpacer to downloaded rows + remaining rows
        tm.bSpacer.style.height = (tm.rowheight * (tm.meta.totalrows - model.request.firstrow + 1) ) + "px";
        
        // build tbody from received rows
        tm.state.firstRendered = model.request.firstrow;
        tm.state.lastRendered = model.request.firstrow - 1;
        for(var i = 0; i < trs.length; i++) {
            var row = createRow(trs[i], ((model.request.firstrow + i)%2 == 1), tm.meta.selectmode);
            tableBody.appendChild(row);
            tm.bSpacer.style.height = (parseInt(tm.bSpacer.style.height) - tm.rowheight) + "px";
            tm.state.lastRendered++;
        }
    
    } else if(model.request.firstrow + model.request.rows < tm.state.firstRendered) {
        // big scroll up
        //  truncate old tbody and resize aSpacer + bSpacer to fit whole space
        var tmp = d.createElement("tbody");
        tableBody.parentNode.replaceChild(tmp, tableBody);
        tableBody = tm.tableBody = tmp;
        tm.bSpacer.style.height = ( 
            tm.rowheight * ( model.meta.totalrows - (model.request.firstrow + model.request.rows) + 1 )
                 ) + "px";
        tm.aSpacer.style.height = ( tm.rowheight * (model.request.firstrow - 1)) + "px";
        
        // build tbody from received rows
        tm.state.firstRendered = model.request.firstrow;
        tm.state.lastRendered = model.request.firstrow - 1;
        for(var i = 0; i < trs.length; i++) {
            var row = createRow(trs[i], ((model.request.firstrow + i)%2 == 1), tm.meta.selectmode);
            tableBody.appendChild(row);
            tm.state.lastRendered++;
        }
    } else {
        // renderer.client.debug("Unhandled update to table (scrolled back, scrolled again shortly or something)");
    }

    // update model object
    // loop all variables from model to tm
    for(var j in model.state) {
        tm.state[j] = model.state[j];
    }
    delete(model);
    tm.status.style.display = "none";
},

// Header order drag & drop	
tableAddWidthListeners : function(client,theme,element,cid,table,pid) {
	
	var colWidths = table.colWidths;
	
	var mouseDragListener = function (e) {
			var evt = client.getEvent(e);
			evt.stop();
			element.ownerDocument.onselectstart = function(e) {return false;}
			var target = element.target;
			var td = target.parentNode;
			var offset = -(target.origX-evt.mouseX);
			var w = (target.origW+offset);
            // minimum height = scrollresizer + space for sort indicator + margin
			if (w < 19) w = 19;
			try {
				td.width = w;
                td.lastChild.style.width = (w-17)+"px";
				colWidths[cid] = w;
                table.recalc(pid,table);
			} catch (err) {
				client.debug("Failed: d&d target.style.left="+ offset+"px");
			}

	}
	
	var mouseUpListener = function(e) {
			client.removeEventListener(element.ownerDocument.body,"mousemove",mouseDragListener);
			client.removeEventListener(element.ownerDocument.body,"mouseup",arguments.callee);
			client.removeEventListener(element.ownerDocument.body,"drag",stopListener);
			var evt = client.getEvent(e);
			evt.stop();
			element.ownerDocument.onselectstart = null;
			element.dragging = false;
			return false;
	};
	
	var stopListener = function (e) {
		var evt = client.getEvent(e);
		evt.stop();
		return false;
	}
	
	client.addEventListener(element,"mousedown", function(e) {
		var evt = client.getEvent(e);
		evt.stop();
		element.dragging = true;
		element.moved = false;
		element.target = evt.target;
		evt.target.origX = evt.mouseX;
		evt.target.origW = evt.target.parentNode.offsetWidth;
		client.addEventListener(element.ownerDocument.body,"mousemove", mouseDragListener);
		client.addEventListener(element.ownerDocument.body,"mouseup", mouseUpListener);
		client.addEventListener(element.ownerDocument.body,"drag",stopListener);
	});
},

scrollTableRegisterLF : function(client,theme,paintableElement,inner,cout,hout,cin,hin) {
	client.registerLayoutFunction(paintableElement,function() {
        // TODO check this if really needed
		// var w = (inner.offsetWidth-4) +"px";
		//cout.style.width = w;
		//cin.style.width = w;
		//hout.style.width = w;
		//hin.style.width = w;
		//hout.style.width = hout.offsetParent.offsetWidth + "px";
		//div.recalc();
	});
},

scrollTableAddScrollListener : function (theme,target) {
	var hout = target.model.hout;
    var cout = target.model.cout;
	theme.client.addEventListener(cout,"scroll", function (e) {
        if (cout.scrollTimeout) {
 			clearTimeout(cout.scrollTimeout);
		}
		hout.scrollLeft = cout.scrollLeft;	
		target.scrolledLeft = cout.scrollLeft;
		var status = target.model.status;
		var d = theme.scrollTableGetFV(target);
		if ( d != target.model.state.fv && 
			(d + target.model.meta.pagelength > target.model.state.lastRendered || d < target.model.state.firstRendered)
			) {
 			status.innerHTML = d + "-" + (d+target.model.meta.pagelength-1) + " / " + target.model.meta.totalrows;
 			status.style.display = "block";
 		}
		cout.scrollTimeout = setTimeout(function () {
				cout.scrollHandler();
			},250)	
 	});
 	if(window.opera) {
 		// opera has bug: it don't fire onscroll event on mousewheel scroll
 		// hook it explicitely	
 		cout.onmousewheel = function (e) {
        if (cout.scrollTimeout) {
 			clearTimeout(cout.scrollTimeout);
		}
		hout.scrollLeft = cout.scrollLeft;	
		target.scrolledLeft = cout.scrollLeft;
		var status = target.model.status;
		var d = theme.scrollTableGetFV(target);
		if (d + target.model.meta.pagelength > target.model.state.lastRendered || d < target.model.state.firstRendered) {
 			status.innerHTML = d + "-" + (d+target.model.meta.pagelength-1) + " / " + target.model.meta.totalrows;
 			status.style.display = "block";
 		}
		cout.scrollTimeout = setTimeout(function () {
				cout.scrollHandler();
			},250)	
	 	};
 	}
},

/* Calculates first totally visible row */
scrollTableGetFV : function(target) {
    var m = target.model;
    var new_fr = Math.ceil(m.cout.scrollTop/m.rowheight) + 1;
 	if (new_fr > (m.meta.totalrows - m.meta.pagelength + 1)) new_fr=(m.meta.totalrows-m.meta.pagelength + 1); // scrolled past last page
 	if (new_fr < 1) return 1; // scrolled to befor first row
 	return new_fr;
 },
 
scrollTableAddScrollHandler : function(client,theme,target) {
    var m = target.model;
 	m.cout.scrollHandler = function () {
			var d = theme.scrollTableGetFV(target);
 			if (d != m.state.fv) {
 				// only submit if firstvisible changed
 				m.status.innerHTML = d + "-" + (d+m.meta.pagelength-1) + " / " + m.meta.totalrows + "...";
                var fvVar = theme.getVar(target, "firstvisible");
                
                // determine how many rows for chache is needed
                var reqfirstrowVar = theme.getVar(target, "reqfirstrow");
                var reqrowsVar = theme.getVar(target, "reqrows");
                // if up scroll and gone over react Threshold
                if( (d < m.state.fv) && (d - m.state.firstRendered) < m.meta.cacheReactTh) {
                    if ( (d + m.meta.cacheSize + m.meta.pagelength) < m.state.firstRendered ) {
                        // if very big scroll up, skip some rows and redraw whole tbody
                        reqfirstrowVar.value = d - m.meta.cacheSize;
                        reqrowsVar.value = m.meta.cacheSize*2 + m.meta.pagelength;
                    } else {
                        // Need some rows to top of the existing table
                        reqrowsVar.value = m.meta.cacheSize - ( d - m.state.firstRendered ) ;
                        reqfirstrowVar.value = m.state.firstRendered - reqrowsVar.value;
                    }
                    if(reqfirstrowVar.value < 1) {
                        // case 1 - firstRendered
                        // we are quite close to top, fix values to be sane
                        reqrowsVar.value = reqrowsVar.value + reqfirstrowVar.value - 1
                        reqfirstrowVar.value = 1;
                    }
                    theme.updateVar(client,reqrowsVar, false);
                    theme.updateVar(client,reqfirstrowVar, false);
                } else if ( (m.state.lastRendered - d - m.meta.pagelength ) < m.meta.cacheReactTh ) {
                    if(m.state.lastRendered < m.meta.totalrows) {
                        if (d > (m.state.lastRendered + m.meta.cacheSize) ) {
                            // A very big scroll down, skip some rows and redraw whole tbody
                            reqfirstrowVar.value = d - m.meta.cacheSize;
                            reqrowsVar.value = m.meta.cacheSize*2 + m.meta.pagelength;
                        } else {
                            // Just need more cache rows down
                            reqfirstrowVar.value = m.state.lastRendered + 1;
                            reqrowsVar.value = d + m.meta.pagelength + m.meta.cacheSize - m.state.lastRendered - 1;
                        }
                    } else {
                        // already rendered all rows, just update fv to server
                        reqfirstrowVar.value = m.state.lastRendered;
                        reqrowsVar.value = 0;
                    }
                    theme.updateVar(client,reqfirstrowVar, false);
                    theme.updateVar(client,reqrowsVar, false);
                } else {
                    // scroll was so small that don't bother to get any new rows
                    console.info("Small scroll withing reactTh, no request made.")
                    m.status.style.display = "none";
                    return;
                }
                
 				// always immediate to update first visible to server
                fvVar.value = d;
                theme.updateVar(client,fvVar, true);
 			} else {
 				m.status.style.display = "none";
 			}
 	};
},

scrollTableRecalc : function(pid,target) {
	var div = target.ownerDocument.getElementById(pid);
	var colWidths = div.colWidths;
	if (!colWidths) {
		colWidths = new Object();
		div.colWidths = colWidths;
	}
	var hout = target.ownerDocument.getElementById(pid+"hout");
    var cout = target.ownerDocument.getElementById(pid+"cout");
 	var hin = target.ownerDocument.getElementById(pid+"hin");
    var cin = target.ownerDocument.getElementById(pid+"cin");
    var h = hin.getElementsByTagName("td");
    var c = cin.getElementsByTagName("td");
    var m = div.model;
    

    if(!colWidths[h[0].getAttribute("cid")]) {
        // this is the initial calculation, we'll sync header and column depending on which is wider
        // loop headers and columns natural widths, browser may squeeze them to fit whole table
        var defPad = 10;
        for (var i = 0;i<h.length;i++) {
        	if(c && c[i])
	            colWidths[h[i].getAttribute("cid")] = parseInt((h[i].lastChild.clientWidth > c[i].clientWidth) ? (h[i].clientWidth) : c[i].clientWidth) + defPad;
            else
            	colWidths[h[i].getAttribute("cid")] = parseInt(h[i].clientWidth) + defPad;
        }
    }
    for (var i = 0;i< h.length ;i++) {
    	var cell = h[i];
        var cid = cell.getAttribute("cid");
        var w = colWidths[cid] || cell.offsetWidth;
		cell.style.width = w + "px";
        // et div.headerContents width to w - COL_RESIZER_WIDTH - margin - 10px extra for possible sort indicator
        // now text doesn't overlap resizer & sort indicator
        cell.lastChild.style.width = (w - 17)+"px";
        // enter looping rows only if width is changed
        if(c[i] && c[i].offsetWidth != w ) {
            var rows = c.length/h.length;
            for (var j=0;j<rows;j++) {
                var idx = j*h.length+i;
                if (c[idx]) {
                    // workaround for IE overflow bug, set width explicitely for container div
                    // w - (borderwidth + margin/padding)
                    c[idx].firstChild.style.width = (w-4)+"px";
                    c[idx].width = w;
                }
            }
        }
    }
},

// Header order drag & drop	
addToDragOrderGroup : function (client,theme,element,group,variable,sortVar,sortascVar,sortasc) {
	element.dragGroup = group;
	if (!group.elements) {
		group.elements = new Array();
	}
	var idx = group.elements.length;
	group.elements[idx] = element;
	
	var mouseDragListener = function (e) {
			var evt = client.getEvent(e);
			evt.stop();
			element.ownerDocument.onselectstart = function() {return false;}
			var target = element.target;
			target.style.position = "relative";
			target.style.top = "5px";
			try {
				target.style.left = -(target.origX-evt.mouseX+10)+"px";
			} catch (err) {
				client.error("Failed: d&d target.style.left="+ (-(target.origX-evt.mouseX+10)+"px"));
			}
			var dragGroup = element.dragGroup;
			dragGroup.moved = true;
			var els = dragGroup.elements;
			for (var i=0;i<els.length;i++) {
				if (i==element.idx) continue;
				var el = els[i];
				var p = client.getElementPosition(el);
				if (i!=dragGroup.origIdx&&i-1!=dragGroup.origIdx&&p.x < evt.mouseX && p.x+p.w/2 > evt.mouseX) {
						dragGroup.targetIdx = i; 
						el.style.borderLeft = "1px solid black";
						el.style.borderRight = "";
						break;
				} else if (i!=dragGroup.origIdx&&i+1!=dragGroup.origIdx && p.x+p.w/2 < evt.mouseX && p.x+p.w > evt.mouseX) {
						dragGroup.targetIdx = i+1;
						el.style.borderRight = "1px solid black";
						el.style.borderLeft = "";
						break;
				} else {
					dragGroup.targetIdx = dragGroup.origIdx;
					el.style.borderRight = "";
					el.style.borderLeft = "";
				}	
			}
	}
	
	var mouseUpListener = function(e) {
			client.removeEventListener(element.ownerDocument.body,"mousemove",mouseDragListener);
			client.removeEventListener(element.ownerDocument.body,"mouseup",arguments.callee);
			var evt = client.getEvent(e);
			evt.stop();
			element.ownerDocument.onselectstart = null;
			element.target.style.background = "";
			element.dragGroup.dragging = false;
			if (element.dragGroup.dragTM) {
				clearTimeout(element.dragGroup.dragTM);
			}
			if (!element.dragGroup.moved) {
				if (sortVar) {
					var cid = element.getAttribute("cid");
					var sorted = element.getAttribute("sorted");
					//alert("sorting "+element.getAttribute("cid") + " " + sorted + " " + sortasc);
					if (sorted) {
						theme.setVariable(client, sortascVar, !sortasc, true);
					} else {
						theme.setVariable(client, sortVar, cid, true);
					}
				}
			}
			var origIdx = element.dragGroup.origIdx;
			var targetIdx = element.dragGroup.targetIdx;
			if (origIdx != targetIdx) {
				var els = element.dragGroup.elements;
				var neworder = new Array();
				for (var i=0;i<els.length;i++) {
					if (i==origIdx) continue;
					if (i==targetIdx) {
						neworder[neworder.length] = els[origIdx].getAttribute("cid");
					} 
					neworder[neworder.length] = els[i].getAttribute("cid");
				}
				theme.setVariable(client, variable, neworder, true);
			} else {
				element.target.style.left = "0px";
				element.target.style.top = "0px";
			}
	};
	
	client.addEventListener(element,"mousedown", function(e) {
		var evt = client.getEvent(e);
		evt.stop();
		element.dragGroup.dragging = true;
		element.dragGroup.moved = false;
		element.dragGroup.origIdx = idx;
		element.dragGroup.targetIdx = idx;
		element.target = evt.target;
		evt.target.dragGroup = element.dragGroup;
		evt.target.origX = evt.mouseX;
		evt.target.idx = idx;
		if (element.dragGroup.dragTM) {
			clearTimeout(element.dragGroup.dragTM);
		}
		client.addEventListener(element.ownerDocument.body,"mouseup", mouseUpListener);
		if (variable) {
			// column reordering allowed
			group.dragTM = setTimeout(function () {
				if(element.dragGroup.dragging) {
					evt.target.style.background = "white";
					client.addEventListener(element.ownerDocument.body,"mousemove",mouseDragListener);				
				}
			},100);
		}
	});
	//client.addEventListener(element,"mouseup", mouseUpListener);
},

/**
 * This is an event listener that builds context menu for row when
 * context clicked
 */
tableRowShowContextMenu : function(e) {
	var client = itmill.clients[0];
	var evt = client.getEvent(e);
	if(evt.rightclick || evt.type == "contextmenu") {
		// stop bubling
		evt.stop();
		// get TR element ( event may be bubbling from contained elements)
		var row = false;
		var tmp = evt.target;
		while(!row && tmp) {
			if(tmp.actionList)
				row = tmp;
			else
				tmp = tmp.parentNode;
		}
		if(!row) {
			console.error("Couldn't find row element for which to show context menu");
			return false;
		}
		var pntbl = client.getPaintable(row);
		var actions = new Array();
		// actionValue string is sent to server on contextMenu click
		// They comma separated like this: "[listitem],[actionKey]"
		for(var i = 0; i < row.actionList.length; i++) {
			actions.push({
				caption: pntbl.model.meta.actions[row.actionList[i]],
				actionValue: (row.key + "," + row.actionList[i])
			});
		}
		var cm = itmill.clients[0].getContextMenu();
		cm.showContextMenu(actions,evt,pntbl.varMap.action);
		return false;
	}
},

/**
 * This is handler that creates "context menu" for choosing
 * visible columns for table
 */
 tableShowColumnSelectMenu : function(e) {
 	var client = itmill.clients[0];
 	var evt = client.getEvent(e);
 	// stop bubbling
 	evt.stop();
 	var cm = client.getContextMenu();
 	var pntbl = client.getPaintable(evt.target);
 	var vcol = pntbl.model.visiblecols; // columnorder uidl snippet
 	var aOpt = new Array();
 	// loop all arrays and create appropriate values to be sent to
	// server on menu click
 	for(var i = 0; i < vcol.childNodes.length; i++) {
 		var col = vcol.childNodes[i];
 		var collapsed = "true" == col.getAttribute("collapsed");
 		// copy array that has collapsed columns
 		var tmp = pntbl.varMap.collapsedcolumns.value.slice();
 		if(collapsed)
 			tmp.splice(tmp.indexOf(col.getAttribute("cid")), 1);
 		else
 			tmp.push(col.getAttribute("cid"));
		aOpt.push({
			caption: col.getAttribute("caption"),
			actionValue: tmp.join(","),
			checked: !collapsed
		});
 	}
 	cm.showContextMenu(aOpt,evt, pntbl.varMap.collapsedcolumns);
 },

renderSelect : function(renderer,uidl,target,layoutInfo) {

	var theme = renderer.theme;
	var client = renderer.client;	
	var options = theme.getFirstElement(uidl,"options");
			
	// Filtering lazy loading select mode
	var loadfrom = options != null ? options.getAttribute("loadfrom") : "";
	if (loadfrom != null && loadfrom.length > 0) {
		new itmill.themes.Base.FilterSelect(renderer,uidl,target,layoutInfo);
		return;
	}
			
	// Create containing element
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);	
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

	// Create selection variable
	var selectMode = uidl.getAttribute("selectmode");
	var selectable = selectMode == "multi" || selectMode == "single";
	var immediate = ("true" == uidl.getAttribute("immediate"));
	var disabled = ("true" == uidl.getAttribute("disabled"));
	var readonly = ("true" == uidl.getAttribute("readonly"));
	var newitem = ("true" == uidl.getAttribute("allownewitem"));
	var focusid = uidl.getAttribute("focusid");
	var tabindex = uidl.getAttribute("tabindex");
	
	var selectionVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","selected"));

	// Render default header
	theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);

	// Create select input	
	var select = theme.createElementTo(div,"select");
	if (focusid) select.focusid = focusid;
	if (tabindex) select.tabIndex = tabindex;
	if (selectMode == "multi") {
		select.setAttribute("multiple", "true");
		if (newitem) {
			theme.createElementTo(div,"br");
		} 
	} else {
		if (newitem) {
			theme.addCSSClass(div,"nobr");
		} 
	}
	if (options != null) {
		options = options.getElementsByTagName("so");
		if (options && options.length && selectMode == "multi") {
			select.size = (options.length>7?7:options.length);
		}
	}	
	if (disabled||readonly) {
		select.disabled = "true";
	} else {
		// Add change listener
		theme.addSetVarListener(theme,client,select,"change",selectionVariable,select,immediate);
	}
	// Empty selection for WA compatibility
	var nullOptionNode = theme.createElementTo(select,"option");
	theme.createTextNodeTo(nullOptionNode,"-");
	
	// Selected options
	if (options != null && options.length >0) {
		for (var i=0; i<options.length;i++) {
            var optionNode = new Option(
                options[i].getAttribute("caption"),
                options[i].getAttribute("key")                
            );
            select.options[select.options.length] = optionNode;
			if (options[i].getAttribute("selected") == "true") {
				optionNode.selected = true;
                // IE bug workaraund to preserve selection in multiselect
                if(document.all) {
                    window.scrollBy(0,0);
                }
			}
			if (options[i].getAttribute("nullselection") == "true") {
				try {select.removeChild(nullOptionNode);} catch (e) {alert(e);}
			}
		}
	}
	
	if (newitem) {
		var input = theme.createInputElementTo(div,"text");
		var button = theme.createElementTo(div,"button");
		theme.createTextNodeTo(button,"+");
		var newitemVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"string","newitem"));
		theme.addSetVarListener(theme,client,input,"change",newitemVariable,input,true);
	}
},

renderSelectTwincol : function(renderer,uidl,target,layoutInfo) {
    function deleteOptionFromSelectByOptionValue(select, value) {
        for(var i = 0; i < select.options.length; i++) {
            if(select.options[i].value == value) {
                select.options[i] = null;
                return true;
            }
        }
    }
    function setSelectedOfOptionFromSelectByOptionValue(select, value, sel) {
        for(var i = 0; i < select.options.length; i++) {
            if(select.options[i].value == value) {
                select.options[i].selected = sel;
                return true;
            }
        }
    }
	var theme = renderer.theme;
	var client = renderer.client;
	
	// Create containing element
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);	
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

	// Create selection variable
	var selectMode = uidl.getAttribute("selectmode");
	var selectable = selectMode == "multi" || selectMode == "single";
	var immediate = ("true" == uidl.getAttribute("immediate"));
	var disabled = ("true" == uidl.getAttribute("disabled"));
	var readonly = ("true" == uidl.getAttribute("readonly"));
	var newitem = ("true" == uidl.getAttribute("allownewitem"));
	var focusid = uidl.getAttribute("focusid");
	var tabindex = uidl.getAttribute("tabindex");
	
	var selectionVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","selected"));

	// Render default header
	theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);

    if (newitem) {
        theme.createElementTo(div,"br");
    }
    // create two selects
    var unselected = theme.createElementTo(div,"select");
    unselected.setAttribute("multiple","true");
    unselected.className = "unselected";
    var selected = theme.createElementTo(div,"select");
    selected.setAttribute("multiple","true");
    selected.className = "selected";
    // buttons to move selections
    var buttonsDiv = theme.createElementTo(div,"div");
    buttonsDiv.className = "buttons";

    //set focus and tabindex to unselected select    
	if (focusid) unselected.focusid = focusid;
	if (tabindex) unselected.tabIndex = tabindex;

	var options = theme.getFirstElement(uidl,"options");
	if (options != null) {
		options = options.getElementsByTagName("so");
		selected.size = unselected.size = (options.length>7?7:options.length);
	}
	
	// Limit size to be at least 3 selects so that component don't look
	// weird with empty selections
	if(selected.size < 3 )
		selected.size = unselected.size = 3;
    
	// Select options
	if (options != null && options.length >0) {
        for (var i=0; i<options.length;i++) {
            var modelOptionNode;
            if (options[i].getAttribute("selected") == "true") {
                modelOptionNode = theme.createElementTo(selected,"option");
            } else {
                modelOptionNode = theme.createElementTo(unselected,"option");
            }
            theme.createTextNodeTo(modelOptionNode,options[i].getAttribute("caption"));
            modelOptionNode.setAttribute("value", options[i].getAttribute("key"));
        }
	}
	
	if (newitem) {
		var input = theme.createInputElementTo(div,"text");
		var button = theme.createElementTo(div,"button");
		theme.createTextNodeTo(button,"+");
		var newitemVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"string","newitem"));
		theme.addSetVarListener(theme,client,input,"change",newitemVariable,input,true);
	}
    var moveRightButton = theme.createElementTo(buttonsDiv, "button");
    moveRightButton.innerHTML = "&gt;&gt;";
    moveRightButton.onclick = function() {
        // loop all selected options unselected-list
        while(unselected.selectedIndex > -1) {
            var option = unselected.options[unselected.selectedIndex];
            //add selected options to end of selected-list
            selected.options[selected.options.length] = 
                new Option(
                    option.text,
                    option.value                  
                    );
            // remove from unselected-list
            deleteOptionFromSelectByOptionValue(unselected,option.value)
        }
        if(selectMode != "multi") {
            // in single select mode, ensure only one option on right side
            // not ment for single select mode, just to be more compatible
            while(selected.options.length > 1) {
                var optionToLeft = selected.options[0];
                unselected.options[unselected.options.length] = 
                    new Option(
                        optionToLeft.text,
                        optionToLeft.value                  
                );
                selected.options[0] = null;
                // remove from selected-list
                deleteOptionFromSelectByOptionValue(selected,optionToLeft.value)
            }
        } // end checking for single variable
        // fire variable change
        var s = new Array();
		for (var i = 0; i < selected.options.length; i++) {
			s[s.length] = selected.options[i].value;
		}
		var value = s.join(',');		
        theme.setVariable(client,selectionVariable,value,immediate);
    }
    var moveLeftButton = theme.createElementTo(buttonsDiv, "button");
    moveLeftButton.innerHTML = "&lt;&lt;";
    moveLeftButton.onclick = function() {
        // loop all selected options selected-list
        while(selected.selectedIndex > -1) {
            var option = selected.options[selected.selectedIndex];
            //add selected options to end of selected-list
            unselected.options[unselected.options.length] = 
                new Option(
                    option.text,
                    option.value                  
                    );
            // remove from unselected-list
            deleteOptionFromSelectByOptionValue(selected,option.value)
        }
        // fire variable change
        var s = new Array();
		for (var i = 0; i < selected.options.length; i++) {
			s[s.length] = selected.options[i].value;
		}
		var value = s.join(',');		
        theme.setVariable(client,selectionVariable,value,immediate);
    }
    if (disabled||readonly) {
        selected.disabled = "true";
        unselected.disabled = "true";
        moveLeftButton.disabled = "true";
        moveRightButton.disabled = "true";
    }
    
},

renderSelectOptionGroup : function(renderer,uidl,target,layoutInfo) {
				
	var theme = renderer.theme;
	var client = renderer.client;
	
	// Create containing element
	var pntbl = theme.createPaintableElement(renderer,uidl,target);	
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
	
	var selectMode = pntbl.selectMode = uidl.getAttribute("selectmode");
	var selectable = selectMode == "multi" || selectMode == "single";
	pntbl.immediate = ("true" == uidl.getAttribute("immediate"));
	var disabled = ("true" == uidl.getAttribute("disabled"));
	var readonly = ("true" == uidl.getAttribute("readonly"));
	var newitem = ("true" == uidl.getAttribute("allownewitem"));
	
	// Render default header
	theme.renderDefaultComponentHeader(renderer,uidl,pntbl,layoutInfo);

	var selVar = theme.createVarFromUidl(pntbl, theme.getVariableElement(uidl,"array","selected"));

	var select = theme.createElementTo(pntbl,"div");
	var options = theme.getFirstElement(uidl,"options");
	if (options != null) {
		options = options.getElementsByTagName("so");
	}	
	
	var inputName = "input"+uidl.getAttribute("id");
	
	if (options != null && options.length > 0) {
		for (var i=0; i<options.length;i++) {
			var optionUidl = options[i];
			var iconUrl = optionUidl.getAttribute("icon");
			var div = theme.createElementTo(select,"div", "nobr");
			var key = optionUidl.getAttribute("key");
			
			// Create input
			var inputId = inputName+i;
			var caption =  optionUidl.getAttribute("caption");
			var html;
			if (selectMode == "multi") {
				html = "<input class=\"option\" type=checkbox name=\""+inputName+"\" id=\""+inputId+"\" ";
			} else {	
				html = "<input class=\"option\" type=radio name=\""+inputName+"\" id=\""+inputId+"\" ";			
			}
			if (disabled||readonly) html += " disabled=\"true\" "
			if (optionUidl.getAttribute("selected") == "true") {
				html += " checked=\"true\" "
			}
			html += ' value="' + key + '"';
			html += " ><label class=\"clickable\" for=\""+inputId+"\">";
			if (caption) html += caption;
			if (iconUrl) {
				if (iconUrl.indexOf("theme://") == 0) {
	    			iconUrl = (theme.iconRoot != null ? theme.iconRoot : theme.root) 
	    					+ iconUrl.substring(8);
	    		}
	    		html += "<img src=\""+iconUrl+"\" class=\"icon\">";
			}				
			html += "</label>";
			
			div.innerHTML = html;
			if (!(disabled||readonly)) {
				div.firstChild.onchange = theme._optionGroupValueChange;
			}
		}
	}
	if (newitem) {
		var ni = theme.createElementTo(pntbl,"div","newitem");
		var input = theme.createInputElementTo(ni,"text");
		var button = theme.createElementTo(ni,"button");
		theme.createTextNodeTo(button,"+");
		var newitemVariable = theme.createVariableElementTo(ni,theme.getVariableElement(uidl,"string","newitem"));
		theme.addSetVarListener(theme,client,input,"change",newitemVariable,input,true);
	}
},
/**
 * Event listener for radio or checkbox value change.
 * 
 * Updates variable to client.
 */
_optionGroupValueChange : function(e) {
	var evt = itmill.lib.getEvent(e);
	var pntbl = itmill.lib.getPaintable(this);
	var input = this;
	

	var selVar = pntbl.varMap.selected;
	if(pntbl.selectMode == "multi") {
		if( input.checked == true ) {
			// add value to selected list
			selVar.value.push(input.value);
		} else {
			// deselecting item
			var index = selVar.value.indexOf(input.value);
			if(index > -1) {
				selVar.value.splice(index,1);
			}
		}
		pntbl.client.changeVariable(selVar.id, selVar.value.join(','), pntbl.immediate)
	} else {
		pntbl.varMap.selected.value = input.value;
		pntbl.client.changeVariable(pntbl.varMap.selected.id, pntbl.varMap.selected.value, pntbl.immediate)
		// force checked
	}
	// force focus in case of label click, make keyboard navigation easier
	input.focus();
},

renderLabel : function(renderer,uidl,target,layoutInfo) {
			
			// Create container element
			var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
			if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

			// Create default header
			var caption = renderer.theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);

			// Render children to div
			if (uidl.childNodes.length>0) {
				div = renderer.theme.createElementTo(div,"div");
				renderer.theme.renderChildNodes(renderer, uidl, div);
			}
			if (div.innerHTML == "") div.innerHTML = "&nbsp;";
},

renderData : function(renderer,uidl,target) {

	var html = "";
	for (var i=0; i<uidl.childNodes.length; i++) {
		var child = uidl.childNodes.item(i);
		if (child.nodeType == Node.ELEMENT_NODE) {
			html += renderer.theme.nodeToString(child,true);
		} if ((child.nodeType == Node.TEXT_NODE || child.nodeType == Node.CDATA_SECTION_NODE) && child.data != null) {
			html += child.data;
		}
	}
	target.innerHTML = html;
				
},

renderPre : function(renderer,uidl,target) {

	// Create pre node
	var pre = renderer.theme.createElementTo(target,"pre");
	
	var html = "";
	for (var i=0; i<uidl.childNodes.length; i++) {
		var child = uidl.childNodes.item(i);
		if (child.nodeType == Node.ELEMENT_NODE) {
			html += renderer.theme.nodeToString(child,true);
		} if (child.nodeType == Node.TEXT_NODE && child.data != null) {
			html += child.data;
		}
	}
	pre.innerHTML = html;				
},


renderButton : function(renderer,uidl,target,layoutInfo) {
	// Branch for checkbox
	
	if (uidl.getAttribute("type") == "switch") {
		return renderer.theme.renderCheckBox(renderer,uidl,target,layoutInfo);
	}
	
	// Shortcuts
	var theme = renderer.theme;
	var client = renderer.client;
	
	var disabled = "true"==uidl.getAttribute("disabled");
	var readonly = "true"==uidl.getAttribute("readonly");
	var immediate = "true"==uidl.getAttribute("immediate");
	var tabindex = uidl.getAttribute("tabindex");
	
	var linkStyle = "link"==uidl.getAttribute("style");
	
	var pntbl = theme.createPaintableElement(renderer,uidl,target,layoutInfo);
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
	
	div = renderer.theme.createElementTo(pntbl,"div",(linkStyle?"link clickable":"outset clickable"));
	var outer = renderer.theme.createElementTo(div,"div",(linkStyle?"":"outer"));
	var inner = renderer.theme.createElementTo(outer,"div",(linkStyle?"pad":"border pad bg"));
	
	var caption = theme.renderDefaultComponentHeader(renderer,uidl,inner);
	var hiddenInput = theme.addTabtoHandlers(client,theme,caption,div,tabindex,("default"==uidl.getAttribute("style")));
	
	if (!disabled&&!readonly) {
		pntbl.focusableField = hiddenInput;
		pntbl._onfocus = theme._onFieldFocus;
	    // Handlers
		theme.createVarFromUidl(pntbl, theme.getVariableElement(uidl,"boolean", "state"));
		
        this.addEventListener(div,"click",theme._buttonClickListener);
		
		theme.addAddClassListener(theme,client,div,"mousedown","down",div);
		theme.addRemoveClassListener(theme,client,div,"mouseup","down",div);
		theme.addRemoveClassListener(theme,client,div,"mouseout","down",div);
		
		theme.addAddClassListener(theme,client,div,"mouseover","over",div);
		theme.addRemoveClassListener(theme,client,div,"mouseout","over",div);
		
		theme.addPreventSelectionListener(theme,client,div);
		
		// TODO clean
		if(theme.getFirstElement(uidl,"actions")) {
			var actions = theme.getFirstElement(uidl, "actions");
			theme.createVarFromUidl(pntbl, theme.getVariableElement(actions,"string", "action"));
			var aNodes = actions.getElementsByTagName("action");
			for(var i = 0; i < aNodes.length; i++) {
				var node = aNodes[i];
				var modCount = node.getAttribute("modifiers");
				if(modCount > 0) {
					for(var j = 0; j < modCount; j++) {
						var modifier = parseInt(node.getAttribute("modifier" + j));
						switch(modifier) {
							case 17: // ctrl
								var ctrl = true;
								break;
							case 18: // alt
								var alt = true;
								break;
							case 16:
								var shift = true;
								break;
							default:
								break;
						}
					}
				}
				var sc = new itmill.ui.Shortcut(
					pntbl,
					theme._buttonShortcutKeyListener,
					parseInt(node.getAttribute("keycode")),
					ctrl,
					alt,
					shift);
				client.addShortcutHandler(sc);
			}
		}
	}
},
_buttonClickListener : function(e) {
	var evt = itmill.lib.getEvent(e);
	var pntbl = itmill.lib.getPaintable(evt.target);
	pntbl.focusableField.focus();
	pntbl.client.changeVariable(pntbl.varMap.state.id, "true", true);
},

_buttonShortcutKeyListener : function(keycode, modifiers) {
	// this should be called on button paintable and send data to server
	// TODO convert to use action variable, now uses state (button click)
	this.focusableField.focus();
	this.client.changeVariable(this.varMap.action.id, "1,1", true);
},

renderCheckBox : function(renderer,uidl,target,layoutInfo) {
		// Shortcuts
		var theme = renderer.theme;
		var client = renderer.client;
		
		var div = theme.createPaintableElement(renderer,uidl,target,layoutInfo);
		if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

		var immediate = (uidl.getAttribute("immediate") == "true");
		var disabled = (uidl.getAttribute("disabled") == "true");
		var readonly = (uidl.getAttribute("readonly") == "true");
		var tabindex = uidl.getAttribute("tabindex");
		
		// Create input
		var div = theme.createElementTo(div,"div","nocappad nobr");
		var input = theme.createInputElementTo(div,"checkbox");
		input.setAttribute("id", "input"+uidl.getAttribute("id"));
		if (tabindex) input.tabIndex = tabindex;
		if (disabled||readonly) {
			input.disabled = "true";
		}
		
		// Create label
		var label = theme.createElementTo(div,"label", "clickable");
		var cap = theme.renderDefaultComponentHeader(renderer,uidl,label);
		theme.addCSSClass(cap,"inline");
		label.setAttribute("for","input"+uidl.getAttribute("id"));
		// Value
		var v = theme.getVariableElement(uidl,"boolean", "state");
		if ( v!= null) {
			var varId = v.getAttribute("id");
			input.checked = (v.getAttribute("value") == "true");			
			// Attach listener
			theme.addSetVarListener(theme,client,input,(immediate?"click":"change"),varId,input,immediate);
		}
},

///////
/* TODO merge or delete the rest

/**
 *   Render tree as a menubar.
 *   NOTE:
 *   First level nodes are not selectable - menu opens with click. 
 *   If style == "coolmenu", immediate is forced.
 *  
 */

renderTreeMenu : function(renderer,uidl,target,layoutInfo) {
			
	var theme = renderer.theme;
	
	// Create container element
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

	// Get tree attributes
	var style = uidl.getAttribute("style");
	var immediate = ("true" == uidl.getAttribute("immediate")||style=="coolmenu");
	var disabled = ("true" == uidl.getAttribute("disabled"));
	var readonly = ("true" == uidl.getAttribute("readonly"));
	var selectMode = uidl.getAttribute("selectmode");
	var selectable = selectMode == "multi" || selectMode == "single";
	var selected;
	if (selectable) {
		selected = new Object();
	}
	var selectionVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","selected"));
	var expandVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","expand"));
	var collapseVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","collapse"));

	var actions = null;
	var actionVar = null;
	var alNode = theme.getFirstElement(uidl,"actions")
	if (alNode) {
		actionVar = theme.createVariableElementTo(div,theme.getVariableElement(alNode,"string","action"));
		actions = new Object();
		var ak = alNode.getElementsByTagName("action");
		for (var i=0;i<ak.length;i++) {
			actions[ak[i].getAttribute("key")] = ak[i].getAttribute("caption");
		}
	}
	delete alNode;

	// Create default header
	var caption = renderer.theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);

	// Content DIV
	var content = theme.createElementTo(div,"div","content menu"); 
	
	// Iterate all nodes
	for (var i = 0; i< uidl.childNodes.length;i++) {
		var node = uidl.childNodes[i];
		if (node.nodeName == "node" || node.nodeName == "leaf") {
			theme.renderTreeMenuNode(renderer,node,content,selectable,selectMode,selected,selectionVariable,expandVariable,collapseVariable,actions,actionVar,immediate,disabled,readonly,0);
		} 	
	}
},

renderTreeMenuNode : function(renderer,node,target,selectable,selectMode,selected,selectionVariable,expandVariable,collapseVariable,actions,actionVar,immediate,disabled,readonly,level) {

	var theme = renderer.theme;
	var client = renderer.client;

	var n = theme.createElementTo(target,"div",(level==0?"inline clickable":"clickable"));
	
	
	// Caption
	var cap = theme.createElementTo(n,"div","inline caption pad");
	theme.createTextNodeTo(cap,node.getAttribute("caption"));	

	// Expand/collapse/spacer button
	var img = theme.createElementTo(n,"img","icon");
    img.align = "absbottom";
	var key = node.getAttribute("key");	
	var icon = node.getAttribute("icon");
    if (icon) {
        var iconurl = theme.root+icon.split("theme:")[1];
        var iimg = theme.createElementTo(n,"img","icon");
	    iimg.src = iconurl;
    }


	// Hover effects
	if (!disabled&&!readonly) {
		theme.addAddClassListener(theme,client,n,"mouseover","selected",n);
		theme.addRemoveClassListener(theme,client,n,"mouseout","selected",n);
	}
	
	// Server-side selection
	if (selectable && node.getAttribute("selected") == "true") {
		theme.addCSSClass(n,"selected");
		selected[key] = n;
	}

	// Indicate selection	
	if (theme.listContainsInt(selectionVariable.value,key)) {
		theme.addCSSClass(n, "selected");
	}

	// Selection listeners
	if (selectable && !disabled && (level != 0 || node.nodeName == "leaf")) {
		if (!readonly) {		
			if (selectMode == "single") {
				theme.addAddClassListener(theme,client,n,"click","selected",n,selected);
				theme.addSetVarListener(theme,client,n,"click",selectionVariable,key,immediate);
			
			} else if (selectMode == "multi") {	
				theme.addToggleClassListener(theme,client,n,"click","selected");
				theme.addToggleVarListener(theme,client,n,"click",selectionVariable,key,immediate);
			
			}
		}
	} 
	
	// Actions
	if (!disabled && !readonly) {
		for (var i = 0; i< node.childNodes.length;i++) {
			var childNode = node.childNodes[i];
			if (childNode.nodeName == "al" ) {
				theme.renderActionPopup(renderer,childNode,n,actions,actionVar,key,1); // TODO check
			} 
		}	
	}
	
	// Render all sub-nodes
	if (node.nodeName == "node") {
		var subnodes = theme.createElementTo(target,"div","hide popup");
        var inner = theme.createElementTo(subnodes,"div","border");
        theme.addTogglePopupListener(theme,client,n,(level==0?"click":"mouseover"),subnodes,0,null,n);
        //theme.addToggleClassListener(theme,client,n,(level==0?"click":"mouseover"),"hide",subnodes)
		if (node.childNodes != null && node.childNodes.length >0) {
			img.src = theme.root + "img/tree/empty.gif";
			img.expanded = "true";
		} else {
			img.src = theme.root + "img/tree/empty.gif";
			img.expanded = "false";
		}
		for (var i = 0; i< node.childNodes.length;i++) {
			var childNode = node.childNodes[i];
			if (childNode.nodeName == "node" || childNode.nodeName == "leaf") {
				theme.renderTreeMenuNode(renderer,childNode,inner,selectable,selectMode,selected,selectionVariable,expandVariable,collapseVariable,actions,actionVar,immediate,disabled,readonly,level+1);
			} 
		}	
		
		// Add event listener
		if (!disabled&&level!=0) {
			var target = (selectable&&!readonly?img:n);
			theme.addToggleClassListener(theme,client,target,"mouseup","hidden",subnodes);
			theme.addExpandNodeListener(theme,client,img,"mouseup",subnodes,expandVariable,collapseVariable,key,immediate,target);
			theme.addStopListener(theme,client,target,"mouseup");
			theme.addStopListener(theme,client,target,"click");
		}
		
	} else {
			img.src = theme.root + "img/tree/empty.gif";			
	}
},

/**
* 5.6.2006 - Jouni Koivuviita
* New innerHTML components
* RENAMED for testing both - marc
*/

renderNewPanel : function(renderer,uidl,target,layoutInfo) {
    // Shortcuts
    var theme = renderer.theme;
	var style = uidl.getAttribute("style");
	// Create component element
	var div = theme.createPaintableElement(renderer,uidl,target); 
    
	/* New panel theme, 8.6.2006 - Jouni Koivuviita */
	div.innerHTML = "<div class=\"top\"><div class=\"right\"></div><div class=\"left\"><div class=\"title\"></div></div></div><div class=\"middle\"></div><div class=\"bottom\"><div class=\"right\"></div><div class=\"left\"></div></div>";
	var cap = div.firstChild.firstChild.nextSibling.firstChild;
	var content = div.childNodes[1];
	theme.applyWidthAndHeight(uidl,div.childNodes[1],"height");
	theme.applyWidthAndHeight(uidl,div,"width");
	
	/*	
    div.innerHTML = "<TABLE width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><TR height=\"35\"><TD width=\"12\"><IMG onload=\"png(this);\" src=\""+theme.root+"img/top-left.png\"></TD><TD style=\"background: url('"+theme.root+"img/top.png') !important;background: none;background-position:right;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/top.png', sizingMethod='scale');\"></TD><TD  width=\"12\"><IMG onload=\"png(this);\" src=\""+theme.root+"img/top-right.png\"></TD></TR><TR><TD style=\"background: url('"+theme.root+"img/left.png') !important;background: none;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/left.png', sizingMethod='scale');\"></TD><TD bgcolor=white></TD><TD style=\"background: url('"+theme.root+"img/right.png') !important;background: none;background-position:right;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/right.png', sizingMethod='scale');\"></TD></TR><TR height=\"12\"><TD><IMG onload=\"png(this);\" src=\""+theme.root+"img/bottom-left.png\"></TD><TD style=\"background: url('"+theme.root+"img/bottom.png') !important;background: none;background-position:right;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/bottom.png', sizingMethod='scale');\"></TD><TD><IMG onload=\"png(this);\" src=\""+theme.root+"img/bottom-right.png\"></TD></TR></TABLE>";
    var cap = div.firstChild.firstChild.firstChild.childNodes[1];
    var content = div.firstChild.firstChild.childNodes[1].childNodes[1];
	*/
    
    theme.renderDefaultComponentHeader(renderer,uidl,cap);
    theme.renderChildNodes(renderer, uidl, content);
},

renderNewPanelModal : function(renderer,uidl,target,layoutInfo,alignment) {
    // Shortcuts
    var theme = renderer.theme;
    //var parentTheme = theme.parent;
	// Create component element
	var div = theme.createPaintableElement(renderer,uidl,target); 
    var html = "<IFRAME frameborder=\"0\" style=\"border:none;z-index:9997;position:absolute;top:0px;left:0px;width:100%;height:100%;background-color:white;filter: alpha(opacity=80);opacity:0.8;\"></IFRAME>";
    html += "<DIV align=\"center\" style=\"position:absolute;top:0px;width:100%;left:0px;z-index:9999;filter: alpha(opacity=100);opacity:1;\"><TABLE  cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><TR height=\"35\"><TD width=\"12\"><IMG onload=\"png(this);\" src=\""+theme.root+"img/top-left.png\"></TD><TD style=\"background: url('"+theme.root+"img/top.png') !important;background: none;background-position:right;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/top.png', sizingMethod='scale');\"></TD><TD  width=\"12\"><IMG onload=\"png(this);\" src=\""+theme.root+"img/top-right.png\"></TD></TR><TR><TD style=\"background: url('"+theme.root+"img/left.png') !important;background: none;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/left.png', sizingMethod='scale');\"></TD><TD bgcolor=white ></TD><TD style=\"background: url('"+theme.root+"img/right.png') !important;background: none;background-position:right;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/right.png', sizingMethod='scale');\"></TD></TR><TR height=\"12\"><TD><IMG onload=\"png(this);\" src=\""+theme.root+"img/bottom-left.png\"></TD><TD style=\"background: url('"+theme.root+"img/bottom.png') !important;background: none;background-position:right;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/bottom.png', sizingMethod='scale');\"></TD><TD><IMG onload=\"png(this);\" src=\""+theme.root+"img/bottom-right.png\"></TD></TR></TABLE><DIV>";
    div.innerHTML = html;
    var overlay = div.firstChild;
    overlay.style.width = div.ownerDocument.body.offsetWidth + "px";
    overlay.style.height = div.ownerDocument.body.offsetHeight + "px";
    var table = div.childNodes[1].firstChild;
    var cap = table.firstChild.firstChild.childNodes[1];
    var content = table.firstChild.childNodes[1].childNodes[1];
    
    theme.renderDefaultComponentHeader(renderer,uidl,cap);
    theme.renderChildNodes(renderer, uidl, content);
   
   	var ifrdiv = theme.createElementTo(div,"div");
   
   html = "<IFRAME frameborder=\"0\" style=\"border:none;z-index:9998;position:absolute;top:"+(div.childNodes[1].offsetTop+5)+"px;left:"+(table.offsetLeft+5)+"px;width:"+(table.offsetWidth-7)+"px;height:"+(table.offsetHeight-7)+"px;background-color:white;filter: alpha(opacity=100);opacity:1;\"></IFRAME>";
   ifrdiv.innerHTML += html;
},

renderNewPanelLight : function(renderer,uidl,target,layoutInfo) {
    // Shortcuts
    var theme = renderer.theme;
	var style = uidl.getAttribute("style");
	// Create component element
	var div = theme.createPaintableElement(renderer,uidl,target); 
                        
    div.innerHTML = "<TABLE width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><TR><TD width=\"12\"><IMG onload=\"png(this);\" src=\""+theme.root+"img/top-left-lite.png\"></TD><TD style=\"background: url('"+theme.root+"img/top-lite.png') !important;background: none;background-position:right;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/top-lite.png', sizingMethod='scale');\"></TD><TD width=\"12\"><IMG onload=\"png(this);\" src=\""+theme.root+"img/top-right-lite.png\"></TD></TR><TR><TD style=\"background: url('"+theme.root+"img/left.png') !important;background: none;background-position:right;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/left.png', sizingMethod='scale');\"></TD><TD bgcolor=white></TD><TD style=\"background: url('"+theme.root+"img/right.png') !important;background: none;background-position:right;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/right.png', sizingMethod='scale');\"></TD></TR><TR><TD><IMG onload=\"png(this);\" src=\""+theme.root+"img/bottom-left.png\"></TD><TD style=\"background: url('"+theme.root+"img/bottom.png') !important;background: none;background-position:right;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/bottom.png', sizingMethod='scale');\"></TD><TD><IMG onload=\"png(this);\" src=\""+theme.root+"img/bottom-right.png\"></TD></TR></TABLE>";

    var content = div.firstChild.firstChild.childNodes[1].childNodes[1];
    
    theme.renderDefaultComponentHeader(renderer,uidl,content);
    theme.renderChildNodes(renderer, uidl, content);
},

renderNewPanelNone : function(renderer,uidl,target,layoutInfo) {
    // Shortcuts
    var theme = renderer.theme;
	var style = uidl.getAttribute("style");
	// Create component element
	var div = theme.createPaintableElement(renderer,uidl,target); 
            
    var content = theme.createElementTo(div,"div");
   
    theme.renderDefaultComponentHeader(renderer,uidl,content);
    theme.renderChildNodes(renderer, uidl, content);
},

renderNewTabSheet : function(renderer,uidl,target,layoutInfo) {
    // Shortcuts
    var theme = renderer.theme;

	// Create component element
	var div = theme.createPaintableElement(renderer,uidl,target); 
    if (uidl.getAttribute("invisible")) return;  

	var style = uidl.getAttribute("style");
    var disabled  = ("true"==uidl.getAttribute("disabled"));
	
	var cdiv = theme.createElementTo(div,"div");
	var caption = theme.renderDefaultComponentHeader(renderer,uidl,cdiv,layoutInfo);
	div = theme.createElementTo(div,"div");
         
	// Tabs
	var tabNodes = theme.getChildElements(uidl,"tabs");
	if (tabNodes != null && tabNodes.length >0) tabNodes = theme.getChildElements(tabNodes[0],"tab");
	var selectedTabNode = null;
	if (tabNodes != null && tabNodes.length >0) {
	    var html = "<TABLE width=\"100%\" class=\"tabsheet-table\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><TBODY>";
		html += "<TR valign=\"bottom\"><TD></TD>";
		
		var posttabs = "<TR valign=\"top\"><TD><IMG  onload=\"png(this);\" src=\""+theme.root+"img/tabsheet/top-left-lite.png\"/></TD>";
		var len = tabNodes.length;
		for (var i=0; i<len;i++) {
			var tab = tabNodes[i];
			var caption = tab.getAttribute("caption");
			var icon = tab.getAttribute("icon");
			if (icon) icon = theme.root+icon.split("theme://")[1];
			var selected = ("true"==tab.getAttribute("selected"));
			var disabled = ("true"==tab.getAttribute("disabled"));
			var offset = (selected?6:4);
			
			var variant = "";
			if (disabled) {
				variant = "-dis";
			} else if (selected) {
				variant = "-on";
			}

			if (selected) selectedTabNode = tab;
			
   			html += "<TD width=\"1\" align=\"right\"><IMG onload=\"png(this);\" onload=\"png(this);\" src=\""+theme.root+"img/tabsheet/top-left"+variant+".png\"/></TD><TD class=\""+(disabled?"caption":"caption clickable")+"\" style=\"background-image: url('"+theme.root+"img/tabsheet/top"+variant+".png') !important;background: none;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/tabsheet/top"+variant+".png', sizingMethod='scale');\">"
   			html += "<DIV style=\"padding-top:0.5em;\" class=\"caption"+(selected&&!disabled?"":" clickable")+"\">";
   			if (icon) html += "<IMG onload=\"png(this);\" class=\"icon\" src=\""+icon+"\"/>";
   			html += caption+"</DIV>";
   			html += "</TD><TD><IMG  onload=\"png(this);\" src=\""+theme.root+"img/tabsheet/top-right"+variant+".png\"/></TD>";	
   			
   			
   			// POSTTABS		     
   			posttabs += "<TD align=\"right\" style=\"background-image: url('"+theme.root+"img/tabsheet/top-lite.png') !important;background: none;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/tabsheet/top-lite.png', sizingMethod='scale');\"><IMG  onload=\"png(this);\" height=\""+(selected?6:4)+"\" width=\"8\" src=\""+theme.root+"img/tabsheet/tab-left.png\"/></TD><TD "+(selected?"bgcolor=\"white\"":"style=\"background-image: url('"+theme.root+"img/tabsheet/top-lite.png') !important;background: white;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/tabsheet/top-lite.png', sizingMethod='scale');\"")+"></TD><TD style=\"background-image: url('"+theme.root+"img/tabsheet/top-lite.png') !important;background: none;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/tabsheet/top-lite.png', sizingMethod='scale');\"><IMG  onload=\"png(this);\" height=\""+(selected?6:4)+"\" width=\"8\" src=\""+theme.root+"img/tabsheet/tab-right.png\"/></TD>";			
		}
   		html += "<TD width=\"100%\"></TD></TR>"+posttabs+"<TD style=\"background-image: url('"+theme.root+"img/tabsheet/top-lite.png') !important;background: none;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/tabsheet/top-lite.png', sizingMethod='scale');\" ></TD><TD><IMG  onload=\"png(this);\" src=\""+theme.root+"img/tabsheet/top-right-lite.png\"/></TD></TR>";
   		
    	//Content
    	html +="</TBODY></TABLE><TABLE width=\"100%\" class=\"tabsheet-table\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><TBODY><TR valign=\"top\"><TD style=\"width:12px;background-image: url('"+theme.root+"img/tabsheet/left.png') !important;background: none;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/tabsheet/left.png', sizingMethod='scale');\"></TD><TD style=\"width:100% !important;width:auto;\" class=\"tabsheet-content\" bgcolor=\"white\" colspan=\""+(len*3+1)+"\"><DIV></DIV></TD><TD width=\"12\" style=\"background-image: url('"+theme.root+"img/tabsheet/right.png') !important;background: none;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/tabsheet/right.png', sizingMethod='scale');\"></TD></TR>";
		html += "<TR height=\"12\" valign=\"top\"><TD width=\"8\"><IMG onload=\"png(this);\" src=\""+theme.root+"img/tabsheet/bottom-left.png\"></TD><TD style=\"background-image: url('"+theme.root+"img/tabsheet/bottom.png') !important;background: none;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+theme.root+"img/tabsheet/bottom.png', sizingMethod='scale');\" colspan=\""+(len*3+1)+"\"></TD><TD><IMG  onload=\"png(this);\" src=\""+theme.root+"img/tabsheet/bottom-right.png\"></TD></TR></TBODY></TABLE>";
		div.innerHTML = html;
		
		// TODO click listeners
		
		if (!disabled) {
			var varId = theme.getVariableElement(uidl,"string","selected").getAttribute("id");		
			for (var i=0; i<len;i++) {
				var tabNode = tabNodes[i];
				if (tabNode == selectedTabNode||("true"==tabNode.getAttribute("disabled"))) continue;
				var key = tabNode.getAttribute("key");
				var tab = div.firstChild.firstChild.firstChild.childNodes[2+i*3];
				theme.addAddClassListener(theme,client,tab,"mouseover","over",tab);
				theme.addRemoveClassListener(theme,client,tab,"mouseout","over",tab);
				theme.addSetVarListener(theme,client,tab,"click",varId,key,true);
			}		
		}
		
		var content = div.childNodes[1].firstChild.firstChild.childNodes[1];
		if (selectedTabNode) {
			theme.renderChildNodes(renderer,selectedTabNode, content);
		}
		
	}
	
},



/**
 * Prevent text selection in buttons and etc.
 */
addPreventSelectionListener : function(theme,client,div,event) {
	if(itmill.wb.isFF) {
		div.style.MozUserSelect = "none";
	} else if(itmill.wb.isWebkit) {
		div.style.WebkitUserSelect = "none";
	} else {
		// For IE
		this.client.addEventListener(div, "selectstart", function(e) { 
			var evt = itmill.lib.getEvent(e);
			evt.stop();
			return false;
		}
		);
	}
}






}); // End of BaseTheme -class




/** Creates new FilterSelect component.
 *  @param renderer 
 *  @param uidl 
 *  @param target 
 *	@param layoutInfo
 *  @constructor
 *
 *	External resources: filterselect.css, filterselect.js
 *	
 *
 *
 *  @author Oy IT Mill Ltd / Tomi Virtanen
 */

itmill.themes.Base.FilterSelect = function(renderer,uidl,target,layoutInfo) {
	
    // TODO working on undoable & tabbing etc
    
	this.parentTheme = renderer.theme;	
	var parentRenderer = renderer.parentRenderer;
	this.client = renderer.client;			
	var options = renderer.theme.getFirstElement(uidl,"options");
	
	this.filtering = false;
	
	this.id = uidl.getAttribute("id");
	this.uri = options.getAttribute("loadfrom");
	//this.size = parseInt(uidl.getAttribute("size"));
	// TODO
	this.size = 10;
	this.total = parseInt(options.getAttribute("total"));
	this.startIndex = parseInt(0);
	this.selectedIndex = parseInt(-1);
	this.immediate = "true"==uidl.getAttribute("immediate");
	this.focusedIndex = parseInt(0);
    
    var style = uidl.getAttribute("style");
    
	this.popupSelectsHidden = null;
	this.agent = navigator.userAgent.toLowerCase();
	
	var disabled = "true"==uidl.getAttribute("disabled");
	var readonly = "true"==uidl.getAttribute("readonly");	
	var focusid = uidl.getAttribute("focusid");
	var tabindex = uidl.getAttribute("tabindex");
	var caption = uidl.getAttribute("caption");
	this.selectMode = uidl.getAttribute("selectmode");	
	this.selectable = this.selectMode == "multi" || this.selectMode == "single";
	
	var div = this.parentTheme.createPaintableElement(renderer,uidl,target,layoutInfo);
    this.parentTheme.addCSSClass(div,"filterselect");    
	if (uidl.getAttribute("invisible")) {
        // Don't render content if invisible, remove form-style caption as well
        var li = layoutInfo||target.layoutInfo;
        if (li&&li.captionNode) {
            li.captionNode.innerHTML = "";
        }
        return; 
    }
    
 	
	/* 
		ops map contains all current options. Key 'keys' contains all keys in array. Key
	    'values' contains all values in array.
	*/
	var value = options.getAttribute("initial");	
	this.ops = eval("(" + value + ")");
	if (this.ops.keys && this.ops.keys.length > 0) {
		this.size = this.ops.keys.length;
	}

	this.selectionVariable = this.parentTheme.createVariableElementTo(div,this.parentTheme.getVariableElement(uidl,"array","selected"));	

	var modified = uidl.getAttribute("modified");
    var oldValue = (modified?target.oldValue:null);
    var selectedValue = options.getAttribute("selectedValue");
    if (!div.oldValue) 
        div.oldValue = selectedValue;

	// Render default header
	this.parentTheme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
	
	var table = this.parentTheme.createElementTo(div,"table","fslayout");
	var tbody = this.parentTheme.createElementTo(table,"tbody");
	var tr = this.parentTheme.createElementTo(tbody,"tr","row");
	var td = this.parentTheme.createElementTo(tr,"td","cell");
	table = this.parentTheme.createElementTo(td,"table","fssearch-layout");
	var slbody = this.parentTheme.createElementTo(table,"tbody");
	var sltr = this.parentTheme.createElementTo(slbody,"tr");
	var sltdfield = this.parentTheme.createElementTo(sltr,"td","input");
	var sltdtoggle = this.parentTheme.createElementTo(sltr,"td","dropdown");
    //undoable
	var sltdundo = this.parentTheme.createElementTo(sltr,"td");
	
	var	input = this.parentTheme.createElementTo(sltdfield,"input","fsinput");
	input.setAttribute('type','text');
	if(selectedValue != null)
		input.value = selectedValue;	
    //undoable
	input.oldValue = oldValue;
	if(modified&&oldValue&&oldValue != div.oldValue) {
        input.style.background= "#FDFFB3";
	    var undo = this.parentTheme.createElementTo(sltdundo,"span","undo-button");
	    undo.title = "Undo";
        var parentTheme = this.parentTheme;
        var selectionVariable = this.selectionVariable;
		client.addEventListener(undo, "click", 
			function(e) {
                    target.oldValue = input.oldValue;
					parentTheme.setVariable(client, selectionVariable, input.oldValue, true);
				}
		);
	}
    
	var	imagebg = this.parentTheme.createElementTo(sltdtoggle,"div","toggle-bg");	
	var	image = this.parentTheme.createElementTo(imagebg,"div","toggle");	
	this.parentTheme.addAddClassListener(this.parentTheme,this.client,image,"mouseover","highlighted");
	this.parentTheme.addRemoveClassListener(this.parentTheme,this.client,image,"mouseout","highlighted");
			
	tr = this.parentTheme.createElementTo(tbody,"tr","row");
	td = this.parentTheme.createElementTo(tr,"td","cell");
	td.setAttribute('colspan','2');
	
	this.popup = this.parentTheme.createElementTo(td,"div","fspopup");
	var layout = this.parentTheme.createElementTo(this.popup,"div");
	this.parentTheme.addAddClassListener(this.parentTheme,this.client,layout,"mouseover","over");
	this.parentTheme.addRemoveClassListener(this.parentTheme,this.client,layout,"mouseout","over");
	this.upbutton = this.parentTheme.createElementTo(layout,"div","fsup");	
	this.parentTheme.createElementTo(layout,"div");
	
	var selectdiv = this.parentTheme.createElementTo(this.popup,"div","selectbox");
	if (focusid) this.popup.focusid = focusid;
	if (tabindex) this.popup.tabIndex = tabindex;				
	
	this.layout = this.parentTheme.createElementTo(this.popup,"div");
	this.downbutton = this.parentTheme.createElementTo(this.layout,"div","fsdown");
	this.parentTheme.addAddClassListener(this.parentTheme,this.client,this.layout,"mouseover","over");
	this.parentTheme.addRemoveClassListener(this.parentTheme,this.client,this.layout,"mouseout","over");	
	this.statics = this.parentTheme.createElementTo(this.popup,"div","statics");
	this.updateStatistics();
	
	this.updateButtons();
				
			
	this.select = selectdiv;
   	this.toggle = image;
	this.search = input;	
	this.visibleList = null;
	this.isSafari = navigator.userAgent.toLowerCase().indexOf("safari") >=0;
	
	// Some browser dependant configuration
	if (this.isSafari) {
		this.toggle.style.border = "none"; 	
	} else {
		//this.toggle.style.border = "1px solid #7f9db9"; 
	}
	
	// Initial update	
	this.updateContent();
	this.focusOption(this.focusedIndex);
			
	// add listeners
		if (!disabled&&!readonly) {
			
			var fs = this;	
			// Add toggle button click listener	
			this.client.addEventListener(fs.toggle, 'click', function () {					
				if (fs.visibleList != null) {
					fs.closeDropdown();    
	   				fs.focusSearchField();
				} else {										
	    			fs.dropdownMode();
	    			fs.focusSearchField();	    	    			
				}				
			});	
            
			// Add focus listener	
			this.client.addEventListener(fs.search, 'focus', function () {					  
                fs.focusSearchField();				
			});	
            // Add blur (unfocus) listener
            this.client.addEventListener(fs.search, 'change', function (e) {
                    //var evt = this.client.getEvent(e);
                    //evt.stop();                    
                    fs.deselect(fs.selectedIndex%fs.size);					
					fs.selectedIndex = fs.focusedIndex;		
					fs.updateSearch();
					fs.closeDropdown();			
            });	
			
			// Add search field keydown listener	
			this.client.addEventListener(fs.search, 'keydown', function (e) {			
				var keyCode = e.keyCode;				
				if (keyCode == 40) { // down
					fs.dropdownMode();										
					fs.rollDown();					
				} else if (keyCode == 38) { // up
					fs.dropdownMode();	
					fs.rollUp();
				} else if (keyCode == 27) {
					fs.closeDropdown();
					fs.updateSearch();
				} else if (keyCode == 13) {					
					fs.deselect(fs.selectedIndex%fs.size);					
					fs.selectedIndex = fs.focusedIndex;		
					fs.updateSearch();
					fs.closeDropdown();
				} else if (keyCode == 9) {
					fs.closeDropdown();
                }
			});
			
			// Add search field keyup listener	
			this.client.addEventListener(fs.search, 'keyup', function (e) {	
				var keyCode = e.keyCode;
							
				if (keyCode != 38 && 					
					keyCode != 40 && 
					keyCode != 37 && 
					keyCode != 39 && 
					keyCode != 27 && 
					keyCode != 13) {
						fs.doSearch();
				} 
			});														
		}			
}

itmill.themes.Base.FilterSelect.prototype.rollDown = function() {	
	if(this.focusedIndex<this.total-1) {	
		this.defocusOption(this.focusedIndex);
		this.focusedIndex++;
		
		if(this.focusedIndex%this.size == 0) {
			this.startIndex = this.focusedIndex;
			this.moveDown(this);
		} else
			this.focusOption(this.focusedIndex);
	}	
}

itmill.themes.Base.FilterSelect.prototype.rollUp = function() {	
	if(this.focusedIndex>0) {	
		this.defocusOption(this.focusedIndex);
		this.focusedIndex--;
		
		if((this.focusedIndex+1)%this.size == 0) {				
			this.moveUp(this);
		} else
			this.focusOption(this.focusedIndex);
	}	
}


/* Open dropdown box */
itmill.themes.Base.FilterSelect.prototype.dropdownMode = function() {	
	if(this.visibleList != null)
		this.hide(this.visibleList);
	this.visibleList = this.popup;
		
		// ie fix. All select components are hidden when popup is open.
        if (this.agent.indexOf("msie")>=0) {
			var sels = this.popup.ownerDocument.getElementsByTagName("select");
			if (sels) {
				var len = sels.length;
				var hidden = new Array();
				for (var i=0;i<len;i++) {
					var sel = sels[i];
					if (sel.style&&sel.style.display!="none") {
						sel.style.visibility = "hidden";
						hidden[hidden.length] = sel;
					}
				}		
				this.popupSelectsHidden = hidden;
			}
		}
	
	this.show(this.visibleList);
	this.parentTheme.removeCSSClass(this.toggle, "toggle");
	this.parentTheme.addCSSClass(this.toggle, "toggle-selected");				
	this.adjustWidth(this.layout,this.search.clientWidth);			
}

/* Close dropdown box */

itmill.themes.Base.FilterSelect.prototype.closeDropdown = function() {
	// ie fix
	if (this.popupSelectsHidden) {
		var len = this.popupSelectsHidden.length;
		for (var i=0;i<len;i++) {
			var sel = this.popupSelectsHidden[i];
			sel.style.visibility = "visible";
		}
		this.popupSelectsHidden = null;
	}
	this.hide(this.visibleList);
	this.visibleList = null;
	this.parentTheme.removeCSSClass(this.toggle, "toggle-selected");
	this.parentTheme.addCSSClass(this.toggle, "toggle");
}


itmill.themes.Base.FilterSelect.prototype.focusSearchField = function() {
	this.search.focus();
    this.search.select();
	//TODO: Select the text
}

itmill.themes.Base.FilterSelect.prototype.show = function(element) {
	if (element) {
		// TODO width of arrow-icon (18px) could be checked
		element.className = 'fspopup-show';		
		if (element.offsetWidth < element.parentNode.offsetWidth) {
			element.style.width = ( element.parentNode.offsetWidth)+ "px"
		}
	}
}

itmill.themes.Base.FilterSelect.prototype.hide = function(element) {
	if (element) {				
		element.className = 'fspopup';		
	}
}


/* Filter content by server request. Filtering prefix will be added to URL. 
   Server returns only few first options. */
itmill.themes.Base.FilterSelect.prototype.doSearch = function() {
	this.defocusOption(this.focusedIndex);
	this.focusedIndex = 0;
		
	// Get the value to search for
	var searchFor = escape("" + this.search.value.toString().toLowerCase());		
	
	var date = new Date();
	
	// send request
	var text = this.client.loadDocument(this.uri + "/" + date.getTime() + "/" + searchFor, true);	
	this.ops = eval("(" + text + ")");
	
	this.total = (this.ops != null && this.ops.total != null)?parseInt(this.ops.total):0;
	this.startIndex=0;
	this.updateContent();
	
	if(this.total > 0)
		this.dropdownMode();
	this.updateButtons();
	this.focusOption(this.focusedIndex);
}

itmill.themes.Base.FilterSelect.prototype.focusOption = function(index) {
	
	var option = this.select.childNodes[index%this.size];	
	this.parentTheme.addCSSClass(option, "over");
}

itmill.themes.Base.FilterSelect.prototype.defocusOption = function(index) {

	var option = this.select.childNodes[index%this.size];
	if(option != null)
		this.parentTheme.removeCSSClass(option, "over");
}

/* Flash component
*/
itmill.themes.Base.FilterSelect.prototype.flash = function(el) {
	
	if(!this.filtering) {
		var originalColor = el.style.backgroundColor;
		var fs = this;
		var cancelFlash = function() {
			el.style.backgroundColor = originalColor;						
			fs.filtering = false;
		};
		el.style.backgroundColor = "#FFA0A0";
		this.filtering = true;
		setTimeout(cancelFlash,1500);
	}	
}

itmill.themes.Base.FilterSelect.prototype.adjustWidth = function(el, width) {
	if (el.clientWidth <= width) {
		//el.style.width = width;
	} 
}


/* Select option */
itmill.themes.Base.FilterSelect.prototype.selected = function(id) {
	if (id >=0 && id < this.select.childNodes.length) {		
		var option = this.select.childNodes[id];							
		this.parentTheme.removeCSSClass(option,"unselectedrow");	
		this.parentTheme.addCSSClass(option, "selectedrow");
				
		if (this.selectMode == "multi") {
				// TODO support multiselections
		} else {														
			this.parentTheme.setVariable(this.client, this.selectionVariable, option.value, this.immediate);	
		}
	}
}

/* Update search box */
itmill.themes.Base.FilterSelect.prototype.updateSearch = function() {	
	if (this.selectedIndex >=0) {			
		var index = this.selectedIndex%this.size;
		this.selected(index);
		var option = this.select.childNodes[index];
		if(option != null && option.caption != null) {			
			var val = option.caption;
			this.search.value = val;
		}
	} else {
		this.search.value = "";
	}		
}

/* Deselect option */
itmill.themes.Base.FilterSelect.prototype.deselect = function(id) {
	if (id >=0 && id < this.select.childNodes.length) {		
		var option = this.select.childNodes[id];						
		this.parentTheme.removeCSSClass(option, "selectedrow");
		this.parentTheme.addCSSClass(option,"unselectedrow");					
	}
}

/* Update down- and up-buttons listeners and layouts */
itmill.themes.Base.FilterSelect.prototype.updateButtons = function() {		
	if(this.startIndex<=0) {
		this.parentTheme.addCSSClass(this.upbutton,"disabled");
		this.upbutton.fs = this;				
		this.client.removeEventListener(this.upbutton, 'click', this.upButtonClick);		
	} else {
		this.client.removeEventListener(this.upbutton, 'click', this.upButtonClick);
		this.parentTheme.removeCSSClass(this.upbutton,"disabled");
		this.upbutton.fs = this;
		this.client.addEventListener(this.upbutton, 'click', this.upButtonClick);
	}
	 
	if(this.total <= this.size || this.startIndex >= this.total-this.size) {
		this.parentTheme.addCSSClass(this.downbutton,"disabled");
		this.downbutton.fs = this;			
		this.client.removeEventListener(this.downbutton, 'click', this.downButtonClick);		
	} else {
		// remove and then add again
		this.client.removeEventListener(this.downbutton, 'click', this.downButtonClick);	
		this.parentTheme.removeCSSClass(this.downbutton,"disabled");
		this.downbutton.fs = this;
		this.client.addEventListener(this.downbutton, 'click', this.downButtonClick);		
	}	
}

/* Update options. Gets new options from server if nesessary. */
itmill.themes.Base.FilterSelect.prototype.updateContent = function() {

	var keys = this.ops.keys;
	var values = this.ops.values;
	
	// Clear
	this.select.innerHTML = "";
	
	// Add first set of matches
	if (keys != null && values != null && keys.length >0 && keys.length >0) {		
		this.select.disabled = false;
		var index = 0;
		
		for (var i=this.startIndex; i<keys.length && i<this.startIndex+this.size;i++) {
			var optionNode = this.parentTheme.createElementTo(this.select,"div");
			optionNode.style.whiteSpace = "nowrap";
			optionNode.id = index;
			optionNode.value = keys[i];	
			this.parentTheme.addCSSClass(optionNode,"selectbox-row");
			// unescape and replace all '+' characters with space. 
			var caption = this.decodeCaption(values[i]);			
			optionNode.caption = caption;									
			optionNode.innerHTML = caption||"&nbsp";
			if (this.selectMode == "multi") {
				// TODO multiselections
			} else {
				if(this.selectionVariable.value == keys[i]) {
					this.deselect(this.selectedIndex%this.size);					
					this.selected(index);				
					this.selectedIndex = index;									
				} else {				
					this.parentTheme.addAddClassListener(this.parentTheme,this.client,optionNode,"mouseover","over");
					this.parentTheme.addRemoveClassListener(this.parentTheme,this.client,optionNode,"mouseout","over");
					this.parentTheme.addCSSClass(optionNode,"unselectedrow");
				}				
			}
			
			var fs = this;
			
			// clicklistener for this option
			this.client.addEventListener(optionNode, 'click', function () {																											
				var	id = -1;
				if(fs.agent.indexOf("msie")==-1) {
					id = this.id;
					fs.parentTheme.removeCSSClass(this,"over");
				} else {											
					id = this.event.srcElement.id;
					fs.parentTheme.removeCSSClass(this.event.srcElement,"over");						
				}
				fs.deselect(fs.selectedIndex%fs.size);
				fs.selectedIndex = id;
				fs.focusedIndex	= id;
				fs.updateSearch();
				fs.closeDropdown();
				fs.focusSearchField();
			});	
			index++;

		}

	} else {
		this.closeDropdown();		
		this.select.disabled = true;
		this.flash(this.search);		
	}	
	this.updateStatistics();
}

/* Handle up-button click */
itmill.themes.Base.FilterSelect.prototype.upButtonClick = function(e) {	   
    var agent = navigator.userAgent.toLowerCase();
	var fs = null;
	if(agent.indexOf("msie")==-1) {
		fs = this.fs;
	} else {											
		fs = this.event.srcElement.fs;							
	}
	fs.focusSearchField();				
	fs.moveUp(fs);
}


/* Handle down-button click */ 
itmill.themes.Base.FilterSelect.prototype.downButtonClick = function(e) {
 	var agent = navigator.userAgent.toLowerCase();
	var fs = null;
	if(agent.indexOf("msie")==-1) {
		fs = this.fs;
	} else {											
		fs = this.event.srcElement.fs;							
	}
	fs.focusSearchField();
	fs.startIndex += fs.size;	
	fs.moveDown(fs);
}

/* Show previous 'page' */
itmill.themes.Base.FilterSelect.prototype.moveUp = function(fs) {
	fs.focusedIndex = fs.startIndex-1;
	fs.startIndex -= fs.size;	
	if(fs.startIndex<0) {
		fs.startIndex = 0;
		fs.focusedIndex = 0;
	}	
	
	fs.updateContent();
	fs.dropdownMode();
	fs.updateButtons();
	fs.focusOption(fs.focusedIndex);
}

/* Show next 'page' */
itmill.themes.Base.FilterSelect.prototype.moveDown = function(fs) {
	if(fs.startIndex > fs.total) {
		fs.startIndex = fs.startIndex - fs.total%fs.size;		
	}
	fs.focusedIndex = fs.startIndex;
	
	// append new items only when necessary
	if((fs.ops.keys.length<fs.total) && fs.startIndex >=fs.ops.keys.length) {	
		// send request
		var date = new Date();	
		var text = fs.client.loadDocument(fs.uri + "/feedMoreItems/" + date.getTime() + "/" + fs.startIndex, true);
		var map = eval("(" + text + ")");
		fs.appendArray(fs.ops.keys, map.keys); 
		fs.appendArray(fs.ops.values, map.values);
	} 
		
	fs.updateContent();
	fs.dropdownMode();
	fs.updateButtons();
	fs.focusOption(fs.focusedIndex);		
}

/* Appends source array to target array. */
itmill.themes.Base.FilterSelect.prototype.appendArray = function(target, source) {	
	// FIXME any better solution that works?
	for(var i=0;i<source.length;i++) {
		target[target.length] = source[i];
	}
}

/* Update statistics box */
itmill.themes.Base.FilterSelect.prototype.updateStatistics = function() {
	var lastpos = (this.startIndex+this.size-1);
	this.statics.innerHTML = (this.startIndex+1) + "-" + ((lastpos>this.total)?this.total:lastpos+1) + " / " + this.total;
}

itmill.themes.Base.FilterSelect.prototype.decodeCaption = function(encoded) {	
	return unescape(encoded.replace(/[+]+/g, " "));
}

/* ** DIV Windowing ** */


/**
 * Overlay is an object to be used by for example windows (by composition).
 * 
 * Other components that might use it:
 *  * tooltips
 *  * dropdown menus
 * 
 * TODO extend this to make optional shadow around Overlay objects
 * 
 */
itmill.themes.Base.Overlay = function(w,h,x,y,zIndexBase) {
	w = (w ? w : 640);
	h = (h ? h : 400);
	var agent = navigator.userAgent.toLowerCase();
	// IE 6 and mac FF needs iFrame blocker to prevent some form elements
	// seeing trought Overlay, all IE versions with acroreader
	// TODO change to use itmill.wb.isIE
	if ( agent.indexOf("msie") > 0 ) {
		console.log("Adding Iframe blocker");
		this._blocker = document.createElement("iframe");
		this._blocker.className = "overlay_blocker";
		this._blocker.style.width = w + "px";
		this._blocker.style.height = h + "px";
		this._hasBlocker = true;
	} else {
		this._hasBlocker = false;
	}
		
	this._div = document.createElement("div");
	this._div.className = "overlay_body";
	this._div.style.width = w + "px";
	this._div.style.height = h + "px";
	
	this._shadow = document.createElement("div");
	this._shadow.tabIndex = "-1"; // due we need to set mac FF overflow scroll
	
	this._shadow.className = "shadow";
	this._shadow.style.width = (w + 2*this.SHADOW_WIDTH) + "px";
	this._shadow.style.height = (h + 2*this.SHADOW_WIDTH) + "px";
	var tmp = document.createElement("div");
	tmp.className = "NW";
	this._shadow.appendChild(tmp);
	tmp = document.createElement("div");
	tmp.className = "N";
	tmp.style.width = (w + 2*(this.SHADOW_WIDTH - this.SHADOW_CORNER_R)) + "px";
	this._shadow.appendChild(tmp);
	tmp = document.createElement("div");
	tmp.className = "NE";
	this._shadow.appendChild(tmp);
	tmp = document.createElement("div");
	tmp.className = "W";
	tmp.style.height = (h + 2*(this.SHADOW_WIDTH - this.SHADOW_CORNER_R)) + "px";
	this._shadow.appendChild(tmp);
	tmp = document.createElement("div");
	tmp.className = "C";
	tmp.style.width = (w + 2*(this.SHADOW_WIDTH - this.SHADOW_CORNER_R)) + "px";
	tmp.style.height = (h + 2*(this.SHADOW_WIDTH - this.SHADOW_CORNER_R)) + "px";
	this._shadow.appendChild(tmp);
	tmp = document.createElement("div");
	tmp.className = "E";
	tmp.style.height = (h + 2*(this.SHADOW_WIDTH - this.SHADOW_CORNER_R)) + "px";
	this._shadow.appendChild(tmp);
	tmp = document.createElement("div");
	tmp.className = "SW";
	this._shadow.appendChild(tmp);
	tmp = document.createElement("div");
	tmp.className = "S";
	tmp.style.width = (w + 2*(this.SHADOW_WIDTH - this.SHADOW_CORNER_R)) + "px";
	this._shadow.appendChild(tmp);
	tmp = document.createElement("div");
	tmp.className = "SE";
	this._shadow.appendChild(tmp);
	
	this.setXY(x,y);
	this.setZindexBase(zIndexBase ? zIndexBase : 12000);
}

itmill.themes.Base.Overlay.prototype.SHADOW_WIDTH = 2;
itmill.themes.Base.Overlay.prototype.SHADOW_OFFSET = 6;
itmill.themes.Base.Overlay.prototype.SHADOW_CORNER_R = 15;

/**
 * This method is used to set base z-index above which this element floats.
 * Each window has 100 z-indexes. They start from 12000 and below is described
 * what is planned on each:
 *  
 * n + 0 	Window shadow
 * n + 1 	Iframe (blocker)
 * n + 2 	Window border and content divs (content overflow: auto|hidden)
 * n + 50- 	Reserved
 * 
 * @param z base z-index
 */
itmill.themes.Base.Overlay.prototype.setZindexBase = function(z) {
	this._zIndexBase = z;
	this._shadow.style.zIndex = z;
	if(this._hasBlocker)
		this._blocker.style.zIndex = z + 1;
	if(this.isModal())
		this._modalityCurtain.style.zIndex = z + 1;
	this._div.style.zIndex = z + 2;
	// try to workaround nasty scrollbar problems with mac firefox
	var agent = navigator.userAgent.toLowerCase();
	if(agent.indexOf("mac") > 0 && agent.indexOf("firefox") > 0 ) {
		this._div.style.overflow = "auto";
		var fool = this._div.offsetHeight;
		this._div.style.overflow = "visible";
		fool = this._div.offsetHeight;
	}
	
}

/**
 * @return {Number} current z-index base
 */
itmill.themes.Base.Overlay.prototype.getZindexBase = function() {
	return this._zIndexBase;
}


/**
 * With this function Overlay can be made modal, so that components
 * outside modal layer cannot be accessed. This is achieved by drawing a
 * div element just below overlay.
 * 
 * This function can additionally disabble tabbing focus into elements outside
 * modal layer. Tabbing will be restored when modality is removed. This may be
 *  a performance hit on big pages, so this functionality is enabled 
 * optionally.
 * 
 * @param modality true if enabling modality, false on removing
 * @param tabbing boolean flag to indicate if to take "tabbing" into account
 * 
 */
itmill.themes.Base.Overlay.prototype.setModal = function(modality, tabbing) {
	if(modality) {
		this._modalityCurtain = document.createElement("div");
		this._modalityCurtain.tabIndex = "-1";
		this._modalityCurtain.className = "modalityCurtain";
		// to bypass modality curtain by scrolling down
		if(document.body.clientHeight > itmill.wb.getWindowHeight()) this._modalityCurtain.style.height = document.body.clientHeight + "px";
		else this._modalityCurtain.style.height = "100%";
		this._modalityCurtain.style.width = "100%";
		this._modalityCurtain.style.top = "0px";
		this._modalityCurtain.style.left = "0px";
		this._modalityCurtain.style.zIndex = this._zIndexBase + 1;
		// TODO add layout funtions to resize curtain on browser window resizes
		
		// Modality curtain needs to be appended to clients main div
		// FIXME better way to fetch client reference
		itmill.clients[0].mainWindowElement.appendChild(this._modalityCurtain);
		
		if(tabbing) {
			this._disableTabbingOutOfOverlay();
		}
		
	} else {
		// remove modality curtain
		if(this._modalityCurtain && this._modalityCurtain.parentNode) {
			this._modalityCurtain.onclick = null;
			this._modalityCurtain.oncontextmenu = null;
			this._modalityCurtain.parentNode.removeChild(this._modalityCurtain);
		}
		delete this._modalityCurtain;
		if(tabbing) {
			this._restoreTabbing();
		}
		
	}
}

itmill.themes.Base.Overlay.prototype.isModal = function() {
	return this._modalityCurtain != null;
}

itmill.themes.Base.Overlay.prototype.addModalityClickEvent = function(prop) {
	if(this.isModal()) {
		this._modalityCurtain.onclick = function() {
			prop.f.call(prop.obj);
		}
		this._modalityCurtain.oncontextmenu = function() {
			prop.f.call(prop.obj);
		}
	}
}

/**
 * Detects if givent element is in Overlay
 * @param el HTMLElement to be inspected
 */
itmill.themes.Base.Overlay.prototype._containsElement = function(el) {
	if(el) {
		var tmp = el;
		while(tmp.parentNode.nodeType == 1) {
			if(tmp.parentNode && tmp.parentNode == this._div)
				return true;
			tmp = tmp.parentNode;				
		}
	}
	return false;
}

itmill.themes.Base.Overlay.prototype.tabbableTags = new Array("A","BUTTON","TEXTAREA","INPUT","IFRAME", "SELECT", "DIV");

/*
 * This function sets z-index to -1 for all possible elements that might
 * have it
 */
 itmill.themes.Base.Overlay.prototype._disableTabbingOutOfOverlay = function() {
 	if(!this._disTabbableElements) {
 		this._disTabbableElements = new Array();
 	}
	var i = 0;
	for (var j = 0; j < this.tabbableTags.length; j++) {
		var tagElements = document.getElementsByTagName(this.tabbableTags[j]);
		for (var k = 0 ; k < tagElements.length; k++) {
			var el = tagElements[k];
			if(!this._containsElement(el)) {
				// save old tabIndex if element not in modal window and tabbable
				// Divs need an extra check, because they are only tabbable if 
				// tabIndex is set
				if(this.tabbableTags[j] != "DIV" || ( el.tabIndex > 0 || el.style.overflow == "auto" || el.style.overflow == "scroll" )) {
					this._disTabbableElements.push({
						elem: el,
						ti: el.tabIndex
					});
			 		// put out of tabbing order
					el.tabIndex="-1";
				}
			}
			i++;
		}
	}
}

/*
 * This function restores disabled z-indexes to their original value
 */
 itmill.themes.Base.Overlay.prototype._restoreTabbing = function() {
 	while(this._disTabbableElements.length > 0) {
 		var tmp = this._disTabbableElements.pop();
 		tmp.elem.tabIndex = tmp.ti;
 	}
}

/**
 * Set width of floating element (containter and blocker)
 */
itmill.themes.Base.Overlay.prototype.setWidth = function(w) {
	var w2 = (w + 2*(this.SHADOW_WIDTH - this.SHADOW_CORNER_R));
	if(w2 < 0)
		w2 = 0;
	this._shadow.childNodes[1].style.width = w2 + "px";
	this._shadow.childNodes[4].style.width = w2 + "px";
	this._shadow.childNodes[7].style.width = w2 + "px";
	
	this._shadow.style.width = (w + 2*this.SHADOW_WIDTH) + "px";
	if(this._hasBlocker)
		this._blocker.style.width = w + "px";
	this._div.style.width = w + "px";
}

/**
 * Set height of floating element (containter and blocker)
 */
itmill.themes.Base.Overlay.prototype.setHeight = function(h) {
	var h2 = (h + 2*(this.SHADOW_WIDTH - this.SHADOW_CORNER_R));
	if(h2 < 0)
		h2 = 0;
	this._shadow.childNodes[3].style.height = h2 + "px";
	this._shadow.childNodes[4].style.height = h2 + "px";
	this._shadow.childNodes[5].style.height = h2 + "px";
	this._shadow.style.height = (h + 2*this.SHADOW_WIDTH) + "px";
	if(this._hasBlocker)
		this._blocker.style.height = h + "px";
	this._div.style.height = h + "px";
}

/**
 * This function is used to change absolute position of Overlay. To 
 * only set one, define other as null
 * 
 * @param {Number} x x coordinate
 * @param {Number} y y coordinate
 */
itmill.themes.Base.Overlay.prototype.setXY = function(x,y) {
	if(typeof x == "number") {
		this._shadow.style.left = (x - this.SHADOW_WIDTH + this.SHADOW_OFFSET) + "px";
		if(this._hasBlocker)
			this._blocker.style.left = x + "px";	
		this._div.style.left = x + "px";
	}
	if(typeof y == "number") {
		this._shadow.style.top = (y - this.SHADOW_WIDTH + this.SHADOW_OFFSET) + "px";
		if(this._hasBlocker)
			this._blocker.style.top = y + "px";
		this._div.style.top = y + "px";
	}
}

/**
 * Append this Overlay to a given element
 * @param {HTMLElement} parent Element where to append this overlay
 */
itmill.themes.Base.Overlay.prototype.appendTo = function(par) {
	if(!par) {
		par = document.body;
	}
	par.appendChild(this._shadow);
	if(this._hasBlocker)
		par.appendChild(this._blocker);
	par.appendChild(this._div);
	this._par = par;
	var agent = navigator.userAgent.toLowerCase();
	// try to workaround nasty scrollbar problems with mac firefox
	if(agent.indexOf("mac") > 0 && agent.indexOf("firefox") > 0 ) {
		this._div.style.overflow = "auto";
		var fool = this._div.offsetHeight;
		this._div.style.overflow = "visible";
		fool = this._div.offsetHeight;
	}
	
}

/**
 * Get reference to element where content is to be rendered
 * @return {HTMLElement} container element
 */
itmill.themes.Base.Overlay.prototype.getPaintTarget = function() {
	return this._div;
}

itmill.themes.Base.Overlay.prototype.dispose = function() {
	this._shadow.parentNode.removeChild(this._shadow);
	if(this._hasBlocker)
		this._blocker.parentNode.removeChild(this._blocker);
	if(this.isModal())
		this._modalityCurtain.parentNode.removeChild(this._blocker);
	this._div.parentNode.removeChild(this._div);
}

/**
 * TkWindow implements window component with HTMLDiv elements.
 * 
 * Possible arguments and their default values:
 * {
 * width 	: 640,
 * height 	: 400,
 * posX		: 0,
 * posY		: 0,
 * title	: "New Window",
 * parentNode : document.body,
 * draggable: true,
 * resizeable: true
 * }
 * 
 * @argument {Object} args hash containing initial settings for window
 */
itmill.themes.Base.TkWindow = function(args) {
	if(!args)
		args = new Object();

	// TODO remove client determining hack to support mutltiple apps in one page
	this.client = itmill.clients[0];
	
	if(args.width && args.width > 0)
		this._width = parseInt(args.width) > 100  ? args.width : 100;
	else
		this._width = 640;
	if(args.height && args.height > 0)
		this._height = parseInt(args.height) > 100 ? args.height : 100;
	else
		this._height = 400;
	
	if(args.modal ) {
		// if modal, center window
		this._x = Math.floor((itmill.wb.getWindowWidth() - this._width) / 2 ) ;
		this._y = Math.floor((itmill.wb.getWindowHeight() - this._height) / 2 ) ;
	} else {
		this._x	= args.posX ? args.posX : 0;
		this._y	= args.posY ? args.posY : 0;
	}

	this._header = document.createElement("div");
	this._header.tabIndex = "-1"; // due we need to set mac FF overflow scroll
	this._header.className = "winHeader"
	
	this._draggable = ( typeof args.draggable == "boolean" ) ? args.draggable : true;
	if(this._draggable) {
		this.client.addEventListener(this._header,"mousedown", this._onHeaderMouseDown);
		this._header.TkWindow = this;
		
		// constraint to browser window ?
		this._constrainToBrowser = (typeof args.constrainToBrowser == "boolean") ? args.constrainToBrowser : false ;

	}
	
	this._closeable = ( typeof args.closeable == "boolean" ) ? args.closeable : true;
	if(this._closeable) {
		this._closeButton = document.createElement("div");
		this._closeButton.TkWindow = this;
		this._closeButton.className = "closeButton";
		this._header.appendChild(this._closeButton);
		this.client.addEventListener(this._closeButton,"click",this._onCloseListener);
	}
	var capElement = document.createElement("div");
	capElement.appendChild(document.createTextNode(args.title ? args.title : "New Window"));
	capElement.className ="caption";
	this._header.appendChild(capElement);
	
	this.childTarget = this._body = document.createElement("div");
	this._footer = document.createElement("div");
	this._footer.tabIndex = "-1"; // due we need to set mac FF overflow scroll
	
	this._resizeable = typeof args.resizeable == "boolean" ? args.resizeable : true
	if(this._resizeable) {
		this._winResizer = document.createElement("div");
		this._winResizer.className = "winResizer"
		this._winResizer.TkWindow = this;
		this._footer.appendChild(this._winResizer);
		this.client.addEventListener(this._winResizer, "mousedown", this._onResizeStart);
	}
	
	// create and populate container
	this._cont	= document.createElement("div");
	this._cont.TkWindow = this;
	this.client.addEventListener(this._cont, "click", this._onClickHandler);
	
	this._cont.appendChild(this._header);
	this._cont.appendChild(this._body);
	this._cont.appendChild(this._footer);

	
	this._body.className = "winBody";
	this._body.style.height = (this._height - this.HEADER_HEIGHT - this.FOOTER_HEIGHT) + "px";
	this._body.style.position = "absolute";
	this._body.style.width = this._width + "px";

	this._footer.className = "winFooter";
	this._footer.style.position = "absolute";
	this._footer.style.bottom = "0px";
	this._footer.style.width = this._width + "px";
	
	// TODO determine proper z-index base and pass it to Overlay object
	this._ol = new itmill.themes.Base.Overlay(
		this._width + 2 * this.BORDER_WIDTH,
		this._height + 2 * this.BORDER_WIDTH,
		this._x,
		this._y
	);
	
	var tmp = this._ol.getPaintTarget();
	tmp.appendChild(this._cont);
	
	this._ol.appendTo(args.parentNode ? args.parentNode : null);
	
	// new window should be positioned on top
	this._setWindowIndex(this.client.windowOrder.length);
	this.client.windowOrder.push(this);
	if(args.modal)
		this._ol.setModal(true,true);
}

/**
 * This function makes window the frontmost
 */
itmill.themes.Base.TkWindow.prototype.bringToFront = function() {
	var wo = this.client.windowOrder;
	var curIndex = wo.indexOf(this);
	if(curIndex < (wo.length -1) ) {
		// if not on top already
		wo.splice(curIndex,1);
		for(;curIndex < wo.length;curIndex++)
			wo[curIndex]._setWindowIndex(curIndex);
		wo.push(this);
		this._setWindowIndex(curIndex);
	}
}

/**
 * This function is used by bringToFront function to order windows.
 */
itmill.themes.Base.TkWindow.prototype._setWindowIndex = function(i) {
	this._ol.setZindexBase(12000 + i*100);
}

itmill.themes.Base.TkWindow.prototype.setWidth = function(w) {
	if( w > 100) {
		this._ol.setWidth(w + 2 * this.BORDER_WIDTH);
		this._cont.style.widht = w + "px";
		this._width = w;
		this._body.style.width = w + "px";
		this._footer.style.width = w + "px";
		
	}
}

itmill.themes.Base.TkWindow.prototype.setHeight = function(h) {
	if( h > 100) {
		this._ol.setHeight(h + 2 * this.BORDER_WIDTH);
		this._body.style.height = ( h - this.HEADER_HEIGHT - this.FOOTER_HEIGHT ) + "px";
		this._height = h;
	}
}

/**
 * This clickhandler forces window to be to frontmost when clicked,
 * No support for X11 fanatics yet, sorry...
 */
itmill.themes.Base.TkWindow.prototype._onClickHandler = function(e) {
	var evt = itmill.lib.getEvent(e);
	// due lack in IE's event handling, get TkWindow via helper that loops DOM
	var tkWindow = itmill.lib.getTkWindow(evt.target);
	if(tkWindow) {
		tkWindow.bringToFront();
//		evt.stop();
	}
		
}

/**
 * This is actionListener for starting dragging
 */
itmill.themes.Base.TkWindow.prototype._onHeaderMouseDown =  function(e) {
	var evt = itmill.lib.getEvent(e);
	evt.stop();
	var tkWindow = itmill.lib.getTkWindow(evt.target);
	tkWindow.bringToFront();
	
	tkWindow.origMouseX = evt.mouseX;
	tkWindow.origMouseY = evt.mouseY;
	tkWindow.origX = tkWindow._ol._div.offsetLeft;
	tkWindow.origY = tkWindow._ol._div.offsetTop;
	
	var client = itmill.lib.getClient(evt.target);
	client.dragItem = tkWindow;
	client.addEventListener(document, "mousemove", tkWindow._onDrag);
	client.addEventListener(document, "mouseup", tkWindow._onDragMouseUp);
	// don't use drag
	client.addEventListener(document, "drag", tkWindow._stopListener);
	document.onselectstart = function(e) {return false;}
	
}

itmill.themes.Base.TkWindow.prototype._onDragMouseUp = function(e) {
	var evt = itmill.lib.getEvent(e);
	var client = itmill.lib.getClient(evt.target);
	client.removeEventListener(document,"mousemove",itmill.themes.Base.TkWindow.prototype._onDrag);
	client.removeEventListener(document,"mouseup",itmill.themes.Base.TkWindow.prototype._onDragMouseUp);
	client.removeEventListener(document,"drag",itmill.themes.Base.TkWindow.prototype._stopListener);
	document.onselectstart = null ;
	return false;
}

itmill.themes.Base.TkWindow.prototype._onDrag = function(e) {
	// "this" is document.body
	var evt = itmill.lib.getEvent(e);
	var client = itmill.lib.getClient(evt.target);
	evt.stop();
	var tkWindow = client.dragItem;
	var x =	(evt.mouseX - tkWindow.origMouseX + tkWindow.origX);
	var y = (evt.mouseY - tkWindow.origMouseY + tkWindow.origY);
	if(tkWindow._constrainToBrowser) {
		if(x > document.documentElement.clientWidth - tkWindow._width - 2*tkWindow.BORDER_WIDTH)
			x = document.documentElement.clientWidth - tkWindow._width - 2*tkWindow.BORDER_WIDTH;
		// conditional height is due Safari bug
		if(y > (window.innerHeight ? window.innerHeight : document.documentElement.clientHeight) - tkWindow._height - 2*tkWindow.BORDER_WIDTH)
			y = (window.innerHeight ? window.innerHeight :document.documentElement.clientHeight) - tkWindow._height - 2*tkWindow.BORDER_WIDTH;
		if(x < 0)
			x = 0;
		if(y < 0)
			y = 0;
	}
	tkWindow._ol.setXY(x,y);
	tkWindow._x = x;
	tkWindow._y = y;
}

/**
 * This is actionListener for starting window resizing
 */
itmill.themes.Base.TkWindow.prototype._onResizeStart =  function(e) {
	var evt = itmill.lib.getEvent(e);
	var client = itmill.lib.getClient(evt.target);

	evt.stop();

	var tkWindow = evt.target.TkWindow;
	tkWindow.origMouseX = evt.mouseX;
	tkWindow.origMouseY = evt.mouseY;
	tkWindow.origW = tkWindow._width;
	tkWindow.origH = tkWindow._height;
	
	client.dragItem = tkWindow;
	client.addEventListener(document, "mousemove", tkWindow._onResizeDrag);
	client.addEventListener(document, "mouseup", tkWindow._onStopResizing);
	// don't use drag
	client.addEventListener(document, "drag", tkWindow._stopListener);
	document.onselectstart = function(e) {return false;}
	
}

/**
 * Event listener when mouse up after resizing.
 * 
 * We will remove all event listeners used during resizing here.
 */
itmill.themes.Base.TkWindow.prototype._onStopResizing = function(e) {
	var evt = itmill.lib.getEvent(e);
	var client = itmill.lib.getClient(evt.target);
	client.removeEventListener(document,"mousemove",itmill.themes.Base.TkWindow.prototype._onResizeDrag);
	client.removeEventListener(document,"mouseup",itmill.themes.Base.TkWindow.prototype._onStopResizing);
	client.removeEventListener(document,"drag",itmill.themes.Base.TkWindow.prototype._stopListener);
	evt.stop();
	document.onselectstart = null;
	return false;
}

/*
 * This is fired during resizing when mouse moves. Changing the size.
 */
itmill.themes.Base.TkWindow.prototype._onResizeDrag = function(e) {
	// "this" is document.body
	var evt = itmill.lib.getEvent(e);
	evt.stop();
	var client = itmill.lib.getClient(evt.target);
	// client reference might not be got if app's base area is small, then fetch first
	// toolkit app
	// TODO refactor this so that client.dragItem is moved to itmill.dragItem (only one thing
	// can be dragged at once anyway)
	if(!client)
		client = itmill.clients[0];
	var tkWindow = client.dragItem;
	var w = evt.mouseX - tkWindow.origMouseX + tkWindow.origW;
	if(w < 100)
		w = 100;
	var h = evt.mouseY - tkWindow.origMouseY + tkWindow.origH;
	if(h < 100)
		h = 100;
	if(tkWindow._constrainToBrowser) {
		if(tkWindow._x + w > itmill.wb.getWindowWidth() - 2*tkWindow.BORDER_WIDTH)
			w = document.documentElement.clientWidth - tkWindow._x - 2*tkWindow.BORDER_WIDTH;
		if( tkWindow._y + h > itmill.wb.getWindowHeight() - 2*tkWindow.BORDER_WIDTH) {
			h = itmill.wb.getWindowHeight() - tkWindow._y - 2*tkWindow.BORDER_WIDTH;
		}
	}
	tkWindow.setWidth(w);
	tkWindow.setHeight(h);
}


itmill.themes.Base.TkWindow.prototype._stopListener = function(e) {
	var evt = itmill.lib.getEvent(e);
	evt.stop();
	return false;
}

/**
 * This listener is fired when window is to be closed. This will not actually
 * close it, but only notify server that user clicked "close button" or some
 * customized event requested window close.
 * 
 * Server will then in most common situation send back an instruction to 
 * really close the window, but functionality can be overridden on Java side.
 */
itmill.themes.Base.TkWindow.prototype._onCloseListener = function(e) {
	var evt = itmill.lib.getEvent(e);
	var client = itmill.lib.getClient(evt.target);
	evt.stop();
	var tkWindow = evt.target.TkWindow;

	var windowPaintable = client.getPaintable(evt.target);
	var closeVar = windowPaintable.varMap["close"];
	closeVar.value = true;
	client.changeVariable(closeVar.id, closeVar.value, true);
}

/**
 * Using this window it is possible to set window modal
 */
itmill.themes.Base.TkWindow.prototype.setModal = function(modal) {
	if(modal) {
		// ensure this is the frontmost window
		this.bringToFront();
		// tell Overlay to enable modalityCurtain and modify tabbing
		this._ol.setModal(true,true);
		// center window
		var x = Math.floor((itmill.wb.getWindowWidth() - this._width) / 2 ) ;
		var y = Math.floor((itmill.wb.getWindowHeight() - this._height) / 2 ) ;
		this._ol.setXY(x,y);
		
		// TODO capture focus events outside this window
		// Following commented line would be the obvious solution: 
		
	} else {
		if(this._ol.isModal()) {
			// remove modality curtain and restore tabbing order
			this._ol.setModal(false, true);
		}
	}
}

itmill.themes.Base.TkWindow.prototype.cleanUp = function() {
	this.client.debug("Removing window");
	//TODO remove circular references from window
	// move this window to top and order the rest
	this.bringToFront();
	// remove reference from windowOrder
	this.client.windowOrder.pop();
	this.setModal(false);
	this._ol.dispose();
}

/**
 * Defines headers and footers height.
 * 
 * NOTE! CSS file must also comfort to this value
 */
itmill.themes.Base.TkWindow.prototype.HEADER_HEIGHT = 20;

/**
 * Defines footers height.
 * 
 * NOTE! CSS file must also comfort to this value
 */
itmill.themes.Base.TkWindow.prototype.FOOTER_HEIGHT = 15;

/**
 * Defines windows border width.
 * 
 * NOTE! CSS file must also comfort to this value
 */
itmill.themes.Base.TkWindow.prototype.BORDER_WIDTH = 1;

/* DRAFTING BELOW */

 itmill.ui = new Object();

 /**
 * Context menu that gets rendered under mouseClick.
 * 
 * NOTE: Do not call this constructor directly. Instead use getContextMenu() in
 * client, so we can ensure there is only one context menu. We want this to
 * ensure performance on views that have hundreds of objects that may be
 * "right clicked".
 */
itmill.ui.ContextMenu = function() {
 	this.options = new Array();
 	this._container = document.createElement("div");
 	this._container.className = "contextMenu";
 	this._container.style.display = "none";
 	this._htmlElement = document.createElement("ul");
 	this._htmlElement.onclick = this._clickHandler;
 	this._htmlElement.contextMenu = this; // save reference for event handlers
 	this._ol = new itmill.themes.Base.Overlay();
 	this._ol.setZindexBase(30000); // Toolkit default for context menus
	this._ol.appendTo(this._container);
	var pTarget = this._ol.getPaintTarget();
	pTarget.appendChild(this._htmlElement);
	console.info("Context menu created");
}

itmill.ui.ContextMenu.prototype.appendTo = function(el) {
	el.appendChild(this._container);
}

/**
 * This method is called by eventHandlers to show context menu.
 * 
 * @param aOptions {array} containts objects describing context menus options.
 * Option objects look like this:
 * {caption, actionValue}
 * 
 * They may also optionally include property "checked" (true/false).
 * 
 * @param 	evt	crossbrowser event which triggered the contextMenu building,
 *  			is used to determine proper position for contextMenu
 * @param actionVar is the variable which will be updated on action click
 */
itmill.ui.ContextMenu.prototype.showContextMenu = function(aOptions, evt, actionVar) {
	console.log("Populating CMenu");
	// save actionVar reference for event handlers
	this.actionVar = actionVar;
	
	// truncate old menu items
	while(this._htmlElement.firstChild) {
		this._htmlElement.removeChild(this._htmlElement.firstChild);
	}
	// delete possible customClickHandler
	delete this._customClickHandler;
	
	// set maximum width for context menu
	this._ol.setWidth(250);
	
	// append new nodes
	for(var i = 0; i < aOptions.length; i++) {
		var opt = aOptions[i];
		var li = document.createElement("li");
		li.actionValue= opt.actionValue;
		if(opt.icon) {
			var iconNode = document.createElement("img");
			iconNode.src = opt.icon;
			iconNode.className = 'icon';
			li.appendChild(iconNode);
		}
		li.appendChild(document.createTextNode(opt.caption));
		if(typeof opt.checked != "undefined") {
			if(opt.checked) 
				li.className = "on";
			else
				li.className = "off";
		}
		this._htmlElement.appendChild(li);
	}
	this._ol.setModal(true);
	// we may want to define non-standard opacity for context menus
	// modality curtain
	this._ol._modalityCurtain.className += " cmModalityCurtain";
	this._ol.addModalityClickEvent({f:this._hide,obj:this});
	this._container.style.visibility = "hidden";
	this._container.style.display = "block";
	this._ol.setWidth(this._htmlElement.offsetWidth);
	// width now fixed, fix individual list items to blocks again
	for(var i = 0; i < this._htmlElement.childNodes.length; i++) {
		this._htmlElement.childNodes[i].style.display = "block";
		this._htmlElement.style.display = "block";
	}
	this._ol.setHeight(this._htmlElement.offsetHeight);
	var x = evt.mouseX;
	var y = evt.mouseY;
	// if not enough room on right side, pop on left
	if(x + this._htmlElement.offsetWidth + this._ol.SHADOW_WIDTH > itmill.wb.getWindowWidth()) {
		x = itmill.wb.getWindowWidth() - this._htmlElement.offsetWidth - this._ol.SHADOW_WIDTH;
	}
	
	// if not enough room below, pop on top
	var height = itmill.wb.getWindowHeight() 
	             + document.body.scrollTop 
	             + document.documentElement.scrollTop;
	if(y + this._htmlElement.offsetHeight > height) {
		y = height - this._htmlElement.offsetHeight;
	}
	this._container.style.top = y + "px";
	this._container.style.left = x + "px";
	this._container.style.visibility = "visible";
}

/**
 * This is default clickHandler for context menu. It can be overridden
 * by setting own to clickHandler
 */
itmill.ui.ContextMenu.prototype._clickHandler = function(e) {
	var evt = itmill.lib.getEvent(e);
	var client = itmill.lib.getClient(evt.target);
	// get cm from target which is icon or li element
	var cm;
	if(evt.target.parentNode.contextMenu)
		cm = evt.target.parentNode.contextMenu;
	else
		cm = evt.target.parentNode.parentNode.contextMenu;
	if(!cm) return;
	if(!cm.clickHandler) {
		var li = evt.target;
		var actionVar = cm.actionVar;
		client.changeVariable(actionVar.id, li.actionValue, true);
		cm._hide();
	} else {
		cm.clickHandler(evt);	
	}
}

/*
 * This functiond hides context menu from User, but does not destroy it
 * so it can be re-used with showContextMenu function.
 * 
 * @private Called by ContextMenus own event handlers.
 */
itmill.ui.ContextMenu.prototype._hide = function() {
	this._ol.setModal(false);
	this._container.style.display = "none";
}

/**
 * Cleans circular references to avoid IE leaks and detaches menu from
 * html DOM
 */
itmill.ui.ContextMenu.prototype.cleanUp = function() {
	// TODO
}

/**
 * TODO
 */
itmill.ui.Shortcut = function(target, func, keyCode, c, a, s) {
	this.target = target;
	this.func = func;
	this.keyCode = keyCode;
	this.ctrl = c || false;
	this.alt = a || false;
	this.shift = s || false;
}

itmill.ui._shortcutHandler =  function(e) {
	var evt = itmill.Client.prototype.getEvent(e);
	// TODO cont shoud be fetched
	var cont = document.body;
	if(cont.shortcutMap.length > 0) {
		// loop shortcuts and detect "hit"
		for(var i = 0 ; i < cont.shortcutMap.length;i++) {
			sc = cont.shortcutMap[i];
			if(
			sc.keyCode == evt.e.keyCode &&
			sc.alt == evt.alt &&
			sc.ctrl == evt.ctrl &&
			sc.shift == evt.shift
			) {
				console.info("matched key combination")
				evt.stop();
				sc.func.call(sc.target);
				return false; // prevent default action
			}
		}
	}
}

 /**
 * Context menu that gets rendered under mouseClick.
 * 
 * NOTE: Do not call this constructor directly. Instead use getContextMenu() in
 * client, so we can ensure there is only one context menu. We want this to
 * ensure performance on views that have hundreds of objects that may be
 * "right clicked".
 */
itmill.ui.Tooltip = function() {
 	this._container = document.createElement("div");
 	this._container.className = "tooltip";
 	this._container.style.display = "none";
 	this._htmlElement = document.createElement("div");
 	this._htmlElement.tooltip = this; // save reference for event handlers
 	this._ol = new itmill.themes.Base.Overlay();
 	this._ol.setZindexBase(31000); // Toolkit default for context menus
 	this._ol.SHADOW_OFFSET = 0;
 	this._ol.SHADOW_WIDTH = 0;
	this._ol.appendTo(this._container);
	var pTarget = this._ol.getPaintTarget();
	pTarget.appendChild(this._htmlElement);
	console.info("Tooltip container created");
}

itmill.ui.Tooltip.prototype.appendTo = function(el) {
	el.appendChild(this._container);
}

/**
 * Pixels reserved for tooltips padding, border and shadow
 */
itmill.ui.Tooltip.prototype.EXTRA_WIDTH = 10;

/**
 * This method is called by eventHandlers to show context menu.
 * 
 * @param content {htmlSnippet} containts content to be shown in tooltip normally plain text
 * @param 	evt	crossbrowser event which triggered the contextMenu building,
 *  			is used to determine proper position for contextMenu
 */
itmill.ui.Tooltip.prototype.showTooltip = function(content, evt) {
	// truncate old content
	while(this._htmlElement.firstChild) {
		this._htmlElement.removeChild(this._htmlElement.firstChild);
	}
	
	// set maximum width for context menu
	this._ol.setWidth(250);
	
	// append new tooltip
	
	this._htmlElement.innerHTML = "<span>"+content+"</span>";
	
	this._container.style.visibility = "hidden";
	this._container.style.display = "block";
	this._ol.setWidth(this._htmlElement.firstChild.offsetWidth + this.EXTRA_WIDTH*2);
	
	this._ol.setHeight(this._htmlElement.offsetHeight);
	var x = evt.mouseX;
	var y = evt.mouseY + 15;
	// if not enough room on right side, pop on left
	if(x + this._htmlElement.offsetWidth + this.EXTRA_WIDTH > itmill.wb.getWindowWidth()) {
		x = itmill.wb.getWindowWidth() - this._htmlElement.offsetWidth - this.EXTRA_WIDTH;
	}
	
	// if not enough room below, pop on top
	if(y + this._htmlElement.offsetHeight + this.EXTRA_WIDTH*2 > itmill.wb.getWindowHeight()) {
		y = itmill.wb.getWindowHeight() - this._htmlElement.offsetHeight * 2 - this.EXTRA_WIDTH - 15;
	}
	this._container.style.top = y + "px";
	this._container.style.left = x + "px";
	this._container.style.visibility = "visible";
}

/*
 * This functiond hides tooltip menu from User, but does not destroy it
 * so it can be re-used with showTooltip function.
 * 
 * @private Called by Tooltips own event handlers.
 */
itmill.ui.Tooltip.prototype._hide = function() {
	this._container.style.display = "none";
}

/**
 * Cleans circular references to avoid IE leaks and detaches menu from
 * html DOM
 */
itmill.ui.Tooltip.prototype.cleanUp = function() {
	// TODO
}
