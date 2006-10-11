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

package com.enably.tk.demo.features;

public class FeatureContainers extends Feature {

	protected String getTitle() {
		return "Container Data Model";
	}

	protected String getDescriptionXHTML() {
		return "<p>Container is the most advanced of the data "
			+ "model supported by Millstone. It provides a very flexible "
			+ "way of managing set of items that share common properties. Each "
			+ "item is identified by an item id. "
			+ "Properties can be requested from container with item "
			+ "and property ids. Other way of accessing properties is to first "
			+ "request an item from container and then request its properties "
			+ "from it. </p>"
			+ "<p>Container interface was designed with flexibility and "
			+ "efficiency in mind. It contains inner interfaces for ordering "
			+ "the items sequentially, indexing the items and accessing them "
			+ "hierarchically. Those ordering models provide basis for "
			+ "Table, Tree and Select UI components. As with other data "
			+ "models, the containers support events for notifying about the "
			+ "changes.</p>"
			+ "<p>Set of utilities for converting between container models by "
			+ "adding external indexing or hierarchy into existing containers. "
			+ "In memory containers implementing indexed and hierarchical "
			+ "models provide easy to use tools for setting up in memory data "
			+ "storages. There is even a hierarchical container for direct "
			+ "file system access.</p>";
	}

	protected String getImage() {
		return "containers.jpg";
	}
}
