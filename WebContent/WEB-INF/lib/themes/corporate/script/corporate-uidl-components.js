/** Corporate theme class extends BaseTheme */
itmill.themes.Corporate = itmill.themes.Base.extend( {

/** Corporate theme constructor.
 *
 *  @param themeRoot The base URL for theme resources.
 *  @constructor
 *
 */
constructor : function(themeRoot) {

	// Call parent constructur (explicit call is necessary)
	this.$.constructor(themeRoot);
	this.themeName = "corporate";
},

/** Register all renderers to a ajax client.
 *
 * @param client The ajax client instance.
 */
registerTo : function(client) {
	// Register all parent rendering functions
	this.$.registerTo(client);
	
	// Register additional renderers
	// TODO add corporate theme implementation here
}

}) // End of class
