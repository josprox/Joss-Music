package com.zionhuang.music.ui.component.admob

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.zionhuang.music.BuildConfig
import com.zionhuang.music.ui.component.DonationCardWithTiming
import org.dotenv.vault.dotenvVault
import java.util.Calendar

@Composable
fun AdMobBannerAd() {
    val context = LocalContext.current
    val adView = rememberAdView(context)

    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    val shouldShowAd = currentMonth != Calendar.DECEMBER

    if (shouldShowAd) {
        AndroidView(
            factory = { adView },
            update = {
                val adRequest = AdRequest.Builder().build()
                it.loadAd(adRequest)
            }
        )
    }else{
        DonationCardWithTiming()
    }
}

@Composable
fun rememberAdView(context: Context): AdView {
    val dotenv = dotenvVault(BuildConfig.DOTENV_KEY) {
        directory = "/assets"
        filename = "env.vault" // instead of '.env', use 'env'
    }
    val adView = AdView(context)
    adView.setAdSize(AdSize.BANNER)
    adView.adUnitId = dotenv["YOUR_AD_UNIT_ID"] // Reemplaza con tu ID de bloque de anuncios
    return adView
}
