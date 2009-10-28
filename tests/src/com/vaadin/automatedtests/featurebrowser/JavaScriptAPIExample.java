/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.automatedtests.featurebrowser;

import java.util.Date;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

/**
 * An example using a RichTextArea to edit a Label in XHTML-mode.
 * 
 */
public class JavaScriptAPIExample extends CustomComponent {

    public static final String txt = "<p>For advanced client side programmers Vaadin offers a simple method which can be used to force sync client with server. This may be needed for example if another part of a mashup changes things on server.</p> (more examples will be added here as the APIs are made public)<br/><br/><A href=\"javascript:vaadin.forceSync();\">javascript:vaadin.forceSync();</A>";

    private final VerticalLayout main;
    private final Label l;
    private final TextField editor = new TextField();

    public JavaScriptAPIExample() {
        // main layout
        main = new VerticalLayout();
        main.setMargin(true);
        setCompositionRoot(main);
        editor.setRows(7);
        editor.setColumns(50);
        // Add the label
        l = new Label(txt);
        l.setContentMode(Label.CONTENT_XHTML);
        main.addComponent(l);
        // Edit button with inline click-listener
        Button b = new Button("Edit", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                // swap Label <-> RichTextArea
                if (main.getComponentIterator().next() == l) {
                    editor.setValue(l.getValue());
                    main.replaceComponent(l, editor);
                    event.getButton().setCaption("Save");
                } else {
                    l.setValue(editor.getValue());
                    main.replaceComponent(editor, l);
                    event.getButton().setCaption("Edit");
                }
            }
        });
        main.addComponent(b);
        main.setComponentAlignment(b, Alignment.MIDDLE_RIGHT);

        // 
        Label l = new Label(
                "This label will update it's server-side value AFTER it's rendered to the client-side. "
                        + "The client will be synchronized on reload, when you click a button, "
                        + "or when vaadin.forceSync() is called.") {

            @Override
            public void paintContent(PaintTarget target) throws PaintException {

                super.paintContent(target);
                Delay d = new Delay(this);
                d.start();
            }

        };
        main.addComponent(l);

    }

    private class Delay extends Thread {
        Label label;

        public Delay(Label l) {
            label = l;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(500);
                label.setValue(new Date().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}