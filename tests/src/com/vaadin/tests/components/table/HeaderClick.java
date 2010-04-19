package com.vaadin.tests.components.table;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.tests.components.TestBase;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Table.HeaderClickEvent;

@SuppressWarnings("serial")
public class HeaderClick extends TestBase {

    @Override
    protected void setup() {
        final Table table = new Table();
        table.setContainerDataSource(createContainer());
        table.setWidth("400px");
        table.setHeight("400px");
        table.setImmediate(true);

        final TextField columnField = new TextField(
                "ProperyId of clicked column");

        // Add header click handler
        table.setHeaderClickHandler(new Table.HeaderClickHandler() {
            public void handleHeaderClick(HeaderClickEvent event) {
                columnField.setValue(event.getPropertyId());
            }
        });


        addComponent(table);
        addComponent(columnField);

    }

    @Override
    protected String getDescription() {
        return "Tests the header click handler";
    }

    @Override
    protected Integer getTicketNumber() {
        return 4515;
    }

    private Container createContainer() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty("col1", String.class, "");
        container.addContainerProperty("col2", String.class, "");
        container.addContainerProperty("col3", String.class, "");

        for (int i = 0; i < 100; i++) {
            Item item = container.addItem("item " + i);
            item.getItemProperty("col1").setValue("first" + i);
            item.getItemProperty("col2").setValue("middle" + i);
            item.getItemProperty("col3").setValue("last" + i);
        }

        return container;
    }

}
