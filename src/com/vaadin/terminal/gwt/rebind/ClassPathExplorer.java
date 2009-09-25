package com.vaadin.terminal.gwt.rebind;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.vaadin.terminal.Paintable;
import com.vaadin.ui.ClientWidget;

/**
 * Utility class to find server side widgets with {@link ClientWidget}
 * annotation. Used by WidgetMapGenerator to implement some monkey coding for
 * you.
 * <p>
 * If you end up reading this comment, I guess you have faced a sluggish
 * performance of widget compilation or unreliable detection of components in
 * your classpaths. The thing you might be able to do is to use annotation
 * processing tool like apt to generate the needed information. Then either use
 * that information in {@link WidgetMapGenerator} or create the appropriate
 * monkey code for gwt directly in annotation processor and get rid of
 * {@link WidgetMapGenerator}. Using annotation processor might be a good idea
 * when dropping Java 1.5 support (integrated to javac in 6).
 * 
 */
public class ClassPathExplorer {
    private final static FileFilter DIRECTORIES_ONLY = new FileFilter() {
        public boolean accept(File f) {
            if (f.exists() && f.isDirectory()) {
                return true;
            } else {
                return false;
            }
        }
    };

    private static Map<URL, String> classpathLocations = getClasspathLocations();

    private ClassPathExplorer() {
    }

    public static Collection<Class<? extends Paintable>> getPaintablesHavingWidgetAnnotation() {
        Collection<Class<? extends Paintable>> paintables = new HashSet<Class<? extends Paintable>>();
        Set<URL> keySet = classpathLocations.keySet();
        for (URL url : keySet) {
            searchForPaintables(url, classpathLocations.get(url), paintables);
        }
        return paintables;

    }

    /**
     * Determine every URL location defined by the current classpath, and it's
     * associated package name.
     */
    public final static Map<URL, String> getClasspathLocations() {
        Map<URL, String> locations = new HashMap<URL, String>();

        String pathSep = System.getProperty("path.separator");
        String classpath = System.getProperty("java.class.path");

        String[] split = classpath.split(pathSep);
        for (int i = 0; i < split.length; i++) {
            String classpathEntry = split[i];
            if (acceptClassPathEntry(classpathEntry)) {
                File file = new File(classpathEntry);
                include(null, file, locations);
            }
        }

        return locations;
    }

    private static boolean acceptClassPathEntry(String classpathEntry) {
        if (!classpathEntry.endsWith(".jar")) {
            // accept all non jars (practically directories)
            return true;
        } else {
            // accepts jars that comply with vaadin-component packaging
            // convention (.vaadin. or vaadin- as distribution packages),
            if (classpathEntry.contains("vaadin-")
                    || classpathEntry.contains(".vaadin.")) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Recursively add subdirectories and jar files to classpathlocations
     * 
     * @param name
     * @param file
     * @param locations
     */
    private final static void include(String name, File file,
            Map<URL, String> locations) {
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            // could be a JAR file
            includeJar(file, locations);
            return;
        }

        if (file.isHidden() || file.getPath().contains(File.separator + ".")) {
            return;
        }

        if (name == null) {
            name = "";
        } else {
            name += ".";
        }

        // add all directories recursively
        File[] dirs = file.listFiles(DIRECTORIES_ONLY);
        for (int i = 0; i < dirs.length; i++) {
            try {
                // add the present directory
                locations.put(new URL("file://" + dirs[i].getCanonicalPath()),
                        name + dirs[i].getName());
            } catch (Exception ioe) {
                return;
            }
            include(name + dirs[i].getName(), dirs[i], locations);
        }
    }

    private static void includeJar(File file, Map<URL, String> locations) {
        try {
            URL url = new URL("file:" + file.getCanonicalPath());
            url = new URL("jar:" + url.toExternalForm() + "!/");
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            JarFile jarFile = conn.getJarFile();
            if (jarFile != null) {
                locations.put(url, "");
            }
        } catch (Exception e) {
            // e.printStackTrace();
            return;
        }

    }

    private static String packageNameFor(JarEntry entry) {
        if (entry == null) {
            return "";
        }
        String s = entry.getName();
        if (s == null) {
            return "";
        }
        if (s.length() == 0) {
            return s;
        }
        if (s.startsWith("/")) {
            s = s.substring(1, s.length());
        }
        if (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s.replace('/', '.');
    }

    private final static void searchForPaintables(URL location,
            String packageName,
            Collection<Class<? extends Paintable>> paintables) {

        // Get a File object for the package
        File directory = new File(location.getFile());

        if (directory.exists() && !directory.isHidden()) {
            // Get the list of the files contained in the directory
            String[] files = directory.list();
            for (int i = 0; i < files.length; i++) {
                // we are only interested in .class files
                if (files[i].endsWith(".class")) {
                    // remove the .class extension
                    String classname = files[i].substring(0,
                            files[i].length() - 6);
                    classname = packageName + "." + classname;
                    tryToAdd(classname, paintables);
                }
            }
        } else {
            try {
                // check files in jar file, entries will list all directories
                // and files in jar

                URLConnection openConnection = location.openConnection();

                if (openConnection instanceof JarURLConnection) {
                    JarURLConnection conn = (JarURLConnection) openConnection;

                    JarFile jarFile = conn.getJarFile();

                    Enumeration<JarEntry> e = jarFile.entries();
                    while (e.hasMoreElements()) {
                        JarEntry entry = e.nextElement();
                        String entryname = entry.getName();
                        if (!entry.isDirectory()
                                && entryname.endsWith(".class")
                                && !entryname.contains("$")) {
                            String classname = entryname.substring(0, entryname
                                    .length() - 6);
                            if (classname.startsWith("/")) {
                                classname = classname.substring(1);
                            }
                            classname = classname.replace('/', '.');
                            tryToAdd(classname, paintables);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }

    }

    private static void tryToAdd(final String fullclassName,
            Collection<Class<? extends Paintable>> paintables) {
        try {
            Class<?> c = Class.forName(fullclassName);
            if (c.getAnnotation(ClientWidget.class) != null) {
                paintables.add((Class<? extends Paintable>) c);
                System.out.println("Found paintable " + fullclassName);
            }
        } catch (ExceptionInInitializerError e) {
            // e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // e.printStackTrace();
        } catch (NoClassDefFoundError e) {
            // NOP
        } catch (UnsatisfiedLinkError e) {
            // NOP
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for helper tool
     */
    public static void main(String[] args) {
        Collection<Class<? extends Paintable>> paintables = ClassPathExplorer
                .getPaintablesHavingWidgetAnnotation();
        System.out.println("Found annotated paintables:");
        for (Class<? extends Paintable> cls : paintables) {
            System.out.println(cls.getCanonicalName());
        }
    }
}