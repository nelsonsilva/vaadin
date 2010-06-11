/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import java.util.Date;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.TextBox;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.BrowserInfo;
import com.vaadin.terminal.gwt.client.ClientExceptionHandler;
import com.vaadin.terminal.gwt.client.ContainerResizedListener;
import com.vaadin.terminal.gwt.client.EventId;
import com.vaadin.terminal.gwt.client.Focusable;
import com.vaadin.terminal.gwt.client.LocaleNotLoadedException;
import com.vaadin.terminal.gwt.client.LocaleService;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class VTextualDate extends VDateField implements Paintable, Field,
        ChangeHandler, ContainerResizedListener, Focusable {

    private static final String PARSE_ERROR_CLASSNAME = CLASSNAME
            + "-parseerror";

    private final TextBox text;

    private String formatStr;

    private String width;

    private boolean needLayout;

    protected int fieldExtraWidth = -1;

    private boolean lenient;

    private static final String CLASSNAME_PROMPT = "prompt";
    private static final String ATTR_INPUTPROMPT = "prompt";
    private String inputPrompt = "";
    private boolean prompting = false;

    public VTextualDate() {

        super();
        text = new TextBox();
        // use normal textfield styles as a basis
        text.setStyleName(VTextField.CLASSNAME);
        // add datefield spesific style name also
        text.addStyleName(CLASSNAME + "-textfield");
        text.addChangeHandler(this);
        text.addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                text.addStyleName(VTextField.CLASSNAME + "-"
                        + VTextField.CLASSNAME_FOCUS);
                if (prompting) {
                    text.setText("");
                    setPrompting(false);
                }
                if (client != null
                        && client.hasEventListeners(VTextualDate.this,
                                EventId.FOCUS)) {
                    client.updateVariable(id, EventId.FOCUS, "", true);
                }
            }
        });
        text.addBlurHandler(new BlurHandler() {
            public void onBlur(BlurEvent event) {
                text.removeStyleName(VTextField.CLASSNAME + "-"
                        + VTextField.CLASSNAME_FOCUS);
                String value = getText();
                setPrompting(inputPrompt != null
                        && (value == null || "".equals(value)));
                if (prompting) {
                    text.setText(readonly ? "" : inputPrompt);
                }
                if (client != null
                        && client.hasEventListeners(VTextualDate.this,
                                EventId.BLUR)) {
                    client.updateVariable(id, EventId.BLUR, "", true);
                }
            }
        });
        add(text);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        int origRes = currentResolution;
        String oldLocale = currentLocale;
        super.updateFromUIDL(uidl, client);
        if (origRes != currentResolution || oldLocale != currentLocale) {
            // force recreating format string
            formatStr = null;
        }
        if (uidl.hasAttribute("format")) {
            formatStr = uidl.getStringAttribute("format");
        }

        inputPrompt = uidl.getStringAttribute(ATTR_INPUTPROMPT);

        lenient = !uidl.getBooleanAttribute("strict");

        buildDate();
        // not a FocusWidget -> needs own tabindex handling
        if (uidl.hasAttribute("tabindex")) {
            text.setTabIndex(uidl.getIntAttribute("tabindex"));
        }

        if (readonly) {
            text.addStyleDependentName("readonly");
        } else {
            text.removeStyleDependentName("readonly");
        }

    }

    protected String getFormatString() {
        if (formatStr == null) {
            if (currentResolution == RESOLUTION_YEAR) {
                formatStr = "yyyy"; // force full year
            } else {

                try {
                    String frmString = LocaleService
                            .getDateFormat(currentLocale);
                    frmString = cleanFormat(frmString);
                    String delim = LocaleService
                            .getClockDelimiter(currentLocale);

                    if (currentResolution >= RESOLUTION_HOUR) {
                        if (dts.isTwelveHourClock()) {
                            frmString += " hh";
                        } else {
                            frmString += " HH";
                        }
                        if (currentResolution >= RESOLUTION_MIN) {
                            frmString += ":mm";
                            if (currentResolution >= RESOLUTION_SEC) {
                                frmString += ":ss";
                                if (currentResolution >= RESOLUTION_MSEC) {
                                    frmString += ".SSS";
                                }
                            }
                        }
                        if (dts.isTwelveHourClock()) {
                            frmString += " aaa";
                        }

                    }

                    formatStr = frmString;
                } catch (LocaleNotLoadedException e) {
                    ClientExceptionHandler.displayError(e);
                }
            }
        }
        return formatStr;
    }

    /**
     * 
     */
    protected void buildDate() {
        removeStyleName(PARSE_ERROR_CLASSNAME);
        // Create the initial text for the textfield
        String dateText;
        if (date != null) {
            dateText = DateTimeFormat.getFormat(getFormatString()).format(date);
        } else {
            dateText = "";
        }

        setText(dateText);
        text.setEnabled(enabled);
        text.setReadOnly(readonly);

        if (readonly) {
            text.addStyleName("v-readonly");
        } else {
            text.removeStyleName("v-readonly");
        }

    }

    protected void setPrompting(boolean prompting) {
        this.prompting = prompting;
        if (prompting) {
            addStyleDependentName(CLASSNAME_PROMPT);
        } else {
            removeStyleDependentName(CLASSNAME_PROMPT);
        }
    }

    public void onChange(ChangeEvent event) {
        if (!text.getText().equals("")) {
            try {
                DateTimeFormat format = DateTimeFormat
                        .getFormat(getFormatString());
                if (lenient) {
                    date = format.parse(text.getText());
                    if (date != null) {
                        // if date value was leniently parsed, normalize text
                        // presentation
                        text.setValue(DateTimeFormat.getFormat(
                                getFormatString()).format(date), false);
                    }
                } else {
                    date = format.parseStrict(text.getText());
                }

                long stamp = date.getTime();
                if (stamp == 0) {
                    // If date parsing fails in firefox the stamp will be 0
                    date = null;
                    addStyleName(PARSE_ERROR_CLASSNAME);
                } else {
                    // remove possibly added invalid value indication
                    removeStyleName(PARSE_ERROR_CLASSNAME);
                }
            } catch (final Exception e) {
                ClientExceptionHandler.displayError(e.getMessage());

                addStyleName(PARSE_ERROR_CLASSNAME);
                // this is a hack that may eventually be removed
                client.updateVariable(id, "lastInvalidDateString", text
                        .getText(), false);
                date = null;
            }
        } else {
            date = null;
            // remove possibly added invalid value indication
            removeStyleName(PARSE_ERROR_CLASSNAME);
        }
        // always send the date string
        client.updateVariable(id, "dateString", text.getText(), false);

        if (date != null) {
            showingDate = new Date(date.getTime());
        }

        // Update variables
        // (only the smallest defining resolution needs to be
        // immediate)
        client.updateVariable(id, "year", date != null ? date.getYear() + 1900
                : -1, currentResolution == VDateField.RESOLUTION_YEAR
                && immediate);
        if (currentResolution >= VDateField.RESOLUTION_MONTH) {
            client.updateVariable(id, "month",
                    date != null ? date.getMonth() + 1 : -1,
                    currentResolution == VDateField.RESOLUTION_MONTH
                            && immediate);
        }
        if (currentResolution >= VDateField.RESOLUTION_DAY) {
            client
                    .updateVariable(id, "day", date != null ? date.getDate()
                            : -1,
                            currentResolution == VDateField.RESOLUTION_DAY
                                    && immediate);
        }
        if (currentResolution >= VDateField.RESOLUTION_HOUR) {
            client.updateVariable(id, "hour", date != null ? date.getHours()
                    : -1, currentResolution == VDateField.RESOLUTION_HOUR
                    && immediate);
        }
        if (currentResolution >= VDateField.RESOLUTION_MIN) {
            client.updateVariable(id, "min", date != null ? date.getMinutes()
                    : -1, currentResolution == VDateField.RESOLUTION_MIN
                    && immediate);
        }
        if (currentResolution >= VDateField.RESOLUTION_SEC) {
            client.updateVariable(id, "sec", date != null ? date.getSeconds()
                    : -1, currentResolution == VDateField.RESOLUTION_SEC
                    && immediate);
        }
        if (currentResolution == VDateField.RESOLUTION_MSEC) {
            client.updateVariable(id, "msec", date != null ? getMilliseconds()
                    : -1, immediate);
        }

    }

    private String cleanFormat(String format) {
        // Remove unnecessary d & M if resolution is too low
        if (currentResolution < VDateField.RESOLUTION_DAY) {
            format = format.replaceAll("d", "");
        }
        if (currentResolution < VDateField.RESOLUTION_MONTH) {
            format = format.replaceAll("M", "");
        }

        // Remove unsupported patterns
        // TODO support for 'G', era designator (used at least in Japan)
        format = format.replaceAll("[GzZwWkK]", "");

        // Remove extra delimiters ('/' and '.')
        while (format.startsWith("/") || format.startsWith(".")
                || format.startsWith("-")) {
            format = format.substring(1);
        }
        while (format.endsWith("/") || format.endsWith(".")
                || format.endsWith("-")) {
            format = format.substring(0, format.length() - 1);
        }

        // Remove duplicate delimiters
        format = format.replaceAll("//", "/");
        format = format.replaceAll("\\.\\.", ".");
        format = format.replaceAll("--", "-");

        return format.trim();
    }

    @Override
    public void setWidth(String newWidth) {
        if (!"".equals(newWidth) && (width == null || !newWidth.equals(width))) {
            if (BrowserInfo.get().isIE6()) {
                // in IE6 cols ~ min-width
                DOM.setElementProperty(text.getElement(), "size", "1");
            }
            needLayout = true;
            width = newWidth;
            super.setWidth(width);
            iLayout();
            if (newWidth.indexOf("%") < 0) {
                needLayout = false;
            }
        } else {
            if ("".equals(newWidth) && width != null && !"".equals(width)) {
                if (BrowserInfo.get().isIE6()) {
                    // revert IE6 hack
                    DOM.setElementProperty(text.getElement(), "size", "");
                }
                super.setWidth("");
                needLayout = true;
                iLayout();
                needLayout = false;
                width = null;
            }
        }
    }

    /**
     * Returns pixels in x-axis reserved for other than textfield content.
     * 
     * @return extra width in pixels
     */
    protected int getFieldExtraWidth() {
        if (fieldExtraWidth < 0) {
            text.setWidth("0");
            fieldExtraWidth = text.getOffsetWidth();
            if (BrowserInfo.get().isFF3()) {
                // Firefox somehow always leaves the INPUT element 2px wide
                fieldExtraWidth -= 2;
            }
        }
        return fieldExtraWidth;
    }

    public void updateWidth() {
        needLayout = true;
        fieldExtraWidth = -1;
        iLayout();
    }

    public void iLayout() {
        if (needLayout) {
            text.setWidth((getOffsetWidth() - getFieldExtraWidth()) + "px");
        }
    }

    public void focus() {
        text.setFocus(true);
    }

    protected String getText() {
        if (prompting) {
            return "";
        }
        return text.getText();
    }

    protected void setText(String text) {
        if (inputPrompt != null && (text == null || "".equals(text))) {
            text = readonly ? "" : inputPrompt;
            setPrompting(true);
        } else {
            setPrompting(false);
        }

        this.text.setText(text);
    }

}
