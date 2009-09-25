package com.vaadin.tests.components.datefield;

import java.util.Calendar;
import java.util.Locale;

import com.vaadin.tests.components.TestBase;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class DateFieldReadOnly extends TestBase {

    @Override
    protected String getDescription() {
        return "A read-only DateField should not show the popup button and not be editable.";
    }

    @Override
    protected Integer getTicketNumber() {
        return 3163;
    }

    @Override
    protected void setup() {
        final DateField timeField = new DateField("A read-only datefield");
        timeField.setResolution(DateField.RESOLUTION_SEC);
        timeField.setDateFormat("HH:mm:ss");
        timeField.setCaption(null);
        timeField.setIcon(null);
        timeField.setWidth("8em");
        timeField.addStyleName("timeField");

        // Set date so that testing always has same time
        Calendar c = Calendar.getInstance(Locale.ENGLISH);
        c.set(2009, 05, 12);

        timeField.setValue(c.getTime());
        timeField.setReadOnly(true);

        addComponent(timeField);

        Button b = new Button("Switch read-only");
        b.addListener(new ClickListener() {

            public void buttonClick(ClickEvent event) {
                timeField.setReadOnly(!timeField.isReadOnly());
            }
        });

        addComponent(b);
    }
}
