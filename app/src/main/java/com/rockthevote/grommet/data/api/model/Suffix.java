package com.rockthevote.grommet.data.api.model;

import android.support.annotation.NonNull;

public enum Suffix {
    JR("Jr."), SR("Sr."), SECOND("II"), THIRD("III"), FOURTH("IV"), EMPTY("");

    private final String suffix;

    Suffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String toString() {
        return suffix;
    }

    @NonNull
    public static Suffix fromString(String suffix) {
        for (Suffix value : values()) {
            if (value.suffix.equals(suffix)) {
                return value;
            }
        }
        return EMPTY;
    }

}
