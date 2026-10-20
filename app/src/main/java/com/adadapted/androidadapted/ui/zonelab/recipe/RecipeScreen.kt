package com.adadapted.androidadapted.ui.zonelab.recipe

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutBoundsHolder
import androidx.compose.ui.layout.layoutBounds
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adadapted.androidadapted.R
import com.adadapted.androidadapted.adadapted.AdZoneCatalog
import com.adadapted.androidadapted.adadapted.AdZoneHandler
import com.adadapted.androidadapted.ui.components.BackButton
import com.adadapted.androidadapted.ui.components.ButtonContent
import com.adadapted.androidadapted.ui.components.ContentCard
import com.adadapted.androidadapted.ui.components.InstrumentedAdZone
import com.adadapted.androidadapted.ui.components.MatteCard
import com.adadapted.androidadapted.ui.components.PreviewAdZoneHandler
import com.adadapted.androidadapted.ui.components.SectionHeader
import com.adadapted.androidadapted.ui.theme.AccentColors
import com.adadapted.androidadapted.ui.theme.AndroidAdaptedTheme
import com.adadapted.androidadapted.ui.theme.appTopBarColors

@Composable
fun RecipeScreen(viewModel: RecipeViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RecipeScreen(
        uiState = uiState,
        zoneHandler = viewModel.zoneHandler,
        onContextChange = viewModel::setContextId,
        onAddIngredients = viewModel::addIngredientsToList,
        onMessageShown = viewModel::onMessageShown,
        onBack = onBack,
    )
}

/**
 * A recipe with a zone above and below it. The page is long enough that the bottom zone starts off
 * screen, so each zone's visibility can be watched as the page scrolls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecipeScreen(
    uiState: RecipeUiState,
    zoneHandler: AdZoneHandler,
    onContextChange: (String) -> Unit,
    onAddIngredients: () -> Unit,
    onMessageShown: (RecipeMessage) -> Unit,
    onBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val viewport = remember { LayoutBoundsHolder() }

    uiState.message?.let { message ->
        val text = message.text()
        LaunchedEffect(message) {
            // Also clears it when the screen goes away mid-snackbar, so it doesn't show again on return.
            try {
                snackbarHostState.showSnackbar(text)
            } finally {
                onMessageShown(message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = appTopBarColors(),
                title = { Text(stringResource(R.string.scenario_off_screen)) },
                navigationIcon = { BackButton(onBack) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ContextPicker(
                selected = uiState.contextId,
                onSelect = onContextChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            HorizontalDivider()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .layoutBounds(viewport)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                InstrumentedAdZone(
                    title = stringResource(R.string.recipe_top_zone),
                    zoneId = AdZoneCatalog.OFF_SCREEN_TOP,
                    handler = zoneHandler,
                    viewport = viewport,
                    contextId = uiState.contextId,
                )

                RecipeHeader(uiState.recipe)
                Ingredients(uiState.recipe.ingredients, onAddIngredients)
                Steps(uiState.recipe.steps)

                InstrumentedAdZone(
                    title = stringResource(R.string.recipe_bottom_zone),
                    zoneId = AdZoneCatalog.OFF_SCREEN_BOTTOM,
                    handler = zoneHandler,
                    viewport = viewport,
                    contextId = uiState.contextId,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContextPicker(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val choices = RecipeViewModel.ContextChoices
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        choices.forEachIndexed { index, choice ->
            SegmentedButton(
                selected = choice == selected,
                onClick = { onSelect(choice) },
                shape = SegmentedButtonDefaults.itemShape(index, choices.size),
                label = {
                    Text(
                        if (choice.isEmpty()) {
                            stringResource(R.string.recipe_no_context)
                        } else {
                            stringResource(R.string.recipe_context_named, choice)
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun RecipeHeader(recipe: Recipe) {
    MatteCard(modifier = Modifier.fillMaxWidth()) {
        Icon(
            Icons.Outlined.RestaurantMenu,
            contentDescription = null,
            tint = AccentColors.Mint.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(80.dp),
        )
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(end = 72.dp)
                    .semantics { heading() },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroChip(Icons.Outlined.Group, stringResource(R.string.recipe_serves, recipe.servings))
                HeroChip(Icons.Outlined.Timer, stringResource(R.string.recipe_minutes, recipe.minutes))
            }
        }
    }
}

@Composable
private fun HeroChip(icon: ImageVector, text: String) {
    Surface(shape = CircleShape, color = AccentColors.Mint.copy(alpha = 0.18f)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = null, tint = AccentColors.Mint, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = AccentColors.Mint)
        }
    }
}

@Composable
private fun Ingredients(ingredients: List<String>, onAddIngredients: () -> Unit) {
    ContentCard(shape = MaterialTheme.shapes.extraLarge) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // A card's own title, not a section of the page, so it isn't a SectionHeader.
            Text(
                text = stringResource(R.string.recipe_ingredients),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ingredients.forEachIndexed { index, ingredient ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .background(AccentColors.cycle(index), CircleShape),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(ingredient, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            Button(onClick = onAddIngredients, modifier = Modifier.fillMaxWidth()) {
                ButtonContent(Icons.Outlined.AddShoppingCart, stringResource(R.string.recipe_add_ingredients))
            }
        }
    }
}

@Composable
private fun Steps(steps: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(stringResource(R.string.recipe_steps), contentPadding = PaddingValues())
        steps.forEachIndexed { index, step -> StepCard(number = index + 1, text = step) }
    }
}

@Composable
private fun StepCard(number: Int, text: String) {
    ContentCard(shape = MaterialTheme.shapes.large) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(number.toString(), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RecipeMessage.text(): String = when (this) {
    is RecipeMessage.IngredientsAdded -> pluralStringResource(R.plurals.recipe_ingredients_added, count, count)
    is RecipeMessage.NoList -> stringResource(R.string.recipe_no_list)
}

@Preview
@Composable
private fun RecipeScreenPreview() {
    AndroidAdaptedTheme {
        RecipeScreen(
            uiState = RecipeUiState(),
            zoneHandler = PreviewAdZoneHandler,
            onContextChange = {},
            onAddIngredients = {},
            onMessageShown = {},
            onBack = {},
        )
    }
}
