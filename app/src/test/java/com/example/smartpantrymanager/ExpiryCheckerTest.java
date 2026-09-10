package com.example.smartpantrymanager;

import static org.junit.Assert.assertEquals;

import com.example.smartpantrymanager.logic.ExpiryChecker;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExpiryCheckerTest {

    @Test
    public void pastDateIsExpired() throws ParseException {
        assertEquals(ExpiryChecker.ExpiryStatus.EXPIRED,
                ExpiryChecker.getStatus("09/09/2026", 7, date("10/09/2026")));
    }

    @Test
    public void dateInsideWarningPeriodIsExpiringSoon() throws ParseException {
        assertEquals(ExpiryChecker.ExpiryStatus.EXPIRING_SOON,
                ExpiryChecker.getStatus("15/09/2026", 7, date("10/09/2026")));
    }

    @Test
    public void dateOutsideWarningPeriodIsFresh() throws ParseException {
        assertEquals(ExpiryChecker.ExpiryStatus.FRESH,
                ExpiryChecker.getStatus("30/09/2026", 7, date("10/09/2026")));
    }

    @Test
    public void invalidDateIsReported() throws ParseException {
        assertEquals(ExpiryChecker.ExpiryStatus.INVALID,
                ExpiryChecker.getStatus("31/02/2026", 7, date("10/09/2026")));
    }

    private Date date(String value) throws ParseException {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).parse(value);
    }
}
