package com.vaadin.tests.fieldbinder;

import com.vaadin.data.fieldbinder.BeanFieldBinder;
import com.vaadin.data.fieldbinder.FieldBinder;
import com.vaadin.data.fieldbinder.FieldBinder.CommitException;
import com.vaadin.data.fieldbinder.FormBuilder;
import com.vaadin.data.util.BeanItem;
import com.vaadin.tests.components.TestBase;
import com.vaadin.tests.data.bean.Address;
import com.vaadin.tests.data.bean.Country;
import com.vaadin.tests.data.bean.Person;
import com.vaadin.tests.data.bean.PersonWithBeanValidationAnnotations;
import com.vaadin.tests.data.bean.Sex;
import com.vaadin.tests.util.Log;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Root;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

public class FieldBinderWithBeanValidation extends TestBase {

    private Log log = new Log(5);
    private TextField firstName;
    private TextArea lastName;
    private TextField email;
    private TextField age;
    private Table sex;
    private TextField deceased;

    @Override
    protected void setup() {
        addComponent(log);

        final BeanFieldBinder<PersonWithBeanValidationAnnotations> binder = new BeanFieldBinder<PersonWithBeanValidationAnnotations>(
                PersonWithBeanValidationAnnotations.class);

        FormBuilder builder = new FormBuilder(binder);
        builder.buildAndBindFields(this);
        addComponent(firstName);
        addComponent(lastName);
        addComponent(email);
        addComponent(age);
        addComponent(sex);
        addComponent(deceased);

        Button commitButton = new Button("Commit", new Button.ClickListener() {

            public void buttonClick(ClickEvent event) {
                String msg = "Commit succesful";
                try {
                    binder.commit();
                } catch (CommitException e) {
                    msg = "Commit failed: " + e.getMessage();
                }
                Root.getCurrentRoot().showNotification(msg);
                log.log(msg);

            }
        });
        Button discardButton = new Button("Discard",
                new Button.ClickListener() {

                    public void buttonClick(ClickEvent event) {
                        binder.discard();
                        log.log("Discarded changes");

                    }
                });
        Button showBean = new Button("Show bean values",
                new Button.ClickListener() {

                    public void buttonClick(ClickEvent event) {
                        log.log(getPerson(binder).toString());

                    }
                });
        addComponent(commitButton);
        addComponent(discardButton);
        addComponent(showBean);
        sex.setPageLength(0);

        PersonWithBeanValidationAnnotations p = new PersonWithBeanValidationAnnotations(
                "John", "Doe", "john@doe.com", 64, Sex.MALE, new Address(
                        "John street", 11223, "John's town", Country.USA));
        binder.setItemDataSource(new BeanItem<PersonWithBeanValidationAnnotations>(
                p));
    }

    public static Person getPerson(FieldBinder binder) {
        return ((BeanItem<Person>) binder.getItemDataSource()).getBean();
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
