package com.rockthevote.grommet.ui.registration

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import com.rockthevote.grommet.data.db.dao.PartnerInfoDao
import com.rockthevote.grommet.ui.registration.name.BirthdayValidationState
import com.rockthevote.grommet.ui.registration.personal.AdditionalInfoPartnerData
import com.rockthevote.grommet.util.Dates
import com.rockthevote.grommet.util.coroutines.DispatcherProvider
import com.rockthevote.grommet.util.coroutines.DispatcherProviderImpl
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

/**
 * Created by Mechanical Man on 6/14/20.
 */
class PartnerPreferenceViewModel(
        private val dispatchers: DispatcherProvider = DispatcherProviderImpl(),
        private val partnerInfoDao: PartnerInfoDao) : ViewModel() {

    val additionalInfoPartnerData =
            Transformations.map(partnerInfoDao.getCurrentPartnerInfoLive()) {
                AdditionalInfoPartnerData(
                        it.partnerName,
                        it.volunteerText
                )
            }

    val registrationText = Transformations.map(partnerInfoDao.getCurrentPartnerInfoLive()) {
        it.registrationNotificationText
    }

    private val _birthdayValidationState = LiveEvent<BirthdayValidationState>()
    val birthdayValidationState: LiveData<BirthdayValidationState> = _birthdayValidationState

    /*
        check if registrant will be 18 by the election date.
        Calendar uses 0 as the first month but LocalDate does not, so make sure and add 1 to it
     */
    fun validateBirthDay(birthDate: Date) {
        viewModelScope.launch(dispatchers.io) {
            val registrationDeadline = partnerInfoDao.getCurrentPartnerInfo().registrationDeadlineDate

            val birthCal = Calendar.getInstance()
            birthCal.time = birthDate

            val regCal = Calendar.getInstance()
            regCal.time = registrationDeadline

            val regDate = LocalDate.of(
                    regCal[Calendar.YEAR],
                    regCal[Calendar.MONTH] + 1,
                    regCal[Calendar.DAY_OF_MONTH])

            val birthday = LocalDate.of(
                    birthCal[Calendar.YEAR],
                    birthCal[Calendar.MONTH] + 1,
                    birthCal[Calendar.DAY_OF_MONTH])


            if (ChronoUnit.YEARS.between(birthday, regDate) >= 18) {
                _birthdayValidationState.postValue(BirthdayValidationState.Success)
            } else {
                _birthdayValidationState.postValue(
                        BirthdayValidationState.Error(Dates.formatAsISO8601_ShortDate(registrationDeadline)))
            }
        }
    }
}

class PartnerPreferenceViewModelFactory(
        private val partnerInfoDao: PartnerInfoDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val dispatchers = DispatcherProviderImpl()

        @Suppress("UNCHECKED_CAST")
        return PartnerPreferenceViewModel(dispatchers, partnerInfoDao) as T
    }
}