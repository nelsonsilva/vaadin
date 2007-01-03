/* *************************************************************************
 
                               IT Mill Toolkit 

               Development of Browser User Intarfaces Made Easy

                    Copyright (C) 2000-2006 IT Mill Ltd
                     
   *************************************************************************

   This product is distributed under commercial license that can be found
   from the product package on license/license.txt. Use of this product might 
   require purchasing a commercial license from IT Mill Ltd. For guidelines 
   on usage, see license/licensing-guidelines.html

   *************************************************************************
   
   For more information, contact:
   
   IT Mill Ltd                           phone: +358 2 4802 7180
   Ruukinkatu 2-4                        fax:   +358 2 4802 7181
   20540, Turku                          email:  info@itmill.com
   Finland                               company www: www.itmill.com
   
   Primary source for information and releases: www.itmill.com

   ********************************************************************** */

package com.itmill.toolkit.event;

import java.util.EventObject;
import java.util.LinkedList;
import java.util.Iterator;
import java.lang.reflect.Method;

/** Event router class implementing the inheritable event
 * listening model. For more information on the event model see the
 * {@link com.itmill.toolkit.event package documentation}.
 *
 * @author IT Mill Ltd.
 * @version @VERSION@
 * @since 3.0
 */
public class EventRouter implements MethodEventSource {
    
    /** List of registered listeners. */
    private LinkedList listenerList =  null;
    
	/* Registers a new listener with the specified activation method to
	 * listen events generated by this component.
	 * Don't add a JavaDoc comment here, we use the default documentation
	 * from implemented interface.
	 */
    public void addListener(Class eventType, Object object, Method method) {
        
        if (listenerList == null)
            listenerList = new LinkedList();
        
        listenerList.add(new ListenerMethod(eventType, object, method));
    }
    
	/* Registers a new listener with the specified named activation method
	 * to listen events generated by this component.
	 * Don't add a JavaDoc comment here, we use the default documentation
	 * from implemented interface.
	 */
    public void addListener(Class eventType, Object object, String methodName) {
        
        if (listenerList == null)
            listenerList = new LinkedList();
        
        listenerList.add(new ListenerMethod(eventType, object, methodName));
    }
    
	/* Removes all registered listeners matching the given parameters.
	 * Don't add a JavaDoc comment here, we use the default documentation
	 * from implemented interface.
	 */
    public void removeListener(Class eventType, Object target) {
        
        if (listenerList != null) {
            Iterator i = listenerList.iterator();
            while (i.hasNext()) {
                try {
                    ListenerMethod lm = (ListenerMethod) i.next();
                    if (lm.matches(eventType,target))
                        i.remove();
                } catch (java.lang.ClassCastException e) {
                    // Class cast exceptions are ignored
                }
            }
        }
    }
    
    /* Removes the event listener methods matching the given given
     * paramaters.
	 * Don't add a JavaDoc comment here, we use the default documentation
	 * from implemented interface.
	 */
    public void removeListener(Class eventType, Object target, Method method) {
        
        if (listenerList != null) {
            Iterator i = listenerList.iterator();
            while (i.hasNext()) {
                try {
                    ListenerMethod lm = (ListenerMethod) i.next();
                    if (lm.matches(eventType,target,method))
                        i.remove();
                } catch (java.lang.ClassCastException e) {
                    // Class cast exceptions are ignored
                }
            }
        }
    }

    /* Removes the event listener method matching the given given
     * paramaters.
	 * Don't add a JavaDoc comment here, we use the default documentation
	 * from implemented interface.
	 */
    public void removeListener(Class eventType, Object target, String methodName) {
        
        // Find the correct method
        Method[] methods = target.getClass().getMethods();
        Method method = null;
        for (int i=0; i<methods.length; i++)
            if (methods[i].getName().equals(methodName))
                method = methods[i];
        if (method == null) throw new IllegalArgumentException();
        
        // Remove the listeners
        if (listenerList != null) {
            Iterator i = listenerList.iterator();
            while (i.hasNext()) {
                try {
                    ListenerMethod lm = (ListenerMethod) i.next();
                    if (lm.matches(eventType,target,method))
                        i.remove();
                } catch (java.lang.ClassCastException e) {
                    // Class cast exceptions are ignored
                }
            }
        }
    }
    
    /** Remove all listeners from event router */
    public void removeAllListeners() {
    	listenerList = null;	
    }
    
    /** Send an event to all registered listeners. The listeners will decide
     * if the activation method should be called or not.
     * 
     * @param event Event to be sent to all listeners
     */
    public void fireEvent(EventObject event) {
        
        // It is not necessary to send any events if there are no listeners
        if (listenerList != null) {
            
            // Send the event to all listeners. The listeners themselves
            // will filter out unwanted events.            
            Iterator i = new LinkedList(listenerList).iterator();
            while(i.hasNext()) ((ListenerMethod)i.next()).receiveEvent(event);
        }
    }
}
