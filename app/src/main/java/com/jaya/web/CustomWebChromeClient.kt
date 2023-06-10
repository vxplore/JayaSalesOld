package com.jaya.web

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.widget.EditText
import android.text.InputType
import android.webkit.*
import android.widget.Toast

import android.content.ActivityNotFoundException

import android.content.Intent
import android.util.Log
import com.jaya.MainActivity



interface WebChromeClientCallback{
    fun onProgressChanged(view: WebView, newProgress: Int)

}

class CustomWebChromeClient(val myActivity: MainActivity) : WebChromeClient() {
    var webChromeClientCallback: WebChromeClientCallback? = null
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        webChromeClientCallback?.onProgressChanged(view,newProgress)
    }

    override fun onJsConfirm(
        view: WebView,
        url: String,
        message: String,
        result: JsResult
    ): Boolean {
        val b = AlertDialog.Builder(view.context)
            .setTitle("JayaSales")
            .setCancelable(false)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { dialog, which -> result.confirm() }
            .setNegativeButton(R.string.cancel) { dialog, which -> result.cancel() }
        b.show()

        // Indicate that we're handling this manually
        return true
    }

    override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
        val b = AlertDialog.Builder(view.context)
            .setTitle("JayaSales").setCancelable(false)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { dialog, which -> result.confirm() }
        b.show()

        // Indicate that we're handling this manually
        return true
    }

    override fun onJsPrompt(
        view: WebView,
        url: String,
        message: String,
        defaultValue: String,
        result: JsPromptResult
    ): Boolean {
        val input = EditText(view.context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(defaultValue)
        AlertDialog.Builder(view.context)
            .setTitle("JayaSales").setCancelable(false)
            .setView(input)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { dialog, which -> result.confirm(input.text.toString()) }
            .setNegativeButton(R.string.cancel) { dialog, which -> result.cancel() }
            .create()
            .show()
        return true
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: WebChromeClient.FileChooserParams?
    ): Boolean {
        Log.d("mode_debug","${fileChooserParams?.mode},${fileChooserParams?.acceptTypes?.joinToString(",")}")
        /*return super.onShowFileChooser(webView, filePathCallback, fileChooserParams)*/
        // make sure there is no existing message
        // make sure there is no existing message
        if (myActivity.uploadMessage != null) {
            myActivity.uploadMessage?.onReceiveValue(null)
            myActivity.uploadMessage = null
        }

        myActivity.uploadMessage = filePathCallback

        val intent = fileChooserParams?.createIntent()
        intent?.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,fileChooserParams.mode==1)
        intent?.type = "*/*";
        intent?.putExtra(Intent.EXTRA_MIME_TYPES, fileChooserParams.acceptTypes)

        try {
            if (intent != null) {
                myActivity.startActivityForResult(intent, MainActivity.REQUEST_SELECT_FILE)
            }
        } catch (e: ActivityNotFoundException) {
            myActivity.uploadMessage = null
            Toast.makeText(myActivity, "Cannot open file chooser", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }
}