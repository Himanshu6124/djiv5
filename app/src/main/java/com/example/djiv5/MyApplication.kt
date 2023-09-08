package com.example.djiv5

import android.app.Application
import android.content.Context

class MyApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        com.secneo.sdk.Helper.install(this)
    }


}