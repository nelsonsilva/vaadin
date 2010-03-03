package com.vaadin.ui.themes;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public class Reindeer extends BaseTheme {

    public static final String THEME_NAME = "Reindeer";

    /***************************************************************************
     * 
     * Label styles
     * 
     **************************************************************************/

    /**
     * Large font for main application headings
     */
    public static final String LABEL_H1 = "h1";

    /**
     * Large font for different sections in the application
     */
    public static final String LABEL_H2 = "h2";

    /**
     * Small and a little lighter font
     */
    public static final String LABEL_SMALL = "light";

    /***************************************************************************
     * 
     * Button styles
     * 
     **************************************************************************/

    /**
     * Default action style for buttons (the button that gets activated when
     * user presses 'enter' in a form). Use sparingly, only one default button
     * per screen should be visible.
     */
    public static final String BUTTON_DEFAULT = "primary";

    /**
     * Small sized button, use for context specific actions for example
     */
    public static final String BUTTON_SMALL = "small";

    /***************************************************************************
     * 
     * TextField styles
     * 
     **************************************************************************/

    /**
     * Small sized text field with small font
     */
    public static final String TEXTFIELD_SMALL = "small";

    /***************************************************************************
     * 
     * Panel styles
     * 
     **************************************************************************/

    /**
     * Removes borders and background color from the panel
     */
    public static final String PANEL_LIGHT = "light";

    /***************************************************************************
     * 
     * SplitPanel styles
     * 
     **************************************************************************/

    /**
     * Reduces the split handle to a minimal size (1 pixel)
     */
    public static final String SPLITPANEL_SMALL = "small";

    /***************************************************************************
     * 
     * TabSheet styles
     * 
     **************************************************************************/

    /**
     * Removes borders and background color from the tab sheet, and shows the
     * tabs as a small bar.
     */
    public static final String TABSHEET_BAR = "bar";

    /**
     * Removes borders and background color from the tab sheet. The tabs are
     * presented with minimal lines indicating the selected tab.
     */
    public static final String TABSHEET_MINIMAL = "minimal";

    /***************************************************************************
     * 
     * Table styles
     * 
     **************************************************************************/

    /**
     * Removes borders from the table
     */
    public static final String TABLE_BORDERLESS = "borderless";

    /**
     * Makes the table headers dark and more prominent.
     */
    public static final String TABLE_STRONG = "strong";

    /***************************************************************************
     * 
     * Layout styles
     * 
     **************************************************************************/

    /**
     * Changes the background of a layout to a shade of blue. Applies to
     * {@link VerticalLayout}, {@link HorizontalLayout}, {@link GridLayout},
     * {@link FormLayout} and {@link CssLayout}.
     */
    public static final String LAYOUT_BLUE = "blue";

    /**
     * <p>
     * Changes the background of a layout to almost black, and at the same time
     * transforms contained components to their black style correspondents when
     * available. At least texts, buttons, text fields, selects, date fields,
     * tables and a few other component styles should change.
     * </p>
     * <p>
     * Applies to {@link VerticalLayout}, {@link HorizontalLayout},
     * {@link GridLayout}, {@link FormLayout} and {@link CssLayout}.
     * </p>
     * 
     */
    public static final String LAYOUT_BLACK = "black";

    /***************************************************************************
     * 
     * Window styles
     * 
     **************************************************************************/

    /**
     * Makes the whole window white and increases the font size of the title.
     */
    public static final String WINDOW_LIGHT = "light";

    /**
     * Makes the whole window black, and changes contained components in the
     * same way as {@link #LAYOUT_BLACK} does.
     */
    public static final String WINDOW_BLACK = "black";
}