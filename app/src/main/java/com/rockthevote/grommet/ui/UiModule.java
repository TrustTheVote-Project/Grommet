package com.rockthevote.grommet.ui;


import com.rockthevote.grommet.ui.eventFlow.EventCanvasserInfo;
import com.rockthevote.grommet.ui.eventFlow.EventFlowWizard;
import com.rockthevote.grommet.ui.eventFlow.EventPartnerLogin;
import com.rockthevote.grommet.ui.eventFlow.SessionProgressDialogFragment;
import com.rockthevote.grommet.ui.eventFlow.SessionSummary;
import com.rockthevote.grommet.ui.eventFlow.SessionTimeTracking;
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoFragment;
import com.rockthevote.grommet.ui.registration.assistance.AssistantInfoFragment;
import com.rockthevote.grommet.ui.registration.name.NewRegistrantFragment;
import com.rockthevote.grommet.ui.registration.address.PersonalInfoFragment;
import com.rockthevote.grommet.ui.registration.RegistrationActivity;
import com.rockthevote.grommet.ui.registration.RegistrationCompleteDialogFragment;
import com.rockthevote.grommet.ui.registration.review.ReviewAndConfirmFragment;
import com.rockthevote.grommet.ui.views.AddressView;
import com.rockthevote.grommet.ui.views.NameView;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                InfoActivity.class,
                BaseActivity.class,
                DataUsageActivity.class,
                HelpActivity.class,
                MainActivity.class,
                RegistrationActivity.class,
                NewRegistrantFragment.class,
                PersonalInfoFragment.class,
                AdditionalInfoFragment.class,
                AssistantInfoFragment.class,
                ReviewAndConfirmFragment.class,
                AddressView.class,
                NameView.class,
                EventCanvasserInfo.class,
                SessionTimeTracking.class,
                EventFlowWizard.class,
                SessionSummary.class,
                SessionProgressDialogFragment.class,
                RegistrationCompleteDialogFragment.class,
                EventPartnerLogin.class
        },
        complete = false,
        library = true
)
public final class UiModule {
    @Provides
    @Singleton
    ViewContainer provideViewContainer() {
        return ViewContainer.DEFAULT;
    }

    @Provides
    @Singleton
    ActivityHierarchyServer provideActivityHierarchyServer() {
        return ActivityHierarchyServer.NONE;
    }
}

