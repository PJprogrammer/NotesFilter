package com.coderboy19.notes.filter;

import java.io.Serializable;

/**
 * Created by notes on 7/9/2017.
 */

public class SchoolClass implements Serializable {
    private int imageNumber = 1;
    private int audioNumber = 1;
    private int memoNumber = 1;

    private String className;
    private int periodNumber;
    private TimeSlot classTime;
    private String[] periodList = new String[] { "Period 1", "Period 2", "Period 3", "Period 4",
            "Period 5", "Period 6", "Period 7", "Period 8", "Period 9"};

    public SchoolClass(String className, int periodNumber, TimeSlot classTime) {
        this.className = className;
        this.periodNumber = periodNumber;
        this.classTime = classTime;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getPeriodNumber() {
        return periodNumber;
    }

    public void setPeriodNumber(int periodNumber) {
        this.periodNumber = periodNumber;
    }

    public String getPeriodString() {
        return periodList[periodNumber-1];
    }

    public TimeSlot getClassTime() {
        return classTime;
    }

    public void setClassTime(TimeSlot classTime) {
        this.classTime = classTime;
    }

    public int getImageNumber() {
        imageNumber++;
        return imageNumber-1;

    }

    public int getAudioNumber() {
        audioNumber++;
        return audioNumber-1;
    }

    public int getMemoNumber() {
        memoNumber++;
        return memoNumber-1;
    }
}
