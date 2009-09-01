package com.vaadin.tests.components.button;

import com.vaadin.data.Item;
import com.vaadin.tests.components.TestBase;
import com.vaadin.ui.Button;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Table;

public class ButtonUndefinedWidth extends TestBase {

    @Override
    protected String getDescription() {
        return "Both the button outside the table and inside the table should be only as wide as necessary. There should be empty space in the table to the right of the button.";
    }

    @Override
    protected Integer getTicketNumber() {
        return 3257;
    }

    @Override
    protected void setup() {
        Button b = new Button("Undefined wide");
        addComponent(b);
        NativeButton b2 = new NativeButton("Undefined wide");
        addComponent(b2);

        Table t = new Table();
        t.addContainerProperty("A", Button.class, null);
        t.setWidth("500px");

        Item i = t.addItem("1");
        i.getItemProperty("A").setValue(new Button("Undef wide"));
        Item i2 = t.addItem("2");
        i2.getItemProperty("A").setValue(new NativeButton("Undef wide"));

        addComponent(t);
    }

}
