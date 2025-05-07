package com.zionhuang.music.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.zionhuang.music.R
import java.util.Calendar

// Define un tipo que puede ser un ícono vectorial o un recurso drawable
sealed class IconResult {
    data class Vector(val icon: ImageVector) : IconResult()
    data class Drawable(@DrawableRes val resId: Int) : IconResult()
}

/**
 * Obtiene el ícono correspondiente según la fecha actual.
 *
 * @return El ícono como [IconResult], que puede ser un [ImageVector] o un drawable.
 */
fun getIconForDate(): IconResult {
    val calendar = Calendar.getInstance()
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1

    return when {
        month == 2 && day == 14 -> IconResult.Vector(Icons.Outlined.Favorite)

        month == 5 && day == 10 -> IconResult.Drawable(R.drawable.heart_love_day)

        month == 6 && day == 8 -> IconResult.Vector(Icons.Outlined.Cake)

        month == 6 && day == 28 -> IconResult.Vector(Icons.Outlined.Star)

        month == 10 && day == 12 -> IconResult.Vector(Icons.Outlined.HeartBroken)

        (month == 10 && day == 31) || (month == 11 && (day == 1 || day == 2)) -> IconResult.Drawable(R.drawable.ghost)

        month == 12 -> IconResult.Vector(Icons.Outlined.AcUnit)

        else -> IconResult.Vector(Icons.Outlined.Settings)
    }
}
