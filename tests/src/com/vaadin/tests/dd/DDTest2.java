package com.vaadin.tests.dd;

import java.util.Collection;

import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.demo.tutorial.addressbook.data.Person;
import com.vaadin.demo.tutorial.addressbook.data.PersonContainer;
import com.vaadin.event.AbstractDropHandler;
import com.vaadin.event.DataBindedTransferrable;
import com.vaadin.event.Transferable;
import com.vaadin.event.AbstractDropHandler.AcceptCriterion;
import com.vaadin.event.AbstractDropHandler.And;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.tests.components.TestBase;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Window;
import com.vaadin.ui.Tree.TreeDropDetails;

public class DDTest2 extends TestBase {

    java.util.Random r = new java.util.Random(1);

    HorizontalLayout hl = new HorizontalLayout();
    Tree tree1 = new Tree("Tree that accepts table rows to folders");
    Table table = new Table("Drag rows to Tree on left or right");
    Tree tree2 = new Tree("Accepts items, copies values");

    @Override
    protected void setup() {
        Window w = getLayout().getWindow();
        /* darn reindeer has no icons */
        w.setTheme("runo");

        hl.addComponent(tree1);
        hl.addComponent(table);
        hl.addComponent(tree2);
        hl.setWidth("100%");
        hl.setSpacing(true);
        hl.setExpandRatio(table, 1);
        popuplateTrees();
        table.setWidth("100%");
        table.setPageLength(10);
        populateTable();
        addComponent(hl);

        /*
         * Make table rows draggable
         */
        table.setDragMode(Table.DragModes.ROWS);

        AbstractDropHandler dropHandler = new AbstractDropHandler() {
            @Override
            public void receive(Transferable transferable, Object dropdetails) {
                /*
                 * We know transferrable is from table, so it is of type
                 * DataBindedTransferrable
                 */
                DataBindedTransferrable tr = (DataBindedTransferrable) transferable;
                Object itemId = tr.getItemId();
                Table fromTable = (Table) tr.getSourceComponent();
                String name = fromTable.getItem(itemId).getItemProperty("Name")
                        .toString();

                tree1.addItem(name);
                tree1.setChildrenAllowed(name, false);

                /*
                 * As we also accept only drops on folders, we know dropDetails
                 * is from Tree and it contains itemIdOver.
                 */
                TreeDropDetails details = (TreeDropDetails) dropdetails;
                Object idOver = details.getItemIdOver();
                tree1.setParent(name, idOver);

                /*
                 * Remove the item from table
                 */
                table.removeItem(itemId);

            }
        };
        AcceptCriterion onNode = new AbstractDropHandler.OverTreeNode();
        AcceptCriterion fromTree = new AbstractDropHandler.ComponentFilter(
                table);
        And and = new AbstractDropHandler.And(fromTree, onNode);
        dropHandler.setAcceptCriterion(and);
        tree1.setDropHandler(dropHandler);

        /*
         * First step done. tree1 now accepts drags only from table and only
         * over tree nodes aka "folders"
         */

        /*
         * Now set the rightmost tree accept any item drag. On drop, copy from
         * source. Also make drags from tree1 possible.
         */

        dropHandler = new AbstractDropHandler() {
            @Override
            public void receive(Transferable transferable, Object dropdetails) {
                TreeDropDetails details = (TreeDropDetails) dropdetails;

                if (transferable instanceof DataBindedTransferrable) {
                    DataBindedTransferrable tr = (DataBindedTransferrable) transferable;

                    Object itemId = tree2.addItem();
                    tree2.setParent(itemId, details.getItemIdOver());
                    if (tr.getSourceComponent() == tree1) {
                        // use item id from tree1 as caption
                        tree2.setItemCaption(itemId, (String) tr.getItemId());
                        // if comes from tree1, move subtree too
                        copySubTree(tr.getItemId(), itemId);
                    } else if (tr.getSourceComponent() == table) {
                        // comes from table, override caption with name
                        String name = (String) table.getItem(tr.getItemId())
                                .getItemProperty("Name").getValue();
                        tree2.setItemCaption(itemId, name);
                    } else if (tr.getSourceComponent() == tree2) {
                        tree2.setItemCaption(itemId, tree2.getItemCaption(tr
                                .getItemId()));
                    }
                }
            }

            private void copySubTree(Object itemId, Object itemIdTo) {
                Collection children = tree1.getChildren(itemId);
                if (children != null) {
                    for (Object childId : children) {
                        Object newItemId = tree2.addItem();
                        tree2.setItemCaption(newItemId, (String) childId);
                        tree2.setParent(newItemId, itemIdTo);
                        copySubTree(childId, newItemId);
                    }
                }
            }
        };
        dropHandler
                .setAcceptCriterion(AbstractDropHandler.CRITERION_HAS_ITEM_ID);

        tree2.setDropHandler(dropHandler);

        /*
         * Finally add two windows with DragDropPane. First accept anything,
         * second has server side accept rule to allow only drops from Tree1.
         * Check the code in implementing classes.
         */
        Window acceptAnyThing = new AcceptAnythingWindow();
        Window acceptFromTree1viaServerCheck = new AcceptFromComponent(tree1);

        w.addWindow(acceptAnyThing);
        acceptAnyThing.setPositionY(450);
        acceptAnyThing.setPositionX(0);
        w.addWindow(acceptFromTree1viaServerCheck);
        acceptFromTree1viaServerCheck.setPositionY(450);
        acceptFromTree1viaServerCheck.setPositionX(300);

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
            "icons/16/folder.png");
    private final static ThemeResource DOC = new ThemeResource(
            "icons/16/document.png");

    private void popuplateTrees() {
        HierarchicalContainer hc = new HierarchicalContainer();
        hc.addContainerProperty("icon", Resource.class, DOC);
        Item addItem = hc.addItem("Fats");
        addItem.getItemProperty("icon").setValue(FOLDER);
        hc.addItem("Tarja");
        hc.setParent("Tarja", "Fats");
        hc.setChildrenAllowed("Tarja", false);
        addItem = hc.addItem("Thins");
        addItem.getItemProperty("icon").setValue(FOLDER);
        addItem = hc.addItem("Anorectic");
        addItem.getItemProperty("icon").setValue(FOLDER);
        hc.setParent("Anorectic", "Thins");
        addItem = hc.addItem("Normal weighted");
        addItem.getItemProperty("icon").setValue(FOLDER);

        tree1.setContainerDataSource(hc);
        tree1.setItemIconPropertyId("icon");

        tree2.setContainerDataSource(new HierarchicalContainer());

        tree2.addItem("/");

    }

    @Override
    protected String getDescription() {
        return "dd";
    }

    @Override
    protected Integer getTicketNumber() {
        return 119;
    }

}
