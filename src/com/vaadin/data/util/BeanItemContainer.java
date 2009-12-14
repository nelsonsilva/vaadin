package com.vaadin.data.util;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Container.Indexed;
import com.vaadin.data.Container.ItemSetChangeNotifier;
import com.vaadin.data.Container.Sortable;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Property.ValueChangeNotifier;

/**
 * An {@link ArrayList} backed container for {@link BeanItem}s.
 * <p>
 * Bean objects act as identifiers. For this reason, they should implement
 * Object.equals(Object) and Object.hashCode().
 * </p>
 * 
 * @param <BT>
 * 
 * @since 5.4
 */
@SuppressWarnings("serial")
public class BeanItemContainer<BT> implements Indexed, Sortable, Filterable,
        ItemSetChangeNotifier, ValueChangeListener {
    // filtered and unfiltered item IDs
    private ArrayList<BT> list = new ArrayList<BT>();
    private ArrayList<BT> allItems = new ArrayList<BT>();
    private final Map<BT, BeanItem<BT>> beanToItem = new HashMap<BT, BeanItem<BT>>();

    // internal data model to obtain property IDs etc.
    private final Class<? extends BT> type;
    private transient LinkedHashMap<String, PropertyDescriptor> model;

    private List<ItemSetChangeListener> itemSetChangeListeners;

    private Set<Filter> filters = new HashSet<Filter>();

    /**
     * The item sorter which is used for sorting the container.
     */
    private ItemSorter itemSorter = new DefaultItemSorter();

    /* Special serialization to handle method references */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        model = BeanItem.getPropertyDescriptors(type);
    }

    /**
     * Constructs BeanItemContainer for beans of a given type.
     * 
     * @param type
     *            the class of beans to be used with this containers.
     * @throws IllegalArgumentException
     *             If the type is null
     */
    public BeanItemContainer(Class<? extends BT> type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "The type passed to BeanItemContainer must not be null");
        }
        this.type = type;
        model = BeanItem.getPropertyDescriptors(type);
    }

    /**
     * Constructs BeanItemContainer with given collection of beans in it. The
     * collection must not be empty or an IllegalArgument is thrown.
     * 
     * @param collection
     *            non empty {@link Collection} of beans.
     * @throws IllegalArgumentException
     *             If the collection is null or empty.
     */
    public BeanItemContainer(Collection<BT> collection)
            throws IllegalArgumentException {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(
                    "The collection passed to BeanItemContainer must not be null or empty");
        }

        type = (Class<? extends BT>) collection.iterator().next().getClass();
        model = BeanItem.getPropertyDescriptors(type);
        int i = 0;
        for (BT bt : collection) {
            addItemAt(i++, bt);
        }
    }

    /**
     * Unsupported operation.
     * 
     * @see com.vaadin.data.Container.Indexed#addItemAt(int)
     */
    public Object addItemAt(int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds new item at given index.
     * 
     * The bean is used both as the item contents and as the item identifier.
     * 
     * @see com.vaadin.data.Container.Indexed#addItemAt(int, Object)
     */
    public BeanItem<BT> addItemAt(int index, Object newItemId)
            throws UnsupportedOperationException {
        if (index < 0 || index > size()) {
            return null;
        } else if (index == 0) {
            // add before any item, visible or not
            return addItemAtInternalIndex(0, newItemId);
        } else {
            // if index==size(), adds immediately after last visible item
            return addItemAfter(getIdByIndex(index - 1), newItemId);
        }
    }

    /**
     * Adds new item at given index of the internal (unfiltered) list.
     * <p>
     * The item is also added in the visible part of the list if it passes the
     * filters.
     * </p>
     * 
     * @param index
     *            Internal index to add the new item.
     * @param newItemId
     *            Id of the new item to be added.
     * @return Returns new item or null if the operation fails.
     */
    private BeanItem<BT> addItemAtInternalIndex(int index, Object newItemId) {
        // Make sure that the Item has not been created yet
        if (allItems.contains(newItemId)) {
            return null;
        }
        if (type.isAssignableFrom(newItemId.getClass())) {
            BT pojo = (BT) newItemId;
            // "list" will be updated in filterAll()
            allItems.add(index, pojo);
            BeanItem<BT> beanItem = new BeanItem<BT>(pojo, model);
            beanToItem.put(pojo, beanItem);
            // add listeners to be able to update filtering on property changes
            for (Filter filter : filters) {
                // addValueChangeListener avoids adding duplicates
                addValueChangeListener(beanItem, filter.propertyId);
            }

            // it is somewhat suboptimal to filter all items
            filterAll();
            return beanItem;
        } else {
            return null;
        }
    }

    public BT getIdByIndex(int index) {
        return list.get(index);
    }

    public int indexOfId(Object itemId) {
        return list.indexOf(itemId);
    }

    /**
     * Unsupported operation.
     * 
     * @see com.vaadin.data.Container.Ordered#addItemAfter(Object)
     */
    public Object addItemAfter(Object previousItemId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds new item after the given item.
     * 
     * The bean is used both as the item contents and as the item identifier.
     * 
     * @see com.vaadin.data.Container.Ordered#addItemAfter(Object, Object)
     */
    public BeanItem<BT> addItemAfter(Object previousItemId, Object newItemId)
            throws UnsupportedOperationException {
        // only add if the previous item is visible
        if (containsId(previousItemId)) {
            return addItemAtInternalIndex(allItems.indexOf(previousItemId) + 1,
                    newItemId);
        } else {
            return null;
        }
    }

    public BT firstItemId() {
        if (size() > 0) {
            return getIdByIndex(0);
        } else {
            return null;
        }
    }

    public boolean isFirstId(Object itemId) {
        return firstItemId() == itemId;
    }

    public boolean isLastId(Object itemId) {
        return lastItemId() == itemId;
    }

    public BT lastItemId() {
        if (size() > 0) {
            return getIdByIndex(size() - 1);
        } else {
            return null;
        }
    }

    public BT nextItemId(Object itemId) {
        int index = indexOfId(itemId);
        if (index >= 0 && index < size() - 1) {
            return getIdByIndex(index + 1);
        } else {
            // out of bounds
            return null;
        }
    }

    public BT prevItemId(Object itemId) {
        int index = indexOfId(itemId);
        if (index > 0) {
            return getIdByIndex(index - 1);
        } else {
            // out of bounds
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean addContainerProperty(Object propertyId, Class type,
            Object defaultValue) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported operation.
     * 
     * @see com.vaadin.data.Container#addItem()
     */
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new Item with the bean into the Container.
     * 
     * The bean is used both as the item contents and as the item identifier.
     * 
     * @see com.vaadin.data.Container#addItem(Object)
     */
    public BeanItem<BT> addBean(BT bean) {
        return addItem(bean);
    }

    /**
     * Creates a new Item with the bean into the Container.
     * 
     * The bean is used both as the item contents and as the item identifier.
     * 
     * @see com.vaadin.data.Container#addItem(Object)
     */
    public BeanItem<BT> addItem(Object itemId)
            throws UnsupportedOperationException {
        if (size() > 0) {
            // add immediately after last visible item
            int lastIndex = allItems.indexOf(lastItemId());
            return addItemAtInternalIndex(lastIndex + 1, itemId);
        } else {
            return addItemAtInternalIndex(0, itemId);
        }
    }

    public boolean containsId(Object itemId) {
        // only look at visible items after filtering
        return list.contains(itemId);
    }

    public Property getContainerProperty(Object itemId, Object propertyId) {
        return getItem(itemId).getItemProperty(propertyId);
    }

    public Collection<String> getContainerPropertyIds() {
        return model.keySet();
    }

    public BeanItem<BT> getItem(Object itemId) {
        return beanToItem.get(itemId);
    }

    public Collection<BT> getItemIds() {
        return (Collection<BT>) list.clone();
    }

    public Class<?> getType(Object propertyId) {
        return model.get(propertyId).getPropertyType();
    }

    public boolean removeAllItems() throws UnsupportedOperationException {
        allItems.clear();
        list.clear();
        // detach listeners from all BeanItems
        for (BeanItem<BT> item : beanToItem.values()) {
            removeAllValueChangeListeners(item);
        }
        beanToItem.clear();
        fireItemSetChange();
        return true;
    }

    public boolean removeContainerProperty(Object propertyId)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public boolean removeItem(Object itemId)
            throws UnsupportedOperationException {
        if (!allItems.remove(itemId)) {
            return false;
        }
        // detach listeners from Item
        removeAllValueChangeListeners(getItem(itemId));
        // remove item
        beanToItem.remove(itemId);
        list.remove(itemId);
        fireItemSetChange();
        return true;
    }

    private void addValueChangeListener(BeanItem<BT> beanItem, Object propertyId) {
        Property property = beanItem.getItemProperty(propertyId);
        if (property instanceof ValueChangeNotifier) {
            // avoid multiple notifications for the same property if
            // multiple filters are in use
            ValueChangeNotifier notifier = (ValueChangeNotifier) property;
            notifier.removeListener(this);
            notifier.addListener(this);
        }
    }

    private void removeValueChangeListener(BeanItem<BT> item, Object propertyId) {
        Property property = item.getItemProperty(propertyId);
        if (property instanceof ValueChangeNotifier) {
            ((ValueChangeNotifier) property).removeListener(this);
        }
    }

    private void removeAllValueChangeListeners(BeanItem<BT> item) {
        for (Object propertyId : item.getItemPropertyIds()) {
            removeValueChangeListener(item, propertyId);
        }
    }

    public int size() {
        return list.size();
    }

    public Collection<Object> getSortableContainerPropertyIds() {
        LinkedList<Object> sortables = new LinkedList<Object>();
        for (Object propertyId : getContainerPropertyIds()) {
            Class<?> propertyType = getType(propertyId);
            if (Comparable.class.isAssignableFrom(propertyType)) {
                sortables.add(propertyId);
            }
        }
        return sortables;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.data.Container.Sortable#sort(java.lang.Object[],
     * boolean[])
     */
    public void sort(Object[] propertyId, boolean[] ascending) {
        itemSorter.setSortProperties(this, propertyId, ascending);

        doSort();

        // notifies if anything changes in the filtered list, including order
        filterAll();
    }

    /**
     * Perform the sorting of the data structures in the container. This is
     * invoked when the <code>itemSorter</code> has been prepared for the sort
     * operation. Typically this method calls
     * <code>Collections.sort(aCollection, getItemSorter())</code> on all arrays
     * (containing item ids) that need to be sorted.
     * 
     */
    protected void doSort() {
        Collections.sort(allItems, getItemSorter());
    }

    public void addListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners == null) {
            itemSetChangeListeners = new LinkedList<ItemSetChangeListener>();
        }
        itemSetChangeListeners.add(listener);
    }

    public void removeListener(ItemSetChangeListener listener) {
        if (itemSetChangeListeners != null) {
            itemSetChangeListeners.remove(listener);
        }
    }

    private void fireItemSetChange() {
        if (itemSetChangeListeners != null) {
            final Container.ItemSetChangeEvent event = new Container.ItemSetChangeEvent() {
                public Container getContainer() {
                    return BeanItemContainer.this;
                }
            };
            for (ItemSetChangeListener listener : itemSetChangeListeners) {
                listener.containerItemSetChange(event);
            }
        }
    }

    public void addContainerFilter(Object propertyId, String filterString,
            boolean ignoreCase, boolean onlyMatchPrefix) {
        if (filters.isEmpty()) {
            list = (ArrayList<BT>) allItems.clone();
        }
        // listen to change events to be able to update filtering
        for (BeanItem<BT> item : beanToItem.values()) {
            addValueChangeListener(item, propertyId);
        }
        Filter f = new Filter(propertyId, filterString, ignoreCase,
                onlyMatchPrefix);
        filter(f);
        filters.add(f);
        fireItemSetChange();
    }

    /**
     * Filter the view to recreate the visible item list from the unfiltered
     * items, and send a notification if the set of visible items changed in any
     * way.
     */
    protected void filterAll() {
        // avoid notification if the filtering had no effect
        List<BT> originalItems = list;
        // it is somewhat inefficient to do a (shallow) clone() every time
        list = (ArrayList<BT>) allItems.clone();
        for (Filter f : filters) {
            filter(f);
        }
        // check if exactly the same items are there after filtering to avoid
        // unnecessary notifications
        // this may be slow in some cases as it uses BT.equals()
        if (!originalItems.equals(list)) {
            fireItemSetChange();
        }
    }

    protected void filter(Filter f) {
        Iterator<BT> iterator = list.iterator();
        while (iterator.hasNext()) {
            BT bean = iterator.next();
            if (!f.passesFilter(getItem(bean))) {
                iterator.remove();
            }
        }
    }

    public void removeAllContainerFilters() {
        if (!filters.isEmpty()) {
            filters = new HashSet<Filter>();
            // stop listening to change events for any property
            for (BeanItem<BT> item : beanToItem.values()) {
                removeAllValueChangeListeners(item);
            }
            filterAll();
        }
    }

    public void removeContainerFilters(Object propertyId) {
        if (!filters.isEmpty()) {
            for (Iterator<Filter> iterator = filters.iterator(); iterator
                    .hasNext();) {
                Filter f = iterator.next();
                if (f.propertyId.equals(propertyId)) {
                    iterator.remove();
                }
            }
            // stop listening to change events for the property
            for (BeanItem<BT> item : beanToItem.values()) {
                removeValueChangeListener(item, propertyId);
            }
            filterAll();
        }
    }

    public void valueChange(ValueChangeEvent event) {
        // if a property that is used in a filter is changed, refresh filtering
        filterAll();
    }

    public ItemSorter getItemSorter() {
        return itemSorter;
    }

    public void setItemSorter(ItemSorter itemSorter) {
        this.itemSorter = itemSorter;
    }

}
