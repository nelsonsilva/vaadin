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

renderFeatureBrowserLayout : function(renderer,uidl,target,layoutInfo) {

	var theme = renderer.theme;
	var pid = uidl.getAttribute("id");

	// Paint layout if needed
	if (document.getElementById(pid) == null) {
		// Create container element
		var paintableDiv = renderer.theme.createPaintableElement(renderer,uidl,document.body,layoutInfo);
	 	var div = theme.createElementTo(paintableDiv,"div",null);
		div.id="featurebrowser-mainlayout";
		
		// Build layout
		div.innerHTML = "<div id=\"featurebrowser-features\" style='background: white;'>features</div>"+
		"<div id=\"featurebrowser-demo\"><table border='0' cellpadding='0' cellspacing='0' height='100%' width='100%'><tr><td align='center' valign='middle' id='featurebrowser-demo-td'> </td></tr></table></div>"+
		"<div id=\"featurebrowser-tabs\" style='background: white'>tabs</div>"+
		"<div id=\"featurebrowser-properties\" style='border-left: 1px solid #909090; width: 10px;'>properties</div>"+
		"<div id=\"featurebrowser-properties-toggler\" style='border: 1px solid #909090; width: 10px; height: 10px; background-color: gray; position: absolute; top: 20px; right: 10px;'> </div>"+
		"<div id=\"featurebrowser-control\" style='border-right: 1px solid #909090; border-top: 1px solid #909090;'><table border='0' height='100%' width='100%'><tr><td width='50%' align='center' valign='middle' id='featurebrowser-control-left'></td><td align='center' width='50%' valign='middle' id='featurebrowser-control-right'></td></tr></table></div>" + 
		"<div id=\"featurebrowser-divider\" style='background: gray;'> </div>";
		
		// Properties hiddening
		var propertiesDiv = document.getElementById("featurebrowser-properties");
		propertiesDiv.targetWidth = 0;
		
		// Divider resize
		var dividerDiv = document.getElementById("featurebrowser-divider");
		dividerDiv.isActive = false;
		dividerDiv.onmousedown = function() {
			dividerDiv.isActive = true;
			// Hilight divider
			dividerDiv.style.background="black";
			div.onmouseup = function() {
				dividerDiv.isActive = false;
				// Reset divider to its original color
				dividerDiv.style.background="gray";
				div.onmouseup = null;
				div.onmousemove = null;
			}
			div.onmousemove = function(e) {
				dividerDiv.mouseY = typeof e != 'undefined' ? e.clientY : window.event.clientY;
				itmill.themes.Demo.prototype.recalcFeatureBrowserLayout()
			}
			return false;
		}
		
		// Resize layout
		var prevWindowResizeFunc = window.onresize;
		window.onresize=function () { 
			theme.recalcFeatureBrowserLayout(); 
			if (prevWindowResizeFunc != null) prevWindowResizeFunc.call(this);
		};
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

	// Props
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

renderCheckBox : function(renderer,uidl,target,layoutInfo) {

		arguments.callee.$.renderCheckBox.call(this,renderer,uidl,target,layoutInfo);
		
		
		if (target.propPanelDummy || target.parentNode.propPanelDummy) {
			var propertiesMaxWidth = 265;
			var theme = renderer.theme;
			var propertiesDiv = document.getElementById("featurebrowser-properties");
			var buttonDiv = document.getElementById("featurebrowser-properties-toggler");
			buttonDiv.onclick=function() {			
				propertiesDiv.targetWidth = propertiesDiv.buttonState == "false" ? propertiesMaxWidth : 0;
				theme.recalcFeatureBrowserLayout()
				renderer.client.changeVariable(propertiesDiv.buttonId,propertiesDiv.buttonState == "false" ? "true" : "false",true);
			}	
			var buttonVar = theme.elementByIndex(uidl.childNodes,0);
			propertiesDiv.buttonId = buttonVar.getAttribute("id");
			propertiesDiv.buttonState = buttonVar.getAttribute("value");
			propertiesDiv.targetWidth = propertiesDiv.buttonState == "true" ? propertiesMaxWidth : 0;
			theme.recalcFeatureBrowserLayout();
		}
},

recalcFeatureBrowserLayout : function() {

		var animationNeeded = false;

		var mainDiv = document.getElementById("featurebrowser-mainlayout"); 
		var featuresDiv = document.getElementById("featurebrowser-features");
		var demoDiv = document.getElementById("featurebrowser-demo");
		var tabsDiv = document.getElementById("featurebrowser-tabs");
		var propertiesDiv = document.getElementById("featurebrowser-properties");
		var controlDiv = document.getElementById("featurebrowser-control");
		var dividerDiv = document.getElementById("featurebrowser-divider");

		// Logobar
		var logoBarHeight = 40; // TODO ADJUST THIS TO LOGO HEIGHT

		document.body.scroll='no';

		// Recalc main div dimensions
		mainDiv.style.position="absolute";
		mainDiv.style.overflow="hidden";
		mainDiv.style.top="0";
		mainDiv.style.left="0";
		mainDiv.style.width="100%";
		mainDiv.style.height="100%";
		var width = mainDiv.offsetWidth;
		var height = mainDiv.offsetHeight;
		if (height < 400 || width < 600) {
			height = 400;
			width = 600;
			if (document.body.offsetWidth > width) width = document.body.offsetWidth;
			// TODO This does work in IE6, but not in IE7 ?!
			if (document.body.offsetHeight > height) height = document.body.offsetHeight;
			mainDiv.style.width="" + width + "px";
			mainDiv.style.height="" + height + "px";
		}
		
		// Recalc features div dimensions
		var featuresWidth = 200;
		var controlHeight = 50;
		featuresDiv.style.position="absolute";
		featuresDiv.style.overflow="scroll";
		featuresDiv.style.top="" + logoBarHeight + "px";
		featuresDiv.style.left="0";
		featuresDiv.style.width="" + featuresWidth + "px";
		featuresDiv.style.height="" + (height-controlHeight-logoBarHeight) + "px";
		
		// Recalc properties div dimensions
		var propWidth = Math.floor((propertiesDiv.offsetWidth + propertiesDiv.targetWidth)/2);
		if (propWidth >width-featuresWidth) propWidth = width-featuresWidth; 
		if (propWidth < 0) propWidth = 0; 
		if (propWidth != propertiesDiv.targetWidth) animationNeeded=true;
		var centerWidth = width - propWidth - featuresWidth;
		propertiesDiv.style.position="absolute";
		propertiesDiv.style.overflow=propWidth>50?"auto":"hidden";
		propertiesDiv.style.top="" + logoBarHeight + "px";
		propertiesDiv.style.left="" + (centerWidth + featuresWidth) + "px";
		propertiesDiv.style.width=propWidth + "px";
		propertiesDiv.style.height="" + (height - logoBarHeight) + "px";	
		
		// Recalc divider div dimensions
		if (typeof dividerDiv.demoHeight == 'undefined') dividerDiv.demoHeight = Math.floor(height/2);
		if (dividerDiv.isActive) {
			dividerDiv.demoHeight = dividerDiv.mouseY-7 - logoBarHeight;
		} 
		var dividerHeight = 8;
		dividerDiv.style.position="absolute";
		dividerDiv.style.overflow="hidden";
		dividerDiv.style.top="" + (dividerDiv.demoHeight + logoBarHeight)+ "px";
		dividerDiv.style.left="" + featuresWidth +"px";
		dividerDiv.style.width="" + centerWidth + "px";
		dividerDiv.style.height="" + dividerHeight + "px";		
		
		// Recalc tabs div dimensions
		tabsDiv.style.position="absolute";
		tabsDiv.style.overflow="auto";
		tabsDiv.style.top="" + (dividerDiv.demoHeight + dividerHeight + logoBarHeight) + "px";
		tabsDiv.style.left="" + featuresWidth + "px";
		tabsDiv.style.width="" + centerWidth + "px";
		tabsDiv.style.height="" + (height - dividerDiv.demoHeight - dividerHeight - logoBarHeight) + "px";		
		
		// Recalc demo div dimensions
		demoDiv.style.position="absolute";
		demoDiv.style.overflow="auto";
		demoDiv.style.top="" + logoBarHeight + "px";
		demoDiv.style.left="" + featuresWidth + "px";
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

renderTabSheet : function(renderer,uidl,target,layoutInfo) {
	arguments.callee.$.renderTabSheet.call(this,renderer,uidl,target,layoutInfo);
	if (target.id == "featurebrowser-tabs" || target.parentNode.id ==  "featurebrowser-tabs") {
		var content = renderer.theme.elementByIndex(
				target.id == "featurebrowser-tabs" ? 
				renderer.theme.elementByIndex(target.childNodes,0).childNodes : 
				target.childNodes,1);
		content.style.border='0';
	}
},

elementByIndex : function(nodeArray, index) {
	var i=0;
	while (index>=0) {
		while(nodeArray[i].nodeType != Node.ELEMENT_NODE) i++;
		if (index == 0) return nodeArray[i];
		index--; i++;
	}
}


}) // End of class
