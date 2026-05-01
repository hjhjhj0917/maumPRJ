package com.example.maum.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
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

    // String을 파싱해서 LocalDate 객체로 변환
    public static LocalDate parseLocalDate(String dateStr, String fm) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(fm));
    }

    // LocalDate 객체를 특정 포맷의 String으로 변환
    public static String formatLocalDate(LocalDate localDate, String fm) {
        return localDate.format(DateTimeFormatter.ofPattern(fm));
    }

    public static String getLongDateTime(Integer time, String fm) {
        Instant instant = Instant.ofEpochSecond(time);
        return DateTimeFormatter.ofPattern(fm)
                .withZone(ZoneId.systemDefault())
                .format(instant);

    }


}
