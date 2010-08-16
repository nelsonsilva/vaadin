package com.vaadin.tests.components.window;

import com.vaadin.terminal.Sizeable;
import com.vaadin.tests.components.TestBase;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.ResizeEvent;
import com.vaadin.ui.Window.ResizeListener;

public class WindowResizeListener extends TestBase {

    @Override
    protected String getDescription() {
        return "Size changes from windows (both sub "
                + "and browsers level) should get back to server."
                + " If size changes, a separate server side event should occur.";
    }

    @Override
    protected Integer getTicketNumber() {
        return null;
    }

    Window subwin = new ResizeListenerWindow();

    @Override
    protected void setup() {

        final Label l = new Label();
        getLayout().addComponent(l);

        getMainWindow().addListener(new ResizeListener() {
            public void windowResized(ResizeEvent e) {
                l.setValue("Current main window size: "
                        + getMainWindow().getWidth() + " x "
                        + getMainWindow().getHeight());
            }
        });

        CheckBox subwindow = new CheckBox("show subwindow");
        subwindow.setImmediate(true);
        subwindow.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                if (event.getButton().booleanValue()) {
                    getMainWindow().addWindow(subwin);
                } else {
                    getMainWindow().removeWindow(subwin);
                }
            }
        });
        getLayout().addComponent(subwindow);

        CheckBox immediate = new CheckBox("immediate");
        immediate.addListener(new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                boolean booleanValue = event.getButton().booleanValue();
                getMainWindow().setImmediate(booleanValue);
                subwin.setImmediate(booleanValue);
            }
        });
        immediate.setImmediate(true);
        immediate.setValue(true);
        getMainWindow().setImmediate(true);
        subwin.setImmediate(true);
        getLayout().addComponent(immediate);

        getLayout().addComponent(new Button("Sync"));

    }
}

class ResizeListenerWindow extends Window {
    Label sizeLabel = new Label();

    public ResizeListenerWindow() {
        super("Subwindow");
        setWidth("400px");

        Layout hl = getLayout();
        hl.addComponent(new Label("Current size: "));
        hl.addComponent(sizeLabel);

        addListener(new ResizeListener() {
            public void windowResized(ResizeEvent e) {
                updateLabel();
            }
        });

        updateLabel();
    }

    public void updateLabel() {
        sizeLabel
                .setValue(getWidth() + Sizeable.UNIT_SYMBOLS[getWidthUnits()]
                        + " x " + getHeight()
                        + Sizeable.UNIT_SYMBOLS[getHeightUnits()]);
    }
}
