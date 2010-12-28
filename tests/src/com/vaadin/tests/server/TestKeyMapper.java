package com.vaadin.tests.server;

import java.lang.reflect.Field;
import java.util.Hashtable;

import junit.framework.TestCase;

import com.vaadin.terminal.KeyMapper;

public class TestKeyMapper extends TestCase {

    public void testAdd() {
        KeyMapper mapper = new KeyMapper();
        Object o1 = new Object();
        Object o2 = new Object();
        Object o3 = new Object();

        // Create new ids
        String key1 = mapper.key(o1);
        String key2 = mapper.key(o2);
        String key3 = mapper.key(o3);

        assertEquals(mapper.get(key1), o1);
        assertEquals(mapper.get(key2), o2);
        assertEquals(mapper.get(key3), o3);
        assertNotSame(key1, key2);
        assertNotSame(key1, key3);
        assertNotSame(key2, key3);

        assertSize(mapper, 3);

        // Key should not add if there already is a mapping
        assertEquals(mapper.key(o3), key3);
        assertSize(mapper, 3);

        // Remove -> add should return a new key
        mapper.remove(o1);
        String newkey1 = mapper.key(o1);
        assertNotSame(key1, newkey1);

    }

    public void testRemoveAll() {
        KeyMapper mapper = new KeyMapper();
        Object o1 = new Object();
        Object o2 = new Object();
        Object o3 = new Object();

        // Create new ids
        mapper.key(o1);
        mapper.key(o2);
        mapper.key(o3);

        assertSize(mapper, 3);
        mapper.removeAll();
        assertSize(mapper, 0);

    }

    public void testRemove() {
        KeyMapper mapper = new KeyMapper();
        Object o1 = new Object();
        Object o2 = new Object();
        Object o3 = new Object();

        // Create new ids
        mapper.key(o1);
        mapper.key(o2);
        mapper.key(o3);

        assertSize(mapper, 3);
        mapper.remove(o1);
        assertSize(mapper, 2);
        mapper.key(o1);
        assertSize(mapper, 3);
        mapper.remove(o1);
        assertSize(mapper, 2);

        mapper.remove(o2);
        mapper.remove(o3);
        assertSize(mapper, 0);

    }

    private void assertSize(KeyMapper mapper, int i) {
        try {
            Field f1 = KeyMapper.class.getDeclaredField("objectKeyMap");
            Field f2 = KeyMapper.class.getDeclaredField("keyObjectMap");
            f1.setAccessible(true);
            f2.setAccessible(true);

            Hashtable<?, ?> h1 = (Hashtable<?, ?>) f1.get(mapper);
            Hashtable<?, ?> h2 = (Hashtable<?, ?>) f2.get(mapper);

            assertEquals(i, h1.size());
            assertEquals(i, h2.size());
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
    }
}
