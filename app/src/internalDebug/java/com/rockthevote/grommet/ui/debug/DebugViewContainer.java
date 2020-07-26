package com.rockthevote.grommet.ui.debug;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.PowerManager;
import androidx.core.view.GravityCompat;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.f2prateek.rx.preferences2.Preference;
import com.mattprecious.telescope.TelescopeLayout;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.LumberYard;
import com.rockthevote.grommet.data.SeenDebugDrawer;
import com.rockthevote.grommet.ui.ViewContainer;
import com.rockthevote.grommet.ui.bugreport.BugReportLens;
import com.rockthevote.grommet.util.EmptyActivityLifecycleCallbacks;

import javax.inject.Inject;
import javax.inject.Singleton;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

import static android.content.Context.POWER_SERVICE;
import static android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP;
import static android.os.PowerManager.FULL_WAKE_LOCK;
import static android.os.PowerManager.ON_AFTER_RELEASE;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

/**
 * An {@link ViewContainer} for debug builds which wraps a sliding drawer on the right that holds
 * all of the debug information and settings.
 */
@Singleton
public final class DebugViewContainer implements ViewContainer {
  private final LumberYard lumberYard;
  private final Preference<Boolean> seenDebugDrawer;

  static class ViewHolder {
    @BindView(R.id.debug_drawer_layout) DebugDrawerLayout drawerLayout;
    @BindView(R.id.debug_drawer) ViewGroup debugDrawer;
    @BindView(R.id.telescope_container) TelescopeLayout telescopeLayout;
    @BindView(R.id.debug_content) FrameLayout content;
  }

  @Inject public DebugViewContainer(LumberYard lumberYard,
      @SeenDebugDrawer Preference<Boolean> seenDebugDrawer) {
    this.lumberYard = lumberYard;
    this.seenDebugDrawer = seenDebugDrawer;
  }

  @Override public ViewGroup forActivity(final Activity activity) {
    activity.setContentView(R.layout.debug_activity_frame);

    final ViewHolder viewHolder = new ViewHolder();
    ButterKnife.bind(viewHolder, activity);

    final Context drawerContext = new ContextThemeWrapper(activity, R.style.Theme_Grommet_Debug);
    final DebugView debugView = new DebugView(drawerContext);
    viewHolder.debugDrawer.addView(debugView);

    // Set up the contextual actions to watch views coming in and out of the content area.
    ContextualDebugActions contextualActions = debugView.getContextualDebugActions();
    contextualActions.setActionClickListener(v -> viewHolder.drawerLayout.closeDrawers());
    viewHolder.content.setOnHierarchyChangeListener(
        HierarchyTreeChangeListener.wrap(contextualActions));

    viewHolder.drawerLayout.setDrawerShadow(R.drawable.debug_drawer_shadow, GravityCompat.END);
    viewHolder.drawerLayout.setDrawerListener(new DebugDrawerLayout.SimpleDrawerListener() {
      @Override public void onDrawerOpened(View drawerView) {
        debugView.onDrawerOpened();
      }
    });

    TelescopeLayout.cleanUp(activity); // Clean up any old screenshots.
    viewHolder.telescopeLayout.setLens(new BugReportLens(activity, lumberYard));

    // If you have not seen the debug drawer before, show it with a message
    if (!seenDebugDrawer.get()) {
      viewHolder.drawerLayout.postDelayed(() -> {
        viewHolder.drawerLayout.openDrawer(GravityCompat.END);
        Toast.makeText(drawerContext, R.string.debug_drawer_welcome, Toast.LENGTH_LONG).show();
      }, 1000);
      seenDebugDrawer.set(true);
    }

    final CompositeDisposable disposables = new CompositeDisposable();

    final Application app = activity.getApplication();
    app.registerActivityLifecycleCallbacks(new EmptyActivityLifecycleCallbacks() {
      @Override public void onActivityDestroyed(Activity lifecycleActivity) {
        if (lifecycleActivity == activity) {
          disposables.dispose();
          app.unregisterActivityLifecycleCallbacks(this);
        }
      }
    });

    riseAndShine(activity);
    return viewHolder.content;
  }

  /**
   * Show the activity over the lock-screen and wake up the device. If you launched the app manually
   * both of these conditions are already true. If you deployed from the IDE, however, this will
   * save you from hundreds of power button presses and pattern swiping per day!
   */
  public static void riseAndShine(Activity activity) {
    activity.getWindow().addFlags(FLAG_SHOW_WHEN_LOCKED);

    PowerManager power = (PowerManager) activity.getSystemService(POWER_SERVICE);
    PowerManager.WakeLock lock =
        power.newWakeLock(FULL_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP | ON_AFTER_RELEASE, "wakeup!");
    lock.acquire();
    lock.release();
  }
}
