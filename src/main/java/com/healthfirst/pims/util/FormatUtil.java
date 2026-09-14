package com.healthfirst.pims.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Central display formats keep prices and dates consistent throughout the user interface. */
public final class FormatUtil {
    private static final NumberFormat ZAR = NumberFormat.getCurrencyInstance(Locale.of("en", "ZA"));
    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM uuuu");
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm");
    private FormatUtil() { }
    public static String money(BigDecimal value) { return ZAR.format(value == null ? BigDecimal.ZERO : value); }
    public static String date(LocalDate value) { return value == null ? "" : DATE.format(value); }
    public static String dateTime(LocalDateTime value) { return value == null ? "" : DATE_TIME.format(value); }
}
