package dev.vstd.myiot.screen.analytic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.vstd.myiot.data.Singleton
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun led_history_(navigator: DestinationsNavigator) {
    val vimel: LedHistoryVimel = viewModel()
    val activity = LocalContext.current as FragmentActivity

    val uiData by vimel.uiData.collectAsStateWithLifecycle()

    var showFilterDialog by remember { mutableStateOf(false) }
    var initTimes by remember {
        mutableStateOf(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -7)
        }.timeInMillis to System.currentTimeMillis())
    }
    var initWhichLed by remember { mutableStateOf<Int?>(null) }
    var initOnLed by remember { mutableStateOf<Boolean?>(null)}

    LaunchedEffect(true) {
        vimel.setData(Singleton.rawLedHistory)
    }

    Scaffold(topBar = {
        TopAppBar(navigationIcon = {
            IconButton(onClick = { navigator.popBackStack() }) {
                Icon(Icons.Rounded.ArrowBack, null)
            }
        }, title = { Text(text = "Led History") }, actions = {
            IconButton(onClick = vimel::sortByTime) {
                Icon(imageVector = Icons.Rounded.Sort, contentDescription = null)
            }
            IconButton(onClick = { showFilterDialog = true }) {
                Icon(imageVector = Icons.Rounded.FilterAlt, contentDescription = null)
            }
        })
    }) {
        Column(Modifier.padding(it)) {
            LazyColumn {
                items(uiData) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = it.first.toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (it.third) "ON" else "OFF",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (it.third) Color.Green else Color.Red,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = it.second.toDDMMYYYYHHMM())
                    }
                    Divider()
                }
            }
        }
    }
    if (showFilterDialog) {
        _filter_dialog(activity, initTimes, initWhichLed, initOnLed, onDismiss = {
            showFilterDialog = false
        }) { timeRange, which, on ->
            initTimes = timeRange
            initWhichLed = which
            initOnLed = on
            vimel.filter(timeRange, which, on)
            showFilterDialog = false
        }
    }
}

@Composable
private fun _filter_dialog(
    activity: FragmentActivity,
    initTimes: Pair<Long, Long>,
    initWhichLed: Int?,
    initOnLed: Boolean?,
    onDismiss: () -> Unit,
    onComplete: (Pair<Long, Long>, Int?, Boolean?) -> Unit
) {
    var initTimes by remember { mutableStateOf(initTimes) }
    var initWhichLed by remember { mutableStateOf(initWhichLed) }
    var initOnLed by remember { mutableStateOf(initOnLed)}
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .clip(RoundedCornerShape(16))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Filter Dialog", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Time Range", style = MaterialTheme.typography.labelMedium)
                _time_button(activity = activity, init = initTimes, onDone = { initTimes = it })
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(text = "Which", style = MaterialTheme.typography.labelMedium)
                Checkbox(checked = initWhichLed == null || initWhichLed == 1, onCheckedChange = {
                    initWhichLed = if (initWhichLed == 2) null else 2
                })
                Text(text = "LED1")
                Spacer(modifier = Modifier.width(16.dp))
                Checkbox(checked = initWhichLed == null || initWhichLed == 2, onCheckedChange = {
                    initWhichLed = if (initWhichLed == 1) null else 1
                })
                Text(text = "LED2")
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(text = "On/Off", style = MaterialTheme.typography.labelMedium)
                Checkbox(checked = initOnLed == null || initOnLed == true, onCheckedChange = {
                    initOnLed = if (initOnLed == false) null else false
                })
                Text(text = "ON")
                Spacer(modifier = Modifier.width(16.dp))
                Checkbox(checked = initOnLed == null || initOnLed == false, onCheckedChange = {
                    initOnLed = if (initOnLed == true) null else true
                })
                Text(text = "OFF")
            }
            Button(
                onClick = {
                    onComplete(initTimes, initWhichLed, initOnLed)
                }, modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 16.dp)
            ) {
                Text(text = "Filter")
            }
        }
    }
}