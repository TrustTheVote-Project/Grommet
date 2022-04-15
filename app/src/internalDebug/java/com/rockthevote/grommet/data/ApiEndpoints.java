package com.rockthevote.grommet.data;

import com.rockthevote.grommet.data.api.ApiModule;

public enum ApiEndpoints {
    PRODUCTION("Production", ApiModule.PRODUCTION_API_URL.toString()),
    STAGING("Staging","https://staging.rocky.rockthevote.com/api/v4/"),
    STAGING_2("Staging 2","https://staging2.rocky.rockthevote.com/api/v4/"),
    MOCK_MODE("Mock Mode", "http://localhost/mock/");
    public final String name;
    public final String url;

    ApiEndpoints(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ApiEndpoints from(String endpoint) {
        for (ApiEndpoints value : values()) {
            if (value.url != null && value.url.equals(endpoint)) {
                return value;
            }
        }
        return PRODUCTION;
    }

    public static boolean isMockMode(String endpoint) {
        return from(endpoint) == MOCK_MODE;
    }
}
