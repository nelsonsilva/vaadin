/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.automatedtests.featurebrowser;

import java.net.MalformedURLException;
import java.net.URL;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Select;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;

/**
 * Demonstrates the use of Embedded and "suggesting" Select by creating a simple
 * web-browser. Note: does not check for recursion.
 * 
 * @author IT Mill Ltd.
 * @see com.vaadin.ui.Window
 */
@SuppressWarnings("serial")
public class EmbeddedBrowserExample extends VerticalLayout implements
        Select.ValueChangeListener {

    // Default URL to open.
    private static final String DEFAULT_URL = "http://www.vaadin.com/";

    // The embedded page
    Embedded emb = new Embedded();

    public EmbeddedBrowserExample() {
        this(new String[] { DEFAULT_URL, "http://www.vaadin.com/learn",
                "http://www.vaadin.com/api", "http://www.vaadin.com/book" });
    }

    public EmbeddedBrowserExample(String[] urls) {
        setSizeFull();

        // create the address combobox
        final Select select = new Select();
        // allow input
        select.setNewItemsAllowed(true);
        // no empty selection
        select.setNullSelectionAllowed(false);
        // no 'go' -button clicking necessary
        select.setImmediate(true);
        // add some pre-configured URLs
        for (int i = 0; i < urls.length; i++) {
            select.addItem(urls[i]);
        }
        // add to layout
        addComponent(select);
        // add listener and select initial URL
        select.addListener(this);
        select.setValue(urls[0]);

        select.setWidth("100%");

        // configure the embedded and add to layout
        emb.setType(Embedded.TYPE_BROWSER);
        emb.setSizeFull();
        addComponent(emb);
        // make the embedded as large as possible
        setExpandRatio(emb, 1);

    }

    public void valueChange(ValueChangeEvent event) {
        final String url = (String) event.getProperty().getValue();
        if (url != null) {
            try {
                // the selected url has changed, let's go there
                @SuppressWarnings("unused")
                URL u = new URL(url);
                emb.setSource(new ExternalResource(url));

            } catch (MalformedURLException e) {
                getWindow().showNotification("Invalid address",
                        e.getMessage() + " (example: http://www.vaadin.com)",
                        Notification.TYPE_WARNING_MESSAGE);
            }

        }

    }
}