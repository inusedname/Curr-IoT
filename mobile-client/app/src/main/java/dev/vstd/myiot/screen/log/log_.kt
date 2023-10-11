package dev.vstd.myiot.screen.log

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.vstd.myiot.R
import dev.vstd.myiot.BaseCard
import dev.vstd.myiot.screen.home.MainVimel
import kotlinx.coroutines.launch

@Composable
fun log_(vimel: MainVimel) {
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
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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