/*
@ITMillApache2LicenseForJavaFiles@
 */
package com.vaadin.data.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.data.Property;
import com.vaadin.util.SerializerHelper;

/**
 * Property descriptor that is able to create simple {@link MethodProperty}
 * instances for a bean, using given accessors.
 * 
 * @param <BT>
 *            bean type
 * 
 * @since 6.6
 */
public class MethodPropertyDescriptor<BT> implements
        VaadinPropertyDescriptor<BT> {

    private static final Logger logger = Logger
            .getLogger(MethodPropertyDescriptor.class.getName());

    private final String name;
    private Class<?> propertyType;
    private transient Method readMethod;
    private transient Method writeMethod;

    /**
     * Creates a property descriptor that can create MethodProperty instances to
     * access the underlying bean property.
     * 
     * @param name
     *            of the property
     * @param propertyType
     *            type (class) of the property
     * @param readMethod
     *            getter {@link Method} for the property
     * @param writeMethod
     *            setter {@link Method} for the property or null if read-only
     *            property
     */
    public MethodPropertyDescriptor(String name, Class<?> propertyType,
            Method readMethod, Method writeMethod) {
        this.name = name;
        this.propertyType = propertyType;
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
    }

    /* Special serialization to handle method references */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        SerializerHelper.writeClass(out, propertyType);

        if (writeMethod != null) {
            out.writeObject(writeMethod.getName());
            SerializerHelper.writeClass(out, writeMethod.getDeclaringClass());
            SerializerHelper.writeClassArray(out,
                    writeMethod.getParameterTypes());
        } else {
            out.writeObject(null);
            out.writeObject(null);
            out.writeObject(null);
        }

        if (readMethod != null) {
            out.writeObject(readMethod.getName());
            SerializerHelper.writeClass(out, readMethod.getDeclaringClass());
            SerializerHelper.writeClassArray(out,
                    readMethod.getParameterTypes());
        } else {
            out.writeObject(null);
            out.writeObject(null);
            out.writeObject(null);
        }
    }

    /* Special serialization to handle method references */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        try {
            @SuppressWarnings("unchecked")
            // business assumption; type parameters not checked at runtime
            Class<BT> class1 = (Class<BT>) SerializerHelper.readClass(in);
            propertyType = class1;

            String name = (String) in.readObject();
            Class<?> writeMethodClass = SerializerHelper.readClass(in);
            Class<?>[] paramTypes = SerializerHelper.readClassArray(in);
            if (name != null) {
                writeMethod = writeMethodClass.getMethod(name, paramTypes);
            } else {
                writeMethod = null;
            }

            name = (String) in.readObject();
            Class<?> readMethodClass = SerializerHelper.readClass(in);
            paramTypes = SerializerHelper.readClassArray(in);
            if (name != null) {
                readMethod = readMethodClass.getMethod(name, paramTypes);
            } else {
                readMethod = null;
            }
        } catch (SecurityException e) {
            logger.log(Level.SEVERE, "Internal deserialization error", e);
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Internal deserialization error", e);
        }
    };

    public String getName() {
        return name;
    }

    public Class<?> getPropertyType() {
        return propertyType;
    }

    public Property<?> createProperty(Object bean) {
        return new MethodProperty<Object>(propertyType, bean, readMethod,
                writeMethod);
    }

}