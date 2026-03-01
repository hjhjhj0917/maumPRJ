package com.example.example.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {

    public static String getDateTime(String fm) {

        Date today = new Date();
        SimpleDateFormat date = new SimpleDateFormat(fm);

        return date.format(today);
    }

    public static String getDateTime() {
        return getDateTime("yyyy.MM.dd");

    }

    public static String getLongDateTime(Object time) {

        return getLongDateTime(time, "yyyy-MM-dd HH:mm:ss");
    }

    public static String getLongDateTime(Integer time) {

        return getLongDateTime(time, "yyyy-MM-dd HH:mm:ss");
    }

    public static String getLongDateTime(Object time, String fm) {
        return getLongDateTime((Integer) time, fm);

    }

    public static String getLongDateTime(Integer time, String fm) {
        Instant instant = Instant.ofEpochSecond(time);
        return DateTimeFormatter.ofPattern(fm)
                .withZone(ZoneId.systemDefault())
                .format(instant);

    }


}
