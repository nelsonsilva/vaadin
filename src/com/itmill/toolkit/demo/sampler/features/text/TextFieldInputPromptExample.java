package com.itmill.toolkit.demo.sampler.features.text;

import com.itmill.toolkit.data.Property;
import com.itmill.toolkit.data.Property.ValueChangeEvent;
import com.itmill.toolkit.ui.TextField;
import com.itmill.toolkit.ui.VerticalLayout;

public class TextFieldInputPromptExample extends VerticalLayout implements
        Property.ValueChangeListener {

    public TextFieldInputPromptExample() {
        // add som 'air' to the layout
        setSpacing(true);
        setMargin(true);

        // Username field + input prompt
        TextField username = new TextField();
        username.setInputPrompt("Username");
        // configure & add to layout
        username.setImmediate(true);
        username.addListener(this);
        addComponent(username);

        // Password field + input prompt
        TextField password = new TextField();
        password.setInputPrompt("Password");
        // configure & add to layout
        password.setSecret(true);
        password.setImmediate(true);
        password.addListener(this);
        addComponent(password);

        // Comment field + input prompt
        TextField comment = new TextField();
        comment.setInputPrompt("Comment");
        // configure & add to layout
        comment.setRows(3);
        comment.setImmediate(true);
        comment.addListener(this);
        addComponent(comment);

    }

    public void valueChange(ValueChangeEvent event) {
        getWindow().showNotification("Received " + event.getProperty());

    }

}
