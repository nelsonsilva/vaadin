package com.vaadin.tests.tickets;

import com.vaadin.Application;
import com.vaadin.ui.Label;
import com.vaadin.ui.Root;
import com.vaadin.ui.TabSheet;

public class Ticket2098 extends Application.LegacyApplication {

    private static final String info = "First tab hidden, second should initially be selected";

    @Override
    public void init() {
        Root w = new Root(getClass().getSimpleName());
        setMainWindow(w);
        // setTheme("tests-tickets");
        w.addComponent(new Label(info));
        createUI(w);
    }

    private void createUI(Root w) {
        TabSheet ts = new TabSheet();
        Label l1 = new Label("111");
        Label l2 = new Label("222");
        Label l3 = new Label("333");
        Label l4 = new Label("444");

        ts.addTab(l1, "1", null);
        ts.addTab(l2, "2", null);
        ts.addTab(l3, "3", null);
        ts.addTab(l4, "4", null);

        l1.setVisible(false);

        w.addComponent(ts);
    }
}
