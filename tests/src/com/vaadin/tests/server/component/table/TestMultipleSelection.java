package com.vaadin.tests.server.component.table;

import java.util.Set;

import junit.framework.TestCase;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.MultiSelectMode;

public class TestMultipleSelection extends TestCase {

    /**
     * Tests weather the multiple select mode is set when using Table.set
     */
    @SuppressWarnings("unchecked")
    public void testSetMultipleItems() {
        Table table = new Table("", createTestContainer());

        // Tests if multiple selection is set
        table.setMultiSelect(true);
        assertTrue(table.isMultiSelect());

        // Test multiselect by setting several items at once
        table.setValue(Lists.asList("1", new String[] { "3" }));
        assertEquals(2, ((Set<String>) table.getValue()).size());
    }

    /**
     * Tests setting the multiselect mode of the Table. The multiselect mode
     * affects how mouse selection is made in the table by the user.
     */
    public void testSetMultiSelectMode() {
        Table table = new Table("", createTestContainer());

        // Default multiselect mode should be MultiSelectMode.DEFAULT
        assertEquals(MultiSelectMode.DEFAULT, table.getMultiSelectMode());

        // Tests if multiselectmode is set
        table.setMultiSelectMode(MultiSelectMode.SIMPLE);
        assertEquals(MultiSelectMode.SIMPLE, table.getMultiSelectMode());
    }

    /**
     * Creates a testing container for the tests
     * 
     * @return A new container with test items
     */
    private Container createTestContainer() {
        IndexedContainer container = new IndexedContainer(Lists.asList("1",
                new String[] { "2", "3", "4" }));
        return container;
    }
}
