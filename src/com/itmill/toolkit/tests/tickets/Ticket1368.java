package com.itmill.toolkit.tests.tickets;

import com.itmill.toolkit.Application;
import com.itmill.toolkit.tests.TestForTablesInitialColumnWidthLogicRendering;
import com.itmill.toolkit.ui.ComboBox;
import com.itmill.toolkit.ui.Table;
import com.itmill.toolkit.ui.Window;

/**
 */
public class Ticket1368 extends Application {

    private Table t;

    public void init() {

        final Window mainWin = new Window("Test app to #1368");
        setMainWindow(mainWin);

        t = TestForTablesInitialColumnWidthLogicRendering.getTestTable(3, 5);

        mainWin.addComponent(t);

        ComboBox addColumn = new ComboBox();
        addColumn.setImmediate(true);
        addColumn.setNewItemsAllowed(true);
        addColumn.setNewItemHandler(new ComboBox.NewItemHandler() {
            public void addNewItem(String newItemCaption) {
                t.addContainerProperty(newItemCaption, String.class, "-");
            }
        });
        mainWin.addComponent(addColumn);

    }
}