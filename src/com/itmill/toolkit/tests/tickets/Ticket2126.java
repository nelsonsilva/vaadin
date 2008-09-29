package com.itmill.toolkit.tests.tickets;

import com.itmill.toolkit.data.Item;
import com.itmill.toolkit.data.util.IndexedContainer;
import com.itmill.toolkit.ui.Button;
import com.itmill.toolkit.ui.Component;
import com.itmill.toolkit.ui.Label;
import com.itmill.toolkit.ui.Table;
import com.itmill.toolkit.ui.Window;

/**
 * 
 * Toggling container with an empty one may result duplicate header cell in
 * client.
 * 
 */
public class Ticket2126 extends com.itmill.toolkit.Application {

    Window main = new Window();
    Table table = new Table();

    public void init() {
        setMainWindow(main);

        final IndexedContainer container1 = new IndexedContainer();
        container1.addContainerProperty("text", Component.class, null);
        final IndexedContainer container2 = new IndexedContainer();

        // Case #2 Try to comment the following line for another type of strange
        // behaviour
        container2.addContainerProperty("text", Component.class, null);

        for (int i = 0; i < 100; i++) {
            Item item = container1.addItem(i);
            item.getItemProperty("text").setValue(new Label("Test " + i));
        }

        table.setContainerDataSource(container1);

        // workaround for case #2
        // table.setWidth("300px");
        // table.setHeight("300px");

        Button refreshTable = new Button("Switch table container");
        refreshTable.addListener(new Button.ClickListener() {
            boolean full = true;

            public void buttonClick(Button.ClickEvent e) {
                if (full) {
                    table.setContainerDataSource(container2);
                } else {
                    table.setContainerDataSource(container1);
                }
                full = !full;
            }
        });

        main.addComponent(table);
        main.addComponent(refreshTable);
    }
}