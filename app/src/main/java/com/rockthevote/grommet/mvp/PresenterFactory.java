package com.rockthevote.grommet.mvp;

import androidx.annotation.NonNull;

public interface PresenterFactory<P extends Presenter> {
    @NonNull P createPresenter();
}
