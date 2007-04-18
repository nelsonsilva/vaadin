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

import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Listener interface for UI variable changes. The user communicates with the
 * application using the so-called <i>variables</i>. When the user makes a
 * change using the UI the terminal trasmits the changed variables to the
 * application, and the components owning those variables may then process those
 * changes.
 * </p>
 * 
 * <p>
 * The variable-owning components can be linked with <i>dependency relationships</i>.
 * A dependency between two components means that all variable change events to
 * the depended component will be handled before any such events to the
 * depending component.
 * </p>
 * 
 * <p>
 * For example, the commit button for a text field will depend on that text
 * field. This is because we want to handle any pending changes the user makes
 * to the contents on the text field before we accept the click of the commit
 * button which starts processing the text field contents.
 * </p>
 * 
 * @author IT Mill Ltd.
 * @version
 * @VERSION@
 * @since 3.0
 */
public interface VariableOwner {

	/**
	 * Gets the variable change listeners this <code>VariableOwner</code>
	 * directly depends on. This list does not contain any indirect
	 * dependencies, for example, if A depends on B and B depends on C, the
	 * dependency list of A does not include C.
	 * 
	 * @return Set of <code>VariableOwners</code> this component directly
	 *         depend on, <code>null</code> if this component does not depend
	 *         on anybody.
	 */
	public Set getDirectDependencies();

	/**
	 * Called when one or more variables handled by the implementing class are
	 * changed.
	 * 
	 * @param source
	 *            the Source of the variable change. This is the origin of the
	 *            event. For example in Web Adapter this is the request.
	 * @param variables
	 *            the Mapping from variable names to new variable values.
	 */
	public void changeVariables(Object source, Map variables);

	/**
	 * Makes this <code>VariableOwner</code> depend on the given
	 * <code>VariableOwner</code>. This means that any variable change events
	 * relating to <code>depended</code> must be sent before any such events
	 * that relate to this object.
	 * 
	 * @param depended
	 *            the <code>VariableOwner</code> component who this component
	 *            depends on.
	 */
	public void dependsOn(VariableOwner depended);

	/**
	 * Removes the given component from this component's dependency list. After
	 * the call this component will no longer depend on <code>depended</code>
	 * wdepende direct dependency from the component. Indirect dependencies are
	 * not removed.
	 * 
	 * @param depended
	 *            the component to be removed from this component's dependency
	 *            list.
	 */
	public void removeDirectDependency(VariableOwner depended);

	/**
	 * <p>
	 * Tests if the variable owner is enabled or not. The terminal should not
	 * send any variable changes to disabled variable owners.
	 * </p>
	 * 
	 * @return <code>true</code> if the variable owner is enabled,
	 *         <code>false</code> if not
	 */
	public boolean isEnabled();

	/**
	 * <p>
	 * Tests if the variable owner is in immediate mode or not. Being in
	 * immediate mode means that all variable changes are required to be sent
	 * back from the terminal immediately when they occur.
	 * </p>
	 * 
	 * <p>
	 * <strong>Note:</strong> <code>VariableOwner</code> does not include a
	 * set- method for the immediateness property. This is because not all
	 * VariableOwners wish to offer the functionality. Such VariableOwners are
	 * never in the immediate mode, thus they always return <code>false</code>
	 * in {@link #isImmediate()}.
	 * </p>
	 * 
	 * @return <code>true</code> if the component is in immediate mode,
	 *         <code>false</code> if not.
	 */
	public boolean isImmediate();

	/**
	 * VariableOwner error event.
	 */
	public interface ErrorEvent extends Terminal.ErrorEvent {

		/**
		 * Gets the source VariableOwner.
		 * 
		 * @return the variable owner.
		 */
		public VariableOwner getVariableOwner();

	}
}
