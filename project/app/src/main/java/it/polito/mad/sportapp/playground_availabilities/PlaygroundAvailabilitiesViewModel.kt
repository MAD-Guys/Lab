package it.polito.mad.sportapp.playground_availabilities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.model.DetailedPlaygroundSport
import it.polito.mad.sportapp.model.Repository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class PlaygroundAvailabilitiesViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    internal val defaultDate = LocalDate.now()
    internal val defaultMonth = YearMonth.now()

    // selected sport
    val sports = mutableListOf<Sport>().also {
        Thread {
            it.addAll(repository.getAllSports())
        }.start()
    }
    private val defaultSport = Sport(0, "Basket", 10)
    private val _selectedSport = MutableLiveData(defaultSport)
    val selectedSport: LiveData<Sport> = _selectedSport // TODO

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
    private val slotDuration: Duration = Duration.ofMinutes(30)


    // available playgrounds ***for current sport and month***
    private var _availablePlaygroundsPerSlot:
            MutableLiveData<Map<LocalDateTime, List<DetailedPlaygroundSport>>> =
                MutableLiveData(
                    // retrieve initial playgrounds availabilities from the repository,
                    // for current month and default sport
                    repository.getAvailablePlaygroundsPerSlotIn(
                        currentMonth.value ?: defaultMonth,
                        selectedSport.value ?: defaultSport
                    )
                )


    internal var availablePlaygroundsPerSlot:
            LiveData<Map<LocalDateTime, List<DetailedPlaygroundSport>>> = _availablePlaygroundsPerSlot

    fun setSelectedDate(newSelectedDate: LocalDate?) {
        val tempPreviousSelectedDate = this.selectedDate.value

        // default selected date is the current date
        this._selectedDate.value = newSelectedDate ?: LocalDate.now()
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

        val availablePlaygrounds = this.availablePlaygroundsPerSlot.value.orEmpty().filterKeys {
            it.toLocalDate() == date
        }

        // fill with missing slots
        return fillWithMissingTimeSlots(availablePlaygrounds)
    }

    fun updatePlaygroundAvailabilities(newMonth: YearMonth) {
        this._availablePlaygroundsPerSlot.value = repository.getAvailablePlaygroundsPerSlotIn(
            newMonth,
            selectedSport.value ?: defaultSport
        )
    }

    private fun fillWithMissingTimeSlots(
        availablePlaygroundsOnDate: Map<LocalDateTime, List<DetailedPlaygroundSport>>,
    ): Map<LocalDateTime, List<DetailedPlaygroundSport>> {
        val availablePlaygroundsInAllSlots = availablePlaygroundsOnDate.toMutableMap()

        // if there are no available playgrounds on this date, keep the map empty
        if(availablePlaygroundsOnDate.isEmpty())
            return availablePlaygroundsInAllSlots

        // retrieve the first and the last slot to show
        var startSlot = availablePlaygroundsInAllSlots.keys.min()
        val endSlot = availablePlaygroundsInAllSlots.keys.max()

        // fill the missing slots between the first and the last one
        while(startSlot.isBefore(endSlot)) {
            if (!availablePlaygroundsInAllSlots.containsKey(startSlot))
                availablePlaygroundsInAllSlots[startSlot] = emptyList()

            startSlot = startSlot.plus(slotDuration)
        }

        return availablePlaygroundsInAllSlots
    }
}