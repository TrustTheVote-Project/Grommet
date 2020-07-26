package com.rockthevote.grommet.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Pattern;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Counties;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.AddressType;
import com.rockthevote.grommet.ui.misc.BetterSpinner;
import com.rockthevote.grommet.ui.misc.ChildrenViewStateHelper;
import com.rockthevote.grommet.ui.misc.ObservableValidator;
import com.rockthevote.grommet.util.PennValidations;
import com.rockthevote.grommet.util.Strings;
import com.rockthevote.grommet.util.ValidationRegex;
import com.rockthevote.grommet.util.ZipTextWatcher;
import com.squareup.moshi.Moshi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;


public class AddressView extends GridLayout {
    private static final String PA_ABREV = "PA";

    private static final String COUNTY_ENABLED_KEY = "county_enabled_key";

    private String childrenStateKey;
    private String superStateKey;

    @Inject Moshi moshi;

    @NotEmpty(messageResId = R.string.required_field)
    @Pattern(regex = ValidationRegex.ADDRESS, messageResId = R.string.address_format_error)
    @BindView(R.id.til_street_address) TextInputLayout streetTIL;
    @BindView(R.id.street) EditText streetEditText;

    @Pattern(regex = ValidationRegex.ADDRESS, messageResId = R.string.address_format_error)
    @BindView(R.id.til_street_address_2) TextInputLayout streetTIL2;
    @BindView(R.id.street_2) EditText streetEditText2;

    @BindView(R.id.til_unit) TextInputLayout unitTIL;
    @Length(max = PennValidations.UNIT_MAX_CHARS)
    @BindView(R.id.unit) EditText unitEditText;

    @NotEmpty(messageResId = R.string.required_field)
    @BindView(R.id.spinner_county) BetterSpinner countySpinner;

    @NotEmpty(messageResId = R.string.required_field)
    @Pattern(regex = ValidationRegex.CITY, messageResId = R.string.city_format_error)
    @BindView(R.id.til_city) TextInputLayout cityTIL;
    @BindView(R.id.city) EditText cityEditText;

    @BindView(R.id.spinner_state) BetterSpinner stateSpinner;
    @BindView(R.id.spinner_unit_type) BetterSpinner unitTypeSpinner;

    @Pattern(regex = ValidationRegex.ZIP, messageResId = R.string.zip_code_error)
    @BindView(R.id.til_zip_code) TextInputLayout zipTIL;
    @BindView(R.id.zip) EditText zipEditText;

    @BindView(R.id.address_section_title) TextView sectionTitle;

    private ObservableValidator validator;

    private ArrayAdapter<CharSequence> countyAdapter;
    private ArrayAdapter<CharSequence> stateAdapter;
    private ArrayAdapter<CharSequence> unitTypeAdapter;

    private AddressType type;
    private ZipTextWatcher zipTextWatcher = new ZipTextWatcher();

    private HashMap<String, List<String>> counties;

    public AddressView(Context context) {
        this(context, null);
    }

