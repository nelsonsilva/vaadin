/** Declare our own namespace. 
 * 
 * All globals should be defined in this namespace.
 *
 */
if (typeof itmill == 'undefined') itmill = new Object();
if (typeof itmill.html == 'undefined') itmill.html = new Object();

/*
* Manages draggable table headers.
* variableId: id of the variable (e.g textfield) to update.
* hidName: the name of the element attribute that contains the header id.
*
* Call addDraggableById(elementId) to add elements to participate in d&d.
* note: draggable elements should have position:relative
*/
itmill.html.TableHeaderDragger = function(variableId,cidName) {
    this.all = new Array();
    this.variableId = variableId;
    this.cidName = (cidName?cidName:"cid");

    /* Add an element to participate in d&d */
    this.addDraggableById = function(elementId) {
        var o = document.getElementById(elementId);
        o.thd = this;
        var i = this.all.length;
        this.all[i] = o;
        o.next = null;
        if (i>0) o.prev = this.all[i-1];
        if (o.prev) o.prev.next = o;
        o.onmousedown = this.start;
        o.dragclickhandler = o.onclick;
        o.onclick = this.catchClick;
    }
    this.start = function(e) {
        e = (e?e:window.event);
       	var o = (e.srcElement?e.srcElement:e.target);
       	if (!o.thd) o = o.parentNode; 
        o.thd.dragging = o;
        o.thd.moved = false;
        var y = parseInt(o.style.bottom);
        var x = parseInt(o.style.left);
	    var posx = o.thd.getMouseX(e);
	    var posy = o.thd.getMouseY(e);

        o.lastMouseX = posx;
        o.lastMouseY = posy;

        o.startx = posx;

        if (o.minX != null)	o.minMouseX	= posx - x + o.minX;
        if (o.maxX != null)	o.maxMouseX	= o.minMouseX + o.maxX - o.minX;
        if (o.minY != null) o.maxMouseY = -o.minY + posy + y;
        if (o.maxY != null) o.minMouseY = -o.maxY + posy + y;

        document.onmousemove	= o.thd.drag;
        document.onmouseup		= o.thd.end;
        document.currentTHD = o.thd;
        return false;
    }
    this.drag = function(e) {
        var thd = document.currentTHD;
        var o = thd.dragging;
        e = thd.fixE(e);

	    var posx = thd.getMouseX(e);
	    var posy = thd.getMouseY(e);

        var ey	= posy;
        var ex	= posx;
        var y = parseInt(o.style.bottom);
        var x = parseInt(o.style.left);
        var nx, ny;

        nx = x + (ex - o.lastMouseX);
        ny = y + (ey - o.lastMouseY);

        if (!thd.moved&&(posy-o.startx)!=0) thd.moved = true;

        o.style["left"] = posx-o.startx;
        o.style["top"] = 10;

        for (i=0;i<thd.all.length;i++) {
            var trg = thd.all[i];
            if (trg!=o&&ex>=thd.getX(trg)&&ex<=thd.getX(trg)+(trg.offsetWidth/2)) {
            	trg.style.borderRight = 'none';
                trg.style.borderLeft = '2px solid white';
            } else if (trg!=o&&ex>=thd.getX(trg)+(trg.offsetWidth/2)&&ex<=thd.getX(trg)+trg.offsetWidth) {
            	trg.style.borderLeft = 'none';
                trg.style.borderRight = '2px solid white';
            } else {
            	trg.style.borderLeft = 'none';
            	trg.style.borderRight='none';
            }
        }


        return false;
    }
    this.end = function(e) {
        var thd = document.currentTHD;
        var o = thd.dragging;
        e = thd.fixE(e);
        document.onmousemove = null;
        document.onmouseup   = null;
	    var posx = thd.getMouseX(e);
	    var posy = thd.getMouseY(e);

        var hit = false;
        for (i=0;i<thd.all.length;i++) {
            var trg = thd.all[i];
            if (trg!=o&&posx>=thd.getX(trg)&&posx<=thd.getX(trg)+(trg.offsetWidth/2)) {
                hit = true;
                // disconnect form old position
                var oprev = o.prev;
                var onext = o.next;
                if (oprev!=null) oprev.next = null;
                if (onext!=null) onext.prev = null;
                o.next = null;
                o.prev = null;
                if (oprev!=null) oprev.next = onext;
                if (onext!=null) onext.prev = oprev;
                // connect at new position
                o.prev = trg.prev;
                if (trg.prev!=null) trg.prev.next = o;
                o.next = trg;
                trg.prev = o;
                break;
            } else if (trg!=o&&posx>=thd.getX(trg)+(trg.offsetWidth/2)&&posx<=thd.getX(trg)+trg.offsetWidth) {
                hit = true;
                // disconnect form old position
                var oprev = o.prev;
                var onext = o.next;
                if (oprev!=null) oprev.next = null;
                if (onext!=null) onext.prev = null;
                o.next = null;
                o.prev = null;
                if (oprev!=null) oprev.next = onext;
                if (onext!=null) onext.prev = oprev;
                // connect at new position
                o.next = trg.next;
                if (trg.next!=null) trg.next.prev = o;
                o.prev = trg;
                trg.next = o;
                break;
            }
            
        }
        o.style["top"] = 0;
        if (!hit) {
            o.style["left"] = 0;
            return;
        }
        var beg = o;
        while (beg.prev != null) {
            beg = beg.prev;
        }
        var list = "";
        while (beg.next!=null) {
            list += beg.attributes[thd.cidName].value + ",";
             beg = beg.next;
        }
        list += beg.attributes[thd.cidName].value;
        document.getElementById(thd.variableId).value = list;

        o = null;
        document.currentTHD = null;
        itmill.html.utils.submit();
    }
    this.fixE = function(e) {
        if (typeof e == 'undefined') e = window.event;
        if (typeof e.layerX == 'undefined') e.layerX = e.offsetX;
        if (typeof e.layerY == 'undefined') e.layerY = e.offsetY;
        return e;
    }
    this.catchClick = function(e) {
        e= (e?e:window.event);
        var o = (e.srcElement?e.srcElement:e.target);
        if (!o.thd) o = o.parentNode;
        var thd = o.thd;
         if (!thd.moved) {
           if (o.dragclickhandler) o.dragclickhandler(e);
        }
   }
    this.getMouseY = function (e) {
        if (e.pageY)
        {
            return e.pageY;
        }
        else if (e.clientY)
        {
            return (e.clientY + document.body.scrollTop);
        }
        return 0;
    }
    this.getMouseX = function (e) {
        if (e.pageX)
        {
            return e.pageX;
        }
        else if (e.clientX)
        {
            return (e.clientX + document.body.scrollLeft);
        }
        return 0;
    }
    
    this.getX = function (elm)
    {
        var curleft = 0;
        if (elm.offsetParent)
        {
            while (elm.offsetParent)
            {
                curleft += elm.offsetLeft
                elm = elm.offsetParent;
            }
        }
        else if (elm.x)
            curleft += elm.x;
        return curleft;
    }

}

