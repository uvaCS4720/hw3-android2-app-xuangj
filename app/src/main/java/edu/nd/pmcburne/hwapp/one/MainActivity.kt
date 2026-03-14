package edu.nd.pmcburne.hwapp.one

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.nd.pmcburne.hwapp.one.ui.theme.HWStarterRepoTheme
import java.util.*
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType
import kotlinx.serialization.json.Json



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val networkJson = Json { ignoreUnknownKeys = true }

        // API and repository
        val retrofit = Retrofit.Builder()
            .baseUrl("https://ncaa-api.henrygd.me/")
            .addConverterFactory(networkJson.asConverterFactory(MediaType.get("application/json")))
            .build()
        val api = retrofit.create(RetrofitAPI::class.java)
        val repository = GameRepository(api, this)
        val viewModel = GameViewModel(repository)

        setContent {
            HWStarterRepoTheme {
                // default to showing today's date
                ScoreboardScreen(viewModel)
            }
        }
    }
}

@Composable
fun ScoreboardScreen(viewModel: GameViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    // data load
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("NCAA Scoreboard") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // date picker
                Button(onClick = { showDatePicker = true }) {
                    val cal = viewModel.calendar
                    Text("Date: ${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.YEAR)}")
                }
                Spacer(modifier = Modifier.height(8.dp))

                // toggle btwn men's and women's games
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Men")
                    Switch(
                        checked = viewModel.selectedGender == "women",
                        onCheckedChange = { isWomen ->
                            viewModel.selectedGender = if (isWomen) "women" else "men"
                            viewModel.refresh()
                        }
                    )
                    Text("Women")
                }
            }


            // refresh btn
            Button(
                onClick = { viewModel.refresh() },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Refresh Scores")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = viewModel.gameUIState) {
                    is GameUIState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is GameUIState.Success -> {
                        if (state.games.isEmpty()) {
                            Text(
                                text = "No games scheduled for this date.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(state.games) { game ->
                                    GameItem(game)
                                }
                            }
                        }
                    }
                    is GameUIState.Error -> {
                        Text(
                            text = "Failed to load games. Check connection.",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        if (showDatePicker) {
            GameDatePickerDialog(
                onDateSelected = { cal ->
                    viewModel.calendar = cal
                    viewModel.refresh()
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

@Composable
fun GameItem(game: GameEntity) {
    // game card
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // home vs away team names
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Away: ${game.awayTeam}", fontWeight = FontWeight.Bold, color = Color.Black)
                // show score if live or finished
                if(game.gameState != "pre") Text(text = game.awayScore)

            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Home: ${game.homeTeam}", fontWeight = FontWeight.Bold, color = Color.Black)

                if(game.gameState != "pre") Text(text = game.homeScore)

            }
            Spacer(modifier = Modifier.height(8.dp))

            // game status
            when(game.gameState) {
                "pre" -> {
                    Text(text = "${game.homeTeam} vs $game.awayTeam starts at: ${game.startTime}", color = Color.Gray)
                }
                "live" -> {
                    // show curr period and time left
                    Text(text = "Live: ${game.currentPeriod}", color = Color.Blue)
                }
                "final" -> {
                    // show final info and winning team
                    val winner = if (game.winnerHome) game.homeTeam else game.awayTeam
                    Text(text = "Final", fontWeight = FontWeight.ExtraBold, color = Color.Red)
                    Text(text = "Winner: $winner", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.bodySmall)
                }
            }

        }
    }
}


// Date picker
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDatePickerDialog(
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton= {
            TextButton(onClick = {
                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    timeInMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                }
                onDateSelected(cal)
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
