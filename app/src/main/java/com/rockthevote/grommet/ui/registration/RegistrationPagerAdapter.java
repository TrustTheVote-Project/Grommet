package com.rockthevote.grommet.ui.registration;


import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.ui.registration.assistance.AssistantInfoFragment;
import com.rockthevote.grommet.ui.registration.name.NewRegistrantFragment;
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoFragment;
import com.rockthevote.grommet.ui.registration.address.PersonalInfoFragment;
import com.rockthevote.grommet.ui.registration.review.ReviewAndConfirmFragment;

public class RegistrationPagerAdapter extends FragmentPagerAdapter {

    private final SparseArray<Fragment> fragments = new SparseArray<>();
    private final SparseArray<String> titles = new SparseArray<>();

    public RegistrationPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        titles.put(0, context.getString(R.string.fragment_title_new_registrant));
        titles.put(1, context.getString(R.string.fragment_title_personal_info));
        titles.put(2, context.getString(R.string.fragment_title_additional_info));
        titles.put(3, context.getString(R.string.fragment_title_assistant_info));
        titles.put(4, context.getString(R.string.fragment_title_review));
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position, getNewFragment(position));
    }

    private Fragment getNewFragment(int position) {
        switch (position) {
            case 0:
                return new NewRegistrantFragment();
            case 1:
                return new PersonalInfoFragment();
            case 2:
                return new AdditionalInfoFragment();
            case 3:
                return new AssistantInfoFragment();
            case 4:
                return new ReviewAndConfirmFragment();
            default:
                throw new IllegalStateException("Unknown fragment position: " + position);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments.put(position, fragment);
        return fragment;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position, "");
    }
}
