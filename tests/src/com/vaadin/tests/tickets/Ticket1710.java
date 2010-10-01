package com.vaadin.tests.tickets;

import java.util.Iterator;
import java.util.LinkedList;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.MethodProperty;
import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.SystemError;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.DateField;
import com.vaadin.ui.ExpandLayout;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Layout.AlignmentHandler;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.OrderedLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

public class Ticket1710 extends com.vaadin.Application {

    LinkedList listOfAllFields = new LinkedList();

    public void init() {

        setTheme("tests-tickets");

        OrderedLayout lo = new OrderedLayout();
        setMainWindow(new Window("#1710", lo));
        lo.setMargin(true);
        lo.setSpacing(true);
        lo.setWidth("100%");

        // Hiding controls
        OrderedLayout hidingControls = new OrderedLayout(
                OrderedLayout.ORIENTATION_HORIZONTAL);
        lo.addComponent(hidingControls);

        // OrderedLayout
        final OrderedLayout orderedLayout = new OrderedLayout();
        LayoutTestingPanel oltp = new LayoutTestingPanel("OrderedLayout",
                orderedLayout);
        hidingControls.addComponent(new Button("OrderedLayout",
                new MethodProperty<Boolean>(oltp, "visible")));
        lo.addComponent(oltp);
        orderedLayout.setSpacing(false);
        addFields(orderedLayout);
        final Button orientationButton = new Button("horizontal orientation",
                false);
        orientationButton.addListener(new Property.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
                orderedLayout.setOrientation(orientationButton.booleanValue() ? OrderedLayout.ORIENTATION_HORIZONTAL
                        : OrderedLayout.ORIENTATION_VERTICAL);
            }
        });
        orientationButton.setImmediate(true);
        oltp.controls.addComponent(orientationButton);

        // GridLayout
        GridLayout grid = new GridLayout(1, 1);
        Panel g1tp = new LayoutTestingPanel("Gridlayout with 1 column", grid);
        hidingControls.addComponent(new Button("GridLayout (1col)",
                new MethodProperty<Boolean>(g1tp, "visible")));
        g1tp.setVisible(false);
        lo.addComponent(g1tp);
        grid.setSpacing(true);
        addFields(grid);
        GridLayout grid2 = new GridLayout(2, 1);
        Panel g2tp = new LayoutTestingPanel("Gridlayout with 2 columns", grid2);
        hidingControls.addComponent(new Button("GridLayout (2cols)",
                new MethodProperty<Boolean>(g2tp, "visible")));
        g2tp.setVisible(false);
        lo.addComponent(g2tp);
        grid2.setSpacing(true);
        addFields(grid2);

        // ExpandLayout
        ExpandLayout el = new ExpandLayout();
        Panel elp = new LayoutTestingPanel(
                "ExpandLayout width first component expanded", el);
        hidingControls.addComponent(new Button("ExpandLayout (vertical)",
                new MethodProperty<Boolean>(elp, "visible")));
        elp.setVisible(false);
        el.setHeight(700);
        addFields(el);
        Component firstComponent = (Component) el.getComponentIterator().next();
        firstComponent.setSizeFull();
        el.expand(firstComponent);
        lo.addComponent(elp);
        ExpandLayout elh = new ExpandLayout(ExpandLayout.ORIENTATION_HORIZONTAL);
        Panel elhp = new LayoutTestingPanel(
                "ExpandLayout width first component expanded; horizontal", elh);
        hidingControls.addComponent(new Button("ExpandLayout (horizontal)",
                new MethodProperty<Boolean>(elhp, "visible")));
        elhp.setVisible(false);
        elhp.setScrollable(true);
        elh.setWidth(2000);
        elh.setHeight(100);
        addFields(elh);
        Component firstComponentElh = (Component) elh.getComponentIterator()
                .next();
        firstComponentElh.setSizeFull();
        elh.expand(firstComponentElh);
        lo.addComponent(elhp);

        // CustomLayout
        OrderedLayout cl = new OrderedLayout();
        Panel clp = new LayoutTestingPanel("CustomLayout", cl);
        hidingControls.addComponent(new Button("CustomLayout",
                new MethodProperty<Boolean>(clp, "visible")));
        clp.setVisible(false);
        lo.addComponent(clp);
        cl.addComponent(new Label("<<< Add customlayout testcase here >>>"));

        // Form
        Panel formPanel = new Panel("Form");
        hidingControls.addComponent(new Button("Form",
                new MethodProperty<Boolean>(formPanel, "visible")));
        formPanel.setVisible(false);
        formPanel.addComponent(getFormPanelExample());
        lo.addComponent(formPanel);

        for (Iterator i = hidingControls.getComponentIterator(); i.hasNext();) {
            ((AbstractComponent) i.next()).setImmediate(true);
        }

    }

    private Form getFormPanelExample() {
        Form f = new Form();
        f.setCaption("Test form");
        Button fb1 = new Button("Test button");
        fb1.setComponentError(new SystemError("Test error"));
        f.addField("fb1", fb1);
        Button fb2 = new Button("Test button", true);
        fb2.setComponentError(new SystemError("Test error"));
        f.addField("fb2", fb2);
        TextField ft1 = new TextField("With caption");
        ft1.setComponentError(new SystemError("Error"));
        f.addField("ft1", ft1);
        TextField ft2 = new TextField();
        ft2.setComponentError(new SystemError("Error"));
        ft2.setValue("Without caption");
        f.addField("ft2", ft2);
        TextField ft3 = new TextField("With caption and required");
        ft3.setComponentError(new SystemError("Error"));
        ft3.setRequired(true);
        f.addField("ft3", ft3);
        return f;
    }

    private void addFields(ComponentContainer lo) {
        Button button = new Button("Test button");
        button.setComponentError(new SystemError("Test error"));
        lo.addComponent(button);

        Button b2 = new Button("Test button");
        b2.setComponentError(new SystemError("Test error"));
        b2.setSwitchMode(true);
        lo.addComponent(b2);

        TextField t1 = new TextField("With caption");
        t1.setComponentError(new SystemError("Error"));
        lo.addComponent(t1);

        TextField t2 = new TextField("With caption and required");
        t2.setComponentError(new SystemError("Error"));
        t2.setRequired(true);
        lo.addComponent(t2);

        TextField t3 = new TextField();
        t3.setValue("Without caption");
        t3.setComponentError(new SystemError("Error"));
        lo.addComponent(t3);

        lo.addComponent(new TextField("Textfield with no error in it"));

        TextField tt1 = new TextField("100% wide Textfield with no error in it");
        tt1.setWidth("100%");
        lo.addComponent(tt1);

        TextField tt2 = new TextField();
        tt2.setWidth("100%");
        tt2.setValue("100% wide Textfield with no error in it and no caption");
        lo.addComponent(tt2);

        TextField t4 = new TextField();
        t4.setValue("Without caption, With required");
        t4.setComponentError(new SystemError("Error"));
        t4.setRequired(true);
        lo.addComponent(t4);

        TextField t5 = new TextField();
        t5.setValue("Without caption,  WIDE");
        t5.setComponentError(new SystemError("Error"));
        t5.setWidth(100);
        t5.setWidthUnits(Sizeable.UNITS_PERCENTAGE);
        lo.addComponent(t5);

        TextField t6 = new TextField();
        t6.setValue("Without caption, With required, WIDE");
        t6.setComponentError(new SystemError("Error"));
        t6.setRequired(true);
        t6.setWidth(100);
        t6.setWidthUnits(Sizeable.UNITS_PERCENTAGE);
        lo.addComponent(t6);

        TextField t7 = new TextField();
        t7.setValue("With icon and required and icon");
        t7.setComponentError(new SystemError("Error"));
        t7.setRequired(true);
        t7.setIcon(new ThemeResource("../runo/icons/16/ok.png"));
        lo.addComponent(t7);

        DateField d1 = new DateField(
                "Datefield with caption and icon, next one without caption");
        d1.setComponentError(new SystemError("Error"));
        d1.setRequired(true);
        d1.setIcon(new ThemeResource("../runo/icons/16/ok.png"));
        lo.addComponent(d1);

        DateField d2 = new DateField();
        d2.setComponentError(new SystemError("Error"));
        d2.setRequired(true);
        lo.addComponent(d2);
    }

    public class LayoutTestingPanel extends Panel {

        Layout testedLayout;

        OrderedLayout controls = new OrderedLayout(
                OrderedLayout.ORIENTATION_HORIZONTAL);
        Button marginLeft = new Button("m-left", false);
        Button marginRight = new Button("m-right", false);
        Button marginTop = new Button("m-top", false);
        Button marginBottom = new Button("m-bottom", false);
        Button spacing = new Button("spacing", false);
        OrderedLayout testPanelLayout = new OrderedLayout();

        LayoutTestingPanel(String caption, Layout layout) {
            super(caption);
            OrderedLayout internalLayout = new OrderedLayout();
            internalLayout.setWidth("100%");
            setLayout(internalLayout);
            testedLayout = layout;
            testPanelLayout.setWidth("100%");
            Panel controlWrapper = new Panel();
            controlWrapper.addComponent(controls);
            controlWrapper.setWidth("100%");
            controlWrapper.setScrollable(true);
            controlWrapper.setStyleName("controls");
            internalLayout.addComponent(controlWrapper);
            Panel testPanel = new Panel(testPanelLayout);
            testPanel.setStyleName("testarea");
            testPanelLayout.addComponent(testedLayout);
            internalLayout.addComponent(testPanel);
            internalLayout.setMargin(true);
            internalLayout.setSpacing(true);
            controls.setSpacing(true);
            controls.setMargin(false);
            controls.addComponent(new Label("width"));
            controls.addComponent(new TextField(new MethodProperty<Float>(
                    testedLayout, "width")));
            controls.addComponent(new Button("%", new MethodProperty<Boolean>(
                    this, "widthPercents")));
            controls.addComponent(new Label("height"));
            controls.addComponent(new TextField(new MethodProperty<Float>(
                    testedLayout, "height")));
            controls.addComponent(new Button("%", new MethodProperty<Boolean>(
                    this, "heightPercents")));
            controls.addComponent(marginLeft);
            controls.addComponent(marginRight);
            controls.addComponent(marginTop);
            controls.addComponent(marginBottom);
            if (testedLayout instanceof Layout.SpacingHandler) {
                controls.addComponent(spacing);
            }

            Property.ValueChangeListener marginSpacingListener = new Property.ValueChangeListener() {
                public void valueChange(ValueChangeEvent event) {
                    updateMarginsAndSpacing();
                }
            };

            marginBottom.addListener(marginSpacingListener);
            marginTop.addListener(marginSpacingListener);
            marginLeft.addListener(marginSpacingListener);
            marginRight.addListener(marginSpacingListener);
            spacing.addListener(marginSpacingListener);
            updateMarginsAndSpacing();

            addAlignmentControls();

            testedLayout.setStyleName("tested-layout");
            setStyleName("layout-testing-panel");

            for (Iterator i = controls.getComponentIterator(); i.hasNext();) {
                ((AbstractComponent) i.next()).setImmediate(true);
            }
        }

        private void addAlignmentControls() {
            if (!(testedLayout instanceof Layout.AlignmentHandler)) {
                return;
            }
            @SuppressWarnings("unused")
            final Layout.AlignmentHandler ah = (AlignmentHandler) testedLayout;

            final NativeSelect vAlign = new NativeSelect();
            final NativeSelect hAlign = new NativeSelect();
            controls.addComponent(new Label("component alignment"));
            controls.addComponent(hAlign);
            controls.addComponent(vAlign);
            hAlign.setNullSelectionAllowed(false);
            vAlign.setNullSelectionAllowed(false);

            vAlign.addItem(new Integer(Layout.AlignmentHandler.ALIGNMENT_TOP));
            vAlign.setItemCaption(new Integer(
                    Layout.AlignmentHandler.ALIGNMENT_TOP), "top");
            vAlign.addItem(new Integer(
                    Layout.AlignmentHandler.ALIGNMENT_VERTICAL_CENTER));
            vAlign.setItemCaption(new Integer(
                    Layout.AlignmentHandler.ALIGNMENT_VERTICAL_CENTER),
                    "center");
            vAlign.addItem(new Integer(Layout.AlignmentHandler.ALIGNMENT_BOTTOM));
            vAlign.setItemCaption(new Integer(
                    Layout.AlignmentHandler.ALIGNMENT_BOTTOM), "bottom");

            hAlign.addItem(new Integer(Layout.AlignmentHandler.ALIGNMENT_LEFT));
            hAlign.setItemCaption(new Integer(
                    Layout.AlignmentHandler.ALIGNMENT_LEFT), "left");
            hAlign.addItem(new Integer(
                    Layout.AlignmentHandler.ALIGNMENT_HORIZONTAL_CENTER));
            hAlign.setItemCaption(new Integer(
                    Layout.AlignmentHandler.ALIGNMENT_HORIZONTAL_CENTER),
                    "center");
            hAlign.addItem(new Integer(Layout.AlignmentHandler.ALIGNMENT_RIGHT));
            hAlign.setItemCaption(new Integer(
                    Layout.AlignmentHandler.ALIGNMENT_RIGHT), "right");

            Property.ValueChangeListener alignmentChangeListener = new Property.ValueChangeListener() {
                public void valueChange(ValueChangeEvent event) {
                    updateAlignments(((Integer) hAlign.getValue()).intValue(),
                            ((Integer) vAlign.getValue()).intValue());
                }

            };

            hAlign.setValue(new Integer(Layout.AlignmentHandler.ALIGNMENT_LEFT));
            vAlign.addListener(alignmentChangeListener);
            hAlign.addListener(alignmentChangeListener);
            vAlign.setValue(new Integer(Layout.AlignmentHandler.ALIGNMENT_TOP));

            controls.addComponent(new Label("layout alignment"));
            final NativeSelect lAlign = new NativeSelect();
            controls.addComponent(lAlign);
            lAlign.setNullSelectionAllowed(false);
            lAlign.addItem(new Integer(Layout.AlignmentHandler.ALIGNMENT_LEFT));
            lAlign.setItemCaption(new Integer(
                    Layout.AlignmentHandler.ALIGNMENT_LEFT), "left");
            lAlign.addItem(new Integer(
                    Layout.AlignmentHandler.ALIGNMENT_HORIZONTAL_CENTER));
            lAlign.setItemCaption(new Integer(
                    Layout.AlignmentHandler.ALIGNMENT_HORIZONTAL_CENTER),
                    "center");
            lAlign.addItem(new Integer(Layout.AlignmentHandler.ALIGNMENT_RIGHT));
            lAlign.setItemCaption(new Integer(
                    Layout.AlignmentHandler.ALIGNMENT_RIGHT), "right");

            lAlign.addListener(new Property.ValueChangeListener() {
                public void valueChange(ValueChangeEvent event) {
                    testPanelLayout.setComponentAlignment(testedLayout,
                            ((Integer) lAlign.getValue()).intValue(),
                            OrderedLayout.ALIGNMENT_TOP);
                }
            });
        }

        private void updateAlignments(int h, int v) {
            for (Iterator i = testedLayout.getComponentIterator(); i.hasNext();) {
                ((Layout.AlignmentHandler) testedLayout).setComponentAlignment(
                        (Component) i.next(), h, v);
            }
        }

        private void updateMarginsAndSpacing() {
            testedLayout.setMargin(
                    ((Boolean) marginTop.getValue()).booleanValue(),
                    ((Boolean) marginRight.getValue()).booleanValue(),
                    ((Boolean) marginBottom.getValue()).booleanValue(),
                    ((Boolean) marginLeft.getValue()).booleanValue());
            if (testedLayout instanceof Layout.SpacingHandler) {
                ((Layout.SpacingHandler) testedLayout)
                        .setSpacing(((Boolean) spacing.getValue())
                                .booleanValue());
            }
        }

        public boolean getWidthPercents() {
            return testedLayout.getWidthUnits() == Sizeable.UNITS_PERCENTAGE;
        }

        public void setWidthPercents(boolean b) {
            testedLayout.setWidthUnits(b ? Sizeable.UNITS_PERCENTAGE
                    : Sizeable.UNITS_PIXELS);
        }

        public boolean getHeightPercents() {
            return testedLayout.getHeightUnits() == Sizeable.UNITS_PERCENTAGE;
        }

        public void setHeightPercents(boolean b) {
            testedLayout.setHeightUnits(b ? Sizeable.UNITS_PERCENTAGE
                    : Sizeable.UNITS_PIXELS);
        }
    }
}
