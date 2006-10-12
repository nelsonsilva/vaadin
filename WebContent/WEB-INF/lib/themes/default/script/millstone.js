// ==========================================================================
// ==========================================================================
//
// Variable handling functions
// 
// ==========================================================================

// Global objects
var Millstone = new MillstoneUtils();



// --------------------------------------------------------------------------
// Submit the millstone form.
//
// Params:
//
// Returns nothing
// --------------------------------------------------------------------------

MillstoneUtils.prototype.submit = function() {

	// Manually call this beacause it is not
	// called automatically ???
	if (typeof form_onsubmit != 'undefined') {
   		form_onsubmit();
   	}
    this.showHourglassCursor();
	document.millstone.submit();
}

// Window component javascript

MillstoneUtils.prototype.form_submit = function() {
	 // Invoke all registered listeners	    
	 if (typeof millstoneEventManager != 'undefined') {
		millstoneEventManager.submitCallback();
	}
}	    


MillstoneUtils.prototype.window_onunload = function() {    
	// Invoke all registered listeners	    
	if (typeof millstoneEventManager != 'undefined') {    
	    		millstoneEventManager.unloadCallback();
	}
}


MillstoneUtils.prototype.setFocusedFromActiveElement = function() {
	elementFocused(document.activeElement);
}


// --------------------------------------------------------------------------
// Get variable
//
// Params:
// id           ID the the INPUT tag
//
// Returns false iff no error occurred
// --------------------------------------------------------------------------

MillstoneUtils.prototype.getVarById = function(id) {
  var e = document.getElementById(id);
  if (e) {
    return e.value; 
  }
  return false;
}


// --------------------------------------------------------------------------
// Set variable
//
// Params:
// id           ID the the INPUT tag
// value        New value of the variable
// immediate    Should the server side event be created immediately
//
// Returns false iff no error occurred
// --------------------------------------------------------------------------

MillstoneUtils.prototype.setVarById = function(id,value,immediate) {
  e = document.getElementById(id);
  if (e) {
    e.value = value;
    if (immediate) {
      Millstone.submit();
    }
    return false; 
  }
  return true;
}


// --------------------------------------------------------------------------
// Check if integer list contains a number.
//
// Params:
// list         Comma separated list of integers
// number       Number to be tested
//
// Returns true iff the number can be found in the list
// --------------------------------------------------------------------------

MillstoneUtils.prototype.listContainsInt = function(list,number) {
  a = list.split(",");

  for (i=0;i<a.length;i++) {
    if (a[i] == number) return true;
  }
  
  return false;
}


// --------------------------------------------------------------------------
// Add number to integer list, if it does not exit before.
//
// Params:
// list         Comma separated list of integers
// number       Number to be added
//
// Return new list
// --------------------------------------------------------------------------

MillstoneUtils.prototype.listAddInt = function(list,number) {

  if (this.listContainsInt(list,number)) 
    return list;
    
  if (list == "") return number;
  else return list + "," + number;
}


// --------------------------------------------------------------------------
// Remove number from integer list.
//
// Params:
// list         Comma separated list of integers
// number       Number to be removed
//
// Return new list
// --------------------------------------------------------------------------

MillstoneUtils.prototype.listRemoveInt = function(list,number) {

  retval = "";
  a = list.split(',');

  for (i=0;i<a.length;i++) {
    if (a[i] != number) {
      if (i == 0) retval += a[i];
      else retval += "," + a[i];
    }
  }
  
  return retval;
}




// ==========================================================================
// ==========================================================================
//
// CSS class name handling functions
// 
// ==========================================================================



// --------------------------------------------------------------------------
// Get the unselected class name based on current class name.
// selected CSS class names are created by appending the string "-selected"
// to the base (unselected) class name. This function removes the
// selected and highlighted appendix from the class name.
//
// Params:
// currentName  Current class name 
//
// returns Class name without the selected appendix.
// --------------------------------------------------------------------------
MillstoneUtils.prototype.toUnselectedClassName = function(currentName) {
	if (currentName) {
		i = currentName.lastIndexOf("-selected");
		if (i>=0) {
			return currentName.substring(0,i);
		}
		i = currentName.lastIndexOf("-highlighted");
		if (i>=0) {
			return currentName.substring(0,i);
		}
	}
	return currentName;
}

