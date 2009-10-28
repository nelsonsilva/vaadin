/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.automatedtests.featurebrowser;

import java.util.HashMap;
import java.util.Iterator;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Select;
import com.vaadin.ui.SplitPanel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

/**
 * 
 * @author IT Mill Ltd.
 * @see com.vaadin.ui.Window
 */
@SuppressWarnings("serial")
public class FeatureBrowser extends com.vaadin.Application implements
        Select.ValueChangeListener {

    // Property IDs

    private static final Object PROPERTY_ID_CATEGORY = "Category";
    private static final Object PROPERTY_ID_NAME = "Name";
    private static final Object PROPERTY_ID_DESC = "Description";
    private static final Object PROPERTY_ID_CLASS = "Class";
    private static final Object PROPERTY_ID_VIEWED = "Viewed";

    // Global components
    private Tree tree;
    private Table table;
    private TabSheet ts;

    // Example "cache"
    private final HashMap<Class<?>, Component> exampleInstances = new HashMap<Class<?>, Component>();
    private String section;

    // List of examples
    private static final Object[][] demos = new Object[][] {
    // Category, Name, Desc, Class, Viewed
            // Getting started: Labels
            { "Getting started", "Labels", "Some variations of Labels",
                    LabelExample.class },
            // Getting started: Buttons
            { "Getting started", "Buttons and links",
                    "Various Buttons and Links", ButtonExample.class },
            // Getting started: Fields
            { "Getting started", "Basic value input",
                    "TextFields, DateFields, and such", ValueInputExample.class },
            //
            { "Getting started", "RichText", "Rich text editing",
                    RichTextExample.class },
            // Getting started: Selects
            { "Getting started", "Choices, choices",
                    "Some variations of simple selects", SelectExample.class },
            // Layouts
            { "Layouts", "Basic layouts", "Laying out components",
                    LayoutExample.class },
            // Layouts
            { "Layouts", "Accordion", "Play the Accordion!",
                    AccordionExample.class },
            // Wrangling data: ComboBox
            { "Wrangling data", "ComboBox", "ComboBox - the swiss army select",
                    ComboBoxExample.class },
            // Wrangling data: Table
            {
                    "Wrangling data",
                    "Table (\"grid\")",
                    "Table with bells, whistles, editmode and actions (contextmenu)",
                    TableExample.class },
            // Wrangling data: Form
            { "Wrangling data", "Form", "Every application needs forms",
                    FormExample.class },
            // Wrangling data: Tree
            { "Wrangling data", "Tree", "A hierarchy of things",
                    TreeExample.class },
            // Misc: Notifications
            { "Misc", "Notifications", "Notifications can improve usability",
                    NotificationExample.class },
            // Misc: Caching
            { "Misc", "Client caching", "Demonstrating of client-side caching",
                    ClientCachingExample.class },
            // Misc: Embedded
            { "Misc", "Embedding",
                    "Embedding resources - another site in this case",
                    EmbeddedBrowserExample.class },
            // Windowing
            { "Misc", "Windowing", "About windowing", WindowingExample.class },
            // JavaScript API
            { "Misc", "JavaScript API", "JavaScript to Vaadin communication",
                    JavaScriptAPIExample.class },
    // END
    };

    @Override
    public void init() {

        // Need to set a theme for ThemeResources to work
        setTheme("example");

        // Create new window for the application and give the window a visible.
        final Window main = new Window("Vaadin 6");
        main.setDebugId("mainWindow");
        // set as main window
        setMainWindow(main);

        final SplitPanel split = new SplitPanel(
                SplitPanel.ORIENTATION_HORIZONTAL);
        split.setSplitPosition(200, SplitPanel.UNITS_PIXELS);
        main.setContent(split);

        final HashMap<String, Object> sectionIds = new HashMap<String, Object>();
        final HierarchicalContainer container = createContainer();
        final Object rootId = container.addItem();
        Item item = container.getItem(rootId);
        Property p = item.getItemProperty(PROPERTY_ID_NAME);
        p.setValue("All examples");
        for (int i = 0; i < demos.length; i++) {
            final Object[] demo = demos[i];
            final String section = (String) demo[0];
            Object sectionId;
            if (sectionIds.containsKey(section)) {
                sectionId = sectionIds.get(section);
            } else {
                sectionId = container.addItem();
                sectionIds.put(section, sectionId);
                container.setParent(sectionId, rootId);
                item = container.getItem(sectionId);
                p = item.getItemProperty(PROPERTY_ID_NAME);
                p.setValue(section);
            }
            final Object id = container.addItem();
            container.setParent(id, sectionId);
            initItem(container.getItem(id), demo);

        }

        tree = new Tree();
        tree.setDebugId("FeatureBrowser: Main Tree");
        tree.setSelectable(true);
        tree.setMultiSelect(false);
        tree.setNullSelectionAllowed(false);
        tree.setContainerDataSource(container);
        tree.setItemCaptionMode(AbstractSelect.ITEM_CAPTION_MODE_PROPERTY);
        tree.setItemCaptionPropertyId(PROPERTY_ID_NAME);
        tree.addListener(this);
        tree.setImmediate(true);
        tree.expandItemsRecursively(rootId);
        for (Iterator<?> i = container.getItemIds().iterator(); i.hasNext();) {
            Object id = i.next();
            if (container.getChildren(id) == null) {
                tree.setChildrenAllowed(id, false);
            }
        }

        split.addComponent(tree);

        final SplitPanel split2 = new SplitPanel();
        split2.setSplitPosition(200, SplitPanel.UNITS_PIXELS);
        split.addComponent(split2);

        table = new Table();
        table.setDebugId("FeatureBrowser: Main Table");
        table.setSizeFull();
        table.setColumnReorderingAllowed(true);
        table.setColumnCollapsingAllowed(true);
        table.setSelectable(true);
        table.setMultiSelect(false);
        table.setNullSelectionAllowed(false);
        try {
            table.setContainerDataSource((IndexedContainer) container.clone());
        } catch (final Exception e) {
            e.printStackTrace(System.err);
        }
        // Hide some columns
        table.setVisibleColumns(new Object[] { PROPERTY_ID_CATEGORY,
                PROPERTY_ID_NAME, PROPERTY_ID_DESC, PROPERTY_ID_VIEWED });
        table.addListener(this);
        table.setImmediate(true);
        split2.addComponent(table);

        final VerticalLayout exp = new VerticalLayout();
        exp.setSizeFull();
        exp.setMargin(true);
        split2.addComponent(exp);

        final HorizontalLayout wbLayout = new HorizontalLayout();
        Button b = new Button("Open in sub-window", new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                Component component = (Component) ts.getComponentIterator()
                        .next();
                String caption = ts.getTab(component).getCaption();
                try {
                    component = component.getClass().newInstance();
                } catch (Exception e) {
                    // Could not create
                    return;
                }
                Window w = new Window(caption);
                w.setWidth("640px");
                if (Layout.class.isAssignableFrom(component.getClass())) {
                    w.setContent((Layout) component);
                } else {
                    // w.getLayout().getSize().setSizeFull();
                    w.addComponent(component);
                }
                getMainWindow().addWindow(w);
            }
        });
        b.setStyleName(Button.STYLE_LINK);
        wbLayout.addComponent(b);
        b = new Button("Open in native window", new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                Component component = (Component) ts.getComponentIterator()
                        .next();
                final String caption = ts.getTab(component).getCaption();
                Window w = getWindow(caption);
                if (w == null) {
                    try {
                        component = component.getClass().newInstance();
                    } catch (final Exception e) {
                        // Could not create
                        return;
                    }
                    w = new Window(caption);
                    w.setName(caption);
                    if (Layout.class.isAssignableFrom(component.getClass())) {
                        w.setContent((Layout) component);
                    } else {
                        // w.getLayout().getSize().setSizeFull();
                        w.addComponent(component);
                    }
                    addWindow(w);
                }
                getMainWindow().open(new ExternalResource(w.getURL()), caption);
            }
        });
        b.setStyleName(Button.STYLE_LINK);
        wbLayout.addComponent(b);

        exp.addComponent(wbLayout);
        exp.setComponentAlignment(wbLayout, Alignment.TOP_RIGHT);

        ts = new TabSheet();
        ts.setSizeFull();
        ts.addTab(new Label(""), "Choose example", null);
        exp.addComponent(ts);
        exp.setExpandRatio(ts, 1);

        final Label status = new Label(
                "<a href=\"http://www.vaadin.com/learn\">30 Seconds to Vaadin</a>"
                        + " | <a href=\"http://www.vaadin.com/book\">Book of Vaadin</a>");
        status.setContentMode(Label.CONTENT_XHTML);
        exp.addComponent(status);
        exp.setComponentAlignment(status, Alignment.MIDDLE_RIGHT);

        // select initial section ("All")
        tree.setValue(rootId);

        getMainWindow()
                .showNotification(
                        "Welcome",
                        "Choose an example to begin.<br/><br/>And remember to experiment!",
                        Window.Notification.TYPE_TRAY_NOTIFICATION);
    }

    private void initItem(Item item, Object[] data) {
        int p = 0;
        Property prop = item.getItemProperty(PROPERTY_ID_CATEGORY);
        prop.setValue(data[p++]);
        prop = item.getItemProperty(PROPERTY_ID_NAME);
        prop.setValue(data[p++]);
        prop = item.getItemProperty(PROPERTY_ID_DESC);
        prop.setValue(data[p++]);
        prop = item.getItemProperty(PROPERTY_ID_CLASS);
        prop.setValue(data[p++]);
    }

    private HierarchicalContainer createContainer() {
        final HierarchicalContainer c = new HierarchicalContainer();
        c.addContainerProperty(PROPERTY_ID_CATEGORY, String.class, null);
        c.addContainerProperty(PROPERTY_ID_NAME, String.class, "");
        c.addContainerProperty(PROPERTY_ID_DESC, String.class, "");
        c.addContainerProperty(PROPERTY_ID_CLASS, Class.class, null);
        c.addContainerProperty(PROPERTY_ID_VIEWED, Embedded.class, null);
        return c;
    }

    public void valueChange(ValueChangeEvent event) {
        if (event.getProperty() == tree) {
            final Object id = tree.getValue();
            if (id == null) {
                return;
            }
            final Item item = tree.getItem(id);
            //
            String newSection;
            if (tree.isRoot(id)) {
                newSection = ""; // show all sections
            } else if (tree.hasChildren(id)) {
                newSection = (String) item.getItemProperty(PROPERTY_ID_NAME)
                        .getValue();
            } else {
                newSection = (String) item
                        .getItemProperty(PROPERTY_ID_CATEGORY).getValue();
            }

            table.setValue(null);
            final IndexedContainer c = (IndexedContainer) table
                    .getContainerDataSource();

            if (newSection != null && !newSection.equals(section)) {
                c.removeAllContainerFilters();
                c.addContainerFilter(PROPERTY_ID_CATEGORY, newSection, false,
                        true);
            }
            section = newSection;
            if (!tree.hasChildren(id)) {
                // Example, not section
                // update table selection
                table.setValue(id);
            }

        } else if (event.getProperty() == table) {
            if (table.getValue() != null) {
                table.removeListener(this);
                tree.setValue(table.getValue());
                table.addListener(this);
                final Item item = table.getItem(table.getValue());
                final Class<?> c = (Class<?>) item.getItemProperty(
                        PROPERTY_ID_CLASS).getValue();
                final Component component = getComponent(c);
                if (component != null) {
                    final String caption = (String) item.getItemProperty(
                            PROPERTY_ID_NAME).getValue();
                    ts.removeAllComponents();
                    ts.addTab(component, caption, null);
                }
                // update "viewed" state
                final Property p = item.getItemProperty(PROPERTY_ID_VIEWED);
                if (p.getValue() == null) {
                    p.setValue(new Embedded("", new ThemeResource(
                            "icons/ok.png")));
                }
                table.requestRepaint();
            }
        }

    }

    private Component getComponent(Class<?> componentClass) {
        if (!exampleInstances.containsKey(componentClass)) {
            try {
                final Component c = (Component) componentClass.newInstance();
                exampleInstances.put(componentClass, c);
            } catch (final Exception e) {
                return null;
            }
        }
        return exampleInstances.get(componentClass);
    }

}