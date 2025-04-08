package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                TemperatureStatsApp()
            }
        }
    }
}

//Обробка помилок
class TemperatureOutOfRangeException(message: String) : Exception(message)

@Composable
fun TemperatureStatsApp(modifier: Modifier = Modifier) {
    var daysInput by remember { mutableStateOf("") }
    var temperaturesInput by remember { mutableStateOf(listOf<String>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var stats by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))


        OutlinedTextField(
            value = daysInput,
            onValueChange = {
                daysInput = it
                errorMessage = null
                // Оновлення списку температур
                temperaturesInput = List(it.toIntOrNull() ?: 0) { "" }
            },
            label = { Text("Кількість днів") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Динамічно створені поля для введення температури по кожному дню
        temperaturesInput.forEachIndexed { index, value ->
            OutlinedTextField(
                value = value,
                onValueChange = {
                    temperaturesInput = temperaturesInput.toMutableList().also { list -> list[index] = it }
                    errorMessage = null
                },
                label = { Text("Температура за день ${index + 1}") },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                try {
                    // Перетворення введених значень у список чисел та перевірка діапазону
                    val temps = temperaturesInput.mapIndexed { index, str ->
                        val temp = str.toDoubleOrNull() ?: throw Exception("Невірне значення на день ${index + 1}")
                        if (temp < -60 || temp > 60) throw TemperatureOutOfRangeException("Температура $temp поза діапазоном (-60; 60)")
                        temp
                    }

                    // Обчислення статистики
                    val average = temps.average()
                    val frostyDays = temps.count { it < 0 }
                    val warmest = temps.maxOrNull() ?: 0.0
                    val coldest = temps.minOrNull() ?: 0.0

                    val comment = when {
                        average < 0 -> "Холодно"
                        average > 0 && average <=20 -> "Чіназез"
                        else -> "Тепло"
                    }

                    stats = """
                        Середня температура: %.2f
                        Кількість морозних днів: %d
                        Найвища температура: %.2f
                        Найнижча температура: %.2f
                        Коментар: %s
                    """.trimIndent().format(average, frostyDays, warmest, coldest, comment)
                } catch (e: TemperatureOutOfRangeException) {
                    errorMessage = e.message
                    stats = ""
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Помилка вводу"
                    stats = ""
                }
            }
        ) {
            Text("Аналізувати")
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        if (stats.isNotBlank()) {
            Text(text = stats)
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewTemperatureStatsApp() {
    MyApplicationTheme {
        TemperatureStatsApp()
    }
}
