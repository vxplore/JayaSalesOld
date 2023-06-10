package com.jaya.app

import android.app.Application
import android.content.ContextWrapper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jaya.ConnectivityListener
import com.pixplicity.easyprefs.library.Prefs

class MyApplication  : Application(){

    override fun onCreate() {
        super.onCreate()

        instance = this
        // Initialize the Prefs class
        Prefs.Builder()
            .setContext(this)
            .setMode(ContextWrapper.MODE_PRIVATE)
            .setPrefsName(packageName)
            .setUseDefaultSharedPreference(true)
            .build()

        ConnectivityListener(this).net.observeForever {
            _net.postValue(it)
        }
    }

    companion object{
        lateinit var instance: MyApplication
        private val  _net = MutableLiveData<ConnectivityListener.Net>()
        val net :LiveData<ConnectivityListener.Net> = _net
    }
}