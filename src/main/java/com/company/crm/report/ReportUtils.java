package com.company.crm.report;

import org.springframework.lang.Nullable;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Map;

public class ReportUtils {

    @SuppressWarnings("unchecked")
    public static <T> T getParam(Map<String, Object> params, String paramName) {
        return (T) params.get(paramName);
    }

    public static String formatDateTime(@Nullable Object date, String format) {
        if (date == null) {
            return "";
        }
        if (date instanceof Date) {
            return new SimpleDateFormat(format).format(date);
        }
        if (date instanceof Temporal temporal) {
            return DateTimeFormatter.ofPattern(format).format(temporal);
        }
        return date.toString();
    }

    public static Date getFirstDayOfMonth(Object date) {
        if (!(date instanceof Date)) {
            throw new IllegalArgumentException("date parameter must be of type java.util.Date");
        }
        LocalDate localDate = dateToLocalDate((Date) date);
        return localDateToDate(localDate.with(TemporalAdjusters.firstDayOfMonth()));
    }

    public static Date getLastDayOfMonth(Object date) {
        if (!(date instanceof Date)) {
            throw new IllegalArgumentException("date parameter must be of type java.util.Date");
        }
        LocalDate localDate = dateToLocalDate((Date) date);
        return localDateToDate(localDate.with(TemporalAdjusters.lastDayOfMonth()));
    }

    public static LocalDate dateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date localDateToDate(LocalDate lastDayOfMonth) {
        return Date.from(lastDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
