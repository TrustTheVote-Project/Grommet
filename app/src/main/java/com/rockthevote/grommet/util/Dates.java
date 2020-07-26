package com.rockthevote.grommet.util;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Dates {

    private static final String TIME_ZONE_UTC = "UTC";
    private static final String SHORT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_OF_DAY_FORMAT = "MMM d, h:mm a";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm'Z'";

    public static Date parseISO8601_Date(String date) {
        if (Strings.isBlank(date)) {
            return null;
        }

        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_UTC));


        Date ret = new Date();
        try {
            ret = df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static String formatAsISO8601_Date(Date date) {
        if(null == date){
            return "";
        }

        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_UTC));

        return df.format(date);
    }

    @Nullable
    public static Date parseISO8601_ShortDate(String date){
        if(Strings.isBlank(date)){
            return null;
        }

        DateFormat df = new SimpleDateFormat(SHORT_DATE_FORMAT);

        Date ret = new Date();
        try {
            ret = df.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return ret;
    }


    public static String formatAsISO8601_ShortDate(Date date) {
        if(null == date){
            return "";
        }

        DateFormat df = new SimpleDateFormat(SHORT_DATE_FORMAT);
        df.setTimeZone(TimeZone.getTimeZone(TIME_ZONE_UTC));

        return df.format(date);
    }

    public static String formatAs_LocalTimeOfDay(Date date){
        if(null == date){
            return "";
        }

        DateFormat df = new SimpleDateFormat(TIME_OF_DAY_FORMAT);
        return df.format(date);
    }


}
