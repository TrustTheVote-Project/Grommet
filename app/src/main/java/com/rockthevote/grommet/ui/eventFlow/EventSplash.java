package com.rockthevote.grommet.ui.eventFlow;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.ui.misc.ObservableValidator;

import javax.inject.Inject;

import butterknife.ButterKnife;
import timber.log.Timber;

import static com.rockthevote.grommet.data.db.model.SessionStatus.NEW_SESSION;
import static com.rockthevote.grommet.data.db.model.SessionStatus.PARTNER_UPDATE;

public class EventSplash extends FrameLayout implements EventFlowPage {

    @Inject
    RockyService rockyService;
    @Inject
    PartnerInfoDao partnerInfoDao;
    @Inject
    SessionDao sessionDao;



    private ObservableValidator validator;

    private EventFlowCallback listener;

    private PartnerLoginViewModel viewModel;

    public EventSplash(Context context) {
        this(context, null);
    }

    public EventSplash(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventSplash(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_splash, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            ButterKnife.bind(this);
            validator = new ObservableValidator(this, getContext());
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
}

