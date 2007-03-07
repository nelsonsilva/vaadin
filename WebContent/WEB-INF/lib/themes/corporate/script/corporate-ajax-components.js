/** Corporate theme class extends BaseTheme */
itmill.themes.Corporate = itmill.themes.Base.extend( {

/** Corporate theme constructor.
 *
 *  @param themeRoot The base URL for theme resources.
 *  @constructor
 *
 */
construct : function(themeRoot) {

	// Call parent constructur (explicit call is necessary)
	arguments.callee.$.construct.call(this,themeRoot);
	this.themeName = "corporate";
},

/** Register all renderers to a ajax client.
 *
 * @param client The ajax client instance.
 */
registerTo : function(client) {

	// Register all parent rendering functions
	arguments.callee.$.registerTo.call(this,client);
	
	// Register additional renderers
	client.registerRenderer(this,"tabsheet",null,this.renderTabSheet);
	client.registerRenderer(this,"tree",null,this.renderTree);
	
},

renderTabSheet : function(renderer,uidl,target,layoutInfo) {

			var theme = renderer.theme;
			
			// Create container element
			var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
			if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

			// Create default header
			var caption = renderer.theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
			theme.addCSSClass(caption, "tabsheetcaption");
			
			// If no actual caption, remove description popup listener
			if(caption && caption.className.indexOf("hide") > -1) {
				this.removeEventListener(div,undefined,null,"descriptionPopup");
			}

			
			//  Render tabs
			var tabs_container = theme.createElementTo(div,"div","tabs-container");
			var table = theme.createElementTo(tabs_container,"table","tabs");
			table.cellSpacing = 0;
			var tabs = theme.createElementTo(theme.createElementTo(table, "tbody"),"tr");
			var varId = theme.getVariableElement(uidl,"string","selected").getAttribute("id");
			
			var tabNodes = theme.getChildElements(uidl,"tabs");
			if (tabNodes != null && tabNodes.length >0)  tabNodes = theme.getChildElements(tabNodes[0],"tab");
			var selectedTabNode = null;
			
			var space = theme.createElementTo(tabs,"td","tab-space");
			space.innerHTML = "&nbsp;"
			
			if (tabNodes != null && tabNodes.length >0) {
				for (var i=0; i < tabNodes.length; i++) {
					var tabNode = tabNodes[i];
					var tab = theme.createElementTo(tabs,"td");
					var key = tabNode.getAttribute("key");
					var iconUrl =  tabNode.getAttribute("icon");
					if (iconUrl && iconUrl.indexOf("theme://") == 0) {
		   				iconUrl = (theme.iconRoot != null ? theme.iconRoot : theme.root) 
		    				+ iconUrl.substring(8);
					}		
					if (tabNode.getAttribute("selected") == "true") {
						theme.addCSSClass(tab,"tab-on");
						selectedTabNode = tabNode;
					} else if (tabNode.getAttribute("disabled") == "true" 
								|| uidl.getAttribute("disabled") == "true"
								|| uidl.getAttribute("readonly") == "true") {
						theme.setCSSClass(tab,"tab disabled inline");
					} else {
						theme.setCSSClass(tab,"tab clickable");
						theme.addAddClassListener(theme,this,tab,"mouseover","over",tab);
						theme.addRemoveClassListener(theme,this,tab,"mouseout","over",tab);
						theme.addSetVarListener(theme,this,tab,"click",varId,key,true);
						theme.addPreventSelectionListener(theme,this,tab,"mousedown");
					}
					
					// Icon
					if (iconUrl) {
						tab.innerHTML = "<IMG src=\""+iconUrl+"\" class=\"icon\" />" + tabNode.getAttribute("caption");
					} else {
						tab.innerHTML = tabNode.getAttribute("caption");
					}
				
					if(i < tabNodes.length-1) {
						space = theme.createElementTo(tabs,"td","tab-space");
						space.innerHTML = "&nbsp;"
					}
				}
				
				// For visual needs
				var last = theme.createElementTo(tabs,"td","tab-last");
				last.innerHTML = "&nbsp;";
				last.width = "100%";
			}
			
			// Render content (IE renderbug need three)
			var content = theme.createElementTo(div,"div","tab-content");
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
	
	// If no actual caption, remove description popup listener from the container
	if(caption && caption.className.indexOf("hide") > -1) {
		this.removeEventListener(div,undefined,null,"descriptionPopup");
	}

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
	// TODO don't render in-place, use generic popup-container (on-the-fly rendering)
	
	var hasChildren = false;
	for (var i=0; i<node.childNodes.length; i++) {
		var childNode = node.childNodes[i];
		if(!hasChildren && (childNode.nodeName == "node" || childNode.nodeName == "leaf")) {
			hasChildren = true;
		}
		if (!disabled && !readonly && childNode.nodeName == "al" ) {
			theme.renderActionPopup(renderer,childNode,li,actions,actionVar,key);
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
}

}); // End of class

/**
 * Defines headers and footers height.
 * 
 * NOTE! CSS file must also comfort to this value
 */
itmill.themes.Base.TkWindow.prototype.HEADER_HEIGHT = 20;

/**
 * Defines windows border width.
 * 
 * NOTE! CSS file must also comfort to this value
 */
itmill.themes.Base.TkWindow.prototype.BORDER_WIDTH = 2;

