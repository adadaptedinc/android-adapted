package com.adadapted.androidadapted.ui.lists

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.adadapted.androidadapted.R
import com.adadapted.androidadapted.data.model.GroceryItem
import com.adadapted.androidadapted.ui.components.FormSheet

@Composable
internal fun ItemEditorSheet(
    item: GroceryItem,
    onSave: (GroceryItem) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable(item.id) { mutableStateOf(item.name) }
    var quantity by rememberSaveable(item.id) { mutableIntStateOf(item.quantity) }
    var isCrossedOff by rememberSaveable(item.id) { mutableStateOf(item.isCrossedOff) }

    FormSheet(title = stringResource(R.string.edit_item), onDismiss = onDismiss) { hideThen ->
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.edit_item_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            modifier = Modifier.fillMaxWidth(),
        )

        QuantityStepper(quantity = quantity, onQuantityChange = { quantity = it })

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = isCrossedOff, role = Role.Switch, onValueChange = { isCrossedOff = it }),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.edit_item_crossed_off), modifier = Modifier.weight(1f))
            Switch(checked = isCrossedOff, onCheckedChange = null)
        }

        HorizontalDivider()

        item.origin.badge?.let { badge ->
            DetailRow(stringResource(R.string.edit_item_added_by), stringResource(badge.label))
        }
        DetailRow(stringResource(R.string.edit_item_added), relativeTime(item.addedAtMillis))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = { hideThen(onDelete) }) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { hideThen(onDismiss) }) { Text(stringResource(R.string.cancel)) }
            Button(
                onClick = {
                    val edited = item.copy(name = name.trim(), quantity = quantity, isCrossedOff = isCrossedOff)
                    hideThen { onSave(edited) }
                },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
private fun QuantityStepper(quantity: Int, onQuantityChange: (Int) -> Unit) {
    val range = GroceryItem.QuantityRange
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.edit_item_quantity), modifier = Modifier.weight(1f))
        OutlinedIconButton(onClick = { onQuantityChange(quantity - 1) }, enabled = quantity > range.first) {
            Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.quantity_decrease))
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 40.dp),
        )
        OutlinedIconButton(onClick = { onQuantityChange(quantity + 1) }, enabled = quantity < range.last) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.quantity_increase))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}

@Composable
private fun relativeTime(millis: Long): String {
    val now = System.currentTimeMillis()
    return if (now - millis < DateUtils.MINUTE_IN_MILLIS) {
        stringResource(R.string.just_now)
    } else {
        DateUtils.getRelativeTimeSpanString(millis, now, DateUtils.MINUTE_IN_MILLIS).toString()
    }
}
