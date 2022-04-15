package com.rockthevote.grommet.ui.eventFlow;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
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
import timber.log.Timber;

import static com.rockthevote.grommet.data.db.model.SessionStatus.END_SHIFT;
import static com.rockthevote.grommet.data.db.model.SessionStatus.END_STRANGER_COLLECTION;
import static com.rockthevote.grommet.data.db.model.SessionStatus.PARTNER_UPDATE;
import static com.rockthevote.grommet.data.db.model.SessionStatus.SESSION_CLEARED;

public class EventEndCollection extends FrameLayout implements EventFlowPage {

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

    @BindView(R.id.end_collection_info)
    MaterialTextView endCollectionInfo;

    @BindView(R.id.avg_rgs)
    MaterialTextView endCollectionAvg;

    @BindView(R.id.end_clt_stranger)
    MaterialTextView endCltStranger;

    @BindView(R.id.scfl_to_upload_count)
    MaterialTextView scflToUploadCount;

    @BindView(R.id.scfl_uploaded_count)
    MaterialTextView scflUploadedCount;

    @BindView(R.id.summary_total_registrations)
    TextView summaryRegistrations;

//    @BindView(R.id.end_collection_info)
//    MaterialTextView cltStartText;

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

    @BindView(R.id.end_collection_warning)
    TextView endClnWarning;

    @BindView(R.id.end_collection_error)
    TextView endCollectionError;


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

    private EventFlowCallback listener;
    private String canvasserName = "";
    private SessionTimeTrackingViewModel viewModel;
//    private MainActivityViewModel mainViewModel;

    //    private Observer<? super SessionStatus> sessionStatusObserver = this::updateUI;
    private Observer<? super SessionSummaryData> sessionSummaryDataObserver = this::handleSessionSummary;
    private Observer<? super EndCollectionState> endStgEventObserver = this::handleEndStgEvent;
    private Observer<? super UploadRegistrationState> uploadStateEventObserver = this::handleUploadRegistrEvent;

    public EventEndCollection(Context context) {
        this(context, null);
    }

    public EventEndCollection(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventEndCollection(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_end_collection, this);

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
        Timber.e("error updating view after updating canvasser info");
        viewModel = new ViewModelProvider(
                (AppCompatActivity) getContext(),
                new SessionTimeTrackingViewModelFactory(
                        partnerInfoDao,
                        sessionDao,
                        registrationDao,
                        sharedPreferences,
                        rockyService)
        ).get(SessionTimeTrackingViewModel.class);

        viewModel.getEffect().observe(
                (AppCompatActivity) getContext(), effect -> {
                    if (effect instanceof SessionSummaryState.ShiftUpdated) {
                        // update wizard view pager to SESSION_CLEARED to tell the first page to reset
                        if (!viewModel.isEffectDone()) {
                            viewModel.uploadRegistrations();
                        }

                    } else if (effect instanceof SessionSummaryState.Error) {
                        //todo anything?
                        Timber.e("error updating view after updating canvasser info");
                    }
                }
        );
    }

