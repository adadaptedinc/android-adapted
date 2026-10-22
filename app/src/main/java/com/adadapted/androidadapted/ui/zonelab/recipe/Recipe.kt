package com.adadapted.androidadapted.ui.zonelab.recipe

data class Recipe(
    val title: String,
    val servings: Int,
    val minutes: Int,
    val ingredients: List<String>,
    val steps: List<String>,
) {
    companion object {
        val TomatoSauce = Recipe(
            title = "Weeknight Tomato Sauce",
            servings = 4,
            minutes = 35,
            ingredients = listOf(
                "Olive Oil",
                "Yellow Onion",
                "Garlic",
                "Chili Flakes",
                "Crushed Tomatoes",
                "Fresh Basil",
            ),
            steps = listOf(
                "Warm the olive oil in a wide pan over medium heat until it shimmers.",
                "Add the onion and a pinch of salt, then cook until soft and translucent.",
                "Stir in the garlic and chili flakes and cook for another minute.",
                "Pour in the tomatoes, breaking them up with a spoon as they heat through.",
                "Simmer uncovered for twenty minutes, stirring now and then.",
                "Fold in the fresh basil off the heat and taste for seasoning.",
            ),
        )
    }
}
