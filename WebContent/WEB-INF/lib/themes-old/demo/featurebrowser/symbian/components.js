// This is a special version of the feature browser demo layout customized for limited
// screensize of symbian based mobile phones

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
		
		// Create container element
		var paintableDiv = renderer.theme.createPaintableElement(renderer,uidl,document.body,layoutInfo);
	 	var div = theme.createElementTo(paintableDiv,"div",null);
		div.id="featurebrowser-mainlayout";
		
		// Build layout with html
		div.innerHTML = "<img src=\""+theme.root+"featurebrowser/symbian/header.png\"/>"+
			"<table><tr><td valign='top' id=\"featurebrowser-features\"'></td><td  valign='top'>"+
			"<div id=\"featurebrowser-demo\"><table border='0' cellpadding='0' cellspacing='0' height='100px' width='100%'><tr><td align='center' valign='middle'><table><tr><td style='text-align: left;' id='featurebrowser-demo-td'> </td></tr></table></td></tr></table></div>"+
			"<div id=\"featurebrowser-tabs\"></div>"+
			"<div id=\"featurebrowser-properties\"></div>"+
			"</td></tr></table>";			
	}
	

	// Render UIDL to layout
	
	// Tree
	target = document.getElementById("featurebrowser-features")
	theme.removeAllChildNodes(target);
	renderer.client.renderUIDL(theme.elementByIndex(uidl.childNodes,0),target);
	
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
	renderer.client.renderUIDL(theme.elementByIndex(uidl.childNodes,2),target);
},


/** Helper method. Get element from node array by index */
elementByIndex : function(nodeArray, index) {
	if (typeof nodeArray == 'undefined' || nodeArray == null) return null;
	var i=0;
	while (index>=0 && i < nodeArray.length) {
		while(i < nodeArray.length && nodeArray[i].nodeType != Node.ELEMENT_NODE) i++;
		if (index == 0) return nodeArray[i];
		index--; i++;
	}
	return null;
}


}) // End of class
