package com.adadapted.androidadapted.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.adadapted.android.sdk.AdAdaptedListManager
import com.adadapted.android.sdk.core.ad.AdContentListener
import com.adadapted.android.sdk.core.atl.AddToListContent
import com.adadapted.android.sdk.core.keyword.KeywordInterceptMatcher
import com.adadapted.android.sdk.core.keyword.Suggestion
import com.adadapted.android.sdk.core.view.AaZoneView
import com.adadapted.androidadapted.ui.theme.AAPurpleDark
import com.adadapted.androidadapted.ui.theme.AAPurpleMid
import com.adadapted.androidadapted.ui.theme.AATealLight
import com.adadapted.androidadapted.ui.theme.GroceryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(onBack: () -> Unit) {
    val groceryItems = remember { mutableStateListOf("Milk", "Eggs", "Cheese", "Bread", "Coffee") }
    var inputText by remember { mutableStateOf("") }
    var currentSuggestions by remember { mutableStateOf<List<Suggestion>>(emptyList()) }
    var zoneView by remember { mutableStateOf<AaZoneView?>(null) }
    var useOriginalContext by remember { mutableStateOf(false) }

    val contentListener = remember {
        object : AdContentListener {
            override fun onContentAvailable(zoneId: String, content: AddToListContent) {
                val items = content.getItems()
                for (item in items) {
                    groceryItems.add(item.title)
                    content.itemAcknowledge(item)
                }
            }

            override fun onNonContentAction(zoneId: String, adId: String) {
                Log.d("GroceryList", "Non-content action: zone=$zoneId, ad=$adId")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            zoneView?.onStop()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grocery List", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (useOriginalContext) "Recipe Context On" else "Recipe Context Off",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = useOriginalContext,
                            onCheckedChange = { checked ->
                                useOriginalContext = checked
                                zoneView?.setAdZoneContextId(if (checked) "original" else "")
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GroceryGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(AAPurpleDark, AAPurpleMid)
                    )
                )
                .padding(padding)
        ) {
            // Input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { text ->
                        inputText = text
                        if (text.length >= 2) {
                            try {
                                val matches = KeywordInterceptMatcher.match(text)
                                currentSuggestions = matches.toList()
                                currentSuggestions.forEach { it.presented() }
                            } catch (e: Exception) {
                                currentSuggestions = emptyList()
                            }
                        } else {
                            currentSuggestions = emptyList()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add an item...", color = Color.White.copy(alpha = 0.5f)) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AATealLight,
                        focusedBorderColor = AATealLight,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (inputText.isNotBlank()) {
                                groceryItems.add(inputText.trim())
                                AdAdaptedListManager.itemAddedToList(item = inputText.trim())
                                inputText = ""
                                currentSuggestions = emptyList()
                            }
                        }
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            groceryItems.add(inputText.trim())
                            AdAdaptedListManager.itemAddedToList(item = inputText.trim())
                            inputText = ""
                            currentSuggestions = emptyList()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GroceryGreen)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
                    Text("Add", color = Color.White)
                }
            }

            // Suggestions dropdown
            if (currentSuggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(max = 220.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = AAPurpleMid)
                ) {
                    LazyColumn {
                        itemsIndexed(currentSuggestions) { index, suggestion ->
                            Text(
                                text = suggestion.name,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        suggestion.selected()
                                        groceryItems.add(suggestion.name)
                                        AdAdaptedListManager.itemAddedToList(item = suggestion.name)
                                        inputText = ""
                                        currentSuggestions = emptyList()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                fontSize = 16.sp
                            )
                            if (index < currentSuggestions.lastIndex) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }

            // Grocery items list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(
                    items = groceryItems,
                    key = { index, item -> "$index-$item" }
                ) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier.weight(1f),
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        IconButton(
                            onClick = {
                                AdAdaptedListManager.itemDeletedFromList(item = groceryItems[index])
                                groceryItems.removeAt(index)
                            }
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = Color.Red.copy(alpha = 0.6f)
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    )
                }
            }

            // Ad Zone
            AndroidView(
                factory = { ctx ->
                    AaZoneView(ctx).apply {
                        init("102110")
                        setAdZoneContextId("")
                        onStart(contentListener)
                        zoneView = this
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }
    }
}
