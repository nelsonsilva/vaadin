package com.vaadin.tests.tickets;

import com.vaadin.Application;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Select;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class Ticket1230 extends Application {

    private static final Object PROPERTY_ID = new Object();
    private static final Object NULL_ITEM_ID = new Object();
    private Select selectWithoutNullItem;
    private Select selectWithNullItem;

    @Override
    public void init() {
        Window w = new Window(getClass().getName());
        setMainWindow(w);

        GridLayout layout = new GridLayout(5, 5);
        w.setLayout(layout);

        layout.setSpacing(true);

        {
            selectWithoutNullItem = createSelect();

            layout.addComponent(selectWithoutNullItem);
            Button b = new Button("Select NULL_PROPERTY", new ClickListener() {

                public void buttonClick(ClickEvent event) {
                    selectWithoutNullItem.select(NULL_ITEM_ID);

                }
            });
            layout.addComponent(b);
            b = new Button("Select 1", new ClickListener() {

                public void buttonClick(ClickEvent event) {
                    selectWithoutNullItem.select("1");

                }
            });
            layout.addComponent(b);
            b = new Button("Select 2", new ClickListener() {

                public void buttonClick(ClickEvent event) {
                    selectWithoutNullItem.select("2");

                }
            });
            layout.addComponent(b);

            b = new Button("Select null", new ClickListener() {

                public void buttonClick(ClickEvent event) {
                    selectWithoutNullItem.select(null);

                }
            });
            layout.addComponent(b);
        }

        {
            selectWithNullItem = createSelect();
            Item nullItem = selectWithNullItem.addItem(NULL_ITEM_ID);
            nullItem.getItemProperty(PROPERTY_ID).setValue("NULL");
            selectWithNullItem.setNullSelectionItemId(NULL_ITEM_ID);

            layout.addComponent(selectWithNullItem);
            Button b = new Button("Select NULL_PROPERTY", new ClickListener() {

                public void buttonClick(ClickEvent event) {
                    selectWithNullItem.select(NULL_ITEM_ID);

                }
            });
            layout.addComponent(b);

            b = new Button("Select 1", new ClickListener() {

                public void buttonClick(ClickEvent event) {
                    selectWithNullItem.select("1");

                }
            });
            layout.addComponent(b);
            b = new Button("Select 2", new ClickListener() {

                public void buttonClick(ClickEvent event) {
                    selectWithNullItem.select("2");

                }
            });
            layout.addComponent(b);

            b = new Button("Select null", new ClickListener() {

                public void buttonClick(ClickEvent event) {
                    selectWithNullItem.select(null);

                }
            });
            layout.addComponent(b);

        }
    }

    private Select createSelect() {
        Select select = new Select();
        select.setMultiSelect(false);
        select.addContainerProperty(PROPERTY_ID, String.class, "");
        select.setItemCaptionPropertyId(PROPERTY_ID);

        Item item1 = select.addItem("1");
        item1.getItemProperty(PROPERTY_ID).setValue("1");
        Item item2 = select.addItem("2");
        item2.getItemProperty(PROPERTY_ID).setValue("2");

        select.setNullSelectionAllowed(true);

        return select;
    }

}