package edu.nd.pmcburne.hwapp.one
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Calendar

// screen can be in three states
sealed interface GameUIState {
    data class Success(val games: List<GameEntity>) : GameUIState
    object Error : GameUIState
    object Loading : GameUIState
}

class GameViewModel(private val repository: GameRepository) : ViewModel() {
    var gameUIState: GameUIState by mutableStateOf(GameUIState.Loading) // screen state
    var selectedGender by mutableStateOf("men")
    var calendar: Calendar by mutableStateOf(Calendar.getInstance())

    // refresh when date or gender changes; asks repository for new data
    fun refresh() {
        viewModelScope.launch {
            gameUIState = GameUIState.Loading

            val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
            val year = calendar.get(Calendar.YEAR).toString()

            // try to fetch data
            gameUIState = try {
                val listResult = repository.getGames(selectedGender, year, month, day)
                GameUIState.Success(listResult)
            } catch (e: IOException) {
                GameUIState.Error
            }
        }
    }
}