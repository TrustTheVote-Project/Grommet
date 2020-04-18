package com.rockthevote.grommet.ui.eventFlow;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.f2prateek.rx.preferences2.Preference;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.model.SessionStatus;
import com.rockthevote.grommet.data.prefs.CanvasserName;
import com.rockthevote.grommet.data.prefs.CurrentSessionRowId;
import com.rockthevote.grommet.data.prefs.DeviceID;
import com.rockthevote.grommet.data.prefs.EventName;
import com.rockthevote.grommet.data.prefs.EventZip;
import com.rockthevote.grommet.data.prefs.PartnerId;
import com.rockthevote.grommet.data.prefs.PartnerName;
import com.rockthevote.grommet.data.prefs.PartnerTimeout;
import com.rockthevote.grommet.ui.misc.AnimatorListenerHelper;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;

import static com.rockthevote.grommet.data.db.model.SessionStatus.CLOCKED_IN;
import static com.rockthevote.grommet.data.db.model.SessionStatus.CLOCKED_OUT;
import static com.rockthevote.grommet.data.db.model.SessionStatus.NEW_SESSION;

public class SessionTimeTracking extends FrameLayout implements EventFlowPage {
    @Inject ReactiveLocationProvider locationProvider;

    @Inject @CurrentSessionRowId Preference<Long> currentSessionRowId;

    @BindView(R.id.ed_canvasser_name) TextView edCanvasserName;
    @BindView(R.id.ed_event_name) TextView edEventName;
    @BindView(R.id.ed_event_zip) TextView edEventZip;
    @BindView(R.id.ed_partner_name) TextView edPartnerName;
    @BindView(R.id.clock_in_button) View clockInButton;
    @BindView(R.id.event_details_static_edit) Button editButton;
    @BindView(R.id.ed_clock_in_time) TextView clockInTime;
    @BindView(R.id.session_progress_button) Button sessionProgressButton;
    @BindView(R.id.ed_device_id) TextView edDeviceId;

    @Inject @CanvasserName Preference<String> canvasserNamePref;
    @Inject @EventName Preference<String> eventNamePref;
    @Inject @EventZip Preference<String> eventZipPref;
    @Inject @PartnerId Preference<String> partnerIdPref;
    @Inject @PartnerName Preference<String> partnerNamePref;
    @Inject @DeviceID Preference<String> deviceIdPref;
    @Inject @PartnerTimeout Preference<Long> partnerTimeoutPref;

    private CompositeDisposable disposables = new CompositeDisposable();

    private EventFlowCallback listener;

    public SessionTimeTracking(Context context) {
        this(context, null);
    }

    public SessionTimeTracking(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SessionTimeTracking(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.session_time_tracking, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            ButterKnife.bind(this);

            disposables.add(canvasserNamePref.asObservable()
                    .subscribe(name -> edCanvasserName.setText(name)));

            disposables.add(eventNamePref.asObservable()
                    .subscribe(name -> edEventName.setText(name)));

            disposables.add(eventZipPref.asObservable()
                    .subscribe(name -> edEventZip.setText(name)));

            disposables.add(partnerNamePref.asObservable()
                    .subscribe(name -> edPartnerName.setText(name)));

            disposables.add(deviceIdPref.asObservable()
                    .subscribe(name -> edDeviceId.setText(name)));
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) {
            disposables.dispose();
        }
    }

    @Override
    public void registerCallbackListener(EventFlowCallback listener) {
        this.listener = listener;
    }

    @Override
    public void unregisterCallbackListener() {
        listener = null;
    }

    void updateUI(SessionStatus status) {

        // we can check for this via the DB and if there are no rows, it's fine

        switch (status) {
            case CLOCKED_IN:
                clockInButton.setSelected(true);

                break;
            default:
                clockInButton.setSelected(false);
                clockInTime.setText(R.string.time_tracking_default_value);
                break;
        }

        editButton.setEnabled(!clockInButton.isSelected());

        TextView text = (TextView) ((ViewGroup) clockInButton).getChildAt(0);
        text.setText(clockInButton.isSelected() ? R.string.clock_out_text : R.string.clock_in_text);
    }

    @OnClick(R.id.session_progress_button)
    public void onProgressClick(View v) {
        SessionProgressDialogFragment.newInstance()
                .show(((AppCompatActivity) getContext()).getSupportFragmentManager(),
                        "session_progress");
    }

    @OnClick(R.id.event_details_static_edit)
    public void onClickEdit(View v) {
        listener.setState(NEW_SESSION, true);
    }

    @OnClick(R.id.clock_in_button)
    public void onClickClockIn(View button) {

        if (button.isSelected()) {
            new AlertDialog.Builder(getContext())
                    .setMessage(R.string.clock_out_dialog_text)
                    .setPositiveButton(R.string.dialog_yes, (dialogInterface, i) -> {
                        clockOut();
                    })
                    .setNegativeButton(R.string.dialog_no,
                            (dialogInterface, i) -> dialogInterface.dismiss())
                    .create()
                    .show();
        } else {

            TextView text = (TextView) ((ViewGroup) button).getChildAt(0);
            AnimatorSet animSet = new AnimatorSet();

            int width = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    getResources().getDimension(R.dimen.clock_in_transition_width),
                    getResources().getDisplayMetrics());

            ValueAnimator shrinkAnim = ValueAnimator.ofInt(button.getMeasuredWidth(), width);

            shrinkAnim.addUpdateListener(valueAnimator -> {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
                layoutParams.width = val;
                button.requestLayout();
            });

            shrinkAnim.addListener(new AnimatorListenerHelper() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    clockIn();
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                    button.setSelected(!button.isSelected());
                    text.setText(button.isSelected() ? R.string.clock_out_text : R.string.clock_in_text);
                }
            });

            ValueAnimator fadeOutAnim = ObjectAnimator.ofFloat(text, "alpha", 1f, 0f);
            ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(text, "alpha", 0f, 1f);

            // this is 500 ms each way, so with a "reverse" it's a total of 1 second
            shrinkAnim.setDuration(500);
            shrinkAnim.setRepeatMode(ValueAnimator.REVERSE);
            shrinkAnim.setRepeatCount(1);

            fadeOutAnim.setDuration(200);
            fadeInAnim.setDuration(200);

            animSet.play(fadeOutAnim)
                    .with(shrinkAnim);
            animSet.play(fadeInAnim)
                    .after(fadeOutAnim)
                    .after(750); // wait to fade in so we don't get jittery text

            animSet.start();
        }
    }

    private void clockIn() {

        updateUI(CLOCKED_IN);

    }

    private void clockOut() {

        listener.setState(CLOCKED_OUT, true);
    }
}
