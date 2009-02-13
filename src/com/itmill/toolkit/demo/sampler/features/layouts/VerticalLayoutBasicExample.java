package com.itmill.toolkit.demo.sampler.features.layouts;

import com.itmill.toolkit.ui.TextField;
import com.itmill.toolkit.ui.VerticalLayout;

public class VerticalLayoutBasicExample extends VerticalLayout {

    public VerticalLayoutBasicExample() {
        // this is a VerticalLayout
        // let's add some components
        for (int i = 0; i < 5; i++) {
            TextField tf = new TextField("Row " + (i + 1));
            tf.setWidth("300px");
            // Add the component to the VerticalLayout
            addComponent(tf);
        }
    }
}
