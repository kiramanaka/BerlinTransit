package com.berlintransit.app.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.berlintransit.app.AppContainer
import com.berlintransit.app.data.formatUpdatedTimestamp
import com.berlintransit.app.ui.components.DepartureRow
import com.berlintransit.app.ui.components.ErrorCard
import com.berlintransit.app.ui.components.SplitFlapText
import com.berlintransit.app.ui.theme.BoardSurface
import com.berlintransit.app.ui.theme.FlapAmber
import com.berlintransit.app.ui.theme.TextPrimary
import com.berlintransit.app.ui.theme.TextSecondary
import kotlinx.coroutines.delay

private const val REFRESH_INTERVAL_MILLIS = 45_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    container: AppContainer,
    stopId: String,
    stopName: String,
    onBack: () -> Unit
) {
    val viewModel: BoardViewModel = viewModel(key = "board_$stopId") {
        BoardViewModel(container, stopId, stopName)
    }
    val state by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message -> snackbarHostState.showSnackbar(message) }
    }
    LaunchedEffect(stopId) {
        while (true) {
            viewModel.refresh()
            delay(REFRESH_INTERVAL_MILLIS)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stopName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) FlapAmber else Color(0xFF54575E)
                        )
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BoardSurface,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BoardHeader(stopName = stopName)
            when {
                state.isLoading && state.departures.isEmpty() -> {
                    LoadingBoard()
                }
                state.error != null && state.departures.isEmpty() -> {
                    ErrorCard(
                        message = state.error ?: "",
                        onRetry = { viewModel.refresh() },
                        modifier = Modifier.padding(16.dp)
                    )
                }
                state.departures.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No departures in the next 60 minutes",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
                else -> {
                    DepartureList(state = state)
                }
            }
        }
    }
}

@Composable
private fun BoardHeader(stopName: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
    ) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val cells = ((maxWidth - 24.dp) / 19.dp).toInt().coerceAtLeast(8)
            SplitFlapText(
                text = stopName.uppercase().let { it.take(cells).padEnd(cells, ' ') },
                cellWidth = 19.dp,
                cellHeight = 27.dp,
                startDelayMillis = 40L,
                staggerPerCellMillis = 40L,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DEPARTURES",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.weight(1f))
            com.berlintransit.app.ui.components.ClockFlap()
        }
    }
}

@Composable
private fun LoadingBoard() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        CircularProgressIndicator(color = FlapAmber)
        Spacer(Modifier.height(16.dp))
        Text("Loading departures…", color = TextSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun DepartureList(state: BoardViewModel.BoardUiState) {
    LazyColumn(Modifier.fillMaxSize()) {
        itemsIndexed(state.departures, key = { _, departure -> departure.key }) { index, departure ->
            DepartureRow(departure = departure, index = index)
            if (index < state.departures.lastIndex) {
                HorizontalDivider(
                    color = Color(0xFF191B20),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
        item {
            Text(
                text = "Updated ${formatUpdatedTimestamp(state.updatedAtMs)} · refreshes every 45s",
                color = TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            )
        }
    }
}
