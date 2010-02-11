package com.vaadin.tests.util;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class Log extends VerticalLayout {
    List<Label> eventLabels = new ArrayList<Label>();

    public Log(int nr) {
        for (int i = 0; i < nr; i++) {
            Label l = createEventLabel();
            eventLabels.add(l);
            addComponent(l);
        }

        setCaption("Events:");
    }

    public void clear() {
        for (Label l : eventLabels) {
            l.setValue("");
        }
    }

    public void log(String event) {
        int nr = eventLabels.size();
        for (int i = nr - 1; i > 0; i--) {
            eventLabels.get(i).setValue(eventLabels.get(i - 1).getValue());
        }
        eventLabels.get(0).setValue(event);
        System.out.println(event);
    }

    private Label createEventLabel() {
        Label l = new Label("&nbsp;", Label.CONTENT_XHTML);
        l.setWidth(null);
        return l;
    }

}