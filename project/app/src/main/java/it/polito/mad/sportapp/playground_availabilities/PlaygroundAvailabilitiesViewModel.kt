package it.polito.mad.sportapp.playground_availabilities

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.entities.DetailedPlaygroundSport
import it.polito.mad.sportapp.entities.firestore.utilities.FireListener
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult.*
import it.polito.mad.sportapp.model.IRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class PlaygroundAvailabilitiesViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel()
{
    // default values
    internal val defaultDate = LocalDate.now()
    internal val defaultMonth = YearMonth.now()

    // all sports
    private val _sports = MutableLiveData<List<Sport>>()
    val sports: LiveData<List<Sport>> = _sports

    // selected sport
    private val _selectedSport = MutableLiveData<Sport?>()
    val selectedSport: LiveData<Sport?> = _selectedSport

    // previous selected date
    private val _previousSelectedDate = MutableLiveData<LocalDate>()
    val previousSelectedDate: LiveData<LocalDate> = _previousSelectedDate

    // selected date
    private val _selectedDate = MutableLiveData<LocalDate>()
    val selectedDate: LiveData<LocalDate> = _selectedDate

    // current month
    private val _currentMonth = MutableLiveData<YearMonth>()
    val currentMonth: LiveData<YearMonth> = _currentMonth

    // hardcoded slot duration
    internal val slotDuration: Duration = Duration.ofMinutes(30)

    private val _isAvailablePlaygroundsLoadedFlag = MutableLiveData(false)
    internal val isAvailablePlaygroundsLoadedFlag: LiveData<Boolean> = _isAvailablePlaygroundsLoadedFlag

    // available playgrounds ***for current sport and month***
    private val _availablePlaygroundsPerSlot:
            MutableLiveData<MutableMap<LocalDate, Map<LocalDateTime, List<DetailedPlaygroundSport>>>?> = MutableLiveData()

    internal val availablePlaygroundsPerSlot:
            LiveData<MutableMap<LocalDate, Map<LocalDateTime, List<DetailedPlaygroundSport>>>?> = _availablePlaygroundsPerSlot

    // error message to show in a red toast in case of app errors
    private val _toastErrorMessage = MutableLiveData<String>()
    internal val toastErrorMessage: LiveData<String> = _toastErrorMessage

    internal var listener = FireListener()


    internal fun loadAllSportsAsync(sportToShow: String?) {
        repository.getAllSports { fireResult ->
            when(fireResult) {
                is Error -> {
                    Log.e("PlaygroundAvailabilitiesViewModel error", "repository.getAllSports() returned error")
                    _toastErrorMessage.value = fireResult.errorMessage()
                    return@getAllSports
                }
                is Success -> {
                    var allSports = fireResult.value.sortedBy { it.name }

                    // restrict the loaded sports to one only
                    if(sportToShow != null)
                        allSports = allSports.filter { sport -> sport.id == sportToShow }

                    _sports.postValue(allSports)
                }
            }
        }
    }

    fun setSelectedDate(selectedDate: LocalDate?) {
        val tempPreviousSelectedDate = this.selectedDate.value ?: defaultDate
        val newSelectedDate = selectedDate ?: defaultDate

        // default selected date is the current date
        this._selectedDate.value = newSelectedDate
        // update previous selected date
        this._previousSelectedDate.value = tempPreviousSelectedDate
    }

    fun setCurrentMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun getAvailablePlaygroundsOnSelectedDate(): Map<LocalDateTime, List<DetailedPlaygroundSport>> {
        val selectedDate = this.selectedDate.value ?: defaultDate

        return getAvailablePlaygroundsOn(selectedDate)
    }

    fun getAvailablePlaygroundsOn(date: LocalDate): Map<LocalDateTime, List<DetailedPlaygroundSport>> {
        return availablePlaygroundsPerSlot.value.orEmpty()[date] ?: mapOf()
    }

    fun updatePlaygroundAvailabilitiesForCurrentMonthAndSport() {
        listener.unregister()

        val listener = this.getPlaygroundAvailabilitiesForCurrentMonthAndSport { newAvailabilities ->
            // (this callback is executed only if no error occurred)

            // merge new months' availabilities with the previous one's
            val oldAvailabilities = this.availablePlaygroundsPerSlot.value

            val mergedAvailabilities =
                if(oldAvailabilities == null && newAvailabilities == null) null
                else mutableMapOf<LocalDate, Map<LocalDateTime, List<DetailedPlaygroundSport>>>().also {
                        it.putAll(oldAvailabilities ?: mutableMapOf())
                        it.putAll(newAvailabilities ?: mutableMapOf())
                    }

            // save merged availabilities in the LiveData
            this._availablePlaygroundsPerSlot.postValue(mergedAvailabilities)
        }

        this.listener.add(listener)
    }

    private fun getPlaygroundAvailabilitiesForCurrentMonthAndSport(
        returnCallback: (MutableMap<LocalDate, Map<LocalDateTime, List<DetailedPlaygroundSport>>>?) -> Unit
    ): FireListener {
        val fireListener = FireListener()

        // retrieve current month availabilities
        val listener1 = repository.getAvailablePlaygroundsPerSlot(
            currentMonth.value ?: defaultMonth,
            selectedSport.value
        ) getAvailablePlaygroundsPerSlot1@ { fireResult ->
            when(fireResult) {
                is Error -> {
                    // show error message in a toast
                    _toastErrorMessage.value = fireResult.errorMessage()
                    return@getAvailablePlaygroundsPerSlot1
                }
                is Success -> {
                    val currentMonthAvailabilities = fireResult.unwrap()

                    // retrieve previous month availabilities
                    val listener2 = repository.getAvailablePlaygroundsPerSlot(
                        currentMonth.value?.minusMonths(1) ?: defaultMonth,
                        selectedSport.value
                    ) getAvailablePlaygroundsPerSlot2@ { fireResult2 ->
                        when(fireResult2) {
                            is Error -> {
                                // show error message in a toast
                                _toastErrorMessage.value = fireResult2.errorMessage()
                                return@getAvailablePlaygroundsPerSlot2
                            }
                            is Success -> {
                                val previousMonthAvailabilities = fireResult2.unwrap()

                                // retrieve next month availabilities
                                val listener3 = repository.getAvailablePlaygroundsPerSlot(
                                    currentMonth.value?.plusMonths(1) ?: defaultMonth,
                                    selectedSport.value
                                ) getAvailablePlaygroundsPerSlot3@ { fireResult3 ->
                                    when(fireResult3) {
                                        is Error -> {
                                            // show error message in a toast
                                            _toastErrorMessage.value = fireResult3.errorMessage()
                                            return@getAvailablePlaygroundsPerSlot3
                                        }
                                        is Success -> {
                                            val nextMonthAvailabilities = fireResult3.unwrap()

                                            val allAvailabilities = mutableMapOf<LocalDate, Map<LocalDateTime, List<DetailedPlaygroundSport>>>().also {
                                                if(currentMonthAvailabilities != null)
                                                    it.putAll(currentMonthAvailabilities)

                                                if(previousMonthAvailabilities != null)
                                                    it.putAll(previousMonthAvailabilities)

                                                if(nextMonthAvailabilities != null)
                                                    it.putAll(nextMonthAvailabilities)
                                            }

                                            if (currentMonthAvailabilities == null && previousMonthAvailabilities == null && nextMonthAvailabilities == null)
                                                returnCallback(null)
                                            else
                                                returnCallback(allAvailabilities)
                                        }
                                    }
                                }

                                fireListener.add(listener3)
                            }
                        }
                    }

                    fireListener.add(listener2)
                }
            }
        }

        fireListener.add(listener1)
        return fireListener
    }

    fun isAvailablePlaygroundsLoaded(): Boolean = isAvailablePlaygroundsLoadedFlag.value ?: false
    fun setAvailablePlaygroundsLoaded(loaded: Boolean = true) {
        _isAvailablePlaygroundsLoadedFlag.value = loaded
    }

    /* selected sport */
    fun setSelectedSport(selectedSport: Sport?) {
        this._selectedSport.value = selectedSport
    }

    fun clearAvailablePlaygroundsPerSlot() {
        _availablePlaygroundsPerSlot.value = null
    }
}