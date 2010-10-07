package com.vaadin.tests.tickets;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Window;

public class Ticket2339 extends Application {

    @Override
    public void init() {

        final Window mainWin = new Window(getClass().getSimpleName());
        setMainWindow(mainWin);

        try {
            CustomLayout cl = new CustomLayout(new ByteArrayInputStream("<div style=\"width:400px;overflow:hidden;background-color:red;\"><div style=\"border:1em solid blue; height:4em; padding:1em 1.5em;\" location=\"b\"></div></div>".getBytes()));
            Button button = new Button("b");
            button.setSizeFull();

            cl.addComponent(button, "b");

            mainWin.addComponent(cl);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
