package com.adadapted.androidadapted.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.adadapted.android.sdk.core.view.AaZoneView
import com.adadapted.androidadapted.ui.theme.AAPurpleDark
import com.adadapted.androidadapted.ui.theme.AAPurpleMid
import com.adadapted.androidadapted.ui.theme.AATealLight
import com.adadapted.androidadapted.ui.theme.ScrollerBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollerScreen(onBack: () -> Unit) {
    val totalItems = 24
    val zoneViews = remember { mutableStateMapOf<Int, AaZoneView>() }
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.map { it.index }.toSet()
        }.collect { visibleIndices ->
            zoneViews.forEach { (index, zoneView) ->
                zoneView.setAdZoneVisibility(index in visibleIndices)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            zoneViews.values.forEach { it.onStop() }
            zoneViews.clear()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Infinite Scroller", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AAPurpleDark
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AAPurpleDark, AAPurpleMid)
                    )
                )
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            items(totalItems) { index ->
                val isAdCell = index % 6 == 0

                if (isAdCell) {
                    AndroidView(
                        factory = { ctx ->
                            AaZoneView(ctx).apply {
                                init("102110")
                                onStart()
                                setAdZoneVisibility(true)
                                zoneViews[index] = this
                            }
                        },
                        update = { view ->
                            val isVisible = listState.layoutInfo.visibleItemsInfo.any { it.index == index }
                            view.setAdZoneVisibility(isVisible)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ScrollerBlue.copy(alpha = 0.15f)),
                        onReset = null,
                        onRelease = { view ->
                            view.setAdZoneVisibility(false)
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AATealLight.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Content Placeholder $index",
                            fontSize = 16.sp,
                            color = AATealLight.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
