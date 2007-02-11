/** Corporate theme class extends BaseTheme */
itmill.themes.Demo = itmill.themes.Corporate.extend( {

/** Corporate theme constructor.
 *
 *  @param themeRoot The base URL for theme resources.
 *  @constructor
 *
 */
construct : function(themeRoot) {

	// Call parent constructur (explicit call is necessary)
	arguments.callee.$.construct.call(this,themeRoot);
	this.themeName = "demo";
},

/** Register all renderers to a ajax client.
 *
 * @param client The ajax client instance.
 */
registerTo : function(client) {

	// Register all parent rendering functions
	arguments.callee.$.registerTo.call(this,client);
	
	// Register additional renderers
	client.registerRenderer(this,"orderedlayout","featurebrowser-mainlayout",this.renderFeatureBrowserLayout);
},

/** FeatureBrowser mainlayout. This captures main level ordered layouts */
renderFeatureBrowserLayout : function(renderer,uidl,target,layoutInfo) {

	var theme = renderer.theme;
	var pid = uidl.getAttribute("id");

	// Paint layout if it is not done already
	if (document.getElementById(pid) == null) {
		
		// Try to disable window scrollbars on IEs
	    if (document.all) document.body.scroll = "no";
	    else document.body.style.overflow = "hidden";
		
		// Create container element
		var paintableDiv = renderer.theme.createPaintableElement(renderer,uidl,document.body,layoutInfo);
	 	var div = theme.createElementTo(paintableDiv,"div",null);
		div.id="featurebrowser-mainlayout";
		
		// Build layout with html
		div.innerHTML = "<img src=\""+theme.root+"featurebrowser/img/header.png\"/>"+
			"<div id=\"featurebrowser-features\"'>features</div>"+
			"<div id=\"featurebrowser-demo\"><table border='0' cellpadding='0' cellspacing='0' height='100%' width='100%'><tr><td align='center' valign='middle'><table><tr><td style='text-align: left;' id='featurebrowser-demo-td'> </td></tr></table></td></tr></table></div>"+
			"<div id=\"featurebrowser-tabs\">tabs</div>"+
			"<div id=\"featurebrowser-properties\">properties</div>"+
			"<img id=\"featurebrowser-properties-toggler\" src=\""+theme.root+"featurebrowser/img/show_properties.png\"> </div>"+
			"<div id=\"featurebrowser-control\"><table border='0' height='100%' width='100%'><tr><td width='50%' align='center' valign='middle' id='featurebrowser-control-left'></td><td align='center' width='50%' valign='middle' id='featurebrowser-control-right'></td></tr></table></div>" + 
			"<div id=\"featurebrowser-divider\" ></div>";
		
		// Initialize divider
		var dividerDiv = document.getElementById("featurebrowser-divider");
		dividerDiv.isActive = false;
		dividerDiv.onmousedown = itmill.themes.Demo.prototype.dividerUpdate;
		
		// Resize layout
		window.onresize=itmill.themes.Demo.prototype.recalcFeatureBrowserLayout; 
		theme.recalcFeatureBrowserLayout();
	}
	
	
	// Render UIDL to layout
	
	// Tree
	target = document.getElementById("featurebrowser-features")
	theme.removeAllChildNodes(target);
	renderer.client.renderUIDL(theme.elementByIndex(theme.elementByIndex(uidl.childNodes,0).childNodes,0),target);
	
	// Control
	target = document.getElementById("featurebrowser-control-left")
	theme.removeAllChildNodes(target);
	renderer.client.renderUIDL(theme.elementByIndex(theme.elementByIndex(uidl.childNodes,0).childNodes,1),target);
	target = document.getElementById("featurebrowser-control-right")
	theme.removeAllChildNodes(target);
	renderer.client.renderUIDL(theme.elementByIndex(theme.elementByIndex(uidl.childNodes,0).childNodes,2),target);

	// Demo
	target = document.getElementById("featurebrowser-demo-td")
	theme.removeAllChildNodes(target);
	renderer.client.renderUIDL(theme.elementByIndex(theme.elementByIndex(uidl.childNodes,1).childNodes,0),target);
	
	// Tabs
	target = document.getElementById("featurebrowser-tabs")
	theme.removeAllChildNodes(target);
	renderer.client.renderUIDL(theme.elementByIndex(theme.elementByIndex(uidl.childNodes,1).childNodes,1),target);

	// Properties and property button
	target = document.getElementById("featurebrowser-properties")
	theme.removeAllChildNodes(target);
	var buttonContainer = theme.createElementTo(target,"div",null);
	buttonContainer.style.display='none';
	buttonContainer.propPanelDummy = true;
	var buttonUIDL = theme.elementByIndex(theme.elementByIndex(uidl.childNodes,2).childNodes,0);
	renderer.client.renderUIDL(buttonUIDL,buttonContainer);
	var propsUIDL = theme.elementByIndex(theme.elementByIndex(uidl.childNodes,2).childNodes,1);
	if (propsUIDL != null) {
		renderer.client.renderUIDL(propsUIDL,target);
	}

},

dividerUpdate : function() {
	var dividerDiv = document.getElementById("featurebrowser-divider");
	var div = document.getElementById("featurebrowser-mainlayout");
	dividerDiv.isActive = true;
	div.onmouseup = function() {
		dividerDiv.isActive = false;
		div.onmouseup = null;
		div.onmousemove = null;
	}
	div.onmousemove = function(e) {
		dividerDiv.mouseY = typeof e != 'undefined' ? e.clientY : window.event.clientY;
		itmill.themes.Demo.prototype.recalcFeatureBrowserLayout();
	}
	return false;
},

/** Property hiding button renderer */
renderCheckBox : function(renderer,uidl,target,layoutInfo) {

		arguments.callee.$.renderCheckBox.call(this,renderer,uidl,target,layoutInfo);
		
		// If this is the property hiding button
		if (target.propPanelDummy || target.parentNode.propPanelDummy) {
			var theme = renderer.theme;
			var propertiesDiv = document.getElementById("featurebrowser-properties");
			var buttonDiv = document.getElementById("featurebrowser-properties-toggler");
			buttonDiv.onclick=itmill.themes.Demo.prototype.propertiesButtonClick;
			var buttonVar = theme.elementByIndex(uidl.childNodes,0);
			propertiesDiv.buttonId = buttonVar.getAttribute("id");
			propertiesDiv.buttonState = buttonVar.getAttribute("value");
			buttonDiv.hidePng = theme.root+"featurebrowser/img/hide_properties.png";
			buttonDiv.showPng = theme.root+"featurebrowser/img/show_properties.png";
			propertiesDiv.maxWidth = 265;
			propertiesDiv.targetWidth = propertiesDiv.buttonState == "true" ? propertiesDiv.maxWidth : 0;
			itmill.themes.Demo.prototype.recalcFeatureBrowserLayout();
		}
},

/** Property panel hiding button click event handler */
propertiesButtonClick : function() {
	var propertiesDiv = document.getElementById("featurebrowser-properties");
	propertiesDiv.targetWidth = propertiesDiv.buttonState == "false" ? propertiesDiv.maxWidth : 0;
	itmill.themes.Demo.prototype.recalcFeatureBrowserLayout();
	client.changeVariable(propertiesDiv.buttonId,propertiesDiv.buttonState == "false" ? "true" : "false",true);
},

/** Recalculate feature browser main layout dimensions for all components */
recalcFeatureBrowserLayout : function() {

	// Then animations are needed, this will be reset to true
	var animationNeeded = false;

	// References to all elements
	var mainDiv = document.getElementById("featurebrowser-mainlayout"); 
	var featuresDiv = document.getElementById("featurebrowser-features");
	var demoDiv = document.getElementById("featurebrowser-demo");
	var tabsDiv = document.getElementById("featurebrowser-tabs");
	var propertiesDiv = document.getElementById("featurebrowser-properties");
	var controlDiv = document.getElementById("featurebrowser-control");
	var dividerDiv = document.getElementById("featurebrowser-divider");

	// Logobar height
	var logoBarHeight = 62;

	// Get whole window contents size
	mainDiv.style.width="100%";
	mainDiv.style.height="100%";
	mainDiv.style.position="absolute";
	mainDiv.style.top="0";
	mainDiv.style.left="0";
	var width = mainDiv.offsetWidth;
	var height = mainDiv.offsetHeight;
	if (document.body.offsetWidth > 10) width = document.body.offsetWidth;
	if (document.body.offsetHeight > 10) height = document.body.offsetHeight;
		
	// Recalc features div dimensions
	var featuresWidth = 200;
	var controlHeight = 50;
	featuresDiv.style.top="" + logoBarHeight + "px";
	featuresDiv.style.width="" + featuresWidth + "px";
	featuresDiv.style.height="" + (height-controlHeight-logoBarHeight) + "px";
	
	// Recalc properties div dimensions
	if (typeof propertiesDiv.targetWidth == 'undefined') propertiesDiv.targetWidth = 0;
	var propWidth = Math.floor((propertiesDiv.offsetWidth + propertiesDiv.targetWidth)/2);
	if (Math.abs(propWidth - propertiesDiv.targetWidth) <= 1) propWidth = propertiesDiv.targetWidth;
	if (propWidth >width-featuresWidth) propWidth = width-featuresWidth; 
	if (propWidth < 0) propWidth = 0; 
	if ((propWidth+1) != (propertiesDiv.targetWidth+1)) animationNeeded=true;
	var centerWidth = width - propWidth - featuresWidth - 40;
	propertiesDiv.style.position="absolute";
	propertiesDiv.style.overflow="hidden";
	propertiesDiv.style.top="" + 15 + "px";
	propertiesDiv.style.left="" + (centerWidth + featuresWidth + 40) + "px";
	propertiesDiv.style.width=propWidth + "px";
	propertiesDiv.style.height="" + (height - 15) + "px";	
	var buttonDiv = document.getElementById("featurebrowser-properties-toggler");
	buttonDiv.style.left = "" + (centerWidth + featuresWidth - 20 + 40) + "px"
	if (propWidth == 0) { buttonDiv.src = buttonDiv.showPng; buttonDiv.style.height = "92px"; }
	if (propWidth == propertiesDiv.maxWidth) { buttonDiv.src = buttonDiv.hidePng; buttonDiv.style.height = "23px"; }
	itmill.themes.Demo.prototype.updatePropertiesContentHeight();
	
	// Recalc divider div dimensions
	if (typeof dividerDiv.demoHeight == 'undefined') dividerDiv.demoHeight = Math.floor(height/2);
	if (dividerDiv.isActive) {
		dividerDiv.demoHeight = dividerDiv.mouseY-10 - logoBarHeight;
		if (dividerDiv.demoHeight < 0) dividerDiv.demoHeight = 0;
	} else {
		if (dividerDiv.demoHeight + logoBarHeight > height - 20) dividerDiv.demoHeight = height - 20 - logoBarHeight;
		if (dividerDiv.demoHeight < 0) dividerDiv.demoHeight = 0;
	}
	var dividerHeight = 17;
	dividerDiv.style.position="absolute";
	dividerDiv.style.overflow="hidden";
	dividerDiv.style.top="" + (dividerDiv.demoHeight + logoBarHeight)+ "px";
	dividerDiv.style.left="" + (15+featuresWidth+Math.round((centerWidth-137)/2)) +"px";
	dividerDiv.style.width="" + 137 + "px";
	dividerDiv.style.height="" + dividerHeight + "px";		
	
	// Recalc tabs div dimensions
	tabsDiv.style.position="absolute";
	tabsDiv.style.overflow="hidden";
	tabsDiv.style.top="" + (dividerDiv.demoHeight + dividerHeight + logoBarHeight) + "px";
	tabsDiv.style.left="" + (featuresWidth + 15) + "px";
	tabsDiv.style.width="" + centerWidth + "px";
	tabsDiv.style.height="" + (height - dividerDiv.demoHeight - dividerHeight - logoBarHeight) + "px";	
	itmill.themes.Demo.prototype.updateTabsContentHeight();	
	
	// Recalc demo div dimensions
	demoDiv.style.position="absolute";
	demoDiv.style.overflow="auto";
	demoDiv.style.top="" + logoBarHeight + "px";
	demoDiv.style.left="" + (featuresWidth + 15) + "px";
	demoDiv.style.width="" + centerWidth + "px";
	demoDiv.style.height="" + dividerDiv.demoHeight + "px";	
	
	// Recalc control div dimensions
	// TODO, make floating
	controlDiv.style.position="absolute";
	controlDiv.style.overflow="hidden";
	controlDiv.style.top="" + (height - controlHeight) + "px";
	controlDiv.style.left="0";
	controlDiv.style.width="" + featuresWidth + "px";
	controlDiv.style.height="" + controlHeight + "px";	
	
	if (animationNeeded) {
		setTimeout("itmill.themes.Demo.prototype.recalcFeatureBrowserLayout()",30);
	}
},

/** Adjust tabsheet rendering on main layout */
renderTabSheet : function(renderer,uidl,target,layoutInfo) {
	arguments.callee.$.renderTabSheet.call(this,renderer,uidl,target,layoutInfo);
	if (target.id == "featurebrowser-tabs" || target.parentNode.id ==  "featurebrowser-tabs") 
		itmill.themes.Demo.prototype.updateTabsContentHeight();
},

/** Adjust property panel rendering on main layout */
renderPanel : function(renderer,uidl,target,layoutInfo) {
	arguments.callee.$.renderPanel.call(this,renderer,uidl,target,layoutInfo);
	if (target.id == "featurebrowser-properties" || target.parentNode.id ==  "featurebrowser-properties") 
		itmill.themes.Demo.prototype.updatePropertiesContentHeight();
},

/** Recalculate tabs content width */
updateTabsContentHeight : function() {
	try {
		var tabsDiv = document.getElementById("featurebrowser-tabs");
		var tabsComponent = itmill.themes.Demo.prototype.elementByIndex(tabsDiv.childNodes,0);
		var tabs = itmill.themes.Demo.prototype.elementByIndex(tabsComponent.childNodes,0);
		var content = itmill.themes.Demo.prototype.elementByIndex(tabsComponent.childNodes,1);
		content.style.height="" + (tabsDiv.offsetHeight - tabs.offsetHeight-13) + "px";
		content.style.borderBottom="0";
		content.style.overflow='auto';
		var iframe = content.getElementsByTagName("iframe").item(0);
		if (iframe != null) { 
			content.style.overflow='hidden';
			content.style.padding='0';	
			content.style.height="" + (tabsDiv.offsetHeight - tabs.offsetHeight) + "px";
			iframe.height=content.style.height;
			iframe.style.border='0';
		}
	} catch (e) {}
},

/** Recalculate property panel content width */
updatePropertiesContentHeight : function() {
	try {
		var propsDiv = document.getElementById("featurebrowser-properties");
		var panel = itmill.themes.Demo.prototype.elementByIndex(propsDiv.childNodes,1);
		var content = itmill.themes.Demo.prototype.elementByIndex(itmill.themes.Demo.prototype.elementByIndex(panel.childNodes,0).childNodes,1);
		content.style.height="" + (propsDiv.offsetHeight - 15 - 20) + "px";
		content.style.borderBottom="0";
		content.style.borderRight="0";
		content.style.overflow='auto';
	} catch (e) {}
},

/** Helper method. Get element from node array by index */
elementByIndex : function(nodeArray, index) {
	var i=0;
	while (index>=0) {
		while(nodeArray[i].nodeType != Node.ELEMENT_NODE) i++;
		if (index == 0) return nodeArray[i];
		index--; i++;
	}
}


}) // End of class
