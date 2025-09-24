package com.company.crm.report;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Date;

public class ReportUtils {

    public static String formatDateTime(Object date, String format) {
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
}