    private void setClickableSpan(Boolean isEmpty,String canvasserName) {
        SpannableString ss = new SpannableString(getResources().getString(R.string.if_this_isn_t_you_click_here));
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
//                if (isEmpty) {
//                    listener.setState(SESSION_CLEARED, true);
//                } else {
//                    createDialog(canvasserName);
//                }
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.colorAccent));
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan1, 25, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        endCltStranger.setText(ss);
        endCltStranger.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
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
        viewModel.getEndStrState().observe((AppCompatActivity) getContext(), endStgEventObserver);
        viewModel.getState().observe((AppCompatActivity) getContext(), uploadStateEventObserver);
    }

    private void unregisterDataObservers() {
        viewModel.getSessionData().removeObserver(sessionSummaryDataObserver);
        viewModel.getEndStrState().removeObserver(endStgEventObserver);
        viewModel.getState().removeObserver(uploadStateEventObserver);
    }


    private void handleEndStgEvent(EndCollectionState event) {
        Timber.e("loxEnd %s", event.toString());
        if (event instanceof EndCollectionState.Done) {
            listener.setState(END_STRANGER_COLLECTION, true);
        } else if (event instanceof EndCollectionState.Error) {

        }
    }

    private void handleUploadRegistrEvent(UploadRegistrationState event) {
        Timber.e("loxEnd %s", event.toString());
        if (event instanceof UploadRegistrationState.Content) {

            int failedUpload = ((UploadRegistrationState.Content) event).getFailedUploads();
            int pendUpload = ((UploadRegistrationState.Content) event).getPendingUploads();
            scflUploadedCount.setText(String.valueOf(failedUpload));
            scflToUploadCount.setText(String.valueOf(pendUpload));

            if(failedUpload>0){
                endCollectionError.setVisibility(View.VISIBLE);
                endClnWarning.setVisibility(View.GONE);
            }else{
                endCollectionError.setVisibility(View.VISIBLE);
                endClnWarning.setVisibility(View.GONE);
                listener.setState(END_SHIFT,true);
            }
        } else if (event instanceof UploadRegistrationState.Error) {
            endCollectionError.setVisibility(View.VISIBLE);
            endClnWarning.setVisibility(View.GONE);
//            showClockInSpinner();
        }
    }

    private void handleSessionSummary(SessionSummaryData data) {
        Timber.e(data.toString());
        DateFormat fhm = new SimpleDateFormat("hh:mm aa ", Locale.US);
        String firstName = data.getCanvasserName();
        String lastName = data.getCanvasserLastName();
        Date startDate = data.getClockInTime();
        Date endDate = data.getClockOutTime();
        int totalRgs = data.getTotalRegistrations();
        canvasserName = firstName+" "+lastName;
        if (startDate != null && endDate != null) {
            long diff = endDate.getTime() - startDate.getTime();
            Timber.e(String.valueOf(diff));
            long hours = diff / (1000 * 60 * 60);
            if (hours < 1) {
                if(totalRgs>0) {
                    endCollectionAvg.setVisibility(View.VISIBLE);
                    endCollectionAvg.setText("/ " + totalRgs + " avg per hour");
                }else{
                    endCollectionAvg.setVisibility(View.INVISIBLE);
                }
//                endCollectionAvg.setText(R.string.less_than_hour);
//                endCollectionAvg.setText(" / " + endCollectionAvg.getText());
            } else {
                double avg = (double) data.getTotalRegistrations() / hours;
                endCollectionAvg.setText(new DecimalFormat("##.##").format(avg)+ " avg per hour");
            }

            Timber.e(String.valueOf(diff / (1000 * 60 * 60)));
            Timber.e(String.valueOf(diff / (1000 * 60)));

        }
        String hm = null;
        if (startDate != null) {
            hm = fhm.format(startDate).toLowerCase();
            endCollectionInfo.setText("Collection Ended at " + hm + "on " + convertDateToString(startDate) + " for " + firstName + " " + lastName);
        }

        if (data.getTotalRegistrations() == 0 && data.getAbandonedRegistrations() == 0) {
            setClickableSpan(false,firstName+" "+lastName);
        } else {
            setClickableSpan(false,"");
        }


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
            Timber.e("EventEndSHiftCalc");
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
//            if (abandonedRegistrations > 0) {
//                Timber.e("EventCollectionStatus");
//                smrAbandonedInPrt.setVisibility(View.VISIBLE);
//                smrAbandonedInPrt.setText("/ " + new DecimalFormat("##.##").format((float)abandonedRegistrations / totalRgs * 100) + "% of total");
//            }
        }

        summaryRegistrations.setText(String.valueOf(totalRgs));
        summaryDln.setText(String.valueOf(totalDln));
        summaryEmailOptIn.setText(String.valueOf(emailOptInCount));
        summarySsn.setText(String.valueOf(ssnCount));
        summarySmsOptIn.setText(String.valueOf(smsCount));
        summaryAbandoned.setText(String.valueOf(abandonedRegistrations));
        scflToUploadCount.setText(String.valueOf(data.getRegistrations().size()));
    }


    private String convertDateToString(Date date) {
        DateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy ", Locale.US);
        String mdy = null;
        if (date != null) {
            mdy = fmt.format(date);
        }
        if (mdy != null) {
            return mdy = mdy.substring(0, mdy.indexOf(',') + 1) + "\n" + mdy.substring(mdy.indexOf(',') + 1);
        } else return " ";
    }


    private void createDialog(String canvasserName) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Dialog partnerDialog = new Dialog(this.getContext());
        partnerDialog.setContentView(R.layout.end_strangershift_dialog);
        MaterialTextView endStrTxt = partnerDialog.findViewById(R.id.end_stranger_text);
        MaterialTextView shureBtn = partnerDialog.findViewById(R.id.shure_end_shift_btn);
        MaterialTextView cnlBtn = partnerDialog.findViewById(R.id.cancel_stranger_btn);


        String partnerName = "<b>WARNING: </b>";
        String canvasserNameHtml = "<b>" +canvasserName+"</b> shift.<br>";
        String endText = this.getResources().getString(R.string.you_are_about_end_strg);
        String shreText = this.getResources().getString(R.string.shre_text);

        endStrTxt.setText(Html.fromHtml(partnerName + endText + "\n" + canvasserNameHtml + shreText));
        Objects.requireNonNull(partnerDialog.getWindow()).setLayout((9 * width) / 10, (2 * height) / 5);

        cnlBtn.setOnClickListener(v -> {
            partnerDialog.hide();
        });
        shureBtn.setOnClickListener(v -> {
            partnerDialog.hide();
//            listener.setState(END_STRANGER_COLLECTION, true);
        });
        partnerDialog.show();
    }

    @OnClick(R.id.end_clt_stranger)
    public void onClickImpSt(View v) {
        createDialog(canvasserName);
    }



    @OnClick(R.id.upload_registrations_btn)
    public void onClickEndCltn(View v) {
        v.setAlpha(0.8f);
        viewModel.updateShift();
        v.setAlpha(1);
//        listener.setState(END_SHIFT, true);
    }

}