// --------------------------------------------------------------------------
// Get the  selected class name based on current class name.
// selected CSS class names are created by appending the string "-selected"
// to the base (unselected) class name. This function appends the
// selected appendix from the class name.
//
// Params:
// currentName  Current class name 
//
// returns Class name with the selected appendix.
// --------------------------------------------------------------------------
MillstoneUtils.prototype.toSelectedClassName = function(currentName) {
	if (currentName) {
		i = currentName.lastIndexOf("-selected");
		if (i>=0) {
			return currentName;
		}		
	}
	return currentName+"-selected";
}

// --------------------------------------------------------------------------
// Get the  highlight class name based on current class name.
// selected CSS class names are created by appending the string "-highlight"
// to the base (unselected) class name. This function appends the
// highlight appendix from the class name.
//
// Params:
// currentName  Current class name 
//
// returns Class name with the highlight appendix.
// --------------------------------------------------------------------------
MillstoneUtils.prototype.toHighlightClassName = function(currentName) {
	if (currentName) {
		i = currentName.lastIndexOf("-highlighted");
		if (i>=0) {
			return currentName;
		}		
	}
	return currentName+"-highlighted";
}




// ==========================================================================
// ==========================================================================
//
// Window related functions
//
// ==========================================================================



// --------------------------------------------------------------------------
// Open new window with specified url
//
// Params:
// url:     URL to open in new window.
// name:    Name of the target (usually a window or frame). Empty window
//          name opens the result in this window.
// width:   Width of the target (usually a window).
// height:  Height of the target (usually a window).
// border:  Border decoration of the target (default|minimal|none)
// modal:   true if the target should be modal
//
// --------------------------------------------------------------------------

MillstoneUtils.prototype.openWindow = function(url,name,width,height,border) {
	var props = '';

	// Open the url in this window ?
	if (name == '') {
		name = window.name;
	}

	// Borders		
	if (border == 'minimal') {
	    props += 'toolbar=1,location=0,menubar=0,status=1,resizable=1,scrollbars=1';
	} else if (border == 'none') {
	    props += 'toolbar=0,location=0,menubar=0,status=0,resizable=1,scrollbars=1';	
	} else {
	    props += 'toolbar=1,location=1,menubar=1,status=1,resizable=1,scrollbars=1';
	}
	
	// Height and width
	if ((width > 0) && (height > 0)) {
		props += ',height='+height+',width='+width;
	}
	
	var win = window.open(url,name,props);	
	win.blur();	
	win.focus();
	
	return win;
}



// ==========================================================================
// ==========================================================================
//
// Element handling functions
//
// ==========================================================================


// --------------------------------------------------------------------------
// Disables a document element.
//
// Params:
// id:      ID of the element
//
// --------------------------------------------------------------------------
MillstoneUtils.prototype.disableElement = function(element) {
        if (element != null) {
            element.disabled = true;
        }
}

// --------------------------------------------------------------------------
// Enables a document element.
//
// Params:
// id:      ID of the element
//
// --------------------------------------------------------------------------
MillstoneUtils.prototype.enableElement = function(element) {
        if (element != null) {
			element.disabled = false;        
        }
}

// --------------------------------------------------------------------------
// Hides a document element.
//
// Params:
// id:      ID of the element
//
// --------------------------------------------------------------------------
MillstoneUtils.prototype.hideElement = function(element) {
        if (element != null) {
            element.style.display="none";
        }
}

// --------------------------------------------------------------------------
// Shows a document element.
//
// Params:
// id:      ID of the element
//
// --------------------------------------------------------------------------
MillstoneUtils.prototype.showElement = function(element) {
        if (element != null) {
			element.style.display="";        
        }
}

