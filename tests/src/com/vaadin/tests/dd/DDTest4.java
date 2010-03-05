package com.vaadin.tests.dd;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.demo.tutorial.addressbook.data.Person;
import com.vaadin.demo.tutorial.addressbook.data.PersonContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptCriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptCriteria.DragSourceIs;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.tests.components.TestBase;
import com.vaadin.tests.util.TestUtils;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.AbstractSelect.AbstractSelectDropTargetDetails;

public class DDTest4 extends TestBase {

    java.util.Random r = new java.util.Random(1);

    HorizontalLayout hl = new HorizontalLayout();
    Table table = new Table("Drag and drop sortable table");

    @Override
    protected void setup() {
        Window w = getLayout().getWindow();

        TestUtils
                .injectCSS(
                        w,
                        ".v-table-row-drag-middle .v-table-cell-content {"
                                + "        background-color: inherit ; border-bottom: 1px solid cyan;"
                                + "}");
        /* darn reindeer has no icons */

        // hl.addComponent(tree1);
        hl.addComponent(table);
        // hl.addComponent(tree2);
        hl.setWidth("100%");
        hl.setSpacing(true);
        hl.setExpandRatio(table, 1);
        table.setWidth("100%");
        table.setPageLength(10);
        table.setRowHeaderMode(Table.ROW_HEADER_MODE_ID);
        table.setSelectable(true);
        populateTable();
        addComponent(hl);

        /*
         * Make table rows draggable
         */
        table.setDragMode(Table.DragModes.ROWS);

        table.setDropHandler(new DropHandler() {
            // accept only drags from this table
            AcceptCriterion crit = new DragSourceIs(table);

            public AcceptCriterion getAcceptCriterion() {
                return crit;
            }

            public void drop(DragAndDropEvent dropEvent) {
                AbstractSelectDropTargetDetails dropTargetData = (AbstractSelectDropTargetDetails) dropEvent
                        .getDropTargetDetails();
                DataBoundTransferable transferable = (DataBoundTransferable) dropEvent
                        .getTransferable();
                Object itemIdOver = dropTargetData.getItemIdOver();
                Object itemId = transferable.getItemId();
                if (itemId == null || itemIdOver == null
                        || itemId.equals(itemIdOver)) {
                    return; // no move happened
                }

                // IndexedContainer goodies... (hint: don't use it in real apps)
                IndexedContainer containerDataSource = (IndexedContainer) table
                        .getContainerDataSource();
                IndexedContainer clone = null;
                try {
                    clone = (IndexedContainer) containerDataSource.clone();
                    int newIndex = containerDataSource.indexOfId(itemIdOver) - 1;
                    if (dropTargetData.getDropLocation() != VerticalDropLocation.TOP) {
                        newIndex++;
                    }
                    if (newIndex < 0) {
                        newIndex = 0;
                    }
                    containerDataSource.removeItem(itemId);
                    Item newItem = containerDataSource.addItemAt(newIndex,
                            itemId);

                    Item item = clone.getItem(itemId);
                    for (Object propId : item.getItemPropertyIds()) {
                        newItem.getItemProperty(propId).setValue(
                                item.getItemProperty(propId).getValue());
                    }
                } catch (CloneNotSupportedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

    }

    private void populateTable() {
        table.addContainerProperty("Name", String.class, "");
        table.addContainerProperty("Weight", Integer.class, 0);

        PersonContainer testData = PersonContainer.createWithTestData();

        for (int i = 0; i < 10; i++) {
            Item addItem = table.addItem("Item" + i);
            Person p = testData.getIdByIndex(i);
            addItem.getItemProperty("Name").setValue(
                    p.getFirstName() + " " + p.getLastName());
            addItem.getItemProperty("Weight").setValue(50 + r.nextInt(60));
        }

    }

    private final static ThemeResource FOLDER = new ThemeResource(
            "../runo/icons/16/folder.png");
    private final static ThemeResource DOC = new ThemeResource(
            "../runo/icons/16/document.png");

    @Override
    protected String getDescription() {
        return "dd";
    }

    @Override
    protected Integer getTicketNumber() {
        return 119;
    }

}