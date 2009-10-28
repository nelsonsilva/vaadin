package com.vaadin.tests.tickets;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class Ticket2209 extends Application {

    private GridLayout gl;
    private ComboBox combo;
    private Label labelLong;

    @Override
    public void init() {
        setMainWindow(new Window());

        gl = new GridLayout(1, 2);
        gl.setStyleName("borders");
        getMainWindow().addComponent(gl);
        setTheme("tests-tickets");
        combo = new ComboBox("Combo caption");
        labelLong = new Label(
                "A long label, longer than the combo box. Why doesn't it affect the width? And why is the gridlayout so high?");
        gl.addComponent(combo);
        gl.addComponent(labelLong);

        Button b = new Button("Add label text", new ClickListener() {

            public void buttonClick(ClickEvent event) {
                labelLong.setValue(labelLong.getValue() + "-12345");
            }

        });
        getMainWindow().addComponent(b);
    }

}