package com.rockthevote.grommet.ui.registration.review;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.github.gcacace.signaturepad.views.SignaturePad;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.databinding.FragmentReviewAndConfirmBinding;
import com.rockthevote.grommet.ui.registration.BaseRegistrationFragment;
import com.rockthevote.grommet.ui.registration.DisclosureAgreementDialogFragment;
import com.rockthevote.grommet.ui.registration.RegistrationCompleteDialogFragment;
import com.rockthevote.grommet.ui.registration.RegistrationState;
import com.rockthevote.grommet.util.LocaleUtils;

import java.util.Locale;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class ReviewAndConfirmFragment extends BaseRegistrationFragment {

    @BindView(R.id.signature_pad)
    SignaturePad signaturePad;
    @BindView(R.id.signature_pad_error)
    TextView signaturePadError;
    @BindView(R.id.button_register)
    Button buttonRegister;

    @BindView(R.id.checkbox_agreement)
    CheckBox confirmCheckbox;

    private DisclosureAgreementDialogFragment dialog;

    private FragmentReviewAndConfirmBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReviewAndConfirmBinding.inflate(inflater, container, false);
        return wrapBinding(binding.getRoot(), inflater, container);
    }

    @Override
    public void storeState() {
        /* Stub */
    }

    public void completeRegistration() {
        ReviewData data = ReviewExtKt.toReviewData(binding);
        viewModel.completeRegistration(data);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        dialog = new DisclosureAgreementDialogFragment();

        viewModel.getRegistrationState().observe(getViewLifecycleOwner(), registrationState -> {
            if (registrationState instanceof RegistrationState.Complete) {
                completeForm();
            } else if (registrationState instanceof RegistrationState.RegistrationError) {
                showErrorDialog((RegistrationState.RegistrationError) registrationState);
            }
        });

        viewModel.getReviewAndConfirmState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ReviewAndConfirmState.Content) {
                ReviewExtKt.bindReviewData(binding, ((ReviewAndConfirmState.Content) state).getData());
            }
        });
    }

    private void showErrorDialog(RegistrationState.RegistrationError error) {
        if (error.isAcknowledged()) {
            return;
        }

        String errorMessage;

        if (error.getFormatVar() != null && error.getFormatVar().length > 0) {
            int formatVarLen = error.getFormatVar().length;
            String[] vargs = new String[formatVarLen];

            for (int i = 0; i < formatVarLen; i++) {
                vargs[i] = getResources().getString(error.getFormatVar()[i]);
            }

            errorMessage = getResources().getString(error.getErrorMsg(), (Object[]) vargs);
        } else {
            errorMessage = getResources().getString(error.getErrorMsg());
        }

        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setMessage(errorMessage)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                })
                .create();

        alertDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        dialog.setListener(new DisclosureAgreementDialogFragment.DisclosureListener() {
            @Override
            public void onDeclineClick() {
                confirmCheckbox.setChecked(false);
                dialog.dismiss();
            }

            @Override
            public void onAcceptClick() {
                buttonRegister.setEnabled(true);
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Injector.obtain(getActivity()).inject(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        dialog.setListener(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @OnClick(R.id.clear_signature)
    public void onClearSignatureClick(View v) {
        signaturePad.clear();
    }

    @OnCheckedChanged(R.id.checkbox_agreement)
    public void onCheckChanged(boolean checked) {
        if (checked) {
            dialog.show(getFragmentManager(), "disclosure_dialog");
        } else {
            buttonRegister.setEnabled(false);
        }
    }

    // the default value is set in the data module
    @OnClick(R.id.button_register)
    public void onRegisterClick(View v) {
        if (!signaturePad.isEmpty()) {
            completeRegistration();
        } else {
            signaturePadError.setVisibility(View.VISIBLE);
        }
    }

    private void completeForm() {
        signaturePadError.setVisibility(View.GONE);

        RegistrationCompleteDialogFragment dialog = new RegistrationCompleteDialogFragment();
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "complete_dialog");

        LocaleUtils.setLocale(new Locale("en"));
    }

}
