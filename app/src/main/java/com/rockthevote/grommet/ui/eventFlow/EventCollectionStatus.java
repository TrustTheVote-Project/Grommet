package com.rockthevote.grommet.ui.eventFlow;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import com.rockthevote.grommet.ui.registration.RegistrationActivity;

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

import static com.rockthevote.grommet.data.db.model.SessionStatus.END_COLLECTION;
import static com.rockthevote.grommet.data.db.model.SessionStatus.END_SHIFT;
import static com.rockthevote.grommet.data.db.model.SessionStatus.END_STRANGER_COLLECTION;
import static com.rockthevote.grommet.data.db.model.SessionStatus.PARTNER_UPDATE;
import static com.rockthevote.grommet.data.db.model.SessionStatus.SPLASH;

public class EventCollectionStatus extends FrameLayout implements EventFlowPage {

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

    @BindView(R.id.impostor_status_text)
    MaterialTextView impostorStatusText;

    //    @BindView(R.id.clock_in_button) View clockInButton;
//    @BindView(R.id.event_details_static_edit) Button editButton;
    @BindView(R.id.summary_total_registrations)
    TextView summaryRegistrations;
    @BindView(R.id.collection_start_text)
    MaterialTextView cltStartText;
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
    @BindView(R.id.collection_status_last_name)
    TextView lastRegisteredName;

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

    @BindView(R.id.register_voter_btn)
    MaterialTextView registerVoterBtn;
    @BindView(R.id.avg_rgs)
    TextView avgRgs;
    @BindView(R.id.last_completed_registr_section)
    LinearLayout lcrSection;

    private String canvasserName = "";
    private EventFlowCallback listener;

    private SessionTimeTrackingViewModel viewModel;
    private int totalRgs;
//    private Observer<? super SessionStatus> sessionStatusObserver = this::updateUI;
    private Observer<? super SessionSummaryData> sessionSummaryDataObserver = this::handleSessionSummary;
    private Observer<? super EndCollectionState> endStgEventObserver = this::handleEndStgEvent;

    public EventCollectionStatus(Context context) {
        this(context, null);
    }

    public EventCollectionStatus(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventCollectionStatus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_collection_status, this);

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

    private void setClickableSpan(Boolean isEmpty,String canvasserName) {
        SpannableString ss = new SpannableString(getResources().getString(R.string.if_this_isn_t_you_click_here));
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
//                if (isEmpty) {
//                    viewModel.clearSession();
//                    listener.setState(PARTNER_UPDATE, true);
//                } else {
////                    createDialog(canvasserName);
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
        impostorStatusText.setText(ss);
        impostorStatusText.setMovementMethod(LinkMovementMethod.getInstance());
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
//        viewModel.getClockState().observe((AppCompatActivity) getContext(), clockEventObserver);
    }

    private void unregisterDataObservers() {
        viewModel.getSessionData().removeObserver(sessionSummaryDataObserver);
        viewModel.getEndStrState().removeObserver(endStgEventObserver);
//        viewModel.getSessionStatus().removeObserver(sessionStatusObserver);
//        viewModel.getClockState().removeObserver(clockEventObserver);
    }


    private void handleEndStgEvent(EndCollectionState event) {
        Timber.e("lox %s", event.toString());
        if (event instanceof EndCollectionState.DoneStrangerClt) {

            if(totalRgs>0){
                listener.setState(END_STRANGER_COLLECTION, true);
            }else{
                createEndDialog();
            }
        } else if (event instanceof EndCollectionState.Done) {
            if(totalRgs>0){
                listener.setState(END_COLLECTION, true);
            }else{
                listener.setState(END_SHIFT, true);
            }
        }else {

        }
    }

