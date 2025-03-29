package com.lisss79.android.myperiodictasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;

public class RestartReceiver extends BroadcastReceiver {
    LoadAndSaveData loadAndSaveData; // класс с методами загрузки/сохранения
    private int colorPrimary = Integer.parseInt("BB86FC", 16); // цвет Color Primary
    private int notification_hour = 14; // час выдачи уведомлений о задачах

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        colorPrimary = typedValue.data;
        colorPrimary = context.getColor(R.color.purple_200);

        loadAndSaveData = new LoadAndSaveData(context, context.getFilesDir(),
                notification_hour, colorPrimary);
        loadAndSaveData.loadData();
        notification_hour = loadAndSaveData.notification_hour;
        loadAndSaveData.setDailyAlarm();

        //Toast.makeText(context, "Напоминания установлены на " + notification_hour + "ч", Toast.LENGTH_LONG).show();
    }

}