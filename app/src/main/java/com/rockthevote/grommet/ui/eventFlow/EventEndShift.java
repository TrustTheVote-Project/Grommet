package com.rockthevote.grommet.ui.eventFlow;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textview.MaterialTextView;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.data.db.dao.RegistrationDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.droidsonroids.gif.GifImageView;
import timber.log.Timber;

import static com.rockthevote.grommet.data.db.model.SessionStatus.PARTNER_UPDATE;
import static com.rockthevote.grommet.data.db.model.SessionStatus.SESSION_CLEARED;
import static com.rockthevote.grommet.data.db.model.SessionStatus.SPLASH;

public class EventEndShift extends FrameLayout implements EventFlowPage {

    @Inject
    PartnerInfoDao partnerInfoDao;
    @Inject
    SessionDao sessionDao;
    @Inject
    RegistrationDao registrationDao;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    RockyService rockyService;


    @BindView(R.id.end_shift_ttl_hours)
    TextView endShiftTtlHours;
    @BindView(R.id.end_shift_avg)
    TextView endShiftAvg;

    @BindView(R.id.summary_total_registrations)
    TextView summaryRegistrations;
    @BindView(R.id.summary_uploaded_registrations)
    TextView summaryUploaded;
    @BindView(R.id.end_shift_info)
    MaterialTextView endShiftInfo;
    @BindView(R.id.summary_total_dln)
    TextView summaryDln;
    @BindView(R.id.summary_total_ssn)
    TextView summarySsn;
    @BindView(R.id.summary_total_email_opt_in)
    TextView summaryEmailOptIn;
    @BindView(R.id.summary_total_sms_opt_in)
    TextView summarySmsOptIn;
    @BindView(R.id.summary_total_abandoned)
    TextView summaryAbandoned;

    @BindView(R.id.summary_dln_percentage)
    TextView smrDlnPrt;
    @BindView(R.id.summary_ssn_percentage)
    TextView smrSsnPrt;
    @BindView(R.id.summary_email_opt_in_percentage)
    TextView smrEmailOptInPrt;
    @BindView(R.id.summary_sms_opt_in_percentage)
    TextView smrSmsOptInPrt;
    @BindView(R.id.summary_total_abandoned_in_percentage)
    TextView smrAbandonedInPrt;

    private String canvasserName = "";

    private EventFlowCallback listener;

    private SessionTimeTrackingViewModel viewModel;

    private Observer<? super SessionSummaryData> sessionSummaryDataObserver = this::handleSessionSummary;
    private Observer<? super CompleteShiftState> completeShiftObserver = this::handleCompleteShiftEvent;

    public EventEndShift(Context context) {
        this(context, null);
    }

    public EventEndShift(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventEndShift(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_end_shift, this);

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
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }


    private void handleSessionSummary(SessionSummaryData data) {
        canvasserName = data.getCanvasserName();
        String firstName = data.getCanvasserName();
        String lastName = data.getCanvasserLastName();
        Date startDate = data.getClockInTime();
        Date endDate = data.getClockOutTime();
        if (startDate != null && endDate != null) {
            long diff = endDate.getTime() - startDate.getTime();
            Timber.d(String.valueOf(diff));
            long hours = diff / (1000 * 60 * 60);
            if (hours < 1) {
                long minutes = diff / (1000 * 60);
                endShiftTtlHours.setText(minutes + " minutes");
                endShiftAvg.setText(R.string.less_than_hour);
            } else {
                double avg = (double) data.getTotalRegistrations() / hours;
                endShiftTtlHours.setText(hours + " hours");
                endShiftAvg.setText(new DecimalFormat("##.##").format(avg)+ " avg per hour");

//                endShiftAvg.setText(avg + " avg per hour");
            }
        }
        endShiftInfo.setText("End Shift for " + firstName + " " + lastName);

        int totalRgs = data.getTotalRegistrations();
        int totalDln = data.getDlnCount();
        int emailOptInCount = data.getEmailOptInCount();
        int ssnCount = data.getSsnCount();
        int smsCount = data.getSmsCount();
        int abandonedRegistrations = data.getAbandonedRegistrations();

        smrDlnPrt.setVisibility(View.INVISIBLE);
        smrSsnPrt.setVisibility(View.INVISIBLE);
        smrEmailOptInPrt.setVisibility(View.INVISIBLE);
        smrSmsOptInPrt.setVisibility(View.INVISIBLE);
        smrAbandonedInPrt.setVisibility(View.INVISIBLE);
        if (totalRgs > 0) {
            if (ssnCount > 0) {
                smrSsnPrt.setVisibility(View.VISIBLE);
                smrSsnPrt.setText("/ " + new DecimalFormat("##.##").format( (float)ssnCount /totalRgs * 100) + "% of total");
            }
            if (emailOptInCount > 0) {
                smrEmailOptInPrt.setVisibility(View.VISIBLE);
                smrEmailOptInPrt.setText("/ " + new DecimalFormat("##.##").format((float)emailOptInCount / totalRgs * 100) + "% of total");
            }
            if (totalDln > 0) {
                smrDlnPrt.setVisibility(View.VISIBLE);
                smrDlnPrt.setText("/ " + new DecimalFormat("##.##").format((float)totalDln / totalRgs * 100) + "% of total");
            }
            if (smsCount > 0) {
                smrSmsOptInPrt.setVisibility(View.VISIBLE);
                smrSmsOptInPrt.setText("/ " + new DecimalFormat("##.##").format((float)smsCount / totalRgs  * 100) + "% of total");
            }
        }
        summaryUploaded.setText(String.valueOf(data.getTotalRegistrations()));
        summaryRegistrations.setText(String.valueOf(totalRgs));
        summaryDln.setText(String.valueOf(totalDln));
        summaryEmailOptIn.setText(String.valueOf(emailOptInCount));
        summarySsn.setText(String.valueOf(ssnCount));
        summarySmsOptIn.setText(String.valueOf(smsCount));
        summaryAbandoned.setText(String.valueOf(abandonedRegistrations));
    }

    private void handleCompleteShiftEvent(CompleteShiftState event) {
        Timber.e(event.toString());
        if (event instanceof CompleteShiftState.Completed) {
            createDialog();
        } else if (event instanceof CompleteShiftState.Error) {
        }
    }


    private void createDialog() {
        Dialog endShiftDialog = new Dialog(this.getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        endShiftDialog.setContentView(R.layout.end_shift_dialog);
        MaterialTextView doneBtn = endShiftDialog.findViewById(R.id.done_es_btn);
        MaterialTextView endShiftTxt = endShiftDialog.findViewById(R.id.end_dl_shift_name);
        GifImageView gif = endShiftDialog.findViewById(R.id.gif_anim);
        gif.setVisibility(View.VISIBLE);
        String endText = "Thank you, " + canvasserName + "!";

        endShiftTxt.setText(endText);
        endShiftDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        doneBtn.setOnClickListener(v -> {
            viewModel.clearSession();
            gif.setVisibility(View.GONE);
            listener.setState(SPLASH, false);
            endShiftDialog.dismiss();
        });
        endShiftDialog.show();
    }


    @OnClick(R.id.end_shift_btn)
    public void onClickEndCltn(View v) {
        v.setEnabled(false);
        viewModel.getCompleteShiftState().observe((AppCompatActivity) getContext(), completeShiftObserver);
        viewModel.completeShift();
//        gifAnim.setVisibility(View.VISIBLE);
    }

    @Override
    public void registerCallbackListener(EventFlowCallback listener) {
        this.listener = listener;
        registerDataObservers();
        viewModel.updateSessionStatus();
    }

    @Override
    public void unregisterCallbackListener() {
        unregisterDataObservers();
        listener = null;
    }

    private void registerDataObservers() {
        viewModel.getSessionData().observe((AppCompatActivity) getContext(), sessionSummaryDataObserver);
    }

    private void unregisterDataObservers() {
        viewModel.getSessionData().removeObserver(sessionSummaryDataObserver);
        viewModel.getCompleteShiftState().removeObserver(completeShiftObserver);
    }
}

