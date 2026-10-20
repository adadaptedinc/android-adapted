package com.adadapted.androidadapted.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * A modal sheet holding a short form under [title]. [content] gets `hideThen`, which animates the
 * sheet away before handing control back, for the form's own buttons. Only the first call counts,
 * so a double tap can't submit the form twice.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.(hideThen: (() -> Unit) -> Unit) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var isHiding by remember { mutableStateOf(false) }
    val hideThen: (() -> Unit) -> Unit = { action ->
        if (!isHiding) {
            isHiding = true
            scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content(hideThen)
        }
    }
}
