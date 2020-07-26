package com.rockthevote.grommet.ui.eventFlow;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.data.db.dao.RegistrationDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.util.Strings;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * Created by Mechanical Man, LLC on 8/16/17. Grommet
 */

public class SessionProgressDialogFragment extends DialogFragment {

    @Inject PartnerInfoDao partnerInfoDao;
    @Inject SessionDao sessionDao;
    @Inject RegistrationDao registrationDao;
    @Inject SharedPreferences sharedPreferences;
    @Inject RockyService rockyService;

    // Total Counts
    @BindView(R.id.summary_total_registrations) TextView totalRegistrations;
    @BindView(R.id.summary_total_abandoned) TextView totalAbandoned;
    @BindView(R.id.summary_total_dln) TextView totalDLN;
    @BindView(R.id.summary_total_ssn) TextView totalSSN;
    @BindView(R.id.summary_total_email_opt_in) TextView totalEmailOptIn;
    @BindView(R.id.summary_total_sms_opt_in) TextView totalSMSOptIn;

    // Percentage Counts
    @BindView(R.id.summary_dln_percentage) TextView percentDLN;
    @BindView(R.id.summary_ssn_percentage) TextView percentSSN;
    @BindView(R.id.summary_email_opt_in_percentage) TextView percentEmailOptIn;
    @BindView(R.id.summary_sms_opt_in_percentage) TextView percentSMSOptIn;

    private SessionTimeTrackingViewModel viewModel;

    static SessionProgressDialogFragment newInstance() {
        return new SessionProgressDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.obtain(requireContext()).inject(this);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this,
                new SessionTimeTrackingViewModelFactory(
                        partnerInfoDao,
                        sessionDao,
                        registrationDao,
                        sharedPreferences,
                        rockyService)
        ).get(SessionTimeTrackingViewModel.class);

        observeData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view;
        if (getShowsDialog()) {
            view = inflater.inflate(R.layout.session_progress_dialog_fragment, container, false);
        } else {
            view = inflater.inflate(R.layout.summary_totals, container, false);
        }

        ButterKnife.bind(this, view);
        return view;
    }

    private void observeData() {
        viewModel.getSessionData().observe(getViewLifecycleOwner(), data -> {
            // update count totals
            totalRegistrations.setText(String.valueOf(data.getTotalRegistrations()));
            totalAbandoned.setText(String.valueOf(data.getAbandonedRegistrations()));
            totalDLN.setText(String.valueOf(data.getDlnCount()));
            totalSSN.setText(String.valueOf(data.getSsnCount()));
            totalEmailOptIn.setText(String.valueOf(data.getEmailOptInCount()));
            totalSMSOptIn.setText(String.valueOf(data.getSmsCount()));

            // update percentages
            double totalReg = data.getTotalRegistrations() * 1.0;

            if (totalReg > 0) {
                percentDLN.setText(
                        Strings.formatNumberAsPercentage(data.getDlnCount() / totalReg));
                percentSSN.setText(
                        Strings.formatNumberAsPercentage(data.getSsnCount() / totalReg));
                percentEmailOptIn.setText(
                        Strings.formatNumberAsPercentage(data.getEmailOptInCount() / totalReg));
                percentSMSOptIn.setText(
                        Strings.formatNumberAsPercentage(data.getSmsCount() / totalReg));
            }
        });

    }

    @Optional
    @OnClick(R.id.session_progress_dismiss)
    public void onDismissClick(View v) {
        getDialog().dismiss();
    }
}
