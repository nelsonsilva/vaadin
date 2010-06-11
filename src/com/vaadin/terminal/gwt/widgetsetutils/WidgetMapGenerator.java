/*
@ITMillApache2LicenseForJavaFiles@
 */
package com.vaadin.terminal.gwt.widgetsetutils;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.vaadin.terminal.Paintable;
import com.vaadin.terminal.gwt.client.ui.VView;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;

/**
 * GWT generator to build WidgetMapImpl dynamically based on
 * {@link ClientWidget} annotations available in workspace.
 * 
 */
public class WidgetMapGenerator extends Generator {

    private String packageName;
    private String className;

    @Override
    public String generate(TreeLogger logger, GeneratorContext context,
            String typeName) throws UnableToCompleteException {

        try {
            TypeOracle typeOracle = context.getTypeOracle();

            // get classType and save instance variables
            JClassType classType = typeOracle.getType(typeName);
            packageName = classType.getPackage().getName();
            className = classType.getSimpleSourceName() + "Impl";
            // Generate class source code
            generateClass(logger, context);
        } catch (Exception e) {
            logger.log(TreeLogger.ERROR, "WidgetMap creation failed", e);
        }
        // return the fully qualifed name of the class generated
        return packageName + "." + className;
    }

    /**
     * Generate source code for WidgetMapImpl
     * 
     * @param logger
     *            Logger object
     * @param context
     *            Generator context
     */
    private void generateClass(TreeLogger logger, GeneratorContext context) {
        // get print writer that receives the source code
        PrintWriter printWriter = null;
        printWriter = context.tryCreate(logger, packageName, className);
        // print writer if null, source code has ALREADY been generated,
        // return (WidgetMap is equal to all permutations atm)
        if (printWriter == null) {
            return;
        }
        logger
                .log(Type.INFO,
                        "Detecting Vaadin components in classpath to generate WidgetMapImpl.java ...");
        Date date = new Date();

        // init composer, set class properties, create source writer
        ClassSourceFileComposerFactory composer = null;
        composer = new ClassSourceFileComposerFactory(packageName, className);
        composer.addImport("com.google.gwt.core.client.GWT");
        composer.addImport("java.util.HashMap");
        composer.addImport("com.google.gwt.core.client.RunAsyncCallback");
        composer.setSuperclass("com.vaadin.terminal.gwt.client.WidgetMap");
        SourceWriter sourceWriter = composer.createSourceWriter(context,
                printWriter);

        Collection<Class<? extends Paintable>> paintablesHavingWidgetAnnotation = getUsedPaintables();

        validatePaintables(logger, context, paintablesHavingWidgetAnnotation);

        // generator constructor source code
        generateImplementationDetector(sourceWriter,
                paintablesHavingWidgetAnnotation);
        generateInstantiatorMethod(sourceWriter,
                paintablesHavingWidgetAnnotation);
        // close generated class
        sourceWriter.outdent();
        sourceWriter.println("}");
        // commit generated class
        context.commit(logger, printWriter);
        logger.log(Type.INFO, "Done. ("
                + (new Date().getTime() - date.getTime()) / 1000 + "seconds)");

    }

    /**
     * Verifies that all client side components are available for client side
     * GWT module.
     * 
     * @param logger
     * @param context
     * @param paintablesHavingWidgetAnnotation
     */
    private void validatePaintables(
            TreeLogger logger,
            GeneratorContext context,
            Collection<Class<? extends Paintable>> paintablesHavingWidgetAnnotation) {
        TypeOracle typeOracle = context.getTypeOracle();

        for (Iterator<Class<? extends Paintable>> iterator = paintablesHavingWidgetAnnotation
                .iterator(); iterator.hasNext();) {
            Class<? extends Paintable> class1 = iterator.next();

            ClientWidget annotation = class1.getAnnotation(ClientWidget.class);

            if (typeOracle.findType(annotation.value().getName()) == null) {
                // GWT widget not inherited
                logger.log(Type.WARN, "Widget class "
                        + annotation.value().getName()
                        + " was not found. The component " + class1.getName()
                        + " will not be included in the widgetset.");
                iterator.remove();
            }

        }
        logger
                .log(Type.INFO,
                        "Widget set will contain implementations for following components: ");

        TreeSet<String> classNames = new TreeSet<String>();
        for (Class<? extends Paintable> class1 : paintablesHavingWidgetAnnotation) {
            classNames.add(class1.getCanonicalName());
        }
        for (String string : classNames) {
            logger.log(Type.INFO, "\t" + string);
        }
    }

    /**
     * This method is protected to allow creation of optimized widgetsets. The
     * Widgetset will contain only implementation returned by this function. If
     * one knows which widgets are needed for the application, returning only
     * them here will significantly optimize the size of the produced JS.
     * 
     * @return a collections of Vaadin components that will be added to
     *         widgetset
     */
    protected Collection<Class<? extends Paintable>> getUsedPaintables() {
        return ClassPathExplorer.getPaintablesHavingWidgetAnnotation();
    }

