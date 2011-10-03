package com.vaadin.tests.server;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.TestCase;

import org.junit.Test;

public class TestClassesSerializable extends TestCase {

    /**
     * JARs that will be scanned for classes to test, in addition to classpath
     * directories.
     */
    private static String JAR_PATTERN = ".*vaadin.*\\.jar";

    private static String[] BASE_PACKAGES = { "com.vaadin" };

    private static String[] EXCLUDED_PATTERNS = {
            "com\\.vaadin\\.demo\\..*", //
            "com\\.vaadin\\.external\\.org\\.apache\\.commons\\.fileupload\\..*", //
            "com\\.vaadin\\.launcher\\..*", //
            "com\\.vaadin\\.terminal\\.gwt\\.client\\..*", //
            "com\\.vaadin\\.terminal\\.gwt\\.widgetsetutils\\..*", //
            "com\\.vaadin\\.tests\\..*", // exclude automated tests
            "com\\.vaadin\\.tools\\..*", //
            "com\\.vaadin\\.ui\\.themes\\..*", //
            // exact class level filtering
            "com\\.vaadin\\.event\\.FieldEvents", //
            "com\\.vaadin\\.event\\.LayoutEvents", //
            "com\\.vaadin\\.event\\.MouseEvents", //
            "com\\.vaadin\\.terminal\\.gwt\\.server\\.AbstractApplicationPortlet", //
            "com\\.vaadin\\.terminal\\.gwt\\.server\\.ApplicationPortlet2", //
            "com\\.vaadin\\.terminal\\.gwt\\.server\\.Constants", //
            "com\\.vaadin\\.util\\.SerializerHelper", // fully static
            // class level filtering, also affecting nested classes and
            // interfaces
            "com\\.vaadin\\.terminal\\.gwt\\.server\\.AbstractCommunicationManager.*", //
            "com\\.vaadin\\.terminal\\.gwt\\.server\\.ApplicationRunnerServlet.*", //
            "com\\.vaadin\\.terminal\\.gwt\\.server\\.CommunicationManager.*", //
            "com\\.vaadin\\.terminal\\.gwt\\.server\\.PortletCommunicationManager.*", //
    };

    /**
     * Tests that all the relevant classes and interfaces under
     * {@link #BASE_PACKAGES} implement Serializable.
     * 
     * @throws Exception
     */
    public void testClassesSerializable() throws Exception {
        List<String> rawClasspathEntries = getRawClasspathEntries();

        List<String> classes = new ArrayList<String>();
        for (String location : rawClasspathEntries) {
            classes.addAll(findServerClasses(location));
        }

        ArrayList<Class<?>> nonSerializableClasses = new ArrayList<Class<?>>();
        for (String className : classes) {
            Class<?> cls = Class.forName(className);
            // skip annotations and synthetic classes
            if (cls.isAnnotation() || cls.isSynthetic()) {
                continue;
            }
            // Don't add classes that have a @Test annotation on any methods
            boolean testPresent = false;
            for (Method method : cls.getMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    testPresent = true;
                    break;
                }
            }
            if (testPresent) {
                continue;
            }

            // report non-serializable classes and interfaces
            if (!Serializable.class.isAssignableFrom(cls)) {
                nonSerializableClasses.add(cls);
                // TODO easier to read when testing
                // System.err.println(cls);
            }
        }

