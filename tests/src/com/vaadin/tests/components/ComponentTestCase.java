package com.vaadin.tests.components;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.terminal.UserError;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout.SpacingHandler;

public abstract class ComponentTestCase extends TestBase {

    private List<AbstractComponent> testComponents = new ArrayList<AbstractComponent>();

    @Override
    protected void setup() {
        ((SpacingHandler) getLayout()).setSpacing(true);
        addComponent(createActionLayout());
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

    protected abstract List<Component> createActions();

    private Component createActionLayout() {
        HorizontalLayout actionLayout = new HorizontalLayout();
        actionLayout.setSpacing(true);
        actionLayout.setMargin(true);
        for (Component c : createActions()) {
            actionLayout.addComponent(c);
        }
        addComponent(actionLayout);
        return actionLayout;
    }

    protected void addTestComponent(AbstractComponent c) {
        testComponents.add(c);
        addComponent(c);
    }

    protected List<AbstractComponent> getTestComponents() {
        return testComponents;
    }

    protected void setErrorIndicators(boolean on) {
        for (AbstractComponent c : getTestComponents()) {
            if (c == null) {
                continue;
            }

            if (on) {
                c.setComponentError(new UserError("It failed!"));
            } else {
                c.setComponentError(null);

            }
        }

    }

    protected void setRequired(boolean on) {

        for (AbstractComponent c : getTestComponents()) {
            if (c == null) {
                continue;
            }

            if (c instanceof AbstractField) {
                ((AbstractField) c).setRequired(on);
            }

        }

    }

    protected void setEnabled(boolean on) {
        for (AbstractComponent c : getTestComponents()) {
            if (c == null) {
                continue;
            }

            c.setEnabled(on);
        }

    }

    protected void setReadOnly(boolean on) {
        for (AbstractComponent c : getTestComponents()) {
            if (c == null) {
                continue;
            }

            c.setReadOnly(on);
        }

    }

}