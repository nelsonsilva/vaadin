/* 
@ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.terminal.gwt.client;

import java.util.Date;

/**
 * This class provides date/time parsing services to all components on the
 * client side.
 * 
 * @author IT Mill Ltd.
 * 
 */
@SuppressWarnings("deprecation")
public class DateTimeService {
    public static int RESOLUTION_YEAR = 0;
    public static int RESOLUTION_MONTH = 1;
    public static int RESOLUTION_DAY = 2;
    public static int RESOLUTION_HOUR = 3;
    public static int RESOLUTION_MIN = 4;
    public static int RESOLUTION_SEC = 5;
    public static int RESOLUTION_MSEC = 6;

    private String currentLocale;

    private static int[] maxDaysInMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30,
            31, 30, 31 };

    /**
     * Creates a new date time service with the application default locale.
     */
    public DateTimeService() {
        currentLocale = LocaleService.getDefaultLocale();
    }

    /**
     * Creates a new date time service with a given locale.
     * 
     * @param locale
     *            e.g. fi, en etc.
     * @throws LocaleNotLoadedException
     */
    public DateTimeService(String locale) throws LocaleNotLoadedException {
        setLocale(locale);
    }

    public void setLocale(String locale) throws LocaleNotLoadedException {
        if (LocaleService.getAvailableLocales().contains(locale)) {
            currentLocale = locale;
        } else {
            throw new LocaleNotLoadedException(locale);
        }
    }

    public String getLocale() {
        return currentLocale;
    }

    public String getMonth(int month) {
        try {
            return LocaleService.getMonthNames(currentLocale)[month];
        } catch (final LocaleNotLoadedException e) {
            ClientExceptionHandler.displayError(e);
        }
        return null;
    }

    public String getShortMonth(int month) {
        try {
            return LocaleService.getShortMonthNames(currentLocale)[month];
        } catch (final LocaleNotLoadedException e) {
            ClientExceptionHandler.displayError(e);
        }
        return null;
    }

    public String getDay(int day) {
        try {
            return LocaleService.getDayNames(currentLocale)[day];
        } catch (final LocaleNotLoadedException e) {
            ClientExceptionHandler.displayError(e);
        }
        return null;
    }

    public String getShortDay(int day) {
        try {
            return LocaleService.getShortDayNames(currentLocale)[day];
        } catch (final LocaleNotLoadedException e) {
            ClientExceptionHandler.displayError(e);
        }
        return null;
    }

    public int getFirstDayOfWeek() {
        try {
            return LocaleService.getFirstDayOfWeek(currentLocale);
        } catch (final LocaleNotLoadedException e) {
            ClientExceptionHandler.displayError(e);
        }
        return 0;
    }

    public boolean isTwelveHourClock() {
        try {
            return LocaleService.isTwelveHourClock(currentLocale);
        } catch (final LocaleNotLoadedException e) {
            ClientExceptionHandler.displayError(e);
        }
        return false;
    }

    public String getClockDelimeter() {
        try {
            return LocaleService.getClockDelimiter(currentLocale);
        } catch (final LocaleNotLoadedException e) {
            ClientExceptionHandler.displayError(e);
        }
        return ":";
    }

    public String[] getAmPmStrings() {
        try {
            return LocaleService.getAmPmStrings(currentLocale);
        } catch (final LocaleNotLoadedException e) {
            ClientExceptionHandler.displayError(e);
        }
        final String[] temp = new String[2];
        temp[0] = "AM";
        temp[1] = "PM";
        return temp;
    }

    public int getStartWeekDay(Date date) {
        final Date dateForFirstOfThisMonth = new Date(date.getYear(), date
                .getMonth(), 1);
        int firstDay;
        try {
            firstDay = LocaleService.getFirstDayOfWeek(currentLocale);
        } catch (final LocaleNotLoadedException e) {
            firstDay = 0;
            ClientExceptionHandler.displayError(e);
        }
        int start = dateForFirstOfThisMonth.getDay() - firstDay;
        if (start < 0) {
            start = 6;
        }
        return start;
    }

    public static int getNumberOfDaysInMonth(Date date) {
        final int month = date.getMonth();
        if (month == 1 && true == isLeapYear(date)) {
            return 29;
        }
        return maxDaysInMonth[month];
    }

    public static boolean isLeapYear(Date date) {
        // Instantiate the date for 1st March of that year
        final Date firstMarch = new Date(date.getYear(), 2, 1);

        // Go back 1 day
        final long firstMarchTime = firstMarch.getTime();
        final long lastDayTimeFeb = firstMarchTime - (24 * 60 * 60 * 1000); // NUM_MILLISECS_A_DAY

        // Instantiate new Date with this time
        final Date febLastDay = new Date(lastDayTimeFeb);

        // Check for date in this new instance
        return (29 == febLastDay.getDate()) ? true : false;
    }

    public static boolean isSameDay(Date d1, Date d2) {
        return (getDayInt(d1) == getDayInt(d2));
    }

    public static boolean isInRange(Date date, Date rangeStart, Date rangeEnd,
            int resolution) {
        Date s;
        Date e;
        if (rangeStart.after(rangeEnd)) {
            s = rangeEnd;
            e = rangeStart;
        } else {
            e = rangeEnd;
            s = rangeStart;
        }
        long start = s.getYear() * 10000000000l;
        long end = e.getYear() * 10000000000l;
        long target = date.getYear() * 10000000000l;

        if (resolution == RESOLUTION_YEAR) {
            return (start <= target && end >= target);
        }
        start += s.getMonth() * 100000000l;
        end += e.getMonth() * 100000000l;
        target += date.getMonth() * 100000000l;
        if (resolution == RESOLUTION_MONTH) {
            return (start <= target && end >= target);
        }
        start += s.getDate() * 1000000;
        end += e.getDate() * 1000000;
        target += date.getDate() * 1000000;
        if (resolution == RESOLUTION_DAY) {
            return (start <= target && end >= target);
        }
        start += s.getHours() * 10000;
        end += e.getHours() * 10000;
        target += date.getHours() * 10000;
        if (resolution == RESOLUTION_HOUR) {
            return (start <= target && end >= target);
        }
        start += s.getMinutes() * 100;
        end += e.getMinutes() * 100;
        target += date.getMinutes() * 100;
        if (resolution == RESOLUTION_MIN) {
            return (start <= target && end >= target);
        }
        start += s.getSeconds();
        end += e.getSeconds();
        target += date.getSeconds();
        return (start <= target && end >= target);

    }

    private static int getDayInt(Date date) {
        final int y = date.getYear();
        final int m = date.getMonth();
        final int d = date.getDate();

        return ((y + 1900) * 10000 + m * 100 + d) * 1000000000;
    }

    /**
     * Returns the ISO-8601 week number of the given date.
     * 
     * @param date
     *            The date for which the week number should be resolved
     * @return The ISO-8601 week number for {@literal date}
     */
    public static int getISOWeekNumber(Date date) {
        final long MILLISECONDS_PER_DAY = 24 * 3600 * 1000;
        int dayOfWeek = date.getDay(); // 0 == sunday

        // ISO 8601 use weeks that start on monday so we use
        // mon=1,tue=2,...sun=7;
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        // Find nearest thursday (defines the week in ISO 8601). The week number
        // for the nearest thursday is the same as for the target date.
        int nearestThursdayDiff = 4 - dayOfWeek; // 4 is thursday
        Date nearestThursday = new Date(date.getTime() + nearestThursdayDiff
                * MILLISECONDS_PER_DAY);

        Date firstOfJanuary = new Date(nearestThursday.getYear(), 0, 1);
        long timeDiff = nearestThursday.getTime() - firstOfJanuary.getTime();
        int daysSinceFirstOfJanuary = (int) (timeDiff / MILLISECONDS_PER_DAY);

        int weekNumber = (daysSinceFirstOfJanuary) / 7 + 1;

        return weekNumber;
    }

}
