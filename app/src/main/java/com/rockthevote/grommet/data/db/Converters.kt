package com.rockthevote.grommet.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rockthevote.grommet.data.api.model.ApiGeoLocation
import com.rockthevote.grommet.data.api.model.PartnerVolunteerText
import com.rockthevote.grommet.data.api.model.RegistrationNotificationText
import com.rockthevote.grommet.data.api.model.ValidLocation
import com.rockthevote.grommet.data.db.model.SessionStatus
import com.squareup.moshi.Moshi
import java.util.*

/**
 * Created by Mechanical Man on 4/12/20.
 */
class Converters {

    val moshi: Moshi by lazy { Moshi.Builder().build() }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun fromPartnerVolunteerText(volunteerText: PartnerVolunteerText): String? {
        val adapter = PartnerVolunteerText.jsonAdapter(moshi)
        return adapter.toJson(volunteerText)
    }

    @TypeConverter
    fun jsonToPartnerVolunteerText(value: String): PartnerVolunteerText? {
        val adapter = PartnerVolunteerText.jsonAdapter(moshi)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun fromRegistrationNotificationText(volunteerText: RegistrationNotificationText): String? {
        val adapter = RegistrationNotificationText.jsonAdapter(moshi)
        return adapter.toJson(volunteerText)
    }

    @TypeConverter
    fun jsonToRegistrationNotificationText(value: String): RegistrationNotificationText? {
        val adapter = RegistrationNotificationText.jsonAdapter(moshi)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun sessionStatusToString(value: SessionStatus): String? {
        return value.toString()
    }

    @TypeConverter
    fun stringToSessionStatus(value: String): SessionStatus? {
        return SessionStatus.fromString(value)
    }

    @TypeConverter
    fun fromApiGeoLocation(value: ApiGeoLocation): String? {
        val adapter = ApiGeoLocation.jsonAdapter(moshi)
        return adapter.toJson(value)
    }

    @TypeConverter
    fun jsonToApiGeoLocation(value: String): ApiGeoLocation? {
        val adapter = ApiGeoLocation.jsonAdapter(moshi)
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun fromStringToList(data: String?): ArrayList<String?>? {
        val listType = object : TypeToken<ArrayList<String?>?>() {}.type
        return Gson().fromJson(data, listType)
    }

    @TypeConverter
    fun fromListToString(list: ArrayList<String?>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

//    @TypeConverter
//    fun listToJson(value: ArrayList<ValidLocation>?) = Gson().toJson(value)

//    @TypeConverter
//    fun jsonToList(value: String) = Gson().fromJson(value, Array<ValidLocation>::class.java).toList()

    @TypeConverter
    fun fromValidLocationToList(data: String): ArrayList<ValidLocation?>? {
        if (data == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<ArrayList<ValidLocation?>?>() {}.type
        return gson.fromJson(data, type)
    }
//
    @TypeConverter
    fun fromValidLocationListToString(list: ArrayList<ValidLocation?>?): String? {
        if (list == null) {
            return null
        }
        val gson = Gson()
        val type = object : TypeToken<List<ValidLocation?>?>() {}.type
        return gson.toJson(list, type)
    }
}