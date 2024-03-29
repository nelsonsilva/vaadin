package com.vaadin.tests.server.component.abstractselect;

import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.Container.ItemSetChangeListener;
import com.vaadin.data.Container.PropertySetChangeEvent;
import com.vaadin.data.Container.PropertySetChangeListener;
import com.vaadin.tests.server.component.AbstractListenerMethodsTest;
import com.vaadin.ui.ComboBox;

public class TestAbstractSelectListeners extends AbstractListenerMethodsTest {
    public void testItemSetChangeListenerAddGetRemove() throws Exception {
        testListenerAddGetRemove(ComboBox.class, ItemSetChangeEvent.class,
                ItemSetChangeListener.class);
    }

    public void testPropertySetChangeListenerAddGetRemove() throws Exception {
        testListenerAddGetRemove(ComboBox.class, PropertySetChangeEvent.class,
                PropertySetChangeListener.class);
    }
}
