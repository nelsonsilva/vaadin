/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.tests.featurebrowser;

import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;

public abstract class Feature extends CustomComponent {

    private static final String PROP_REMINDER_TEXT = "" + "<br /><br />Note: Use <b>Properties</b> panel located at the top" + " right corner to try out how different properties affect" + " the presentation or functionality of currently selected component.";

    private boolean propsReminder = true;

    private final VerticalLayout layout;

    private TabSheet ts;

    private boolean initialized = false;

    private static Resource sampleIcon;

    protected PropertyPanel propertyPanel;

    private Label javadoc;

    private Label description;

    /** Constuctor for the feature component */
    public Feature() {
        layout = new VerticalLayout();
        setCompositionRoot(layout);
    }

    /**
     * Actual URL consists of "http://www.vaadin.com/api/com/vaadin/"+url
     * 
     * @param url
     */
    public void setJavadocURL(String url) {
        javadoc.setValue("<iframe width=\"100%\" src=\"http://www.vaadin.com/api/com/vaadin/" + url + "\"></iframe>");
    }

    /**
     * Feature component initialization is lazily done when the feature is
     * attached to application
     */
    @Override
    public void attach() {
        super.attach();

        // Check if the feature is already initialized
        if (initialized) {
            return;
        }
        initialized = true;

        // Javadoc
        javadoc = new Label();
        javadoc.setContentMode(Label.CONTENT_XHTML);

        // Demo
        final Component demo = getDemoComponent();
        if (demo != null) {
            layout.addComponent(demo);
        }

        ts = new TabSheet();

        ts.setSizeFull();

        // Description tab
        final String title = getTitle();
        if (getDescriptionXHTML() != null) {
            final VerticalLayout mainLayout = new VerticalLayout();
            final HorizontalLayout layout = new HorizontalLayout();
            mainLayout.addComponent(layout);
            if (getImage() != null) {
                layout.addComponent(new Embedded("", new ClassResource(getImage(), getApplication())));
            }
            String label = "";
            label += getDescriptionXHTML();
            if (propsReminder) {
                label += PROP_REMINDER_TEXT;
            }
            if (title != null) {
                layout.addComponent(new Label("<h3>" + title + "</h3>", Label.CONTENT_XHTML));
            }
            description = new Label(label, Label.CONTENT_XHTML);
            mainLayout.addComponent(description);
            mainLayout.setMargin(true);

            ts.addTab(mainLayout, "Description", null);
        }

        // Properties table tab
        ts.addTab(getPropertyPanel().getAllProperties(), "Properties", null);

        // Javadoc tab
        if (!javadoc.getValue().equals("")) {
            ts.addTab(javadoc, "Javadoc", null);
        }

        // Code Sample tab
        final String example = getExampleSrc();
        if (example != null) {
            final VerticalLayout l = new VerticalLayout();
            if (getTitle() != null) {
                l.addComponent(new Label("<b>// " + getTitle() + " example</b>", Label.CONTENT_XHTML));
            }
            l.addComponent(new Label(example, Label.CONTENT_PREFORMATTED));
            ts.addTab(l, "Code Sample", null);
        }

    }

    /** Get the desctiption of the feature as XHTML fragment */
    protected String getDescriptionXHTML() {
        return "<h2>Feature description is under construction</h2>";
    }

    /** Get the title of the feature */
    protected String getTitle() {
        return getClass().getName();
    }

    public TabSheet getTabSheet() {
        return ts;
    }

    /** Get the name of the image file that will be put on description page */
    protected String getImage() {
        return null;
    }

    /** Get the example application source code */
    protected String getExampleSrc() {
        return null;
    }

    /** Get the feature demo component */
    protected Component getDemoComponent() {
        return null;
    }

    /** Get sample icon resource */
    protected Resource getSampleIcon() {
        if (sampleIcon == null) {
            sampleIcon = new ClassResource("m.gif", getApplication());
        }
        return sampleIcon;
    }

    public PropertyPanel getPropertyPanel() {
        return propertyPanel;
    }

    public void setPropsReminder(boolean propsReminder) {
        this.propsReminder = propsReminder;
    }

    public void updateDescription() {
        String label = "";
        label += getDescriptionXHTML();
        if (propsReminder) {
            label += PROP_REMINDER_TEXT;
        }
        description.setValue(label);
    }

    // Fix for #512
    @Override
    public String getDescription() {
        if (description != null && description.getValue() != null) {
            return description.getValue().toString();
        } else {
            return null;
        }
    }

}