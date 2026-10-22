package com.adadapted.androidadapted.ui.lists

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.adadapted.androidadapted.data.model.ListIcon
import com.adadapted.androidadapted.ui.theme.AccentColors

val ListIcon.imageVector: ImageVector
    get() = when (this) {
        ListIcon.CART -> Icons.Filled.ShoppingCart
        ListIcon.PRODUCE -> Icons.Filled.Eco
        ListIcon.HOUSEHOLD -> Icons.Filled.Home
        ListIcon.ELECTRONICS -> Icons.Filled.Tv
        ListIcon.PARTY -> Icons.Filled.Cake
        ListIcon.TAKEOUT -> Icons.Filled.Fastfood
    }

/** Each icon has its own color, so lists are easy to tell apart at a glance. */
val ListIcon.accent: Color
    get() = when (this) {
        ListIcon.CART -> AccentColors.Mint
        ListIcon.PRODUCE -> AccentColors.Jade
        ListIcon.HOUSEHOLD -> AccentColors.Sky
        ListIcon.ELECTRONICS -> AccentColors.Seafoam
        ListIcon.PARTY -> AccentColors.Cocoa
        ListIcon.TAKEOUT -> AccentColors.Lime
    }
