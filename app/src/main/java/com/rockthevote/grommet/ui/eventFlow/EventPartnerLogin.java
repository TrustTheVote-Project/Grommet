package com.rockthevote.grommet.ui.eventFlow;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Html;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.ui.misc.BetterViewAnimator;
import com.rockthevote.grommet.ui.misc.ObservableValidator;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static com.rockthevote.grommet.data.db.model.SessionStatus.NEW_SESSION;

/**
 * Created by Mechanical Man on 7/14/18.
 */
public class EventPartnerLogin extends FrameLayout implements EventFlowPage {

    @Inject
    RockyService rockyService;
    @Inject
    PartnerInfoDao partnerInfoDao;
    @Inject
    SessionDao sessionDao;

    @NotEmpty
    @BindView(R.id.ede_til_partner_id)
    TextInputLayout edePartnerIdTIL;
    @BindView(R.id.ede_partner_id)
    EditText edePartnerId;

    @BindView(R.id.save_view_animator)
    BetterViewAnimator viewAnimator;

    private ObservableValidator validator;

    private EventFlowCallback listener;

    private PartnerLoginViewModel viewModel;


    private Observer<? super PartnerLoginState> partnerLoginStateObserver = this::handlePartnerLoginState;
    private Observer<? super String> partnerNameObserver = this::handlePartnerNameState;
    private Observer<? super PartnerLoginState.Effect> partnerLoginEffectObserver = this::handleEffectState;

    public EventPartnerLogin(Context context) {
        this(context, null);
    }

    public EventPartnerLogin(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventPartnerLogin(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_partner_login, this);

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
                new PartnerLoginViewModelFactory(rockyService, partnerInfoDao, sessionDao)
        ).get(PartnerLoginViewModel.class);

    }

    private void handlePartnerLoginState(PartnerLoginState partnerLoginState){
        if (partnerLoginState instanceof PartnerLoginState.Init) {
            viewAnimator.setDisplayedChildId(R.id.event_partner_id_save);
            edePartnerId.setEnabled(true);

        } else if (partnerLoginState instanceof PartnerLoginState.Loading) {
            viewAnimator.setDisplayedChildId(R.id.save_progress_bar);
            edePartnerId.setEnabled(false);
        }
    }

    private void handlePartnerNameState(String name){
        createDialog(null, name);
        edePartnerIdTIL.setError(null);
    }

    private void handleEffectState(PartnerLoginState.Effect effect){
        if (effect instanceof PartnerLoginState.Success) {
//                        edePartnerIdTIL.setError(null);
//                        createDialog(effect, "");

        } else if (effect instanceof PartnerLoginState.Error) {
            edePartnerIdTIL.setError(
                    getContext().getString(R.string.error_partner_id));

            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.error_title)
                    .setIcon(R.drawable.ic_warning_24dp)
                    .setMessage(R.string.login_no_wifi_error)
                    .setPositiveButton(R.string.action_ok, (dialogInterface, i) -> dialogInterface.dismiss())
                    .create()
                    .show();

        } else if (effect instanceof PartnerLoginState.InvalidVersion) {
            edePartnerIdTIL.setError("Partner ID not found. Please check the Partner ID and try again.");
        } else if (effect instanceof PartnerLoginState.NotFound) {
            createDialog(effect, "");
            edePartnerIdTIL.setError(
                    getContext().getString(R.string.error_partner_id));
        }
    }

//        viewModel.getPartnerInfoPartnerID().observe(
//                (AppCompatActivity) getContext(), id -> {
//                    if (id != -1) {
//                        edePartnerId.setText(String.valueOf(id));
//                    } else {
//                        edePartnerId.setText("");
//                    }
//                });

    private void createDialog(PartnerLoginState state, String name) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Dialog partnerDialog = new Dialog(this.getContext());
        partnerDialog.setContentView(R.layout.partner_id_dialog);
        MaterialTextView rtyBtn = partnerDialog.findViewById(R.id.retry_id_dialog_btn);
        MaterialTextView cntBtn = partnerDialog.findViewById(R.id.partner_continue_btn);
        MaterialTextView ptrVersion = partnerDialog.findViewById(R.id.partner_version_text);

        if (state instanceof PartnerLoginState.NotFound) {

            ptrVersion.setText(this.getResources().getString(R.string.tbd));
            cntBtn.setVisibility(View.INVISIBLE);
            rtyBtn.setVisibility(View.INVISIBLE);
        } else {
            String partnerName = "<b> " + name + " <br></b>";
            String partnerCorrectText = "\nIf this is the correct Partner, please click <i>Continue.</i>";
            String partnerIdFound = this.getResources().getString(R.string.partner_id_found);
            String otherwiseClickRetry = "<br>Otherwise, click <i>Retry</i> to try again.";
            ptrVersion.setText(Html.fromHtml(partnerIdFound + partnerName + "\n" + partnerCorrectText + otherwiseClickRetry));
            Objects.requireNonNull(partnerDialog.getWindow()).setLayout((8 * width) / 9, (2 * height) / 5);
        }
        rtyBtn.setOnClickListener(v -> {
            partnerDialog.dismiss();
        });
        cntBtn.setOnClickListener(v -> {
            partnerDialog.dismiss();
            listener.setState(NEW_SESSION, true);
//            partnerDialog.hide();
        });
        partnerDialog.show();
    }

    @OnClick(R.id.event_partner_id_save)
    public void onClickSave(View v) {

        InputMethodManager inputMethodManager = (InputMethodManager)
                getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

        if (validator.validate().toBlocking().single()) {
            viewModel.validatePartnerId(Long.parseLong(edePartnerId.getText().toString()));
        }
    }

    @OnClick(R.id.clear_partner_info)
    public void onClickClearPartnerInfo(View v) {
        edePartnerId.setText("");
        viewModel.clearPartnerInfo();
    }

    private void registerDataObservers() {
        viewModel.getPartnerLoginState().observe((AppCompatActivity) getContext(), partnerLoginStateObserver);
        viewModel.getPartnerName().observe((AppCompatActivity) getContext(), partnerNameObserver);
        viewModel.getEffect().observe((AppCompatActivity) getContext(), partnerLoginEffectObserver);
    }

    private void unregisterDataObservers() {
        viewModel.getPartnerLoginState().removeObserver(partnerLoginStateObserver);
        viewModel.getPartnerName().removeObserver(partnerNameObserver);
        viewModel.getEffect().removeObserver(partnerLoginEffectObserver);
    }


    @Override
    public void registerCallbackListener(EventFlowCallback listener) {
        registerDataObservers();
        this.listener = listener;
    }

    @Override
    public void unregisterCallbackListener() {
        unregisterDataObservers();
        listener = null;
    }
}
