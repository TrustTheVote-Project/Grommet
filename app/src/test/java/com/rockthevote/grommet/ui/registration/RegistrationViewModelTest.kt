package com.rockthevote.grommet.ui.registration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.rockthevote.grommet.data.api.model.ApiGeoLocation
import com.rockthevote.grommet.data.db.dao.SessionDao
import com.rockthevote.grommet.data.db.model.GeoLocation
import com.rockthevote.grommet.data.db.model.RockyRequest
import com.rockthevote.grommet.data.db.model.Session
import com.rockthevote.grommet.data.db.model.SessionStatus
import com.rockthevote.grommet.testdata.Fake
import com.rockthevote.grommet.testdata.FakeRegistrationDao
import com.rockthevote.grommet.testdata.FakeSessionDao
import com.rockthevote.grommet.testdata.TestDispatcherProvider
import com.rockthevote.grommet.ui.registration.review.ReviewAndConfirmState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class RegistrationViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val fakeSessionData = SessionData(
        1,
        "temp",
        "temp",
        "temp",
        GeoLocation(1.0, 1.0),
        "temp"
    )

    private val fakeSession = Session(
        partnerInfoId = 1,
        canvasserName = "temp",
        sourceTrackingId = "temp",
        partnerTrackingId = "temp",
        geoLocation = ApiGeoLocation.builder().latitude(1.0).longitude(1.0).build(),
        openTrackingId = "temp",
        deviceId = "1",
        abandonedCount = 0,
        registrationCount = 0,
        smsCount = 0,
        driversLicenseCount = 0,
        clockInTime = Date(),
        clockOutTime = Date(),
        sessionStatus = SessionStatus.CLOCKED_IN
    )

    private lateinit var testDispatcher: TestCoroutineDispatcher

    private lateinit var fakeRegistrationDao: FakeRegistrationDao

    private lateinit var fakeSessionDao: SessionDao

    private lateinit var ut: RegistrationViewModel

    @Before
    fun setUp() {
        testDispatcher = TestCoroutineDispatcher()
        val dispatcherProvider = TestDispatcherProvider(testDispatcher)

        fakeRegistrationDao = FakeRegistrationDao()

        fakeSessionDao = FakeSessionDao(mutableListOf(fakeSession))

        ut = RegistrationViewModel(fakeRegistrationDao, dispatcherProvider, fakeSessionDao)

        // reviewAndConfirmState is backed by a MediatorLiveData, which doesn't produce a value if not observed
        ut.reviewAndConfirmState.observeForever {  }
    }

    @Test
    fun `registrationData init state is empty`() {
        assertNull(currentRegistrationData.newRegistrantData)
        assertNull(currentRegistrationData.addressData)
        assertNull(currentRegistrationData.additionalInfoData)
        assertNull(currentRegistrationData.assistanceData)
        assertNull(currentRegistrationData.reviewData)
    }

    @Test
    fun `registrationState initializes in init state`() {
        assertEquals(RegistrationState.Init, currentRegistrationState)
    }

    @Test
    fun `reviewAndConfirmState initializes with empty data`() {
        val expected = Fake.EMPTY_REVIEW_AND_CONFIRM_STATE_DATA

        val state = currentReviewAndConfirmState as ReviewAndConfirmState.Content

        assertEquals(expected, state.data)
    }

    @Test
    fun `storeNewRegistrantData adds to main data state`() {
        val fakeNewRegistrantData = Fake.NEW_REGISTRANT_DATA

        ut.storeNewRegistrantData(fakeNewRegistrantData)

        assertEquals(fakeNewRegistrantData, currentRegistrationData.newRegistrantData)
    }

    @Test
    fun `storeAddressData adds to main data state`() {
        val fakeAddressData = Fake.PERSONAL_INFO_DATA

        ut.storeAddressData(fakeAddressData)

        assertEquals(fakeAddressData, currentRegistrationData.addressData)
    }

    @Test
    fun `storeAdditionalInfoData adds to main data state`() {
        val fakeAdditionalInfo = Fake.ADDITIONAL_INFO_DATA

        ut.storeAdditionalInfoData(fakeAdditionalInfo)

        assertEquals(fakeAdditionalInfo, currentRegistrationData.additionalInfoData)
    }

    @Test
    fun `storeAssistanceData adds to main data state`() {
        val fakeAssistanceData = Fake.ASSISTANCE_DATA

        ut.storeAssistanceData(fakeAssistanceData)

        assertEquals(fakeAssistanceData, currentRegistrationData.assistanceData)
    }

    @Test
    fun `completeRegistration happy path`() {
        val expectedRegistrationData = RegistrationData(
            Fake.NEW_REGISTRANT_DATA,
            Fake.PERSONAL_INFO_DATA,
            Fake.ADDITIONAL_INFO_DATA,
            Fake.ASSISTANCE_DATA,
            Fake.REVIEW_DATA
        )

        val completionDate = Date()

        val expectedJson = getExpectedRequestJson(expectedRegistrationData, completionDate)

        ut.storeNewRegistrantData(expectedRegistrationData.newRegistrantData!!)
        ut.storeAddressData(expectedRegistrationData.addressData!!)
        ut.storeAdditionalInfoData(expectedRegistrationData.additionalInfoData!!)
        ut.storeAssistanceData(expectedRegistrationData.assistanceData!!)

        var timesInsertCalled = 0
        fakeRegistrationDao.insertHandler = {
            timesInsertCalled++
            assertEquals(1, it.size)

            val registration = it[0]
            assertEquals(expectedJson, registration.registrationData)
        }

        ut.completeRegistration(expectedRegistrationData.reviewData!!, completionDate)

        // Database insertion happens on the IO dispatcher, so we need to advance it to complete
        testDispatcher.advanceUntilIdle()

        assertEquals(1, timesInsertCalled)
        assert(currentRegistrationState is RegistrationState.Complete)
    }

    @Test
    fun `completeRegistration invalid registration exception`() {
        val badRegistrationData = RegistrationData(
            Fake.NEW_REGISTRANT_DATA,
            // This forces an Invalid registration exception
            Fake.PERSONAL_INFO_DATA.copy(
                isMailingAddressDifferent = true,
                mailingAddress = null
            ),
            Fake.ADDITIONAL_INFO_DATA,
            Fake.ASSISTANCE_DATA,
            Fake.REVIEW_DATA
        )

        ut.storeNewRegistrantData(badRegistrationData.newRegistrantData!!)
        ut.storeAddressData(badRegistrationData.addressData!!)
        ut.storeAdditionalInfoData(badRegistrationData.additionalInfoData!!)
        ut.storeAssistanceData(badRegistrationData.assistanceData!!)

        ut.completeRegistration(badRegistrationData.reviewData!!)

        assert(currentRegistrationState is RegistrationState.RegistrationError)
    }

    private fun getExpectedRequestJson(
        expectedRegistrationData: RegistrationData,
        completionDate: Date
    ): String {
        val transformer = RegistrationDataTransformer(
            expectedRegistrationData,
            fakeSessionData,
            completionDate
        )
        val requestData = transformer.transform()

        val adapter = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter(RockyRequest::class.java)

        return adapter.toJson(requestData)
    }

    private val currentRegistrationData
        get() = ut.registrationData.value!!

    private val currentRegistrationState
        get() = ut.registrationState.value!!

    private val currentReviewAndConfirmState
        get() = ut.reviewAndConfirmState.value!!
}
