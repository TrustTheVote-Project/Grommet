package com.rockthevote.grommet.data.db.model;

public enum AddressType {
    MAILING_ADDRESS("mailing_address"),
    PREVIOUS_ADDRESS("previous_address"),
    REGISTRATION_ADDRESS("registration_address"),
    ASSISTANT_ADDRESS("assistant_address");

    private final String type;

    AddressType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    public static AddressType fromString(String type) {
        for (AddressType val : values()) {
            if (val.toString().equals(type)) {
                return val;
            }
        }
        return MAILING_ADDRESS;
    }
}
