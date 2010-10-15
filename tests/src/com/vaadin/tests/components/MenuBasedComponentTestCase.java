package com.vaadin.tests.components;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.tests.util.LoremIpsum;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

//TODO swap the inheritance order so AbstractComponentTestCase refers to AbstractComponent and this is the base class. Can only be done when all old tests are converted to use this.
public abstract class MenuBasedComponentTestCase<T extends AbstractComponent>
        extends AbstractComponentTestCase<T> {

    protected static final String TEXT_SHORT = "Short";
    protected static final String TEXT_MEDIUM = "This is a semi-long text that might wrap.";
    protected static final String TEXT_LONG = "This is a long text. "
            + LoremIpsum.get(500);
    protected static final String TEXT_VERY_LONG = "This is a very, very long text. "
            + LoremIpsum.get(5000);

    private static final Resource SELECTED_ICON = new ThemeResource(
            "../runo/icons/16/ok.png");

    private MenuItem mainMenu;

    private MenuBar menu;

    private T component;

    // Used to determine if a menuItem should be selected and the other
    // unselected on click
    private Set<MenuItem> parentOfSelectableMenuItem = new HashSet<MenuItem>();
    private MenuItem windowMenu;

    protected static final String CATEGORY_STATE = "State";
    protected static final String CATEGORY_SIZE = "Size";
    protected static final String CATEGORY_SELECTION = "Selection";
    protected static final String CATEGORY_LISTENERS = "Listeners";
    protected static final String CATEGORY_FEATURES = "Features";
    protected static final String CATEGORY_DECORATIONS = "Decorations";

    @Override
    protected final void setup() {
        // Create menu here so it appears before the components
        menu = new MenuBar();
        mainMenu = menu.addItem("Component", null);
        windowMenu = menu.addItem("Test", null);
        addComponent(menu);

        getLayout().setSizeFull();
        enableLog();
        super.setup();

        // Create menu actions and trigger default actions
        createActions();

        // Clear initialization log messages
        clearLog();
    }

    /**
     * By default initializes just one instance of {@link #getTestClass()} using
     * {@link #constructComponent()}.
     */
    @Override
    protected void initializeComponents() {
        component = constructComponent();
        addTestComponent(component);
    }

    public T getComponent() {
        return component;
    }

    @Override
    protected void addTestComponent(T c) {
        super.addTestComponent(c);
        getLayout().setExpandRatio(c, 1);

    };

    /**
     * Construct the component that is to be tested. This method uses a no-arg
     * constructor by default. Override to customize.
     * 
     * @return Instance of the component that is to be tested.
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    protected T constructComponent() {
        try {
            return getTestClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate "
                    + getTestClass(), e);
        }
    }

    /**
     * Create actions for the component. Remember to call super.createActions()
     * when overriding.
     */
    protected void createActions() {
        createBooleanAction("Immediate", CATEGORY_STATE, true, immediateCommand);
        createBooleanAction("Enabled", CATEGORY_STATE, true, enabledCommand);
        createBooleanAction("Readonly", CATEGORY_STATE, false, readonlyCommand);
        createBooleanAction("Visible", CATEGORY_STATE, true, visibleCommand);
        createBooleanAction("Error indicator", CATEGORY_STATE, false,
                errorIndicatorCommand);
        createErrorMessageSelect(CATEGORY_DECORATIONS);

        createDescriptionSelect(CATEGORY_DECORATIONS);
        createCaptionSelect(CATEGORY_DECORATIONS);
        createIconSelect(CATEGORY_DECORATIONS);

        createWidthSelect(CATEGORY_SIZE);
        createHeightSelect(CATEGORY_SIZE);

        // TODO Style name

    }

    private void createErrorMessageSelect(String category) {
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        options.put("-", null);
        options.put(TEXT_SHORT, TEXT_SHORT);
        options.put("Medium", TEXT_MEDIUM);
        options.put("Long", TEXT_LONG);
        options.put("Very long", TEXT_VERY_LONG);
        createSelectAction("Error message", category, options, "-",
                errorMessageCommand);

    }

    private void createDescriptionSelect(String category) {
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        options.put("-", null);
        options.put(TEXT_SHORT, TEXT_SHORT);
        options.put("Medium", TEXT_MEDIUM);
        options.put("Long", TEXT_LONG);
        options.put("Very long", TEXT_VERY_LONG);
        createSelectAction("Description / tooltip", category, options, "-",
                descriptionCommand);

    }

    private void createCaptionSelect(String category) {
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        options.put("-", null);
        options.put("Short", TEXT_SHORT);
        options.put("Medium", TEXT_MEDIUM);
        options.put("Long", TEXT_LONG);
        options.put("Very long", TEXT_VERY_LONG);
        createSelectAction("Caption", category, options, "Short",
                captionCommand);

    }

    private void createWidthSelect(String category) {
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        options.put("Undefined", null);
        options.put("50%", "50%");
        options.put("100%", "100%");
        for (int w = 200; w < 1000; w += 100) {
            options.put(w + "px", w + "px");
        }

        createSelectAction("Width", category, options, "Undefined",
                widthCommand, null);
    }

    private void createIconSelect(String category) {
        LinkedHashMap<String, Resource> options = new LinkedHashMap<String, Resource>();
        options.put("-", null);
        options.put("16x16", ICON_16_USER_PNG_CACHEABLE);
        options.put("32x32", ICON_32_ATTENTION_PNG_CACHEABLE);
        options.put("64x64", ICON_64_EMAIL_REPLY_PNG_CACHEABLE);

        createSelectAction("Icon", category, options, "-", iconCommand, null);
    }

    private void createLocaleSelect(String category) {
        LinkedHashMap<String, Locale> options = new LinkedHashMap<String, Locale>();
        options.put("-", null);
        options.put("fi_FI", new Locale("fi", "FI"));
        options.put("en_US", Locale.US);
        options.put("zh_CN", Locale.SIMPLIFIED_CHINESE);
        options.put("fr_FR", Locale.FRANCE);

        createSelectAction("Locale", category, options, "-", localeCommand,
                null);
    }

    private void createHeightSelect(String category) {
        LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
        options.put("Undefined", null);
        options.put("50%", "50%");
        options.put("100%", "100%");
        for (int w = 200; w < 1000; w += 100) {
            options.put(w + "px", w + "px");
        }

        createSelectAction("Height", category, options, "Undefined",
                heightCommand, null);
    }

    protected void createBooleanAction(String caption, String category,
            boolean initialState, final Command<T, Boolean> command) {
        createBooleanAction(caption, category, initialState, command, null);
    }

    protected <DATATYPE> void createBooleanAction(String caption,
            String category, boolean initialState,
            final Command<T, Boolean> command, Object data) {
        MenuItem categoryItem = getCategoryMenuItem(category);
        MenuItem item = categoryItem.addItem(caption,
                menuBooleanCommand(command, data));
        setSelected(item, initialState);
        doCommand(caption, command, initialState, data);
    }

    private MenuItem getCategoryMenuItem(String category) {
        if (category == null) {
            return getCategoryMenuItem("Misc");
        }

        if (mainMenu.getChildren() != null) {
            for (MenuItem i : mainMenu.getChildren()) {
                if (i.getText().equals(category)) {
                    return i;
                }
            }
        }
        return mainMenu.addItem(category, null);
    }

    private MenuBar.Command menuBooleanCommand(
            final com.vaadin.tests.components.ComponentTestCase.Command<T, Boolean> booleanCommand,
            final Object data) {

        return new MenuBar.Command() {
            public void menuSelected(MenuItem selectedItem) {
                boolean selected = !isSelected(selectedItem);
                doCommand(getText(selectedItem), booleanCommand, selected, data);
                setSelected(selectedItem, selected);
            }

        };
    }

    protected void setSelected(MenuItem item, boolean selected) {
        if (selected) {
            item.setIcon(SELECTED_ICON);
        } else {
            item.setIcon(null);
        }
    }

    protected boolean isSelected(MenuItem item) {
        return (item.getIcon() != null);
    }

    private <VALUETYPE> MenuBar.Command singleSelectMenuCommand(
            final com.vaadin.tests.components.ComponentTestCase.Command<T, VALUETYPE> cmd,
            final VALUETYPE object, final Object data) {
        return new MenuBar.Command() {
            public void menuSelected(MenuItem selectedItem) {
                doCommand(getText(selectedItem), cmd, object, data);

                if (parentOfSelectableMenuItem.contains(selectedItem
                        .getParent())) {
                    unselectChildren(selectedItem.getParent());
                    setSelected(selectedItem, true);
                }
            }

        };

    }

    /**
     * Unselect all child menu items
     * 
     * @param parent
     */
    protected void unselectChildren(MenuItem parent) {
        List<MenuItem> children = parent.getChildren();
        if (children == null) {
            return;
        }

        for (MenuItem child : children) {
            setSelected(child, false);
        }
    }

    protected String getText(MenuItem item) {
        if (!isCategory(item.getParent())) {
            return item.getParent().getText();
        } else {
            return item.getText();
        }
    }

    private boolean isCategory(MenuItem item) {
        return item.getParent() == mainMenu;
    }

    protected <TYPE> void createSelectAction(
            String caption,
            String category,
            LinkedHashMap<String, TYPE> options,
            String initialValue,
            com.vaadin.tests.components.ComponentTestCase.Command<T, TYPE> command) {
        createSelectAction(caption, category, options, initialValue, command,
                null);

    }

    protected <TYPE> void createSelectAction(
            String caption,
            String category,
            LinkedHashMap<String, TYPE> options,
            String initialValue,
            com.vaadin.tests.components.ComponentTestCase.Command<T, TYPE> command,
            Object data) {

        MenuItem parentItem = getCategoryMenuItem(category);
        MenuItem mainItem = parentItem.addItem(caption, null);

        parentOfSelectableMenuItem.add(mainItem);
        for (String option : options.keySet()) {
            MenuBar.Command cmd = singleSelectMenuCommand(command,
                    options.get(option), data);
            MenuItem item = mainItem.addItem(option, cmd);
            if (option.equals(initialValue)) {
                cmd.menuSelected(item);
            }
        }
    }

}
