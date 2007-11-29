/**
 * 
 */
package com.itmill.toolkit.demo.featurebrowser;

import com.itmill.toolkit.ui.Button;
import com.itmill.toolkit.ui.CustomComponent;
import com.itmill.toolkit.ui.Label;
import com.itmill.toolkit.ui.OrderedLayout;
import com.itmill.toolkit.ui.RichTextArea;
import com.itmill.toolkit.ui.Button.ClickEvent;

/**
 * @author marc
 * 
 */
public class RichTextExample extends CustomComponent {

    public static final String txt = "<h1>RichText editor example</h1>"
            + "To edit <i>this text</i>, press the <b>Edit</b> button below."
            + "<br/>"
            + "See the <A href=\"http://www.itmill.com/manual/\">maual</a> "
            + "for more information.";

    private OrderedLayout main;
    private Label l;
    private RichTextArea editor;
    private Button b;

    public RichTextExample() {
        main = new OrderedLayout();
        main.setMargin(true);
        setCompositionRoot(main);

        l = new Label("asd");
        l.setContentMode(Label.CONTENT_XHTML);
        main.addComponent(l);

        editor = new RichTextArea();

        b = new Button("Edit", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                if (main.getComponentIterator().next() == l) {
                    editor.setValue(l.getValue());
                    main.replaceComponent(l, editor);
                    b.setCaption("Save");
                } else {
                    l.setValue(editor.getValue());
                    main.replaceComponent(editor, l);
                    b.setCaption("Edit");
                }
            }
        });
        main.addComponent(b);
    }

}
