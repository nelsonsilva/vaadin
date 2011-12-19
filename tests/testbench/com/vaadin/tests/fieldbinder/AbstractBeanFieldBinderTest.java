package com.vaadin.tests.fieldbinder;

import com.vaadin.data.fieldbinder.BeanFieldBinder;
import com.vaadin.data.fieldbinder.FieldBinder.CommitException;
import com.vaadin.tests.components.TestBase;
import com.vaadin.tests.util.Log;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Root;

public abstract class AbstractBeanFieldBinderTest extends TestBase {

    private Button commitButton;
    protected Log log = new Log(5);

    private Button discardButton;
    private Button showBeanButton;
    private BeanFieldBinder fieldBinder;

    @Override
    protected void setup() {
        addComponent(log);
    }

    protected Button getDiscardButton() {
        if (discardButton == null) {
            discardButton = new Button("Discard", new Button.ClickListener() {

                public void buttonClick(ClickEvent event) {
                    getFieldBinder().discard();
                    log.log("Discarded changes");

                }
            });
        }
        return discardButton;
    }

    protected Button getShowBeanButton() {
        if (showBeanButton == null) {
            showBeanButton = new Button("Show bean values",
                    new Button.ClickListener() {

                        public void buttonClick(ClickEvent event) {
                            log.log(getFieldBinder().getItemDataSource()
                                    .getBean().toString());

                        }
                    });
        }
        return showBeanButton;
    }

    protected Button getCommitButton() {
        if (commitButton == null) {
            commitButton = new Button("Commit");
            commitButton.addListener(new ClickListener() {

                public void buttonClick(ClickEvent event) {
                    String msg = "Commit succesful";
                    try {
                        getFieldBinder().commit();
                    } catch (CommitException e) {
                        msg = "Commit failed: " + e.getMessage();
                    }
                    Root.getCurrentRoot().showNotification(msg);
                    log.log(msg);

                }
            });
        }
        return commitButton;
    }

    protected BeanFieldBinder getFieldBinder() {
        return fieldBinder;
    }

    protected void setFieldBinder(BeanFieldBinder beanFieldBinder) {
        fieldBinder = beanFieldBinder;
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
