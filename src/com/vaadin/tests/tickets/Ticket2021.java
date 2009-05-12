package com.vaadin.tests.tickets;

import com.vaadin.Application;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.ExpandLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.OrderedLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class Ticket2021 extends Application {

    private TextField tf1, tf2, tf3;

    private String contents = "This TextField SHOULD FILL the panel and NOT CAUSE any scrollbars to appear in the Panel. Scrollbars SHOULD appear in the TextField AND the whole scrollbars (includinc arrow down) SHOULD be visible.\n\n"
            + ""
            + "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Praesent quis justo. Vivamus nec mi eu libero convallis auctor. Mauris et arcu. Nunc luctus justo. Aenean convallis, odio in vehicula scelerisque, est magna condimentum pede, a aliquam elit eros vitae diam. Phasellus porttitor convallis tellus. Nullam elementum, ligula nec viverra malesuada, risus tortor bibendum dui, eget hendrerit sem enim at massa. Nam eu pede sed nulla congue fermentum. Vestibulum malesuada libero non nunc. Proin rutrum. Fusce erat pede, volutpat vitae, aliquam ut, sagittis vel, augue. Fusce dui pede, convallis nec, accumsan tincidunt, consectetuer ac, purus. Nulla facilisi. Ut nisi. Sed orci risus, lacinia eu, sodales molestie, gravida quis, neque. Vestibulum pharetra ornare elit. Nulla porttitor molestie mauris. Morbi fringilla tellus sed risus. Curabitur varius massa."
            + "Nulla nisi. Sed blandit, ante vitae sagittis volutpat, arcu mauris vehicula risus, vitae posuere felis lectus sit amet purus. Donec nec magna et leo eleifend scelerisque. Suspendisse condimentum pharetra ligula. Curabitur lorem. Pellentesque a augue sit amet enim fermentum placerat. Phasellus ante risus, molestie at, iaculis at, pellentesque non, tellus. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Phasellus non urna eget risus tempus imperdiet. Integer est diam, sagittis sit amet, posuere sit amet, bibendum sed, lacus. Aenean adipiscing cursus ipsum. Quisque at elit. Vestibulum vitae nunc. Praesent placerat metus viverra lorem. Cras nec elit congue nisi faucibus feugiat. Nam eget mi. Vestibulum condimentum. Nunc nisl ante, cursus in, dictum ac, lobortis rutrum, mi. Nulla eu nisi. In ultricies vehicula magna."
            + "Nunc eros dui, elementum at, ullamcorper eget, varius at, velit. Ut dictum. Cras ullamcorper ante vel tortor. Quisque viverra mauris vulputate quam. Nulla dui. Suspendisse non eros at ipsum faucibus hendrerit. Morbi dignissim pharetra tortor. Etiam malesuada. Mauris lacinia elementum erat. Duis mollis placerat metus. Nunc risus felis, cursus ac, cursus vel, convallis vel, metus. Ut vehicula nibh et nulla. Vivamus id pede. Quisque egestas arcu a ligula. Maecenas vehicula. Quisque sed ligula quis tellus tempus rutrum. Curabitur vel augue sed orci egestas pharetra. Duis pharetra.";

    private OrderedLayout orderedLayout;

    @Override
    public void init() {
        Window w = new Window(getClass().getSimpleName());
        w.setLayout(new GridLayout(2, 2));
        setMainWindow(w);

        Panel p = new Panel();
        p.setCaption("ExpandLayout");
        p.setWidth("500px");
        p.setHeight("500px");
        p.setLayout(new ExpandLayout());
        p.getLayout().setSizeFull();

        w.getLayout().addComponent(p);

        tf1 = new TextField();
        tf1.setRows(5);
        tf1.setSizeFull();
        tf1.setValue(contents);
        tf1.setCaption("TextField caption");
        p.getLayout().addComponent(tf1);

        /*
         * 
         * OrderedLayout
         */

        Panel p2 = new Panel();
        p2.setCaption("OrderedLayout");
        p2.setWidth("500px");
        p2.setHeight("500px");
        p2.setLayout(new OrderedLayout());
        p2.getLayout().setSizeFull();

        w.getLayout().addComponent(p2);

        tf2 = new TextField();
        tf2.setRows(5);
        tf2.setSizeFull();
        tf2.setValue(contents);
        tf2.setCaption("TextField caption");
        p2.getLayout().addComponent(tf2);

        /*
         * 
         * GridLayout
         */

        Panel p3 = new Panel();
        p3.setCaption("GridLayout");
        p3.setWidth(500);
        p3.setHeight(500);
        // p3.setLayout(new GridLayout());
        p3.getLayout().setSizeFull();
        p3.getLayout().setMargin(false);

        GridLayout gl = new GridLayout();
        gl.setSizeFull();
        gl.setMargin(false);
        p3.getLayout().addComponent(gl);
        w.getLayout().addComponent(p3);

        tf3 = new TextField();
        tf3.setRows(5);
        tf3.setSizeFull();
        tf3.setValue(contents);
        tf3.setCaption("TextField caption");
        // p3.getLayout().addComponent(tf3);
        gl.addComponent(tf3);

        // Panel pp = new Panel();
        // pp.setCaption("OrderedLayout");
        // pp.setWidth("500px");
        // pp.setHeight("500px");
        // pp.getLayout().setSizeFull();
        // orderedLayout = new OrderedLayout();
        // pp.getLayout().addComponent(orderedLayout);
        // w.getLayout().addComponent(pp);
        // createUI(orderedLayout);
    }

    @SuppressWarnings("unused")
    private void createUI(Layout layout) {
        Label l = new Label("Label");
        Button b = new Button("Enable/disable caption and watch button move",
                new ClickListener() {

                    public void buttonClick(ClickEvent event) {
                        System.out.println("Enable/disable caption");
                        for (AbstractComponent l : new AbstractComponent[] {
                                tf1, tf2, tf3 }) {
                            // AbstractComponent l = tf2;
                            // Layout l = (Layout) event.getButton().getData();
                            if (l.getCaption() == null) {
                                l.setCaption("Expand layout caption");
                            } else {
                                l.setCaption(null);
                            }
                        }
                    }

                });
        b.setData(layout);
        Label l2 = new Label("This should always be visible");

        layout.addComponent(l);
        layout.addComponent(b);
        layout.addComponent(l2);

        if (layout instanceof ExpandLayout) {
            ((ExpandLayout) layout).expand(l);

        }
    }
}