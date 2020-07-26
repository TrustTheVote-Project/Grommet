package com.rockthevote.grommet.ui.registration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao;
import com.rockthevote.grommet.data.db.dao.RegistrationDao;
import com.rockthevote.grommet.data.db.dao.SessionDao;
import com.rockthevote.grommet.databinding.FragmentRegistrationBaseBinding;

import javax.inject.Inject;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import rx.Observable;

public abstract class BaseRegistrationFragment extends Fragment {

    private @LayoutRes int contentView;

    protected RegistrationViewModel viewModel;

    @Inject
    RegistrationDao registrationDao;

    @Inject
    SessionDao sessionDao;

    @Inject
    PartnerInfoDao partnerInfoDao;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Injector.obtain(getActivity()).inject(this);
        viewModel = new ViewModelProvider(
                requireActivity(),
                new RegistrationViewModelFactory(registrationDao, sessionDao, partnerInfoDao)
        ).get(RegistrationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_registration_base, container, false);
        CardView content = (CardView) v.findViewById(R.id.content_area);
        inflater.inflate(contentView, content);
        return v;
    }

    protected View wrapBinding(
            View view,
            LayoutInflater inflater,
            ViewGroup container
    ) {

        FragmentRegistrationBaseBinding binding = FragmentRegistrationBaseBinding
                .inflate(inflater, container, false);

        binding.contentArea.addView(view);

        return binding.getRoot();
    }

    protected void setContentView(@LayoutRes int contentView) {
        this.contentView = contentView;
    }

    public Observable<Boolean> verify() {
        return Observable.just(true);
    }

    /**
     * Stores the current Fragment's state. ONLY store valid state or it will fail.
     */
    public abstract void storeState();
}
