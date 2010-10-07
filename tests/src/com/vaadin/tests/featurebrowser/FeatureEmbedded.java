/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.tests.featurebrowser;

import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Form;
import com.vaadin.ui.VerticalLayout;

public class FeatureEmbedded extends Feature {

    public FeatureEmbedded() {
        super();
    }

    @Override
    protected Component getDemoComponent() {

        final VerticalLayout l = new VerticalLayout();

        final ClassResource flashResource = new ClassResource(
                "vaadin_spin.swf", getApplication());
        final Embedded emb = new Embedded("Embedded Caption", flashResource);
        emb.setType(Embedded.TYPE_OBJECT);
        emb.setMimeType("application/x-shockwave-flash");
        emb.setWidth("250px");
        emb.setHeight("100px");
        l.addComponent(emb);

        // Properties
        propertyPanel = new PropertyPanel(emb);
        final Form ap = propertyPanel.createBeanPropertySet(new String[] {
                "type", "source", "width", "height", "widthUnits",
                "heightUnits", "codebase", "codetype", "archive", "mimeType",
                "standby", "classId" });
        ap.replaceWithSelect("type", new Object[] {
                new Integer(Embedded.TYPE_IMAGE),
                new Integer(Embedded.TYPE_OBJECT) }, new Object[] { "Image",
                "Object" });
        final Object[] units = new Object[Embedded.UNIT_SYMBOLS.length];
        final Object[] symbols = new Object[Embedded.UNIT_SYMBOLS.length];
        for (int i = 0; i < units.length; i++) {
            units[i] = new Integer(i);
            symbols[i] = Embedded.UNIT_SYMBOLS[i];
        }
        ap.replaceWithSelect("heightUnits", units, symbols);
        ap.replaceWithSelect("widthUnits", units, symbols);
        ap.replaceWithSelect("source", new Object[] { flashResource },
                new Object[] { "vaadin_spin.swf" });
        propertyPanel.addProperties("Embedded Properties", ap);
        propertyPanel.getField("standby").setDescription(
                "The text to display while loading the object.");
        propertyPanel.getField("codebase").setDescription(
                "root-path used to access resources with relative paths.");
        propertyPanel.getField("codetype").setDescription(
                "MIME-type of the code.");
        propertyPanel
                .getField("classId")
                .setDescription(
                        "Unique object id. This can be used for example to identify windows components.");

        setJavadocURL("ui/Embedded.html");

        return l;
    }

    @Override
    protected String getExampleSrc() {
        return "// Load image from jpg-file, that is in the same package with the application\n"
                + "Embedded e = new Embedded(\"Image title\",\n"
                + "   new ClassResource(\"image.jpg\", getApplication()));";
    }

    @Override
    protected String getDescriptionXHTML() {
        return "The embedding feature allows for adding images, multimedia and other non-specified "
                + "content to your application. "
                + "The feature has provisions for embedding both applets and Active X controls. "
                + "Actual support for embedded media types is left to the terminal.";
    }

    @Override
    protected String getImage() {
        return "icon_demo.png";
    }

    @Override
    protected String getTitle() {
        return "Embedded";
    }

}
