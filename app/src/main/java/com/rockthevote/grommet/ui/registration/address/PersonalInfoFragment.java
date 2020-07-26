package com.rockthevote.grommet.ui.registration.address;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.databinding.FragmentPersonalInfoBinding;
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment;

import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import rx.Observable;


public class PersonalInfoFragment extends BaseRegistrationFragment {

    private FragmentPersonalInfoBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPersonalInfoBinding.inflate(inflater, container, false);
        return wrapBinding(binding.getRoot(), inflater, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Injector.obtain(getActivity()).inject(this);
        observeState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void observeState() {
        viewModel.getRegistrationData().observe(getViewLifecycleOwner(), registrationData -> {
            PersonalInfoData data = registrationData.getAddressData();
            if (data != null) {
                PersonalInfoExtKt.toFragmentPersonalInfoBinding(data, binding);
            }
        });
    }

    @OnCheckedChanged(R.id.mailing_address_is_different)
    public void onMailingAddressDifferentChecked(boolean checked) {
        binding.mailingAddress.setVisibility(checked ? View.VISIBLE : View.GONE);
        binding.mailingAddressDivider.setVisibility(checked ? View.VISIBLE : View.GONE);
    }

    @OnCheckedChanged(R.id.address_changed)
    public void onAddressChangeChecked(boolean checked) {
        binding.previousAddress.setVisibility(checked ? View.VISIBLE : View.GONE);
        binding.previousAddressDivider.setVisibility(checked ? View.VISIBLE : View.GONE);
    }

    @Override
    public Observable<Boolean> verify() {

        Observable<Boolean> mailingAddressObs = binding.mailingAddress.verify()
                .flatMap(val -> Observable.just(binding.mailingAddressIsDifferent.isChecked() ? val : true));

        Observable<Boolean> changedAddObs = binding.previousAddress.verify()
                .flatMap(val -> Observable.just(binding.addressChanged.isChecked() ? val : true));

        return Observable.zip(binding.homeAddress.verify(), mailingAddressObs, changedAddObs,
                (home, mail, change) -> home && mail && change);

    }

    @Override
    public void storeState() {
        PersonalInfoData data = PersonalInfoExtKt.toAddressData(binding);
        viewModel.storeAddressData(data);
    }
}
