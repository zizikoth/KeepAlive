package com.memo.alive.service

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.memo.alive.notification.ForegroundService

/**
 * title:本地服务
 * describe:
 *
 * @author zhou
 * @date 2019-04-01 14:11
 */
class LocalService : ForegroundService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bindService(
            Intent(this, RemoteService::class.java),
            connection, Service.BIND_IMPORTANT)
        return super.onStartCommand(intent, flags, startId)
    }

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            //断开连接的时候重新开启
            startService(Intent(this@LocalService, RemoteService::class.java))
            bindService(
                Intent(this@LocalService, RemoteService::class.java),
                this, Service.BIND_IMPORTANT)
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {}

    }
}