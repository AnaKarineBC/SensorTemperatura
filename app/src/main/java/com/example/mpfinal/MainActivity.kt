package com.example.mpfinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
data class SensorData(val date: String = "", val hmd: String = "", val temp: String = "", val time: String = "")

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorDataApp() {
    var date by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var allSensorData by remember { mutableStateOf<List<SensorData>>(emptyList()) }
    var filteredData by remember { mutableStateOf<List<SensorData>>(emptyList()) }

    // Função para buscar todos os dados do Firestore
    fun fetchAllData() {
        val db = FirebaseFirestore.getInstance()

        db.collection("SensorData")
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
            allSensorData.filter { it.date.contains(date) }
        }
    }

    // Carregar os dados ao iniciar
    fetchAllData()

    // Layout principal sem Scaffold
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Fundo branco
            .padding(8.dp), // Padding reduzido para telas menores
        verticalArrangement = Arrangement.spacedBy(12.dp) // Menor espaçamento entre os itens
    ) {
        // Top bar customizada
        Text(
            text = "Sensor Data Viewer",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.DarkGray)
                .padding(16.dp)
        )

        // Campo de entrada de data para filtragem
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Filtrar por Data (YYYY-MM-DD)", color = Color.Black) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { filterData() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp), // Ajustando o espaçamento
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.Black
            )
        )

        // Botão para aplicar filtro
        Button(
            onClick = { filterData() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Buscar Dados", color = Color.White)
        }

        // Exibe mensagem de erro, se houver
        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }

        // Exibe os dados em uma tabela
        if (filteredData.isNotEmpty()) {
            Text(
                text = "Resultados Encontrados:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp), color = Color.Black
            )

            // LazyColumn com scroll
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
                    .weight(1f) // Preenchendo o espaço restante
            ) {
                // Cabeçalho da tabela
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(Color.Black.copy(alpha = 0.1f))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp) // Ajustando o espaçamento
                    ) {
                        Text("Data", modifier = Modifier.weight(1f), color = Color.Black)
                        Text("Umid.", modifier = Modifier.weight(1f), color = Color.Black)
                        Text("Temp.", modifier = Modifier.weight(1f), color = Color.Black)
                        Text("Hora", modifier = Modifier.weight(1f), color = Color.Black)
                    }
                }

                // Exibe os itens da tabela
                items(filteredData) { data ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp) // Ajustando o espaçamento
                    ) {
                        Text(data.date, modifier = Modifier.weight(1f), color = Color.Black)
                        Text("${data.hmd}%", modifier = Modifier.weight(1f), color = Color.Black)
                        Text("${data.temp}°C", modifier = Modifier.weight(1f), color = Color.Black)
                        Text(data.time, modifier = Modifier.weight(1f), color = Color.Black)
                    }
                    Divider(color = Color.Black.copy(alpha = 0.2f))
                }
            }
        } else if (error.isEmpty() && filteredData.isEmpty() && allSensorData.isNotEmpty()) {
            Text(
                text = "Nenhum dado correspondente ao filtro foi encontrado.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp),
                color = Color.Black
            )
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
