package com.lisss79.android.myperiodictasks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

public class LoadAndSaveData {
    public ArrayList<String> mTasksList = new ArrayList<>();// название задачи
    public ArrayList<LocalDate> mDateList = new ArrayList<>(); // ближайшая дата
    public ArrayList<Integer> mPeriodList = new ArrayList<>(); // период между уведомлениями
    public ArrayList<Boolean> mLengthPeriodList = new ArrayList<>(); // true - месяцев, false - дней
    public ArrayList<Boolean> isActive = new ArrayList<>();
    private File dataDir;
    private Context context;
    public int notification_hour;
    private int colorPrimary;

    public LoadAndSaveData(Context context, File dataDir, int notification_hour, int colorPrimary) {
        this.context = context;
        this.dataDir = dataDir;
        this.notification_hour = notification_hour;
        this.colorPrimary = colorPrimary;
    }

    public boolean saveData() {
        boolean result = false;
        DataSerialize dataSerialize = new DataSerialize(mTasksList, mDateList,
                mPeriodList, mLengthPeriodList, isActive, notification_hour);
        FileOutputStream fos = null;
        File file = new File(dataDir + "/taskslist");
        try {
            fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(dataSerialize);
            oos.close();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
            showDialogIOError();
        }
        return result;
    }

    public boolean loadData() {
        boolean result = false;
        FileInputStream fis = null;
        File file = new File(dataDir + "/taskslist");
        try {
            fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            DataSerialize dataSerialize = (DataSerialize) ois.readObject();
            ois.close();
            mTasksList = dataSerialize.getTasksList();
            mDateList = dataSerialize.getDateList();
            mPeriodList = dataSerialize.getPeriodList();
            mLengthPeriodList = dataSerialize.getLengthPeriodList();
            isActive = dataSerialize.getIsActive();
            notification_hour = dataSerialize.getNotification_hour();
            result = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showDialogNoFile();
            result = false;
        } catch (IOException e) {
            e.printStackTrace();
            showDialogIOError();
            result = false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public void showDialogNoFile() { // окно с сообщением "нет файла с данными"
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Файл со списком задач не найден. Создайте задачи, и они будут сохранены в новом файле."); // сообщение в окне
        builder.setTitle("Предупреждение"); // заголовок окна
        builder.setIcon(R.drawable.ic_warning); // иконка в заголовке
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() { //текст на кнопке
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.show();
    }

    public void showDialogIOError() { // окно с сообщением "ошибка ввода/вывода"
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Операция чтения/записи данных не выполнена"); // сообщение в окне
        builder.setTitle("Ошибка"); // заголовок окна
        builder.setIcon(R.drawable.ic_error); // иконка в заголовке
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() { //текст на кнопке
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.show();
    }

    // включение ежедневной рассылки
    public void setDailyAlarm() {
        long interval;
        Intent alarmIntent = new Intent(context, DailyReceiver.class); // класс приемника рассылки
        alarmIntent = putExtraInIntent(alarmIntent);
        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, 0,
                alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        boolean isAlarmActive = PendingIntent.getBroadcast(context, 0,
                alarmIntent, PendingIntent.FLAG_NO_CREATE) != null;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        int second = calendar.get(Calendar.SECOND);
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if(notification_hour < 100) {
            if (calendar.get(Calendar.HOUR_OF_DAY) >= notification_hour) {
                calendar.add(Calendar.DAY_OF_MONTH, +1);
            }
            calendar.set(Calendar.HOUR_OF_DAY, notification_hour);
            interval = AlarmManager.INTERVAL_DAY;
        }
        else {
            calendar.set(Calendar.MINUTE, minute);
            calendar.add(Calendar.MINUTE, +2);
            interval = 120000;
        }

        //calendar.set(Calendar.MINUTE, minute);
        //calendar.set(Calendar.SECOND, second);
        //calendar.add(Calendar.SECOND, +20);

        //alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmPendingIntent);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy в HH:mm");
        String dateTime = "Следующая проверка напоминаний: " + simpleDateFormat.format(calendar.getTime());
        Toast.makeText(context, dateTime, Toast.LENGTH_LONG).show();
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), interval, alarmPendingIntent);
    }

    // создание Intent для рассылки на приемник Daily Receiver
    public Intent putExtraInIntent(Intent alarmIntent) {
        alarmIntent.putExtra("TASKS_LIST", mTasksList);
        alarmIntent.putExtra("DATE_LIST", mDateList);
        alarmIntent.putExtra("PERIOD_LIST", mPeriodList);
        alarmIntent.putExtra("LENGTH_PERIOD_LIST", mLengthPeriodList);
        alarmIntent.putExtra("IS_ACTIVE", isActive);
        alarmIntent.putExtra("NOTIFICATION_HOUR", notification_hour);
        alarmIntent.putExtra("SIZE", mTasksList.size());
        alarmIntent.putExtra("COLOR_PRIMARY", colorPrimary);
        return alarmIntent;
    }
}
