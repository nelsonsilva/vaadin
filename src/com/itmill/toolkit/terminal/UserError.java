/* *************************************************************************
 
                               IT Mill Toolkit 

               Development of Browser User Interfaces Made Easy

                    Copyright (C) 2000-2006 IT Mill Ltd
                     
   *************************************************************************

   This product is distributed under commercial license that can be found
   from the product package on license.pdf. Use of this product might 
   require purchasing a commercial license from IT Mill Ltd. For guidelines 
   on usage, see licensing-guidelines.html

   *************************************************************************
   
   For more information, contact:
   
   IT Mill Ltd                           phone: +358 2 4802 7180
   Ruukinkatu 2-4                        fax:   +358 2 4802 7181
   20540, Turku                          email:  info@itmill.com
   Finland                               company www: www.itmill.com
   
   Primary source for information and releases: www.itmill.com

   ********************************************************************** */

package com.itmill.toolkit.terminal;

/** User error is a controlled error occurred in application. User errors
 * are occur in normal usage of the application and guide the user.
 *
 * @author IT Mill Ltd.
 * @version @VERSION@
 * @since 3.0
 */
public class UserError implements ErrorMessage {

	/** Content mode, where the error contains only plain text. 
	 */
	public static final int CONTENT_TEXT = 0;

	/** Content mode, where the error contains preformatted text.
	 */
	public static final int CONTENT_PREFORMATTED = 1;

	/** Formatted content mode, where the contents is XML restricted to the
	 * UIDL 1.0 formatting markups.
	 */
	public static final int CONTENT_UIDL = 2;

	/** Content mode */
	private int mode = CONTENT_TEXT;

	/** Message in content mode */
	private String msg;

	/** Error level */
	private int level = ErrorMessage.ERROR;

	/** Create a textual error message of level ERROR.
	 * 
	 * @param textErrorMessage The text of the error message. 
	 */
	public UserError(String textErrorMessage) {
		this.msg = textErrorMessage;
	}

	/** Create error message with level and content mode.
	 */
	public UserError(String message, int contentMode, int errorLevel) {

		// Check the parameters
		if (contentMode < 0 || contentMode > 2)
			throw new java.lang.IllegalArgumentException(
				"Unsupported content mode: " + contentMode);

		this.msg = message;
		this.mode = contentMode;
		this.level = errorLevel;
	}

	/* Documenten in interface */
	public int getErrorLevel() {
		return level;
	}

	/* Documenten in interface */
	public void addListener(RepaintRequestListener listener) {
	}

	/* Documenten in interface */
	public void removeListener(RepaintRequestListener listener) {
	}

	/* Documenten in interface */
	public void requestRepaint() {
	}

	/* Documenten in interface */
	public void paint(PaintTarget target) throws PaintException {

		target.startTag("error");

		// Error level
		if (level >= ErrorMessage.SYSTEMERROR)
			target.addAttribute("level", "system");
		else if (level >= ErrorMessage.CRITICAL)
			target.addAttribute("level", "critical");
		else if (level >= ErrorMessage.ERROR)
			target.addAttribute("level", "error");
		else if (level >= ErrorMessage.WARNING)
			target.addAttribute("level", "warning");
		else
			target.addAttribute("level", "info");

		// Paint the message
		switch (mode) {
			case CONTENT_TEXT :
				target.addText(msg);
				break;
			case CONTENT_UIDL :
				target.addUIDL(msg);
				break;
			case CONTENT_PREFORMATTED :
				target.startTag("pre");
				target.addText(msg);
				target.endTag("pre");
		}

		target.endTag("error");
	}

	/* Documenten in interface */
	public void requestRepaintRequests() {
	}
	
	/* Documented in superclass */
	public String toString() {
		return msg;
	}

}
