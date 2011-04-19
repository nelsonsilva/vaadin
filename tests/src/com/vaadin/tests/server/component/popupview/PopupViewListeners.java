package com.vaadin.tests.server.component.popupview;

import com.vaadin.tests.server.component.ListenerMethods;
import com.vaadin.ui.Label;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.PopupView.PopupVisibilityEvent;
import com.vaadin.ui.PopupView.PopupVisibilityListener;

public class PopupViewListeners extends ListenerMethods {
    public void testPopupVisibilityListenerAddGetRemove() throws Exception {
        testListenerAddGetRemove(PopupView.class, PopupVisibilityEvent.class,
                PopupVisibilityListener.class, new PopupView("", new Label()));
    }
}
