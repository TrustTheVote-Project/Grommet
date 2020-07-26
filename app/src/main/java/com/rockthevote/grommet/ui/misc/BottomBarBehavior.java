package com.rockthevote.grommet.ui.misc;

import android.content.Context;
import android.graphics.Rect;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.legacy.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;

@SuppressWarnings("unused")
public class BottomBarBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();
    private boolean mIsAnimatingOut = false;

    public BottomBarBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        // First let the parent lay it out
        parent.onLayoutChild(child, layoutDirection);

        // Offset the vertical layout
        int systemWindowOffset = 0;
        if (ViewCompat.getFitsSystemWindows(parent)) {
            Rect rectangle = new Rect();
            parent.getWindowVisibleDisplayFrame(rectangle);
            systemWindowOffset = rectangle.top;
        }
        int verticalOffset = Math.max(0, parent.getHeight() - child.getHeight() - systemWindowOffset);
        ViewCompat.offsetTopAndBottom(child, verticalOffset);

        // Offset the horizontal layout
        int horizontalOffset = Math.max(0, (parent.getWidth() - child.getWidth()) / 2);
        ViewCompat.offsetLeftAndRight(child, horizontalOffset);

        return true;
    }

    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final V child,
                                       final View directTargetChild, final View target, final int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
                || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onNestedScroll(final CoordinatorLayout coordinatorLayout, final V child,
                               final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0 && child.getVisibility() != View.VISIBLE) {
            animateIn(child);
        } else if (dyConsumed == 0 && dyUnconsumed > 0 && child.getVisibility() != View.VISIBLE) {
            animateIn(child);
        } else if (dyConsumed < 0 && !this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
            animateOut(child);
        }
    }

    private void animateOut(final V child) {
        ViewCompat.animate(child).translationY(168F).alpha(0.0F).setInterpolator(INTERPOLATOR).withLayer()
                .setListener(new ViewPropertyAnimatorListener() {
                    public void onAnimationStart(View view) {
                        BottomBarBehavior.this.mIsAnimatingOut = true;
                    }

                    public void onAnimationCancel(View view) {
                        BottomBarBehavior.this.mIsAnimatingOut = false;
                    }

                    public void onAnimationEnd(View view) {
                        BottomBarBehavior.this.mIsAnimatingOut = false;
                        view.setVisibility(View.INVISIBLE);
                    }
                }).start();

    }

    private void animateIn(V child) {

        child.setVisibility(View.VISIBLE);
        ViewCompat.animate(child).translationY(0).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                .setInterpolator(INTERPOLATOR).withLayer().setListener(null)
                .start();
    }
}