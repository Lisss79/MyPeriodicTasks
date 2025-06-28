package com.lisss79.android.myperiodictasks;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;

import androidx.core.app.NotificationCompat;

import java.time.LocalDate;
import java.util.ArrayList;

public class DailyReceiver extends BroadcastReceiver {
    private ArrayList<String> mTasksList;// название задачи
    private ArrayList<LocalDate> mDateList; // ближайшая дата
    private ArrayList<Integer> mPeriodList; // период между уведомлениями
    private ArrayList<Boolean> mLengthPeriodList; // true - месяцев, false - дней
    private ArrayList<Boolean> isActive;

    private int notification_hour;
    private int[] activeTasks; // задачи, о которых надо сегодня уведомить
    private boolean[] outOfDateTasks; // просрочена ли задача из activeTask
    private int numOfActiveTasks; // количество задача на сегодня
    private int numOutOfDateTasks; // количество просроченных задач
    private String PRIMARY_CHANNEL_ID; // ID канала уведомлений
    public NotificationManager mNotifyManager; // менеджер уведомлений
    private int NOTIFICATION_ID; // ID уведомлений
    private Context context;
    private int colorPrimary;

    private final String mName = "NAME_OF_TASK";
    private final String mDateYear = "DATE_YEAR__OF_TASK";
    private final String mDateMonth = "DATE_MONTH_OF_TASK";
    private final String mDateDay = "DATE_DAY_OF_TASK";
    private final String mPeriod = "PERIOD_OF_TASK";
    private final String mLengthPeriod = "LENGTH_PERIOD_OF_TASK";
    private final String mNotificationId = "NOTIFICATION_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        String currTask;
        boolean outOfDate;
        this.context = context;

        initVars();
        getDataFromIntent(intent);

        lookForTodayTasks();
        createNotificationChannel();

