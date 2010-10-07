package com.vaadin.tests.tickets;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.Application;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Layout.AlignmentHandler;
import com.vaadin.ui.OrderedLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class Ticket1966_2 extends Application {

    private static final int LEFT = OrderedLayout.ALIGNMENT_LEFT;
    private static final int CENTER = OrderedLayout.ALIGNMENT_HORIZONTAL_CENTER;
    private static final int RIGHT = OrderedLayout.ALIGNMENT_RIGHT;
    private static final int TOP = OrderedLayout.ALIGNMENT_TOP;
    private static final int VCENTER = OrderedLayout.ALIGNMENT_VERTICAL_CENTER;
    private static final int BOTTOM = OrderedLayout.ALIGNMENT_BOTTOM;

    private static Map<Integer, String> names = new HashMap<Integer, String>();
    static {
        names.put(new Integer(LEFT), "Left");
        names.put(new Integer(CENTER), "Center");
        names.put(new Integer(RIGHT), "Right");
        names.put(new Integer(BOTTOM), "Bottom");
        names.put(new Integer(VCENTER), "Vcenter");
        names.put(new Integer(TOP), "Top");
    }

    @Override
    public void init() {
        Window w = new Window(getClass().getName());
        setMainWindow(w);
        w.setLayout(new GridLayout(2, 2));

        // Panel p = new Panel("test");
        // p.setWidth(500);
        // p.setHeight(500);
        // p.setLayout(new GridLayout(1, 2));
        // p.getLayout().setSizeFull();
        //
        // p.addComponent(new Button("asjkdfhakshdf"));
        // p.addComponent(new Button("öalijgto8aq5"));

        // GridLayout gl = new GridLayout(4, 1);
        // // gl.setCaption("Vertical");
        // gl.setWidth("100%");
        // gl.setHeight(500);

        // addButtons(gl);
        // gl.addComponent(new Label("abc"));
        // p.addComponent(gl);

        // w.getLayout().addComponent(p);
        createUI(w.getLayout());
    }

    private void createUI(Layout layout) {
        orderedLayout(layout);
        gridLayout(layout);
        expandLayout(layout);
    }

    private void gridLayout(Layout layout) {
        Panel p = new Panel("GridLayout");
        p.setWidth(500);
        p.setHeight(500);
        p.getLayout().setSizeFull();
        layout.addComponent(p);

        GridLayout gl = new GridLayout(1, 4);
        gl.setCaption("Horizontal");
        gl.setWidth("100%");

        // Button b;

        // b = new Button("Wide button");
        // b.setWidth("500");
        // gl.addComponent(b);

        addButtons(gl);

        p.addComponent(gl);

        /* VERTICAL */

        gl = new GridLayout(4, 1);
        // gl.setCaption("Vertical");
        gl.setHeight("100%");
        addButtons(gl);

        // Button b = new Button("High button");
        // b.setHeight(200);
        // gl.addComponent(b);

        p.addComponent(gl);

    }

    private void orderedLayout(Layout layout) {
        Panel p = new Panel("OrderedLayout");
        p.setWidth(500);
        p.setHeight(500);
        p.getLayout().setWidth("100%");
        layout.addComponent(p);

        AbstractOrderedLayout ol = new VerticalLayout();
        // ol.setCaption("Horizontal");
        ol.setWidth("100%");
        addButtons(ol);
        p.addComponent(ol);

        /* VERTICAL */

        ol = new HorizontalLayout();
        // ol.setCaption("Vertical");
        ol.setHeight(200);
        addButtons(ol);
        // Button b = new Button("High button");
        // b.setHeight(200);
        // ol.addComponent(b);
        p.addComponent(ol);

    }

    private void expandLayout(Layout layout) {
        Panel p = new Panel("ExpandLayout");
        layout.addComponent(p);
        p.getLayout().setWidth("500");
        p.getLayout().setHeight("400");

        AbstractOrderedLayout el = new VerticalLayout();
        // el.setCaption("Horizontal");
        // el.setSizeUndefined();
        // el.setWidth("100%");
        // ol.setWidth("100%");
        Button b;

        b = new Button("Wide button");
        b.setWidth("100%");
        // b.setHeight(200);
        // el.setExpandRatio(b,1);
        // el.addComponent(b);

        addButtons(el);
        p.addComponent(el);

        /* VERTICAL */

        el = new HorizontalLayout();
        // el.setHeight(400);
        // el.setWidth("100%");
        // el.setCaption("Vertical");

        addButtons(el);
        // b = new Button("High button");
        // el.setExpandRatio(b,1);
        // b.setHeight(100);
        // el.addComponent(b);

        p.addComponent(el);

    }

    private void addButtons(Layout ol) {
        ol.addComponent(getButton(ol, LEFT, TOP));
        ol.addComponent(getButton(ol, CENTER, VCENTER));
        ol.addComponent(getButton(ol, RIGHT, BOTTOM));

    }

    private Button getButton(Layout l, int hAlign, int vAlign) {
        Button b = new Button(names.get(new Integer(hAlign)) + " - " + names.get(new Integer(vAlign)));
        // b.setWidth("100");
        ((AlignmentHandler) l).setComponentAlignment(b, hAlign, vAlign);

        return b;

    }
}
