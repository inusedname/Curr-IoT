package dev.vstd.myiot

import android.os.Bundle
import androidx.activity.compose.setContent
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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.vstd.myiot.screen.home.MainVimel
import dev.vstd.myiot.screen.home.dashboard_
import dev.vstd.myiot.screen.log.log_
import dev.vstd.myiot.ui.theme.MyIOTTheme
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        // Plant Timber
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                super.log(priority, tag, "$tag >> $message", t)
            }
        })

        setContent {
            DestinationsNavHost(navGraph = NavGraphs.root)
        }
    }
}

@Destination
@RootNavGraph(start = true)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun body(navigator: DestinationsNavigator) {
    val vimel: MainVimel = viewModel()
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    MyIOTTheme {
        Scaffold { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    arrayOf("Dashboard", "Log").forEachIndexed { index, s ->
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
                            0 -> dashboard_(vimel, navigator)
                            1 -> log_(vimel)
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(8.dp), content = content)
    }
}