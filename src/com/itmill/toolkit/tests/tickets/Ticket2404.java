package com.itmill.toolkit.tests.tickets;

import com.itmill.toolkit.Application;
import com.itmill.toolkit.ui.Button;
import com.itmill.toolkit.ui.GridLayout;
import com.itmill.toolkit.ui.Window;

public class Ticket2404 extends Application {

    @Override
    public void init() {

        GridLayout gl = new GridLayout(2, 2);
        gl.setSizeFull();

        Button bb = new Button("1st row on 2x2 GridLayout");
        bb.setSizeFull();
        gl.addComponent(bb, 0, 0, 1, 0);
        for (int i = 0; i < 2; i++) {
            Button b = new Button("" + i);
            gl.addComponent(b);
            b.setSizeFull();
        }

        setMainWindow(new Window("GridLayout test", gl));

    }
}
