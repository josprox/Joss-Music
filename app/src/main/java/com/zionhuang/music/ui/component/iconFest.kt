package com.zionhuang.music.ui.component

import androidx.annotation.DrawableRes
import com.zionhuang.music.R
import java.util.Calendar

/**
 * Obtiene el recurso del ícono correspondiente según la fecha actual.
 *
 * @return El recurso del ícono según la fecha.
 */
@DrawableRes
fun getIconForDate(): Int {
    val calendar = Calendar.getInstance()
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1 // Los meses van de 0 a 11

    return when {
        // Verificar si es el Día del Amor y la Amistad (14 de febrero)
        month == 2 && day == 14 -> R.drawable.heart_love_day

        // Verificar si es 12 de octubre (Zara fest)
        month == 10 && day == 12 -> R.drawable.favorite_border

        // Verificar si es Día de Muertos (31 de octubre al 2 de noviembre)
        (month == 10 && day == 31) || (month == 11 && (day == 1 || day == 2)) -> R.drawable.ghost

        // Verificar si es diciembre
        month == 12 -> R.drawable.snowflake

        // Cualquier otro día
        else -> R.drawable.settings
    }
}