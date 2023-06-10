package com.jaya.web

import android.graphics.Bitmap
import android.webkit.*

interface WebClientCallback{
    fun onLoadResource(view: WebView, url: String)
    fun onPageStarted(view: WebView, url: String, favicon: Bitmap?)
    fun onPageFinished(view: WebView, url: String)
    fun onReceivedError(webview: WebView, request: WebResourceRequest, error: WebResourceError)
    fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse
    )
}

class CustomWebClient : WebViewClient() {

    var webClientCallback: WebClientCallback? = null
    override fun onLoadResource(view: WebView, url: String) {
        super.onLoadResource(view, url)
        webClientCallback?.onLoadResource(view,url)
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        webClientCallback?.onPageStarted(view,url,favicon)
    }

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
    }

    /*override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        }
        return super.shouldOverrideUrlLoading(view, request)
    }*/

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        webClientCallback?.onPageFinished(view,url)
    }

    override fun onReceivedError(
        webview: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        super.onReceivedError(webview, request, error)
        webClientCallback?.onReceivedError(webview,request,error)
    }

    override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        webClientCallback?.onReceivedHttpError(view,request,errorResponse)
    }
}