package com.rockthevote.grommet.ui.registration;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.rockthevote.grommet.ui.MainActivity;
import com.rockthevote.grommet.util.Dates;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class DatePickerDialogFragment extends DialogFragment {

    public static final String DATE_ARG = "date_arg";
    private DatePickerDialog.OnDateSetListener listener;

    private Date startDate;

    public static DatePickerDialogFragment newInstance(DatePickerDialog.OnDateSetListener listener,
                                                       Date startDate) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment.setListener(listener);

        Bundle args = new Bundle();
        args.putString(DATE_ARG, Dates.formatAsISO8601_ShortDate(startDate));
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onResume() {
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        Objects.requireNonNull(getDialog().getWindow()).setLayout((8 * width) / 9, (2 * height) / 5);

        super.onResume();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startDate = Dates.parseISO8601_ShortDate(
                getArguments().getString(DATE_ARG, null));
    }

    private void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DatePickerDialog dialog;
        if (startDate == null) {
            // start the date picker at Jan 1, 1998
            dialog = new DatePickerDialog(
                    getActivity(),
                    android.R.style.Theme_Holo_Light_Dialog,
                    listener,
                    1975,
                    0,
                    1);

        } else {
            Calendar startDateCal = Calendar.getInstance();
            startDateCal.setTime(startDate);
            dialog = new DatePickerDialog(
                    getActivity(),
                    android.R.style.Theme_Holo_Light_Dialog,
                    listener,
                    startDateCal.get(Calendar.YEAR),
                    startDateCal.get(Calendar.MONTH),
                    startDateCal.get(Calendar.DAY_OF_MONTH));
        }
        dialog.getDatePicker().setSpinnersShown(true);
        dialog.getDatePicker().setCalendarViewShown(false);
//        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Objects.requireNonNull(dialog.getWindow()).setLayout((8 * width) / 9, (2 * height) / 5);

//        params.height = 1000; // dialogHeight;
//        dialog.getWindow().setAttributes(params);
        return dialog;
    }
}
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                dialog.getDatePicker().setSpinnersShown(true);
//                dialog.getDatePicker().setCalendarViewShown(false);