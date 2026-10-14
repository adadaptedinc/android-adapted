package com.adadapted.androidadapted.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.adadapted.androidadapted.R

@Composable
fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
    }
}

/** A ⋮ button that opens a menu of [OverflowMenuScope.MenuItem]s. */
@Composable
fun OverflowMenu(contentDescription: String, items: @Composable OverflowMenuScope.() -> Unit) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(onClick = { isExpanded = true }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = contentDescription)
        }
        DropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            OverflowMenuScope(this) { isExpanded = false }.items()
        }
    }
}

class OverflowMenuScope internal constructor(
    columnScope: ColumnScope,
    private val dismiss: () -> Unit,
) : ColumnScope by columnScope {

    /** A menu row that closes the menu before running [onClick]. Destructive rows are tinted. */
    @Composable
    fun MenuItem(
        text: String,
        icon: ImageVector,
        onClick: () -> Unit,
        enabled: Boolean = true,
        isDestructive: Boolean = false,
        trailingIcon: (@Composable () -> Unit)? = null,
    ) {
        val destructive = MaterialTheme.colorScheme.error
        DropdownMenuItem(
            text = { Text(text) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            trailingIcon = trailingIcon,
            colors = if (isDestructive) {
                MenuDefaults.itemColors(textColor = destructive, leadingIconColor = destructive)
            } else {
                MenuDefaults.itemColors()
            },
            enabled = enabled,
            onClick = {
                dismiss()
                onClick()
            },
        )
    }
}
