package com.rockthevote.grommet.data.api.model;


import com.google.auto.value.AutoValue;
import com.rockthevote.grommet.data.db.model.ContactMethod;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.ArrayList;
import java.util.List;

import static com.rockthevote.grommet.data.db.model.RockyRequest.PhoneType;

@AutoValue
public abstract class ApiContactMethod {

    abstract String type();
    abstract String value();
    abstract List<String> capabilities();

    public static JsonAdapter<ApiContactMethod> jsonAdapter(Moshi moshi){
        return new AutoValue_ApiContactMethod.MoshiJsonAdapter(moshi);
    }

    static Builder builder(){
        return new AutoValue_ApiContactMethod.Builder();
    }

    @AutoValue.Builder
    abstract static class Builder{
        abstract Builder type(String value);
        abstract Builder value(String value);
        abstract Builder capabilities(List<String> values);
        abstract ApiContactMethod build();
    }

    public static ApiContactMethod fromDb(ContactMethod contactMethod, PhoneType phoneType){
        List<String> capabilities = new ArrayList<>();
        // right now we only support phone (no fax)
        if(contactMethod.type() == ContactMethod.Type.PHONE){
            capabilities.add(ContactMethod.Capability.VOICE.toString());
            if(phoneType == PhoneType.MOBILE){
                capabilities.add(ContactMethod.Capability.SMS.toString());
            }
        }

        return builder()
                .type(contactMethod.type().toString())
                .value(contactMethod.value())
                .capabilities(capabilities)
                .build();
    }
}