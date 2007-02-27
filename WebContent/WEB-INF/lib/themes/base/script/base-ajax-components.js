if(document.all) {
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
	client.registerRenderer(this,"select","twincol",this.renderSelectTwincol);
	client.registerRenderer(this,"upload",null,this.renderUpload);
	client.registerRenderer(this,"embedded",null,this.renderEmbedded);

	client.registerRenderer(this,"window",null,this.renderWindow);
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

createTextNodeTo : function (target,text) {

	// Sanity check
	if (text == null || target == null) return null;

	// Create DIV as container
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
        variable.value = this.arrayToList(variableElement);
    } else if (variable.type == "string") {
        var node = this.getFirstTextNode(variableElement);
        variable.value = (node?node.data:"");
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
	
		this.addCSSClass(caption,"clickable");
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
		if(description) popupTarget._descriptionHTML = client.getXMLtext(description);
		if(error) popupTarget._errorHTML = client.getXMLtext(error);
		this.addDescriptionAndErrorPopupListener(theme, client, popupTarget, errorIcon);
	}

	return caption;
},

renderActionPopup : function(renderer, uidl, to, actions, actionVar, id, popupEvent) {
	// Shortcuts
	var theme = renderer.theme;
	var client = renderer.client;
	var evtName = popupEvent||"rightclick";

	var ak = uidl.getElementsByTagName("ak");
	var len = ak.length;
	if (len < 1) return;

	var popup = theme.createElementTo((to.nodeName=="TR"?to.firstChild:to),"div", "actions outset hide");
	theme.addHidePopupListener(theme,client,popup,"click");
	theme.addStopListener(theme,client,popup,"click");
	
	var inner = theme.createElementTo(popup,"div", "border");	
	var item = theme.createElementTo(inner,"div", "item pad clickable");
	
	for (var k=0;k<len;k++) {
		var key = theme.getFirstTextNode(ak[k]).data;
		var item = theme.createElementTo(inner,"div", "item pad clickable");
		theme.createTextNodeTo(item,actions[key]);
		theme.addAddClassListener(theme,client,item,"mouseover","over");
		theme.addRemoveClassListener(theme,client,item,"mouseout","over");
		theme.addSetVarListener(theme,client,item,"click",actionVar,id+","+key,true);
		theme.addHidePopupListener(theme,client,item,"click");
		theme.addStopListener(theme,client,item,"click");
	}					
	theme.addStopListener(theme,client,to,"contextmenu");
	//theme.addStopListener(theme,client,to,evtName);
	theme.addTogglePopupListener(theme,client,to,evtName,popup);
},

/** Show popup at specified position.
 *  Hides previous popup.
 *  
 *  @param popup		The element to popup
 *  @param x			horizontal popup position
 *  @param y			vertical popup position
 *  @param delay		delay before popping up
 *  @param defWidth		(optional) default width for the popup
 *  @param dontHideSelects (optional) set true if there's no need to hide browsers select components (for IE).
 *  
 */