// --------------------------------------------------------------------------
// Check if a document element is visible.
//
// Params:
// id:      ID of the element
//
// --------------------------------------------------------------------------
MillstoneUtils.prototype.isElementVisible = function(element) {
        if (element != null) {
			return (element.style.display != "none");        
        }
        return false;
}

// --------------------------------------------------------------------------
// Hides or shows a document element depending it current state.
//
// Params:
// id:      ID of the element
//
// --------------------------------------------------------------------------
MillstoneUtils.prototype.toggleElement = function(element) {
        if (element != null) {
        	if (this.isElementVisible(element)) {
				this.hideElement(element);
			} else {
				this.showElement(element);
			}
        }
}

// --------------------------------------------------------------------------
// Hides a document element by id.
//
// Params:
// id:      ID of the element
//
// --------------------------------------------------------------------------
MillstoneUtils.prototype.hideElementById = function(id) {
    	this.hideElement(document.getElementById(id));
}

// --------------------------------------------------------------------------
// Shows an document element by id.
//
// Params:
// id:      ID of the element
//
// --------------------------------------------------------------------------
MillstoneUtils.prototype.showElementById = function(id) {
    	this.showElement(document.getElementById(id));
}

// --------------------------------------------------------------------------
// Hides or shows an document element depending its current state.
//
// Params:
// id:      ID of the element
//
// --------------------------------------------------------------------------
MillstoneUtils.prototype.toggleElementById = function(id) {
    	this.toggleElement(document.getElementById(id));
}


// --------------------------------------------------------------------------
// Show object's properties.
//
// Opens new window showing given objects properties
// 
// Params:
// el   Element to be inspected.
//
// returns:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.showObjectProperties = function(el) 
{ 
    var result = getObjectProperties(el);
	var w = window.open(); 
	w.document.write("<pre>"+result+"</pre>"); 
	w.document.close(); 
} 

// --------------------------------------------------------------------------
// Get object properties.
//
// Params:
// el   Element to be inspected.
//
// returns: String containing objects properties in format name=value, one per line.
// --------------------------------------------------------------------------
MillstoneUtils.prototype.getObjectProperties = function(obj) {
    var result = "" 
    for (var p in obj) {
        result += p + "=" + obj[p] + "\n"; 
    }
	return result;
}



// ==========================================================================
// ==========================================================================
//
// Action related functions
//
// ==========================================================================


// --------------------------------------------------------------------------
// Opens action popup.
//
// Params:
//
// --------------------------------------------------------------------------
MillstoneUtils.prototype.actionPopup = function(event,actionListId,itemKey,activeActions) {

	document.getElementById(actionListId+"_ACTIVE_ITEM").value = itemKey;
	var popup = document.getElementById(actionListId + '_POPUP');

	// Disable all actions
	for (i=0; i<popup.childNodes.length;i++) {
		if (popup.childNodes[i].style && popup.childNodes[i].className == 'action-item') {
			popup.childNodes[i].className = 'action-item-disabled';
		} 
	}
	
	// Enable all active actions 
	var acts = activeActions.split(',');
	for (a in acts) {
		var s = actionListId + "_" + acts[a];
		var action = document.getElementById(s);
		if (action && action.style) {
			action.className = 'action-item';
		}
	}

	this.showPopupById(actionListId+"_POPUP",event.clientX,event.clientY);
	
	window.event.returnValue = false;		
}

// --------------------------------------------------------------------------
// Fires action on current item in given popup.
//
// Params:
//
// return:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.fireAction = function(actionListId,actionVariableId,actionKey) {
	this.hidePopupById(actionListId+"_POPUP");
	actionVariable = document.getElementById(actionVariableId);
	currentItem = document.getElementById(actionListId+"_ACTIVE_ITEM");
	if (currentItem && actionVariable) {
		actionVariable.value = currentItem.value+","+actionKey;
		void(Millstone.submit());	
	}
}






// ==========================================================================
// ==========================================================================
//
// Pop-up related functions
//
// ==========================================================================




