package com.rockthevote.grommet.data.db.model;

import androidx.annotation.NonNull;

import java.util.Locale;

public enum Prefix {
    MR("Mr", "Sr"),
    MS("Ms", "Ms"), // intentionally the same
    MRS("Mrs", "Srta"),
    MISS("Miss", "Sra"),
    NONE("", "");

    private final String enTitle;
    private final String esTitle;

    Prefix(String enTitle, String esTitle) {
        this.enTitle = enTitle;
        this.esTitle = esTitle;
    }

    @Override
    @NonNull
    public String toString() {
        if ("es".equals(Locale.getDefault().getLanguage())) {
            return esTitle;
        } else {
            // default to english
            return enTitle;
        }
    }

    public String toEnString() {
        return enTitle;
    }

    @NonNull
    public static Prefix fromString(String title) {
        for (Prefix value : Prefix.values()) {
            if (value.enTitle.equals(title) ||
                    value.esTitle.equals(title)) {
                return value;
            }
        }
        return NONE;
    }

}
