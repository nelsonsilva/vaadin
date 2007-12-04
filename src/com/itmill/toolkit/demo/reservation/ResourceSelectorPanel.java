package com.itmill.toolkit.demo.reservation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.itmill.toolkit.data.Container;
import com.itmill.toolkit.data.Item;
import com.itmill.toolkit.ui.Button;
import com.itmill.toolkit.ui.Layout;
import com.itmill.toolkit.ui.OrderedLayout;
import com.itmill.toolkit.ui.Panel;
import com.itmill.toolkit.ui.Button.ClickEvent;

public class ResourceSelectorPanel extends Panel implements
        Button.ClickListener {
    private HashMap categoryLayouts = new HashMap();
    private HashMap categoryResources = new HashMap();

    private Container allResources;
    private LinkedList selectedResources = null;

    public ResourceSelectorPanel(String caption) {
        super(caption, new OrderedLayout(OrderedLayout.ORIENTATION_HORIZONTAL));
        addStyleName(Panel.STYLE_LIGHT);
    }

    public void setResourceContainer(Container resources) {
        removeAllComponents();
        categoryLayouts.clear();
        categoryResources.clear();
        if (resources != null && resources.size() > 0) {
            for (Iterator it = resources.getItemIds().iterator(); it.hasNext();) {
                Item resource = resources.getItem(it.next());
                Integer id = (Integer) resource.getItemProperty(
                        SampleDB.Resource.PROPERTY_ID_ID).getValue();
                String category = (String) resource.getItemProperty(
                        SampleDB.Resource.PROPERTY_ID_CATEGORY).getValue();
                String name = (String) resource.getItemProperty(
                        SampleDB.Resource.PROPERTY_ID_NAME).getValue();
                String description = (String) resource.getItemProperty(
                        SampleDB.Resource.PROPERTY_ID_DESCRIPTION).getValue();
                Button rButton = new Button(name, this);
                rButton.setStyleName("link");
                rButton.setDescription(description);
                rButton.setData(resource);
                Layout resourceLayout = (Layout) categoryLayouts.get(category);
                LinkedList resourceList = (LinkedList) categoryResources
                        .get(category);
                if (resourceLayout == null) {
                    resourceLayout = new OrderedLayout();
                    resourceLayout.setMargin(true);
                    addComponent(resourceLayout);
                    categoryLayouts.put(category, resourceLayout);
                    resourceList = new LinkedList();
                    categoryResources.put(category, resourceList);
                    Button cButton = new Button(category + " (any)", this);
                    cButton.setStyleName("important-link");
                    cButton.setData(category);
                    resourceLayout.addComponent(cButton);
                }
                resourceLayout.addComponent(rButton);
                resourceList.add(resource);
            }
        }
    }

    // Selects one initial categore, inpractice randomly
    public void selectFirstCategory() {
        try {
            Object catId = categoryResources.keySet().iterator().next();
            LinkedList res = (LinkedList) categoryResources.get(catId);
            Layout l = (Layout) categoryLayouts.get(catId);
            Button catB = (Button) l.getComponentIterator().next();
            setSelectedResources(res);
            catB.setStyleName("selected-link");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void setSelectedResources(LinkedList resources) {
        selectedResources = resources;
        fireEvent(new SelectedResourcesChangedEvent());
    }

    public LinkedList getSelectedResources() {
        return selectedResources;
    }

    public void buttonClick(ClickEvent event) {
        Object source = event.getSource();
        if (source instanceof Button) {
            Object data = ((Button) source).getData();
            String name = ((Button) source).getCaption();
            resetStyles();
            if (data instanceof Item) {
                LinkedList rlist = new LinkedList();
                rlist.add(data);
                setSelectedResources(rlist);
            } else {
                String category = (String) data;
                LinkedList resources = (LinkedList) categoryResources
                        .get(category);
                setSelectedResources(resources);
            }
            ((Button) source).setStyleName("selected-link");
        }

    }

    private void resetStyles() {
        for (Iterator it = categoryLayouts.values().iterator(); it.hasNext();) {
            Layout lo = (Layout) it.next();
            for (Iterator bit = lo.getComponentIterator(); bit.hasNext();) {
                Button b = (Button) bit.next();
                if (b.getData() instanceof Item) {
                    b.setStyleName("link");
                } else {
                    b.setStyleName("important-link");
                }
            }
        }

    }

    public class SelectedResourcesChangedEvent extends Event {
        public SelectedResourcesChangedEvent() {
            super(ResourceSelectorPanel.this);
        }
    }
}
