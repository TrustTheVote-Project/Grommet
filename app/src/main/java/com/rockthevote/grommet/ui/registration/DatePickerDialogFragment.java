package com.rockthevote.grommet.ui.registration;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.rockthevote.grommet.util.Dates;

import java.util.Calendar;
import java.util.Date;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startDate = Dates.parseISO8601_ShortDate(
                getArguments().getString(DATE_ARG, null));
    }

    private void setListener(DatePickerDialog.OnDateSetListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (startDate == null) {
            // start the date picker at Jan 1, 1998
            return new DatePickerDialog(
                    getActivity(), listener,
                    1975, 0, 1);
        } else {
            Calendar startDateCal = Calendar.getInstance();
            startDateCal.setTime(startDate);

            return new DatePickerDialog(
                    getActivity(), listener,
                    startDateCal.get(Calendar.YEAR),
                    startDateCal.get(Calendar.MONTH),
                    startDateCal.get(Calendar.DAY_OF_MONTH));
        }
    }
}
