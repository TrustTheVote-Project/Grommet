package com.rockthevote.grommet.ui.eventFlow;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.EditText;
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
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.ui.misc.ObservableValidator;

import java.util.Date;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.rockthevote.grommet.data.db.model.SessionStatus.PARTNER_UPDATE;
import static com.rockthevote.grommet.data.db.model.SessionStatus.SESSION_CLEARED;
import static com.rockthevote.grommet.data.db.model.SessionStatus.SPLASH;
import static com.rockthevote.grommet.data.db.model.SessionStatus.START_COLLECTION;
import static com.rockthevote.grommet.data.db.model.SessionStatus.STATUS_COLLECTION;

public class EventStartCollection extends FrameLayout implements EventFlowPage {

    @Inject
    FusedLocationProviderClient fusedLocationProviderClient;
    @Inject
    PartnerInfoDao partnerInfoDao;
    @Inject
    SessionDao sessionDao;
    @Inject
    RockyService rockyService;

    @BindView(R.id.start_collection_name)
    MaterialTextView startCollectionName;

    @BindView(R.id.impostor_text)
    MaterialTextView impostorText;

    @BindView(R.id.start_collection_btn)
    MaterialTextView startClnBtn;

    private EventFlowCallback listener;

    private CanvasserInfoViewModel viewModel;

    private Observer<? super CanvasserInfoData> canvasserInfoDataObserver = this::handleCanvasserInfoData;
    private Observer<? super StartCollectionEvent> startCollectionEventObserver = this::handleStartCollectionData;



    public EventStartCollection(Context context) {
        this(context, null);
    }

    public EventStartCollection(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventStartCollection(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_start_collection, this);

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
                new CanvasserInfoViewModelFactory(partnerInfoDao, sessionDao, fusedLocationProviderClient, rockyService)
        ).get(CanvasserInfoViewModel.class);

//        observeData();
        setClickableSpan();
    }


    private void setClickableSpan() {
        SpannableString ss = new SpannableString(getResources().getString(R.string.if_this_isn_t_you_click_here));
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                createDialog(startCollectionName.getText().toString());
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.colorAccent));
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan1, 25, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        impostorText.setText(ss);
        impostorText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void registerDataObservers() {
        viewModel.getCanvasserInfoData().observe((AppCompatActivity) getContext(), canvasserInfoDataObserver);
        viewModel.getStatusState().observe((AppCompatActivity) getContext(), startCollectionEventObserver);
//        viewModel.getClockState().observe((AppCompatActivity) getContext(), clockEventObserver);
    }

    private void unregisterDataObservers() {
        viewModel.getCanvasserInfoData().removeObserver(canvasserInfoDataObserver);
        viewModel.getStatusState().removeObserver(startCollectionEventObserver);
//        viewModel.getSessionStatus().removeObserver(sessionStatusObserver);
//        viewModel.getClockState().removeObserver(clockEventObserver);
    }

    private void handleCanvasserInfoData(CanvasserInfoData data) {
        startClnBtn.setAlpha(1);
//        viewModel.getCanvasserInfoData().observe(
//                (AppCompatActivity) getContext(), data -> {
        startCollectionName.setText(data.getCanvasserName() + " " + data.getCanvasserLastName());
    }

    private void createDialog(String canvasserName) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Dialog endDialog = new Dialog(this.getContext());
        endDialog.setContentView(R.layout.end_strangershift_dialog);
        MaterialTextView endStrTxt = endDialog.findViewById(R.id.end_stranger_text);
        MaterialTextView shureBtn = endDialog.findViewById(R.id.shure_end_shift_btn);
        MaterialTextView cnlBtn = endDialog.findViewById(R.id.cancel_stranger_btn);


        String partnerName = "<b>WARNING: </b>";
        String canvasserNameHtml = "<b>" +canvasserName+"</b> shift.<br>";
        String endText = this.getResources().getString(R.string.you_are_about_end_strg);
        String shreText = this.getResources().getString(R.string.shre_text);

        endStrTxt.setText(Html.fromHtml(partnerName + endText + "\n" + canvasserNameHtml + shreText));
        Objects.requireNonNull(endDialog.getWindow()).setLayout((9 * width) / 10, (2 * height) / 5);

        cnlBtn.setOnClickListener(v -> {
            endDialog.hide();
        });
        shureBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.setState(SPLASH, false);
            }
            viewModel.clearSession();
            endDialog.hide();

        });
        endDialog.show();
    }

    private void handleStartCollectionData(StartCollectionEvent data) {

        if (data instanceof StartCollectionEvent.Done) {
            if (listener != null) {
                listener.setState(STATUS_COLLECTION, true);
            }
        } else {
            Toast.makeText(getContext(), R.string.error_title, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void registerCallbackListener(EventFlowCallback listener) {
        registerDataObservers();
        Timber.d("registeredEventCln");
        this.listener = listener;
    }

    @Override
    public void unregisterCallbackListener() {
        unregisterDataObservers();
        listener = null;
    }

    @OnClick(R.id.start_collection_btn)
    public void onClickUpdatePartner(View v) {
        v.setAlpha(0.8f);
        viewModel.startCollection(new Date());
        v.setAlpha(1f);
    }

    @OnClick(R.id.impostor_text)
    public void onClickImpSt(View v) {
        createDialog(startCollectionName.getText().toString());
    }


}