        for(int i = 0; i < numOfActiveTasks; i++) {
            currTask = mTasksList.get(activeTasks[i]);
            outOfDate = outOfDateTasks[i];
            sendNotification(activeTasks[i], currTask, outOfDate);
        }
    }

    // инициализировать переменные
    private void initVars() {
        mTasksList = new ArrayList<>();
        mDateList = new ArrayList<>();
        //mPeriodList = new ArrayList<>();
        //mLengthPeriodList = new ArrayList<>();
        isActive = new ArrayList<>();

        activeTasks = new int[100];
        outOfDateTasks = new boolean[100];
        numOfActiveTasks = 0;
        numOutOfDateTasks = 0;
        PRIMARY_CHANNEL_ID = "primary_notification_channel"; // ID канала уведомлений
        NOTIFICATION_ID = 0; // ID уведомлений
    }

    // поиск активных задач на сегодня
    private void lookForTodayTasks() {
        int numOfActiveTasks = 0;
        int numOutOfDateTasks = 0;
        int compare;
        LocalDate today = LocalDate.now();
        LocalDate taskDate;
        for(int i = 0; i < mTasksList.size(); i++) {
            taskDate = mDateList.get(i);
            compare = today.compareTo(taskDate);
            if(isActive.get(i)) {
                if (compare == 0) {
                    outOfDateTasks[numOfActiveTasks] = false;
                    activeTasks[numOfActiveTasks] = i;
                    numOfActiveTasks++;
                }
                if (compare > 0) {
                    outOfDateTasks[numOfActiveTasks] = true;
                    numOutOfDateTasks++;
                    activeTasks[numOfActiveTasks] = i;
                    numOfActiveTasks++;
                }
            }

            this.numOfActiveTasks = numOfActiveTasks;
            this.numOutOfDateTasks = numOutOfDateTasks;
        }
    }

    // получение массива данных из MainActivity
    private void getDataFromIntent(Intent intent) {
        mTasksList = (ArrayList<String>) intent.getSerializableExtra("TASKS_LIST");
        mDateList = (ArrayList<LocalDate>) intent.getSerializableExtra("DATE_LIST");
        mPeriodList = (ArrayList<Integer>) intent.getSerializableExtra("PERIOD_LIST");
        mLengthPeriodList = (ArrayList<Boolean>) intent.getSerializableExtra("LENGTH_PERIOD_LIST");
        isActive = (ArrayList<Boolean>) intent.getSerializableExtra("IS_ACTIVE");
        notification_hour = intent.getIntExtra("NOTIFICATION_HOUR" ,-1);
        colorPrimary = intent.getIntExtra("COLOR_PRIMARY", -1);
    }

    // создание канала уведомлений
    public void createNotificationChannel() {
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                "Tasks Notification", NotificationManager.IMPORTANCE_HIGH); // имя канала
        notificationChannel.enableLights(true); // использовать светодиод
        notificationChannel.setLightColor(Color.YELLOW); // цвет светодиода
        notificationChannel.enableVibration(true); // использовать вибрацию
        notificationChannel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null);
        notificationChannel.setDescription("Notification about Tasks"); // описание уведомения
        NotificationChannel isChannelCreated = mNotifyManager.getNotificationChannel(PRIMARY_CHANNEL_ID);
        mNotifyManager.createNotificationChannel(notificationChannel); // создание канала
    }

    // создать builder уведомлений
    private NotificationCompat.Builder getNotificationBuilder(int NOTIFICATION_ID, String currTask, boolean outOfDate) {

        LocalDate localDate = mDateList.get(NOTIFICATION_ID);
        Intent clickIntent = new Intent(context, DetailsActivity.class);
        clickIntent.putExtra(mName, mTasksList.get(NOTIFICATION_ID));
        clickIntent.putExtra(mDateYear, localDate.getYear());
        clickIntent.putExtra(mDateMonth, localDate.getMonthValue());
        clickIntent.putExtra(mDateDay, localDate.getDayOfMonth());
        clickIntent.putExtra(mPeriod, mPeriodList.get(NOTIFICATION_ID));
        clickIntent.putExtra(mLengthPeriod, mLengthPeriodList.get(NOTIFICATION_ID));
        clickIntent.putExtra(mNotificationId, NOTIFICATION_ID);
        PendingIntent clickPendingIntent2 = PendingIntent.getActivity(context, 0,
                clickIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // создание intent'а для кнопки "отметить выполненным"
        Intent doneIntent = new Intent(context, ClearNotificationReceiver.class);
        doneIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID);
        doneIntent.putExtra("NOTIFICATION_HOUR", notification_hour);
        doneIntent.putExtra("COLOR_PRIMARY", colorPrimary);
        PendingIntent clickPendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent donePendingIntent;
        String text;
        NotificationCompat.Builder notifyBuilder;

        Bitmap myLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo_jpg);

        // если запланировано на сегодня, показать уведомление с одной кнопкой
        if(!outOfDate) {
            text = "Запланирована на сегодня!";
            donePendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID,
                    doneIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            notifyBuilder =
                    new NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID) //id канала
                            .setContentTitle(currTask) // заголовок уведомления
                            .setContentText(text) // текст уведомления
                            .setSmallIcon(R.drawable.ic_task_notify) // иконка увдомления (обязательно)
                            .setContentIntent(clickPendingIntent2) // действие при нажатии
                            .setAutoCancel(false) // закрывать после клика
                            // большая картинка справа уведомления
                            .setLargeIcon(myLogo)
                            //.setDeleteIntent(deletePendingIntent); // действия после удаления пользователем
                            .addAction(R.drawable.ic_dialog, context.getString(R.string.done), donePendingIntent)
                            .setColor(colorPrimary)
                            .setColorized(true)
                            .setOngoing(false) // можно удалить смахиванием
                            .setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        else {
            // новый intent и pending intent для варианта сдвига даты
            Intent doneShiftIntent = new Intent(context, ClearNotificationReceiver.class);
            doneShiftIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID + 100);
            doneShiftIntent.putExtra("NOTIFICATION_HOUR", notification_hour);
            doneShiftIntent.putExtra("COLOR_PRIMARY", colorPrimary);
            PendingIntent doneShiftPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID + 100,
                    doneShiftIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            text = "Просрочена!" + System.lineSeparator() + "Выбрав \"" + context.getString(R.string.done) +
            "\", вы назначите новую дату, отсчитанную от исходной. Выбрав \"" + context.getString(R.string.done_shift) +
            "\" - от сегодняшнего дня.";
            donePendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID,
                    doneIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            notifyBuilder =
                    new NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID) //id канала
                            .setContentTitle(currTask) // заголовок уведомления

                            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                            .setSmallIcon(R.drawable.ic_task_notify) // иконка увдомления (обязательно)
                            .setContentIntent(clickPendingIntent) // действие при нажатии
                            .setAutoCancel(false) // закрывать после клика
                            // большая картинка справа уведомления
                            .setLargeIcon(myLogo)
                            //.setDeleteIntent(deletePendingIntent); // действия после удаления пользователем
                            .addAction(R.drawable.ic_dialog, context.getString(R.string.done), donePendingIntent)
                            .addAction(R.drawable.ic_dialog, context.getString(R.string.done_shift), doneShiftPendingIntent)
                            .setColor(colorPrimary)
                            .setColorized(true)
                            .setOngoing(false) // можно удалить смахиванием
                            .setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        return notifyBuilder;
    }

    // отправить уведомление
    public void sendNotification(int NOTIFICATION_ID, String currTask, boolean outOfDate) {
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder(NOTIFICATION_ID, currTask, outOfDate);
        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());
    }

}