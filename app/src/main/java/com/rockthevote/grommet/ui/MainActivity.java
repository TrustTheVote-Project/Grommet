package com.rockthevote.grommet.ui;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.rockthevote.grommet.R;
import com.rockthevote.grommet.data.Injector;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.ObjectGraph;

public final class MainActivity extends AppCompatActivity {

    @Inject ViewContainer viewContainer;

    private ObjectGraph activityGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();

        ObjectGraph appGraph = Injector.obtain(getApplication());
        appGraph.inject(this);

        activityGraph = appGraph.plus(new MainActivityModule(this));

        ViewGroup container = viewContainer.forActivity(this);

        inflater.inflate(R.layout.activity_main, container);
        ButterKnife.bind(this, container);

    }

    @Override
    public Object getSystemService(@NonNull String name) {
        if (Injector.matchesService(name)) {
            return activityGraph;
        }
        return super.getSystemService(name);
    }

    @Override
    protected void onDestroy() {
        activityGraph = null;
        super.onDestroy();
    }
}
