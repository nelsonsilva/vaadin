package com.itmill.toolkit.tests.tickets;

import com.itmill.toolkit.Application;
import com.itmill.toolkit.ui.Button;
import com.itmill.toolkit.ui.OrderedLayout;
import com.itmill.toolkit.ui.TextField;
import com.itmill.toolkit.ui.Window;
import com.itmill.toolkit.ui.Button.ClickEvent;

public class Ticket1939 extends Application {

    public void init() {
        Window w = new Window(getClass().getName());
        setMainWindow(w);

        final OrderedLayout l = new OrderedLayout();
        l.setWidth(400);
        l.setHeight(100);
        l.addComponent(new TextField("This one works fine"));
        TextField t = new TextField();
        t.setRequired(true);
        t.setValue("This one bugs");
        l.addComponent(t);
        w.addComponent(l);

        w.addComponent(new Button("show me the bug",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        l.setWidth(-1);
                    }
                }));

    }

}
