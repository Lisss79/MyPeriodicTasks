package com.lisss79.android.myperiodictasks;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;

public class DataSerialize implements Serializable {
    private static final long serialVersionUID = 1L; // самое важное!
    private ArrayList<String> mTasksList = new ArrayList<>();
    private ArrayList<LocalDate> mDateList = new ArrayList<>();
    private ArrayList<Integer> mPeriodList = new ArrayList<>();
    private ArrayList<Boolean> mLengthPeriodList = new ArrayList<>();
    private ArrayList<Boolean> isActive = new ArrayList<>();
    private int notification_hour;

    public DataSerialize(ArrayList<String> mTasksList, ArrayList<LocalDate> mDateList,
                         ArrayList<Integer> mPeriodList, ArrayList<Boolean> mLengthPeriodList,
                         ArrayList<Boolean> isActive, int notification_hour) {
        this.mTasksList = mTasksList;
        this.mDateList = mDateList;
        this.mPeriodList = mPeriodList;
        this.mLengthPeriodList = mLengthPeriodList;
        this.isActive = isActive;
        this.notification_hour = notification_hour;
    }
    public ArrayList<String> getTasksList() {
        return mTasksList;
    }

    public ArrayList<LocalDate> getDateList() {
        return mDateList;
    }

    public ArrayList<Integer> getPeriodList() {
        return mPeriodList;
    }

    public ArrayList<Boolean> getLengthPeriodList() {
        return mLengthPeriodList;
    }

    public ArrayList<Boolean> getIsActive() {
        return isActive;
    }

    public int getNotification_hour() {
        return notification_hour;
    }
}
