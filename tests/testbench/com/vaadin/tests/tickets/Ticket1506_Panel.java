package com.vaadin.tests.tickets;

import com.vaadin.data.Container;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;

/**
 * @author Efecte R&D
 * @version $Revision$, $Date$
 */
public class Ticket1506_Panel extends Panel {

    public Ticket1506_Panel() {
        ObjectProperty<String> property1 = new ObjectProperty<String>(null,
                String.class);
        addComponent(initSelect(new Ticket1506_TestContainer(), "Test select",
                property1));
        addComponent(initButton(property1));
        addComponent(initSelect(new Ticket1506_TestContainer2(),
                "Test select 2", new ObjectProperty<String>(null, String.class)));
    }

    private Component initButton(final ObjectProperty<?> property) {
        Button button = new Button("Clear select");
        button.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent event) {
                property.setValue(null);
            }
        });
        return button;
    }

    private Component initSelect(Container containerDataSource, String caption,
            ObjectProperty<?> property) {
        Select select = new Select(caption);
        select.setFilteringMode(Select.FILTERINGMODE_CONTAINS);
        select.setImmediate(true);
        select.setNullSelectionAllowed(false);
        select.setItemCaptionPropertyId(Ticket1506_TestContainer.PROPERTY_2_ID);

        select.setContainerDataSource(containerDataSource);
        select.setPropertyDataSource(property);
        return select;
    }
}
