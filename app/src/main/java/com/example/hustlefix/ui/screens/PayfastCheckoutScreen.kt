package com.example.hustlefix.ui.screens

import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayfastCheckoutScreen(
    url: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    onBackClick: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure Payment") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }

                        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                            val requestUrl = request?.url?.toString() ?: ""
                            
                            // More flexible detection for success/cancel
                            if (requestUrl.contains("success") || requestUrl.startsWith("hustlefix://payment-success")) {
                                onSuccess()
                                return true
                            }
                            if (requestUrl.contains("cancel") || requestUrl.startsWith("hustlefix://payment-cancel")) {
                                onCancel()
                                return true
                            }
                            
                            // Handle custom schemes if necessary (e.g. for external apps)
                            if (requestUrl.startsWith("intent://")) {
                                try {
                                    val intent = Intent.parseUri(requestUrl, Intent.URI_INTENT_SCHEME)
                                    context.startActivity(intent)
                                    return true
                                } catch (ignored: Exception) {
                                    return false
                                }
                            }
                            
                            return false
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true // Essential for many modern payment gateways
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        )
    }
}
