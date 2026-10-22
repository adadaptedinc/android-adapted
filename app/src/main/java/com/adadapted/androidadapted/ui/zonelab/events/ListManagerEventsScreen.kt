package com.adadapted.androidadapted.ui.zonelab.events

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adadapted.androidadapted.R
import com.adadapted.androidadapted.ui.components.BackButton
import com.adadapted.androidadapted.ui.components.ButtonContent
import com.adadapted.androidadapted.ui.components.SectionHeader
import com.adadapted.androidadapted.ui.components.wash
import com.adadapted.androidadapted.ui.theme.AccentColors
import com.adadapted.androidadapted.ui.theme.AndroidAdaptedTheme
import com.adadapted.androidadapted.ui.theme.appTopBarColors
import com.adadapted.androidadapted.ui.theme.forText

@Composable
fun ListManagerEventsScreen(viewModel: ListManagerEventsViewModel, onBack: () -> Unit) {
    ListManagerEventsScreen(
        itemName = viewModel.itemName,
        listName = viewModel.listName,
        canFire = viewModel.canFire,
        lastFired = viewModel.lastFired,
        onItemNameChange = viewModel::onItemNameChange,
        onListNameChange = viewModel::onListNameChange,
        onFire = viewModel::fire,
        onFireSequence = viewModel::fireFullSequence,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListManagerEventsScreen(
    itemName: String,
    listName: String,
    canFire: Boolean,
    lastFired: String?,
    onItemNameChange: (String) -> Unit,
    onListNameChange: (String) -> Unit,
    onFire: (ListEventKind) -> Unit,
    onFireSequence: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = appTopBarColors(),
                title = { Text(stringResource(R.string.scenario_list_events)) },
                navigationIcon = { BackButton(onBack) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionHeader(stringResource(R.string.events_payload), contentPadding = SectionTitlePadding)
            OutlinedTextField(
                value = itemName,
                onValueChange = onItemNameChange,
                label = { Text(stringResource(R.string.events_item_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = listName,
                onValueChange = onListNameChange,
                label = { Text(stringResource(R.string.events_list_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )

            SectionHeader(stringResource(R.string.events_fire), contentPadding = SectionTitlePadding)
            ListEventKind.entries.forEach { kind ->
                FilledTonalButton(
                    onClick = { onFire(kind) },
                    enabled = canFire,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = kind.color.wash(),
                        contentColor = kind.color.forText(),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    ButtonContent(kind.icon, stringResource(kind.label))
                }
            }

            Button(
                onClick = onFireSequence,
                enabled = canFire,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                ButtonContent(Icons.Outlined.Bolt, stringResource(R.string.events_fire_sequence))
            }
            Text(
                text = stringResource(R.string.events_fire_sequence_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (lastFired != null) {
                SectionHeader(stringResource(R.string.events_last), contentPadding = SectionTitlePadding)
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(lastFired, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(20.dp))
                }
            }
        }
    }
}

/** Headers here sit in an already-padded form, so they only need room above. */
private val SectionTitlePadding = PaddingValues(top = 8.dp)

@get:StringRes
private val ListEventKind.label: Int
    get() = when (this) {
        ListEventKind.ADDED -> R.string.events_item_added
        ListEventKind.CROSSED_OFF -> R.string.events_item_crossed_off
        ListEventKind.DELETED -> R.string.events_item_deleted
    }

private val ListEventKind.color: Color
    get() = when (this) {
        ListEventKind.ADDED -> AccentColors.Jade
        ListEventKind.CROSSED_OFF -> AccentColors.Sky
        ListEventKind.DELETED -> AccentColors.Red
    }

private val ListEventKind.icon: ImageVector
    get() = when (this) {
        ListEventKind.ADDED -> Icons.Outlined.AddCircleOutline
        ListEventKind.CROSSED_OFF -> Icons.Outlined.CheckCircleOutline
        ListEventKind.DELETED -> Icons.Outlined.RemoveCircleOutline
    }

@Preview
@Composable
private fun ListManagerEventsScreenPreview() {
    AndroidAdaptedTheme {
        ListManagerEventsScreen(
            itemName = "Test Milk",
            listName = "Test List",
            canFire = true,
            lastFired = "Manual event: Added \"Test Milk\" — Test List",
            onItemNameChange = {},
            onListNameChange = {},
            onFire = {},
            onFireSequence = {},
            onBack = {},
        )
    }
}
