package com.rockthevote.grommet.ui.misc;

import android.content.Context;
import com.google.android.material.textfield.TextInputLayout;
import android.view.View;
import android.widget.CheckBox;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;

import java.util.List;

import rx.Observable;

public class ObservableValidator {

    Observable<Boolean> observable;

    public ObservableValidator(Object controller, Context context) {
        Validator validator = new Validator(controller);
        validator.registerAdapter(TextInputLayout.class, new TilStringValidator());
        validator.registerAdapter(BetterSpinner.class, new BetterSpinnerValidator());
        validator.setViewValidatedAction(new Validator.ViewValidatedAction() {
            @Override
            public void onAllRulesPassed(View view) {
                if (view instanceof TextInputLayout) {
                    ((TextInputLayout) view).setError(null);
//                    ((TextInputLayout) view).setErrorEnabled(false);
                } else if (view instanceof CheckBox) {
                    ((CheckBox) view).setError(null);
                }
            }
        });

        observable = Observable.create(subscriber -> {
            validator.setValidationListener(new Validator.ValidationListener() {
                @Override
                public void onValidationSucceeded() {
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                }

                @Override
                public void onValidationFailed(List<ValidationError> errors) {
                    for (ValidationError error : errors) {
                        View view = error.getView();
                        String message = error.getCollatedErrorMessage(context);
                        if (view instanceof TextInputLayout) {
                            ((TextInputLayout) view).setError(message);
                        } else if (view instanceof CheckBox) {
                            ((CheckBox) view).setError(message);
                        }
                    }
                    subscriber.onNext(false);
                    subscriber.onCompleted();
                }
            });
            validator.validate();
        });
    }

    public Observable<Boolean> validate() {
        return observable;
    }


}
