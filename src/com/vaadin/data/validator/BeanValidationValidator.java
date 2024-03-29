/* 
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.data.validator;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator.Context;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import com.vaadin.data.Validator;
import com.vaadin.ui.Field;

/**
 * Vaadin {@link Validator} using the JSR-303 (javax.validation)
 * annotation-based bean validation.
 * 
 * The annotations of the fields of the beans are used to determine the
 * validation to perform.
 * 
 * Note that a JSR-303 implementation (e.g. Hibernate Validator or Apache Bean
 * Validation - formerly agimatec validation) must be present on the project
 * classpath when using bean validation.
 * 
 * @since 7.0
 * 
 * @author Petri Hakala
 * @author Henri Sara
 */
public class BeanValidationValidator implements Validator {

    private static final long serialVersionUID = 1L;
    private static ValidatorFactory factory;
    private static Boolean implementationAvailable = null;

    private transient javax.validation.Validator javaxBeanValidator;
    private String propertyName;
    private Class<?> beanClass;
    private Locale locale;

    /**
     * Simple implementation of a message interpolator context that returns
     * fixed values.
     */
    protected static class SimpleContext implements Context, Serializable {

        private final Object value;
        private final ConstraintDescriptor<?> descriptor;

        /**
         * Create a simple immutable message interpolator context.
         * 
         * @param value
         *            value being validated
         * @param descriptor
         *            ConstraintDescriptor corresponding to the constraint being
         *            validated
         */
        public SimpleContext(Object value, ConstraintDescriptor<?> descriptor) {
            this.value = value;
            this.descriptor = descriptor;
        }

        public ConstraintDescriptor<?> getConstraintDescriptor() {
            return descriptor;
        }

        public Object getValidatedValue() {
            return value;
        }

    }

    /**
     * Creates a Vaadin {@link Validator} utilizing JSR-303 bean validation.
     * 
     * @param beanClass
     *            bean class based on which the validation should be performed
     * @param propertyName
     *            property to validate
     */
    public BeanValidationValidator(Class<?> beanClass, String propertyName) {
        this.beanClass = beanClass;
        this.propertyName = propertyName;
        locale = Locale.getDefault();
    }

    /**
     * Apply a bean validation validator to a field based on a bean class and
     * the identifier of the property the field displays. The field is also
     * marked as required if the bean field has the {@link NotNull} annotation.
     * <p>
     * No actual Vaadin validator is added in case no or only {@link NotNull}
     * validation is used (required is practically same as NotNull validation).
     * 
     * @param field
     *            the {@link Field} component to which to add a validator
     * @param objectPropertyId
     *            the property ID of the field of the bean that this field
     *            displays
     * @param beanClass
     *            the class of the bean with the bean validation annotations
     * @return the created validator
     */
    public static BeanValidationValidator addValidator(Field<?> field,
            Object objectPropertyId, Class<?> beanClass) {
        if (objectPropertyId == null || !(objectPropertyId instanceof String)) {
            throw new IllegalArgumentException(
                    "Property id must be a non-null String");
        }

        String propertyId = (String) objectPropertyId;
        BeanValidationValidator validator = new BeanValidationValidator(
                beanClass, propertyId);
        PropertyDescriptor constraintsForProperty = validator
                .getJavaxBeanValidator().getConstraintsForClass(beanClass)
                .getConstraintsForProperty(propertyId);
        if (constraintsForProperty != null) {
            int nonNotNullValidators = constraintsForProperty
                    .getConstraintDescriptors().size();
            if (validator.isRequired()) {
                field.setRequired(true);
                field.setRequiredError(validator.getRequiredMessage());
                nonNotNullValidators--;
            }
            if (nonNotNullValidators > 0) {
                field.addValidator(validator);
            }
        }
        return validator;
    }

