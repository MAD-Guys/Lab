package it.polito.mad.sportapp.playground_availabilities

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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
    // previous selected date
    private val _previousSelectedDate = MutableLiveData<LocalDate>()
    val previousSelectedDate: LiveData<LocalDate> = _previousSelectedDate

    // selected date
    private val _selectedDate = MutableLiveData(LocalDate.now())
    val selectedDate: LiveData<LocalDate> = _selectedDate

    // current month
    private val _currentMonth = MutableLiveData(YearMonth.now())
    val currentMonth: LiveData<YearMonth> = _currentMonth

    // available playgrounds ***for current sport and month***
    private val _availablePlaygrounds = MutableLiveData<Map<LocalDateTime, List<DetailedPlaygroundSport>>>()
    val availablePlaygrounds = _availablePlaygrounds

    init {
    }

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
}