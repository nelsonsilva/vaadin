/* *************************************************************************
 
   								Millstone(TM) 
   				   Open Sourced User Interface Library for
   		 		       Internet Development with Java

             Millstone is a registered trademark of IT Mill Ltd
                  Copyright (C) 2000-2005 IT Mill Ltd
                     
   *************************************************************************

   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   license version 2.1 as published by the Free Software Foundation.

   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with this library; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

   *************************************************************************
   
   For more information, contact:
   
   IT Mill Ltd                           phone: +358 2 4802 7180
   Ruukinkatu 2-4                        fax:  +358 2 4802 7181
   20540, Turku                          email: info@itmill.com
   Finland                               company www: www.itmill.com
   
   Primary source for MillStone information and releases: www.millstone.org

   ********************************************************************** */

package com.enably.tk.terminal;

/** Interface to be implemented by components wishing to
 *  display some object that may be dynamically 
 *  resized during runtime.
 *
 * @author IT Mill Ltd.
 * @version @VERSION@
 * @since 3.0
 */
public interface Sizeable {

	/** Unit code representing pixels. */
	public static final int UNITS_PIXELS = 0;

	/** Unit code representing points (1/72nd of an inch). */
	public static final int UNITS_POINTS = 1;

	/** Unit code representing picas (12 points). */
	public static final int UNITS_PICAS = 2;

	/** Unit code representing the font-size of the relevant font. */
	public static final int UNITS_EM = 3;

	/** Unit code representing the x-height of the relevant font. */
	public static final int UNITS_EX = 4;

	/** Unit code representing millimetres. */
	public static final int UNITS_MM = 5;

	/** Unit code representing centimetres. */
	public static final int UNITS_CM = 6;

	/** Unit code representing inches. */
	public static final int UNITS_INCH = 7;

	/** Unit code representing in percentage of the containing element 
	 *  defined by terminal. 
	 */
	public static final int UNITS_PERCENTAGE = 8;

	/** Textual representations of units symbols.
	 *  Supported units and their symbols are:
	 *  <ul>
	 *  <li><code>UNITS_PIXELS</code>: "" (unit is omitted for pixels)</li>
	 *  <li><code>UNITS_POINTS</code>: "pt"</li>
	 *  <li><code>UNITS_PICAS</code>: "pc"</li>
	 *  <li><code>UNITS_EM</code>: "em"</li>
	 *  <li><code>UNITS_EX</code>: "ex"</li>
	 *  <li><code>UNITS_MM</code>: "mm"</li>
	 *  <li><code>UNITS_CM</code>. "cm"</li>
	 *  <li><code>UNITS_INCH</code>: "in"</li>
	 *  <li><code>UNITS_PERCENTAGE</code>: "%"</li>
	 *  </ul>
	 *  These can be used like <code>Sizeable.UNIT_SYMBOLS[UNITS_PIXELS]</code>.
	 */
	public static final String[] UNIT_SYMBOLS =
		{ "", "pt", "pc", "em", "ex", "mm", "cm", "in", "%" };

	/** Get width of the object. Negative number implies unspecified size
	 * (terminal is free to set the size).
	 * @return width of the object in units specified by widthUnits property.
	 */
	public int getWidth();

	/** Set width of the object. Negative number implies unspecified size
	 * (terminal is free to set the size).
	 * @param width width of the object in units specified by widthUnits property.
	 */
	public void setWidth(int width);

	/** Get height of the object. Negative number implies unspecified size
	 * (terminal is free to set the size).
	 * @return height of the object in units specified by heightUnits property.
	 */
	public int getHeight();

	/** Set height of the object. Negative number implies unspecified size
	 * (terminal is free to set the size).
	 * @param height height of the object in units specified by heightUnits property.
	 */
	public void setHeight(int height);

	/** Get width property units. 
	 * @return units used in width property.
	 */
	public int getWidthUnits();

	/** Set width property units. 
	 * @param units units used in width property.
	 */
	public void setWidthUnits(int units);

	/** Get height property units. 
	 * @return units used in height property.
	 */
	public int getHeightUnits();

	/** Set height property units. 
	 * @param units units used in height property.
	 */
	public void setHeightUnits(int units);

}
