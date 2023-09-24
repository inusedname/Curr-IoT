package dev.vstd.myiot.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun dashboard_(vimel: MainVimel) {
    val state by vimel.uiState.collectAsState()
    val context = LocalContext.current
    val chart = remember { TempHumidChart(context) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            _float_block(title = "Temperature", unit = "oC", value = state.temperature)
        }
        item {
            _float_block(title = "Humidity", unit = "%", value = state.humidity)
        }
        item {
            _float_block(title = "Lightness", unit = "lux", value = state.lux)
        }
        item {
            _switch_block(title = "LED", value = state.ledOn) {
                vimel.toggleLed()
            }
        }
        item(span = { GridItemSpan(2) }) {
            BaseCard {
                Text(text = "Realtime Temp & Humid", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                AndroidView(
                    factory = { chart.view },
                    update = { chart.addData(state.humidity, state.temperature) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }
        }
    }
}

@Composable
private fun _float_block(
    title: String = "temp",
    unit: String,
    value: Float = 27f,
    decimal: Int = 1
) {
    BaseCard {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(String.format("%.${decimal}f", value), fontSize = 56.sp)
            Text(text = unit, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun _switch_block(
    title: String,
    value: Boolean,
    onSwitch: () -> Unit
) {
    BaseCard(onSwitch) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = value, onCheckedChange = { onSwitch() }, modifier = Modifier.width(64.dp))
        }
    }
}