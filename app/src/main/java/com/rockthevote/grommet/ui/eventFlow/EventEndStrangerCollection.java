package com.rockthevote.grommet.ui.eventFlow;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.textview.MaterialTextView;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.data.db.dao.RegistrationDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.data.db.model.SessionStatus;

import java.text.DateFormat;
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
import static com.rockthevote.grommet.data.db.model.SessionStatus.SESSION_CLEARED;
import static com.rockthevote.grommet.data.db.model.SessionStatus.STATUS_COLLECTION;

public class EventEndStrangerCollection extends FrameLayout implements EventFlowPage {

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

    @BindView(R.id.stranger_end_text)
    MaterialTextView strangerEndText;

    @BindView(R.id.stranger_end_info)
    MaterialTextView strangerEndInfo;

    private Observer<? super SessionSummaryData> sessionSummaryDataObserver = this::handleSessionSummary;
    private Observer<? super UploadRegistrationState> uploadStateEventObserver = this::handleUploadRegistrEvent;


    private EventFlowCallback listener;

    private SessionTimeTrackingViewModel viewModel;

    public EventEndStrangerCollection(Context context) {
        this(context, null);
    }

    public EventEndStrangerCollection(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventEndStrangerCollection(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_end_stranger_shift, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }


    private void registerDataObservers() {
        viewModel.getSessionData().observe((AppCompatActivity) getContext(), sessionSummaryDataObserver);
        viewModel.getState().observe((AppCompatActivity) getContext(), uploadStateEventObserver);
    }

    private void unregisterDataObservers() {
        viewModel.getSessionData().removeObserver(sessionSummaryDataObserver);
        viewModel.getState().removeObserver(uploadStateEventObserver);
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



    private void handleSessionSummary(SessionSummaryData data) {
        DateFormat fhm = new SimpleDateFormat("hh:mm aa ", Locale.US);
        String firstName = data.getCanvasserName();
        String lastName = data.getCanvasserLastName();
        Date date = data.getClockOutTime();
        String hm = null;
        if (date != null) {
            hm = fhm.format(date).toLowerCase();
            strangerEndInfo.setText("Collection Ended at " + hm + "on " + convertDateToString(date) + "by a canvasser on behalf of " + firstName + " " + lastName);
        }


        String canvasserNameHtml = "<b> " +firstName+" "+lastName+ " </b>";
        String fullText = this.getResources().getString(R.string.you_have_ended_cln_full);
        String fPart = this.getResources().getString(R.string.you_have_ended_cln);
        String sPart = this.getResources().getString(R.string.you_have_ended_cln_part_sec);
        String tPart = this.getResources().getString(R.string.you_have_ended_cln_part_third);
        String fourthPart = this.getResources().getString(R.string.you_have_ended_cln_part_fourth);

//        strangerEndText.setText(Html.fromHtml(fPart +  canvasserNameHtml +sPart +"<br>"+ tPart+" "+fourthPart));
        strangerEndText.setText(Html.fromHtml(fPart +  canvasserNameHtml + fullText));
    }

    private void handleUploadRegistrEvent(UploadRegistrationState event) {
        if (event instanceof UploadRegistrationState.Content) {

            int failedUpload = ((UploadRegistrationState.Content) event).getFailedUploads();
            int pendUpload = ((UploadRegistrationState.Content) event).getPendingUploads();
//            scflUploadedCount.setText(String.valueOf(failedUpload));
//            scflToUploadCount.setText(String.valueOf(pendUpload));

            if(failedUpload>0){
//                endCollectionError.setVisibility(View.VISIBLE);
//                endClnWarning.setVisibility(View.GONE);
            }else{
//                endCollectionError.setVisibility(View.VISIBLE);
//                endClnWarning.setVisibility(View.GONE);
                listener.setState(END_SHIFT,true);
            }
        } else if (event instanceof UploadRegistrationState.Error) {
//            endCollectionError.setVisibility(View.VISIBLE);
//            endClnWarning.setVisibility(View.GONE);
//            showClockInSpinner();
        }
    }


    private String convertDateToString(Date date) {
        DateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy ", Locale.US);
        String mdy = null;
        if (date != null) {
            mdy = fmt.format(date);
        }
        if (mdy != null) {
            return mdy = mdy.substring(0, mdy.indexOf(',') + 1) +  mdy.substring(mdy.indexOf(',') + 1);
        } else return " ";
    }

    @Override
    public void registerCallbackListener(EventFlowCallback listener) {
        this.listener = listener;
        registerDataObservers();
    }

    @Override
    public void unregisterCallbackListener() {
        unregisterDataObservers();
        listener = null;
    }


    private boolean checkWifiOnAndConnected() {







            WifiManager wifiMgr = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiMgr != null) {
                if (wifiMgr.isWifiEnabled()) {

                    WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

                    return wifiInfo.getNetworkId() != -1; // Not connected to an access point
                }
                else {
                    return false;
                }
            }else {
                return false;
            }




    }

    @OnClick(R.id.upload_stranger_registrations_btn)
    public void onRegisterVoterClick(View v) {
        if(!checkWifiOnAndConnected()){
            createDialog();
        }else{
            viewModel.uploadRegistrations();
        }
    }

    private void createDialog() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Dialog partnerDialog = new Dialog(this.getContext());
        partnerDialog.setContentView(R.layout.bad_connection_dialog);
        MaterialTextView dsmBtn = partnerDialog.findViewById(R.id.dismiss_bc_dialog_btn);

        Objects.requireNonNull(partnerDialog.getWindow()).setLayout((8 * width) / 9, (2 * height) / 5);
        dsmBtn.setOnClickListener(v -> {
            partnerDialog.hide();
        });
        partnerDialog.show();
    }
}