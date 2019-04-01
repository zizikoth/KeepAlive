package com.memo.alive.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat

/**
 * title:前台服务
 * describe:
 *
 * @author zhou
 * @date 2019-04-01 13:51
 */
open class ForegroundService : Service() {

    private val SERVICE_ID: Int = 1

    override fun onBind(intent: Intent?): IBinder?{
        return LocalBinder()
    }

    private class LocalBinder: Binder()

    /**
     * 开启Service
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //如果小于 4.3
            startForeground(SERVICE_ID, Notification())
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //小于7.0
            startForeground(SERVICE_ID, Notification())
            //由于Android 7.0之后开启前台服务会在通知栏提示App在前台服务
            //消除前台服务的提示
            startService(Intent(this,InnerService::class.java))
        } else {
            //7.0之后
            val manager: NotificationManager? = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            //设置Channel 主要是设置Importance 越小表示通知重要性越低 通知可能越低
            val channel = NotificationChannel("channelId", "channelName", NotificationManager.IMPORTANCE_MIN)
            manager?.createNotificationChannel(channel)
            val notification: Notification = NotificationCompat.Builder(this, "channelId").build()
            startForeground(SERVICE_ID, notification)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 子Service
     */
    inner class InnerService : Service() {
        override fun onBind(intent: Intent?): IBinder? = null

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            // 开启一个新的服务
            startForeground(SERVICE_ID, Notification())
            // 关闭服务和提示
            // 由于使用的是同一个ServiceId 所以就不会有提示了 但是停止的是这个内部服务
            stopForeground(true)
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

    }
}