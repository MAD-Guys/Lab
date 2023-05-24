package it.polito.mad.sportapp.playground_availabilities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.room.RoomSport
import it.polito.mad.sportapp.entities.room.RoomDetailedPlaygroundSport
import it.polito.mad.sportapp.model.LocalRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class PlaygroundAvailabilitiesViewModel @Inject constructor(
    private val repository: LocalRepository
) : ViewModel()
{
    // default values
    internal val defaultDate = LocalDate.now()
    internal val defaultMonth = YearMonth.now()

    // all sports
    private val _sports = MutableLiveData<List<RoomSport>>()
    val sports: LiveData<List<RoomSport>> = _sports

    // selected sport
    private val _selectedSport = MutableLiveData<RoomSport?>()
    val selectedSport: LiveData<RoomSport?> = _selectedSport

    // previous selected date
    private val _previousSelectedDate = MutableLiveData<LocalDate>()
    val previousSelectedDate: LiveData<LocalDate> = _previousSelectedDate

    // selected date
    private val _selectedDate = MutableLiveData(defaultDate)
    val selectedDate: LiveData<LocalDate> = _selectedDate

    // current month
    private val _currentMonth = MutableLiveData(defaultMonth)
    val currentMonth: LiveData<YearMonth> = _currentMonth

    // hardcoded slot duration
    internal val slotDuration: Duration = Duration.ofMinutes(30)

    private val _isAvailablePlaygroundsLoadedFlag = MutableLiveData(false)
    private val isAvailablePlaygroundsLoadedFlag: LiveData<Boolean> = _isAvailablePlaygroundsLoadedFlag

    // available playgrounds ***for current sport and month***
    private val _availablePlaygroundsPerSlot:
            MutableLiveData<MutableMap<LocalDate, Map<LocalDateTime, List<RoomDetailedPlaygroundSport>>>> = MutableLiveData()

    internal val availablePlaygroundsPerSlot:
            LiveData<MutableMap<LocalDate, Map<LocalDateTime, List<RoomDetailedPlaygroundSport>>>> = _availablePlaygroundsPerSlot


    internal fun loadAllSportsAsync(sportToShow: Int?) {
        // call the db from a secondary thread
        Thread {
            var allSports = repository.getAllSports().sortedBy { it.name }

            // restrict the loaded sports to one only
            if(sportToShow != null)
                allSports = allSports.filter { sport -> sport.id == sportToShow }

            _sports.postValue(allSports)
        }.start()
    }

    fun setSelectedDate(selectedDate: LocalDate?) {
        val tempPreviousSelectedDate = this.selectedDate.value
        val newSelectedDate = selectedDate ?: LocalDate.now()

        // default selected date is the current date
        this._selectedDate.value = newSelectedDate
        // update previous selected date
        this._previousSelectedDate.value = tempPreviousSelectedDate
    }

    fun setCurrentMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun getAvailablePlaygroundsOnSelectedDate(): Map<LocalDateTime, List<RoomDetailedPlaygroundSport>> {
        val selectedDate = this.selectedDate.value ?: defaultDate

        return getAvailablePlaygroundsOn(selectedDate)
    }

    fun getAvailablePlaygroundsOn(date: LocalDate): Map<LocalDateTime, List<RoomDetailedPlaygroundSport>> {
        return availablePlaygroundsPerSlot.value.orEmpty()[date] ?: mapOf()
    }

    fun updatePlaygroundAvailabilitiesForCurrentMonthAndSport() {
        Thread {
            val oldAvailabilities = this.availablePlaygroundsPerSlot.value ?: mutableMapOf()
            val newAvailabilities = this.getPlaygroundAvailabilitiesForCurrentMonthAndSport().also {
                // merge new months' availabilities with the previous one's
                it.putAll(oldAvailabilities)
            }

            this._availablePlaygroundsPerSlot.postValue(newAvailabilities)
        }.start()
    }

    private fun getPlaygroundAvailabilitiesForCurrentMonthAndSport()
        : MutableMap<LocalDate, Map<LocalDateTime, List<RoomDetailedPlaygroundSport>>> {
        val currentMonthAvailabilities = repository.getAvailablePlaygroundsPerSlot(
            currentMonth.value ?: defaultMonth,
            selectedSport.value
        )

        val previousMonthAvailabilities = repository.getAvailablePlaygroundsPerSlot(
            currentMonth.value?.minusMonths(1) ?: defaultMonth,
            selectedSport.value
        )

        val nextMonthAvailabilities = repository.getAvailablePlaygroundsPerSlot(
            currentMonth.value?.plusMonths(1) ?: defaultMonth,
            selectedSport.value
        )

        val allAvailabilities = mutableMapOf<LocalDate, Map<LocalDateTime, List<RoomDetailedPlaygroundSport>>>().also {
            it.putAll(currentMonthAvailabilities)
            it.putAll(previousMonthAvailabilities)
            it.putAll(nextMonthAvailabilities)
        }

        return allAvailabilities
    }

    fun isAvailablePlaygroundsLoaded(): Boolean = isAvailablePlaygroundsLoadedFlag.value ?: false
    fun setAvailablePlaygroundsLoaded(loaded: Boolean = true) {
        _isAvailablePlaygroundsLoadedFlag.value = loaded
    }

    /* selected sport */
    fun setSelectedSport(selectedSport: RoomSport?) {
        this._selectedSport.value = selectedSport
    }

    fun clearAvailablePlaygroundsPerSlot() {
        _availablePlaygroundsPerSlot.value = mutableMapOf()
    }
}