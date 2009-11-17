/*
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class VPopupCalendar extends VTextualDate implements Paintable, Field,
        ClickHandler, CloseHandler<PopupPanel> {

    private final Button calendarToggle;

    private final VCalendarPanel calendar;

    private final VOverlay popup;
    private boolean open = false;

    public VPopupCalendar() {
        super();

        calendarToggle = new Button();
        calendarToggle.setStyleName(CLASSNAME + "-button");
        calendarToggle.setText("");
        calendarToggle.addClickHandler(this);
        add(calendarToggle);

        calendar = new VCalendarPanel(this);
        popup = new VOverlay(true, true, true);
        popup.setStyleName(VDateField.CLASSNAME + "-popup");
        popup.setWidget(calendar);
        popup.addCloseHandler(this);

        DOM.setElementProperty(calendar.getElement(), "id",
                "PID_VAADIN_POPUPCAL");

    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        boolean lastReadOnlyState = readonly;
        super.updateFromUIDL(uidl, client);
        addStyleName(CLASSNAME + "-popupcalendar");
        popup.setStyleName(VDateField.CLASSNAME + "-popup "
                + VDateField.CLASSNAME + "-"
                + resolutionToString(currentResolution));
        if (date != null) {
            calendar.updateCalendar();
        }
        calendarToggle.setEnabled(enabled);

        if (lastReadOnlyState != readonly) {
            updateWidth();
        }

    }

    public void onClick(ClickEvent event) {
        if (event.getSource() == calendarToggle && !open && !readonly) {
            open = true;
            calendar.updateCalendar();
            // clear previous values
            popup.setWidth("");
            popup.setHeight("");
            popup.setPopupPositionAndShow(new PositionCallback() {
                public void setPosition(int offsetWidth, int offsetHeight) {
                    final int w = offsetWidth;
                    final int h = offsetHeight;
                    final int browserWindowWidth = Window.getClientWidth()
                            + Window.getScrollLeft();
                    final int browserWindowHeight = Window.getClientHeight()
                            + Window.getScrollTop();
                    int t = calendarToggle.getAbsoluteTop();
                    int l = calendarToggle.getAbsoluteLeft();

                    // Add a little extra space to the right to avoid
                    // problems with IE6/IE7 scrollbars and to make it look
                    // nicer.
                    int extraSpace = 30;

                    boolean overflowRight = false;
                    if (l + +w + extraSpace > browserWindowWidth) {
                        overflowRight = true;
                        // Part of the popup is outside the browser window 
                        // (to the right)
                        l = browserWindowWidth - w - extraSpace;
                    }

                    if (t + h + calendarToggle.getOffsetHeight() + 30 > browserWindowHeight) {
                        // Part of the popup is outside the browser window
                        // (below)
                        t = browserWindowHeight - h
                                - calendarToggle.getOffsetHeight() - 30;
                        if (!overflowRight) {
                            // Show to the right of the popup button unless we
                            // are in the lower right corner of the screen
                            l += calendarToggle.getOffsetWidth();
                        }
                    }

                    // fix size
                    popup.setWidth(w + "px");
                    popup.setHeight(h + "px");

                    popup.setPopupPosition(l, t
                            + calendarToggle.getOffsetHeight() + 2);

                    setFocus(true);
                }
            });
        }
    }

    public void onClose(CloseEvent<PopupPanel> event) {
        if (event.getSource() == popup) {
            buildDate();
            // Sigh.
            Timer t = new Timer() {
                @Override
                public void run() {
                    open = false;
                }
            };
            t.schedule(100);
        }
    }

    /**
     * Sets focus to Calendar panel.
     * 
     * @param focus
     */
    public void setFocus(boolean focus) {
        calendar.setFocus(focus);
    }

    @Override
    protected int getFieldExtraWidth() {
        if (fieldExtraWidth < 0) {
            fieldExtraWidth = super.getFieldExtraWidth();
            fieldExtraWidth += calendarToggle.getOffsetWidth();
        }
        return fieldExtraWidth;
    }

}
