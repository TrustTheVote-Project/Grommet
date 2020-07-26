package com.rockthevote.grommet.ui.registration;

import android.app.AlertDialog;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.data.db.dao.RegistrationDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.ui.BaseActivity;
import com.rockthevote.grommet.ui.ViewContainer;
import com.rockthevote.grommet.ui.misc.StepperTabLayout;
import com.rockthevote.grommet.util.KeyboardUtil;
import com.rockthevote.grommet.util.LocaleUtils;

import java.util.Locale;

import javax.inject.Inject;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

import static androidx.viewpager.widget.ViewPager.OnPageChangeListener;


public class RegistrationActivity extends BaseActivity {

    @Inject ViewContainer viewContainer;

    @Inject RegistrationDao registrationDao;

    @Inject SessionDao sessionDao;

    @Inject PartnerInfoDao partnerInfoDao;

    @BindView(R.id.appbar) AppBarLayout appbar;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.viewPager) ViewPager viewPager;
    @BindView(R.id.tabLayout) StepperTabLayout tabLayout;
    @BindView(R.id.button_previous) Button previousButton;
    @BindView(R.id.button_next) Button nextButton;

    private RegistrationPagerAdapter adapter;

    private OnPageChangeListener listener;

    private KeyboardUtil keyboardUtil;

    public RegistrationActivity() {
        LocaleUtils.updateConfig(this);
    }

    private RegistrationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // inside your activity (if you did not enable transitions in your theme)
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setExitTransition(new Slide());
        getWindow().setEnterTransition(new Slide());

        super.onCreate(savedInstanceState);
        Injector.obtain(this).inject(this);

        viewModel = new ViewModelProvider(
                this,
                new RegistrationViewModelFactory(registrationDao, sessionDao, partnerInfoDao)
        ).get(RegistrationViewModel.class);

        ViewGroup contentView = getContentView();
        getLayoutInflater().inflate(R.layout.activity_registration, contentView);
        ButterKnife.bind(this, contentView);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setupViewPager();
        keyboardUtil = new KeyboardUtil(this, appbar);
    }

    private void setupViewPager() {
        adapter = new RegistrationPagerAdapter(getSupportFragmentManager(), this);

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);

        tabLayout.setupWithViewPager(viewPager);

        // Steppers are sequential and we want to prevent skipping steps
        LinearLayout tabStrip = (LinearLayout) tabLayout.getChildAt(0);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setClickable(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        listener = new RegistrationPageListener();
        viewPager.addOnPageChangeListener(listener);
        updateNavigation(viewPager.getCurrentItem());
        keyboardUtil.enable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewPager.removeOnPageChangeListener(listener);
        keyboardUtil.disable();
    }

    private void updateNavigation(int pagePosition) {
        previousButton.setEnabled(pagePosition != 0);
        nextButton.setEnabled(pagePosition != adapter.getCount() - 1);
        KeyboardUtil.hideKeyboard(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.registration, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel:
                showCancelDialog();
                return true;
            case R.id.action_english:
                if (!Locale.getDefault().equals(new Locale("en"))) {
                    LocaleUtils.setLocale(new Locale("en"));
                    recreate();
                }
                return true;
            case R.id.action_espanol:
                if (!Locale.getDefault().equals(new Locale("es"))) {
                    LocaleUtils.setLocale(new Locale("es"));
                    recreate();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        showCancelDialog();
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.cancel_dialog_message)
                .setPositiveButton(R.string.dialog_yes, ((dialog, i) -> {
                    viewModel.incrementAbandonedCount();

                    LocaleUtils.setLocale(new Locale("en"));
                    finish();

                }))
                .setNegativeButton(R.string.dialog_no, ((dialog, i) -> dialog.dismiss()))
                .create()
                .show();
    }

    @OnClick(R.id.button_previous)
    public void onPreviousClick(View v) {
        int curPage = viewPager.getCurrentItem();
        if (curPage > 0) {
            int previousItem = curPage - 1;
            viewPager.setCurrentItem(previousItem);
        }
    }

    @OnClick(R.id.button_next)
    public void onNextClick(View v) {
        int curPage = viewPager.getCurrentItem();
        if (curPage < adapter.getCount() - 1) {
            BaseRegistrationFragment previousFragment =
                    ((BaseRegistrationFragment) adapter.getItem(curPage));

            previousFragment
                    .verify()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isValid -> {
                        if (isValid) {
                            tabLayout.enableTabAtPosition(curPage + 1);
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);

                            previousFragment.storeState();
                        }
                    });
        }
    }

    private class RegistrationPageListener implements OnPageChangeListener {
        private int prevPage = viewPager.getCurrentItem();

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {

            updateNavigation(position);

            //only check if we're swiping right
            if (position - 1 == prevPage) {
                BaseRegistrationFragment previousFragment =
                        ((BaseRegistrationFragment) adapter.getItem(prevPage));

                previousFragment
                        .verify()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(isValid -> {
                            if (isValid) {
                                previousFragment.storeState();
                                tabLayout.enableTabAtPosition(position);
                                prevPage = position;
                            } else {
                                viewPager.setCurrentItem(prevPage);
                            }
                        });
            } else {
                prevPage = position;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}
