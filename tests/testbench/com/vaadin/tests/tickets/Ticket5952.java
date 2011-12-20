package com.vaadin.tests.tickets;

import com.vaadin.Application;
import com.vaadin.ui.Label;
import com.vaadin.ui.Root.LegacyWindow;

public class Ticket5952 extends Application.LegacyApplication {

    @Override
    public void init() {
        final LegacyWindow mainWindow = new LegacyWindow(
                "Forumtests Application");
        setMainWindow(mainWindow);

        String mathml = "<math mode='display' xmlns='http://www.w3.org/1998/Math/MathML'>"
                + "<mrow>"
                + "    <msup>"
                + "      <mi>x</mi>"
                + "      <mn>2</mn>"
                + "    </msup>"
                + "    <msup>"
                + "      <mi>c</mi>"
                + "      <mn>2</mn>"
                + "    </msup>"
                + "  </mrow>" + "</math>";
        Label mathLabel = new Label(mathml, Label.CONTENT_XML);
        mainWindow.addComponent(mathLabel);
    }
}
