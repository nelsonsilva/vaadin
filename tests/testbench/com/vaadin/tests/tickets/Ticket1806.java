package com.vaadin.tests.tickets;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Root;
import com.vaadin.ui.TextField;

public class Ticket1806 extends com.vaadin.Application.LegacyApplication {

    @Override
    public void init() {
        final Root main = new Root(getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1));
        setMainWindow(main);

        final ObjectProperty<String> prop = new ObjectProperty<String>("");
        final TextField tf1 = new TextField(
                "Buffered TextField bound to ObjectProperty");
        tf1.setWriteThrough(false);
        tf1.setReadThrough(false);
        tf1.setPropertyDataSource(prop);
        main.addComponent(tf1);
        main.addComponent(new Button(
                "This button does nothing (but flushes queued variable changes)"));
        main.addComponent(new Button("Commit the field to property",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        tf1.commit();
                    }
                }));
        main.addComponent(new Button("Show property value",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        main.showNotification("'" + prop.getValue() + "'");
                    }
                }));
        main.addComponent(new Button("Show field value",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        main.showNotification("'" + tf1.getValue() + "'");
                    }
                }));
    }
}