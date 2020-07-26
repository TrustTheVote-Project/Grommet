package com.rockthevote.grommet.ui.registration.assistance;

import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.Pattern;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.databinding.FragmentAssistantInfoBinding;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment;
import com.rockthevote.grommet.ui.views.AddressView;
import com.rockthevote.grommet.ui.views.NameView;
import com.rockthevote.grommet.util.Phone;
import com.rockthevote.grommet.util.ValidationRegex;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import rx.Observable;

public class AssistantInfoFragment extends BaseRegistrationFragment {

    @BindView(R.id.assistant_fields) View assistantFields;

    @BindView(R.id.assistant_name) NameView nameView;

    @BindView(R.id.assistant_address) AddressView addressView;

    @Phone(messageResId = R.string.phone_format_error)
    @Pattern(regex = ValidationRegex.PHONE, messageResId = R.string.phone_format_error)
    @BindView(R.id.til_assistant_phone) TextInputLayout phoneTIL;

    @BindView(R.id.assistant_phone) EditText phoneEditText;

    @BindView(R.id.checkbox_has_assistant) CheckBox hasAssistant;

    @Checked(messageResId = R.string.must_be_checked)
    @BindView(R.id.checkbox_assistant_affirmation) CheckBox assistantAffirmation;

    private ObservableValidator validator;

    private PhoneNumberFormattingTextWatcher phoneFormatter;

    private FragmentAssistantInfoBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAssistantInfoBinding.inflate(inflater, container, false);
        return wrapBinding(binding.getRoot(), inflater, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        Validator.registerAnnotation(Phone.class);
        validator = new ObservableValidator(this, getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Injector.obtain(getActivity()).inject(this);
        observeState();
    }

    @Override
    public void onResume() {
        super.onResume();

        phoneFormatter = new PhoneNumberFormattingTextWatcher("US");
        phoneEditText.addTextChangedListener(phoneFormatter);
    }

    @Override
    public void onPause() {
        super.onPause();
        phoneEditText.removeTextChangedListener(phoneFormatter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void observeState() {
        viewModel.getRegistrationData().observe(getViewLifecycleOwner(), registrationData -> {
            AssistanceData data = registrationData.getAssistanceData();
            if (data != null) {
                AssistanceExtKt.toFragmentAssistantInfoBinding(data, binding);
            }
        });
    }

    @OnCheckedChanged(R.id.checkbox_has_assistant)
    public void onHasAssistantChecked(boolean checked) {
        assistantFields.setVisibility(checked ? View.VISIBLE : View.GONE);

    }

    @Override
    public Observable<Boolean> verify() {

        if (!hasAssistant.isChecked()) {
            return super.verify();
        }

        return Observable.zip(nameView.verify(), addressView.verify(), validator.validate(),
                (name, address, other) -> name && address && other);
    }

    @Override
    public void storeState() {
        AssistanceData data = AssistanceExtKt.toAssistanceData(binding);
        viewModel.storeAssistanceData(data);
    }
}
