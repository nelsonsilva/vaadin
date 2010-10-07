/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.tests.featurebrowser;

import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;
import com.vaadin.ui.VerticalLayout;

public class FeatureValidators extends Feature {

    private static final String INTRO_TEXT = ""
            + "Vaadin contains simple, yet powerful validation interface, "
            + "that consists of two parts: Validator and Validatable. Validator is "
            + "any class that can check validity of an Object. Validatable is "
            + "a class with configurable validation. "
            + "Validation errors are passed as special exceptions that implement "
            + "ErrorMessage interface. This way the validation errors can be "
            + "automatically added to components."
            + "<br /><br />Utilities for simple string and null validation are provided, as "
            + "well as combinative validators. The validation interface can also "
            + "be easily implemented by the applications for more complex "
            + "validation needs.";

    public FeatureValidators() {
        super();
    }

    @Override
    protected Component getDemoComponent() {

        final VerticalLayout l = new VerticalLayout();

        final Panel panel = new Panel();
        panel.setCaption("Validators");
        l.addComponent(panel);

        final Label label = new Label();
        panel.addComponent(label);

        label.setContentMode(Label.CONTENT_XHTML);
        label.setValue(INTRO_TEXT);

        // Properties
        propertyPanel = new PropertyPanel(panel);
        final Form ap = propertyPanel.createBeanPropertySet(new String[] {
                "width", "height" });
        final Select themes = (Select) propertyPanel.getField("style");
        themes.addItem("light")
                .getItemProperty(themes.getItemCaptionPropertyId())
                .setValue("light");
        themes.addItem("strong")
                .getItemProperty(themes.getItemCaptionPropertyId())
                .setValue("strong");
        propertyPanel.addProperties("Panel Properties", ap);

        setJavadocURL("data/Validator.html");

        return l;
    }

    @Override
    protected String getExampleSrc() {
        return null;
    }

    /**
     * @see com.vaadin.tests.featurebrowser.Feature#getDescriptionXHTML()
     */
    @Override
    protected String getDescriptionXHTML() {
        return null;
    }

    @Override
    protected String getImage() {
        return null;
    }

    @Override
    protected String getTitle() {
        return null;
    }

}
