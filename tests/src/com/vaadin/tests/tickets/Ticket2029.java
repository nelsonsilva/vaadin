package com.vaadin.tests.tickets;

import java.util.Random;

import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class Ticket2029 extends Application {

    int COMPONENTS;
    int DIM1, DIM2;
    Random r = new Random();

    @Override
    public void init() {
        COMPONENTS = 5;
        DIM1 = 504;
        DIM2 = 100;

        Window w = new Window(getClass().getSimpleName());
        setMainWindow(w);
        // setTheme("tests-tickets");
        Panel p = createPanel();
        w.getLayout().addComponent(p);
        // w.getLayout().addComponent(createGLPanel());
        w.getLayout().addComponent(createPanelV());
    }

    private Panel createPanel() {
        Panel p = new Panel(DIM1 + "x" + DIM2 + " OrderedLayout");
        p.setWidth(DIM1 + "px");
        p.setHeight(DIM2 + "px");

        HorizontalLayout layout = new HorizontalLayout();
        p.setLayout(layout);
        p.getLayout().setSizeFull();

        for (int i = 0; i < COMPONENTS; i++) {
            TextField tf = new TextField();
            if (r.nextBoolean()) {
                tf.setCaption("Caption");
            }
            if (r.nextBoolean()) {
                tf.setRequired(true);
            }
            if (r.nextBoolean()) {
                tf.setComponentError(new UserError("Error"));
            }
            tf.setWidth("100%");
            layout.setComponentAlignment(tf, Alignment.BOTTOM_LEFT);
            p.addComponent(tf);

        }

        return p;
    }

    private Panel createGLPanel() {
        Panel p = new Panel("" + DIM1 + "x" + DIM2 + " GridLayout");
        p.setWidth("" + DIM1 + "px");
        p.setHeight("" + DIM2 + "px");

        GridLayout layout = new GridLayout(COMPONENTS, 1);
        p.setLayout(layout);
        p.getLayout().setSizeFull();

        for (int i = 0; i < COMPONENTS; i++) {
            TextField tf = new TextField();
            tf.setImmediate(true);
            tf.addListener(new ValueChangeListener() {

                public void valueChange(ValueChangeEvent event) {
                    Component c = ((Component) event.getProperty());
                    c.setCaption("askfdj");

                }
            });
            if (r.nextBoolean()) {
                tf.setCaption("Caption");
            }
            if (r.nextBoolean()) {
                tf.setRequired(true);
            }
            if (r.nextBoolean()) {
                tf.setComponentError(new UserError("Error"));
            }
            tf.setWidth("100%");
            layout.setComponentAlignment(tf, Alignment.MIDDLE_LEFT);
            p.addComponent(tf);

        }

        return p;
    }

    private Panel createPanelV() {
        Panel p = new Panel("" + DIM1 + "x" + DIM2 + " OrderedLayout");
        p.setWidth("" + DIM2 + "px");
        p.setHeight("" + DIM1 + "px");

        VerticalLayout layout = new VerticalLayout();
        p.setLayout(layout);
        p.getLayout().setSizeFull();

        for (int i = 0; i < COMPONENTS; i++) {
            TextField tf = new TextField();
            if (r.nextBoolean()) {
                tf.setCaption("Caption");
            }
            if (r.nextBoolean()) {
                tf.setRequired(true);
            }
            if (r.nextBoolean()) {
                tf.setComponentError(new UserError("Error"));
            }

            tf.setRows(2);
            tf.setSizeFull();

            layout.setComponentAlignment(tf, Alignment.BOTTOM_LEFT);
            p.addComponent(tf);

        }

        return p;
    }
}
