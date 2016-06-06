package com.rockthevote.grommet.data.api.model;

import android.support.annotation.NonNull;

public enum Title {
    MR("Mr"), MS("Ms"), MRS("Mrs"), MISS("Miss"), EMPTY("");

    private final String title;

    Title(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }

    @NonNull
    public static Title fromString(String title) {
        for (Title value : values()) {
            if (value.title.equals(title)) {
                return value;
            }
        }
        return EMPTY;
    }

}
