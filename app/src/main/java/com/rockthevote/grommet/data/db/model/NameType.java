package com.rockthevote.grommet.data.db.model;

import androidx.annotation.NonNull;

public enum NameType {
    CURRENT_NAME("current_name"),
    PREVIOUS_NAME("previous_name"),
    ASSISTANT_NAME("assistant_name");

    private final String type;

    NameType(String type) {
        this.type = type;
    }

    @Override
    @NonNull
    public String toString() {
        return type;
    }

    public static NameType fromString(String type) {
        for (NameType val : values()) {
            if (val.toString().equals(type)) {
                return val;
            }
        }
        return CURRENT_NAME;
    }
}
