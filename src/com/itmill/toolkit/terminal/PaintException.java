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

import java.io.IOException;

/** 
 * <code>PaintExcepection</code> is thrown if painting of a component fails.
 *
 * @author IT Mill Ltd.
 * @version @VERSION@
 * @since 3.0
 */
public class PaintException extends IOException {
    
    /**
     * Serial generated by eclipse.
     */
    private static final long serialVersionUID = 3762535607221891897L;

    /** 
     * Constructs an instance of <code>PaintExeception</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PaintException(String msg) {
        super(msg);
    }
    
    /** 
     * Constructs an instance of <code>PaintExeception</code> from IOException.
     * @param exception the original exception.
     */
    public PaintException(IOException exception) {
        super(exception.getMessage());
    }
}
