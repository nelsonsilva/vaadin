/* 
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.tests;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class TestForContainerFilterable extends CustomComponent {

    VerticalLayout lo = new VerticalLayout();
    IndexedContainer ic = new IndexedContainer();
    Table t = new Table();
    private static String parts[] = { "Neo", "Sa", "rem", "the", "adi", "za",
            "tre", "day", "Ca", "re", "cen", "ter", "mi", "nal" };
    TextField fooFilter = new TextField("foo-filter");
    TextField barFilter = new TextField("bar-filter");
    Button filterButton = new Button("Filter");
    Label count = new Label();

    public TestForContainerFilterable() {
        setCompositionRoot(lo);

        // Init datasource
        ic.addContainerProperty("foo", String.class, "");
        ic.addContainerProperty("bar", String.class, "");
        for (int i = 0; i < 1000; i++) {
            final Object id = ic.addItem();
            ic.getContainerProperty(id, "foo").setValue(randomWord());
            ic.getContainerProperty(id, "bar").setValue(randomWord());
        }

        // Init filtering view
        final Panel filterPanel = new Panel("Filter", new HorizontalLayout());
        filterPanel.setWidth(100, Panel.UNITS_PERCENTAGE);
        lo.addComponent(filterPanel);
        filterPanel.addComponent(fooFilter);
        filterPanel.addComponent(barFilter);
        filterPanel.addComponent(filterButton);
        fooFilter
                .setDescription("Filters foo column in case-sensitive contains manner.");
        barFilter
                .setDescription("Filters bar column in case-insensitive prefix manner.");
        filterPanel.addComponent(count);

        // Table
        lo.addComponent(t);
        t.setPageLength(12);
        t.setWidth(100, Table.UNITS_PERCENTAGE);
        t.setContainerDataSource(ic);

        // Handler
        filterButton.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                ic.removeAllContainerFilters();
                if (fooFilter.getStringValue().length() > 0) {
                    ic.addContainerFilter("foo", fooFilter.getStringValue(), false,
                            false);
                }
                if (barFilter.getStringValue().length() > 0) {
                    ic.addContainerFilter("bar", barFilter.getStringValue(), true,
                            true);
                }
                count.setValue("Rows in table: " + ic.size());
            }
        });

        // Resetbutton
        lo.addComponent(new Button("Rebind table datasource",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        t.setContainerDataSource(ic);
                    }
                }));
    }

    private String randomWord() {
        int len = (int) (Math.random() * 4);
        final StringBuffer buf = new StringBuffer();
        while (len-- >= 0) {
            buf.append(parts[(int) (Math.random() * parts.length)]);
        }
        return buf.toString();
    }
}
