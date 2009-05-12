package com.vaadin.tests.components;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public abstract class TestBase extends Application {

    @Override
    public final void init() {
        window = new Window(getClass().getName());
        setMainWindow(window);
        window.getLayout().setSizeFull();

        Label label = new Label(getDescription(), Label.CONTENT_XHTML);
        label.setWidth("100%");
        window.getLayout().addComponent(label);

        layout = new VerticalLayout();
        window.getLayout().addComponent(layout);
        ((VerticalLayout) window.getLayout()).setExpandRatio(layout, 1);

        setup();
    }

    private Window window;
    private SplitPanel splitPanel;
    private Layout layout;

    public TestBase() {

    }

    protected Layout getLayout() {
        return layout;
    }

    protected abstract String getDescription();

    protected abstract Integer getTicketNumber();

    protected abstract void setup();

    protected void addComponent(Component c) {
        getLayout().addComponent(c);
    }

}