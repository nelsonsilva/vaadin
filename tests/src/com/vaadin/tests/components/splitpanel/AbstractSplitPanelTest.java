package com.vaadin.tests.components.splitpanel;

import com.vaadin.terminal.Sizeable;
import com.vaadin.tests.components.AbstractLayoutTest;
import com.vaadin.ui.AbstractSplitPanel;
import com.vaadin.ui.AbstractSplitPanel.SplitterClickEvent;
import com.vaadin.ui.AbstractSplitPanel.SplitterClickListener;

public abstract class AbstractSplitPanelTest<T extends AbstractSplitPanel>
        extends AbstractLayoutTest<T> implements SplitterClickListener {

    private Command<T, Boolean> splitterClickListenerCommand = new Command<T, Boolean>() {

        public void execute(T c, Boolean value, Object data) {
            c.addListener((SplitterClickListener) AbstractSplitPanelTest.this);

        }
    };
    private Command<T, SplitPosition> setSplitPositionCommand = new Command<T, AbstractSplitPanelTest.SplitPosition>() {
        public void execute(T c, SplitPosition value, Object data) {
            value.apply(c);
        }
    };

    @Override
    protected void createActions() {
        super.createActions();
        createSetSplitPositionAction(CATEGORY_FEATURES);
        createSplitterClickListenerAction(CATEGORY_LISTENERS);

        // Default to 100% x 100% as SplitPanel does not work as undefined
        for (T c : getTestComponents()) {
            c.setSizeFull();
        }
    }

    public static class SplitPosition {

        private boolean reverse = false;
        private int position;
        private int unit;
        private String posString;

        public SplitPosition(String pos) {
            this.posString = pos;
            if (pos.startsWith("-")) {
                reverse = true;
                pos = pos.substring(1);
            }

            if (pos.endsWith("px")) {
                position = Integer.parseInt(pos.substring(0, pos.length() - 2));
                unit = Sizeable.UNITS_PIXELS;
            } else if (pos.endsWith("%")) {
                position = Integer.parseInt(pos.substring(0, pos.length() - 1));
                unit = Sizeable.UNITS_PERCENTAGE;
            } else {
                throw new RuntimeException("Could not parse " + pos);
            }
        }

        public void apply(AbstractSplitPanel sp) {
            sp.setSplitPosition(position, unit, reverse);
        }

        @Override
        public String toString() {
            return posString;
        }
    }

    private void createSetSplitPositionAction(String categoryFeatures) {
        String subCategory = "Set splitter position";
        createCategory(subCategory, categoryFeatures);

        createClickAction("0px from left/top", subCategory,
                setSplitPositionCommand, new SplitPosition("0px"));
        createClickAction("200px from left/top", subCategory,
                setSplitPositionCommand, new SplitPosition("200px"));
        createClickAction("0px from right/bottom", subCategory,
                setSplitPositionCommand, new SplitPosition("-0px"));
        createClickAction("200px from right/bottom", subCategory,
                setSplitPositionCommand, new SplitPosition("-200px"));

        createClickAction("0% from left/top", subCategory,
                setSplitPositionCommand, new SplitPosition("0%"));
        createClickAction("0% from right/bottom", subCategory,
                setSplitPositionCommand, new SplitPosition("-0%"));
        createClickAction("50% from left/top", subCategory,
                setSplitPositionCommand, new SplitPosition("50%"));
        createClickAction("50% from right/bottom", subCategory,
                setSplitPositionCommand, new SplitPosition("-50%"));
        createClickAction("100% from left/top", subCategory,
                setSplitPositionCommand, new SplitPosition("100%"));
        createClickAction("100% from right/bottom", subCategory,
                setSplitPositionCommand, new SplitPosition("-100%"));

    }

    private void createSplitterClickListenerAction(String category) {
        createBooleanAction("SplitterClickListener", category, false,
                splitterClickListenerCommand);

    }

    public void splitterClick(SplitterClickEvent event) {
        log(event.getClass().getSimpleName() + ": " + event.getButtonName()
                + " at " + event.getRelativeX() + "," + event.getRelativeY());
    }
}
