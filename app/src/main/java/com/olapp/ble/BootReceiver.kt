package com.olapp.ble

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.olapp.data.preferences.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var appPreferences: AppPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val profileExists = appPreferences.isSetupComplete.first()
                    if (profileExists) {
                        BleForegroundService.start(context)
                    }
                } finally {
                    pending.finish()
                }
            }
        }
    }
}