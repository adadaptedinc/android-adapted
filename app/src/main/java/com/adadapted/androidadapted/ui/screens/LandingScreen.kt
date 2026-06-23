package com.adadapted.androidadapted.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adadapted.android.sdk.AdAdaptedListManager
import com.adadapted.androidadapted.ui.theme.AAPurpleDark
import com.adadapted.androidadapted.ui.theme.AAPurpleMid
import com.adadapted.androidadapted.ui.theme.AATealLight
import com.adadapted.androidadapted.ui.theme.EventOrange
import com.adadapted.androidadapted.ui.theme.GroceryGreen
import com.adadapted.androidadapted.ui.theme.ScrollerBlue

@Composable
fun LandingScreen(
    onNavigateToGroceryList: () -> Unit,
    onNavigateToScroller: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AAPurpleDark, AAPurpleMid, AAPurpleDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AATealLight.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "App Icon",
                    modifier = Modifier.size(56.dp),
                    tint = AATealLight
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "AdAdapted SDK",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Compose Test App",
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                color = AATealLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose a test scenario",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            LandingButton(
                icon = Icons.Filled.ShoppingCart,
                title = "Grocery List",
                description = "Text input, keyword suggestions, single ad zone",
                color = GroceryGreen,
                onClick = onNavigateToGroceryList
            )

            Spacer(modifier = Modifier.height(14.dp))

            LandingButton(
                icon = Icons.Filled.SwapVert,
                title = "Infinite Scroller",
                description = "LazyColumn with alternating ad zones",
                color = ScrollerBlue,
                onClick = onNavigateToScroller
            )

            Spacer(modifier = Modifier.height(14.dp))

            LandingButton(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                title = "List Manager Events",
                description = "Fire all ListManager tracking methods",
                color = EventOrange,
                onClick = {
                    AdAdaptedListManager.itemAddedToList(item = "TestItem")
                    AdAdaptedListManager.itemAddedToList(item = "TestItem", list = "TestList")
                    AdAdaptedListManager.itemCrossedOffList(item = "TestItem")
                    AdAdaptedListManager.itemDeletedFromList(item = "TestItem")
                    Toast.makeText(context, "All ListManager events sent", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun LandingButton(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(28.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }
    }
}
