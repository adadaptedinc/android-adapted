package com.adadapted.androidadapted.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Titles a group of rows, optionally with a count beside it. By default it sits in a list of rows,
 * inset to line up with their content and set apart from the section above by [SectionSpacing].
 */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    contentPadding: PaddingValues = PaddingValues(start = 12.dp, end = 12.dp, top = SectionSpacing),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding)
            .semantics(mergeDescendants = true) { heading() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
        if (count != null) {
            Spacer(Modifier.width(8.dp))
            Pill(text = count.toString(), color = MaterialTheme.colorScheme.secondary)
        }
    }
}
