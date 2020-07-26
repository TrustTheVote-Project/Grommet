package com.rockthevote.grommet.ui.eventFlow;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.android.material.textfield.TextInputLayout;
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
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.rockthevote.grommet.data.db.model.SessionStatus.NEW_SESSION;

/**
 * Created by Mechanical Man on 7/14/18.
 */
public class EventPartnerLogin extends FrameLayout implements EventFlowPage {

    @Inject RockyService rockyService;
    @Inject PartnerInfoDao partnerInfoDao;
    @Inject SessionDao sessionDao;

    @NotEmpty
    @BindView(R.id.ede_til_partner_id) TextInputLayout edePartnerIdTIL;
    @BindView(R.id.ede_partner_id) EditText edePartnerId;

    @BindView(R.id.save_view_animator) BetterViewAnimator viewAnimator;

    private ObservableValidator validator;

    private EventFlowCallback listener;

    private PartnerLoginViewModel viewModel;

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

        observeData();
    }

    private void observeData() {
        viewModel.getPartnerLoginState().observe(
                (AppCompatActivity) getContext(), partnerLoginState -> {
                    if (partnerLoginState instanceof PartnerLoginState.Init) {
                        viewAnimator.setDisplayedChildId(R.id.event_partner_id_save);
                        edePartnerId.setEnabled(true);

                    } else if (partnerLoginState instanceof PartnerLoginState.Loading) {
                        viewAnimator.setDisplayedChildId(R.id.save_progress_bar);
                        edePartnerId.setEnabled(false);
                    }
                });

        viewModel.getEffect().observe(
                (AppCompatActivity) getContext(), effect -> {
                    if (effect instanceof PartnerLoginState.Success) {
                        edePartnerIdTIL.setError(null);
                        listener.setState(NEW_SESSION, true);

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
                        edePartnerIdTIL.setError(null);
                        new AlertDialog.Builder(getContext())
                                .setTitle(R.string.update_required)
                                .setIcon(R.drawable.ic_warning_24dp)
                                .setMessage(R.string.login_invalid_version)
                                .setPositiveButton(R.string.action_ok, (dialogInterface, i) -> dialogInterface.dismiss())
                                .create()
                                .show();
                    } else if (effect instanceof PartnerLoginState.NotFound) {
                        edePartnerIdTIL.setError(
                                getContext().getString(R.string.error_partner_id));
                    }
                });

        viewModel.getPartnerInfoPartnerID().observe(
                (AppCompatActivity) getContext(), id -> {
                    if (id != -1) {
                        edePartnerId.setText(String.valueOf(id));
                    } else {
                        edePartnerId.setText("");
                    }
                });
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

    @Override
    public void registerCallbackListener(EventFlowCallback listener) {
        this.listener = listener;
    }

    @Override
    public void unregisterCallbackListener() {
        listener = null;
    }
}
