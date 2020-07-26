package com.rockthevote.grommet.ui.eventFlow;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.data.db.dao.RegistrationDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.util.Dates;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.rockthevote.grommet.data.db.model.SessionStatus.SESSION_CLEARED;

public class SessionSummary extends FrameLayout implements EventFlowPage {

    @Inject PartnerInfoDao partnerInfoDao;
    @Inject SessionDao sessionDao;
    @Inject RegistrationDao registrationDao;
    @Inject SharedPreferences sharedPreferences;
    @Inject RockyService rockyService;

    // Session Details
    @BindView(R.id.summary_canvasser_name) TextView edCanvasserName;
    @BindView(R.id.summary_event_name) TextView edEventName;
    @BindView(R.id.summary_event_zip) TextView edEventZip;
    @BindView(R.id.summary_partner_name) TextView edPartnerName;
    @BindView(R.id.summary_device_id) TextView edDeviceId;

    // Time Tracking
    @BindView(R.id.summary_clock_in_time) TextView clockInTime;
    @BindView(R.id.summary_clock_out_time) TextView clockOutTime;
    @BindView(R.id.summary_total_time) TextView totalTime;

    private EventFlowCallback listener;

    private SessionTimeTrackingViewModel viewModel;

    public SessionSummary(Context context) {
        this(context, null);
    }

    public SessionSummary(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SessionSummary(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.session_summary, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            ButterKnife.bind(this);
        }
        viewModel = new ViewModelProvider(
                (AppCompatActivity) getContext(),
                new SessionTimeTrackingViewModelFactory(
                        partnerInfoDao,
                        sessionDao,
                        registrationDao,
                        sharedPreferences,
                        rockyService)
        ).get(SessionTimeTrackingViewModel.class);

        observeData();
    }

    private void observeData() {

        viewModel.getSessionData().observe(
                (AppCompatActivity) getContext(), data -> {
                    edCanvasserName.setText(data.getCanvasserName());
                    edEventName.setText(data.getOpenTrackingId());
                    edEventZip.setText(data.getPartnerTrackingId());
                    edPartnerName.setText(data.getPartnerName());
                    edDeviceId.setText(data.getDeviceId());

                    if (data.getClockInTime() != null && data.getClockOutTime() != null) {
                        Date clockIn = data.getClockInTime();
                        Date clockOut = data.getClockOutTime();

                        clockInTime.setText(Dates.formatAs_LocalTimeOfDay(clockIn));
                        clockOutTime.setText(Dates.formatAs_LocalTimeOfDay(clockOut));

                        long elapsedMilliseconds = clockOut.getTime() - clockIn.getTime();

                        String elapsedTime = String.format(Locale.getDefault(), "%d hours, %d min",
                                TimeUnit.MILLISECONDS.toHours(elapsedMilliseconds),
                                TimeUnit.MILLISECONDS.toMinutes(elapsedMilliseconds) -
                                        TimeUnit.MINUTES.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedMilliseconds))
                        );

                        totalTime.setText(elapsedTime);

                    } else {
                        clockInTime.setText("");
                        clockOutTime.setText("");
                    }
                });

        viewModel.getEffect().observe(
                (AppCompatActivity) getContext(), effect -> {
                    if (effect instanceof SessionSummaryState.Cleared) {
                        // update wizard view pager to SESSION_CLEARED to tell the first page to reset
                        listener.setState(SESSION_CLEARED, false);
                    } else if (effect instanceof SessionSummaryState.Error) {
                        //todo anything?
                        Timber.e("error updating view after updating canvasser info");
                    }
                }
        );
    }

    @Override
    public void registerCallbackListener(EventFlowCallback listener) {
        this.listener = listener;
    }

    @Override
    public void unregisterCallbackListener() {
        listener = null;
    }

    @OnClick(R.id.session_summary_clear)
    public void onClickClear(View v) {
        viewModel.clearSession();
    }

}
