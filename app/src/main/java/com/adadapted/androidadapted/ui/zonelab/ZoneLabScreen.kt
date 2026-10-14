package com.adadapted.androidadapted.ui.zonelab

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adadapted.androidadapted.R
import com.adadapted.androidadapted.adadapted.AdAdaptedConfig
import com.adadapted.androidadapted.ui.components.ContentCard
import com.adadapted.androidadapted.ui.components.IconBadge
import com.adadapted.androidadapted.ui.components.ListRowSpacing
import com.adadapted.androidadapted.ui.components.MatteCard
import com.adadapted.androidadapted.ui.components.Pill
import com.adadapted.androidadapted.ui.components.SectionHeader
import com.adadapted.androidadapted.ui.theme.AccentColors
import com.adadapted.androidadapted.ui.theme.AndroidAdaptedTheme
import com.adadapted.androidadapted.ui.theme.appTopBarColors

enum class ZoneLabScenario(
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    val icon: ImageVector,
    val tint: Color,
) {
    SCROLLER(R.string.scenario_scroller, R.string.scenario_scroller_subtitle, Icons.Filled.SwapVert, AccentColors.Mint),
    OFF_SCREEN(R.string.scenario_off_screen, R.string.scenario_off_screen_subtitle, Icons.AutoMirrored.Filled.MenuBook, AccentColors.Seafoam),
    LIST_EVENTS(R.string.scenario_list_events, R.string.scenario_list_events_subtitle, Icons.AutoMirrored.Filled.ListAlt, AccentColors.Sky),
}

/** SDK test scenarios, kept out of the shopping flow, and the configuration the SDK started with. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneLabScreen(config: AdAdaptedConfig, onOpenScenario: (ZoneLabScenario) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val configRows = config.rows()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.tab_zone_lab)) },
                colors = appTopBarColors(),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(ListRowSpacing),
        ) {
            item { SectionHeader(stringResource(R.string.zone_lab_scenarios)) }
            items(ZoneLabScenario.entries) { scenario ->
                ScenarioCard(scenario, onClick = { onOpenScenario(scenario) })
            }

            item { SectionHeader(stringResource(R.string.zone_lab_configuration)) }
            items(configRows) { row ->
                ConfigRow(row)
            }
        }
    }
}

@Composable
private fun ScenarioCard(scenario: ZoneLabScenario, onClick: () -> Unit, modifier: Modifier = Modifier) {
    MatteCard(glow = scenario.tint, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = scenario.tint.copy(alpha = 0.2f), modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(scenario.icon, contentDescription = null, tint = scenario.tint, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(scenario.title), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = stringResource(scenario.subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalContentColor.current.copy(alpha = 0.7f),
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = scenario.tint,
            )
        }
    }
}

@Composable
private fun ConfigRow(row: ConfigRow) {
    ContentCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(row.icon, AccentColors.Jade, size = 36.dp)
            Spacer(Modifier.width(14.dp))
            Text(stringResource(row.label), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            when (val flag = row.flag) {
                null -> Text(row.value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> Pill(text = row.value, color = if (flag) AccentColors.Jade else MaterialTheme.colorScheme.outline)
            }
        }
    }
}

private data class ConfigRow(
    @StringRes val label: Int,
    val icon: ImageVector,
    val value: String,
    /** Set for on/off settings, which show as a colored pill. */
    val flag: Boolean? = null,
)

@Composable
private fun AdAdaptedConfig.rows(): List<ConfigRow> {
    val on = stringResource(R.string.on)
    val off = stringResource(R.string.off)
    fun flagRow(@StringRes label: Int, icon: ImageVector, isOn: Boolean) = ConfigRow(label, icon, if (isOn) on else off, isOn)
    return listOf(
        ConfigRow(R.string.config_app_id, Icons.Outlined.Key, appId),
        ConfigRow(R.string.config_environment, Icons.Outlined.Cloud, environmentName),
        ConfigRow(R.string.config_custom_identifier, Icons.Outlined.Badge, customIdentifier),
        flagRow(R.string.config_keyword_intercept, Icons.Outlined.Search, isKeywordInterceptEnabled),
        flagRow(R.string.config_payloads, Icons.Outlined.Inventory2, arePayloadsEnabled),
        flagRow(R.string.config_debug_logging, Icons.Outlined.BugReport, isDebugLoggingEnabled),
    )
}

@Preview
@Composable
private fun ZoneLabScreenPreview() {
    AndroidAdaptedTheme {
        ZoneLabScreen(config = AdAdaptedConfig.Default, onOpenScenario = {})
    }
}