        // useful failure message including all non-serializable classes and
        // interfaces
        if (!nonSerializableClasses.isEmpty()) {
            String nonSerializableString = "";
            Iterator<Class<?>> it = nonSerializableClasses.iterator();
            nonSerializableString = it.next().getName();
            while (it.hasNext()) {
                nonSerializableString += ", " + it.next().getName();
            }
            fail("Serializable not implemented by the following classes and interfaces: "
                    + nonSerializableString);
        }
    }

    /**
     * Lists all class path entries by splitting the class path string.
     * 
     * Adapted from ClassPathExplorer.getRawClasspathEntries(), but without
     * filtering.
     * 
     * @return List of class path segment strings
     */
    //
    private final static List<String> getRawClasspathEntries() {
        // try to keep the order of the classpath
        List<String> locations = new ArrayList<String>();

        String pathSep = System.getProperty("path.separator");
        String classpath = System.getProperty("java.class.path");

        if (classpath.startsWith("\"")) {
            classpath = classpath.substring(1);
        }
        if (classpath.endsWith("\"")) {
            classpath = classpath.substring(0, classpath.length() - 1);
        }

        String[] split = classpath.split(pathSep);
        for (int i = 0; i < split.length; i++) {
            String classpathEntry = split[i];
            locations.add(classpathEntry);
        }

        return locations;
    }

    /**
     * Finds the server side classes/interfaces under a class path entry -
     * either a directory or a JAR that matches {@link #JAR_PATTERN}.
     * 
     * Only classes under {@link #BASE_PACKAGES} are considered, and those
     * matching {@link #EXCLUDED_PATTERNS} are filtered out.
     * 
     * @param classpathEntry
     * @return
     * @throws IOException
     */
    private List<String> findServerClasses(String classpathEntry)
            throws IOException {
        Collection<String> classes = new ArrayList<String>();

        File file = new File(classpathEntry);
        if (file.isDirectory()) {
            classes = findClassesInDirectory(null, file);
        } else if (file.getName().matches(JAR_PATTERN)) {
            classes = findClassesInJar(file);
        } else {
            System.out.println("Ignoring " + classpathEntry);
            return Collections.emptyList();
        }

        List<String> filteredClasses = new ArrayList<String>();
        for (String className : classes) {
            boolean ok = false;
            for (String basePackage : BASE_PACKAGES) {
                if (className.startsWith(basePackage + ".")) {
                    ok = true;
                    break;
                }
            }
            for (String excludedPrefix : EXCLUDED_PATTERNS) {
                if (className.matches(excludedPrefix)) {
                    ok = false;
                    break;
                }
            }

            // Don't add test classes
            if (className.contains("Test")) {
                ok = false;
            }

            if (ok) {
                filteredClasses.add(className);
            }
        }

        return filteredClasses;
    }

    /**
     * Lists class names (based on .class files) in a JAR file.
     * 
     * @param file
     *            a valid JAR file
     * @return collection of fully qualified class names in the JAR
     * @throws IOException
     */
    private Collection<String> findClassesInJar(File file) throws IOException {
        Collection<String> classes = new ArrayList<String>();

        JarFile jar = new JarFile(file);
        Enumeration<JarEntry> e = jar.entries();
        while (e.hasMoreElements()) {
            JarEntry entry = e.nextElement();
            if (entry.getName().endsWith(".class")) {
                String nameWithoutExtension = entry.getName().replaceAll(
                        "\\.class", "");
                String className = nameWithoutExtension.replace('/', '.');
                classes.add(className);
            }
        }
        return classes;
    }

    /**
     * Lists class names (based on .class files) in a directory (a package path
     * root).
     * 
     * @param parentPackage
     *            parent package name or null at root of hierarchy, used by
     *            recursion
     * @param parent
     *            File representing the directory to scan
     * @return collection of fully qualified class names in the directory
     */
    private final static Collection<String> findClassesInDirectory(
            String parentPackage, File parent) {
        if (parent.isHidden()
                || parent.getPath().contains(File.separator + ".")) {
            return Collections.emptyList();
        }

        if (parentPackage == null) {
            parentPackage = "";
        } else {
            parentPackage += ".";
        }

        Collection<String> classNames = new ArrayList<String>();

        // add all directories recursively
        File[] files = parent.listFiles();
        for (File child : files) {
            if (child.isDirectory()) {
                classNames.addAll(findClassesInDirectory(
                        parentPackage + child.getName(), child));
            } else if (child.getName().endsWith(".class")) {
                classNames.add(parentPackage.replace(File.separatorChar, '.')
                        + child.getName().replaceAll("\\.class", ""));
            }
        }

        return classNames;
    }

}
