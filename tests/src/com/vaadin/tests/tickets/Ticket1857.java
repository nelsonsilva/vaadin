package com.vaadin.tests.tickets;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.event.Action;
import com.vaadin.event.Action.Handler;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class Ticket1857 extends Application implements Handler {

    @Override
    public void init() {

        setTheme("tests-tickets");

        VerticalLayout el = new VerticalLayout();
        Window main = new Window("Testcase for #1857", el);
        setMainWindow(main);
        el.setMargin(true);
        el.setSpacing(true);

        final Table t = new Table();
        el.addComponent(t);
        el.setExpandRatio(t, 1);
        t.setSizeFull();
        addContentsToTable(t);
        t.setStyleName("foo");

        HorizontalLayout footer = new HorizontalLayout();
        el.addComponent(footer);
        footer.setSpacing(true);

        final CheckBox actionHandlerEnabler = new CheckBox("Action handlers",
                false);
        footer.addComponent(actionHandlerEnabler);
        actionHandlerEnabler.setImmediate(true);
        actionHandlerEnabler.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                if (((Boolean) actionHandlerEnabler.getValue()).booleanValue()) {
                    t.addActionHandler(Ticket1857.this);
                } else {
                    t.removeActionHandler(Ticket1857.this);
                }
            }
        });

        final CheckBox cellStylesEnabler = new CheckBox("Cell styles", false);
        footer.addComponent(cellStylesEnabler);
        cellStylesEnabler.setImmediate(true);
        cellStylesEnabler.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                if (((Boolean) cellStylesEnabler.getValue()).booleanValue()) {
                    t.setCellStyleGenerator(new Table.CellStyleGenerator() {
                        public String getStyle(Object itemId, Object propertyId) {
                            Object cell = t.getContainerProperty(itemId,
                                    propertyId).getValue();
                            if (!(cell instanceof Integer)) {
                                return null;
                            }
                            int age = ((Integer) cell).intValue();
                            return age > 65 ? "old" : (age < 18 ? "young"
                                    : null);
                        }
                    });
                } else {
                    t.setCellStyleGenerator(null);
                }
            }
        });
        cellStylesEnabler.setValue(Boolean.TRUE);

    }

    private void addContentsToTable(Table t) {

        t.addContainerProperty("First name", String.class, "");
        t.addContainerProperty("Last name", String.class, "");
        t.addContainerProperty("Age", Integer.class, "");

        String firstNames[] = { "Quentin", "Marc", "Peter", "David", "Mary",
                "Jani", "Jane", "Brita" };
        String lastNames[] = { "Heiskanen", "Bjorn", "Torwalds", "Autere",
                "Smith", "Lindström" };

        for (int i = 0; i < 1000; i++) {
            t.addItem(new Object[] {
                    firstNames[((int) (Math.random() * firstNames.length))],
                    lastNames[((int) (Math.random() * lastNames.length))],
                    new Integer((int) (Math.random() * 100) + 10) },
                    new Integer(i));
        }
    }

    private final Action removeAction = new Action("Remove");

    public Action[] getActions(Object target, Object sender) {
        return new Action[] { removeAction };
    }

    public void handleAction(Action action, Object sender, Object target) {
        getMainWindow().showNotification("Removing row number:" + target);
        ((Table) sender).removeItem(target);
    }
}