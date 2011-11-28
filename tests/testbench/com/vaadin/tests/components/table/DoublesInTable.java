package com.vaadin.tests.components.table;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.NumberToStringConverter;
import com.vaadin.tests.components.TestBase;
import com.vaadin.tests.data.bean.Address;
import com.vaadin.tests.data.bean.Country;
import com.vaadin.tests.data.bean.Person;
import com.vaadin.tests.data.bean.Sex;
import com.vaadin.tests.util.Log;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Table;

public class DoublesInTable extends TestBase {
    BeanItemContainer<Person> personBeanItemContainer = new BeanItemContainer<Person>(
            Person.class);

    private Table table;

    private Log log = new Log(5);

    private ComboBox localeSelect;

    private CheckBox useCustomConverters;

    private CheckBox editMode;

    @Override
    protected void setup() {
        editMode = new CheckBox("Edit mode");
        editMode.setImmediate(true);
        editMode.addListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                table.setEditable(editMode.getValue());

            }
        });

        useCustomConverters = new CheckBox("Use custom converters");
        useCustomConverters.setImmediate(true);
        useCustomConverters.addListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                recreateTable();
            }
        });

        localeSelect = createLocaleSelect();
        personBeanItemContainer = createContainer(100);
        addComponent(log);
        addComponent(localeSelect);
        addComponent(useCustomConverters);
        addComponent(editMode);
        recreateTable();

    }

    private ComboBox createLocaleSelect() {
        ComboBox cb = new ComboBox();
        cb.setNullSelectionAllowed(false);
        for (Locale l : Locale.getAvailableLocales()) {
            cb.addItem(l);
        }
        cb.setImmediate(true);
        cb.setValue(Locale.US);
        cb.addListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                recreateTable();
            }
        });
        return cb;
    }

    protected void recreateTable() {
        Table newTable = createTable(useCustomConverters.getValue(),
                (Locale) localeSelect.getValue());
        newTable.setEditable(editMode.getValue());
        if (table == null) {
            addComponent(newTable);
        } else {
            replaceComponent(table, newTable);
        }
        table = newTable;
    }

    private static BeanItemContainer<Person> createContainer(int nr) {
        BeanItemContainer<Person> bic = new BeanItemContainer<Person>(
                Person.class);
        for (int i = 1; i <= nr; i++) {
            Person p = new Person();
            p.setFirstName("First " + i);
            p.setLastName("Last " + i);
            p.setAge(i);
            p.setDeceased((i % 5 - 2) == 0);
            p.setEmail("person" + i + "@mail.com");
            p.setRent(new BigDecimal(i * 1250.25));
            p.setSalary(3000 + i);
            p.setSex((i % 4) == 0 ? Sex.MALE : Sex.FEMALE);
            p.setBirthDate(new Date(2011 - 1900 - p.getAge(), 11 - 1, 24));
            if (i % 42 == 0) {
                p.setSex(Sex.UNKNOWN);
            }
            String city = "City " + (i / 10);
            Country country = Country.FINLAND;
            Address address = new Address("Street " + i, 12345 + i * 2, city,
                    country);
            p.setAddress(address);
            bic.addBean(p);
        }

        return bic;
    }

    protected Table createTable(boolean useCustomConverters, Locale locale) {
        Table t = new Table("Persons");
        t.setLocale(locale);
        t.setContainerDataSource(personBeanItemContainer);
        t.setSortDisabled(false);
        if (useCustomConverters) {
            addConverters(t);
        }
        t.setSelectable(true);
        t.setImmediate(true);
        t.addListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {
                log.log("Value is now: " + event.getProperty().getValue());

            }
        });
        return t;
    }

    private void addConverters(Table t) {
        t.setConverter("sex", new Converter<Sex, String>() {

            public Sex convertFromTargetToSource(String value, Locale locale)
                    throws com.vaadin.data.util.converter.Converter.ConversionException {
                // TODO
                return null;
            }

            public String convertFromSourceToTarget(Sex value, Locale locale)
                    throws com.vaadin.data.util.converter.Converter.ConversionException {
                if (value == null) {
                    value = Sex.UNKNOWN;
                }
                return value.getStringRepresentation();
            }

            public Class<Sex> getSourceType() {
                return Sex.class;
            }

            public Class<String> getTargetType() {
                return String.class;
            }
        });
        t.setConverter("deceased", new Converter<Boolean, String>() {

            public Boolean convertFromTargetToSource(String value, Locale locale) {
                if ("-".equals(value)) {
                    return true;
                } else {
                    return false;
                }
            }

            public String convertFromSourceToTarget(Boolean value, Locale locale) {
                if (value == null || value) {
                    return "YES, DEAD!";
                } else {
                    return "-";
                }
            }

            public Class<Boolean> getSourceType() {
                return Boolean.class;
            }

            public Class<String> getTargetType() {
                return String.class;
            }
        });
        t.setConverter("age", new Converter<Integer, String>() {

            public Integer convertFromTargetToSource(String value, Locale locale)
                    throws com.vaadin.data.util.converter.Converter.ConversionException {
                // TODO Auto-generated method stub
                return null;
            }

            public String convertFromSourceToTarget(Integer value, Locale locale)
                    throws com.vaadin.data.util.converter.Converter.ConversionException {
                if (value == null) {
                    return null;
                }
                if (value < 3) {
                    return value + " (baby)";
                } else if (value < 7) {
                    return value + " (kid)";
                } else if (value < 18) {
                    return value + " (young)";
                } else {
                    return value + "";
                }
            }

            public Class<Integer> getSourceType() {
                return Integer.class;
            }

            public Class<String> getTargetType() {
                return String.class;
            }
        });
        t.setConverter("address", new Converter<Address, String>() {

            public Address convertFromTargetToSource(String value, Locale locale)
                    throws ConversionException {
                // TODO Auto-generated method stub
                return null;
            }

            public String convertFromSourceToTarget(Address value, Locale locale)
                    throws ConversionException {
                return value.getStreetAddress() + ", " + value.getCity() + " ("
                        + value.getCountry() + ")";
            }

            public Class<Address> getSourceType() {
                return Address.class;
            }

            public Class<String> getTargetType() {
                return String.class;
            }

        });

        t.setConverter("rent", new NumberToStringConverter() {
            @Override
            protected NumberFormat getFormatter(Locale locale) {
                return NumberFormat.getCurrencyInstance(locale);
                // DecimalFormat df = new DecimalFormat();
                // df.setDecimalSeparatorAlwaysShown(true);
                // df.setGroupingUsed(true);
                // df.setMinimumFractionDigits(2);
                // df.setMaximumFractionDigits(2);
                // return df;
            }
        });
    }

    @Override
    protected String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Integer getTicketNumber() {
        // TODO Auto-generated method stub
        return null;
    }

}
