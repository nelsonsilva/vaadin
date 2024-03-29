/* 
@VaadinApache2LicenseForJavaFiles@
 */

package com.vaadin.tests;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Root.LegacyWindow;

public class TestSetVisibleAndCaching extends
        com.vaadin.Application.LegacyApplication {

    Panel panelA = new Panel("Panel A");
    Panel panelB = new Panel("Panel B");
    Panel panelC = new Panel("Panel C");

    Button buttonNextPanel = new Button("Show next panel");

    int selectedPanel = 0;

    @Override
    public void init() {
        final LegacyWindow mainWindow = new LegacyWindow(
                "TestSetVisibleAndCaching");
        setMainWindow(mainWindow);

        panelA.addComponent(new Label(
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        panelB.addComponent(new Label(
                "BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB"));
        panelC.addComponent(new Label(
                "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC"));

        mainWindow
                .addComponent(new Label(
                        "Inspect transfered data from server to "
                                + "client using firebug (http request / response cycles)."
                                + " See how widgets are re-used,"
                                + " after each panel is once shown in GUI then"
                                + " their contents are not resend."));
        mainWindow.addComponent(buttonNextPanel);
        mainWindow.addComponent(panelA);
        mainWindow.addComponent(panelB);
        mainWindow.addComponent(panelC);

        selectPanel(selectedPanel);

        buttonNextPanel.addListener(new ClickListener() {
            public void buttonClick(ClickEvent event) {
                selectedPanel++;
                if (selectedPanel > 2) {
                    selectedPanel = 0;
                }
                selectPanel(selectedPanel);
            }
        });

    }

    private void selectPanel(int selectedPanel) {
        System.err.println("Selecting panel " + selectedPanel);
        switch (selectedPanel) {
        case 0:
            panelA.setVisible(true);
            panelB.setVisible(false);
            panelC.setVisible(false);
            break;
        case 1:
            panelA.setVisible(false);
            panelB.setVisible(true);
            panelC.setVisible(false);
            break;
        case 2:
            panelA.setVisible(false);
            panelB.setVisible(false);
            panelC.setVisible(true);
            break;
        }
    }
}
