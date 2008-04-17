package com.itmill.toolkit.demo.reservation.simple;

import com.itmill.toolkit.data.Item;
import com.itmill.toolkit.data.Property.ValueChangeEvent;
import com.itmill.toolkit.ui.Button;
import com.itmill.toolkit.ui.ComboBox;
import com.itmill.toolkit.ui.OrderedLayout;
import com.itmill.toolkit.ui.TextField;
import com.itmill.toolkit.ui.AbstractSelect.NewItemHandler;
import com.itmill.toolkit.ui.Button.ClickEvent;
import com.itmill.toolkit.ui.Button.ClickListener;

public class AdminView extends OrderedLayout {

    private ComboBox resources = new ComboBox(
            "Select for editing or type new resource");
    private SimpleReserver application;
    private OrderedLayout form = new OrderedLayout();
    private Button save = new Button("Save resource");

    private TextField name = new TextField("Name:");
    private TextField desc = new TextField("Description:");
    protected Item editedItem;

    AdminView(SimpleReserver app) {
        setWidth("280px");

        application = app;

        resources.setImmediate(true);
        resources.setFilteringMode(ComboBox.FILTERINGMODE_CONTAINS);
        refreshList();
        resources.setItemCaptionMode(ComboBox.ITEM_CAPTION_MODE_PROPERTY);
        resources.setItemCaptionPropertyId(SampleDB.Resource.PROPERTY_ID_NAME);
        resources.setNewItemsAllowed(true);
        resources.setNewItemHandler(new NewItemHandler() {
            public void addNewItem(String newItemCaption) {
                name.setValue(newItemCaption);
                desc.setValue("");
                form.setVisible(true);
            }
        });

        resources.addListener(new ComboBox.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                if (resources.getValue() != null) {
                    editedItem = resources.getItem(resources.getValue());

                    name
                            .setPropertyDataSource(editedItem
                                    .getItemProperty(SampleDB.Resource.PROPERTY_ID_NAME));
                    desc
                            .setPropertyDataSource(editedItem
                                    .getItemProperty(SampleDB.Resource.PROPERTY_ID_DESCRIPTION));

                    form.setVisible(true);

                } else {
                    form.setVisible(false);
                    editedItem = null;
                }

            }
        });
        addComponent(resources);

        form.setVisible(false);
        addComponent(form);
        form.addComponent(name);
        form.addComponent(desc);
        name.setWidth("100%");
        desc.setWidth("100%");
        form.addComponent(save);
        save.addListener(new ClickListener() {
            public void buttonClick(ClickEvent event) {
                if (editedItem == null) {
                    // save
                    int addResource = application.getDb().addResource(
                            name.getValue().toString(),
                            desc.getValue().toString());
                } else {
                    // update
                    application.getDb().updateResource(editedItem,
                            name.getValue().toString(),
                            desc.getValue().toString());
                }
                resources.setValue(null);
                refreshList();
            }
        });

    }

    private void refreshList() {
        resources
                .setContainerDataSource(application.getDb().getResources(null));
    }
}