    /**
     * Check the validity of a value. Normally, {@link #validate(Object)} should
     * be used instead of this method to also get the validation error message.
     * 
     * @param value
     * @return true if the value is valid
     */
    public boolean isValid(Object value) {
        try {
            validate(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if the property has been marked as required (has the
     * {@link NotNull} annotation.
     * 
     * @return true if the field is marked as not null
     */
    public boolean isRequired() {
        PropertyDescriptor desc = getJavaxBeanValidator()
                .getConstraintsForClass(beanClass).getConstraintsForProperty(
                        propertyName);
        if (desc != null) {
            Iterator<ConstraintDescriptor<?>> it = desc
                    .getConstraintDescriptors().iterator();
            while (it.hasNext()) {
                final ConstraintDescriptor<?> d = it.next();
                Annotation a = d.getAnnotation();
                if (a instanceof NotNull) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the message to show if a value is required but missing. The
     * message that of the {@link NotNull} annotation.
     * 
     * @return error message to show for missing required value
     */
    @SuppressWarnings("unchecked")
    public String getRequiredMessage() {
        return getErrorMessage(null, NotNull.class);
    }

    public void validate(final Object value) throws InvalidValueException {
        Set<?> violations = getJavaxBeanValidator().validateValue(beanClass,
                propertyName, value);
        if (violations.size() > 0) {
            List<String> exceptions = new ArrayList<String>();
            for (Object v : violations) {
                final ConstraintViolation<?> violation = (ConstraintViolation<?>) v;
                String msg = getJavaxBeanValidatorFactory()
                        .getMessageInterpolator().interpolate(
                                violation.getMessageTemplate(),
                                new SimpleContext(value, violation
                                        .getConstraintDescriptor()), locale);
                exceptions.add(msg);
            }
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < exceptions.size(); i++) {
                if (i != 0) {
                    b.append("<br/>");
                }
                b.append(exceptions.get(i));
            }
            throw new InvalidValueException(b.toString());
        }
    }

    private String getErrorMessage(final Object value,
            Class<? extends Annotation>... an) {
        BeanDescriptor beanDesc = getJavaxBeanValidator()
                .getConstraintsForClass(beanClass);
        PropertyDescriptor desc = beanDesc
                .getConstraintsForProperty(propertyName);
        if (desc == null) {
            // validate() reports a conversion error in this case
            return null;
        }
        Iterator<ConstraintDescriptor<?>> it = desc.getConstraintDescriptors()
                .iterator();
        List<String> exceptions = new ArrayList<String>();
        while (it.hasNext()) {
            final ConstraintDescriptor<?> d = it.next();
            Annotation a = d.getAnnotation();
            boolean skip = false;
            if (an != null && an.length > 0) {
                skip = true;
                for (Class<? extends Annotation> t : an) {
                    if (t == a.annotationType()) {
                        skip = false;
                        break;
                    }
                }
            }
            if (!skip) {
                String messageTemplate = null;
                try {
                    Method m = a.getClass().getMethod("message");
                    messageTemplate = (String) m.invoke(a);
                } catch (Exception ex) {
                    throw new InvalidValueException(
                            "Annotation must have message attribute");
                }
                String msg = getJavaxBeanValidatorFactory()
                        .getMessageInterpolator().interpolate(messageTemplate,
                                new SimpleContext(value, d), locale);
                exceptions.add(msg);
            }
        }
        if (exceptions.size() > 0) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < exceptions.size(); i++) {
                if (i != 0) {
                    b.append("<br/>");
                }
                b.append(exceptions.get(i));
            }
            return b.toString();
        }
        return null;
    }

    /**
     * Sets the locale used for validation error messages.
     * 
     * Revalidation is not automatically triggered by setting the locale.
     * 
     * @param locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Gets the locale used for validation error messages.
     * 
     * @return locale used for validation
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the underlying JSR-303 bean validator factory used. A factory is
     * created using {@link Validation} if necessary.
     * 
     * @return {@link ValidatorFactory} to use
     */
    protected static ValidatorFactory getJavaxBeanValidatorFactory() {
        if (factory == null) {
            factory = Validation.buildDefaultValidatorFactory();
        }

        return factory;
    }

    /**
     * Returns a shared Validator instance to use. An instance is created using
     * the validator factory if necessary and thereafter reused by the
     * {@link BeanValidationValidator} instance.
     * 
     * @return the JSR-303 {@link javax.validation.Validator} to use
     */
    protected javax.validation.Validator getJavaxBeanValidator() {
        if (javaxBeanValidator == null) {
            javaxBeanValidator = getJavaxBeanValidatorFactory().getValidator();
        }

        return javaxBeanValidator;
    }

    /**
     * Checks whether a bean validation implementation (e.g. Hibernate Validator
     * or Apache Bean Validation) is available.
     * 
     * @return true if a JSR-303 bean validation implementation is available
     */
    public static boolean isImplementationAvailable() {
        if (implementationAvailable == null) {
            try {
                getJavaxBeanValidatorFactory();
                implementationAvailable = true;
            } catch (Exception e) {
                implementationAvailable = false;
            }
        }
        return implementationAvailable;
    }
}