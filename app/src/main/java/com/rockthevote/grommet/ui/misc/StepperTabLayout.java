package com.rockthevote.grommet.ui.misc;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;

import com.rockthevote.grommet.ui.views.StepperTabView;
import com.rockthevote.grommet.util.SparseBooleanArrayParcelable;

public class StepperTabLayout extends TabLayout {

    private static final String STEPPER_STATE_KEY = "stepper_state_key";

    private ViewPager viewPager;
    private SparseBooleanArray enabled = new SparseBooleanArray();

    public StepperTabLayout(Context context) {
        this(context, null);
    }

    public StepperTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepperTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // first tab is always enabled
        enabled.put(0, true);
    }

    @Override
    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        this.viewPager = viewPager;
        super.setupWithViewPager(viewPager);
    }

    @Override
    public void addTab(@NonNull Tab tab, boolean setSelected) {
        int pos = getTabCount();

        StepperTabView customView = new StepperTabView(getContext())
                .setStepNumber(pos + 1)
                .setStepName(viewPager.getAdapter().getPageTitle(pos).toString());
        customView.setEnabled(enabled.get(pos));

        tab.setCustomView(customView);

        super.addTab(tab, setSelected);
    }

    @SuppressWarnings("ConstantConditions")
    public void enableTabAtPosition(int pos) {
        if (null != getTabAt(pos)) {
            enabled.put(pos, true);
            getTabAt(pos).getCustomView().setEnabled(true);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle state = new Bundle();
        state.putParcelable("stepperSuperState", super.onSaveInstanceState());
        state.putParcelable(STEPPER_STATE_KEY, new SparseBooleanArrayParcelable(enabled));
        return state;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onRestoreInstanceState(final Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle localState = (Bundle) state;
            super.onRestoreInstanceState(localState.getParcelable("stepperSuperState"));
            enabled = localState.getParcelable(STEPPER_STATE_KEY);
            for (int i = 0; i < getTabCount(); i++) {
                getTabAt(i).getCustomView().setEnabled(enabled.get(i));
            }
        } else {
            super.onRestoreInstanceState(state);
        }
    }


}
