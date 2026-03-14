package edu.nd.pmcburne.hwapp.one
import androidx.room.Room
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// handles data ops btwn API and local room DB
class GameRepository(private val api: RetrofitAPI, context: Context) {
    private val dao = AppDB.getDatabase(context).gameDao()
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // check if devise has active internet connection
    private fun isOnline(): Boolean {
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

    }

    suspend fun getGames(gender: String, year: String, month: String, day: String): List<GameEntity> = withContext(Dispatchers.IO) {
        val dateKey = "$year/$month/$day"
        // Log.d("NCAA_DEBUG", "1")

        if(isOnline()) {
            // Log.d("NCAA_DEBUG", "2")
            try {
                // fetch from API
                val response = api.getData(gender, year, month, day)
                Log.d("NCAA_DEBUG", "3. API Success! Received ${response.games.size} games from internet.")

                val networkGames = response.games.map { wrapper ->
                    val g = wrapper.game
                    GameEntity(
                        gameID = g.gameID,
                        gender = gender,
                        date = dateKey,
                        homeTeam = g.homeTeam.names.short,
                        awayTeam = g.awayTeam.names.short,
                        homeScore = g.homeTeam.score,
                        awayScore = g.awayTeam.score,
                        gameState = g.gameState,
                        startTime = g.startTime,
                        currentPeriod = g.currentPeriod,
                        winnerHome = g.homeTeam.winner,
                        winnerAway = g.awayTeam.winner
                    )
                }
                // Log.d("NCAA_DEBUG", "4")
                dao.insertGames(networkGames)
                // Log.d("NCAA_DEBUG", "5")

            } catch (e: Exception) {
                // Log.e("NCAA_DEBUG", "CRASH", e)
            }
        } else {
            // Log.d("NCAA_DEBUG", "2.")
        }
        val dbGames = dao.getGamesByDate(dateKey, gender)
        // Log.d("NCAA_DEBUG", "6")
        return@withContext dbGames    }
}