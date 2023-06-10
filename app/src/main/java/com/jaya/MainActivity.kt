package com.jaya

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.webkit.*
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import com.jaya.app.MyApplication
import com.jaya.app.jayasales.R
import com.jaya.web.CustomWebView
import com.jaya.web.WebCallback

class MainActivity : AppCompatActivity() {
    private var initialized = false
    private var cl: CoordinatorLayout? = null

    companion object{
        val REQUEST_SELECT_FILE = 100
    }

    var uploadMessage: ValueCallback<Array<Uri>>? = null



    var net = MyApplication.net
    var netObserver = Observer<ConnectivityListener.Net> { t ->
        if(t.on){
            wv_custom?.loadInitial(preferredUrl = "https://v-xplore.com/dev/jaya/backend")
            snackbar("You are live")
        }
        else{
            wv_custom?.loadInitial(preferredUrl = "file:///android_asset/scratch.html")
            snackbar("You are offline")
        }
    }

    private var wv_custom: CustomWebView? = null
    private var pb_progress: ProgressBar? = null
    private var cl_top_layer: ConstraintLayout? = null
    private var iv_screenshot: ImageView? = null
    private var bt_try_again: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState==null){
            observeNetState()
        }
        changeStatusBarColor(color = 0xFFFFEB56)
        setContentView(R.layout.activity_main)
        findViews()
    }

    var initialLoaded = false

    override fun onResume() {
        super.onResume()
        if(initialLoaded){
            return
        }
        initialLoaded = true
        wv_custom?.webCallback = object: WebCallback {
            override fun allowLoadUrl(url: String): Boolean {
                val netOn = net.value?.on
                return netOn?:false
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                Log.d("TESTING", "onPageStarted: called")
                pb_progress?.progress = 0
                pb_progress?.isIndeterminate = true

            }

            override fun onLoadResource(view: WebView, url: String) {
                Log.d("TESTING", "onLoadResource: $url")
            }

            override fun onPageFinished(view: WebView, url: String) {
                pb_progress?.isIndeterminate = false
                pb_progress?.progress = 0
                Log.d("TESTING", "onPageFinished: $url")
            }


            override fun onReceivedError(
                webView: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.d("TESTING", "onReceivedError: ${error.errorCode}")
                }
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse
            ) {
                Log.d("TESTING", "onReceivedHttpError: ${errorResponse.reasonPhrase}")
            }

            override fun onProgress(view: WebView, newProgress: Int) {
                pb_progress?.isIndeterminate = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    pb_progress?.setProgress(newProgress,true)
                }
                else{
                    pb_progress?.progress = newProgress
                }
            }

        }
        if(net.value?.on==true){
            wv_custom?.loadInitial()
        }
    }

    private fun findViews() {
        wv_custom = findViewById(R.id.wv_custom)
        iv_screenshot = findViewById(R.id.iv_screenshot)
        cl_top_layer = findViewById(R.id.cl_top_layer)
        bt_try_again = findViewById(R.id.bt_try_again)
        pb_progress = findViewById(R.id.pb_progress)
        cl = findViewById(R.id.cl)
    }

    private fun observeNetState() {

        net.observeForever(netObserver)
    }

    private fun snackbar(s: String) {
        val snackbar = Snackbar
            .make(cl!!, s, Snackbar.LENGTH_SHORT)
        snackbar.show()
    }



    override fun onDestroy() {
        super.onDestroy()
        net.observeForever(netObserver)
    }

    override fun onBackPressed() {
        if (wv_custom?.canGoBack() == true && net.value?.on == true) {
            wv_custom?.goBack()
        } else {
            super.onBackPressed()
        }
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null) return;

            uploadMessage?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;
        }
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        var d = intent?.data
        var c = intent?.clipData
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) return
                if(d!=null){
                    uploadMessage!!.onReceiveValue(
                        WebChromeClient.FileChooserParams.parseResult(
                            resultCode,
                            intent
                        )
                    )
                    uploadMessage = null
                }
                else{
                    if(c!=null){
                        val uris = mutableListOf<Uri>()
                        for (i in 0 until c.itemCount) {
                            val uri = c.getItemAt(i).uri
                            uris.add(uri)
                        }
                        uploadMessage?.onReceiveValue(uris.toTypedArray())
                        uploadMessage = null
                    }
                }
            }
        } else if (requestCode == REQUEST_SELECT_FILE) {
            if (null == uploadMessage) return
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            val result =
                if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
            result?.let {
                uploadMessage?.onReceiveValue(arrayOf(result))
                uploadMessage = null
            }
        }*/

        if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null) return
            if(d!=null){
                uploadMessage!!.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        intent
                    )
                )
                uploadMessage = null
            }
            else{
                if(c!=null){
                    val uris = mutableListOf<Uri>()
                    for (i in 0 until c.itemCount) {
                        val uri = c.getItemAt(i).uri
                        uris.add(uri)
                    }
                    uploadMessage?.onReceiveValue(uris.toTypedArray())
                    uploadMessage = null
                }
            }
        }
    }
}


fun Activity.changeStatusBarColor(color: Long) {
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = color.toInt()
}