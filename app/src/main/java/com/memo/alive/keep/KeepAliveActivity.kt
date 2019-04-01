package com.memo.alive.keep

import android.app.Activity
import android.os.Bundle
import com.memo.alive.log

/**
 * title: KeepAliveActivity 保活前台Activity
 * describe:主要的目的是开启一个透明的前台界面，来提升当前App的进程的优先级
 *
 * @author zhou
 * @date 2019-04-01 11:01
 */
class KeepAliveActivity:Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置界面1x1
        window.setLayout(1,1)
        //绑定当前Activity
        KeepAliveManager.bindKeepAliveActivity(this)
        log("KeepAliveActivity 开启")
    }

    override fun onDestroy() {
        super.onDestroy()
        log("KeepAliveActivity 关闭")
    }
}