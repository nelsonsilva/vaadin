/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client.ui;

import java.util.Date;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class VTime extends FlowPanel implements ChangeListener {

    private final VDateField datefield;

    private ListBox hours;

    private ListBox mins;

    private ListBox sec;

    private ListBox msec;

    private ListBox ampm;

    private int resolution = VDateField.RESOLUTION_HOUR;

    private boolean readonly;

    public VTime(VDateField parent) {
        super();
        datefield = parent;
        setStyleName(VDateField.CLASSNAME + "-time");
    }

    private void buildTime(boolean redraw) {
        final boolean thc = datefield.getDateTimeService().isTwelveHourClock();
        if (redraw) {
            clear();
            final int numHours = thc ? 12 : 24;
            hours = new ListBox();
            hours.setStyleName(VNativeSelect.CLASSNAME);
            for (int i = 0; i < numHours; i++) {
                hours.addItem((i < 10) ? "0" + i : "" + i);
            }
            hours.addChangeListener(this);
            if (thc) {
                ampm = new ListBox();
                ampm.setStyleName(VNativeSelect.CLASSNAME);
                final String[] ampmText = datefield.getDateTimeService()
                        .getAmPmStrings();
                ampm.addItem(ampmText[0]);
                ampm.addItem(ampmText[1]);
                ampm.addChangeListener(this);
            }

            if (datefield.getCurrentResolution() >= VDateField.RESOLUTION_MIN) {
                mins = new ListBox();
                mins.setStyleName(VNativeSelect.CLASSNAME);
                for (int i = 0; i < 60; i++) {
                    mins.addItem((i < 10) ? "0" + i : "" + i);
                }
                mins.addChangeListener(this);
            }
            if (datefield.getCurrentResolution() >= VDateField.RESOLUTION_SEC) {
                sec = new ListBox();
                sec.setStyleName(VNativeSelect.CLASSNAME);
                for (int i = 0; i < 60; i++) {
                    sec.addItem((i < 10) ? "0" + i : "" + i);
                }
                sec.addChangeListener(this);
            }
            if (datefield.getCurrentResolution() == VDateField.RESOLUTION_MSEC) {
                msec = new ListBox();
                msec.setStyleName(VNativeSelect.CLASSNAME);
                for (int i = 0; i < 1000; i++) {
                    if (i < 10) {
                        msec.addItem("00" + i);
                    } else if (i < 100) {
                        msec.addItem("0" + i);
                    } else {
                        msec.addItem("" + i);
                    }
                }
                msec.addChangeListener(this);
            }

            final String delimiter = datefield.getDateTimeService()
                    .getClockDelimeter();
            final boolean ro = datefield.isReadonly();

            if (ro) {
                int h = 0;
                if (datefield.getCurrentDate() != null) {
                    h = datefield.getCurrentDate().getHours();
                }
                if (thc) {
                    h -= h < 12 ? 0 : 12;
                }
                add(new VLabel(h < 10 ? "0" + h : "" + h));
            } else {
                add(hours);
            }

            if (datefield.getCurrentResolution() >= VDateField.RESOLUTION_MIN) {
                add(new VLabel(delimiter));
                if (ro) {
                    final int m = mins.getSelectedIndex();
                    add(new VLabel(m < 10 ? "0" + m : "" + m));
                } else {
                    add(mins);
                }
            }
            if (datefield.getCurrentResolution() >= VDateField.RESOLUTION_SEC) {
                add(new VLabel(delimiter));
                if (ro) {
                    final int s = sec.getSelectedIndex();
                    add(new VLabel(s < 10 ? "0" + s : "" + s));
                } else {
                    add(sec);
                }
            }
            if (datefield.getCurrentResolution() == VDateField.RESOLUTION_MSEC) {
                add(new VLabel("."));
                if (ro) {
                    final int m = datefield.getMilliseconds();
                    final String ms = m < 100 ? "0" + m : "" + m;
                    add(new VLabel(m < 10 ? "0" + ms : ms));
                } else {
                    add(msec);
                }
            }
            if (datefield.getCurrentResolution() == VDateField.RESOLUTION_HOUR) {
                add(new VLabel(delimiter + "00")); // o'clock
            }
            if (thc) {
                add(new VLabel("&nbsp;"));
                if (ro) {
                    add(new VLabel(ampm.getItemText(datefield.getCurrentDate()
                            .getHours() < 12 ? 0 : 1)));
                } else {
                    add(ampm);
                }
            }

            if (ro) {
                return;
            }
        }

        // Update times
        Date cdate = datefield.getCurrentDate();
        boolean selected = true;
        if (cdate == null) {
            cdate = new Date();
            selected = false;
        }
        if (thc) {
            int h = cdate.getHours();
            ampm.setSelectedIndex(h < 12 ? 0 : 1);
            h -= ampm.getSelectedIndex() * 12;
            hours.setSelectedIndex(h);
        } else {
            hours.setSelectedIndex(cdate.getHours());
        }
        if (datefield.getCurrentResolution() >= VDateField.RESOLUTION_MIN) {
            mins.setSelectedIndex(cdate.getMinutes());
        }
        if (datefield.getCurrentResolution() >= VDateField.RESOLUTION_SEC) {
            sec.setSelectedIndex(cdate.getSeconds());
        }
        if (datefield.getCurrentResolution() == VDateField.RESOLUTION_MSEC) {
            if (selected) {
                msec.setSelectedIndex(datefield.getMilliseconds());
            } else {
                msec.setSelectedIndex(0);
            }
        }
        if (thc) {
            ampm.setSelectedIndex(cdate.getHours() < 12 ? 0 : 1);
        }

        if (datefield.isReadonly() && !redraw) {
            // Do complete redraw when in read-only status
            clear();
            final String delimiter = datefield.getDateTimeService()
                    .getClockDelimeter();

            int h = cdate.getHours();
            if (thc) {
                h -= h < 12 ? 0 : 12;
            }
            add(new VLabel(h < 10 ? "0" + h : "" + h));

            if (datefield.getCurrentResolution() >= VDateField.RESOLUTION_MIN) {
                add(new VLabel(delimiter));
                final int m = mins.getSelectedIndex();
                add(new VLabel(m < 10 ? "0" + m : "" + m));
            }
            if (datefield.getCurrentResolution() >= VDateField.RESOLUTION_SEC) {
                add(new VLabel(delimiter));
                final int s = sec.getSelectedIndex();
                add(new VLabel(s < 10 ? "0" + s : "" + s));
            }
            if (datefield.getCurrentResolution() == VDateField.RESOLUTION_MSEC) {
                add(new VLabel("."));
                final int m = datefield.getMilliseconds();
                final String ms = m < 100 ? "0" + m : "" + m;
                add(new VLabel(m < 10 ? "0" + ms : ms));
            }
            if (datefield.getCurrentResolution() == VDateField.RESOLUTION_HOUR) {
                add(new VLabel(delimiter + "00")); // o'clock
            }
            if (thc) {
                add(new VLabel("&nbsp;"));
                add(new VLabel(ampm.getItemText(cdate.getHours() < 12 ? 0 : 1)));
            }
        }

        final boolean enabled = datefield.isEnabled();
        hours.setEnabled(enabled);
        if (mins != null) {
            mins.setEnabled(enabled);
        }
        if (sec != null) {
            sec.setEnabled(enabled);
        }
        if (msec != null) {
            msec.setEnabled(enabled);
        }
        if (ampm != null) {
            ampm.setEnabled(enabled);
        }

    }

    public void updateTime(boolean redraw) {
        buildTime(redraw || resolution != datefield.getCurrentResolution()
                || readonly != datefield.isReadonly());
        if (datefield instanceof VTextualDate) {
            ((VTextualDate) datefield).buildDate();
        }
        resolution = datefield.getCurrentResolution();
        readonly = datefield.isReadonly();
    }

    public void onChange(Widget sender) {
        if (datefield.getCurrentDate() == null) {
            // was null on server, need to set
            Date now = datefield.getShowingDate();
            if (now == null) {
                now = new Date();
                datefield.setShowingDate(now);
            }
            datefield.setCurrentDate(new Date(now.getTime()));

            // Init variables with current time
            datefield.getClient().updateVariable(datefield.getId(), "year",
                    now.getYear() + 1900, false);
            datefield.getClient().updateVariable(datefield.getId(), "month",
                    now.getMonth() + 1, false);
            datefield.getClient().updateVariable(datefield.getId(), "day",
                    now.getDate(), false);
            datefield.getClient().updateVariable(datefield.getId(), "hour",
                    now.getHours(), false);
            datefield.getClient().updateVariable(datefield.getId(), "min",
                    now.getMinutes(), false);
            datefield.getClient().updateVariable(datefield.getId(), "sec",
                    now.getSeconds(), false);
            datefield.getClient().updateVariable(datefield.getId(), "msec",
                    datefield.getMilliseconds(), false);
        }
        if (sender == hours) {
            int h = hours.getSelectedIndex();
            if (datefield.getDateTimeService().isTwelveHourClock()) {
                h = h + ampm.getSelectedIndex() * 12;
            }
            datefield.getCurrentDate().setHours(h);
            datefield.getShowingDate().setHours(h);
            datefield.getClient().updateVariable(datefield.getId(), "hour", h,
                    datefield.isImmediate());
            updateTime(false);
        } else if (sender == mins) {
            final int m = mins.getSelectedIndex();
            datefield.getCurrentDate().setMinutes(m);
            datefield.getShowingDate().setMinutes(m);
            datefield.getClient().updateVariable(datefield.getId(), "min", m,
                    datefield.isImmediate());
            updateTime(false);
        } else if (sender == sec) {
            final int s = sec.getSelectedIndex();
            datefield.getCurrentDate().setSeconds(s);
            datefield.getShowingDate().setSeconds(s);
            datefield.getClient().updateVariable(datefield.getId(), "sec", s,
                    datefield.isImmediate());
            updateTime(false);
        } else if (sender == msec) {
            final int ms = msec.getSelectedIndex();
            datefield.setMilliseconds(ms);
            datefield.setShowingMilliseconds(ms);
            datefield.getClient().updateVariable(datefield.getId(), "msec", ms,
                    datefield.isImmediate());
            updateTime(false);
        } else if (sender == ampm) {
            final int h = hours.getSelectedIndex() + ampm.getSelectedIndex()
                    * 12;
            datefield.getCurrentDate().setHours(h);
            datefield.getShowingDate().setHours(h);
            datefield.getClient().updateVariable(datefield.getId(), "hour", h,
                    datefield.isImmediate());
            updateTime(false);
        }
    }

}