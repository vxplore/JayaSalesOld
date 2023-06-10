package com.jaya.web

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.webkit.*
import android.widget.Toast
import com.pixplicity.easyprefs.library.Prefs

import android.os.Bundle

import android.os.Parcelable
import com.jaya.MainActivity


interface WebCallback{
    fun allowLoadUrl(url: String): Boolean
    fun onPageStarted(view: WebView, url: String, favicon: Bitmap?)
    fun onLoadResource(view: WebView, url: String)
    fun onPageFinished(view: WebView, url: String)
    fun onReceivedError(webView: WebView, request: WebResourceRequest, error: WebResourceError)
    fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        errorResponse: WebResourceResponse
    )

    fun onProgress(view: WebView, newProgress: Int)
}

class CustomWebView : WebView {
    private var currentUrl = "https://v-xplore.com/dev/jaya/backend"
    var webCallback: WebCallback? = null
    var listener: ((String,String,Int,Int,Int)->Unit)? = null
    inner class InlineJavaScriptInterface(var webView: CustomWebView?) {

        @JavascriptInterface
        fun log(tag:String,message:String) {
            Log.d(tag,message)
        }
        @JavascriptInterface
        fun shortToast(message:String) {
            if(webView!=null)
            {
                Toast.makeText(webView!!.context,message,Toast.LENGTH_LONG).show()
            }
        }
        @JavascriptInterface
        fun longToast(message:String) {
            if(webView!=null)
            {
                Toast.makeText(webView!!.context,message,Toast.LENGTH_SHORT).show()
            }
        }
        @JavascriptInterface
        fun savePref(key:String,value:String) {
            Prefs.putString(key, value)
        }

        @JavascriptInterface
        fun getPref(key: String, value: String): String {
            return Prefs.getString(key, value)
        }

        @JavascriptInterface
        fun composeEmail(address: String, subject: String, text: String) {
            val selectorIntent = Intent(Intent.ACTION_SENDTO)
            selectorIntent.data = Uri.parse("mailto:$address")
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
            emailIntent.putExtra(Intent.EXTRA_TEXT, text)
            emailIntent.selector = selectorIntent
            (webView!!._context as MainActivity).startActivity(Intent.createChooser(emailIntent, "Send email..."))
        }
    }
    private var _context: Context
    private var webClient: CustomWebClient? = null
    private fun eval(script: String) {
        try {
            evaluateJavascript(script, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun commonConstructor() {
        setWebContentsDebuggingEnabled(true)
        clearCache(true)
        clearHistory()

        settings.javaScriptEnabled = true
        webClient = CustomWebClient()
        webClient?.webClientCallback = object: WebClientCallback {
            override fun onLoadResource(view: WebView, url: String) {
                webCallback?.onLoadResource(view,url)
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                webCallback?.onPageStarted(view,url,favicon)
            }

            override fun onPageFinished(view: WebView, url: String) {
                webCallback?.onPageFinished(view,url)
            }

            override fun onReceivedError(
                webview: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                webCallback?.onReceivedError(webview,request,error)
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse
            ) {
                webCallback?.onReceivedHttpError(view,request,errorResponse)
            }

        }
        addJavascriptInterface(InlineJavaScriptInterface(this),"agent")
        webViewClient = webClient!!

        settings.javaScriptCanOpenWindowsAutomatically = true;
        try {
            settings.pluginState = WebSettings.PluginState.ON;
        } catch (e: Exception) {
        }
        settings.mediaPlaybackRequiresUserGesture = false;

        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.cacheMode = WebSettings.LOAD_NO_CACHE;
        settings.domStorageEnabled = true

        webChromeClient =
            CustomWebChromeClient(context as MainActivity).apply {
                webChromeClientCallback = object: WebChromeClientCallback {
                    override fun onProgressChanged(view: WebView, newProgress: Int) {
                        webCallback?.onProgress(view,newProgress)
                    }
                }
            }
        setBackgroundColor(Color.TRANSPARENT)

        //loadUrl("file:///android_asset/index.html")
        //loadUrl("http://192.168.4.1")
        //loadUrl("https://www.google.com")
        //loadUrl("https://www.amazon.in/")
        //loadUrl("https://www.wikipedia.org")
    }



    constructor(context: Context) : super(context) {
        this._context = context
        commonConstructor()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this._context = context
        commonConstructor()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this._context = context
        commonConstructor()
    }

    override fun loadUrl(url: String) {
        if(webCallback?.allowLoadUrl(url)==true){
            currentUrl = url
            super.loadUrl(url)
        }
    }

    fun loadInitial(){
        loadUrl(currentUrl)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("superState", super.onSaveInstanceState())
        bundle.putString("currentUrl", currentUrl) // ... save stuff
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var state = state
        if (state is Bundle) // implicit null check
        {
            val bundle = state
            this.currentUrl = bundle.getString("currentUrl")?:"" // ... load stuff
            loadInitial()
            state = bundle.getParcelable("superState")
        }
        super.onRestoreInstanceState(state)
    }
}