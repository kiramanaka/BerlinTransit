package com.berlintransit.app.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.berlintransit.app.AppContainer
import com.berlintransit.app.data.cleanStopName
import com.berlintransit.app.ui.components.ClockFlap
import com.berlintransit.app.ui.components.EmptyHintCard
import com.berlintransit.app.ui.components.ErrorCard
import com.berlintransit.app.ui.components.FavoriteRow
import com.berlintransit.app.ui.components.NearbyStationRow
import com.berlintransit.app.ui.components.PermissionCard
import com.berlintransit.app.ui.components.SectionHeader
import com.berlintransit.app.ui.components.SplitFlapText
import com.berlintransit.app.ui.theme.BoardSurfaceVariant
import com.berlintransit.app.ui.theme.FlapAmber
import com.berlintransit.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    container: AppContainer,
    onOpenStation: (id: String, name: String) -> Unit,
    onOpenSearch: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel { HomeViewModel(container) }
    val state by viewModel.uiState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants -> viewModel.onPermissionResult(grants.values.any { it }) }

    LaunchedEffect(Unit) { viewModel.checkPermissionAndStart() }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.checkPermissionAndStart()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SplitFlapText(
                    text = "BERLIN TRANSIT",
                    cellWidth = 19.dp,
                    cellHeight = 27.dp
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEPARTURES · VBB",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.weight(1f))
                ClockFlap()
            }
            Spacer(Modifier.height(20.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BoardSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable(onClick = onOpenSearch)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Search station…",
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            SectionHeader(
                "NEARBY",
                Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(10.dp))
            NearbySection(
                state = state,
                onRequestPermission = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onOpenSettings = {
                    runCatching {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                        )
                    }
                },
                onRetry = viewModel::retry,
                onOpenStation = onOpenStation
            )

            Spacer(Modifier.height(28.dp))
            SectionHeader(
                "FAVORITES (${favorites.size}/3)",
                Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))
            if (favorites.isEmpty()) {
                EmptyHintCard(
                    "Open a station and tap the star in its board to pin it here. Up to 3 stations.",
                    Modifier.padding(horizontal = 16.dp)
                )
            } else {
                favorites.forEach { entry ->
                    FavoriteRow(
                        entry = entry,
                        onClick = { onOpenStation(entry.id, cleanStopName(entry.name)) },
                        onRemove = { viewModel.removeFavorite(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbySection(
    state: HomeViewModel.HomeUiState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    onOpenStation: (id: String, name: String) -> Unit
) {
    when (val nearby = state.nearby) {
        NearbyState.Idle, NearbyState.NeedsPermission -> {
            PermissionCard(
                onRequest = onRequestPermission,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        NearbyState.Locating -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = FlapAmber,
                    strokeWidth = 2.dp,
                    modifier = Modifier.width(18.dp).height(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text("Finding your position…", color = TextSecondary, fontSize = 13.sp)
            }
        }
        NearbyState.Unavailable -> {
            Column(Modifier.padding(horizontal = 16.dp)) {
                EmptyHintCard("Location unavailable. Enable location services, then retry.")
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BoardSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onRetry)
                ) {
                    Text(
                        "Retry",
                        color = FlapAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
        is NearbyState.Error -> {
            ErrorCard(
                message = nearby.message,
                onRetry = onRetry,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        is NearbyState.Ready -> {
            if (nearby.stations.isEmpty()) {
                EmptyHintCard(
                    "No stations found within 2 km.",
                    Modifier.padding(horizontal = 16.dp)
                )
            } else {
                nearby.stations.forEachIndexed { index, station ->
                    NearbyStationRow(
                        station = station,
                        nearest = index == 0,
                        onClick = { onOpenStation(station.id, station.displayName) }
                    )
                }
            }
        }
    }
}