    /**
     * Returns true if the widget for given component will be lazy loaded by the
     * client. The default implementation reads the information from the
     * {@link ClientWidget} annotation.
     * <p>
     * The method can be overridden to optimize the widget loading mechanism. If
     * the Widgetset is wanted to be optimized for a network with a high latency
     * or for a one with a very fast throughput, it may be good to return false
     * for every component.
     * 
     * @param paintableType
     * @return true iff the widget for given component should be lazy loaded by
     *         the client side engine
     */
    protected LoadStyle getLoadStyle(Class<? extends Paintable> paintableType) {
        ClientWidget annotation = paintableType
                .getAnnotation(ClientWidget.class);
        return annotation.loadStyle();
    }

    private void generateInstantiatorMethod(
            SourceWriter sourceWriter,
            Collection<Class<? extends Paintable>> paintablesHavingWidgetAnnotation) {

        Collection<Class<?>> deferredWidgets = new LinkedList<Class<?>>();

        // TODO detect if it would be noticably faster to instantiate with a
        // lookup with index than with the hashmap

        sourceWriter
                .println("public void ensureInstantiator(Class<? extends Paintable> classType) {");
        sourceWriter.println("if(!instmap.containsKey(classType)){");
        boolean first = true;

        for (Class<? extends Paintable> class1 : paintablesHavingWidgetAnnotation) {
            ClientWidget annotation = class1.getAnnotation(ClientWidget.class);
            Class<? extends com.vaadin.terminal.gwt.client.Paintable> clientClass = annotation
                    .value();
            if (clientClass == VView.class) {
                // VView's are not instantiated by widgetset
                continue;
            }
            if (!first) {
                sourceWriter.print(" else ");
            } else {
                first = false;
            }
            sourceWriter.print("if( classType == " + clientClass.getName()
                    + ".class) {");

            String instantiator = "new WidgetInstantiator() {\n public Paintable get() {\n return GWT.create("
                    + clientClass.getName() + ".class );\n}\n}\n";

            LoadStyle loadStyle = getLoadStyle(class1);

            if (loadStyle != LoadStyle.EAGER) {
                sourceWriter
                        .print("ApplicationConfiguration.startWidgetLoading();\n"
                                + "GWT.runAsync( \n"
                                + "new WidgetLoader() { void addInstantiator() {instmap.put("
                                + clientClass.getName()
                                + ".class,"
                                + instantiator + ");}});\n");

                if (loadStyle == LoadStyle.DEFERRED) {
                    deferredWidgets.add(class1);
                }

            } else {
                // widget implementation in initially loaded js script
                sourceWriter.print("instmap.put(");
                sourceWriter.print(clientClass.getName());
                sourceWriter.print(".class, ");
                sourceWriter.print(instantiator);
                sourceWriter.print(");");
            }
            sourceWriter.print("}");
        }

        sourceWriter.println("}");

        sourceWriter.println("}");

        sourceWriter
                .println("public Class<? extends Paintable>[] getDeferredLoadedWidgets() {");

        sourceWriter.println("return new Class[] {");
        first = true;
        for (Class<?> class2 : deferredWidgets) {
            if (!first) {
                sourceWriter.println(",");
            }
            first = false;
            ClientWidget annotation = class2.getAnnotation(ClientWidget.class);
            Class<? extends com.vaadin.terminal.gwt.client.Paintable> value = annotation
                    .value();
            sourceWriter.print(value.getName() + ".class");
        }

        sourceWriter.println("};");
        sourceWriter.println("}");
    }

    /**
     * 
     * @param sourceWriter
     *            Source writer to output source code
     * @param paintablesHavingWidgetAnnotation
     */
    private void generateImplementationDetector(
            SourceWriter sourceWriter,
            Collection<Class<? extends Paintable>> paintablesHavingWidgetAnnotation) {
        sourceWriter
                .println("public Class<? extends Paintable> "
                        + "getImplementationByServerSideClassName(String fullyQualifiedName) {");
        sourceWriter.indent();
        sourceWriter
                .println("fullyQualifiedName = fullyQualifiedName.intern();");

        for (Class<? extends Paintable> class1 : paintablesHavingWidgetAnnotation) {
            ClientWidget annotation = class1.getAnnotation(ClientWidget.class);
            Class<? extends com.vaadin.terminal.gwt.client.Paintable> clientClass = annotation
                    .value();
            sourceWriter.print("if ( fullyQualifiedName == \"");
            sourceWriter.print(class1.getName());
            sourceWriter.print("\" ) { ensureInstantiator("
                    + clientClass.getName() + ".class); return ");
            sourceWriter.print(clientClass.getName());
            sourceWriter.println(".class;}");
            sourceWriter.print("else ");
        }
        sourceWriter
                .println("return com.vaadin.terminal.gwt.client.ui.VUnknownComponent.class;");
        sourceWriter.outdent();
        sourceWriter.println("}");

    }
}
