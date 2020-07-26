package com.rockthevote.grommet.ui.misc;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.rockthevote.grommet.R;


public class BetterSpinner extends TextInputLayout {
    private String childrenStateKey;
    private String superStateKey;

    ListPopupWindow listPopupWindow;
    ListAdapter listAdapter;
    TextInputEditText editText;

    public BetterSpinner(Context context) {
        this(context, null);
    }

    public BetterSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BetterSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        superStateKey = BetterSpinner.class.getSimpleName() + ".superState";
        childrenStateKey = BetterSpinner.class.getSimpleName() + ".childState";


        editText = new TextInputEditText(context);
        editText.setId(R.id.titleId);
        editText.setHeight((int) getResources().getDimension(R.dimen.list_item_height));
        editText.setFocusable(false);
        editText.setMaxLines(1);
        editText.setCursorVisible(false);
        editText.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                R.drawable.drop_down_arrow, 0);

        addView(editText, 0);
        listPopupWindow = new ListPopupWindow(context);
        listPopupWindow.setAnchorView(editText);
    }

    public void setAdapter(ListAdapter listAdapter) {
        this.listAdapter = listAdapter;
        listPopupWindow.setAdapter(listAdapter);
        getEditText().setOnClickListener(view -> listPopupWindow.show());
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        listPopupWindow.setOnItemClickListener(listener);
    }

    public void dismiss() {
        listPopupWindow.dismiss();
    }

    /**
     * set the height of the popup window in pixes
     */
    public void setHeight(int height) {
        listPopupWindow.setHeight(height);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Bundle state = new Bundle();
        state.putParcelable(superStateKey, super.onSaveInstanceState());
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

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            getEditText().setText("");
        }
        getEditText().setEnabled(enabled);
    }

    @NonNull
    @Override
    public TextInputEditText getEditText() {
        return editText;
    }

    @Nullable
    public String getSpinnerText() {
        if (editText.getText() != null) {
            return editText.getText().toString();
        }

        return null;
    }

    public void setEditText(@NonNull CharSequence text) {
        editText.setText(text);
    }
}
