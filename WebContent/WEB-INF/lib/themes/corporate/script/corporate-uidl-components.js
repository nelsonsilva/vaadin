/** Corporate theme class extends BaseTheme */
itmill.toolkit.themes.Corporate = itmill.toolkit.themes.Base.extend( {

/** Corporate theme constructor.
 *
 *  @param themeRoot The base URL for theme resources.
 *  @constructor
 *
 */
constructor : function(themeRoot) {
	this.themeName = "BaseTheme";

	// Store the the root URL
	this.root = themeRoot;
},

/** Register all renderers to a ajax client.
 *
 * @param client The ajax client instance.
 */
registerTo : function(client) {

	// Register renderer functions

}

}) // End of class
