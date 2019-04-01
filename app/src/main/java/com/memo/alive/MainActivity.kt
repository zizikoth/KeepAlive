package com.memo.alive

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.memo.alive.notification.ForegroundService
import com.memo.alive.service.LocalService
import com.memo.alive.service.RemoteService

class MainActivity : AppCompatActivity() {

    /*** 前台服务 ***/
    private val foregroundService: Intent by lazy { Intent(this, ForegroundService::class.java) }
    /*** 本地服务 ***/
    private val localService: Intent by lazy { Intent(this, LocalService::class.java) }
    /*** 远程服务 ***/
    private val remoteService: Intent by lazy { Intent(this, RemoteService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //方案一：保活 开启一个前台1x1的Activity
        //注册开息屏接收
        //KeepAliveManager.registerKeepAlive(this)

        //方案二：保活 开启一个前台服务
        //startService(foregroundService)

        //方案三：拉活 双进程守护
        startService(localService)
        startService(remoteService)

    }

    override fun onDestroy() {
        super.onDestroy()
        //方案一：注销开息屏接收
        //KeepAliveManager.unRegisterKeepAlive(this)
    }
}
