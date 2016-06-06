package com.rockthevote.grommet;



import com.rockthevote.grommet.ui.debug.ContextualDebugActions;

import java.util.LinkedHashSet;
import java.util.Set;

import dagger.Module;
import dagger.Provides;

import static dagger.Provides.Type.SET_VALUES;

@Module(complete = false, library = true) public final class DebugActionsModule {
  @Provides(type = SET_VALUES) Set<ContextualDebugActions.DebugAction> provideDebugActions() {
    Set<ContextualDebugActions.DebugAction> actions = new LinkedHashSet<>();
    return actions;
  }
}
