package com.vaadin.tests.components.tabsheet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.vaadin.tests.components.TestBase;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;

public class TabSheetCaptions extends TestBase {

    Panel panel1;

    @Override
    protected String getDescription() {
        return "Updating the tabsheet tab text should not change the caption of the component. Click on the button to change the tab text. This must update the tab and not touch the Panel's caption.";
    }

    @Override
    protected Integer getTicketNumber() {
        return 2846;
    }

    @Override
    protected void setup() {
        final TabSheet tabSheet = new TabSheet();
        // Define date and locale so that it doesn't change for machine/time
        final SimpleDateFormat dateFormatter = new SimpleDateFormat(
                "EEE, yyyy-MMM-dd", Locale.ENGLISH);
        final Date date = new Date();
        date.setTime((long) 1000000000000.0);

        panel1 = new Panel("Panel initial caption (should also be tab caption)");
        panel1.setSizeFull();
        panel1.getContent().setSizeFull();
        panel1.addComponent(new Label("This is a panel"));
        tabSheet.addTab(panel1);

        Button button = new Button("Update tab caption");
        button.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                tabSheet.getTab(panel1).setCaption(
                        "This is a new tab caption "
                                + dateFormatter.format(date));
            }
        });

        Button button2 = new Button("Update panel caption");
        button2.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                panel1.setCaption("This is a new panel caption "
                        + dateFormatter.format(date));
            }
        });

        addComponent(tabSheet);
        addComponent(button);
        addComponent(button2);
    }
}
