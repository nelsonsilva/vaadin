/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.PopupListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class VPopupCalendar extends VTextualDate implements Paintable, Field,
        ClickListener, PopupListener {

    private final Button calendarToggle;

    private final VCalendarPanel calendar;

    private final VToolkitOverlay popup;
    private boolean open = false;

    public VPopupCalendar() {
        super();

        calendarToggle = new Button();
        calendarToggle.setStyleName(CLASSNAME + "-button");
        calendarToggle.setText("");
        calendarToggle.addClickListener(this);
        add(calendarToggle);

        calendar = new VCalendarPanel(this);
        popup = new VToolkitOverlay(true, true, true);
        popup.setStyleName(VDateField.CLASSNAME + "-popup");
        popup.setWidget(calendar);
        popup.addPopupListener(this);

        DOM.setElementProperty(calendar.getElement(), "id",
                "PID_TOOLKIT_POPUPCAL");

    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        super.updateFromUIDL(uidl, client);
        addStyleName(CLASSNAME + "-popupcalendar");
        popup.setStyleName(VDateField.CLASSNAME + "-popup "
                + VDateField.CLASSNAME + "-"
                + resolutionToString(currentResolution));
        if (date != null) {
            calendar.updateCalendar();
        }
        calendarToggle.setEnabled(enabled);
    }

    public void onClick(Widget sender) {
        if (sender == calendarToggle && !open) {
            open = true;
            calendar.updateCalendar();
            // clear previous values
            popup.setWidth("");
            popup.setHeight("");
            popup.setPopupPositionAndShow(new PositionCallback() {
                public void setPosition(int offsetWidth, int offsetHeight) {
                    final int w = offsetWidth;
                    final int h = offsetHeight;
                    int t = calendarToggle.getAbsoluteTop();
                    int l = calendarToggle.getAbsoluteLeft();
                    if (l + w > Window.getClientWidth()
                            + Window.getScrollLeft()) {
                        l = Window.getClientWidth() + Window.getScrollLeft()
                                - w;
                    }
                    if (t + h + calendarToggle.getOffsetHeight() + 30 > Window
                            .getClientHeight()
                            + Window.getScrollTop()) {
                        t = Window.getClientHeight() + Window.getScrollTop()
                                - h - calendarToggle.getOffsetHeight() - 30;
                        l += calendarToggle.getOffsetWidth();
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

    public void onPopupClosed(PopupPanel sender, boolean autoClosed) {
        if (sender == popup) {
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
