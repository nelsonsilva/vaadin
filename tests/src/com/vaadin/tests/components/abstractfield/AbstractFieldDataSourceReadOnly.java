package com.vaadin.tests.components.abstractfield;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.tests.components.TestBase;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Component.Event;
import com.vaadin.ui.Component.Listener;

public class AbstractFieldDataSourceReadOnly extends TestBase {

    private static class StateHolder {
        private ObjectProperty textField = new ObjectProperty("");

        public ObjectProperty getTextField() {
            return textField;
        }

        public void setTextField(ObjectProperty textField) {
            this.textField = textField;
        }

        public void buttonClicked() {
            textField.setReadOnly(true);
        }
    }

    @Override
    protected void setup() {
        final StateHolder stateHolder = new StateHolder();

        // Button
        Button button = new Button("Make data source read-only");
        button.addListener(new Listener() {
            public void componentEvent(Event event) {
                stateHolder.buttonClicked();
            }
        });

        // Input field
        TextField input = new TextField("Field");
        input.setPropertyDataSource(stateHolder.getTextField());

        addComponent(button);
        addComponent(input);
    }

    @Override
    protected String getDescription() {
        return "Read-only status changes in data sources are not rendered immediately";
    }

    @Override
    protected Integer getTicketNumber() {
        return 5013;
    }

}
