package com.rockthevote.grommet.ui.registration.name;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;

import com.google.android.material.textfield.TextInputLayout;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.databinding.FragmentNewRegistrantBinding;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment;
import com.rockthevote.grommet.ui.registration.DatePickerDialogFragment;
import com.rockthevote.grommet.ui.registration.PartnerPreferenceViewModel;
import com.rockthevote.grommet.ui.registration.PartnerPreferenceViewModelFactory;
import com.rockthevote.grommet.util.Dates;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

public class NewRegistrantFragment extends BaseRegistrationFragment {

    @Inject PartnerInfoDao partnerInfoDao;

    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.til_birthday)
    TextInputLayout tilBirthday;

    @Checked(messageResId = R.string.eighteen_or_older_err)
    @BindView(R.id.checkbox_is_eighteen)
    CheckBox checkBoxIsEighteen;

    @Checked(messageResId = R.string.us_citizen_err)
    @BindView(R.id.checkbox_is_us_citizen)
    CheckBox checkBoxIsUSCitizen;

    private ObservableValidator validator;

    private FragmentNewRegistrantBinding binding;

    private PartnerPreferenceViewModel partnerPrefViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNewRegistrantBinding.inflate(inflater, container, false);
        return wrapBinding(binding.getRoot(), inflater, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        partnerPrefViewModel = new ViewModelProvider(
                getActivity(),
                new PartnerPreferenceViewModelFactory(partnerInfoDao)
        ).get(PartnerPreferenceViewModel.class);

        observePartnerPrefs();

        validator = new ObservableValidator(this, getActivity());

        binding.edittextBirthday.setOnClickListener(v -> {
            Date startDate = Dates.parseISO8601_ShortDate(binding.edittextBirthday.getText().toString());
            DatePickerDialogFragment.newInstance(NewRegistrantFragment.this::onDateSet, startDate)
                    .show(getFragmentManager(), "datepicker");
        });

        binding.nameChanged.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.previousName.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            binding.previousNameDivider.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        observeState();
    }

    private void observePartnerPrefs() {
        partnerPrefViewModel.getBirthdayValidationState().observe(getViewLifecycleOwner(), birthdayValidationState -> {
            if (birthdayValidationState instanceof BirthdayValidationState.Success) {
                binding.tilBirthday.setError(null);
            } else if (birthdayValidationState instanceof BirthdayValidationState.Error) {

                String regDate = ((BirthdayValidationState.Error) birthdayValidationState).getRegDate();
                String errorMsg = String.format(getString(R.string.birthday_error), regDate);
                binding.tilBirthday.setError(errorMsg);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Injector.obtain(getActivity()).inject(this);
    }

    private void observeState() {
        viewModel.getRegistrationData().observe(getViewLifecycleOwner(), registrationData -> {
            NewRegistrantData data = registrationData.getNewRegistrantData();
            if (data != null) {
                NewRegistrantExtKt.toFragmentNewRegistratntBinding(data, binding);
            }
        });
    }

    private void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        GregorianCalendar date = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        binding.edittextBirthday.setText(Dates.formatAsISO8601_ShortDate(date.getTime()));

        partnerPrefViewModel.validateBirthDay(
                Dates.parseISO8601_ShortDate(binding.edittextBirthday.getText().toString()));
    }

    @Override
    public Observable<Boolean> verify() {

        if (binding.tilBirthday.getError() != null) {
            // special case since there is an additional validation on the birth date
            return Observable.just(false);
        } else {
            Observable<Boolean> previousNameObs = binding.previousName.verify()
                    .flatMap(val -> Observable.just(binding.nameChanged.isChecked() ? val : true));

            return Observable.zip(
                    validator.validate(),
                    binding.name.verify(),
                    previousNameObs,
                    (validator, name, prevName) -> validator && name && prevName);
        }
    }

    @Override
    public void storeState() {
        NewRegistrantData data = NewRegistrantExtKt.toNameRegistrationData(binding);
        viewModel.storeNewRegistrantData(data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