    public AddressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_address, this);

        if (!isInEditMode()) {

            Injector.obtain(context).inject(this);
            TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.AddressView,
                    0, 0);

            try {
                int val = typedArray.getInt(R.styleable.AddressView_address_type, 0);
                switch (val) {
                    case 1:
                        type = AddressType.MAILING_ADDRESS;
                        break;
                    case 2:
                        type = AddressType.PREVIOUS_ADDRESS;
                        break;
                    case 3:
                        type = AddressType.REGISTRATION_ADDRESS;
                        break;
                    case 4:
                        type = AddressType.ASSISTANT_ADDRESS;
                        break;
                }
            } finally {
                typedArray.recycle();
            }

            superStateKey = AddressView.class.getSimpleName() + ".superState." + type.toString();
            childrenStateKey = AddressView.class.getSimpleName() + ".childState." + type.toString();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!isInEditMode()) {
            ButterKnife.bind(this);
            validator = new ObservableValidator(this, getContext());

            switch (type) {
                case REGISTRATION_ADDRESS:
                    sectionTitle.setText(R.string.section_label_registration_address);
                    stateSpinner.setEnabled(false);
                    break;
                case MAILING_ADDRESS:
                    sectionTitle.setText(R.string.section_label_mailing_address);
                    break;
                case PREVIOUS_ADDRESS:
                    sectionTitle.setText(R.string.section_label_previous_address);
                    break;
                case ASSISTANT_ADDRESS:
                    sectionTitle.setText(R.string.section_label_registration_address);
            }

            InputStream is = getResources().openRawResource(R.raw.pa_county_zip);
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            counties = new HashMap<>(0);

            Observable.just(R.raw.pa_county_zip)
                    .flatMap(integer -> {
                        try {
                            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                            int n;
                            while ((n = reader.read(buffer)) != -1) {
                                writer.write(buffer, 0, n);
                            }
                            is.close();


                        } catch (IOException e) {
                            Timber.e(e, "AddressView: error loading zip codes");
                        }

                        return Observable.just(writer.toString());
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()) // observe on UI, or choose another Scheduler
                    .subscribe(string -> {
                        try {
                            counties = Counties.jsonAdapter(moshi).fromJson(string).toHashMap();
                        } catch (IOException e) {
                            Timber.e(e, "AddressView: error loading zip codes");
                        }
                        countyAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,
                                counties.keySet().toArray(new String[0]));

                        countyAdapter.sort((o1, o2) -> o1.toString().compareTo(o2.toString()));

                        countySpinner.setAdapter(countyAdapter);

                    });

            countySpinner.setHeight((int) getResources().getDimension(R.dimen.list_pop_up_max_height));
            countySpinner.setOnItemClickListener((adapterView, view, i, l) -> {
                countySpinner.getEditText().setText(countyAdapter.getItem(i));
                countySpinner.dismiss();
            });

            stateAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.states, android.R.layout.simple_list_item_1);

            stateSpinner.setAdapter(stateAdapter);
            stateSpinner.setHeight((int) getResources().getDimension(R.dimen.list_pop_up_max_height));
            stateSpinner.setOnItemClickListener((adapterView, view, i, l) -> {
                stateSpinner.getEditText().setText(stateAdapter.getItem(i));

                if (!PA_ABREV.equals(stateAdapter.getItem(i))) {
                    countySpinner.setErrorEnabled(false);
                    countySpinner.setEnabled(false);
                } else {
                    countySpinner.setEnabled(true);
                }

                stateSpinner.dismiss();
            });
            if (Strings.isBlank(stateSpinner.getEditText().getEditableText().toString())) {
                stateSpinner.getEditText().setText(stateAdapter.getItem(stateAdapter.getPosition(PA_ABREV)));
            }

            unitTypeAdapter = ArrayAdapter.createFromResource(getContext(),
                    R.array.unit_types, android.R.layout.simple_list_item_1);
            unitTypeSpinner.setAdapter(unitTypeAdapter);
            unitTypeSpinner.setOnItemClickListener((parent, view, position, id) -> {
                unitTypeSpinner.getEditText().setText(unitTypeAdapter.getItem(position));
                unitTypeSpinner.dismiss();
            });

        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            zipEditText.addTextChangedListener(zipTextWatcher);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        zipEditText.removeTextChangedListener(zipTextWatcher);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle state = new Bundle();
        state.putParcelable(superStateKey, super.onSaveInstanceState());
        state.putBoolean(COUNTY_ENABLED_KEY, countySpinner.isEnabled());
        state.putSparseParcelableArray(childrenStateKey,
                ChildrenViewStateHelper.newInstance(this).saveChildrenState(childrenStateKey));
        return state;
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle localState = (Bundle) state;
            super.onRestoreInstanceState(localState.getParcelable(superStateKey));
            ChildrenViewStateHelper.newInstance(this).restoreChildrenState(localState
                    .getSparseParcelableArray(childrenStateKey), childrenStateKey);
            countySpinner.setEnabled(localState.getBoolean(COUNTY_ENABLED_KEY, true));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    /**
     * should only try to validate if PA is the selected state
     */
    private void validateZipCode() {
        if (PA_ABREV.equals(stateSpinner.getEditText().getText().toString())) {
            String chosenCounty = countySpinner.getEditText().getText().toString();
            boolean zipcodeInCounty = !chosenCounty.isEmpty() &&
                    counties.get(chosenCounty).contains(zipEditText.getText().toString());

            zipTIL.setError(zipcodeInCounty ?
                    null : getContext().getString(R.string.zip_code_error));
        } else {
            zipTIL.setError(null);
        }
    }

    public Observable<Boolean> verify() {

        validateZipCode();

        if (zipTIL.getError() != null) {
            // remember there are three of these that are evaluated at the same time in the PersonalInfoFragment
            // these should probably not be validated if they are not used
            return Observable.just(false);
        } else {
            unitTypeSpinner.setError(null);
            unitTIL.setError(null);

            String unit = unitEditText.getText().toString();
            String type = unitTypeSpinner.getEditText().getText().toString();

            boolean typeValidation = (Strings.isBlank(unit) || !Strings.isBlank(type));
            if (!typeValidation) {
                unitTypeSpinner.setError(getContext().getString(R.string.required_field));
            }

            boolean unitValidation = (!Strings.isBlank(unit) || Strings.isBlank(type));
            if (!unitValidation) {
                unitTIL.setError(getContext().getString(R.string.required_field));
            }

            return validator.validate()
                    .map(valid -> valid && (typeValidation && unitValidation));
        }
    }
}
