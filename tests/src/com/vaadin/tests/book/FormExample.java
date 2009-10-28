package com.vaadin.tests.book;

import java.util.Vector;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.ExpandLayout;
import com.vaadin.ui.Field;
import com.vaadin.ui.FieldFactory;
import com.vaadin.ui.Form;
import com.vaadin.ui.OrderedLayout;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextField;

/**
 * This example demonstrates the most important features of the Form component:
 * binding Form to a JavaBean so that form fields are automatically generated
 * from the bean properties, creation of fields with proper types for each bean
 * properly using a FieldFactory, buffering (commit/discard), and validation.
 * 
 * The Form is used with a FormLayout, which automatically lays the components
 * out in a format typical for forms.
 */
public class FormExample extends CustomComponent {
    /** Contact information data model. */
    public class Contact {
        String name = "";
        String address = "";
        int postalCode = 20540;
        String city;
    }

    /** Bean wrapper for the data model. */
    public class ContactBean extends Contact {
        public ContactBean() {
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getAddress() {
            return address;
        }

        public void setPostalCode(String postalCode) {
            try {
                if (postalCode != null) {
                    this.postalCode = Integer.parseInt(postalCode);
                } else {
                    this.postalCode = 0;
                }
            } catch (NumberFormatException e) {
                this.postalCode = 0;
            }
        }

        public String getPostalCode() {
            if (postalCode > 0) {
                return String.valueOf(postalCode);
            } else {
                return "";
            }
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCity() {
            return city;
        }
    }

    /**
     * Factory to create the proper type of field for each property type. We
     * need to implement just one of the factory methods.
     */
    class MyFieldFactory implements FieldFactory {

        public Field createField(Class type, Component uiContext) {
            return null;
        }

        public Field createField(Property property, Component uiContext) {
            return null;
        }

        public Field createField(Item item, Object propertyId,
                Component uiContext) {
            String pid = (String) propertyId;

            if (pid.equals("name")) {
                return new TextField("Name");
            }

            if (pid.equals("address")) {
                return new TextField("Street Address");
            }

            if (pid.equals("postalCode")) {
                TextField field = new TextField("Postal Code");
                field.setColumns(5);
                Validator postalCodeValidator = new Validator() {

                    public boolean isValid(Object value) {
                        if (value == null || !(value instanceof String)) {
                            return false;
                        }

                        return ((String) value).matches("[0-9]{5}");
                    }

                    public void validate(Object value)
                            throws InvalidValueException {
                        if (!isValid(value)) {
                            throw new InvalidValueException(
                                    "Postal code must be a number 10000-99999.");
                        }
                    }
                };
                field.addValidator(postalCodeValidator);
                return field;
            }

            if (pid.equals("city")) {
                Select select = new Select("City");
                final String cities[] = new String[] { "Amsterdam", "Berlin",
                        "Helsinki", "Hong Kong", "London", "Luxemburg",
                        "New York", "Oslo", "Paris", "Rome", "Stockholm",
                        "Tokyo", "Turku" };
                for (int i = 0; i < cities.length; i++) {
                    select.addItem(cities[i]);
                }
                return select;
            }
            return null;
        }

        public Field createField(Container container, Object itemId,
                Object propertyId, Component uiContext) {
            return null;
        }
    }

    public FormExample() {
        // Create a form. It will use FormLayout as its layout by default.
        final Form form = new Form();

        // Set form caption and description texts.
        form.setCaption("Contact Information");
        form
                .setDescription("Please enter valid name and address. Fields marked with * are required.");

        // Use custom field factory to create the fields in the form.
        form.setFieldFactory(new MyFieldFactory());

        // Create the custom bean.
        ContactBean bean = new ContactBean();

        // Create a bean item that is bound to the bean.
        BeanItem item = new BeanItem(bean);

        // Bind the bean item as the data source for the form.
        form.setItemDataSource(item);

        // Set the order of the items in the form.
        Vector order = new Vector();
        order.add("name");
        order.add("address");
        order.add("postalCode");
        order.add("city");
        form.setVisibleItemProperties(order);

        // Set required fields. The required error is displayed in
        // the error indication are of the Form if a required
        // field is empty. If it is not set, no error is displayed
        // about an empty required field.
        form.getField("name").setRequired(true);
        form.getField("name").setRequiredError("Name is missing");
        form.getField("address").setRequired(true); // No error message

        // Set the form to act immediately on user input. This is
        // necessary for the validation of the fields to occur immediately when
        // the input focus changes and not just on commit.
        form.setImmediate(true);

        // Set buffering so that commit() must be called for the form
        // before input is written to the data. (Input is not written
        // immediately through).
        form.setWriteThrough(false);
        form.setReadThrough(false);

        // Add Commit and Discard controls to the form.
        ExpandLayout footer = new ExpandLayout(
                OrderedLayout.ORIENTATION_HORIZONTAL);

        // The Commit button calls form.commit().
        Button commit = new Button("Commit", form, "commit");

        // The Discard button calls form.discard().
        Button discard = new Button("Discard", form, "discard");
        footer.addComponent(commit);
        footer.setComponentAlignment(commit, ExpandLayout.ALIGNMENT_RIGHT,
                ExpandLayout.ALIGNMENT_TOP);
        footer.setHeight("25px");
        footer.addComponent(discard);
        form.setFooter(footer);

        OrderedLayout root = new OrderedLayout();
        root.setWidth(400, OrderedLayout.UNITS_PIXELS);
        root.addComponent(form);
        setCompositionRoot(root);
    }
}