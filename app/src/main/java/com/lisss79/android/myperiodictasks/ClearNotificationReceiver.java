package com.lisss79.android.myperiodictasks;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;

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

    @Override
    public void onReceive(Context context, Intent intent) {
        int NOTIFICATION_ID = intent.getIntExtra("NOTIFICATION_ID", 0);
        notification_hour = intent.getIntExtra("NOTIFICATION_HOUR", 14);
        colorPrimary = intent.getIntExtra("COLOR_PRIMARY",
                Integer.parseInt("BB86FC", 16));
        loadAndSaveData = new LoadAndSaveData(context, context.getFilesDir(),
                notification_hour, colorPrimary);
        loadData(); // загрузить данные
        LocalDate currDate = mDateList.get(NOTIFICATION_ID);
        Integer currPeriod = mPeriodList.get(NOTIFICATION_ID);
        Boolean currLengthPeriod = mLengthPeriodList.get(NOTIFICATION_ID);
        LocalDate nextDate = lookForNextDate(currDate, currPeriod, currLengthPeriod); // следующая дата
        mDateList.set(NOTIFICATION_ID, nextDate); // сохранить новую дату
        //Log.w("StoringClass.mNotifyManager = ", String.valueOf(StoringClass.mNotifyManager));
        //Log.w("DailyReceiver.kkk = ", String.valueOf(DailyReceiver.kkk));

        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifications = mNotifyManager.getActiveNotifications();
        mNotifyManager.cancel(NOTIFICATION_ID);
        saveData(); // сохранить данные
        if (((MainActivity) MainActivity.context) != null) {
            ((MainActivity) MainActivity.context).recreate();
        } else {
            loadAndSaveData.setDailyAlarm();
            Toast.makeText(context, "Установлена новая дата задачи", Toast.LENGTH_SHORT).show();
        }
    }

    // задание выполнено, найти следующую дату уведомления
    private LocalDate lookForNextDate(LocalDate currDate, Integer currPeriod, Boolean currLengthPeriod) {
        LocalDate nextDate;
        today = LocalDate.now();
        if (currLengthPeriod) {
            nextDate = today.plusMonths(currPeriod);
        } else {
            nextDate = today.plusDays(currPeriod);
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