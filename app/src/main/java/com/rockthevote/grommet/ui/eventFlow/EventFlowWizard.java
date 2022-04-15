package com.rockthevote.grommet.ui.eventFlow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.material.textview.MaterialTextView;
import com.rockthevote.grommet.BuildConfig;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.db.dao.RegistrationDao;
import com.rockthevote.grommet.data.db.model.SessionStatus;
import com.rockthevote.grommet.ui.MainActivityViewModel;
import com.rockthevote.grommet.ui.MainActivityViewModelFactory;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by Mechanical Man, LLC on 7/19/17. Grommet
 */

public class EventFlowWizard extends FrameLayout implements EventFlowCallback {

    @Inject
    RockyService rockyService;
    @Inject
    RegistrationDao registrationDao;
    @Inject
    SharedPreferences sharedPreferences;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private EventDetailFlowAdapter adapter;

    private MainActivityViewModel viewModel;


    public EventFlowWizard(Context context) {
        this(context, null);
    }

    public EventFlowWizard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventFlowWizard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_flow_wizard, this, true);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            ButterKnife.bind(this);
            Timber.e("EventFlowWizard onCreate");

            adapter = new EventDetailFlowAdapter(getContext());
            viewPager.setAdapter(adapter);
            //disable scrolling on the view pager
            viewPager.setOnTouchListener((v, event) -> true);
            viewPager.setOffscreenPageLimit(3);

        }

        viewModel = new ViewModelProvider(
                (AppCompatActivity) getContext(),
                new MainActivityViewModelFactory(rockyService, registrationDao, sharedPreferences)
        ).get(MainActivityViewModel.class);


        viewModel.getSessionStatus().observe((AppCompatActivity) getContext(), sessionStatus -> {
            if (sessionStatus == null) {
                return;
            }
            updateState(sessionStatus, true);
        });
        viewModel.loadSplashSessionStatus();
//        viewModel.getValidVersion("2.1.0");
//        viewModel.getValidVersion(BuildConfig.VERSION_NAME);


        viewModel.isValidVersion().observe((AppCompatActivity) getContext(), validVersionState -> {
            if (validVersionState instanceof ValidVersionState.Valid) {
                viewModel.loadSessionStatus();
//                updateState(SessionStatus.PARTNER_UPDATE,true);
                Timber.e("Valid");
            } else if (validVersionState instanceof ValidVersionState.NotValid) {
                createDialog(validVersionState);
                Timber.e("NotValid");
            } else {
                Timber.e("Error");
            }
        });


//        final Handler handler = new Handler();
//        handler.postDelayed(() -> {
//        }, 2000);


    }

    private void createDialog(ValidVersionState state) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Dialog warningDialog = new Dialog(this.getContext());
        warningDialog.setContentView(R.layout.valid_version_dialog);
        MaterialTextView diltext = warningDialog.findViewById(R.id.valid_version_dial_text);
        MaterialTextView dsmbtn = warningDialog.findViewById(R.id.dismiss_dialog_btn);

        if (state instanceof ValidVersionState.NotValid) {
            String first = "<font color='#EE0000'>WARNING: </font>";
            String next = this.getResources().getString(R.string.not_valid_version);

            diltext.setText(Html.fromHtml(first + next));
            Objects.requireNonNull(warningDialog.getWindow()).setLayout((8 * width) / 9, (2 * height) / 5);
        } else {
            diltext.setText(this.getResources().getString(R.string.tbd));
        }
        dsmbtn.setOnClickListener(v -> {
            warningDialog.hide();
        });

        warningDialog.show();
    }

    private void updateState(SessionStatus status, boolean smoothScroll) {
        adapter.getPageAtPosition(viewPager.getCurrentItem())
                .unregisterCallbackListener();

        switch (status) {
            case SPLASH:
                Timber.e(viewModel.getCurrentStatus());
                if(viewModel.getCurrentStatus()!="splash"){
                    viewModel.getValidVersion(BuildConfig.VERSION_NAME);
                }
//                viewModel.getValidVersion(BuildConfig.VERSION_NAME);

                viewPager.setCurrentItem(0, false);
                break;
            case PARTNER_UPDATE:
                Timber.e("1");
                viewPager.setCurrentItem(1, smoothScroll);
                break;
            case SESSION_CLEARED:
                Timber.e("2");
                // dummy state to let us clear entry fields in the editable page
                viewModel.setUploadValue(false);
                viewPager.setCurrentItem(2, smoothScroll);
                break;
            case NEW_SESSION:
                Timber.e("3");
                // show the event details editable screen
                viewModel.setUploadValue(false);
                viewPager.setCurrentItem(2, smoothScroll);
                break;
            case DETAILS_ENTERED: // fall through
//            case CLOCKED_IN:
//                // show the event details static page along with the clock-out option
//                // add location data
//                Timber.d("4");
//                viewModel.setUploadValue(true);
//                viewPager.setCurrentItem(3, smoothScroll);
//                break;
//            case CLOCKED_OUT:
//                // show session summary
//                Timber.d("5");
//                viewPager.setCurrentItem(4, smoothScroll);
//                break;
            case START_COLLECTION:
                Timber.d("6");
                viewPager.setCurrentItem(3, smoothScroll);
                break;
            case STATUS_COLLECTION:
                Timber.d("STATUS_COLLECTION");
                viewPager.setCurrentItem(4, smoothScroll);
                break;
            case END_STRANGER_COLLECTION:
                Timber.d("END_STRANGER_COLLECTION");
                viewPager.setCurrentItem(5, smoothScroll);
                break;
            case END_COLLECTION:
                Timber.d("END_COLLECTION");
                viewPager.setCurrentItem(6, smoothScroll);
                break;
            case END_SHIFT:
                Timber.d("END_SHIFT");
                viewPager.setCurrentItem(7, smoothScroll);
                break;
        }

        // register the current page for callbacks
        adapter.getPageAtPosition(viewPager.getCurrentItem())
                .registerCallbackListener(this);
    }

    @Override
    public void setState(SessionStatus status, boolean smoothScroll) {
        viewModel.updateSessionStatus(status);
    }


}
