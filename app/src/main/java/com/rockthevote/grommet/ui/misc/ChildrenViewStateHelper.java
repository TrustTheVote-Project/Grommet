package com.rockthevote.grommet.ui.misc;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.SparseArray;
import android.view.ViewGroup;

public class ChildrenViewStateHelper {
    public static final String DEFAULT_CHILDREN_STATE_KEY =  ChildrenViewStateHelper.class.getSimpleName() + ".childrenState";
    private ViewGroup mClientViewGroup;

    public static ChildrenViewStateHelper newInstance(@NonNull final ViewGroup viewGroup) {
        final ChildrenViewStateHelper handler = new ChildrenViewStateHelper();
        handler.mClientViewGroup = viewGroup;
        return handler;
    }

    private ChildrenViewStateHelper() {
    }

    /**
     * Handles saving of children view states into SparseArray and returns it for next storing.
     * Also you need override {@link ViewGroup#dispatchSaveInstanceState(SparseArray)} and call {@link ViewGroup#dispatchFreezeSelfOnly(SparseArray)}.
     */
    public SparseArray<Parcelable> saveChildrenState() {
        return saveChildrenState(DEFAULT_CHILDREN_STATE_KEY);
    }

    public SparseArray<Parcelable> saveChildrenState(String stateKey) {
        final SparseArray<Parcelable> array = new SparseArray<>();
        for (int i = 0; i < mClientViewGroup.getChildCount(); i++) {
            final Bundle bundle = new Bundle();
            final SparseArray<Parcelable> childArray = new SparseArray<>(); //create independent SparseArray for each child (View or ViewGroup)
            mClientViewGroup.getChildAt(i).saveHierarchyState(childArray);
            bundle.putSparseParcelableArray(stateKey, childArray);
            array.append(i, bundle);
        }
        return array;
    }

    /**
     * Handles restoring of children view states from SparseArray which need to extract before.
     * Also you need override {@link ViewGroup#dispatchRestoreInstanceState(SparseArray)} and call {@link ViewGroup#dispatchThawSelfOnly(SparseArray)}.
     */
    public void restoreChildrenState(@Nullable final SparseArray<Parcelable> childrenState) {
        restoreChildrenState(childrenState, DEFAULT_CHILDREN_STATE_KEY);
    }

    public void restoreChildrenState(@Nullable final SparseArray<Parcelable> childrenState, String stateKey) {
        if (null == childrenState) {
            return;
        }
        for (int i = 0; i < mClientViewGroup.getChildCount(); i++) {
            final Bundle bundle = (Bundle) childrenState.get(i);
            final SparseArray<Parcelable> childState = bundle.getSparseParcelableArray(stateKey);
            mClientViewGroup.getChildAt(i).restoreHierarchyState(childState);
        }
    }
}