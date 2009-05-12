package com.vaadin.demo.sampler.features.notifications;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window.Notification;

public class NotificationWarningExample extends VerticalLayout {

    public NotificationWarningExample() {
        setSpacing(true);
        setWidth(null); // layout will grow with content

        final TextField caption = new TextField("Caption", "Upload canceled");
        caption.setWidth("200px");
        addComponent(caption);

        final TextField description = new TextField("Description",
                "Invoices-2008.csv will not be processed");
        description.setWidth("300px");
        addComponent(description);

        Button show = new Button("Show notification",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        getWindow().showNotification(
                                (String) caption.getValue(),
                                (String) description.getValue(),
                                Notification.TYPE_WARNING_MESSAGE);

                    }
                });
        addComponent(show);
        setComponentAlignment(show, Alignment.MIDDLE_RIGHT);

    }
}