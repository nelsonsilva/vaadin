package com.vaadin.tests.components.table;

import com.vaadin.Application;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class TableExtraScrollbars extends Application {

    private static int PROPS = 15;
    private static int ROWS = 1000;

    @Override
    public void init() {
        setTheme("runo");
        Window w = new Window("Table scrollbars bug example");
        setMainWindow(w);

        VerticalLayout vl = new VerticalLayout();
        vl.setSizeFull();
        vl.addComponent(createTable());
        w.setContent(vl);
    }

    protected Table createTable() {
        Table table = new Table(null, createContainer());
        table.setSizeFull();
        table.setPageLength(50);
        table.setColumnReorderingAllowed(true);
        table.setSelectable(true);
        return table;
    }

    protected Container createContainer() {
        Container container = new IndexedContainer();
        for (int i = 0; i < PROPS; ++i) {
            container.addContainerProperty("prop" + i, String.class, null);
        }
        for (int i = 0; i < ROWS; ++i) {
            Item item = container.addItem(i);
            for (int p = 0; p < PROPS; ++p) {
                item.getItemProperty("prop" + p).setValue(
                        "property value 1234567890");
            }
        }
        return container;
    }
}