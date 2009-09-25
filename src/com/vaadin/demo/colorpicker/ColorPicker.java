/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.demo.colorpicker;

import java.util.Map;

import com.vaadin.demo.colorpicker.gwt.client.ui.VColorPicker;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.ClientWidget;

@SuppressWarnings("serial")
@ClientWidget(VColorPicker.class)
public class ColorPicker extends AbstractField {

    public ColorPicker() {
        super();
        setValue(new String("white"));
    }

    /** The property value of the field is a String. */
    @Override
    public Class<?> getType() {
        return String.class;
    }

    /** Tag is the UIDL element name for client-server communications. */
    @Override
    public String getTag() {
        return "colorpicker";
    }

    /** Set the currently selected color. */
    public void setColor(String newcolor) {
        // Sets the color name as the property of the component.
        // Setting the property will automatically cause repainting of
        // the component with paintContent().
        setValue(newcolor);
    }

    /** Retrieve the currently selected color. */
    public String getColor() {
        return (String) getValue();
    }

    /** Paint (serialize) the component for the client. */
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        // Superclass writes any common attributes in the paint target.
        super.paintContent(target);

        // Add the currently selected color as a variable in the paint
        // target.
        target.addVariable(this, "colorname", getColor());
    }

    /** Deserialize changes received from client. */
    @SuppressWarnings("unchecked")
    @Override
    public void changeVariables(Object source, Map variables) {
        // Sets the currently selected color
        if (variables.containsKey("colorname") && !isReadOnly()) {
            final String newValue = (String) variables.get("colorname");
            // Changing the property of the component will
            // trigger a ValueChangeEvent
            setValue(newValue, true);
        }
    }
}
