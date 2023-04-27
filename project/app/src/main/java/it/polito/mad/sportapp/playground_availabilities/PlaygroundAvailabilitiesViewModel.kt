package it.polito.mad.sportapp.playground_availabilities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.Sport
import it.polito.mad.sportapp.model.DetailedPlaygroundSport
import it.polito.mad.sportapp.model.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class PlaygroundAvailabilitiesViewModel @Inject constructor(
    repository: Repository
) : ViewModel() {
    private val defaultSport = Sport(0, "Basket", 10)
    private val defaultDate = LocalDate.now()
    private val defaultMonth = YearMonth.now()

    // selected sport
    private val _selectedSport = MutableLiveData(defaultSport)
    val selectedSport: LiveData<Sport> = _selectedSport

    // previous selected date
    private val _previousSelectedDate = MutableLiveData<LocalDate>()
    val previousSelectedDate: LiveData<LocalDate> = _previousSelectedDate

    // selected date
    private val _selectedDate = MutableLiveData(defaultDate)
    val selectedDate: LiveData<LocalDate> = _selectedDate

    // current month
    private val _currentMonth = MutableLiveData(defaultMonth)
    val currentMonth: LiveData<YearMonth> = _currentMonth

    // available playgrounds ***for current sport and month***
    private val _availablePlaygrounds:
            MutableLiveData<Map<LocalDateTime, List<DetailedPlaygroundSport>>> =
                // retrieve initial playgrounds availabilities from the repository, for current month and default sport
                repository.getAvailablePlaygroundsIn(
                    currentMonth.value ?: defaultMonth,
                    selectedSport.value ?: defaultSport
                )

    private val availablePlaygrounds:
            LiveData<Map<LocalDateTime, List<DetailedPlaygroundSport>>> = _availablePlaygrounds

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
        return this.availablePlaygrounds.value.orEmpty().filterKeys {
            it.toLocalDate() == this.selectedDate.value
        }
    }
}