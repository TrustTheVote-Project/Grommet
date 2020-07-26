package com.rockthevote.grommet.ui.registration;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;

import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegistrationCompleteDialogFragment extends DialogFragment {

    @Inject PartnerInfoDao partnerInfoDao;

    @BindView(R.id.drc_text_view) TextView content;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Injector.obtain(getActivity()).inject(this);
    }

    private PartnerPreferenceViewModel partnerPrefViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);

        View v = inflater.inflate(R.layout.dialog_registration_complete, container);
        ButterKnife.bind(this, v);

        getDialog().setTitle(getString(R.string.registration_complete));

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        partnerPrefViewModel = new ViewModelProvider(
                getActivity(),
                new PartnerPreferenceViewModelFactory(partnerInfoDao)
        ).get(PartnerPreferenceViewModel.class);

        observePartnerPrefs();
    }

    private void observePartnerPrefs() {
        partnerPrefViewModel.getRegistrationText().observe(getViewLifecycleOwner(), text -> {
            Locale curLocale = getResources().getConfiguration().locale;
            switch (curLocale.getLanguage()) {
                case "es":
                    content.setText(getString(R.string.registration_complete_dialog_text, text.spanish()));
                    break;
                default: // use english for default
                    content.setText(getString(R.string.registration_complete_dialog_text, text.english()));
                    break;
            }
        });

    }

    @OnClick(R.id.drc_ok_button)
    public void onOKClick(View v) {
        getActivity().finish();
    }
}
