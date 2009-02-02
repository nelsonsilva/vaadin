package com.itmill.toolkit.demo.sampler.features.notifications;

import com.itmill.toolkit.ui.Alignment;
import com.itmill.toolkit.ui.Button;
import com.itmill.toolkit.ui.TextField;
import com.itmill.toolkit.ui.VerticalLayout;
import com.itmill.toolkit.ui.Button.ClickEvent;
import com.itmill.toolkit.ui.Window.Notification;

public class NotificationTrayExample extends VerticalLayout {

    public NotificationTrayExample() {
        setSpacing(true);
        setWidth(null); // layout will grow with content

        final TextField caption = new TextField("Caption", "New message");
        caption.setWidth("200px");
        addComponent(caption);

        final TextField description = new TextField("Description",
                "<b>John:</b> Could you upload Invoices-2008.csv so that...");
        description.setWidth("300px");
        addComponent(description);

        Button show = new Button("Show notification",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        getWindow().showNotification(
                                (String) caption.getValue(),
                                (String) description.getValue(),
                                Notification.TYPE_TRAY_NOTIFICATION);

                    }
                });
        addComponent(show);
        setComponentAlignment(show, Alignment.MIDDLE_RIGHT);

    }
}
