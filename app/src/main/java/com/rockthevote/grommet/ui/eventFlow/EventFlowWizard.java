package com.rockthevote.grommet.ui.eventFlow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.api.RockyService;
import com.rockthevote.grommet.data.db.dao.RegistrationDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.data.db.model.SessionStatus;
import com.rockthevote.grommet.ui.MainActivityViewModel;
import com.rockthevote.grommet.ui.MainActivityViewModelFactory;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mechanical Man, LLC on 7/19/17. Grommet
 */

public class EventFlowWizard extends FrameLayout implements EventFlowCallback {

    @Inject RockyService rockyService;
    @Inject RegistrationDao registrationDao;
    @Inject SessionDao sessionDao;

    @BindView(R.id.viewpager) ViewPager viewPager;

    private EventDetailFlowAdapter adapter;

    private MainActivityViewModel viewModel;

    public EventFlowWizard(Context context) {
        this(context, null);
    }

    public EventFlowWizard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EventFlowWizard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.event_flow_wizard, this);

        if (!isInEditMode()) {
            Injector.obtain(context).inject(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            ButterKnife.bind(this);

            adapter = new EventDetailFlowAdapter(getContext());
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(3);

            //disable scrolling on the view pager
            viewPager.setOnTouchListener((v, event) -> true);

        }

        viewModel = new ViewModelProvider(
                (AppCompatActivity) getContext(),
                new MainActivityViewModelFactory(rockyService, registrationDao, sessionDao)
        ).get(MainActivityViewModel.class);

    }


    @Override
    public void setState(SessionStatus status, boolean smoothScroll) {

        // unregister the current page from callbacks
        adapter.getPageAtPosition(viewPager.getCurrentItem())
                .unregisterCallbackListener();

        viewModel.updateSessionStatus(status);

        switch (status) {
            case PARTNER_UPDATE:
                viewPager.setCurrentItem(0, smoothScroll);
                break;
            case SESSION_CLEARED:
                // dummy state to let us clear entry fields in the editable page
                viewPager.setCurrentItem(1, smoothScroll);
                break;
            case NEW_SESSION:
                // show the event details editable screen
                viewPager.setCurrentItem(1, smoothScroll);
                break;
            case DETAILS_ENTERED: // fall through
            case CLOCKED_IN:
                // show the event details static page along with the clock-out option
                // add location data
                viewPager.setCurrentItem(2, smoothScroll);
                break;
            case CLOCKED_OUT:
                // show session summary
                viewPager.setCurrentItem(3, smoothScroll);
                break;
        }

        // register the current page for callbacks
        adapter.getPageAtPosition(viewPager.getCurrentItem())
                .registerCallbackListener(this);
    }


}
