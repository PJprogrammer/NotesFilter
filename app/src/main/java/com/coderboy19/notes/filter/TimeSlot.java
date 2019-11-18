package com.coderboy19.notes.filter;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by notes on 7/5/2017.
 */

public class TimeSlot implements Serializable{
    Date startTime;
    Date endTime;

    public TimeSlot () {
        startTime = new Date();
        endTime = new Date();
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(int hour,int min) {
        this.startTime.setHours(hour);
        this.startTime.setMinutes(min);
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date compareEndTime(Context context) {
        Date currentTime = Calendar.getInstance().getTime();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String timeChoice = sharedPreferences.getString("notificationTime","2");
        int minutesNumber = Integer.parseInt(timeChoice) * -1;

        Calendar classEndTime = Calendar.getInstance();
        classEndTime.setTime(endTime);
        classEndTime.add(Calendar.MINUTE,minutesNumber);
        Date notifyTime = classEndTime.getTime();

        if(currentTime.before(notifyTime)) {
            return notifyTime;
        } else {
            return null;
        }
    }

    public void setEndTime(int hour,int min) {
        this.endTime.setHours(hour);
        this.endTime.setMinutes(min);
    }

    public Boolean compareTo(Date other) {
        if(other.after(startTime)) {
            if(other.before(endTime))
                return true;
        }
        else{
            return false;
        }
        return false;
    }

    @Override
    public String toString() {
        DateFormat dateFormat = new SimpleDateFormat("h:mm a");
        return dateFormat.format(startTime) + " - " + dateFormat.format(endTime);
    }
}
