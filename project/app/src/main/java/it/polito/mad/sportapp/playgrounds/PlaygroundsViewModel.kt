package it.polito.mad.sportapp.playgrounds

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import it.polito.mad.sportapp.entities.PlaygroundInfo
import it.polito.mad.sportapp.entities.firestore.utilities.DefaultGetFireError
import it.polito.mad.sportapp.entities.firestore.utilities.FireResult
import it.polito.mad.sportapp.model.IRepository
import javax.inject.Inject

@HiltViewModel
class PlaygroundsViewModel @Inject constructor(
    private val repository: IRepository
) : ViewModel() {

    private var _getError = MutableLiveData<DefaultGetFireError?>()
    var getError: LiveData<DefaultGetFireError?> = _getError

    fun clearError() {
        _getError = MutableLiveData<DefaultGetFireError?>()
        getError = _getError
    }

    private val _playgrounds = MutableLiveData<List<PlaygroundInfo>>(listOf())
    val playgrounds: LiveData<List<PlaygroundInfo>> = _playgrounds

    fun loadPlaygroundsFromDb() {
        repository.getAllPlaygroundsInfo { fireResult ->
            when (fireResult) {
                is FireResult.Error -> {
                    Log.e(fireResult.type.message(), fireResult.errorMessage())
                    _getError.postValue(fireResult.type)
                }

                is FireResult.Success -> {
                    val result = fireResult.value

                    _playgrounds.postValue(result)
                }
            }
        }
    }

    enum class PlaygroundOrderKey {
        SPORT, CENTER
    }

    /** return actual viewModel playgrounds value ordered by sport */
    fun getPlaygroundsOrderedBySport(): List<PlaygroundInfo?> {
        val allPlaygrounds = _playgrounds.value!!

        //  order them
        return this.separateAndOrderPlaygroundsBy(PlaygroundOrderKey.SPORT, allPlaygrounds)
    }

    /** return actual viewModel playgrounds value ordered by center */
    fun getPlaygroundsOrderedByCenter(): List<PlaygroundInfo?> {
        val allPlaygrounds = _playgrounds.value!!

        //  order them
        return this.separateAndOrderPlaygroundsBy(PlaygroundOrderKey.CENTER, allPlaygrounds)
    }

    /**
     * Order the input playgrounds list by the specified key ("sport" or
     * "center") alphabetically and insert an empty entry before each key group
     * of playgrounds. E.g.:
     * [null, Basket Playground1, Basket Playground2, null, Tennis Playground1, Tennis Playground2, Tennis Playground3, null, Volleyball Playground1, etc.])
     */
    fun separateAndOrderPlaygroundsBy(
        key: PlaygroundOrderKey,
        playgrounds: List<PlaygroundInfo>
    ): List<PlaygroundInfo?> {
        if (playgrounds.isEmpty())
            return playgrounds

        return when (key) {
            PlaygroundOrderKey.SPORT -> {
                // order playgrounds by sport
                val orderedPlaygrounds = playgrounds.asSequence()
                    .sortedBy { playground -> playground.sportName }
                    .toMutableList<PlaygroundInfo?>()

                // now insert a 'null' entry before each sport type
                val iterator = orderedPlaygrounds.listIterator()
                var lastSportId: String? = null

                while (iterator.hasNext()) {
                    val nextPlayground = iterator.next()!!

                    // check sport type of the next playground
                    if (nextPlayground.sportId != lastSportId) {
                        // move back cursor
                        iterator.previous()
                        // add a null entry
                        iterator.add(null)
                        // come back to the next position
                        iterator.next()
                        // update last sport id
                        lastSportId = nextPlayground.sportId
                    }
                }

                orderedPlaygrounds
            }

            PlaygroundOrderKey.CENTER -> {
                // order playgrounds by sport center
                val orderedPlaygrounds = playgrounds.asSequence()
                    .sortedBy { playground -> playground.sportCenterName }
                    .toMutableList<PlaygroundInfo?>()

                // now insert a 'null' entry before each sport center
                val iterator = orderedPlaygrounds.listIterator()
                var lastSportCenterId: String? = null

                while (iterator.hasNext()) {
                    val nextPlayground = iterator.next()!!

                    // check sport center of the next playground
                    if (nextPlayground.sportCenterId != lastSportCenterId) {
                        // move back cursor
                        iterator.previous()
                        // add a null entry
                        iterator.add(null)
                        // come back to the next position
                        iterator.next()
                        // update last sport center id
                        lastSportCenterId = nextPlayground.sportCenterId
                    }
                }

                orderedPlaygrounds
            }
        }
    }
}