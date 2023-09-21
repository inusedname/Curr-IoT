package dev.vstd.myiot.screen.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        val vimel by viewModels<MainVimel>()
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
                                0 -> log_(vimel)
                                1 -> dashboard_(vimel)
                            }
                        }
                    }
                }
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseCard(
    onClick: () -> Unit,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(onClick = onClick) {
        Column(modifier = Modifier.padding(8.dp), content = content)
    }
}