/* Resources:
https://stackoverflow.com/questions/74889783/how-can-i-use-retrofit-to-parse-a-json-response-from-an-api-that-has-an-embedde
 */
package edu.nd.pmcburne.hwapp.one

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ScoreboardResponse(
    val games: List<GameWrapper> = emptyList()
)

@Serializable
data class GameWrapper(
    val game: GameDataModel
)

@Serializable
data class GameDataModel(
    val gameID: String = "",

    @SerialName("away")
    val awayTeam: AwayTeam,

    @SerialName("home")
    val homeTeam: HomeTeam,

    val url: String = "",
    val network: String = "",
    val startTime: String = "",
    val gameState: String = "",
    val startDate: String = "",
    val currentPeriod: String = "",
    val contestClock: String = ""
)

@Serializable
data class AwayTeam(
    val score: String = "",
    val names: TeamNames,
    val winner: Boolean = false,
    val rank: String = "",
    val conferences: List<Conference> = emptyList(),
)

@Serializable
data class HomeTeam(
    val score: String = "",
    val names: TeamNames,
    val winner: Boolean = false,
    val rank: String = "",
    val conferences: List<Conference> = emptyList(),
)

@Serializable
data class TeamNames(
    val short: String = "",
    val char6: String = "",
    val seo: String = ""
)

@Serializable
data class Conference(
    val conferenceName: String = "",
    val conferenceSeo: String = ""
)