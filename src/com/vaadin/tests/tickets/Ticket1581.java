package com.vaadin.tests.tickets;

import java.util.Date;

import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;

public class Ticket1581 extends com.vaadin.Application {

    private Label time;
    private ProgressIndicator poller;
    private Thread thread;

    @Override
    public void init() {
        final Window main = new Window(getClass().getName().substring(
                getClass().getName().lastIndexOf(".") + 1));
        setMainWindow(main);

        main.addComponent(new Label("Test the second issue in ticket #1581"));

        time = new Label();
        poller = new ProgressIndicator();
        poller.setPollingInterval(200);
        main.addComponent(time);
        main.addComponent(poller);

        thread = new Thread() {

            @Override
            public void run() {
                super.run();
                while (true) {
                    time.setValue(new Date());
                    try {
                        sleep(200);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        };

        thread.start();

        final Button stop = new Button("Stop updating", new ClickListener() {
            boolean active = true;

            public void buttonClick(ClickEvent event) {

                if (active) {
                    main.removeComponent(poller);
                    event.getButton().setCaption("Resume");
                } else {
                    main.addComponent(poller);
                    event.getButton().setCaption("Stop updating");
                }
                active = !active;
            }
        });

        main.addComponent(stop);
    }

}