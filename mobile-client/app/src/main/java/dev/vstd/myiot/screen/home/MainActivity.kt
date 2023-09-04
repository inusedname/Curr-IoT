package dev.vstd.myiot.screen.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
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
                            }) {
                                Text(text = s)
                            }
                        }
                    }
                    HorizontalPager(pageCount = 2, state = pagerState) {
                        when (it) {
                            0 -> log_()
                            1 -> dashboard_()
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
        LazyColumn(reverseLayout = true) {
            items(messages) {
                _message(it)
            }
        }
    }

    @Composable
    private fun _message(content: String) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp)
        ) {
            Text(content)
        }
    }
}

@Composable
private fun dashboard_() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            _float_block(title = "Temperature", value = 27f)
        }
        item {
            _switch_block()
        }
    }
}

@Composable
private fun _float_block(title: String = "temp", value: Float = 27f, decimal: Int = 1) {
    Column(
        Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Text(title)
        Row {
            Text(String.format("%.${decimal}f", value), fontSize = 24.sp)
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
    Column(
        Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Text(title)
        Switch(checked = value, onCheckedChange = onSwitch)
    }
}