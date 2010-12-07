package com.vaadin.tests.components.textarea;

import java.util.LinkedHashMap;

import com.vaadin.tests.components.abstractfield.AbstractTextFieldTest;
import com.vaadin.ui.TextArea;

public class TextAreaTest extends AbstractTextFieldTest<TextArea> {

    private Command<TextArea, Boolean> wordwrapCommand = new Command<TextArea, Boolean>() {
        public void execute(TextArea c, Boolean value, Object data) {
            c.setWordwrap(value);
        }
    };

    private Command<TextArea, Integer> rowsCommand = new Command<TextArea, Integer>() {
        public void execute(TextArea c, Integer value, Object data) {
            c.setRows(value);
        }
    };

    @Override
    protected Class<TextArea> getTestClass() {
        return TextArea.class;
    }

    @Override
    protected void createActions() {
        super.createActions();
        createWordwrapAction(CATEGORY_STATE);
        createRowsAction(CATEGORY_STATE);
    }

    private void createRowsAction(String category) {
        LinkedHashMap<String, Integer> options = createIntegerOptions(20);
        createSelectAction("Rows", category, options, "0", rowsCommand);
    }

    private void createWordwrapAction(String category) {
        createBooleanAction("Wordwrap", category, false, wordwrapCommand);
    }

}