// --------------------------------------------------------------------------
// Shows popup on location specified by given coordinates.
//
// Params:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.showPopupById = function(popupId, clientX, clientY) {

	this.hideAllPopups();

	popup = document.getElementById(popupId);
	this.activePopups[popupCount++] = popupId;
	
	// Safari given clientX,Y in relation to body
	if (this.safari) {
	   clientX = clientX - document.body.scrollLeft;
	   clientY = clientY - document.body.scrollTop;
	}

	// Find out how close the mouse is to the corner of the window
	var rightedge = this.ie5? document.body.clientWidth-clientX : window.innerWidth-clientX;
	var bottomedge = this.ie5? document.body.clientHeight-clientY : window.innerHeight-clientY;

	// If the horizontal distance isn't enough to accomodate the width of the context menu
	if (rightedge<popup.offsetWidth) {
		// Move the horizontal position of the menu to the left by it's width
		popup.style.left=this.ie5? 
			document.body.scrollLeft+clientX-popup.offsetWidth : 
			window.pageXOffset+clientX-popup.offsetWidth;
	} else {
		//position the horizontal position of the menu where the mouse was clicked
		popup.style.left=this.ie5? document.body.scrollLeft+clientX : window.pageXOffset+clientX;
	}
	
	// Same concept with the vertical position
	if (bottomedge<popup.offsetHeight) {
		popup.style.top=this.ie5? 
			document.body.scrollTop+clientY-popup.offsetHeight : 
			window.pageYOffset+clientY-popup.offsetHeight;
	} else {
		popup.style.top=this.ie5? document.body.scrollTop+clientY : window.pageYOffset+clientY;
	}
	
	this.showElement(popup);

	// Assure that this popup is not hidden	
	this.skipNextHideAll = true;
}

// --------------------------------------------------------------------------
// Hide popup by id. 
//
// Params:
// id   ID of the popup to hide.
//
// returns:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.hidePopupById = function(id) {
		popup = document.getElementById(id);
		if (popup) {
			this.hideElement(popup);
		}		
}


// --------------------------------------------------------------------------
// Hide all visible popups
//
// Params:
//
// returns:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.hideAllPopups = function() {
	// Do not immediately remove the popup
	if (this.skipNextHideAll) {
		this.skipNextHideAll = false;
		return true;
	}

	for (id in this.activePopups) {
		this.hidePopupById(this.activePopups[id]);
	}		
	popupCount = 0;

	return true;
}





// ==========================================================================
// ==========================================================================
//
// Component related functions
// 
// ==========================================================================


// --------------------------------------------------------------------------
// Toggle checkbox state
//
// Params:
// id           Id the the INPUT tag (TYPE="CHECKBOX")
//
// Returns false iff no error occurred
// --------------------------------------------------------------------------

MillstoneUtils.prototype.toggleCheckbox = function(id,immediate) {
  
  e = document.getElementById(id);
  if (e) {
	e.checked = (e.checked ? false : true);

	if (immediate) {
		Millstone.submit();
	}
	return false;
  }
  
  return true;
}


// --------------------------------------------------------------------------
// Input controlling table row selection changes state.
//
// Params:
// inputid      Id of the INPUT containing the selection state
// key          Key identifying item connected to this table row
// unselected   Class of the unselected row
// selected     Class of the selected row
// immediate    "true" iff the table is in immediate mode
// mode         "single" or "multi"
// --------------------------------------------------------------------------

