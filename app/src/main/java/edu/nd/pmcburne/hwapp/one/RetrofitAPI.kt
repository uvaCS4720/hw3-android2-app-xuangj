/* Resources:
https://www.geeksforgeeks.org/kotlin/how-to-post-data-to-api-using-retrofit-in-android-using-jetpack-compose/
 */

package edu.nd.pmcburne.hwapp.one
import retrofit2.Call
import retrofit2.http.*

interface RetrofitAPI {
    // pass gender and date into URL
    @GET("scoreboard/basketball-{gender}/d1/{year}/{month}/{day}")
    suspend fun getData(
        @Path("gender") gender: String,
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): ScoreboardResponse
}