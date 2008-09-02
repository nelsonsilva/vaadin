package com.itmill.toolkit.tests.tickets;

import com.itmill.toolkit.Application;
import com.itmill.toolkit.terminal.ExternalResource;
import com.itmill.toolkit.ui.GridLayout;
import com.itmill.toolkit.ui.Link;
import com.itmill.toolkit.ui.Window;

public class Ticket2043 extends Application {

    public void init() {
        Window w = new Window(getClass().getSimpleName());
        setMainWindow(w);
        // setTheme("tests-tickets");
        GridLayout layout = new GridLayout(10, 10);
        w.setLayout(layout);
        createUI(layout);
    }

    private void createUI(GridLayout layout) {
        Link l = new Link("IT Mill home (new 200x200 window, no decor, icon)",
                new ExternalResource("http://www.itmill.com"), "_blank", 200,
                200, Link.TARGET_BORDER_NONE);

        layout.addComponent(l);
    }
}
