package com.rockthevote.grommet.ui.eventFlow;

import com.rockthevote.grommet.data.db.model.SessionStatus;

/**
 * Created by Mechanical Man, LLC on 7/19/17. Grommet
 */

public interface EventFlowCallback {

    void setState(SessionStatus state, boolean smoothScroll);
}
