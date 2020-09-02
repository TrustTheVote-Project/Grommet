package com.rockthevote.grommet.ui.eventFlow;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Pattern;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.api.model.ValidLocation;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.util.EmailOrEmpty;
import com.rockthevote.grommet.util.Phone;
import com.rockthevote.grommet.util.ValidationRegex;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.rockthevote.grommet.data.db.model.SessionStatus.PARTNER_UPDATE;
import static com.rockthevote.grommet.data.db.model.SessionStatus.START_COLLECTION;


public class EventCanvasserInfo extends LinearLayout implements EventFlowPage {

    @Inject
    FusedLocationProviderClient fusedLocationProviderClient;
    @Inject
    PartnerInfoDao partnerInfoDao;
    @Inject
    SessionDao sessionDao;
    @Inject
    RockyService rockyService;

    @BindView(R.id.ede_canvasser_first_name)
    EditText edeCanvasserFirstName;
    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.ede_til_event_zdip)
    TextInputLayout firstNameEventPhoneTIL;


    @BindView(R.id.ede_canvasser_last_name)
    EditText edeCanvasserLastName;
    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.last_name_til_event_zdip)
    TextInputLayout lastNameEventPhoneTIL;

    private PhoneNumberFormattingTextWatcher phoneFormatter;


    @BindView(R.id.email_eventd)
    TextView emailText;

    //    @Pattern(regex = ValidationRegex.EMAIL, messageResId = R.string.email_error_v4)
    @EmailOrEmpty(messageResId = R.string.email_error_v4)
    @BindView(R.id.email_event_zdip)
    TextInputLayout emailTextTIL;

    @BindView(R.id.location_event_zdip)
    TextInputLayout locationListTIL;

    @Pattern(regex = ValidationRegex.PHONE, messageResId = R.string.phone_format_error_v4)
    @BindView(R.id.phone_event_zdip)
    TextInputLayout edeEventPhoneTIL;
    @BindView(R.id.phone_ede_eventd)
    EditText edePhone;

    @BindView(R.id.partner_name_info)
    TextView partnerName;
    @BindView(R.id.spinner)
    Spinner locationSpinner;
    @BindView(R.id.event_details_save)
    MaterialTextView detailsSaveBtn;


    private ObservableValidator validator;

    private EventFlowCallback listener;

    private CanvasserInfoViewModel viewModel;

    private ArrayList<ValidLocation> currentLocations;

    private Observer<? super CanvasserInfoData> canvasserInfoDataObserver = this::handleCanvasserInfoData;
    private Observer<? super CanvasserInfoState> canvasserInfoStateObserver = this::handleEffectEvent;

    public EventCanvasserInfo(Context context) {
        this(context, null);
    }

    public EventCanvasserInfo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventCanvasserInfo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_canvasser_info_v4, this);

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

        Validator.registerAnnotation(EmailOrEmpty.class);
        viewModel = new ViewModelProvider(
                (AppCompatActivity) getContext(),
                new CanvasserInfoViewModelFactory(partnerInfoDao, sessionDao, fusedLocationProviderClient, rockyService)
        ).get(CanvasserInfoViewModel.class);

//        observeData();

    }


    private void setLocationSpinner(ArrayList<ValidLocation> locations) {
        ArrayAdapter<ValidLocation> adapter = new ArrayAdapter<ValidLocation>(this.getContext(),
                android.R.layout.simple_spinner_item, locations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ValidLocation user = (ValidLocation) parent.getSelectedItem();
//                displayUserData(user);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private ValidLocation getLocation(String currentLocation) {
        ValidLocation neededLocation = null;
        if (currentLocations != null) {
            for (ValidLocation location : currentLocations) {
                if (location.getName() == currentLocation) {
                    neededLocation = location;
                }
            }
        }
        return neededLocation;
    }

    private void handleCanvasserInfoData(CanvasserInfoData data) {
//        viewModel.getCanvasserInfoData().observe(
//                (AppCompatActivity) getContext(), data -> {
        partnerName.setText(data.getPartnerName());
        edeCanvasserFirstName.setText(data.getCanvasserName());
        edeCanvasserLastName.setText(data.getCanvasserLastName());
//                    edeEventName.setText(data.getOpenTrackingId());
        edePhone.setText(data.getCanvasserPhone());
        emailText.setText(data.getCanvasserMail());
//                    locationList.setText(data.getLocations().toString());
        currentLocations = data.getLocations();

        setLocationSpinner(currentLocations);
//                    edeEventZip.setText(data.getPartnerTrackingId());
//                    edeDeviceId.setText(data.getDeviceId());
    }
//        );

//    }

    private void handleEffectEvent(CanvasserInfoState effect) {
//        viewModel.getEffect().observe(
//                (AppCompatActivity) getContext(), effect -> {
        if (effect instanceof CanvasserInfoState.Success) {
//                        listener.setState(DETAILS_ENTERED, true);
            if (listener != null) {
                listener.setState(START_COLLECTION, true);
            }
            detailsSaveBtn.setAlpha(1);
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

    @OnClick(R.id.event_update_partner_id)
    public void onClickUpdatePartner(View v) {
        listener.setState(PARTNER_UPDATE, true);
    }

    @SuppressLint("HardwareIds")
    @OnClick(R.id.event_details_save)
    public void onClickSaveDetails(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager)
                getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

        if (validator.validate().toBlocking().single()) {
            v.setAlpha(0.8f);
            Timber.d(emailText.getText().toString());
            viewModel.createShift(
                    edeCanvasserFirstName.getText().toString(),
                    edeCanvasserLastName.getText().toString(),
                    String.valueOf(getLocation(locationSpinner.getSelectedItem().toString()).getId()),
                    getLocation(locationSpinner.getSelectedItem().toString()).getName(),
                    emailText.getText().toString(),
                    edePhone.getText().toString(),
                    Settings.Secure.getString(getContext().getContentResolver(),
                            Settings.Secure.ANDROID_ID));
        }
    }

    private void registerDataObservers() {
        viewModel.getCanvasserInfoData().observe((AppCompatActivity) getContext(), canvasserInfoDataObserver);
        viewModel.getEffect().observe((AppCompatActivity) getContext(), canvasserInfoStateObserver);
    }

    private void unregisterDataObservers() {
        viewModel.getCanvasserInfoData().removeObserver(canvasserInfoDataObserver);
        viewModel.getEffect().removeObserver(canvasserInfoStateObserver);

    }

    @Override
    public void registerCallbackListener(EventFlowCallback listener) {
        registerDataObservers();
        phoneFormatter = new PhoneNumberFormattingTextWatcher("US");
        edePhone.addTextChangedListener(phoneFormatter);
        this.listener = listener;
    }

    @Override
    public void unregisterCallbackListener() {
        unregisterDataObservers();
        edePhone.removeTextChangedListener(phoneFormatter);
        listener = null;
    }
}