showPopup : function(client,popup, x, y, delay, defWidth, dontHideSelects) {

	if (this.popupTimeout) {
		clearTimeout(this.popupTimeout);
		delete this.popupTimeout;
	}
	if (!popup) {
		var popup = this.popup;
		this.popupShowing = true;
		var scrollTop = (document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop);
		var scrollLeft = (document.documentElement.scrollLeft ? document.documentElement.scrollLeft : document.body.scrollLeft);
		var docWidth = document.body.clientWidth;
		var docHeight = document.body.clientHeight;
		this.removeCSSClass(popup,"hide");
		
		if(popup.dontHideSelects) {
			var ua = navigator.userAgent.toLowerCase();
	        if (ua.indexOf("msie")>=0) {
				var sels = popup.ownerDocument.getElementsByTagName("select");
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
		}
		/* TODO fix popup width & position */
		return;		
	}
	if (!delay) var delay = 0;
	if (this.popup && this.popup != popup) {
		this.hidePopup();
	} 
	this.popup = popup;
/*	THIS CODE IS NOT NEEDED IF WE CAN POSITION THE POPUP BEFOREHAND

	popup.style.left = 0+"px";
	popup.style.top = 0+"px";
	this.removeCSSClass(popup,"hide");

    var p = client.getElementPosition(popup);
    this.addCSSClass(popup,"hide");
    // TODOO!!! width not working properly
	if (p.w > document.body.clientWidth/2) {
		popup.style.width = Math.round(document.body.clientWidth/2)+"px";
		p.w = Math.round(document.body.clientWidth/2);
	}

    var posX = x||p.x;
    var posY = y||p.y;
    if (posX+p.w>document.body.clientWidth) {
    	posX = document.body.clientWidth-p.w;
    	if (posX<0) posX=0;
    }
    if (posY+p.h>document.body.clientHeight) {
    	posY = document.body.clientHeight-p.h;
    	if (posY < 0) posY =0;
    }
    
    if (p.h > document.body.clientHeight -20) {
		popup.style.height = document.body.clientHeight -20 + "px";
		popup.style.overflow = "auto";
		posX -= 20;
	}
    
    popup.dontHideSelects = dontHideSelects;
	popup.style.left = posX+"px";
	popup.style.top = posY+"px";
*/
	if (delay > 0) {
		with ({theme:this}) {
			theme.popupTimeout = setTimeout(function(){
					theme.showPopup(client);
				}, delay);
		}
	} else {
		this.showPopup(client);
	}
},

/** Hides previous popup.
 */
hidePopup : function() {
	if (this.popupSelectsHidden) {
		var len = this.popupSelectsHidden.length;
		for (var i=0;i<len;i++) {
			var sel = this.popupSelectsHidden[i];
			sel.style.visibility = "visible";
		}
		this.popupSelectsHidden = null;
	}

	if (this.popup) {
		this.addCSSClass(this.popup,"hide");
		this.popupShowing = false;
	}
	if (this.popupTimeout) {
		clearTimeout(this.popupTimeout);
		delete this.popupTimeout;
	}
},

/** Shows the popup if it's not currently shown,
 *  hides the popup otherwise.
 *  Hides previous popup.
 *  
 *  @param popup		The element to popup
 *  @param x			horizontal popup position
 *  @param y			vertical popup position
 *  @param delay		delay before popping up
 *  @param defWidth		(optional) default width for the popup
 *  @param blocker		Blocker element (usually an iframe) to hide underlying browser chrome
 *  
 */
togglePopup : function(popup, x, y, delay, defWidth, blocker) {
	// presuming "this" is theme
	if (this.popup == popup && this.popupShowing) {
		this.hidePopup();
		if(blocker) this.addCSSClass(blocker,"hide");
	} else {
		this.showPopup(this.client,popup,x,y,delay,defWidth,blocker?true:false);
		if(blocker) {
			blocker.style.position = "absolute";
			blocker.style.width = popup.offsetWidth + "px";
			blocker.style.height = popup.offsetHeight + "px";
			blocker.style.background = "transparent";
			blocker.style.filter = "alpha(opacity=0)";
			blocker.style.opacity = "0";
			this.removeCSSClass(blocker,"hide");
		}
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

// TODO dontstop -> stop in all listeners
addHidePopupListener : function(theme,client,element,event,dontstop) {
	client.addEventListener(element,(event=="rightclick"?"click":event), function(e) {
			var evt = client.getEvent(e);
            if (evt.alt) return;
			if (event=="rightclick"&&!evt.rightclick) return;
			theme.hidePopup();
			if (!dontstop) {
				evt.stop();
			}
		}
	);
},

/**
* Adds a hidden button with a tabindex; adds .over to hoverTarget when focused
*/
addTabtoHandlers : function(client,theme,target,hoverTarget,tabindex,defaultButton) {
	
	var d = this.createElementTo(target,"div");
	d.style.border = "none";
	d.style.background = "none";
	d.style.padding = "0px";
	d.style.margin = "0px";
	d.style.width = "0px";
	d.style.height = "0px";
	d.style.overflow = "hidden";

	var b = this.createInputElementTo(d,(defaultButton?"submit":"button"));

	if (tabindex) b.tabIndex = tabindex;

	client.addEventListener(b,"focus", function() {
		theme.addCSSClass(hoverTarget,"over");
	});
	client.addEventListener(b,"blur", function() {
		theme.removeCSSClass(hoverTarget,"over");
	});
    return b;
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
	var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
	
	var theme = renderer.theme;
	
	// If theme is changed, reload window
	var currentTheme = div.itmtkTheme;
	div.itmtkTheme = uidl.getAttribute("theme");
	if (typeof currentTheme != 'undefined' && div.itmtkTheme != currentTheme)
		window.location.href = window.location.href;
	
    theme.addHidePopupListener(theme,renderer.client,div,"click",true);
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
    
    // Load the layout
    var url = theme.root + style;   
    var text = renderer.client.loadDocument(url,false); 
    if (text == null) {
    	client.debug("CustomLayout " + style + " NOT FOUND @ "+ url);
    	return null; 
    }

	// Create containing element
	var main = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);		
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
    
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
    
    
    var n = theme.createElementTo(main, "div");
    n.setAttribute("id",uidl.getAttribute("id"));
    n.innerHTML=text;
    var divs = n.getElementsByTagName("div");
    for (var i=0; i<divs.length; i++) {
      var div = divs.item(i);
      var name = div.getAttribute("location");      
      if (name != null) {
         var c = locations[name];
         if (c && c.getAttribute("name") == name) {   
          	delete unused[name];       
            for (var k=0; k<c.childNodes.length; k++) {
              var cc = c.childNodes.item(k); 
              if (cc.nodeType == Node.ELEMENT_NODE) {
                var parent = div.parentNode;               
                // TODO
                if (parent != null) {
                	client.removeAllEventListeners(div);
                	theme.removeAllChildNodes(div);
                	var newNode = renderer.client.renderUIDL(cc,div);
                }
              }
            }  
        } else {
        	client.warn("Location " + name + " NOT USED in CustomLayout " + style);
        }
      }
    }
    if (unused.length>0) {
    	for (var k in usedLocations) {
    		client.error("Location " + k + " NOT FOUND in CustomLayout " + style);
    	}
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
                console.info(outer.scrollHeight);
                console.info(caption.offsetHeight);
                content.style.height = (outer.scrollHeight - caption.offsetHeight) + "px";
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

    var f = function() {
        // poll server for changes
        client.processVariableChanges(false,false);
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
	theme.addCSSClass(caption,"treecaption");
	
	// If no actual caption, remove description popup listener
	if(caption && caption.className.indexOf("hide") > -1) {
		client.removeEventListener(div,undefined,null,"descriptionPopup");
	}

	// Content DIV
	var content = theme.createElementTo(div,"div","content"); 
	
	// Iterate all nodes
	for (var i = 0; i< uidl.childNodes.length;i++) {
		var node = uidl.childNodes[i];
		if (node.nodeName == "node" || node.nodeName == "leaf") {
			theme.renderTreeNode(renderer,node,content,selectable,selectMode,selected,selectionVariable,expandVariable,collapseVariable,actions,actionVar,immediate,disabled,readonly);
		} 	
	}
},

renderTreeNode : function(renderer,node,target,selectable,selectMode,selected,selectionVariable,expandVariable,collapseVariable,actions,actionVar,immediate,disabled,readonly) {

	var theme = renderer.theme;
	var client = renderer.client;

	var n = theme.createElementTo(target,"div","node clickable");
	
	// Expand/collapse/spacer button
	var img = theme.createElementTo(n,"img","icon");
	var key = node.getAttribute("key");	
	var icon = node.getAttribute("icon");
	if (icon) {
        var iconurl = theme.root+icon.split("theme:")[1];
        var iimg = theme.createElementTo(n,"img","icon");
	    iimg.src = iconurl;
    }
	
	// Caption
	var cap = theme.createElementTo(n,"span","");
	theme.createTextNodeTo(cap,node.getAttribute("caption"));	
	
	// Hover effects
	if (!disabled&&!readonly) {
		theme.addAddClassListener(theme,client,n,"mouseover","over",n);
		theme.addRemoveClassListener(theme,client,n,"mouseout","over",n);
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
	if (selectable && !disabled) {
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
	// and does node have subnodes/leafs?
	var hasChildren = false;
	for (var i=0; i<node.childNodes.length; i++) {
		var childNode = node.childNodes[i];
		if(!hasChildren && (childNode.nodeName == "node" || childNode.nodeName == "leaf")) {
			hasChildren = true;
		}
		if (!disabled && !readonly && childNode.nodeName == "al" ) {
			theme.renderActionPopup(renderer,childNode,n,actions,actionVar,key);
		} 
	}
	
	// Render all sub-nodes
	if (node.nodeName == "node") {
		
		if (hasChildren) {
			img.src = theme.root + "img/tree/on.gif";
			img.expanded = "true";
		} else {
			img.src = theme.root + "img/tree/off.gif";
			img.expanded = "false";
		}
		
		var subnodes = theme.createElementTo(target,"div","nodes");
		
		if(hasChildren) {
			for (var i = 0; i< node.childNodes.length;i++) {
				var childNode = node.childNodes[i];
				if (childNode.nodeName == "node" || childNode.nodeName == "leaf") {
					theme.renderTreeNode(renderer,childNode,subnodes,selectable,selectMode,selected,selectionVariable,expandVariable,collapseVariable,actions,actionVar,immediate,disabled,readonly);
				}
			}
		}
		
		// Add event listener
		if (!disabled) {
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

dateFieldShowCalendar : function (cal) {
	// "this" is triggering element that has inputs id in inputId
	var inputField = document.getElementById(this.inputId);
	
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
	
	// Render default header
	renderer.theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);

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
    '<input type="submit" value="Send" />' +
    '</form>'
    ;
    
    var form = formContainer.firstChild;
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
	/*
	with(props) {
		client.addEventListener(div,"mouseover", function(e) {
				theme.addCSSClass(div,"over");
			}
		);
		client.addEventListener(div,"mouseout", function(e) {
				theme.removeCSSClass(div,"over");
			}
		);
		client.addEventListener(div,"click", function(e) {
				theme.hidePopup();
				if (!target) {
					window.location = src;
				} else {
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
						feat += ",resizable=no"
					} else {
						feat += ",resizable=yes"
					}
					var win = window.open(src, target,
								feat
									+(width?",width="+width:"")
									+(height?",height="+height:"")
							);
					win.focus();
				}			
			}
		);
	}
	*/
	//var inner = theme.createElementTo(div,"div", "border pad");
	
	// Render default header
	theme.renderDefaultComponentHeader(renderer,uidl,link);
	
	// Description under link
	/* Unnecessary, tooltip description already.
	var descriptionText = theme.getElementContent(uidl,"description");
	if (descriptionText) {
		var desc = theme.createElementTo(link,"div", "description");
		theme.createTextNodeTo(desc,descriptionText);
	}
	*/
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

	// Create default header
	var caption = theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
	
	// If no actual caption, remove description popup listener
	if(caption && caption.className.indexOf("hide") > -1) {
		client.removeEventListener(div,undefined,null,"descriptionPopup");
	}
	
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
	var visibleCols = theme.getFirstElement(uidl,"visiblecolumns");
	var collapseVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","collapsedcolumns"));
	var sortcolVar = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"string","sortcolumn"));
	var sortkey = theme.getVariableElementValue(theme.getVariableElement(uidl,"string","sortcolumn"));
	var sortasc = theme.getVariableElement(uidl,"boolean","sortascending");
	var sortascVar = theme.createVariableElementTo(div,sortasc);
	sortasc = sortasc != null && "true"==sortasc.getAttribute("value");
	
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
	if (cols != null && cols.length >0) {
		tr = theme.createElementTo(table,"tr","header");
		if (rowheaders) {
			theme.createElementTo(tr,"td","empty");
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
				//var icon = theme.createElementTo(cap,"IMG","icon");
				//icon.src = theme.root+"img/table/"+(sortasc?"asc.gif":"desc.gif");
				theme.addCSSClass(td,sortasc?"asc":"desc");
			}
		}
		
		// Collapsing
		td = theme.createElementTo(tr,"td","cheader scroll bg");
		if (visibleCols) {
			var iconDiv = theme.createElementTo(td,"div","img");
			var icon = theme.createElementTo(iconDiv,"img","icon");
			icon.src = theme.root+"img/table/colsel.gif";
			var popup = theme.createElementTo(td,"div","outset popup hide");
			var inner = theme.createElementTo(popup,"div","border");
			// empty row to allow closing:
			var row = theme.createElementTo(inner,"div","item clickable pad border");

			theme.addHidePopupListener(theme,client,row,"click");
			theme.addToggleClassListener(theme,client,row,"mouseover","over");
			theme.addToggleClassListener(theme,client,row,"mouseout","over");		
			theme.addTogglePopupListener(theme,client,iconDiv,"click",popup);
			
			var cols = visibleCols.getElementsByTagName("column");
			for (var i=0;i<cols.length;i++) {
				var row = theme.createElementTo(inner,"div","item clickable pad border");
				var collapsed = "true"==cols[i].getAttribute("collapsed");
				theme.addCSSClass(row,collapsed?"off":"on");				
				theme.createTextNodeTo(row,cols[i].getAttribute("caption"));

				theme.addToggleClassListener(theme,client,row,"mouseover","over");
				theme.addToggleClassListener(theme,client,row,"mouseout","over");
				theme.addToggleVarListener(theme,client,row,"click",collapseVariable,cols[i].getAttribute("cid"),true);
			}
			delete cols;
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
						if (al) {
							theme.renderActionPopup(renderer,al,td,actions,actionVar,key);
						}	
					}
				}
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
        // we are repainting existing table
        redraw = true;
		colWidths = target.colWidths;
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
    
	var pagelength = model.meta.pagelength = parseInt(uidl.getAttribute("pagelength"));
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
    var reqrowsVar = theme.createVarFromUidl(div,theme.getVariableElement(uidl,"integer","reqrows"));
    var reqfirstrowVar = theme.createVarFromUidl(div,theme.getVariableElement(uidl,"integer","reqfirstrow"));


	var ccVar = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","collapsedcolumns"));
	var coVar = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","columnorder"));
	var selVar = model.selVar = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","selected"));
	var sortVar = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"string","sortcolumn"));
	var sortasc = theme.getVariableElement(uidl,"boolean","sortascending");
	var sortascVar = theme.createVariableElementTo(div,sortasc);
	sortasc = (sortasc != null && "true"==sortasc.getAttribute("value"));


	// Create default header
	var caption = theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
	theme.addCSSClass(caption, "tablecaption");
	
	// If no actual caption, remove description popup listener
	if(caption && caption.className.indexOf("hide") > -1) {
		client.removeEventListener(div,undefined,null,"descriptionPopup");
	}
	
	
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
		actionVar = theme.createVariableElementTo(div,theme.getVariableElement(alNode,"string","action"));
        model.actionVar = actionVar;
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
	if (visiblecols) {
		vcols.innerHTML = "<DIV class=\"colsel\"><div></div></DIV>";
		var icon = vcols.firstChild; 
		vcols.id = pid+"vcols";
		var popup_blocker = theme.createElementTo(vcols,"iframe","popup-blocker-iframe hide");
		var popup = theme.createElementTo(vcols,"div","border popup hide");
		theme.addTogglePopupListener(theme,client,icon,"click",popup,null,null,null,popup_blocker);
		theme.addStopListener(theme,client,icon,"mouseover");
		theme.addStopListener(theme,client,icon,"mouseout");
		var row = theme.createElementTo(popup,"div","item clickable pad border");
		theme.addHidePopupListener(theme,client,row,"click");
		var cols = visiblecols.getElementsByTagName("column");
		for (var i=0;i<cols.length;i++) {
			var row = theme.createElementTo(popup,"div","item clickable pad border");
			var collapsed = "true"==cols[i].getAttribute("collapsed");
			row.className += (collapsed ? " off" : " on")
			theme.createTextNodeTo(row,cols[i].getAttribute("caption"));
			theme.addAddClassListener(theme,client,row,"mouseover","over");
			theme.addRemoveClassListener(theme,client,row,"mouseout","over");
			theme.addToggleVarListener(theme,client,row,"click",ccVar,cols[i].getAttribute("cid"),true);
		}
		delete cols;		
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
		if (colWidths["heh"]) {
			html += "width="+colWidths["heh"]+"\" ";
		}
		html += "cid=\"heh\" id=\""+pid+"heh\" class=\"heh\">";
        html += "<img id=\""+pid+"hah\" src=\""+theme.root+"img/table/handle.gif\" class=\"colresizer\" >";
        html += "<div class=\"headerContent\" style=\"";
    	if (colWidths["heh"]) {
			html += "width:"+(colWidths["heh"] - 17)+"px;";
		}
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
		if (colWidths[cid]) {
            // set width explicitely
			html += 'width="'+colWidths[cid]+'"';
		} 
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
		if (colWidths[cid]) {
        // header contents widht needs to be explicitely set to WIDTH - COL_RESIZER_WIDTH
        // to enable grabbing resizer when header cells content overflows
			html += 'style="width:'+(colWidths[cid] - 17)+'px;"';
		} 
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
	cout = theme.createElementTo(inner,"div");
    model.cout = cout; // save reference for use in handlers
	cout.id = pid+"cout";
	theme.addCSSClass(cout,"cout");
    // TODO move this to CSS
	cout.style.overflow = "scroll";
    
    // create spacer elements and save reference to model (needed for webkit bug)
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
			if (colWidths["heh"]) {
                td.width = colWidths["heh"];
                tdDiv.style.width = (colWidths["heh"]-4)+"px";
			} 
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
            tr.appenChild(td);
		}
		var al = null;
		var colNum = -1;
		for (j=0;j<l;j++) {
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
			if (colWidths[model.colorder[colNum]]) {
                // set container divs width explicitely due IE overflow bug
                // width - border - margin
                td.width = colWidths[model.colorder[colNum]];
                tdDiv.style.width = (colWidths[model.colorder[colNum]] - 4) + "px";
			}
            // render content
            // render content
            if(comp.nodeName == 'label') {
                // skip heavy renderUIDL function if only text
                tdDiv.appendChild(d.createTextNode(comp.firstChild.data));
            } else {
                client.renderUIDL(comp, tdDiv);
            }
            td.appendChild(tdDiv);
            tr.appendChild(td);
		}
        if (al && tr.firstChild) {
            theme.renderActionPopup(renderer,al,tr,actions,actionVar,key,"rightclick");
        }
        // selection
        if (model.meta.selectmode != "none"  && ! model.meta.readonly ) {
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
    var prePad = (model.state.fv - 1) * model.rowheight;
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
        cout.style.height = table.offsetHeight+16+"px";
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
        model.state.width = div.style.width = (cout.scrollWidth + 20 ) + "px";
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
    if (model.meta.totalrows > model.state.lastRendered) {
        reqfirstrowVar.value = model.state.lastRendered + 1;
        reqrowsVar.value = model.meta.cacheSize;
        theme.updateVar(client,reqfirstrowVar, false);
        theme.updateVar(client,reqrowsVar, true);
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
        for (k=0;k<l;k++) {
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
            if(comp.nodeName == 'label') {
                // skip heavy renderUIDL function if only text
                cellContent.appendChild(d.createTextNode(comp.firstChild.data));
            } else {
                renderer.client.renderUIDL(comp, cellContent);
            }
            
            row.appendChild(cell);
        }
        if (al&&row.firstChild) {
            theme.renderActionPopup(renderer,al,row,tm.meta.actions,tm.actionVar,key,"rightclick");
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
            var row = createRow(trs[i], ((model.request.firstrow + i)%2 == 1));
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
            row = createRow(trs[i], ((tm.state.firstRendered - 1)%2 == 1 ));
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
            var row = createRow(trs[i], ((model.request.firstrow + i)%2 == 1));
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
            var row = createRow(trs[i], ((model.request.firstrow + i)%2 == 1));
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
			if (w < 17) w = 17;
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
		if (d + target.model.meta.pagelength > target.model.state.lastRendered || d < target.model.state.firstRendered) {
 			status.innerHTML = d + "-" + (d+target.model.meta.pagelength-1) + " / " + target.model.meta.totalrows;
 			status.style.display = "block";
 		}
		cout.scrollTimeout = setTimeout(function () {
				cout.scrollHandler();
			},250)	
 	});
},

/* Calculates first totally visible row */
scrollTableGetFV : function(target) {
    var m = target.model;
    var new_fr = Math.ceil(m.cout.scrollTop/m.rowheight) + 1;
 	if (new_fr < 1) return 1; // scrolled past begin
 	if (new_fr > (m.meta.totalrows - m.meta.pagelength + 1)) new_fr=(m.meta.totalrows-m.meta.pagelength + 1); // scrolled past last page
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
            colWidths[h[i].getAttribute("cid")] = parseInt((h[i].lastChild.clientWidth > c[i].clientWidth) ? (h[i].clientWidth) : c[i].clientWidth) + defPad;
        }
    }
    for (var i = 0;i< h.length ;i++) {
        var cell = h[i];
        var cid = cell.getAttribute("cid");
        var w = colWidths[cid];
        cell.width = w;
        // set div.headerContents width to w - COL_RESIZER_WIDTH - margin - 10px extra for possible sort indicator
        // now text doesn't overlap resizer & sort indicator
        cell.lastChild.style.width = (w - 17)+"px";

        // enter looping rows only if width is changed
        if(c[i].offsetWidth != w ) {
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
		unselected.size = (options.length>7?7:options.length);
        selected.size = (options.length>7?7:options.length);
	}
    
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
	// TODO: 
	// 	- newitem currently always immediate, change
	//	- optiongrouphorizontal style	
					
	var theme = renderer.theme;
	var client = renderer.client;
	
	// Create containing element
	var div = theme.createPaintableElement(renderer,uidl,target);	
	if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
	
	// Create selection variable
	var selectMode = uidl.getAttribute("selectmode");
	var selectable = selectMode == "multi" || selectMode == "single";
	var immediate = ("true" == uidl.getAttribute("immediate"));
	var disabled = ("true" == uidl.getAttribute("disabled"));
	var readonly = ("true" == uidl.getAttribute("readonly"));
	var newitem = ("true" == uidl.getAttribute("allownewitem"));
	var selectionVariable = theme.createVariableElementTo(div,theme.getVariableElement(uidl,"array","selected"));
	
	// Render default header
	theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);

	// Create select input	
	var select = theme.createElementTo(div,"div");
	var options = theme.getFirstElement(uidl,"options");
	if (options != null) {
		options = options.getElementsByTagName("so");
	}	
	
	// Selected options
	if (options != null && options.length >0) {
		for (var i=0; i<options.length;i++) {
			var optionUidl = options[i];
			var iconUrl = optionUidl.getAttribute("icon");
			var div = theme.createElementTo(select,"div", "nobr");
			var key = optionUidl.getAttribute("key");
			
			// Create input
			var inputName = "input"+uidl.getAttribute("id");
			var inputId = inputName+i;
			var input = null;
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
			html += " ><label class=\"clickable\" for=\""+inputId+"\">";
			if (caption) html += caption;
			if (iconUrl) {
				if (iconUrl.indexOf("theme://") == 0) {
	    			iconUrl = (theme.iconRoot != null ? theme.iconRoot : theme.root) 
	    					+ iconUrl.substring(8);
	    		}
	    		html += "<IMG src=\""+iconUrl+"\" class=\"icon\">";
			}				
			html += "</label>";
			
			div.innerHTML = html;
			if (!(disabled||readonly)) {
				var input = div.firstChild;
				if (selectMode == "multi") {
					theme.addToggleVarListener(theme,client,input,"click",selectionVariable,key,immediate);
				} else {
					theme.addSetVarListener(theme,client,input,"click",selectionVariable,key,immediate);
				} 
			}
		}
	}
	if (newitem) {
		var ni = theme.createElementTo(div,"div","newitem");
		var input = theme.createInputElementTo(ni,"text");
		var button = theme.createElementTo(ni,"button");
		theme.createTextNodeTo(button,"+");
		var newitemVariable = theme.createVariableElementTo(ni,theme.getVariableElement(uidl,"string","newitem"));
		theme.addSetVarListener(theme,client,input,"change",newitemVariable,input,true);
	}
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
		} if (child.nodeType == Node.TEXT_NODE && child.data != null) {
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
			
			var div = theme.createPaintableElement(renderer,uidl,target,layoutInfo);
			if (uidl.getAttribute("invisible")) return; // Don't render content if invisible
			
			div = renderer.theme.createElementTo(div,"div",(linkStyle?"link clickable":"outset clickable"));
			var outer = renderer.theme.createElementTo(div,"div",(linkStyle?"":"outer"));
			var inner = renderer.theme.createElementTo(outer,"div",(linkStyle?"pad":"border pad bg"));
			
			var caption = theme.renderDefaultComponentHeader(renderer,uidl,inner);
			var hiddenInput = theme.addTabtoHandlers(client,theme,caption,div,tabindex,("default"==uidl.getAttribute("style")));
			
			if (!disabled&&!readonly) {
                // make sure other components release their focus and possibly update their variables
                renderer.client.addEventListener(div,"mousedown", function() {
                    hiddenInput.focus();    
                });
                // Handlers
				var v = theme.getVariableElement(uidl,"boolean", "state");
				if (v != null) {
					var varId = v.getAttribute("id");
                    
					theme.addSetVarListener(theme,client,div,"click",varId,"true",immediate);
					
					theme.addAddClassListener(theme,client,div,"mousedown","down",div);
					theme.addRemoveClassListener(theme,client,div,"mouseup","down",div);
					theme.addRemoveClassListener(theme,client,div,"mouseout","down",div);
					
					theme.addAddClassListener(theme,client,div,"mouseover","over",div);
					theme.addRemoveClassListener(theme,client,div,"mouseout","over",div);
					
					theme.addPreventSelectionListener(theme,client,div);
				}		
			}
				
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


addDescriptionAndErrorPopupListener : function(theme, client, target, errorIcon) {
	
	client.addEventListener(target, "mouseover", 
		function(e) {
			var pos = theme.calculateAbsoluteEventPosition(theme, client, e);
			theme.showDescriptionAndErrorPopup(theme, target, pos, 500); // 500 = delay
		}
	, "descriptionPopup");
	client.addEventListener(target, "mouseout", 
		function(e) {
			if(!target._forcedOpen) theme.hideDescriptionAndErrorPopup(target);
		}
	);
	client.addEventListener(target.ownerDocument.body, "click", 
		function(e) {
			var ev = e? e:window.event;
			if(!ev.cancelBubble) theme.hideDescriptionAndErrorPopup(target, true); // true = force close
		}
	);
	
	if(errorIcon) {
		client.addEventListener(errorIcon, "click", 
			function(e) {
				var ev = e? e:window.event;
				var pos = theme.calculateAbsoluteEventPosition(theme, client, ev);
				theme.showDescriptionAndErrorPopup(theme, target, pos, null, true); // null = no delay, true = force open
				if(e.stopPropagation) e.stopPropagation();
				ev.cancelBubble = true;
			}
		);
	}
	
},

showDescriptionAndErrorPopup : function(theme, target, pos, delay, forceOpen) {

	if(target._descriptionPopupTimeout) clearTimeout(target._descriptionPopupTimeout);

	var descHTML = target._descriptionHTML;
	var errorHTML = target._errorHTML;
	if(!descHTML && !errorHTML) return;
	
	if(!delay) {
		// Reference to correct document
		var doc = target.ownerDocument;
		
		// Maximum css width of the description popup
		var maxPopupWidth = "35em";
		
		var popupContainer;
		var description;
		var error;
		var iframe;
		
		// If the container div is not found, create it once
		if(!(popupContainer = doc.getElementById("popup-container-div"))) {
			popupContainer = doc.createElement("div");
			popupContainer.className = "popup";
			popupContainer.id = "popup-container-div";
			popupContainer.style.left = "-10000px";
			popupContainer.style.top = "-10000px";
			
			description = doc.createElement("div");
			description.className = "description border";
			popupContainer.appendChild(description);
			
			error = doc.createElement("div");
			error.className = "error";
			popupContainer.appendChild(error);
			
			// IFrame to block browser components from clipping through (for IE6)
			iframe = doc.createElement("iframe");
			iframe.id = "popup-blocker-iframe";
			iframe.style.position = "absolute";
			iframe.style.zIndex = "99998";
			iframe.style.width = "0";
			iframe.style.height = "0";
			
			doc.body.appendChild(popupContainer);
			doc.body.appendChild(iframe);
			
			// Enable clicking on container
			client.addEventListener(popupContainer, "click", 
				function(e) {
					var ev = e? e:window.event;
					if(e.stopPropagation) e.stopPropagation();
					ev.cancelBubble = true;
				}
			);
		// If already created, restore references
		} else {
			description = popupContainer.firstChild;
			error = popupContainer.childNodes[1];
			//iframe = popupContainer.childNodes[2];
			iframe = doc.getElementById("popup-blocker-iframe")
		}
		
		description.innerHTML = descHTML? descHTML : "";
		error.innerHTML = errorHTML? errorHTML : "";
		
		if(!descHTML) description.style.display = "none";
		else description.style.display = "block";
		if(!errorHTML) error.style.display = "none";
		else error.style.display = "block";
		
		// Calculate maximum width in pixels
		popupContainer.style.width = maxPopupWidth;
		var maxPopupWidthPixels = popupContainer.clientWidth;
		popupContainer.style.width = "auto";
		
		// Align the popup
		// TODO take smaller resolutions into consideration (i.e. if the popupWidth is wider than the browser window)
		var popupWidth = popupContainer.clientWidth;
		var available = doc.body.clientWidth-pos.x;
		
		if(popupWidth > available || popupWidth > maxPopupWidthPixels) {
			popupContainer.style.width = maxPopupWidth;
			popupWidth = popupContainer.clientWidth;
			if(popupWidth > available) popupContainer.style.left = pos.x + 12 - (popupWidth-available) + "px";
			else popupContainer.style.left = pos.x + 12 + "px";
			popupContainer.style.top = pos.y + 20 + "px";
		} else {
			popupContainer.style.left = pos.x + 12 + "px";
			popupContainer.style.top = pos.y + 20 + "px";
		}
		
		iframe.style.width = popupWidth + "px";
		iframe.style.height = popupContainer.clientHeight + "px";
		iframe.style.position = "absolute";
		iframe.style.left = popupContainer.style.left;
		iframe.style.top = popupContainer.style.top;
		
		if(forceOpen) target._forcedOpen = true;
		else target._forcedOpen = false;

	} else {
		target._descriptionPopupTimeout = setTimeout(function() {theme.showDescriptionAndErrorPopup(theme, target, pos);}, delay);
	}
},

hideDescriptionAndErrorPopup : function(target, forceClose) {
	var popupContainer;
	if(target._descriptionPopupTimeout) clearTimeout(target._descriptionPopupTimeout);
	if((popupContainer = target.ownerDocument.getElementById("popup-container-div")) && (!target._forcedOpen || forceClose)) {
		popupContainer.style.left = "-10000px";
		popupContainer.style.top = "-10000px";
		var iframe = target.ownerDocument.getElementById("popup-blocker-iframe");
		iframe.style.left = "-10000px";
		iframe.style.top = "-10000px";
		target._forcedOpen = false;
	}
},


/** 
* Calculate the absolute coordinates relative to this documents window of 
* the event using mouse and browser window positions relative to the screen (IE)
* (Not 100% accurate, some pixels off in both IE and FF)
* Firefox uses normal DOM calculations.
*
* @param boolean modFF   Don't use DOM calculations to get the position in Firefox.
* (Not reliable with all frame and browser plugin compositions)
*
* Returns an object with x and y properties.
*/
calculateAbsoluteEventPosition : function(theme, client, e, modFF) {
	
	if (!e) var e = window.event;
	
	if(window.screenY || window.screenY === 0) { // Firefox
		
		// Calculate with default DOM calculation
		if(!modFF) return theme.eventPosition(e);
		
		// Doesn't actually work, always returns true.
		var statusBarHeight = (window.statusbar.visible)? 26 : 0; // Default XP Blue theme estimate
		
		// Coordinates to browser windows content area, with no frames.
		// Substract browser chrome (toolbars and such) from the height.
		// Some plugins, that require screen estate from the browser content 
		// window, may misalign the coordinates.
		var windowY = window.screenY + (window.outerHeight - window.innerHeight - statusBarHeight);
		var windowX = window.screenX + (window.outerWidth - window.innerWidth);
		
		// Gecko needs to take frames into consideration when calculating the position.
		// TODO Only works reliably when the page is simply divided either into rows or cols.
		
		// Are we in a frame?
		// (this script is in one of the frames, so the parent will have frames)
		var frames = window.parent.frames;
		var len = frames.length;
		
		if(len > 0) { // Frames in use
			var precedingWidth = 0, precedingHeight = 0;
			var flagSucceeding = false;
			
			for(var i=0; i < len; i++) {
				var frame = frames[i];
				
				if(window==frame) flagSucceeding = true;
				
				// Only count preceding widths that are different from this window (don't count row widths)
				if(frame.innerWidth != window.innerWidth || frame != window) {
					if(!flagSucceeding) {
						precedingWidth += frame.innerWidth;
						continue; // Don't count the height anymore
					}
				}
				// Same for preceding heights, if no width was added
				if(frame.innerHeight != window.innerHeight || frame != window) {
					if(!flagSucceeding) precedingHeight += frame.innerHeight;
				}
				
			}
			
			// Do the math :)
			windowY = window.screenY + (window.outerHeight - window.parent.innerHeight + precedingHeight - statusBarHeight);
			windowX = window.screenX + (window.outerWidth - window.parent.innerWidth + precedingWidth);
		}
		
	} else if(window.screenTop) { // IE, handles frames correct natively
		var windowY = window.screenTop;
		var windowX = window.screenLeft;
	}
	
	var scroll = theme.getScrollXY(window);
	
	// TODO Relative positioned containers for the popup 
	// will cause wrong positioning
	var posx = e.screenX - windowX + scroll.x;
	var posy = e.screenY - windowY + scroll.y;
	return {x:posx, y:posy};
	
},

/**
* Calculate the scroll amount of a window, both x and y.
*
* Returns an object with x and y properties.
*/
getScrollXY : function(win) {
	var doc = win.document;
	var scrOfX = 0, scrOfY = 0;
	if( typeof( win.pageYOffset ) == 'number' ) {
		//Netscape compliant
		scrOfY = win.pageYOffset;
		scrOfX = win.pageXOffset;
	} else if( doc.body && ( doc.body.scrollLeft || doc.body.scrollTop ) ) {
		//DOM compliant
		scrOfY = doc.body.scrollTop;
		scrOfX = doc.body.scrollLeft;
	} else if( doc.documentElement && ( doc.documentElement.scrollLeft || doc.documentElement.scrollTop ) ) {
		//IE6 standards compliant mode
		scrOfY = doc.documentElement.scrollTop;
		scrOfX = doc.documentElement.scrollLeft;
	}

	return {x:scrOfX, y:scrOfY};
},

/**
* Calculate event position (Quirksmode.org script)
*/
eventPosition : function(e) {
	var posx = 0;
	var posy = 0;
	if (!e) var e = window.event;
	
	if (e.pageX || e.pageY) 	{
		posx = e.pageX;
		posy = e.pageY;
	} else if (e.clientX || e.clientY) 	{
		posx = e.clientX + document.body.scrollLeft
			+ document.documentElement.scrollLeft;
		posy = e.clientY + document.body.scrollTop
			+ document.documentElement.scrollTop;
	}
	
	return {x:posx, y:posy};
},


/**
 * Prevent text selection in buttons and etc.
 */
addPreventSelectionListener : function(theme,client,div,event) {
	this.client.addEventListener(div, "mousedown", function(e) { 
			var evt = client.getEvent(e);
			evt.stop();
			return false;
		}
	);
	// For IE
	this.client.addEventListener(div, "selectstart", function(e) { 
			var evt = client.getEvent(e);
			evt.stop();
			return false;
		}
	);
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
	var layout = this.parentTheme.createElementTo(this.popup,"div","layout");
	this.parentTheme.addAddClassListener(this.parentTheme,this.client,layout,"mouseover","over");
	this.parentTheme.addRemoveClassListener(this.parentTheme,this.client,layout,"mouseout","over");
	this.upbutton = this.parentTheme.createElementTo(layout,"div","fsup");	
	
	var selectdiv = this.parentTheme.createElementTo(this.popup,"div","selectbox");
	if (focusid) this.popup.focusid = focusid;
	if (tabindex) this.popup.tabIndex = tabindex;				
	
	this.layout = this.parentTheme.createElementTo(this.popup,"div","layout");
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
	// ie fix.
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
		element.className = 'fspopup-show';		
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
		el.style.width = width;
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
			optionNode.id = index;
			optionNode.value = keys[i];	
			this.parentTheme.addCSSClass(optionNode,"selectbox-row");
			// unescape and replace all '+' characters with space. 
			var caption = this.decodeCaption(values[i]);			
			optionNode.caption = caption;
			this.parentTheme.createTextNodeTo(optionNode,caption);									
			
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