MillstoneUtils.prototype.tableSelClick = function(inputid,key,immediate,mode) {

  prev = value = this.getVarById(inputid);
  
  // Change selection value
  if (mode == "multi") {
    if (this.listContainsInt(value,key)) 
      value = this.listRemoveInt(value,key);
    else 
      value = this.listAddInt(value,key);
  } else {
    value = key;
  }
  
  this.setVarById(inputid,value);
  var changed = (prev != value);
  
  // In single select mode change remove old selections
  if (mode == "single") {
    keys = prev.split(",");
    for (i=0;i<keys.length;i++) {
      tr = document.getElementById("" + inputid + "_" + keys[i]);
      if (tr) 
        tr.className = this.toUnselectedClassName(tr.className);
    }
  }
   
  // Update visual state of the current item
  tr = document.getElementById("" + inputid + "_" + key);
  if (tr) {
    if (this.listContainsInt(value,key))
      tr.className = this.toSelectedClassName(tr.className); 
    else 
      tr.className = this.toUnselectedClassName(tr.className);
  }

  // Submit if in immediate mode  
  if (changed && immediate) {
    Millstone.submit();
  }
}

// --------------------------------------------------------------------------
// Input controlling tree item selection changes state.
//
// Params:
// inputid      Id of the INPUT containing the selection state
// key          Key identifying item
// immediate    "true" iff the table is in immediate mode
// mode         "single" or "multi"
// --------------------------------------------------------------------------

MillstoneUtils.prototype.treeSelClick = function(inputid,key,immediate,mode) {

  prev = value = this.getVarById(inputid);

  // Change selection value
  if (mode == "multi") {
    if (this.listContainsInt(value,key)) 
      value = this.listRemoveInt(value,key);
    else 
      value = this.listAddInt(value,key);
  } else 
    value = key;
  this.setVarById(inputid,value);
  
  // In single select mode change remove old selections
  if (mode == "single") {
    keys = prev.split(",");
    for (i=0;i<keys.length;i++) {
      itemElement = document.getElementById("" + inputid + "_" + keys[i]);
      if (itemElement)
        itemElement.className = this.toUnselectedClassName(itemElement.className);
    }
  }
   
  // Update visual state of the current item
  itemElement = document.getElementById("" + inputid + "_" + key);
  if (itemElement) {
    if (this.listContainsInt(value,key))
      itemElement.className = this.toSelectedClassName(itemElement.className);
    else 
      itemElement.className = this.toUnselectedClassName(itemElement.className);
  }

  // Submit if in immediate mode  
  if (immediate) {
    Millstone.submit();
  }
}

// --------------------------------------------------------------------------
// Input controlling tree item expansion/collapsion changes state.
//
// Params:
// expandid     Id of the INPUT containing the expansion state
// collapseid   Id of the INPUT containing the collapsion state
// key          Key identifying item
// immediate    "true" iff the table is in immediate mode
// --------------------------------------------------------------------------

MillstoneUtils.prototype.treeExpClick = function(expandid,collapseid,key,immediate) {

  // Fetch the current variable values
  var expanded = this.getVarById(expandid);
  var collapsed = this.getVarById(collapseid);

  // Fetch the "template" images containing collapsed 
  // and expanded images and get the current items image
  var expandedImg = document.getElementById(collapseid+"_IMG");
  var collapsedImg = document.getElementById(expandid+"_IMG");
  var img = document.getElementById("img" + expandid + "_" + key);  

  // Fetch the child element node
  var childElement = document.getElementById("" + expandid + "_" + key);


  // Determine which state the item is now
  isCollapsed = (!childElement) || (!this.isElementVisible(childElement));

  // Expand the item
  if (isCollapsed) {
    // Remove from collapsion list
    if (this.listContainsInt(collapsed,key)) {
      collapsed = this.listRemoveInt(collapsed,key);
    }
    this.setVarById(collapseid,collapsed);

    // Add to expanded list
    if (!this.listContainsInt(expanded,key)) {
       expanded = this.listAddInt(expanded,key);
    }  
    this.setVarById(expandid,expanded);


  // Collapse the item
  } else {
    // Remove from expansion list
    if (this.listContainsInt(expanded,key)) {
      expanded = this.listRemoveInt(expanded,key);
    }
    this.setVarById(expandid,expanded);

    // Add to collapsion list
    if (!this.listContainsInt(collapsed,key)) {
       collapsed = this.listAddInt(collapsed,key);
    }  
    this.setVarById(collapseid,collapsed);
  }
  
  
  // Update visual state of the current item  
  if (childElement && img) {
    if (this.listContainsInt(expanded,key)) {
      this.showElement(childElement);
      if (expandedImg) {
        img.src = expandedImg.src;      
      }
    } else {
      this.hideElement(childElement);
      if (collapsedImg) {
        img.src = collapsedImg.src;      
      }
    }
  } else {
      // Fetch the missing items
      Millstone.submit();
  }
}


