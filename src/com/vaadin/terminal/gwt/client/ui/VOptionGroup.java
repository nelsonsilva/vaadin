/*
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.UIDL;

public class VOptionGroup extends VOptionGroupBase {

    public static final String CLASSNAME = "v-select-optiongroup";

    private final Panel panel;

    private final Map optionsToKeys;

    public VOptionGroup() {
        super(CLASSNAME);
        panel = (Panel) optionsContainer;
        optionsToKeys = new HashMap();
    }

    /*
     * Return true if no elements were changed, false otherwise.
     */
    @Override
    protected void buildOptions(UIDL uidl) {
        panel.clear();
        for (final Iterator it = uidl.getChildIterator(); it.hasNext();) {
            final UIDL opUidl = (UIDL) it.next();
            CheckBox op;
            if (isMultiselect()) {
                op = new VCheckBox();
                op.setText(opUidl.getStringAttribute("caption"));
            } else {
                op = new RadioButton(id, opUidl.getStringAttribute("caption"));
                op.setStyleName("v-radiobutton");
            }
            op.addStyleName(CLASSNAME_OPTION);
            op.setValue(opUidl.getBooleanAttribute("selected"));
            op.setEnabled(!opUidl.getBooleanAttribute("disabled")
                    && !isReadonly() && !isDisabled());
            op.addClickHandler(this);
            optionsToKeys.put(op, opUidl.getStringAttribute("key"));
            panel.add(op);
        }
    }

    @Override
    protected Object[] getSelectedItems() {
        return selectedKeys.toArray();
    }

    @Override
    public void onClick(ClickEvent event) {
        super.onClick(event);
        if (event.getSource() instanceof CheckBox) {
            final boolean selected = ((CheckBox) event.getSource()).getValue();
            final String key = (String) optionsToKeys.get(event.getSource());
            if (!isMultiselect()) {
                selectedKeys.clear();
            }
            if (selected) {
                selectedKeys.add(key);
            } else {
                selectedKeys.remove(key);
            }
            client.updateVariable(id, "selected", getSelectedItems(),
                    isImmediate());
        }
    }

    @Override
    protected void setTabIndex(int tabIndex) {
        for (Iterator iterator = panel.iterator(); iterator.hasNext();) {
            FocusWidget widget = (FocusWidget) iterator.next();
            widget.setTabIndex(tabIndex);
        }
    }

    public void focus() {
        Iterator<Widget> iterator = panel.iterator();
        if (iterator.hasNext()) {
            ((Focusable) iterator.next()).setFocus(true);
        }
    }

}
