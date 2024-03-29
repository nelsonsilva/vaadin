package com.vaadin.tests.tickets;

import java.util.Date;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Root.LegacyWindow;

public class Ticket2106 extends Application.LegacyApplication {

    private static CustomizedSystemMessages msgs = new Application.CustomizedSystemMessages();
    static {
        // We will forward the user to www.vaadin.com when the session expires
        msgs.setSessionExpiredURL("http://www.vaadin.com");
        msgs.setSessionExpiredMessage(null);
        msgs.setSessionExpiredCaption(null);
    }

    public static Application.SystemMessages getSystemMessages() {
        return msgs;
    }

    @Override
    public void init() {
        setMainWindow(new LegacyWindow("#2106"));
        getMainWindow().addComponent(
                new Button("Do nothing", new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        getMainWindow().addComponent(
                                new Label("Last time did nothing: "
                                        + new Date()));
                    }
                }));
    }

}