// ==========================================================================
// ==========================================================================
//
// Calendar related functions
// 
// ==========================================================================



// --------------------------------------------------------------------------
// Update contents of calendar to reflect the given year and month.
//
// Params:
//
// Return:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.getCalendarMonth = function(year, month, weekBegin) {

	var cal = new Array();
	cal[0] = new Array(7);
	cal[1] = new Array(7);
	cal[2] = new Array(7);
	cal[3] = new Array(7);
	cal[4] = new Array(7);
	cal[5] = new Array(7);
	
	var daysInMonth = this.getDaysInMonth(year,month);
	var calDate = new Date(year, month, 1);
	var weekDayOfFirst = (calDate.getDay()-weekBegin+7)%7;	
	var vardate = 1;
	var i,day,week;
	
	for (day = weekDayOfFirst; day < 7; day++) {
		cal[0][day] = vardate;
		vardate++;
	}
	
	for (week = 1; week < 6; week++) {
		for (day = 0; day < 7; day++) {
			if (vardate <= daysInMonth) {
				cal[week][day] = vardate;
				vardate++;
      		}
      		else cal[week][day] = "";
   		}
	}	
	return cal;
}


MillstoneUtils.prototype.getDaysInMonth = function(year, month) {
	var lastDate = new Date(year, 1+month, 0);
	return lastDate.getDate();
}


// --------------------------------------------------------------------------
// Update contents of calendar to reflect the current selection.
//
// Params:
//
// Return:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.updateCalendar = function(calendarId, yearId, monthId, dayId, weekBegin, immediate) {

  if (calendarId != '') {;

	var iWeekBegin = parseInt(weekBegin); 	
	var iYear = parseInt(this.getVarById(yearId)); 	
	var iMonth = parseInt(this.getVarById(monthId))-1; 
	var iDay = parseInt(this.getVarById(dayId));
	
	// Round the date to last available in month
   	if (iDay > this.getDaysInMonth(iYear,iMonth)) {
   		iDay = this.getDaysInMonth(iYear,iMonth);
   		this.setVarById(dayId,iDay);
   	}
	
	// Get the calendar for month
	cal = this.getCalendarMonth(iYear, iMonth, iWeekBegin);
	
	for (week = 0; week < 6; week++) {
		for (day = 0; day < 7; day++) {	        			
			if ((cal[week][day]) && !isNaN(cal[week][day])) {				
			    el = document.getElementById(calendarId+"_"+week+"_"+day);
			    if (el) {
				   el.innerText = cal[week][day];
				   el.innerHTML = cal[week][day];
				   el.className = "cal-day";				   
				}
			} else {
			    el = document.getElementById(calendarId+"_"+week+"_"+day);
			    if (el) {
				   el.innerText = "";
				   el.innerHTML = "";
				   el.className = "";
				}
         	}
      	}
   	}
   	
   	// Visually select the day   	
	this.calendarDaySelect(calendarId, iDay)   	
	
  }

  // Submit if in immediate mode  
  if (immediate && immediate == "true") {
    Millstone.submit();
  }
	
}


// --------------------------------------------------------------------------
// Visually unselect all days.
//
// Params:
//
// Return:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.calendarUnselectAll = function(calendarId) {

	for (week = 0; week < 6; week++) {
		for (day = 0; day < 7; day++) {
		    el = document.getElementById(calendarId+"_"+week+"_"+day);
		    if (el) {
			   el.className = this.toUnselectedClassName(el.className);				   
			} else {
			    el = document.getElementById(calendarId+"_"+week+"_"+day);
			    if (el) {
				   el.className = "";
				}
         	}
      	}
   	}
}



