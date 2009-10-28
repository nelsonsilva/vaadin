/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.tests.featurebrowser;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.Link;
import com.vaadin.ui.OrderedLayout;

public class FeatureLink extends Feature {

    public FeatureLink() {
        super();
    }

    @Override
    protected Component getDemoComponent() {

        final OrderedLayout l = new OrderedLayout();

        final Link lnk = new Link("Link caption", new ExternalResource(
                "http://www.vaadin.com"));
        l.addComponent(lnk);

        // Properties
        propertyPanel = new PropertyPanel(lnk);
        final Form ap = propertyPanel.createBeanPropertySet(new String[] {
                "targetName", "targetWidth", "targetHeight", "targetBorder" });
        ap.replaceWithSelect("targetBorder", new Object[] {
                new Integer(Link.TARGET_BORDER_DEFAULT),
                new Integer(Link.TARGET_BORDER_MINIMAL),
                new Integer(Link.TARGET_BORDER_NONE) }, new Object[] {
                "Default", "Minimal", "None" });
        propertyPanel.addProperties("Link Properties", ap);

        setJavadocURL("ui/Link.html");

        return l;
    }

    @Override
    protected String getExampleSrc() {
        return "Link link = new Link(\"Link caption\",new ExternalResource(\"http://www.vaadin.com\"));\n";
    }

    @Override
    protected String getDescriptionXHTML() {
        return "The link feature allows for making refences to both internal and external resources. "
                + "The link can open the new resource in a new window, allowing for control of the newly "
                + "opened windows attributes, such as size and border. "
                + "<br /><br />"
                + " For example you can create an application pop-up or create link to external resources.";

    }

    @Override
    protected String getImage() {
        return "icon_demo.png";
    }

    @Override
    protected String getTitle() {
        return "Link";
    }
}