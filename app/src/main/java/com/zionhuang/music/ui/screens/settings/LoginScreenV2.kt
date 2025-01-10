package com.zionhuang.music.ui.screens.settings

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.zionhuang.music.LocalPlayerAwareWindowInsets
import com.zionhuang.music.R
import com.zionhuang.music.constants.AccessTokenKey
import com.zionhuang.music.constants.RefreshTokenKey
import com.zionhuang.music.ui.component.IconButton
import com.zionhuang.music.ui.utils.backToMain
import com.zionhuang.music.utils.rememberPreference
import kotlinx.coroutines.DelicateCoroutinesApi

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class, DelicateCoroutinesApi::class)
@Composable
fun LoginScreenV2(
    navController: NavController,
) {
    var accessToken by rememberPreference(AccessTokenKey, "")
    var refreshToken by rememberPreference(RefreshTokenKey, "")

    var webView: WebView? = null

    AndroidView(
        modifier = Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
            .fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = object : WebViewClient() {
                    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
                        // Verifica si el URL contiene los tokens
                        if (url.startsWith("https://jossred.josprox.com/api/yt/oauth/callback")) {
                            // Obtén las cookies
                            val cookies = CookieManager.getInstance().getCookie(url)
                            if (cookies != null) {
                                // Extrae los valores de access_token y refresh_token de las cookies
                                val accessTokenRegex = "access_token=([^;]+)".toRegex()
                                val refreshTokenRegex = "refresh_token=([^;]+)".toRegex()

                                accessToken = accessTokenRegex.find(cookies)?.groupValues?.get(1) ?: ""
                                refreshToken = refreshTokenRegex.find(cookies)?.groupValues?.get(1) ?: ""

                                // Almacena los tokens en SharedPreferences
                                // Usamos rememberPreference o SharedPreferences directamente para guardar los valores
                            }
                        }
                    }

                    override fun onPageFinished(view: WebView, url: String?) {
                        loadUrl("javascript:Android.onRetrieveVisitorData(window.yt.config_.VISITOR_DATA)")
                    }
                }
                settings.apply {
                    javaScriptEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE)  // Asegura que el contenido mixto se maneje correctamente
                    domStorageEnabled = true  // Habilita almacenamiento de DOM
                    allowFileAccessFromFileURLs = false
                    allowUniversalAccessFromFileURLs = false

                    // Establecer User-Agent similar al de Chrome
                    userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36"
                }
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onRetrieveVisitorData(newVisitorData: String?) {
                        // Puedes manejar la información adicional aquí
                    }
                }, "Android")
                webView = this
                loadUrl("https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?scope=openid%20email%20profile%20https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fyoutube.readonly&access_type=offline&state=d43b5331a25f839792a91acf573b25f9&response_type=code&redirect_uri=https%3A%2F%2Fjossred.josprox.com%2Fapi%2Fyt%2Foauth%2Fcallback&client_id=747946626269-g13bg0v09mcg483phv9sdao7ijj5a2el.apps.googleusercontent.com&service=lso&o2v=2&ddm=1&flowName=GeneralOAuthFlow")
            }
        }
    )

    TopAppBar(
        title = { Text(stringResource(R.string.login)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        }
    )

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }
}
