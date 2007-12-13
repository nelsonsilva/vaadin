/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.itmill.toolkit.ui;

import java.util.Iterator;

import com.itmill.toolkit.terminal.HasSize;
import com.itmill.toolkit.terminal.PaintException;
import com.itmill.toolkit.terminal.PaintTarget;
import com.itmill.toolkit.terminal.Size;

/**
 * TODO finish documentation
 * 
 * our layouts (except custom layout of course) don't currently work at all with
 * relative widths. This layout tries to cope with this issue.
 * 
 * basically this is ordered layout which has Sizeable interface 100 % height &
 * width by default
 * 
 * all contained components may also have Sizeable interfaces sizes
 * 
 * can be used to build flexible layout where some component gets all the space
 * other components don't use. Or just provide expanded container.
 * 
 */
public class ExpandLayout extends OrderedLayout implements HasSize {

    private Component expanded;
    private Size size;

    public ExpandLayout() {
        size = new Size(this);
        size.setSizeFull();
    }

    public ExpandLayout(int orientation) {
        this();
        setOrientation(orientation);
    }

    /**
     * @param c
     *                Component which container will be maximized
     */
    public void expand(Component c) {
        expanded = c;
        requestRepaint();
    }

    public String getTag() {
        return "expandlayout";
    }

    public void paintContent(PaintTarget target) throws PaintException {

        // Add margin info. Defaults to false.
        target.addAttribute("margins", margins.getBitMask());

        // Add spacing attribute (omitted if false)
        if (isSpacingEnabled()) {
            target.addAttribute("spacing", true);
        }

        // Size
        size.paint(target);

        // Adds the attributes: orientation
        // note that the default values (b/vertival) are omitted
        if (getOrientation() == ORIENTATION_HORIZONTAL) {
            target.addAttribute("orientation", "horizontal");
        }

        final String[] alignmentsArray = new String[components.size()];

        // Adds all items in all the locations
        int index = 0;
        for (final Iterator i = getComponentIterator(); i.hasNext();) {
            final Component c = (Component) i.next();
            if (c != null) {
                target.startTag("cc");
                if (c == expanded) {
                    target.addAttribute("expanded", true);
                }
                c.paint(target);
                target.endTag("cc");
            }
            alignmentsArray[index++] = String.valueOf(getComponentAlignment(c));

        }

        // Add child component alignment info to layout tag
        target.addAttribute("alignments", alignmentsArray);

    }

    public void addComponent(Component c, int index) {
        if (expanded == null) {
            expanded = c;
        }
        super.addComponent(c, index);
    }

    public void addComponent(Component c) {
        if (expanded == null) {
            expanded = c;
        }
        super.addComponent(c);
    }

    public void addComponentAsFirst(Component c) {
        if (expanded == null) {
            expanded = c;
        }
        super.addComponentAsFirst(c);
    }

    public void removeComponent(Component c) {
        super.removeComponent(c);
        if (c == expanded && getComponentIterator().hasNext()) {
            expanded = (Component) getComponentIterator().next();
        } else {
            expanded = null;
        }
    }

    public void replaceComponent(Component oldComponent, Component newComponent) {
        super.replaceComponent(oldComponent, newComponent);
        if (oldComponent == expanded) {
            expanded = newComponent;
        }
    }

    public Size getSize() {
        return size;
    }

}
