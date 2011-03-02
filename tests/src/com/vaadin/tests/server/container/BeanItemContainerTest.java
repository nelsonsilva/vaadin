package com.vaadin.tests.server.container;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;

/**
 * Test basic functionality of BeanItemContainer.
 * 
 * Most sorting related tests are in {@link BeanItemContainerSortTest}.
 */
public class BeanItemContainerTest extends AbstractBeanContainerTest {

    // basics from the common container test

    private Map<String, ClassName> nameToBean = new LinkedHashMap<String, ClassName>();

    private BeanItemContainer<ClassName> getContainer() {
        return new BeanItemContainer<ClassName>(ClassName.class);
    }

    @Override
    public void setUp() {
        nameToBean.clear();

        for (int i = 0; i < sampleData.length; i++) {
            ClassName className = new ClassName(sampleData[i], i);
            nameToBean.put(sampleData[i], className);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initializeContainer(Container container) {
        BeanItemContainer<ClassName> beanItemContainer = (BeanItemContainer<ClassName>) container;

        beanItemContainer.removeAllItems();

        Iterator<ClassName> it = nameToBean.values().iterator();
        while (it.hasNext()) {
            beanItemContainer.addBean(it.next());
        }
    }

    @Override
    protected void validateContainer(Container container,
            Object expectedFirstItemId, Object expectedLastItemId,
            Object itemIdInSet, Object itemIdNotInSet,
            boolean checkGetItemNull, int expectedSize) {
        Object notInSet = nameToBean.get(itemIdNotInSet);
        if (notInSet == null && itemIdNotInSet != null) {
            notInSet = new ClassName(String.valueOf(itemIdNotInSet), 9999);
        }
        super.validateContainer(container, nameToBean.get(expectedFirstItemId),
                nameToBean.get(expectedLastItemId),
                nameToBean.get(itemIdInSet), notInSet, checkGetItemNull,
                expectedSize);
    }

    @Override
    protected boolean isFilteredOutItemNull() {
        return false;
    }

    public void testBasicOperations() {
        testBasicContainerOperations(getContainer());
    }

    public void testFiltering() {
        testContainerFiltering(getContainer());
    }

    public void testSorting() {
        testContainerSorting(getContainer());
    }

    public void testSortingAndFiltering() {
        testContainerSortingAndFiltering(getContainer());
    }

    // duplicated from parent class and modified - adding items to
    // BeanItemContainer differs from other containers
    public void testContainerOrdered() {
        BeanItemContainer<String> container = new BeanItemContainer<String>(
                String.class);

        String id = "test1";

        Item item = container.addBean(id);
        assertNotNull(item);

        assertEquals(id, container.firstItemId());
        assertEquals(id, container.lastItemId());

        // isFirstId
        assertTrue(container.isFirstId(id));
        assertTrue(container.isFirstId(container.firstItemId()));
        // isLastId
        assertTrue(container.isLastId(id));
        assertTrue(container.isLastId(container.lastItemId()));

        // Add a new item before the first
        // addItemAfter
        String newFirstId = "newFirst";
        item = container.addItemAfter(null, newFirstId);
        assertNotNull(item);
        assertNotNull(container.getItem(newFirstId));

        // isFirstId
        assertTrue(container.isFirstId(newFirstId));
        assertTrue(container.isFirstId(container.firstItemId()));
        // isLastId
        assertTrue(container.isLastId(id));
        assertTrue(container.isLastId(container.lastItemId()));

        // nextItemId
        assertEquals(id, container.nextItemId(newFirstId));
        assertNull(container.nextItemId(id));
        assertNull(container.nextItemId("not-in-container"));

        // prevItemId
        assertEquals(newFirstId, container.prevItemId(id));
        assertNull(container.prevItemId(newFirstId));
        assertNull(container.prevItemId("not-in-container"));

        // addItemAfter(Object)
        String newSecondItemId = "newSecond";
        item = container.addItemAfter(newFirstId, newSecondItemId);
        // order is now: newFirstId, newSecondItemId, id
        assertNotNull(item);
        assertNotNull(container.getItem(newSecondItemId));
        assertEquals(id, container.nextItemId(newSecondItemId));
        assertEquals(newFirstId, container.prevItemId(newSecondItemId));

        // addItemAfter(Object,Object)
        String fourthId = "id of the fourth item";
        Item fourth = container.addItemAfter(newFirstId, fourthId);
        // order is now: newFirstId, fourthId, newSecondItemId, id
        assertNotNull(fourth);
        assertEquals(fourth, container.getItem(fourthId));
        assertEquals(newSecondItemId, container.nextItemId(fourthId));
        assertEquals(newFirstId, container.prevItemId(fourthId));

        // addItemAfter(Object,Object)
        Object fifthId = "fifth";
        Item fifth = container.addItemAfter(null, fifthId);
        // order is now: fifthId, newFirstId, fourthId, newSecondItemId, id
        assertNotNull(fifth);
        assertEquals(fifth, container.getItem(fifthId));
        assertEquals(newFirstId, container.nextItemId(fifthId));
        assertNull(container.prevItemId(fifthId));

    }

    public void testContainerIndexed() {
        testContainerIndexed(getContainer(), nameToBean.get(sampleData[2]), 2,
                false, new ClassName("org.vaadin.test.Test", 8888), true);
    }

    @SuppressWarnings("deprecation")
    public void testCollectionConstructors() {
        List<ClassName> classNames = new ArrayList<ClassName>();
        classNames.add(new ClassName("a.b.c.Def", 1));
        classNames.add(new ClassName("a.b.c.Fed", 2));
        classNames.add(new ClassName("b.c.d.Def", 3));

        // note that this constructor is problematic, users should use the
        // version that
        // takes the bean class as a parameter
        BeanItemContainer<ClassName> container = new BeanItemContainer<ClassName>(
                classNames);

        Assert.assertEquals(3, container.size());
        Assert.assertEquals(classNames.get(0), container.firstItemId());
        Assert.assertEquals(classNames.get(1), container.getIdByIndex(1));
        Assert.assertEquals(classNames.get(2), container.lastItemId());

        BeanItemContainer<ClassName> container2 = new BeanItemContainer<ClassName>(
                ClassName.class, classNames);

        Assert.assertEquals(3, container2.size());
        Assert.assertEquals(classNames.get(0), container2.firstItemId());
        Assert.assertEquals(classNames.get(1), container2.getIdByIndex(1));
        Assert.assertEquals(classNames.get(2), container2.lastItemId());
    }

    // this only applies to the collection constructor with no type parameter
    @SuppressWarnings("deprecation")
    public void testEmptyCollectionConstructor() {
        try {
            new BeanItemContainer<ClassName>((Collection<ClassName>) null);
            Assert.fail("Initializing BeanItemContainer from a null collection should not work!");
        } catch (IllegalArgumentException e) {
            // success
        }
        try {
            new BeanItemContainer<ClassName>(new ArrayList<ClassName>());
            Assert.fail("Initializing BeanItemContainer from an empty collection should not work!");
        } catch (IllegalArgumentException e) {
            // success
        }
    }

    protected abstract class ItemSetChangeListenerTester extends
            BaseItemSetChangeListenerTester<BeanItemContainer<ClassName>> {
        @Override
        protected BeanItemContainer<ClassName> prepareContainer() {
            BeanItemContainer<ClassName> container = getContainer();
            initializeContainer(container);
            return container;
        }
    }

    public void testItemSetChangeListeners() {
        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addBean(new ClassName("com.example.Test", 1111));
            }
        }.listenerTest(1, false);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addItem(new ClassName("com.example.Test", 1111));
            }
        }.listenerTest();

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addItem(new ClassName("com.example.Test", 1111));
                container.addItem(new ClassName("com.example.Test2", 1112));
            }
        }.listenerTest(2, false);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                ClassName cn = new ClassName("com.example.Test", 1111);
                container.addItem(cn);
                // second add is a NOP
                container.addItem(cn);
            }
        }.listenerTest(1, false);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addItemAfter(null, new ClassName("com.example.Test",
                        1111));
                Assert.assertEquals(
                        "com.example.Test",
                        container.getContainerProperty(container.firstItemId(),
                                FULLY_QUALIFIED_NAME).getValue());
            }
        }.listenerTest();

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addItemAfter(container.firstItemId(), new ClassName(
                        "com.example.Test", 1111));
                Assert.assertEquals(
                        "com.example.Test",
                        container
                                .getContainerProperty(
                                        container.getIdByIndex(1),
                                        FULLY_QUALIFIED_NAME).getValue());
            }
        }.listenerTest();

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addItemAfter(container.lastItemId(), new ClassName(
                        "com.example.Test", 1111));
                Assert.assertEquals(
                        "com.example.Test",
                        container.getContainerProperty(container.lastItemId(),
                                FULLY_QUALIFIED_NAME).getValue());
            }
        }.listenerTest();

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addItemAt(0, new ClassName("com.example.Test", 1111));
                Assert.assertEquals(
                        "com.example.Test",
                        container.getContainerProperty(container.firstItemId(),
                                FULLY_QUALIFIED_NAME).getValue());
            }
        }.listenerTest();

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addItemAt(1, new ClassName("com.example.Test", 1111));
                Assert.assertEquals(
                        "com.example.Test",
                        container
                                .getContainerProperty(
                                        container.getIdByIndex(1),
                                        FULLY_QUALIFIED_NAME).getValue());
            }
        }.listenerTest();

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addItemAt(container.size(), new ClassName(
                        "com.example.Test", 1111));
                Assert.assertEquals(
                        "com.example.Test",
                        container.getContainerProperty(container.lastItemId(),
                                FULLY_QUALIFIED_NAME).getValue());
            }
        }.listenerTest();

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addBean(new ClassName("com.example.Test", 1111));
            }
        }.listenerTest();

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.removeItem(nameToBean.get(sampleData[0]));
            }
        }.listenerTest();

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.removeItem(new ClassName("com.example.Test", 1111));
            }
        }.listenerTest(0, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                // this test does not check that there would be no second
                // notification because the collection is already empty
                container.removeAllItems();
            }
        }.listenerTest();

        new ItemSetChangeListenerTester() {
            private int propertyIndex = 0;

            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                Collection<String> containerPropertyIds = container
                        .getContainerPropertyIds();
                container.addContainerFilter(new ArrayList<String>(
                        containerPropertyIds).get(propertyIndex++), "a", true,
                        false);
            }
        }.listenerTest();

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.removeContainerFilters(SIMPLE_NAME);
            }

            @Override
            protected BeanItemContainer<ClassName> prepareContainer() {
                BeanItemContainer<ClassName> container = super
                        .prepareContainer();
                container.addContainerFilter(SIMPLE_NAME, "a", true, false);
                return container;
            };
        }.listenerTest();

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.removeAllContainerFilters();
            }

            @Override
            protected BeanItemContainer<ClassName> prepareContainer() {
                BeanItemContainer<ClassName> container = super
                        .prepareContainer();
                container.addContainerFilter(SIMPLE_NAME, "a", true, false);
                return container;
            };
        }.listenerTest();
    }

    public void testItemSetChangeListenersWhileFiltering() {
        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                container.addBean(new ClassName("com.example.Test", 1111));
            }
        }.listenerTest(2, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                container.addItem(new ClassName("com.example.Test", 1111));
            }
        }.listenerTest(2, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                container.addItem(new ClassName("com.example.Test", 1111));
                // this does not cause a notification - filtered out
                container.addItem(new ClassName("com.example.Other", 2222));
            }
        }.listenerTest(2, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                container.addItem(new ClassName("com.example.Test", 1111));
                container.addItem(new ClassName("com.example.Test2", 1112));
            }
        }.listenerTest(3, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                ClassName cn = new ClassName("com.example.Test", 1111);
                container.addItem(cn);
                // second add is a NOP
                container.addItem(cn);
            }
        }.listenerTest(2, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                container.addItemAfter(null, new ClassName("com.example.Test",
                        1111));
                Assert.assertEquals(
                        "com.example.Test",
                        container.getContainerProperty(container.firstItemId(),
                                FULLY_QUALIFIED_NAME).getValue());
            }
        }.listenerTest(2, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                container.addItemAfter(container.firstItemId(), new ClassName(
                        "com.example.Test", 1111));
                Assert.assertEquals(
                        "com.example.Test",
                        container
                                .getContainerProperty(
                                        container.getIdByIndex(1),
                                        FULLY_QUALIFIED_NAME).getValue());
            }
        }.listenerTest(2, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                container.addItemAfter(container.lastItemId(), new ClassName(
                        "com.example.Test", 1111));
                Assert.assertEquals(
                        "com.example.Test",
                        container.getContainerProperty(container.lastItemId(),
                                FULLY_QUALIFIED_NAME).getValue());
            }
        }.listenerTest(2, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                container.addItemAt(0, new ClassName("com.example.Test", 1111));
                Assert.assertEquals(
                        "com.example.Test",
                        container.getContainerProperty(container.firstItemId(),
                                FULLY_QUALIFIED_NAME).getValue());
            }
        }.listenerTest(2, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                container.addItemAt(1, new ClassName("com.example.Test", 1111));
                Assert.assertEquals(
                        "com.example.Test",
                        container
                                .getContainerProperty(
                                        container.getIdByIndex(1),
                                        FULLY_QUALIFIED_NAME).getValue());
            }
        }.listenerTest(2, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                container.addItemAt(container.size(), new ClassName(
                        "com.example.Test", 1111));
                Assert.assertEquals(
                        "com.example.Test",
                        container.getContainerProperty(container.lastItemId(),
                                FULLY_QUALIFIED_NAME).getValue());
            }
        }.listenerTest(2, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                container.addBean(new ClassName("com.example.Test", 1111));
            }
        }.listenerTest(2, true);

        // new ItemSetChangeListenerTester() {
        // @Override
        // protected void performModification(
        // BeanItemContainer<ClassName> container) {
        // container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
        // true, false);
        // // TODO why notified?
        // container.removeItem(nameToBean.get(sampleData[0]));
        // }
        // }.listenerTest(1, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                container.removeItem(new ClassName("com.example.Test", 1111));
            }
        }.listenerTest(1, true);

        new ItemSetChangeListenerTester() {
            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                // this can also cause a notification (optional, no visible
                // change)
                container.removeAllItems();
            }
        }.listenerTest(2, true);

        new ItemSetChangeListenerTester() {
            private int propertyIndex = 0;

            @Override
            protected void performModification(
                    BeanItemContainer<ClassName> container) {
                container.addContainerFilter(FULLY_QUALIFIED_NAME, "Test",
                        true, false);
                Collection<String> containerPropertyIds = container
                        .getContainerPropertyIds();
                // this causes a notification every time, could also skip here
                container.addContainerFilter(new ArrayList<String>(
                        containerPropertyIds).get(propertyIndex++), "a", true,
                        false);
            }
        }.listenerTest(2, true);

    }

    public void testAddRemoveWhileFiltering() {
        BeanItemContainer<Person> container = new BeanItemContainer<Person>(
                Person.class);

        Person john = new Person("John");
        Person jane = new Person("Jane");
        Person matthew = new Person("Matthew");

        Person jack = new Person("Jack");
        Person michael = new Person("Michael");
        Person william = new Person("William");
        Person julia = new Person("Julia");
        Person george = new Person("George");
        Person mark = new Person("Mark");

        container.addBean(john);
        container.addBean(jane);
        container.addBean(matthew);

        assertEquals(3, container.size());
        // john, jane, matthew

        container.addContainerFilter("name", "j", true, true);

        assertEquals(2, container.size());
        // john, jane, (matthew)

        // add a bean that passes the filter
        container.addBean(jack);
        assertEquals(3, container.size());
        assertEquals(jack, container.lastItemId());
        // john, jane, (matthew), jack

        // add beans that do not pass the filter
        container.addBean(michael);
        // john, jane, (matthew), jack, (michael)
        container.addItemAfter(null, william);
        // (william), john, jane, (matthew), jack, (michael)

        // add after an item that is shown
        container.addItemAfter(john, george);
        // (william), john, (george), jane, (matthew), jack, (michael)
        assertEquals(3, container.size());
        assertEquals(john, container.firstItemId());

        // add after an item that is not shown does nothing
        container.addItemAfter(william, julia);
        // (william), john, (george), jane, (matthew), jack, (michael)
        assertEquals(3, container.size());
        assertEquals(john, container.firstItemId());

        container.addItemAt(1, julia);
        // (william), john, julia, (george), jane, (matthew), jack, (michael)

        container.addItemAt(2, mark);
        // (william), john, julia, (mark), (george), jane, (matthew), jack,
        // (michael)

        container.removeItem(matthew);
        // (william), john, julia, (mark), (george), jane, jack, (michael)

        assertEquals(4, container.size());
        assertEquals(jack, container.lastItemId());

        container.removeContainerFilters("name");

        assertEquals(8, container.size());
        assertEquals(william, container.firstItemId());
        assertEquals(john, container.nextItemId(william));
        assertEquals(julia, container.nextItemId(john));
        assertEquals(mark, container.nextItemId(julia));
        assertEquals(george, container.nextItemId(mark));
        assertEquals(jane, container.nextItemId(george));
        assertEquals(jack, container.nextItemId(jane));
        assertEquals(michael, container.lastItemId());
    }

    public void testRefilterOnPropertyModification() {
        BeanItemContainer<Person> container = new BeanItemContainer<Person>(
                Person.class);

        Person john = new Person("John");
        Person jane = new Person("Jane");
        Person matthew = new Person("Matthew");

        container.addBean(john);
        container.addBean(jane);
        container.addBean(matthew);

        assertEquals(3, container.size());
        // john, jane, matthew

        container.addContainerFilter("name", "j", true, true);

        assertEquals(2, container.size());
        // john, jane, (matthew)

        // #6053 currently, modification of an item that is not visible does not
        // trigger refiltering - should it?
        // matthew.setName("Julia");
        // assertEquals(3, container.size());
        // john, jane, julia

        john.setName("Mark");
        assertEquals(2, container.size());
        // (mark), jane, julia

        container.removeAllContainerFilters();

        assertEquals(3, container.size());
    }

    public void testAddAll() {
        BeanItemContainer<Person> container = new BeanItemContainer<Person>(
                Person.class);

        Person john = new Person("John");
        Person jane = new Person("Jane");
        Person matthew = new Person("Matthew");

        container.addBean(john);
        container.addBean(jane);
        container.addBean(matthew);

        assertEquals(3, container.size());
        // john, jane, matthew

        Person jack = new Person("Jack");
        Person michael = new Person("Michael");

        // addAll
        container.addAll(Arrays.asList(jack, michael));
        // john, jane, matthew, jack, michael

        assertEquals(5, container.size());
        assertEquals(jane, container.nextItemId(john));
        assertEquals(matthew, container.nextItemId(jane));
        assertEquals(jack, container.nextItemId(matthew));
        assertEquals(michael, container.nextItemId(jack));
    }

    public void testUnsupportedMethods() {
        BeanItemContainer<Person> container = new BeanItemContainer<Person>(
                Person.class);
        container.addBean(new Person("John"));

        try {
            container.addItem();
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // should get exception
        }

        try {
            container.addItemAfter(new Person("Jane"));
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // should get exception
        }

        try {
            container.addItemAt(0);
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // should get exception
        }

        try {
            container.addContainerProperty("lastName", String.class, "");
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // should get exception
        }

        try {
            container.removeContainerProperty("name");
            Assert.fail();
        } catch (UnsupportedOperationException e) {
            // should get exception
        }

        assertEquals(1, container.size());
    }

    public void testAddNullBean() {
        BeanItemContainer<Person> container = new BeanItemContainer<Person>(
                Person.class);
        Person john = new Person("John");
        container.addBean(john);

        assertNull(container.addItem(null));
        assertNull(container.addItemAfter(null, null));
        assertNull(container.addItemAfter(john, null));
        assertNull(container.addItemAt(0, null));

        assertEquals(1, container.size());
    }

    public void testBeanIdResolver() {
        BeanItemContainer<Person> container = new BeanItemContainer<Person>(
                Person.class);
        Person john = new Person("John");

        assertSame(john, container.getBeanIdResolver().getIdForBean(john));
    }

    public void testNullBeanClass() {
        try {
            new BeanItemContainer<Object>((Class<Object>) null);
        } catch (IllegalArgumentException e) {
            // should get exception
        }
    }

}
