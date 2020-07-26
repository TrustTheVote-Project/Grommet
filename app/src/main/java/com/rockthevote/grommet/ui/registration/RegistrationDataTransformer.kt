package com.rockthevote.grommet.ui.registration

import android.util.Base64
import com.rockthevote.grommet.R
import com.rockthevote.grommet.data.db.model.*
import com.rockthevote.grommet.ui.registration.address.AddressData
import com.rockthevote.grommet.ui.registration.name.PersonNameData
import com.rockthevote.grommet.util.Dates
import java.util.*

/**
 * Handles transformation of [registrationData]
 */
class RegistrationDataTransformer @Throws(InvalidRegistrationException::class) constructor(
    private val registrationData: RegistrationData,
    private val sessionData: SessionData,
    private val creationDate: Date
) {

    init {
        validateRegistrationData(registrationData)
    }

    // These have already be evaluated as non null in [validateRegistrationData]
    private val newRegistrationData = registrationData.newRegistrantData!!
    private val addressData = registrationData.addressData!!
    private val additionalInfoData = registrationData.additionalInfoData!!
    private val assistanceData = registrationData.assistanceData!!
    private val reviewData = registrationData.reviewData!!

    /**
     * Transforms the [registrationData] and [sessionData] into a [RockyRequest]
     *
     * @throws [InvalidRegistrationException] if unable to validate [RegistrationData]
     */
    fun transform(): RockyRequest {

        val body = RockyRequestBody(
            lang = reviewData.formLanguage,
            optInEmail = false,
            optInSms = false,
            optInVolunteer = false,
            partnerOptInVolunteer = additionalInfoData.partnerVolunteerOptIn,
            finishWithState = true,
            createdViaApi = true,
            partnerOptInSms = additionalInfoData.partnerSmsOptIn,
            partnerOptInEmail = additionalInfoData.partnerEmailOptIn,
            phoneType = additionalInfoData.phoneType.toString().toLowerCase(Locale.US),
            partnerId = sessionData.partnerId,
            sourceTrackingId = sessionData.sourceTrackingId,
            partnerTrackingId = sessionData.partnerTrackingId,
            geoLocation = sessionData.geoLocation,
            openTrackingId = sessionData.openTrackingId,
            voterRecordsRequest = buildVoterRecordsRequest()
        )

        return RockyRequest(body)
    }

    private fun buildVoterRecordsRequest(): VoterRecordsRequest {
        return VoterRecordsRequest(
            type = "registration",
            generatedDate = Dates.formatAsISO8601_Date(creationDate),
            canvasserName = sessionData.canvasserName,
            voterRegistration = buildVoterRegistration()
        )
    }

    private fun buildVoterRegistration(): VoterRegistration {
        val mailingAddress = if (addressData.isMailingAddressDifferent) {
            addressData.mailingAddress ?: throw InvalidRegistrationException(
                "addressData.isMailingAddressDifferent is null",
                R.string.different_mailing_address_not_filled_out
            )
        } else {
            null
        }

        val race = (additionalInfoData.race ?: Race.DECLINE).toString()

        val party = when (additionalInfoData.party) {
            Party.OTHER_PARTY -> additionalInfoData.otherPoliticalParty
                ?: throw InvalidRegistrationException(
                    "otherPoliticalParty is null",
                    R.string.must_write_in_party
                )
            else -> additionalInfoData.party.toString()
        }.toLowerCase(Locale.US)

        val signature = Base64.encodeToString(reviewData.signature, Base64.NO_WRAP)

        return VoterRegistration(
            registrationHelper = buildRegistrationHelper(),
            dateOfBirth = Dates.formatAsISO8601_ShortDate(newRegistrationData.birthday),
            mailingAddress = mailingAddress?.toApiAddressData(),
            previousRegistrationAddress = addressData.previousAddress?.toApiAddressData(),
            registrationAddress = addressData.homeAddress.toApiAddressData(),
            registrationAddressIsMailingAddress = !addressData.isMailingAddressDifferent,
            name = newRegistrationData.name.toApiName(),
            previousName = if (newRegistrationData.hasChangedName) newRegistrationData.previousName?.toApiName() else null,
            gender = Gender.fromPrefix(newRegistrationData.name.title).toString(),
            race = race,
            party = party,
            voterClassifications = buildVoterClassifications(),
            signature = Signature(
                mimeType = "image/png",
                image = signature
            ),
            voterIds = buildVoterIds(),
            contactMethods = buildContactMethods(),
            additionalInfo = buildAdditionalInfo()
        )

    }

    private fun buildRegistrationHelper(): RegistrationHelper? {
        if (!assistanceData.hasSomeoneAssisted) return null

        val helperPhone = assistanceData.helperPhone ?: throw InvalidRegistrationException(
            "helperPhone is null",
            R.string.assistants_phone_number_is_empty
        )

        return RegistrationHelper(
            registrationHelperType = "assistant",
            name = assistanceData.helperName?.toApiName(),
            address = assistanceData.helperAddress?.toApiAddressData(),
            contactMethods = listOf(
                ContactMethod(
                    type = "assistant_phone",
                    value = helperPhone,
                    capabilities = listOf("voice")
                )
            )
        )
    }

    private fun buildVoterClassifications(): List<VoterClassification> {
        val is18OnElectionDay = VoterClassification(
            type = "eighteen_on_election_day",
            assertion = newRegistrationData.is18OrOlderByNextElection
        )

        val isUsCitizen = VoterClassification(
            type = "united_states_citizen",
            assertion = newRegistrationData.isUsCitizen
        )

        val politicalPartyChanged = VoterClassification(
            type = "political_party_change",
            assertion = additionalInfoData.hasChangedPoliticalParty
        )

        return listOfNotNull(
            is18OnElectionDay,
            isUsCitizen,
            politicalPartyChanged
        )
    }

    private fun buildVoterIds(): List<VoterId> {
        val driversLicense = VoterId(
            type = "drivers_license",
            stringValue = additionalInfoData.pennDotNumber,
            attestNoSuchId = !additionalInfoData.knowsPennDotNumber
        )

        val ssn4 = VoterId(
            type = "ssn4",
            stringValue = additionalInfoData.ssnLastFour,
            attestNoSuchId = !additionalInfoData.knowsSsnLastFour
        )

        return listOf(driversLicense, ssn4)
    }

    private fun buildContactMethods(): List<ContactMethod> {
        val phoneCapabilities = listOfNotNull(
            "voice",
            if (additionalInfoData.phoneType == PhoneType.MOBILE) "sms" else null
        )

        val phone = ContactMethod(
            type = "phone",
            value = additionalInfoData.phoneNumber,
            capabilities = phoneCapabilities
        )

        val email = ContactMethod(
            type = "email",
            value = additionalInfoData.emailAddress,
            capabilities = listOf()
        )

        return listOf(phone, email)
    }

    private fun buildAdditionalInfo(): List<AdditionalInfo> {
        val preferredLanguage = AdditionalInfo(
            name = "preferred_language",
            stringValue = additionalInfoData.preferredLanguage?.toString()
        )

        val assistantDeclaration = AdditionalInfo(
            name = "assistant_declaration",
            stringValue = if (assistanceData.hasSomeoneAssisted) {
                if (assistanceData.hasConfirmedTerms) {
                    "true"
                } else {
                    throw InvalidRegistrationException(
                        "assistanceData.hasSomeoneAssisted is true, but did not confirm terms",
                        R.string.confirm_terms_not_checked
                    )
                }
            } else {
                "false"
            }
        )

        return listOf(preferredLanguage, assistantDeclaration)
    }

    private fun PersonNameData.toApiName(): Name {
        return Name(
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            titlePrefix = title.toEnString(),
            titleSuffix = suffix?.toString()
        )
    }

    private fun AddressData.toApiAddressData(): Address {
        val base = NumberedThoroughfareAddress(
            completeAddressNumber = "",
            completeStreetName = streetAddress,
            completeSubAddress = buildSubAddress(),
            completePlaceNames = buildCompletePlacesNames(),
            state = state,
            zipCode = zipCode
        )

        return Address(base)
    }

    private fun AddressData.buildSubAddress(): List<CompleteSubAddress>? {
        val unitType = unitType?.let {
            if (it.isEmpty()) return@let null

            CompleteSubAddress(
                subAddressType = it,
                subAddress = unitNumber
            )
        }

        val line2 = streetAddressTwo?.let {
            if (it.isEmpty()) return@let null
            CompleteSubAddress(
                subAddressType = "LINE2",
                subAddress = it
            )
        }

        return listOfNotNull(unitType, line2).nullIfEmpty()
    }

    private fun AddressData.buildCompletePlacesNames(): List<CompletePlaceName>? {
        val city = CompletePlaceName("MunicipalJurisdiction", city)

        val county = county?.let {
            if (it.isNotEmpty()) {
                CompletePlaceName("County", it)
            } else {
                null
            }
        }

        return listOfNotNull(city, county).nullIfEmpty()
    }

    private fun validateRegistrationData(registrationData: RegistrationData) {
        val errorMsgTemplate = "%s is null"
        val userMessageTemplate = R.string.registration_data_validation_error
        registrationData.newRegistrantData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("newRegistrantData"),
            userMessageTemplate,
            R.string.fragment_title_new_registrant
        )

        registrationData.addressData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("addressData"),
            userMessageTemplate,
            R.string.fragment_title_personal_info
        )

        registrationData.additionalInfoData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("additionalInfoData"),
            userMessageTemplate,
            R.string.fragment_title_additional_info
        )

        registrationData.assistanceData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("assistanceData"),
            userMessageTemplate,
            R.string.fragment_title_assistant_info
        )

        registrationData.reviewData ?: throw InvalidRegistrationException(
            errorMsgTemplate.format("reviewData"),
            userMessageTemplate,
            R.string.fragment_title_review
        )
    }

    private fun <T> List<T>.nullIfEmpty(): List<T>? {
        return if (isEmpty()) {
            null
        } else {
            this
        }
    }
}