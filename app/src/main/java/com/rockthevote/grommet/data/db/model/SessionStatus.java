package com.rockthevote.grommet.data.db.model;

import androidx.annotation.Nullable;

public enum SessionStatus {
    NEW_SESSION("new"),
    PARTNER_UPDATE("partner_update"),
    SESSION_CLEARED("session_cleared"),
    DETAILS_ENTERED("details_entered"),
    CLOCKED_IN("clocked_in"),
    CLOCKED_OUT("clocked_out"),
    SPLASH("splash"),
    START_COLLECTION("start_collection"),
    STATUS_COLLECTION("status_collection"),
    END_STRANGER_COLLECTION("end_stranger_collection"),
    END_COLLECTION("end_collection"),
    END_SHIFT("end_shift");


    private final String type;

    SessionStatus(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    @Nullable
    public static SessionStatus fromString(String type) {
        if (type == null) {
            return null;
        }

        for (SessionStatus val : values()) {
            if (val.toString().equals(type)) {
                return val;
            }
        }
        return null;
    }
}
