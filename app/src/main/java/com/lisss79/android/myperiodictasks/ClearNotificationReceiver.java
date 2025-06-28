package com.lisss79.android.myperiodictasks;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ClearNotificationReceiver extends BroadcastReceiver {
    public ArrayList<String> mTasksList = new ArrayList<>();// название задачи
    public ArrayList<LocalDate> mDateList = new ArrayList<>(); // ближайшая дата
    public ArrayList<Integer> mPeriodList = new ArrayList<>(); // период между уведомлениями
    public ArrayList<Boolean> mLengthPeriodList = new ArrayList<>(); // true - месяцев, false - дней
    public ArrayList<Boolean> isActive = new ArrayList<>();
    LoadAndSaveData loadAndSaveData; // класс с методами загрузки/сохранения
    LocalDate today;
    int notification_hour;
    int colorPrimary;
    private StatusBarNotification[] notifications;
    NotificationManager mNotifyManager;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    boolean shiftDate = false; // сдвигать ли дату при подтверждении

    @Override
    public void onReceive(Context context, Intent intent) {
        //уточняем id и нужно ли сдвигать дату
        int NOTIFICATION_ID = intent.getIntExtra("NOTIFICATION_ID", 0);
        //System.out.println(NOTIFICATION_ID);
        if(NOTIFICATION_ID >= 100) {
            shiftDate = true;
            NOTIFICATION_ID -= 100;
        }
        else {
            shiftDate = false;
        }

        notification_hour = intent.getIntExtra("NOTIFICATION_HOUR", 14);
        colorPrimary = intent.getIntExtra("COLOR_PRIMARY",
                Integer.parseInt("BB86FC", 16));
        loadAndSaveData = new LoadAndSaveData(context, context.getFilesDir(),
                notification_hour, colorPrimary);
        loadData(); // загрузить данные
        LocalDate currDate = mDateList.get(NOTIFICATION_ID);
        Integer currPeriod = mPeriodList.get(NOTIFICATION_ID);
        Boolean currLengthPeriod = mLengthPeriodList.get(NOTIFICATION_ID);
        LocalDate nextDate = lookForNextDate(currDate, currPeriod, currLengthPeriod, shiftDate); // следующая дата
        String nextDateString = nextDate.format(formatter);
        mDateList.set(NOTIFICATION_ID, nextDate); // сохранить новую дату

        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifications = mNotifyManager.getActiveNotifications();
        mNotifyManager.cancel(NOTIFICATION_ID);
        saveData(); // сохранить данные
        MainActivity mainActivity = App.getInstance().getMainActivity();
        if (mainActivity != null) mainActivity.recreate();
        else {
            Toast.makeText(context, "Новая дата задачи: " + nextDateString, Toast.LENGTH_LONG).show();
            loadAndSaveData.setDailyAlarm();
        }
    }

    // задание выполнено, найти следующую дату уведомления
    private LocalDate lookForNextDate(LocalDate currDate, Integer currPeriod,
                                      boolean currLengthPeriod, boolean shift) {
        LocalDate nextDate, firstDate;

        if(shift) firstDate = LocalDate.now();
        else firstDate = currDate;

        if (currLengthPeriod) {
            nextDate = firstDate.plusMonths(currPeriod);
        } else {
            nextDate = firstDate.plusDays(currPeriod);
        }
        return nextDate;
    }

    public void loadData() {
        if (loadAndSaveData.loadData()) {
            mTasksList = loadAndSaveData.mTasksList;
            mDateList = loadAndSaveData.mDateList;
            mPeriodList = loadAndSaveData.mPeriodList;
            mLengthPeriodList = loadAndSaveData.mLengthPeriodList;
            isActive = loadAndSaveData.isActive;
        }
    }

    public void saveData() {
        loadAndSaveData.mTasksList = mTasksList;
        loadAndSaveData.mDateList = mDateList;
        loadAndSaveData.mPeriodList = mPeriodList;
        loadAndSaveData.mLengthPeriodList = mLengthPeriodList;
        loadAndSaveData.isActive = isActive;
        loadAndSaveData.saveData();
    }
}