// --------------------------------------------------------------------------
// Visually select the current day
//
// Params:
//
// Return:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.calendarDaySelect = function(calendarId, dayNumber) {
 
	// Visually select the current day
	for (week = 0; week < 6; week++) {
		for (day = 0; day < 7; day++) {
		    var el = document.getElementById(calendarId+"_"+week+"_"+day);
		    if (el) {
		       var iDay = parseInt(el.innerText);		       
			   if (!isNaN(iDay) && (iDay == dayNumber)) {				      
				   el.className = this.toSelectedClassName(el.className);
				   return;				   			   		
			   }
			}
      	}
   	}
}

// --------------------------------------------------------------------------
// Handle calendar selection click.
// 
// Updates the visual selection and the day variable.
//
// Params:
//
// Return:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.calSel = function(calendarId, dayVariableId, dayElementId, immediate) {
 
	// Visually unselect all
	this.calendarUnselectAll(calendarId);

	// Select the current one
	var dayElement = document.getElementById(dayElementId);
	if (dayElement && (dayElement.innerText || dayElement.innerHTML)) {
		this.setVarById(dayVariableId,dayElement.innerText);
		dayElement.className= this.toSelectedClassName(dayElement.className);
	}	
	
  // Submit if in immediate mode  
  if (immediate) {
    Millstone.submit();
  }
}

// ==========================================================================
// ==========================================================================
//
// Modal dialog functions
// 
// ==========================================================================



// --------------------------------------------------------------------------
// Disable events and focus the latest dialog window.
// 
//
// Params:
//
// Return:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.focusModal = function() {
   var win = Millstone.dialogs.pop();
   if (win) {
     Millstone.dialogs.push(win);
   	 win.focus();
   }
}

// --------------------------------------------------------------------------
// Block all user focus events in given window.
// This is used to disable the opener (parent) of a modal window.
//
// Params: Window object to be disabled.
//
// Return:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.blockEvents = function(win) {
   win.document.onclick = this.focusModal;
   win.document.forms["millstone"].disable
   win.onclick = this.focusModal;
   win.onfocus = this.focusModal;
   win.document.body.onfocus = this.focusModal;
   this.showOverlayLayer(win);
   //alert("blocked: "+win.name);
}

// --------------------------------------------------------------------------
// Unblock all user focus events in given window.
// This is used to re-enable the opener (parent) of a modal window.
//
// Params: Window object to be disabled.
//
// Return:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.unblockEvents = function(win) {
   win.onclick = null;
   this.hideOverlayLayer(win);
   //alert("un-blocked: "+win.name);
}

// --------------------------------------------------------------------------
// Make given window as modal window for its opener.
//
// Params: Window object to be treated as modal.
//
// Return:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.makeModal = function(win) {
      this.dialogs.push(win);
      if (win.opener) {
          this.blockEvents(win.opener);
      }
	  win.document.body.onunload = this.cancelModal;	  
      win.focus();      
      //alert("opened dialog: "+win.name);   
}

// --------------------------------------------------------------------------
// Cancel the modality of given window.
// Typically this is called unload method of modal window.
//
// Params: The modal window object.
//
// Return:
// --------------------------------------------------------------------------
MillstoneUtils.prototype.cancelModal = function() {  
   var win = Millstone.dialogs.pop();
   if (win && win.opener) {
   		Millstone.unblockEvents(win.opener);
   }
   //alert("closed dialog: "+window.name);   
}


