package com.rockthevote.grommet;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.rockthevote.grommet.data.Injector;
import com.rockthevote.grommet.data.LumberYard;
import com.rockthevote.grommet.data.db.AppDatabase;
import com.rockthevote.grommet.ui.ActivityHierarchyServer;
import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import dagger.ObjectGraph;
import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public final class GrommetApp extends Application {
    private ObjectGraph objectGraph;

    @Inject ActivityHierarchyServer activityHierarchyServer;
    @Inject LumberYard lumberYard;

    @Inject AppDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
        LeakCanary.install(this);


        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            // TODO start analytics tracking
        }

        objectGraph = ObjectGraph.create(Modules.list(this));
        objectGraph.inject(this);

        lumberYard.cleanUp();
        Timber.plant(lumberYard.tree());

        registerActivityLifecycleCallbacks(activityHierarchyServer);

    }

    @Override
    public Object getSystemService(@NonNull String name) {
        if (Injector.matchesService(name)) {
            return objectGraph;
        }
        return super.getSystemService(name);
    }
}
