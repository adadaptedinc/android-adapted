package com.adadapted.androidadapted.ui.lists

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adadapted.androidadapted.R
import com.adadapted.androidadapted.adadapted.AdZoneCatalog
import com.adadapted.androidadapted.data.ListZoneRule
import com.adadapted.androidadapted.data.model.ListIcon
import com.adadapted.androidadapted.ui.components.FormSheet
import com.adadapted.androidadapted.ui.components.wash
import com.adadapted.androidadapted.ui.theme.forText
import com.adadapted.androidadapted.ui.theme.onAccent

@Composable
internal fun NewListSheet(onDismiss: () -> Unit, onAdd: (NewListDraft) -> Unit) {
    val nameFocus = remember { FocusRequester() }

    var name by rememberSaveable { mutableStateOf("") }
    var icon by rememberSaveable { mutableStateOf(ListIcon.CART) }
    var zoneId by rememberSaveable { mutableStateOf(AdZoneCatalog.LIST) }
    val draft = NewListDraft(name = name, icon = icon, zoneId = zoneId)

    FormSheet(title = stringResource(R.string.new_list), onDismiss = onDismiss) { hideThen ->
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.new_list_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(nameFocus),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.new_list_icon), style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ListIcon.entries.forEach { option ->
                    FilledIconToggleButton(
                        checked = option == icon,
                        onCheckedChange = { icon = option },
                        colors = IconButtonDefaults.filledIconToggleButtonColors(
                            containerColor = option.accent.wash(),
                            contentColor = option.accent.forText(),
                            checkedContainerColor = option.accent.forText(),
                            checkedContentColor = onAccent(),
                        ),
                    ) {
                        Icon(option.imageVector, contentDescription = stringResource(option.label))
                    }
                }
            }
        }

        val zoneIdProblem = draft.zoneIdProblem
        OutlinedTextField(
            value = zoneId,
            onValueChange = { zoneId = it.filter(Char::isDigit) },
            label = { Text(stringResource(R.string.new_list_zone_id)) },
            singleLine = true,
            isError = zoneIdProblem == ListZoneRule.Problem.RESERVED,
            supportingText = {
                Text(
                    stringResource(
                        when (zoneIdProblem) {
                            ListZoneRule.Problem.RESERVED -> R.string.new_list_zone_used_by_zone_lab
                            ListZoneRule.Problem.BLANK, null -> R.string.new_list_zone_help
                        },
                    ),
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (draft.canSubmit) hideThen { onAdd(draft) } }),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            TextButton(onClick = { hideThen(onDismiss) }) { Text(stringResource(R.string.cancel)) }
            Button(onClick = { hideThen { onAdd(draft) } }, enabled = draft.canSubmit) {
                Text(stringResource(R.string.add))
            }
        }
    }

    LaunchedEffect(Unit) { nameFocus.requestFocus() }
}

@get:StringRes
private val ListIcon.label: Int
    get() = when (this) {
        ListIcon.CART -> R.string.list_icon_cart
        ListIcon.PRODUCE -> R.string.list_icon_produce
        ListIcon.HOUSEHOLD -> R.string.list_icon_household
        ListIcon.ELECTRONICS -> R.string.list_icon_electronics
        ListIcon.PARTY -> R.string.list_icon_party
        ListIcon.TAKEOUT -> R.string.list_icon_takeout
    }
