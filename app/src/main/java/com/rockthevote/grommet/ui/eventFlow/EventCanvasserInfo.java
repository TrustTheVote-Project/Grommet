package com.rockthevote.grommet.ui.eventFlow;


import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.textfield.TextInputLayout;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Pattern;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.ui.misc.ObservableValidator;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.rockthevote.grommet.data.db.model.SessionStatus.DETAILS_ENTERED;
import static com.rockthevote.grommet.data.db.model.SessionStatus.PARTNER_UPDATE;


public class EventCanvasserInfo extends LinearLayout implements EventFlowPage {

    @Inject FusedLocationProviderClient fusedLocationProviderClient;
    @Inject PartnerInfoDao partnerInfoDao;
    @Inject SessionDao sessionDao;

    @BindView(R.id.ede_canvasser_name) EditText edeCanvasserName;
    @BindView(R.id.ede_event_name) EditText edeEventName;

    @Pattern(regex = "^[0-9]{5}(?:-[0-9]{4})?$", messageResId = R.string.zip_code_error)
    @BindView(R.id.ede_til_event_zip) TextInputLayout edeEventZipTIL;
    @BindView(R.id.ede_event_zip) EditText edeEventZip;
    @BindView(R.id.ede_partner_name) TextView edePartnerName;

    @NotEmpty
    @BindView(R.id.ede_til_device_id) TextInputLayout edeDeviceIdTIL;
    @BindView(R.id.ede_device_id) TextView edeDeviceId;

    private ObservableValidator validator;

    private EventFlowCallback listener;

    private CanvasserInfoViewModel viewModel;

    public EventCanvasserInfo(Context context) {
        this(context, null);
    }

    public EventCanvasserInfo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventCanvasserInfo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_canvasser_info, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            ButterKnife.bind(this);
            validator = new ObservableValidator(this, getContext());

        }

        viewModel = new ViewModelProvider(
                (AppCompatActivity) getContext(),
                new CanvasserInfoViewModelFactory(partnerInfoDao, sessionDao, fusedLocationProviderClient)
        ).get(CanvasserInfoViewModel.class);

        observeData();
    }

    private void observeData() {
        viewModel.getCanvasserInfoData().observe(
                (AppCompatActivity) getContext(), data -> {
                    edePartnerName.setText(data.getPartnerName());
                    edeCanvasserName.setText(data.getCanvasserName());
                    edeEventName.setText(data.getOpenTrackingId());
                    edeEventZip.setText(data.getPartnerTrackingId());
                    edeDeviceId.setText(data.getDeviceId());
                }
        );

        viewModel.getEffect().observe(
                (AppCompatActivity) getContext(), effect -> {
                    if (effect instanceof CanvasserInfoState.Success) {
                        listener.setState(DETAILS_ENTERED, true);
                    } else if (effect instanceof CanvasserInfoState.Error) {
                        Toast.makeText(
                                getContext(),
                                R.string.error_updating_canvasser_info,
                                Toast.LENGTH_LONG
                        ).show();
                        Timber.e("error updating view after updating canvasser info");
                    } else if (effect instanceof CanvasserInfoState.LocationError) {
                        Toast.makeText(
                                getContext(),
                                R.string.error_location,
                                Toast.LENGTH_LONG
                        ).show();
                        Timber.e("error updating view due to location error");
                    }
                }
        );
    }

    @OnClick(R.id.event_update_partner_id)
    public void onClickUpdatePartner(View v) {
        listener.setState(PARTNER_UPDATE, true);
    }

    @OnClick(R.id.event_details_save)
    public void onClickSaveDetails(View v) {

        InputMethodManager inputMethodManager = (InputMethodManager)
                getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

        // allow the user to not set a partner ID
        if (validator.validate().toBlocking().single()) {

            viewModel.updateCanvasserInfo(
                    edeCanvasserName.getText().toString(),
                    edeEventZip.getText().toString(),
                    edeEventName.getText().toString(),
                    edeDeviceId.getText().toString());
        }
    }

    @Override
    public void registerCallbackListener(EventFlowCallback listener) {
        this.listener = listener;
    }

    @Override
    public void unregisterCallbackListener() {
        listener = null;
    }
}
