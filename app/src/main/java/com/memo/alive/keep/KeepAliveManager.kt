package com.memo.alive.keep

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import java.lang.ref.WeakReference

/**
 * title:保活管理
 * describe:
 *
 * @author zhou
 * @date 2019-04-01 11:05
 */
object KeepAliveManager {

    /*** 弱引用持有前台Activity ***/
    private var mWeakActivity: WeakReference<KeepAliveActivity>? = null

    /*** 手机关屏熄屏的广播接收者 ***/
    private val mKeepAliveReceiver: KeepAliveReceiver by lazy { KeepAliveReceiver() }

    /**
     * 注册开屏息屏的广播接收者
     */
    fun registerKeepAlive(context: Context) {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        context.registerReceiver(mKeepAliveReceiver, filter)
    }

    /**
     * 解除注册开屏息屏的广播接收者
     */
    fun unRegisterKeepAlive(context: Context) {
        context.unregisterReceiver(mKeepAliveReceiver)
    }

    /**
     * 绑定前台Activity
     */
    fun bindKeepAliveActivity(activity: KeepAliveActivity) {
        mWeakActivity = WeakReference(activity)
    }

    /**
     * 开启KeepAliveActivity
     */
    fun startKeepAlive(context: Context) {
        val intent = Intent(context, KeepAliveActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    /**
     * 关闭KeepAliveActivity
     */
    fun finishKeepAlive() {
        mWeakActivity?.get()?.finish()
        mWeakActivity = null
    }

}