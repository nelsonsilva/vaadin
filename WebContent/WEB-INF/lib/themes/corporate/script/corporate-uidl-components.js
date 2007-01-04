
/** Corporate theme constructor.
 *
 *  @param themeRoot The base URL for theme resources.
 *  @constructor
 *
 */
function CorporateTheme(themeRoot) {
	this.themeName = "BaseTheme";

	// Store the the root URL
	this.root = themeRoot;
}

/** Register all renderers to a ajax client.
 *
 * @param client The ajax client instance.
 */
CorporateTheme.prototype.registerTo = function(client) {

	// Register renderer functions

};
