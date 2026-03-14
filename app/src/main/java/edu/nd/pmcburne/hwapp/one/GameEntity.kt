package edu.nd.pmcburne.hwapp.one
import androidx.room.*

// individual game rows in SQLite db
@Entity(tableName = "games", primaryKeys = ["gameID", "gender"])
data class GameEntity(
    val gameID: String,
    val gender: String,
    val date: String, // "YYYY/MM/DD"
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: String,
    val awayScore: String,
    val gameState: String, // 'pre' or 'live' or "final"
    val startTime: String,
    val currentPeriod: String, // 'HALFTIME', '1st',
    val winnerHome: Boolean,
    val winnerAway: Boolean
)

// data access object for app to interact w/ DB
@Dao
interface GameDao {
    // offline mode: retrieve stored games
    @Query("SELECT * FROM games WHERE date = :date AND gender = :gender")
    suspend fun getGamesByDate(date: String, gender: String): List<GameEntity>

    // update w/ newest game data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>)
}

