package com.lyk.refresh_ui

import android.app.Application
import com.alibaba.fastjson.JSONObject
import com.lyk.log.HiConsolePrinter
import com.lyk.log.HiFilePrinter
import com.lyk.log.HiLogConfig
import com.lyk.log.HiLogManager

class MApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        HiLogManager.init(
            object : HiLogConfig() {
                override fun injectJsonParser(): JsonParser? {
                    return JsonParser { src -> JSONObject.toJSONString(src) }
                }

                override fun getGlobalTag(): String {
                    return "MApplication"
                }

                override fun enable(): Boolean {
                    return true
                }

                override fun includeThread(): Boolean {
                    return false
                }

                override fun stackTraceDepth(): Int {
                    return 0
                }
            },
            HiConsolePrinter(),
            HiFilePrinter.getInstance(applicationContext.cacheDir.absolutePath, 0)
        )
    }
}