    private void createEndDialog() {

        Dialog endShiftDialog = new Dialog(this.getContext(),android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        endShiftDialog.setContentView(R.layout.end_shift_dialog);
        MaterialTextView doneBtn = endShiftDialog.findViewById(R.id.done_es_btn);
        MaterialTextView endShiftTxt = endShiftDialog.findViewById(R.id.end_dl_shift_name);
        GifImageView gif = endShiftDialog.findViewById(R.id.gif_anim);
        gif.setVisibility(View.VISIBLE);
        String endText = "Thank you, "+canvasserName+"!";

        endShiftTxt.setText(endText);
        endShiftDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        doneBtn.setOnClickListener(v -> {
            viewModel.clearSession();
            listener.setState(SPLASH,false);
            gif.setVisibility(View.GONE);
            endShiftDialog.hide();
        });

        endShiftDialog.show();
    }

    private void handleSessionSummary(SessionSummaryData data) {

        DateFormat fhm = new SimpleDateFormat("hh:mm aa ", Locale.US);
        canvasserName = data.getCanvasserName()+ " "+data.getCanvasserLastName();
        String firstName = data.getCanvasserName();
        String lastName = data.getCanvasserLastName();
        Date startDate = data.getClockInTime();
        String hm = null;
        if (startDate != null) {
            hm = fhm.format(startDate).toLowerCase();
            cltStartText.setText("Collection Start"+" for " + firstName + " " + lastName + " at "+ hm + "on " + convertDateToString(startDate) );
        }

        totalRgs = data.getTotalRegistrations();

        if(startDate!=null){
            long diff = new Date().getTime() - startDate.getTime();

            long hours = diff/(1000*60*60);
            if(hours<1 ){
                if(totalRgs>0) {
                    avgRgs.setVisibility(View.VISIBLE);
                    avgRgs.setText("/ " + totalRgs + " avg per hour");
                }else{
                    avgRgs.setVisibility(View.INVISIBLE);
                }
            }else{
                double avg =  (double)totalRgs / hours;
//                avgRgs.setText(avg + " avg per hour");
                avgRgs.setText(new DecimalFormat("##.##").format(avg)+ " avg per hour");
            }

        }

        if (data.getRegistrations().isEmpty()) {
            lastRegisteredName.setText("");
        } else {
            lastRegisteredName.setText(viewModel.getRegistrationFromString(data.getRegistrations().get(data.getRegistrations().size() - 1).getRegistrationData()));
        }
        if (data.getTotalRegistrations() == 0 && data.getAbandonedRegistrations() == 0) {
            setClickableSpan(false,canvasserName);
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

        if(totalRgs>0){
            lcrSection.setVisibility(View.VISIBLE);
            if (ssnCount > 0) {
                smrSsnPrt.setVisibility(View.VISIBLE);
//                smrSsnPrt.setText("/ " + new DecimalFormat("##.##").format((float)emailOptInCount / totalRgs * 100) + "% of total");
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
        }else{
            lcrSection.setVisibility(View.GONE);
        }

//        if(totalRgs<1){
//            lcrSection.setVisibility(View.GONE);
//        }else{
//        }
        summaryRegistrations.setText(String.valueOf(totalRgs));
        summaryDln.setText(String.valueOf(totalDln));
        summaryEmailOptIn.setText(String.valueOf(emailOptInCount));
        summarySsn.setText(String.valueOf(ssnCount));
        summarySmsOptIn.setText(String.valueOf(smsCount));
        summaryAbandoned.setText(String.valueOf(abandonedRegistrations));
    }


    private String convertDateToString(Date date) {
        DateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy ", Locale.US);
        String mdy = null;
        if (date != null) {
            mdy = fmt.format(date);
        }
        if (mdy != null) {
            return mdy = mdy.substring(0, mdy.indexOf(',') + 1) + mdy.substring(mdy.indexOf(',') + 1);
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
            viewModel.endCollection(new Date(),true);
//            listener.setState(END_STRANGER_COLLECTION, true);
        });
        partnerDialog.show();
    }



    @OnClick(R.id.register_voter_btn)
    public void onRegisterVoterClick(View v) {
//        SessionProgressDialogFragment.newInstance()
//                .show(((AppCompatActivity) getContext()).getSupportFragmentManager(),
//                        "session_progress");
        registerVoterBtn.setAlpha(0.8f);
        getContext().startActivity(new Intent(getContext().getApplicationContext(), RegistrationActivity.class),
                ActivityOptions.makeSceneTransitionAnimation((Activity) getContext()).toBundle());
        registerVoterBtn.setAlpha(1);
    }

    @OnClick(R.id.end_collection_btn)
    public void onClickEndCltn(View v) {
        createDialog(false);
    }

    @OnClick(R.id.impostor_status_text)
    public void onClickImpSt(View v) {
        createDialog(canvasserName);
    }


    private void createDialog(boolean isStranger) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Dialog partnerDialog = new Dialog(this.getContext());
        partnerDialog.setContentView(R.layout.end_collection_dialog);
        MaterialTextView cnfBtn = partnerDialog.findViewById(R.id.confirm_end_cln_btn);
        MaterialTextView endText = partnerDialog.findViewById(R.id.end_collection_text);
        MaterialTextView cnlBtn = partnerDialog.findViewById(R.id.cancel_end_dialog_btn);

        String partnerName = "<b>WARNING: </b>";
        String secPart = this.getResources().getString(R.string.you_are_about_end);
        String thirdPart = this.getResources().getString(R.string.confirm_or_cancel);

        endText.setText(Html.fromHtml(partnerName + "\n" + secPart + "<br>" + thirdPart));
        Objects.requireNonNull(partnerDialog.getWindow()).setLayout((8 * width) / 9, (2 * height) / 5);
        cnlBtn.setOnClickListener(v -> {
            partnerDialog.dismiss();
        });
        cnfBtn.setOnClickListener(v -> {
            viewModel.endCollection(new Date(),isStranger);
            partnerDialog.dismiss();
        });
        partnerDialog.show();
    }

}

