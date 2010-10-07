/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.tests;

import java.net.MalformedURLException;
import java.net.URL;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

public class NativeWindowing extends Application {

    Window main = new Window("Windowing test");

    @Override
    public void init() {

        setMainWindow(main);

        main.addComponent(new Button("Add new subwindow",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        final Window w = new Window("sw "
                                + System.currentTimeMillis());
                        main.addWindow(w);
                        w.setPositionX(100);
                        w.setPositionY(100);
                        w.setWidth("200px");
                        w.setHeight("200px");

                        w.setWidth("100px");
                        w.setHeight("400px");

                        final Button closebutton = new Button("Close "
                                + w.getCaption(), new Button.ClickListener() {
                            public void buttonClick(ClickEvent event) {
                                main.removeWindow(w);
                            }

                        });
                        w.addComponent(closebutton);

                        w.addComponent(new Label(
                                "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>"
                                        + "<p>Lorem ipsum dolor sit amet.</p>",
                                Label.CONTENT_XHTML));

                    }
                }));

        main.addComponent(new Button(
                "Open a currently uncreated application level window",
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                        try {
                            main.open(
                                    new com.vaadin.terminal.ExternalResource(
                                            new URL(
                                                    getURL(),
                                                    "mainwin-"
                                                            + System.currentTimeMillis()
                                                            + "/")), null);
                        } catch (final MalformedURLException e) {
                        }
                    }
                }));

        main.addComponent(new Button(
                "Commit (saves window state: size, place, scrollpos)"));
    }

    @Override
    public Window getWindow(String name) {

        final Window w = super.getWindow(name);
        if (w != null) {
            return w;
        }

        if (name != null && name.startsWith("mainwin-")) {
            final String postfix = name.substring("mainwin-".length());
            final Window ww = new Window("Window: " + postfix);
            ww.setName(name);
            ww.addComponent(new Label(
                    "This is a application-level window opened with name: "
                            + name));
            ww.addComponent(new Button("Click me", new Button.ClickListener() {
                int state = 0;

                public void buttonClick(ClickEvent event) {
                    ww.addComponent(new Label("Button clicked " + (++state)
                            + " times"));
                }
            }));
            addWindow(ww);
            return ww;
        }

        return null;
    }

}
