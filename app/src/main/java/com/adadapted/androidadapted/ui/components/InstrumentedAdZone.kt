package com.adadapted.androidadapted.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LayoutBoundsHolder
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adadapted.androidadapted.R
import com.adadapted.androidadapted.adadapted.AdZone
import com.adadapted.androidadapted.adadapted.AdZoneCatalog
import com.adadapted.androidadapted.adadapted.AdZoneHandler
import com.adadapted.androidadapted.ui.theme.AccentColors
import com.adadapted.androidadapted.ui.theme.AndroidAdaptedTheme

/**
 * An [AdZone] in a card labeled with its zone ID, size, and whether it's on screen. The zone counts
 * as on screen while at least half of the card is inside [viewport].
 */
@Composable
fun InstrumentedAdZone(
    title: String,
    zoneId: String,
    handler: AdZoneHandler,
    viewport: LayoutBoundsHolder,
    modifier: Modifier = Modifier,
    contextId: String = "",
) {
    // Not saved: a recreated zone is a new view, so it starts hidden until it's measured on screen.
    var isVisible by remember { mutableStateOf(false) }
    ContentCard(
        shape = MaterialTheme.shapes.large,
        modifier = modifier
            .fillMaxWidth()
            .onVisibilityChanged(minFractionVisible = VISIBLE_FRACTION, viewportBounds = viewport) { isVisible = it },
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Outlined.Campaign, MaterialTheme.colorScheme.tertiary, size = 32.dp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = stringResource(R.string.zone_caption, zoneId, AdZoneCatalog.Height.value.toInt()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Pill(
                    text = stringResource(if (isVisible) R.string.zone_on_screen else R.string.zone_off_screen),
                    color = if (isVisible) AccentColors.Jade else MaterialTheme.colorScheme.outline,
                    icon = if (isVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                )
            }

            AdZone(
                zoneId = zoneId,
                handler = handler,
                isVisible = isVisible,
                contextId = contextId,
                modifier = Modifier.clip(MaterialTheme.shapes.small),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InstrumentedAdZonePreview() {
    AndroidAdaptedTheme {
        InstrumentedAdZone(
            title = "Top zone",
            zoneId = AdZoneCatalog.OFF_SCREEN_TOP,
            handler = PreviewAdZoneHandler,
            viewport = remember { LayoutBoundsHolder() },
        )
    }
}

private const val VISIBLE_FRACTION = 0.5f
