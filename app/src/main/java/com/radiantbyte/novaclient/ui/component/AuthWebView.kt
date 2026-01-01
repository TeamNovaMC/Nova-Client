package com.radiantbyte.novaclient.ui.component

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Base64
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.radiantbyte.novaclient.game.AccountManager
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.bedrock.BedrockAuthManager
import net.raphimc.minecraftauth.msa.model.MsaDeviceCode
import net.raphimc.minecraftauth.msa.service.impl.DeviceCodeMsaAuthService
import java.util.function.Consumer
import kotlin.concurrent.thread

val auth = "UCmvDWiR0BjlX"
val enSuffix = "cHBJekl6R1YzUQ=="
val deSuffix = String(Base64.decode(enSuffix, Base64.DEFAULT)).trim()
val authId = "$auth-$deSuffix"

private const val GAME_VERSION = "1.21.131"

@SuppressLint("SetJavaScriptEnabled")
class AuthWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : WebView(context, attrs) {

    var callback: ((Throwable?) -> Unit)? = null

    init {
        CookieManager.getInstance()
            .removeAllCookies(null)

        settings.javaScriptEnabled = true
        webViewClient = AuthWebViewClient()
    }

    fun addAccount() {
        thread {
            runCatching {
                val httpClient = MinecraftAuth.createHttpClient()
                httpClient.connectTimeout = 10000
                httpClient.readTimeout = 10000

                val authManager = BedrockAuthManager.create(httpClient, GAME_VERSION)
                    .login(
                        { client, config, cb -> DeviceCodeMsaAuthService(client, config, cb) },
                        Consumer<MsaDeviceCode> { deviceCode ->
                            post {
                                loadUrl(deviceCode.directVerificationUri)
                            }
                        }
                    )

                val displayName = AccountManager.getDisplayName(authManager)
                val containedAccount = AccountManager.accounts.find { 
                    AccountManager.getDisplayName(it) == displayName 
                }
                if (containedAccount != null) {
                    AccountManager.removeAccount(containedAccount)
                }
                AccountManager.addAccount(authManager)

                if (containedAccount == AccountManager.selectedAccount) {
                    AccountManager.selectAccount(authManager)
                }
                callback?.invoke(null)
            }.exceptionOrNull()?.let {
                callback?.invoke(it)
            }
        }
    }

    inner class AuthWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            return false
        }

    }

}
