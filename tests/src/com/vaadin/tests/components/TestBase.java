package com.vaadin.tests.components;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public abstract class TestBase extends AbstractTestCase {

    @Override
    public final void init() {
        window = new Window(getClass().getName());
        setMainWindow(window);
        window.getContent().setSizeFull();

        Label label = new Label(getDescription(), Label.CONTENT_XHTML);
        label.setWidth("100%");
        window.getContent().addComponent(label);

        layout = new VerticalLayout();
        window.getContent().addComponent(layout);
        ((VerticalLayout) window.getContent()).setExpandRatio(layout, 1);

        setup();
    }

    private Window window;
    private VerticalLayout layout;

    public TestBase() {

    }

    protected VerticalLayout getLayout() {
        return layout;
    }

    protected abstract void setup();

    protected void addComponent(Component c) {
        getLayout().addComponent(c);
    }

}
