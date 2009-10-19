package com.vaadin.tests.components.table;

import com.vaadin.tests.components.TestBase;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class ColumnCollapsingAndColumnExpansion extends TestBase {

    private Table table;

    @Override
    public void setup() {

        table = new Table();

        table.addContainerProperty("Col1", String.class, null);
        table.addContainerProperty("Col2", String.class, null);
        table.addContainerProperty("Col3", String.class, null);
        table.setColumnCollapsingAllowed(true);

        table.setSizeFull();

        for (int y = 1; y < 5; y++) {
            table.addItem(new Object[] { "cell " + 1 + "-" + y,
                    "cell " + 2 + "-" + y, "cell " + 3 + "-" + y, },
                    new Object());
        }

        addComponent(table);

        HorizontalLayout hl = new HorizontalLayout();
        final TextField tf = new TextField("Column name");
        Button hide = new Button("Collapse", new ClickListener() {

            public void buttonClick(ClickEvent event) {
                try {
                    table.setColumnCollapsed(tf.getValue(), true);
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });

        Button show = new Button("Show", new ClickListener() {

            public void buttonClick(ClickEvent event) {
                try {
                    table.setColumnCollapsed(tf.getValue(), false);
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });

        hl.addComponent(tf);
        hl.addComponent(hide);
        hl.addComponent(show);
        hl.setComponentAlignment(tf, Alignment.BOTTOM_LEFT);
        hl.setComponentAlignment(hide, Alignment.BOTTOM_LEFT);
        hl.setComponentAlignment(show, Alignment.BOTTOM_LEFT);
        addComponent(hl);

    }

    @Override
    protected String getDescription() {
        return "After hiding column 2 the remaining columns (1 and 3) should use all available space in the table";
    }

    @Override
    protected Integer getTicketNumber() {
        return 3246;
    }
}