// --------------------------------------------------------------------------
// Create and show an overlay layer for a window .
// This is used to disable any mouse click actions to given window, and
// this is typically called for opener (parent) of a modal window to disable
// the focus shifting using mouse.
//
// The layer is only created once, and the subsequent calls to hideOverlayLayer and
// this function only hide and show it.
//
// Params: The window object to add the layer.
//
// Return: The layer
// --------------------------------------------------------------------------
MillstoneUtils.prototype.showOverlayLayer = function(win)
{
	var layer = null;
	var left = 0;
	var top = 0;
	var id = "ms_overlay";
	var html = "";
	
	layer =win.document.getElementById(id);
	if (layer == null) {
		var width =  win.document.body.scrollWidth;
		var height = win.document.body.scrollHeight;
		var divhtml = '<div  id=' + id + ' style="zIndex:100000; visibility:visible;left:' + left + 
			'px;top:' + top + 'px;width:' + width + 
			'px;height:' + height + 'px;position:absolute;">' + 
		html + '</div>';
		if (win.document.body.insertAdjacentHTML) {
			win.document.body.insertAdjacentHTML('beforeEnd', divhtml);		
		} else {
			var range = document.createRange();
			range.setStartBefore(document.body);
			var node = range.createContextualFragment(divhtml)
			win.document.body.appendChild(node);
		}
	}
	layer = win.document.getElementById(id);
	if (layer) {
		this.showElement(layer);
	}
	return layer;
}


// --------------------------------------------------------------------------
// Hide an overlay layer for a window .
// This is typically called for opener (parent) of a modal window to re-enable
// the focusing using mouse.
//
// Params: The window object containing the the layer.
//
// Return: nothing.
// --------------------------------------------------------------------------
MillstoneUtils.prototype.hideOverlayLayer = function(win) {
	var layer = win.document.getElementById("ms_overlay");
	if (layer) {
		this.hideElement(layer);
	}
}


// --------------------------------------------------------------------------
// Shows an overlay div and sets the cursor to be style "wait" in current 
// window. This can be used to show a hourglass cursor while processing.
//
// Params: nothing.
//
// Return: nothing.
// --------------------------------------------------------------------------
MillstoneUtils.prototype.showHourglassCursor = function() {
	var layer = this.showOverlayLayer(window);
	layer.style.cursor = "wait";
}

// ==========================================================================
// ==========================================================================
//
// Window load and unload callback manager
// 
// ==========================================================================

Function.prototype.andThen=function(g) {
  var f=this;
  return function() {
    f();g();
  }
}

function MillstoneEventManager() {

  this.loadCallback=function () { }; // do nothing
  this.unloadCallback=function () { }; // do nothing
  this.submitCallback=function () { }; // do nothing

  this.registerLoadCallback=function(callbackFunction) {
    this.loadCallback=(this.loadCallback).andThen(callbackFunction);
  }

  this.registerUnloadCallback=function(callbackFunction) {
    this.unloadCallback=(this.unloadCallback).andThen(callbackFunction);
  }
  
  this.registerSubmitCallback=function(callbackFunction) {
    this.submitCallback=(this.submitCallback).andThen(callbackFunction);
  }
}

var millstoneEventManager = new MillstoneEventManager();

// --------------------------------------------------------------------------
// Millstone utils constructor.
//
// Returns nothing
// --------------------------------------------------------------------------
function MillstoneUtils() {

	// Is the debug mode enabled?
	this.debug = true;

	// Globals for handling browser differences
	this.ie5 = document.all && document.getElementById;
	this.ns6 = document.getElementById && !document.all;
	this.safari = navigator.userAgent.toLowerCase().indexOf("safari") >= 0;

	// Modal window handling variables
	this.dialogWin = new Object();
	this.dialogs = new Array();
	
	// Global array of active popups
	this.activePopups = new Array();
	this.popupCount = 0;
	this.skipNextHideAll = false;
	
	
	// Create instances for other required classes
	this.commons = new MillstoneCommonUtils();
	this.logger = new MillstoneLogger();
	this.events = new MillstoneEventUtils();
	this.focusable = new MillstoneFocusableUtils(this.events);
	this.windows = new MillstoneWindowUtils();	

	// Add handler for otherwise unhandled clicks
	var f = function() { Millstone.hideAllPopups() };
	if (window.document['onclick'] == null) {
		window.document['onclick'] = f;
	} else {
		window.document['onclick'] = (document['onclick']).andThen(f);		
	}
}




