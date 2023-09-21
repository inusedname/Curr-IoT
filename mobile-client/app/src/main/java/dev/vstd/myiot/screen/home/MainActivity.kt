package dev.vstd.myiot.screen.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import dev.vstd.myiot.R
import dev.vstd.myiot.ui.theme.MyIOTTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            body()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun body() {
        val pagerState = rememberPagerState()
        val scope = rememberCoroutineScope()
        MyIOTTheme {
            Scaffold { paddingValues ->
                Column(Modifier.padding(paddingValues)) {
                    TabRow(selectedTabIndex = pagerState.currentPage) {
                        arrayOf("Log", "Dashboard").forEachIndexed { index, s ->
                            Tab(selected = pagerState.currentPage == index, onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }, text = {
                                Text(text = s)
                            })
                        }
                    }
                    HorizontalPager(pageCount = 2, state = pagerState) {
                        Column(Modifier.weight(1f)) {
                            when (it) {
                                0 -> log_()
                                1 -> dashboard_()
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun log_() {
        val vimel by viewModels<MainVimel>()
        val messages by vimel.rawMessage.collectAsState()
        val state = rememberLazyListState()
        val scope = rememberCoroutineScope()
        LaunchedEffect(key1 = messages) {
            scope.launch {
                if (messages.isNotEmpty()) {
                    state.animateScrollToItem(messages.size - 1)
                }
            }
        }
        LazyColumn(
            state = state,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages) {
                Row(verticalAlignment = Alignment.Top) {
                    when (it.first) {
                        MainVimel.Sender.FIREBASE -> {
                            Image(
                                painter = painterResource(id = R.drawable.firebase),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            BaseCard(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Blue.copy(
                                        alpha = 0.5f
                                    )
                                )
                            ) {
                                Text(text = it.second)
                            }
                        }

                        MainVimel.Sender.USER -> {
                            Image(
                                painter = painterResource(id = R.drawable.user),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            BaseCard(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Magenta.copy(
                                        alpha = 0.5f
                                    )
                                )
                            ) {
                                Text(text = it.second)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun dashboard_() {
        val vimel by viewModels<MainVimel>()
        val state by vimel.state.collectAsState()
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                _float_block(title = "Temperature", value = state.temperature)
            }
            item {
                _float_block(title = "Humidity", value = state.humidity)
            }
            item(span = { GridItemSpan(2) }) {
                _switch_block(title = "LED", value = state.ledOn) {
                    vimel.toggleLed(it)
                }
            }
        }
    }
}

@Composable
private fun _float_block(title: String = "temp", value: Float = 27f, decimal: Int = 1) {
    BaseCard {
        Text(title)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(String.format("%.${decimal}f", value), fontSize = 32.sp)
            Text(text = "°C", fontSize = 12.sp)
        }
    }
}

@Composable
private fun _switch_block(
    title: String = "led",
    value: Boolean = true,
    onSwitch: (Boolean) -> Unit = {}
) {
    BaseCard {
        Text(title)
        Switch(checked = value, onCheckedChange = onSwitch)
    }
}

@Composable
fun BaseCard(
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(8.dp), content = content)
    }
}