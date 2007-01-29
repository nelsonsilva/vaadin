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
},

renderTabSheet : function(renderer,uidl,target,layoutInfo) {

			var theme = renderer.theme;
			
			// Create container element
			var div = renderer.theme.createPaintableElement(renderer,uidl,target,layoutInfo);
			if (uidl.getAttribute("invisible")) return; // Don't render content if invisible

			// Create default header
			var caption = renderer.theme.renderDefaultComponentHeader(renderer,uidl,div,layoutInfo);
			theme.addCSSClass(caption, "tabsheetcaption");
			
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
						theme.addAddClassListener(theme,client,tab,"mouseover","over",tab);
						theme.addRemoveClassListener(theme,client,tab,"mouseout","over",tab);
						theme.addSetVarListener(theme,client,tab,"click",varId,key,true);
						theme.addPreventSelectionListener(theme,client,tab,"mousedown");
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
}

}) // End of class
