package com.vaadin.tests.tickets;

import com.vaadin.Application;
import com.vaadin.ui.Root.LegacyWindow;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class Ticket1940 extends Application.LegacyApplication {

    @Override
    public void init() {
        final LegacyWindow w = new LegacyWindow(getClass().getName());
        setMainWindow(w);

        final VerticalLayout l = new VerticalLayout();
        l.setWidth("200px");
        l.setHeight(null);
        TextField t = new TextField();
        l.addComponent(t);
        t.setRequired(true);
        w.addComponent(l);

    }

}
