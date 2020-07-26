package com.rockthevote.grommet.data.db.model;

import androidx.annotation.NonNull;

public enum Suffix {
    EMPTY(""), JR("Jr"), SR("Sr"), SECOND("II"), THIRD("III"), FOURTH("IV"),
    FIFTH("V"), SIXTH("VI"), SEVENTH("VII");

    private final String suffix;

    Suffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    @NonNull
    public String toString() {
        return suffix;
    }

    @NonNull
    public static Suffix fromString(String suffix) {
        for (Suffix value : Suffix.values()) {
            if (value.suffix.equals(suffix)) {
                return value;
            }
        }
        return EMPTY;
    }

}
