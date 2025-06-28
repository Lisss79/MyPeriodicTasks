package com.lisss79.android.myperiodictasks;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class DetailsActivity extends AppCompatActivity {
    private final String mName = "NAME_OF_TASK";
    private final String mDateYear = "DATE_YEAR__OF_TASK";
    private final String mDateMonth = "DATE_MONTH_OF_TASK";
    private final String mDateDay = "DATE_DAY_OF_TASK";
    private final String mPeriod = "PERIOD_OF_TASK";
    private final String mLengthPeriod = "LENGTH_PERIOD_OF_TASK";
    private final String mNotificationId = "NOTIFICATION_ID";
    private LocalDate currDate;
    private String currName;
    private int currYear, currMonth, currDay, currPeriod, NOTIFICATION_ID;
    private boolean currLengthPeriod;
    private LocalDate originalDate;
    private TextView detNameTextView, detDateTextView, detPeriodTextView, detLengthPeriodTextView;
    private EditText nameEditText;
    private NumberPicker periodNumberPicker;
    private RadioButton monthRadioButton, dayRadioButton;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); // формат даты для показа

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.details_root_view),
                (view, insets) -> {
                    // Получаем размеры системных панелей
                    int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
                    int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

                    // Применяем padding к корневому View
                    view.setPadding(
                            view.getPaddingLeft(),
                            statusBarHeight,    // Отступ сверху = статус-бар
                            view.getPaddingRight(),
                            navigationBarHeight // Отступ снизу = навигационная панель
                    );

                    // Возвращаем consumed insets для дочерних View
                    return WindowInsetsCompat.CONSUMED;
                });

        Toolbar toolbar = findViewById(R.id.details_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(listenerNavButton);

        Intent intent = getIntent();

        currName = intent.getStringExtra(mName);
        detNameTextView = findViewById(R.id.det_name_textView);


        currYear = intent.getIntExtra(mDateYear, 0);
        currMonth = intent.getIntExtra(mDateMonth, 0);
        currDay = intent.getIntExtra(mDateDay, 0);
        originalDate = LocalDate.of(currYear, currMonth, currDay);
        currDate = originalDate;
        detDateTextView = findViewById(R.id.det_date_textView);

        currPeriod = intent.getIntExtra(mPeriod, 0);
        detPeriodTextView = findViewById(R.id.det_period_textView);

        currLengthPeriod = intent.getBooleanExtra(mLengthPeriod, true);
        detLengthPeriodTextView = findViewById(R.id.det_length_period_textView);

        NOTIFICATION_ID = intent.getIntExtra(mNotificationId, -1);

        installSpinner();
        refreshView();
    }

    private void installSpinner() {
        Spinner spinner = findViewById(R.id.change_time_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.change_time_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        currDate = originalDate;
                        break;
                    }
                    case 1: {
                        currDate = originalDate.with(TemporalAdjusters.next(DayOfWeek.SATURDAY));
                        break;
                    }
                    case 2: {
                        currDate = originalDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
                        break;
                    }
                    case 3: {
                        currDate = originalDate.plusDays(7);
                        break;
                    }
                    case 4: {
                        currDate = originalDate.plusDays(1);
                        break;
                    }
                }
                currYear = currDate.getYear();
                currMonth = currDate.getMonthValue();
                currDay = currDate.getDayOfMonth();
                refreshView();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private View.OnClickListener listenerNavButton = new View.OnClickListener() {
        public void onClick(View v) {
            finish();
        }
    };


    // подтверждение - нажата ОК
    public void onClickOK(View view) {
        NotificationManager  mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (NOTIFICATION_ID > -1) mNotifyManager.cancel(NOTIFICATION_ID);


        Intent replyIntent = new Intent();

        replyIntent.putExtra(mName, currName);
        replyIntent.putExtra(mDateYear, currYear);
        replyIntent.putExtra(mDateMonth, currMonth);
        replyIntent.putExtra(mDateDay, currDay);
        replyIntent.putExtra(mPeriod, currPeriod);
        replyIntent.putExtra(mLengthPeriod, currLengthPeriod);

        // имя и значение переменной, выдаваемой в ответ
        setResult(RESULT_OK, replyIntent);
        finish();
    }

    // отмена изменение - нажата Отмена
    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    // изменение названия задачи
    public void onNameChangeClick(View view) {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.change_name, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Новое название задачи:");
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                currName = nameEditText.getText().toString();
                refreshView();
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        nameEditText = dialogView.findViewById(R.id.name_editText);

        // если новая задача - вывести подсказку, иначе - редактируемое название
        if(currName.equals(getString(R.string.new_task))) {
            nameEditText.setHint(currName);
        }
        else nameEditText.setText(currName);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // показать подробности задачи
    private void refreshView() {
        detNameTextView.setText(currName);
        currDate = LocalDate.of(currYear, currMonth, currDay);
        String mDateText = currDate.format(formatter);

        detDateTextView.setText(mDateText);
        detPeriodTextView.setText(String.valueOf(currPeriod));
        detLengthPeriodTextView.setText(currLengthPeriod ? "мес." : "дн.");
    }

    // изменение даты выполнения
    public void onDateChangeClick(View view) {
        ChangeDateDialog newDialog = new ChangeDateDialog();
        newDialog.year = currYear;
        newDialog.month = currMonth - 1;
        newDialog.day = currDay;
        newDialog.show(getSupportFragmentManager(),"datePicker");
        refreshView();
    }

    // измненние периода выполнения
    public void onPeriodChangeClick(View view) {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.change_period, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Новая периодичность задачи:");
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                currPeriod = periodNumberPicker.getValue();
                if(monthRadioButton.isChecked()) {
                    currLengthPeriod = true;
                }
                else currLengthPeriod = false;
                refreshView();
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        periodNumberPicker = dialogView.findViewById(R.id.period_numberPicker);
        periodNumberPicker.setMinValue(1);
        periodNumberPicker.setMaxValue(365);
        periodNumberPicker.setValue(currPeriod);
        monthRadioButton = dialogView.findViewById(R.id.month_radioButton);
        dayRadioButton = dialogView.findViewById(R.id.day_radioButton);
        if(currLengthPeriod) {
            monthRadioButton.setChecked(true);
            dayRadioButton.setChecked(false);
        }
        else {
            monthRadioButton.setChecked(false);
            dayRadioButton.setChecked(true);
        }
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void processChangeDateResult(int i, int i1, int i2) {
        currYear = i;
        currMonth = i1;
        currDay = i2;
        refreshView();
    }

}