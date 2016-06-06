package com.rockthevote.grommet.data.api;

import com.rockthevote.grommet.data.api.model.RegistrationResponse;

public enum MockRegistrationResponse {
    SUCCESS("Success", null);

    public final String name;
    public final RegistrationResponse response;

    MockRegistrationResponse(String name, RegistrationResponse response) {
        this.name = name;
        this.response = response;
    }

    @Override
    public String toString() {
        return name;
    }
}
