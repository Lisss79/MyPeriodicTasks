package com.lisss79.android.myperiodictasks;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;


import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    public ArrayList<String> mTasksList = new ArrayList<>();// название задачи
    public ArrayList<LocalDate> mDateList = new ArrayList<>(); // ближайшая дата
    public ArrayList<Integer> mPeriodList = new ArrayList<>(); // период между уведомлениями
    public ArrayList<Boolean> mLengthPeriodList = new ArrayList<>(); // true - месяцев, false - дней
    public ArrayList<Boolean> isActive = new ArrayList<>();
    ActivityResultLauncher<Intent> detailsActivityResultLauncher;
    private RecyclerView mRecyclerView;
    public TasksListAdapter mAdapter;
    private final String mName = "NAME_OF_TASK";
    private final String mDateYear = "DATE_YEAR__OF_TASK";
    private final String mDateMonth = "DATE_MONTH_OF_TASK";
    private final String mDateDay = "DATE_DAY_OF_TASK";
    private final String mPeriod = "PERIOD_OF_TASK";
    private final String mLengthPeriod = "LENGTH_PERIOD_OF_TASK";
    private LocalDate currDate;
    private String currName;
    private int currYear, currMonth, currDay, currPeriod;
    private boolean currLengthPeriod;
    private boolean modeAddNewTask; // true при добавлении новой задачи
    private int position;
    private TextView no_tasksTextView;
    private TextView currentHourTextView;
    private NumberPicker hourNumberPicker;
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel"; // ID канала уведомлений
    private NotificationManager mNotifyManager; // менеджер уведомлений
    StatusBarNotification[] notifications; // активные уведомления
    private static final int NOTIFICATION_ID = 0; // ID уведомлений
    private int colorPrimary; // цвет Color Primary
    private int notification_hour = 14; // час выдачи уведомлений о задачах, если >100, включен режим "3 мин"
    static Context context;
    private String ACTION_CHECK_TASKS; // значение action для локальной рассылки
    LoadAndSaveData loadAndSaveData; // класс с методами загрузки/сохранения
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); // формат даты для показа

    // переменные для работы с гугл диском
    Drive googleDriveService;
    FileList fileList = null;
    ActivityResultLauncher<Intent> googleLoginActivityLauncher;
    ActivityResultLauncher<Intent> googleDriveFilesActivityLauncher;
    GoogleSignInAccount account;
    Collection<String> scopes;
    GoogleDriveFilesActivity googleDriveFilesActivity;
    GoogleDriveOperations googleDriveOperations;
    int operation;
    final int OPERATION_SAVE = 1;
    final int OPERATION_LOAD = 2;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuCompat.setGroupDividerEnabled(menu, true);
        if(notification_hour > 100) menu.findItem(R.id.three_min_timer).setChecked(true);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        colorPrimary = typedValue.data;

        ACTION_CHECK_TASKS = getPackageName() + ".ACTION_CHECK_TASKS";
        loadAndSaveData = new LoadAndSaveData(this, getFilesDir(),
                notification_hour, colorPrimary);

        // создать Toolbar
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        no_tasksTextView = findViewById(R.id.no_tasks_textView);
        context = this;

        loadData(); // загрузить сохраненные данные из памяти телефона
        if(sortData()) saveData(); // отсортировать и сохранить если нужно

        // зарегистрировать приемник для локальной рассылки
        DailyReceiver mReceiver = new DailyReceiver();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mReceiver, new IntentFilter(ACTION_CHECK_TASKS));

        // Создание Recycle View
        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.tasks_recycler_view);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new TasksListAdapter(this, mTasksList, mDateList, isActive);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter.selAll = false;
        mAdapter.unSelAll = false;
        mAdapter.clearSelection(true);
        modeAddNewTask = false;

        setDailyAlarm();

        // лончер GoogleLoginActivity
        googleLoginActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    account = App.getInstance().getAccount();
                    scopes = new HashSet<>();
                    scopes.add(DriveScopes.DRIVE_READONLY);
                    scopes.add(DriveScopes.DRIVE_FILE);
                    App.getInstance().setScopes(scopes);
                    Intent intent = new Intent(this, GoogleDriveFilesActivity.class);
                    googleDriveFilesActivityLauncher.launch(intent);

                }
        );

        // лончер GoogleDriveFilesActivity
        googleDriveFilesActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    googleDriveService = App.getInstance().getDrive();
                    fileList = App.getInstance().getFileList();
                    googleDriveFilesActivity = App.getInstance().getGoogleDriveFilesActivity();
                    googleDriveOperations = new GoogleDriveOperations(googleDriveService, fileList, this);
                    String fId = googleDriveOperations.createFolder("MyPeriodicTasks");
                    String id = googleDriveOperations.getFileId("taskslist", "MyPeriodicTasks");
                    System.out.println("Id файла taskslist: " + id);

                    if(operation == OPERATION_SAVE) {
                        if(id != null) {
                            boolean res = googleDriveOperations.deleteFileId(id);
                            if (!res) Toast.makeText(this,"Невозможно удалить старый файл taskslist",
                                    Toast.LENGTH_SHORT).show();
                        }
                        boolean res = googleDriveOperations.saveConfigFile(fId, String.valueOf(getFilesDir()));
                        if (!res) Toast.makeText(this,"Невозможно сохранить taskslist",
                                Toast.LENGTH_SHORT).show();
                        else {
                            Toast.makeText(this,"taskslist успешно сохранен",
                                    Toast.LENGTH_SHORT).show();
                            System.out.println("Number Of Tasks: " + mTasksList.size());
                        }
                    }

                    else if(operation == OPERATION_LOAD) {
                        if(id == null) {
                            Toast.makeText(this,"Нет сохраненного файла",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            boolean res = googleDriveOperations.loadConfigFile(id, String.valueOf(getFilesDir()));
                            if(!res) Toast.makeText(this,"Невозможно загрузить taskslist",
                                    Toast.LENGTH_SHORT).show();
                            else {
                                Toast.makeText(this,"taskslist успешно загружен",
                                        Toast.LENGTH_SHORT).show();
                                //loadData();
                                //mAdapter.notifyDataSetChanged();
                                //checkForTasks();
                                ((MainActivity) this).recreate();
                                //setDailyAlarm();
                                System.out.println("Number Of Tasks: " + mTasksList.size());
                            }
                        }
                    }
                }
        );

        // контракт на запуск activity с деталями задачи
        detailsActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        // если внесены поедтвержденные изменения
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            currName = data.getStringExtra(mName);
                            currYear = data.getIntExtra(mDateYear, 0);
                            currMonth = data.getIntExtra(mDateMonth, 0);
                            currDay = data.getIntExtra(mDateDay, 0);
                            currDate = LocalDate.of(currYear, currMonth, currDay);
                            currPeriod = data.getIntExtra(mPeriod, 0);
                            currLengthPeriod = data.getBooleanExtra(mLengthPeriod, true);
                            refreshData(modeAddNewTask);
                            sortData();
                            checkForTasks();
                            mAdapter.notifyDataSetChanged();
                            saveData();
                            setDailyAlarm();
                        }
                        else {
                        }
                    }
                });
        checkForTasks();
    }

    // отсортировать задачи по дате выполнения (венуть true, если были изменения)
    private boolean sortData() {
        boolean isChanged = false;
        LocalDate date1, date2;
        boolean isActive1, isActive2;
        for(int i = 0; i < mTasksList.size(); i++) {
            for(int j = i; j < mTasksList.size(); j++) {
                date1 = mDateList.get(i);
                date2 = mDateList.get(j);
                isActive1 = isActive.get(i);
                isActive2 = isActive.get(j);
                if((isActive1 && isActive2 && date1.isAfter(date2)) ||
                        (!isActive1 && isActive2) ||
                        (!isActive1 && !isActive2 && date1.isAfter(date2))){
                    isChanged = true;
                    Collections.swap(mTasksList, i, j);
                    Collections.swap(mDateList, i, j);
                    Collections.swap(mPeriodList, i, j);
                    Collections.swap(mLengthPeriodList, i, j);
                    Collections.swap(isActive, i, j);
                }
            }
        }
        return isChanged;
    }

    // проверка отсутствия задач и вывод сообщения об этом
    private void checkForTasks() {
        if (mTasksList.size() > 0) {
            no_tasksTextView.setVisibility(View.GONE);
        } else {
            no_tasksTextView.setVisibility(View.VISIBLE);
        }
    }

    // обновить данные в ArrayList
    private void refreshData(boolean mode) {
        // редактирование сеществующей задачи
        if(!mode) {
            mTasksList.set(position, currName);
            mDateList.set(position, currDate);
            mPeriodList.set(position, currPeriod);
            mLengthPeriodList.set(position, currLengthPeriod);
        }
        // добавление новой задачи
        else {
            mTasksList.add(currName);
            mDateList.add(currDate);
            mPeriodList.add(currPeriod);
            mLengthPeriodList.add(currLengthPeriod);
            isActive.add(true);
            mAdapter.mItemSelected.add(false);
        }
    }

    // обработка Options Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int pos = mTasksList.size();
        switch (item.getItemId()) {
            case R.id.add_task: // добавить элемент
                addTaskToList(pos);
                return true;
            case R.id.remove_task: // удаление выбранных элементов
                boolean res = checkForActiveNotifications();
                if(!res) removeTaskFromList();
                return true;
            case R.id.select_all: // выбор всех элементов
                selectAll();
                return true;
            case R.id.unselect_all: // отмена выбора
                unselectAll();
                return true;
            case R.id.notification_time: // настроить время уведомлений
                adjustNotificationTime();
                return true;
            case R.id.saveConfig: // сохранить конфигурацию на гугл диск
                saveConfig();
                return true;
            case R.id.loadConfig: // загрузить конфигурацию с гугл диска
                loadConfig();
                return true;
            case R.id.three_min_timer: // режим "3 минуты"
                if(notification_hour < 100) {
                    notification_hour += 100;
                    item.setChecked(true);
                    saveData();
                    setDailyAlarm();
                    Toast.makeText(this, "Режим 3 минуты активен", Toast.LENGTH_SHORT).show();
                }
                else {
                    notification_hour -= 100;
                    item.setChecked(false);
                    saveData();
                    setDailyAlarm();
                    Toast.makeText(this, "Обычный режим", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.about: // о программе
                aboutInfo();
                return true;
            case R.id.check_now: // показать уведомление
                //Intent alarmIntent = new Intent(this, DailyReceiver.class);
                Intent alarmIntent = new Intent();
                alarmIntent = putExtraInIntent(alarmIntent);
                alarmIntent.setAction(ACTION_CHECK_TASKS);
                LocalBroadcastManager.getInstance(this).sendBroadcast(alarmIntent);
                return true;
            case R.id.mark_done: // сбросить уведомления
                mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotifyManager.cancelAll();
                return true;
            default:
                // ничего не выполнять
        }
        return super.onOptionsItemSelected(item);
    }

    // сохранить конфигурацию
    private void saveConfig() {
        operation = OPERATION_SAVE;
        Intent intent = new Intent(this, GoogleLoginActivity.class);
        googleLoginActivityLauncher.launch(intent);
    }

    // сохранить конфигурацию
    private void loadConfig() {
        operation = OPERATION_LOAD;
        Intent intent = new Intent(this, GoogleLoginActivity.class);
        googleLoginActivityLauncher.launch(intent);
    }

    // меню - настроить время уведомлений о задачах
    private void adjustNotificationTime() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.change_hour, null);
        hourNumberPicker = dialogView.findViewById(R.id.hour_numberPicker);
        hourNumberPicker.setMinValue(0);
        hourNumberPicker.setMaxValue(23);
        hourNumberPicker.setValue(notification_hour);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Новое время уведомлений, часов:");
        builder.setMessage("(Текущее значение: " + notification_hour + "ч)");
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                notification_hour = hourNumberPicker.getValue();
                saveData();
                loadAndSaveData.setDailyAlarm();
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    // меню - снять выделение
    private void unselectAll() {
        mAdapter.usualMode = false;
        mAdapter.unSelAll = true;
        mAdapter.selAll = false;
        mAdapter.clearSelection(false);
        mAdapter.notifyDataSetChanged();
    }

    // меню - выбрать все
    private void selectAll() {
        mAdapter.usualMode = false;
        mAdapter.selAll = true;
        mAdapter.unSelAll = false;
        mAdapter.notifyDataSetChanged();
    }

    // меню - о программе
    private void aboutInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.about_text); // сообщение в окне
        builder.setTitle(R.string.app_full_title); // заголовок окна
        builder.setIcon(R.drawable.ic_info); // иконка в заголовке
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() { //текст на кнопке
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.show();
    }

    // меню - удаление задачи
    private void removeTaskFromList() {
        // диалоговое окно - вы уверены?
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Вы точно уверены?"); // сообщение в окне
        builder.setTitle("Удалить выбранные задачи"); // заголовок окна
        builder.setIcon(R.drawable.ic_dialog); // иконка в заголовке
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() { //текст на кнопке
            public void onClick(DialogInterface dialog, int id) {
                // подтверждение
                mAdapter.usualMode = true;
                int i = 0;
                while (i < mTasksList.size()) {
                    mAdapter.notifyItemChanged(i);
                    if (mAdapter.mItemSelected.get(i)) {
                        mTasksList.remove(i);
                        mDateList.remove(i);
                        mPeriodList.remove(i);
                        mLengthPeriodList.remove(i);
                        isActive.remove(i);
                        mAdapter.mItemSelected.remove(i);
                        mAdapter.notifyItemRemoved(i);
                    } else {
                        i++;
                    }
                }
                checkForTasks();
                saveData(); // сохранить данные в память телефона
                setDailyAlarm();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() { //текст на кнопке
            public void onClick(DialogInterface dialog, int id) {
                // отмена
            }
        });
        builder.show();
    }

    // меню - добавление задачи
    private void addTaskToList(int pos) {
        boolean res = checkForActiveNotifications();
        if(!res) {
            modeAddNewTask = true;
            addTask(pos);
            //checkForTasks();
            saveData(); // сохранить данные в память телефона
            showDetails(pos, modeAddNewTask);
        }
    }

    // переход к активности с деталями задачи
    public void showDetails(int position, boolean mode) {
        modeAddNewTask = mode;
        boolean res = checkForActiveNotifications();
        if(!res) {
            // редактирование имеющейся задачи
            if(!mode) {
                LocalDate localDate = mDateList.get(position);
                Intent intent = new Intent(this, DetailsActivity.class);
                intent.putExtra(mName, mTasksList.get(position));
                intent.putExtra(mDateYear, localDate.getYear());
                intent.putExtra(mDateMonth, localDate.getMonthValue());
                intent.putExtra(mDateDay, localDate.getDayOfMonth());
                intent.putExtra(mPeriod, mPeriodList.get(position));
                intent.putExtra(mLengthPeriod, mLengthPeriodList.get(position));
                this.position = position;
                detailsActivityResultLauncher.launch(intent);
            }
            // добавление новой задачи
            else {
                LocalDate localDate = currDate;
                Intent intent = new Intent(this, DetailsActivity.class);
                intent.putExtra(mName, currName);
                intent.putExtra(mDateYear, localDate.getYear());
                intent.putExtra(mDateMonth, localDate.getMonthValue());
                intent.putExtra(mDateDay, localDate.getDayOfMonth());
                intent.putExtra(mPeriod, currPeriod);
                intent.putExtra(mLengthPeriod, currLengthPeriod);
                this.position = position;
                detailsActivityResultLauncher.launch(intent);
                checkForTasks();
            }
        }
    }

    // нажетие летающей кнопки
    public void onClickFAB(View view) {
        addTaskToList(mTasksList.size());
    }

    // добавление задачи в список
    private void addTask(int pos) {
        // максимальное число задач - 100
        if (mTasksList.size() < 100) {
            //mTasksList.add(getString(R.string.new_task));
            currName = getString(R.string.new_task);
            //mDateList.add(LocalDate.now());
            currDate = LocalDate.now();
            //mPeriodList.add(1);
            currPeriod = 1;
            //mLengthPeriodList.add(true);
            currLengthPeriod = true;
            //isActive.add(true);
            //mAdapter.mItemSelected.add(false);
            //mAdapter.notifyItemInserted(pos);
        } else Toast.makeText(this, "Поддерживается не более 100 задач", Toast.LENGTH_SHORT).show();
    }

    public void saveData() {
        loadAndSaveData.mTasksList = mTasksList;
        loadAndSaveData.mDateList = mDateList;
        loadAndSaveData.mPeriodList = mPeriodList;
        loadAndSaveData.mLengthPeriodList = mLengthPeriodList;
        loadAndSaveData.isActive = isActive;
        loadAndSaveData.notification_hour = notification_hour;
        loadAndSaveData.saveData();
    }

    public void loadData() {
        if (loadAndSaveData.loadData()) {
            mTasksList = loadAndSaveData.mTasksList;
            mDateList = loadAndSaveData.mDateList;
            mPeriodList = loadAndSaveData.mPeriodList;
            mLengthPeriodList = loadAndSaveData.mLengthPeriodList;
            isActive = loadAndSaveData.isActive;
            notification_hour = loadAndSaveData.notification_hour;
        }
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

    // включение ежедневной рассылки
    public void setDailyAlarm() {
        loadAndSaveData.setDailyAlarm();
    }

    // проверка наличия активных уведомлений от приложения
    public boolean checkForActiveNotifications() {
        boolean activeNotifications = false;
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notifications = mNotifyManager.getActiveNotifications();
        for(int i = 0; i < notifications.length; i++) {
            if(getPackageName().equals(notifications[i].getPackageName())) {
                activeNotifications = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Прежде чем редактировать список, " +
                        "подтвердите выполнение активных на сегодня задач"); // сообщение в окне
                builder.setTitle("Внимание!"); // заголовок окна
                builder.setIcon(R.drawable.ic_warning); // иконка в заголовке
                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() { //текст на кнопке
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.show();
                break;
            }
        }
        return activeNotifications;
    }
}