package com.rockthevote.grommet.data.db.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = false)
data class RockyRequest(
    @Json(name = "rocky_request")
    val rockyRequest: RockyRequestBody
)

@JsonClass(generateAdapter = false)
data class RockyRequestBody(
    @Json(name = "lang")
    val lang: String,
    @Json(name = "phone_type")
    val phoneType: String? = null,
    @Json(name = "partner_id")
    val partnerId: Long,
    @Json(name = "opt_in_email")
    val optInEmail: Boolean?,
    @Json(name = "opt_in_sms")
    val optInSms: Boolean?,
    @Json(name = "opt_in_volunteer")
    val optInVolunteer: Boolean?,
    @Json(name = "partner_opt_in_sms")
    val partnerOptInSms: Boolean,
    @Json(name = "partner_opt_in_email")
    val partnerOptInEmail: Boolean,
    @Json(name = "partner_opt_in_volunteer")
    val partnerOptInVolunteer: Boolean?,
    @Json(name = "finish_with_state")
    val finishWithState: Boolean?,
    @Json(name = "created_via_api")
    val createdViaApi: Boolean?,
    @Json(name = "source_tracking_id")
    val sourceTrackingId: String,
    @Json(name = "partner_tracking_id")
    val partnerTrackingId: String,
    @Json(name = "geo_location")
    val geoLocation: GeoLocation?,
    @Json(name = "open_tracking_id")
    val openTrackingId: String,
    @Json(name = "voter_records_request")
    val voterRecordsRequest: VoterRecordsRequest?
)

@JsonClass(generateAdapter = false)
data class GeoLocation(
    @Json(name = "lat")
    val lat: Double,
    @Json(name = "long")
    val long: Double
)

@JsonClass(generateAdapter = false)
data class VoterRecordsRequest(
    @Json(name = "type")
    val type: String?,
    @Json(name = "generated_date")
    val generatedDate: String,
    @Json(name = "canvasser_name")
    val canvasserName: String?,
    @Json(name = "voter_registration")
    val voterRegistration: VoterRegistration?
)

@JsonClass(generateAdapter = false)
data class VoterRegistration(
    @Json(name = "registration_helper")
    val registrationHelper: RegistrationHelper?,
    @Json(name = "date_of_birth")
    val dateOfBirth: String,
    @Json(name = "mailing_address")
    val mailingAddress: Address?,
    @Json(name = "previous_registration_address")
    val previousRegistrationAddress: Address?,
    @Json(name = "registration_address")
    val registrationAddress: Address,
    @Json(name = "registration_address_is_mailing_address")
    val registrationAddressIsMailingAddress: Boolean,
    @Json(name = "name")
    val name: Name,
    @Json(name = "previous_name")
    val previousName: Name?,
    @Json(name = "gender")
    val gender: String,
    @Json(name = "race")
    val race: String,
    @Json(name = "party")
    val party: String,
    @Json(name = "voter_classifications")
    val voterClassifications: List<VoterClassification>,
    @Json(name = "signature")
    val signature: Signature,
    @Json(name = "voter_ids")
    val voterIds: List<VoterId>,
    @Json(name = "contact_methods")
    val contactMethods: List<ContactMethod>,
    @Json(name = "additional_info")
    val additionalInfo: List<AdditionalInfo>
)

@JsonClass(generateAdapter = false)
data class AdditionalInfo(
    @Json(name = "name")
    val name: String?,
    @Json(name = "string_value")
    val stringValue: String?
)

@JsonClass(generateAdapter = false)
data class ContactMethod(
    @Json(name = "type")
    val type: String,
    @Json(name = "value")
    val value: String,
    @Json(name = "capabilities")
    val capabilities: List<String>?
)

@JsonClass(generateAdapter = false)
data class Name(
    @Json(name = "first_name")
    val firstName: String,
    @Json(name = "last_name")
    val lastName: String,
    @Json(name = "middle_name")
    val middleName: String?,
    @Json(name = "title_prefix")
    val titlePrefix: String,
    @Json(name = "title_suffix")
    val titleSuffix: String? = null
)

@JsonClass(generateAdapter = false)
data class RegistrationHelper(
    @Json(name = "registration_helper_type")
    val registrationHelperType: String,
    @Json(name = "name")
    val name: Name?,
    @Json(name = "address")
    val address: Address?,
    @Json(name = "contact_methods")
    val contactMethods: List<ContactMethod>?
)

@JsonClass(generateAdapter = false)
data class Signature(
    @Json(name = "mime_type")
    val mimeType: String?,
    @Json(name = "image")
    val image: String?
)

@JsonClass(generateAdapter = false)
data class VoterClassification(
    @Json(name = "type")
    val type: String?,
    @Json(name = "assertion")
    val assertion: Boolean?
)

@JsonClass(generateAdapter = false)
data class VoterId(
    @Json(name = "type")
    val type: String,
    @Json(name = "string_value")
    val stringValue: String?,
    @Json(name = "attest_no_such_id")
    val attestNoSuchId: Boolean?
)

@JsonClass(generateAdapter = false)
data class NumberedThoroughfareAddress(
    @Json(name = "complete_address_number")
    val completeAddressNumber: String?,
    @Json(name = "complete_street_name")
    val completeStreetName: String,
    @Json(name = "complete_sub_address")
    val completeSubAddress: List<CompleteSubAddress>?,
    @Json(name = "complete_place_names")
    val completePlaceNames: List<CompletePlaceName>?,
    @Json(name = "state")
    val state: String,
    @Json(name = "zip_code")
    val zipCode: String
)

@JsonClass(generateAdapter = false)
data class CompletePlaceName(
    @Json(name = "place_name_type")
    val placeNameType: String?,
    @Json(name = "place_name_value")
    val placeNameValue: String?
)

@JsonClass(generateAdapter = false)
data class CompleteSubAddress(
    @Json(name = "sub_address_type")
    val subAddressType: String?,
    @Json(name = "sub_address")
    val subAddress: String?
)

@JsonClass(generateAdapter = false)
data class Address(
    @Json(name = "numbered_thoroughfare_address")
    val numberedThoroughfareAddress: NumberedThoroughfareAddress?
)