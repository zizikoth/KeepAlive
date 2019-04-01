package com.memo.alive.keep

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils

/**
 * title:对开息屏进行监听
 * describe:
 *
 * @author zhou
 * @date 2019-04-01 11:05
 */
class KeepAliveReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val action: String? = intent.action
            if (TextUtils.equals(action, Intent.ACTION_SCREEN_OFF)) {
                //锁屏 开启KeepAliveActivity
                KeepAliveManager.startKeepAlive(context)
            } else if (TextUtils.equals(action, Intent.ACTION_SCREEN_ON)) {
                //开屏 关闭KeepAliveActivity
                KeepAliveManager.finishKeepAlive()
            }
        }
    }
}