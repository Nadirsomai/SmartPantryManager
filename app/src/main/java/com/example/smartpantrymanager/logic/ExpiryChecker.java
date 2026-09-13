package com.example.smartpantrymanager.logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class ExpiryChecker {

    public enum ExpiryStatus {
        FRESH,
        EXPIRING_SOON,
        EXPIRED,
        INVALID
    }

    private ExpiryChecker() {
        // Utility class.
    }

    public static ExpiryStatus getStatus(String expiryDate, int warningDays, Date today) {
        Date parsedExpiryDate = parseDate(expiryDate);
        if (parsedExpiryDate == null) {
            return ExpiryStatus.INVALID;
        }

        // Compare calendar dates from midnight so the current time of day does not
        // incorrectly change the number of days remaining.
        long daysUntilExpiry = daysBetween(startOfDay(today), startOfDay(parsedExpiryDate));
        if (daysUntilExpiry < 0) {
            return ExpiryStatus.EXPIRED;
        }
        if (daysUntilExpiry <= warningDays) {
            return ExpiryStatus.EXPIRING_SOON;
        }
        return ExpiryStatus.FRESH;
    }

    public static long getDaysUntilExpiry(String expiryDate, Date today) {
        Date parsedExpiryDate = parseDate(expiryDate);
        if (parsedExpiryDate == null) {
            return Long.MAX_VALUE;
        }
        return daysBetween(startOfDay(today), startOfDay(parsedExpiryDate));
    }

    // Strict parsing rejects invalid dates instead of silently correcting them.
    private static Date parseDate(String dateText) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
        dateFormat.setLenient(false);
        try {
            return dateFormat.parse(dateText);
        } catch (ParseException exception) {
            return null;
        }
    }

    private static Date startOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static long daysBetween(Date start, Date end) {
        return TimeUnit.MILLISECONDS.toDays(end.getTime() - start.getTime());
    }
}
