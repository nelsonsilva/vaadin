/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.tests.featurebrowser;

import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.ParameterHandler;
import com.vaadin.terminal.URIHandler;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.OrderedLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;

public class IntroWelcome extends Feature implements URIHandler,
        ParameterHandler {

    Panel panel = new Panel();

    private static final String WELCOME_TEXT_UPPER = ""
            + "This application lets you view and play with some features of "
            + "Vaadin. Use menu on the left to select component."
            + "<br /><br />Note the <b>Properties selection</b> on the top "
            + "right corner. Click it open to access component properties and"
            + " feel free to edit properties at any time."
            + "<br /><br />The area that you are now reading is the component"
            + " demo area. Lower area from here contains component description, API"
            + " documentation and optional code sample. Note that not all selections"
            + " contain demo, only description and API documentation is shown."
            + "<br /><br />You may also change application's theme from below the menu."
            + " This example application is designed to work best with"
            + " <em>Demo</em> theme, other themes are for demonstration purposes only."
            + "<br /><br />Vaadin enables you to construct complex Web"
            + " applications using plain Java, no knowledge of other Web technologies"
            + " such as XML, HTML, DOM, JavaScript or browser differences is required."
            + "<br /><br />For more information, point your browser to"
            + " <a href=\"http://www.vaadin.com\" target=\"_new\">www.vaadin.com</a>.";

    private static final String WELCOME_TEXT_LOWER = ""
            + "This area contains the selected component's description, list of properties, javadoc"
            + " and optional code sample. "
            + "Start your tour now by selecting features from the list"
            + " on the left and remember to experiment with the <b>Properties panel</b>"
            + " located at the top right corner area.";

    // TODO Add browser agent string
    private final String description = WELCOME_TEXT_LOWER
            + "<br /><br />Vaadin version: " + ApplicationServlet.VERSION;

    public IntroWelcome() {
        super();
    }

    @Override
    protected Component getDemoComponent() {

        final OrderedLayout l = new OrderedLayout();

        panel.setCaption("Welcome to the Vaadin feature tour!");
        l.addComponent(panel);

        final Label label = new Label();
        panel.addComponent(label);

        label.setContentMode(Label.CONTENT_XHTML);
        label.setValue(WELCOME_TEXT_UPPER);

        propertyPanel = new PropertyPanel(panel);
        final Form ap = propertyPanel.createBeanPropertySet(new String[] {
                "width", "height" });
        final Select themes = (Select) propertyPanel.getField("style");
        themes.addItem("light").getItemProperty(
                themes.getItemCaptionPropertyId()).setValue("light");
        themes.addItem("strong").getItemProperty(
                themes.getItemCaptionPropertyId()).setValue("strong");
        propertyPanel.addProperties("Panel Properties", ap);

        setJavadocURL("package-summary.html");

        setPropsReminder(false);

        return l;
    }

    @Override
    protected String getExampleSrc() {
        return "" + "package com.vaadin.demo;\n"
                + "import com.vaadin.ui.*;\n\n"
                + "public class HelloWorld extends com.vaadin.Application {\n"
                + "    public void init() {\n"
                + "        Window main = new Window(\"Hello window\");\n"
                + "        setMainWindow(main);\n"
                + "        main.addComponent(new Label(\"Hello World!\"));\n"
                + "    }\n" + "}\n";
    }

    // not ready yet to give description, see paint instead
    @Override
    protected String getDescriptionXHTML() {
        return description;
    }

    @Override
    protected String getImage() {
        return "icon_intro.png";
    }

    @Override
    protected String getTitle() {
        return "Welcome";
    }

    /**
     * Add URI and parametes handlers to window.
     * 
     * @see com.vaadin.ui.Component#attach()
     */
    @Override
    public void attach() {
        super.attach();
        getWindow().addURIHandler(this);
        getWindow().addParameterHandler(this);
    }

    /**
     * Remove all handlers from window
     * 
     * @see com.vaadin.ui.Component#detach()
     */
    @Override
    public void detach() {
        super.detach();
        getWindow().removeURIHandler(this);
        getWindow().removeParameterHandler(this);
    }

    /**
     * Update URI
     * 
     * @see com.vaadin.terminal.URIHandler#handleURI(URL, String)
     */
    public DownloadStream handleURI(URL context, String relativeUri) {
        return null;
    }

    /**
     * Show system status if systemStatus is given on URL
     * 
     * @see com.vaadin.terminal.ParameterHandler#handleParameters(Map)
     */
    public void handleParameters(Map parameters) {
        for (final Iterator i = parameters.keySet().iterator(); i.hasNext();) {
            final String name = (String) i.next();
            if (name.equals("systemStatus")) {
                String status = "";
                status += "timestamp=" + new Date() + " ";
                status += "free=" + Runtime.getRuntime().freeMemory() + ", ";
                status += "total=" + Runtime.getRuntime().totalMemory() + ", ";
                status += "max=" + Runtime.getRuntime().maxMemory() + "\n";
                System.out.println(status);
            }
        }
    }

}