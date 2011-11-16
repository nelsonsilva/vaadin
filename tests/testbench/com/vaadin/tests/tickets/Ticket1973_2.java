package com.vaadin.tests.tickets;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Root;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class Ticket1973_2 extends Application.LegacyApplication {
    Root main = new Root();
    Table table = new Table();

    @Override
    public void init() {
        setMainWindow(main);

        final IndexedContainer container1 = new IndexedContainer();
        container1.addContainerProperty("text", String.class, null);
        container1.addContainerProperty("layout", Component.class, null);

        final IndexedContainer container2 = new IndexedContainer();
        container2.addContainerProperty("text", String.class, null);
        container2.addContainerProperty("layout", Component.class, null);

        fill(container1, 100);

        table.setContainerDataSource(container1);

        Button refreshTable = new Button("Change table container");
        refreshTable.addListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent e) {
                table.setContainerDataSource(container2);
                table.setContainerDataSource(container1);
            }
        });

        main.addComponent(table);
        main.addComponent(refreshTable);
    }

    public void fill(IndexedContainer container, int size) {
        for (int i = 0; i < size; i++) {
            int randInt = i;
            Item item = container.addItem(new Integer(i));
            VerticalLayout layout = new VerticalLayout();
            layout.setDebugId("lo" + i);
            layout.addComponent(new Button("Test " + randInt));
            item.getItemProperty("layout").setValue(layout);
        }
    }
}
