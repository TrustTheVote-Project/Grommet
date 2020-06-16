package com.rockthevote.grommet.ui;

import android.Manifest;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.HockeyAppHelper;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.db.dao.RegistrationDao;
import com.rockthevote.grommet.ui.eventFlow.EventFlowWizard;
import com.rockthevote.grommet.ui.registration.RegistrationActivity;

import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.legacy.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kotlin.Unit;
import rx.subscriptions.CompositeSubscription;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public final class MainActivity extends BaseActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.main_content) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.pending_registrations) TextView pendingRegistrations;
    @BindView(R.id.failed_registrations_container) LinearLayout failedRegistrationsContainer;
    @BindView(R.id.failed_registrations) TextView failedRegistrations;
    @BindView(R.id.upload) Button upploadButton;
    @BindView(R.id.event_flow_wizard) EventFlowWizard eventFlowWizard;

    @Inject ViewContainer viewContainer;

    @Inject FusedLocationProviderClient locationProvider;

    @Inject HockeyAppHelper hockeyAppHelper;

    @Inject RockyService rockyService;

    @Inject RegistrationDao registrationDao;

    @Inject SharedPreferences sharedPreferences;

    private CompositeSubscription subscriptions;

    private MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(this).inject(this);

        View view = getLayoutInflater().inflate(R.layout.activity_main, getContentView());
        ButterKnife.bind(this, view);
        setSupportActionBar(toolbar);
        requestGPSPermission();
        hockeyAppHelper.checkForUpdates(this);

        viewModel = new ViewModelProvider(
                this,
                new MainActivityViewModelFactory(rockyService, registrationDao, sharedPreferences)
        ).get(MainActivityViewModel.class);

        observeState();
    }

    private void observeState() {
        viewModel.getSessionStatus().observe(this, sessionStatus -> {
            if (sessionStatus == null) {
                return;
            }

            eventFlowWizard.setState(sessionStatus, true);
        });


        viewModel.getState().observe(this, mainActivityState -> {

            // Reset base states
            upploadButton.setEnabled(false);
            failedRegistrationsContainer.setVisibility(View.GONE);
            failedRegistrations.setText("");

            if (mainActivityState instanceof MainActivityState.Content) {
                MainActivityState.Content content = (MainActivityState.Content) mainActivityState;

                String pendingUploads = String.format(
                        Locale.getDefault(),
                        "%d",
                        content.getPendingUploads()
                );

                pendingRegistrations.setText(pendingUploads);

                if (content.getFailedUploads() > 0 || content.getPendingUploads() > 0) {
                    upploadButton.setEnabled(true);
                } else {
                    upploadButton.setEnabled(false);
                }

                if (content.getFailedUploads() > 0) {
                    failedRegistrationsContainer.setVisibility(View.VISIBLE);

                    String failedUploads = String.format(
                            Locale.getDefault(),
                            "%d",
                            content.getFailedUploads()
                    );

                    failedRegistrations.setText(failedUploads);
                }

            } else if (mainActivityState instanceof MainActivityState.Loading) {
                upploadButton.setEnabled(false);
            } else if (mainActivityState instanceof MainActivityState.Init) {
                viewModel.loadSessionStatus();
                viewModel.refreshPendingUploads();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshPendingUploads();
        hockeyAppHelper.checkForCrashes(this);

        subscriptions = new CompositeSubscription();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hockeyAppHelper.unRegister();
        subscriptions.unsubscribe();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hockeyAppHelper.unRegister();
    }

    private void requestGPSPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
        }
    }

    @OnClick(R.id.fab)
    public void onClick(View v) {
        viewModel.asyncCanRegister(this::canRegister, this::cantRegister);
    }

    private Unit canRegister() {
        startActivity(new Intent(this, RegistrationActivity.class),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());

        return Unit.INSTANCE;
    }

    private Unit cantRegister() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clock_in_alert_title)
                .setIcon(R.drawable.ic_timer_black_24dp)
                .setMessage(R.string.clock_in_alert_message)
                .setPositiveButton(R.string.action_ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .create()
                .show();

        return Unit.INSTANCE;
    }

    /**
     * starts the upload process if wifi is available, else notifies the user there is no wifi.
     * The button is enabled/disabled by the presense of pending applications
     *
     * @param v
     */
    @OnClick(R.id.upload)
    public void onClickUpload(View v) {
        viewModel.uploadRegistrations();
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return R.id.nav_home;
    }
}
