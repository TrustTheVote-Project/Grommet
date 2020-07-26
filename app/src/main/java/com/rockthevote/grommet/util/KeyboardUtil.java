
package com.rockthevote.grommet.util;

import android.app.Activity;
import android.graphics.Rect;
import com.google.android.material.appbar.AppBarLayout;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtil {

    private View decorView;
    private AppBarLayout appBarLayout;

    public KeyboardUtil(Activity act, AppBarLayout appBarLayout) {
        this.decorView = act.getWindow().getDecorView();
        this.appBarLayout = appBarLayout;
    }

    public void enable() {
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);

    }

    public void disable() {
        decorView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        boolean flag = true;

        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            //r will be populated with the coordinates of your view that area still visible.
            decorView.getWindowVisibleDisplayFrame(r);

            //get screen height and calculate the difference with the useable area from the r
            int height = decorView.getContext().getResources().getDisplayMetrics().heightPixels;
            int diff = height - r.bottom;

            //if it could be a keyboard collapse the appbarlayout
            if (diff > 0 && flag) {
                appBarLayout.setExpanded(false, true);
                flag = false;
            } else {
                flag = true;
            }
        }
    };
}