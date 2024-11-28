package com.example.mpfinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mpfinal.ui.theme.MPFinalTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

// Classe de dados
data class SensorData(val dateTime: String = "", val humidity: Int = 0, val temperature: Int = 0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MPFinalTheme {
                Surface(modifier = Modifier.padding(16.dp)) {
                    SensorDataApp()
                }
            }
        }
    }
}

@Composable
fun SensorDataApp() {
    var date by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var allSensorData by remember { mutableStateOf<List<SensorData>>(emptyList()) }
    var filteredData by remember { mutableStateOf<List<SensorData>>(emptyList()) }

    // Função para buscar todos os dados do Firestore
    fun fetchAllData() {
        val db = FirebaseFirestore.getInstance()

        db.collection("sensor")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    error = "Nenhum dado encontrado."
                } else {
                    allSensorData = documents.map { it.toObject<SensorData>() }
                    filteredData = allSensorData
                    error = ""
                }
            }
            .addOnFailureListener { e ->
                error = "Erro ao buscar dados: ${e.message}"
            }
    }

    // Função para filtrar os dados com base na data
    fun filterData() {
        filteredData = if (date.isEmpty()) {
            allSensorData
        } else {
            allSensorData.filter { it.dateTime.contains(date) }
        }
    }

    // Carregar os dados ao iniciar
    fetchAllData()

    // Filtrar sempre que a data mudar
    filterData()

    Column {
        // Campo de entrada de data para filtragem
        TextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Filtrar por Data (DD/MM/YYYY)") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { filterData() }
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Exibe mensagem de erro, se houver
        if (error.isNotEmpty()) {
            Text(text = error, color = Color.Red)
        }

        // Exibe os dados em uma tabela
        if (filteredData.isNotEmpty()) {
            Text("Todos os Dados:", modifier = Modifier.padding(top = 16.dp))

            LazyColumn(
                contentPadding = PaddingValues(top = 16.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Cabeçalho da tabela
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Data/Hora", modifier = Modifier.weight(1f))
                        Text("Umidade", modifier = Modifier.weight(1f))
                        Text("Temperatura", modifier = Modifier.weight(1f))
                    }
                    Divider()
                }

                // Exibe os itens da tabela
                items(filteredData) { data ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(data.dateTime, modifier = Modifier.weight(1f))
                        Text("${data.humidity}%", modifier = Modifier.weight(1f))
                        Text("${data.temperature}°C", modifier = Modifier.weight(1f))
                    }
                    Divider()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MPFinalTheme {
        SensorDataApp()
    }
}
