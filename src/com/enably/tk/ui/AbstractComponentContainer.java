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
   
package com.enably.tk.ui;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Iterator;

/** Extension to {@link AbstractComponent} that defines the default
 * implementation for the methods in {@link ComponentContainer}. Basic UI
 * components that need to contain other components inherit this class to
 * easily qualify as a MillStone component container.
 * 
 * @author  IT Mill Ltd
 * @version @VERSION@
 * @since 3.0
 */
public abstract class AbstractComponentContainer 
extends AbstractComponent implements ComponentContainer {

	/** Constructs a new component container. */
	public AbstractComponentContainer() {
		super();
	}

    /** Removes all components from the container. This should probably be
     * reimplemented in extending classes for a more powerfu
     * implementation. 
     */
    public void removeAllComponents() {
        LinkedList l = new LinkedList();

		// Add all components
        for (Iterator i = getComponentIterator(); i.hasNext();)
        	l.add(i.next());
        	
        // Remove all component
        for (Iterator i = l.iterator(); i.hasNext();)
        	removeComponent((Component)i.next());
    }
    
    /* Moves all components from an another container into this container.
	 * Don't add a JavaDoc comment here, we use the default documentation
	 * from implemented interface.
	 */
	public void moveComponentsFrom(ComponentContainer source) {
		LinkedList components = new LinkedList();
		for (Iterator i = source.getComponentIterator(); i.hasNext();) 
			components.add(i.next());
		
		for (Iterator i = components.iterator(); i.hasNext();) {
			Component c = (Component) i.next();
			source.removeComponent(c);
			addComponent(c);
		}
    }
    
	/** Notifies all contained components that the container is attached to
	 * a window.
	 * 
	 * @see com.enably.tk.ui.Component#attach()
	 */
	public void attach() {
		super.attach();
		
		for (Iterator i = getComponentIterator(); i.hasNext();)
			((Component)i.next()).attach();
	}

	/** Notifies all contained components that the container is detached
	 * from a window.
	 * 
	 * @see com.enably.tk.ui.Component#detach()
	 */
	public void detach() {
		super.detach();
		
		for (Iterator i = getComponentIterator(); i.hasNext();)
			((Component)i.next()).detach();
	}


	/* Events ************************************************************ */

	private static final Method COMPONENT_ATTACHED_METHOD;
	private static final Method COMPONENT_DETACHED_METHOD;

	static {
		try {
			COMPONENT_ATTACHED_METHOD =
				ComponentAttachListener.class.getDeclaredMethod(
					"componentAttachedToContainer",
					new Class[] { ComponentAttachEvent.class });
			COMPONENT_DETACHED_METHOD =
				ComponentDetachListener.class.getDeclaredMethod(
					"componentDetachedFromContainer",
					new Class[] { ComponentDetachEvent.class });
		} catch (java.lang.NoSuchMethodException e) {
			// This should never happen
			throw new java.lang.RuntimeException();
		}
	}

	/* documented in interface */
	public void addListener(ComponentAttachListener listener) {
		addListener(ComponentContainer.ComponentAttachEvent.class, listener, COMPONENT_ATTACHED_METHOD);
	}

	/* documented in interface */
	public void addListener(ComponentDetachListener listener) {
		addListener(ComponentContainer.ComponentDetachEvent.class, listener, COMPONENT_DETACHED_METHOD);
	}

	/* documented in interface */
	public void removeListener(ComponentAttachListener listener) {
		removeListener(ComponentContainer.ComponentAttachEvent.class, listener, COMPONENT_ATTACHED_METHOD);
	}

	/* documented in interface */
	public void removeListener(ComponentDetachListener listener) {
		removeListener(ComponentContainer.ComponentDetachEvent.class, listener, COMPONENT_DETACHED_METHOD);
	}

	/** Fire component attached event. This should be called by the addComponent 
	 * methods after the component have been added to this container. 
	 * @param component The component that has been added to this container.
	 */
	protected void fireComponentAttachEvent(Component component) {
		fireEvent(new ComponentAttachEvent(this,component));
	}

	/** Fire component detached event. This should be called by the removeComponent 
	 * methods after the component have been removed from this container. 
	 * @param component The component that has been removed from this container.
	 */
	protected void fireComponentDetachEvent(Component component) {
		fireEvent(new ComponentAttachEvent(this,component));
	}
	
	/** This only implements the events and component parent calls. The extending
	 * classes must implement component list maintenance and call this method 
	 * after component list maintenance.
	 * @see com.enably.tk.ui.ComponentContainer#addComponent(Component)
	 */
	public void addComponent(Component c) {
		c.setParent(this);
		if (getApplication() != null)
			c.attach();
		fireComponentAttachEvent(c);
	}

	/** This only implements the events and component parent calls. The extending
	 * classes must implement component list maintenance and call this method 
	 * before component list maintenance.
	 * @see com.enably.tk.ui.ComponentContainer#removeComponent(Component)
	 */
	public void removeComponent(Component c) {
		if (getApplication() != null)
			c.detach();
		c.setParent(null);
		fireComponentDetachEvent(c);
	}
}
