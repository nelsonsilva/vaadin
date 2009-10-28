package com.vaadin.tests.tickets;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

public class Ticket1805 extends com.vaadin.Application {

    @Override
    public void init() {
        final Window main = new Window(getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1));
        setMainWindow(main);
        main.getLayout().setMargin(false);

        Label description = new Label(
                "GridLayout with 100% (no height), is wanted to "
                        + "share all available width with columns "
                        + "relatively to their natural width. And it "
                        + "should still work with margins and spacings");
        main.addComponent(description);

        final GridLayout grid = new GridLayout(4, 1);

        final TextField size = new TextField("Grid width in css unit");
        size.addListener(new ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                String width = size.getValue().toString();
                if (width == null || width.equals("")) {
                    grid.setSizeUndefined();
                } else {
                    grid.setWidth(width);
                }
            }
        });
        main.addComponent(size);
        main.addComponent(new Button("set size"));

        grid.setMargin(true);
        grid.setSpacing(true);

        grid.addComponent(new Label("WIDE"));
        grid.addComponent(new Label("_I_"));
        grid.addComponent(new Label("VEEEEEEEEEEERY_WIDE"));
        Label label = new Label("|");
        grid.addComponent(label);
        grid.setComponentAlignment(label, GridLayout.ALIGNMENT_RIGHT,
                GridLayout.ALIGNMENT_TOP);
        main.addComponent(grid);
    }

}