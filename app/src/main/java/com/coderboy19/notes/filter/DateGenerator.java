package com.coderboy19.notes.filter;

import com.coderboy19.notes.filter.SchoolClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
public class DateGenerator {

    public static SchoolClass getClass(ArrayList<SchoolClass> classes) {
        Date now = Calendar.getInstance().getTime();
        SchoolClass currentClass = null;

        for(SchoolClass schoolClass : classes) {
            if(schoolClass.getClassTime().compareTo(now)){
                currentClass = schoolClass;
                break;
            }
        }

        return currentClass;
    }

    public static ArrayList<String> getMonths() {
        return new ArrayList<>(Arrays.asList("January","February","March","April","May","June","July","August","September","October","November","December"));
    }

    public static ArrayList<String> getWeeks(int month, int year) {
        ArrayList<String> weeks = new ArrayList<String>();

        Calendar c = Calendar.getInstance(new Locale("en", "US"));
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, (month-1));
        c.set(Calendar.DAY_OF_MONTH, 1);

        int i = 1;
        String weekDate ="Week " + i + " (" + (c.get(Calendar.MONTH)+1) + "/" + c.get(Calendar.DAY_OF_MONTH) + " - ";
        int maximumWeeks  = c.getActualMaximum(Calendar.WEEK_OF_MONTH);

        while (maximumWeeks != weeks.size()) {
            i++;
            while (c.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && c.getActualMaximum(Calendar.DAY_OF_MONTH) != (int)c.get(Calendar.DAY_OF_MONTH)) {
                c.add(Calendar.DAY_OF_MONTH, 1);
            }
            weekDate += (c.get(Calendar.MONTH)+1) + "/" + c.get(Calendar.DAY_OF_MONTH) + ")";
            weeks.add(weekDate);
            c.add(Calendar.DAY_OF_MONTH, 1);
            weekDate = "Week " + i + " (" + (c.get(Calendar.MONTH)+1) + "/" + c.get(Calendar.DAY_OF_MONTH) + " - ";
        }
        return weeks;
    }

    public static ArrayList<ArrayList<String>> getDays(int month, int year) {
        ArrayList<ArrayList<String>> days = new ArrayList<ArrayList<String>>();
        String [] daysofweek = new String[] {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};

        Calendar c = Calendar.getInstance(new Locale("en", "US"));
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month-1);
        c.set(Calendar.DAY_OF_MONTH, 1);

        int maximumWeeks  = c.getActualMaximum(Calendar.WEEK_OF_MONTH);
        int i = 0;

        while (maximumWeeks != days.size()) {
            days.add(new ArrayList<String>());
            while (c.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && c.getActualMaximum(Calendar.DAY_OF_MONTH) != (int)c.get(Calendar.DAY_OF_MONTH)) {
                days.get(i).add(daysofweek[c.get(Calendar.DAY_OF_WEEK)-1]);
                c.add(Calendar.DAY_OF_MONTH, 1);
            }
            days.get(i).add(daysofweek[c.get(Calendar.DAY_OF_WEEK)-1]);
            c.add(Calendar.DAY_OF_MONTH, 1);
            i++;
        }
        return days;
    }
}
