package com.lisss79.android.myperiodictasks;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class ChangeDateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    public int year, month, day;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create a new instance of DatePickerDialog with current date and return it.
        DatePickerDialog datePickerDialog =
                new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        DetailsActivity activity = (DetailsActivity) getActivity();
        activity.processChangeDateResult(i, i1+1, i2);
    }
}
