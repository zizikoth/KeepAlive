# 目录
[ 我的学习手册 - 热更新了解了一下下 ](https://www.jianshu.com/p/a994c5d08767)  
[ 我的学习手册 - Glide了解了一下下 ](https://www.jianshu.com/p/810490b1ccee)    
[ 我的学习手册 - 进程保活了解了一下下 ](https://www.jianshu.com/p/e3d731314abc) 

# 进程保活
##### 进程被干掉的原因
1.手机厂商出于对手机性能的考虑，会对第三方的app进行一个检查，查看是否开启后台服务，如果是长时间的后台服务，那么系统会干掉进程    
2.安卓手机在手机可用内存过低的时候会根据优先级，先干掉优先级最小占用内存最大的app

##### 优先级的五个分类

前台进程
可见进程
服务进程
后台进程
空进程

前台进程    
指正在与用户进行交互的应用进程，该进程数量较少,是最高优先级进程,系统一般不会终止该进程    
进程中包含处于前台的正与用户交互的activity;
进程中包含与前台activity绑定的service;
进程中包含调用了startForeground()方法的service;
进程中包含正在执行onCreate(), onStart(), 或onDestroy()方法的service;
进程中包含正在执行onReceive()方法的BroadcastReceiver.

可视进程    
能被用户看到，但不能根据根据用户的动作做出相应的反馈    
进程中包含可见但不处于前台进程的activity（如:弹出对话窗时activity处于可见状态，但并不处于前台进程中)
该进程有一个与可见/前台的activity绑定数据的service

服务进程    
没有可见界面仍在不断的执行任务的进程，除非在可视进程和前台进程紧缺资源(如:内存资源)才会被终止  
包含除前台进程和可视进程的service外的service的进程

后台进程    
通常系统中有大量的后台进程,终止后台进程不会影响用户体验,随时为优先级更高的进程腾出资源而被终止,优先回收长时间没用使用过的进程。   
包含不在前台或可视进程的activity的进程,也就是已经调用onStop()方法后的activity

空进程  
为提高整体系统性能，系统会保存已经完成生命周期的应用程序,存在与内存当中，也就是缓存,为下次的启动更加迅速而设计。


### 常用的三种方案
#### 方案一：（保活）开启一个前台Activity
思路：监听手机的开息屏发送的广播，在息屏的时候开启一个1x1透明的Activity，达到提升当前App的优先级的作用，在开屏的时候关闭
主要有三个类：
KeepAliveActivity，这个就是一个1x1透明的Activity
```
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
```
KeepAliveReceiver，对手机的开息屏进行监听
```
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
```
KeepAliveManager，主要进行开启关闭KeepAliveActivity，注册注销监听
```
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
```

#### 方案二：（保活）开启一个前台服务（百度地图导航的时候）
思路：在用户点击home键之后开启一个前台服务，达到提升优先级的作用
就是开启一个前台服务 主要的东西就是对于Notification的适配问题
在安卓7.0上通过创建两个相同id的服务，然后关闭其中一个来达到欺骗手机当前id的服务已经被关闭，使手机通知栏不提示当前App开启了一个前台服务
```
open class ForegroundService : Service() {
    companion object {
        val SERVICE_ID: Int = 1
    }

    override fun onBind(intent: Intent?): IBinder? = LocalBinder()

    private class LocalBinder : Binder()

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
            startService(Intent(this, InnerService::class.java))
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
    class InnerService : Service() {

        override fun onBind(intent: Intent?): IBinder? = LocalBinder()

        override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            // 开启一个新的服务
            // 关闭服务和提示
            // 由于使用的是同一个ServiceId 所以就不会有提示了 但是停止的是这个内部服务
            startForeground(SERVICE_ID, Notification())
            stopForeground(true)
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }
    }
}
```

#### 方案三：（拉活）双进程守护
思路：通过两个进程开启两个前台服务，相互拉起对方，如果那一方被杀死了，先拉起对方，再通过对方拉起自己达到自己始终存在的作用，效率是最好的，如果双方都被杀死也是没用了
```
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
```
```
class RemoteService:ForegroundService(){

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bindService(
                Intent(this, LocalService::class.java),
                connection, Service.BIND_IMPORTANT)
        return super.onStartCommand(intent, flags, startId)
    }

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            //断开连接的时候重新开启
            startService(Intent(this@RemoteService, LocalService::class.java))
            bindService(
                    Intent(this@RemoteService, LocalService::class.java),
                    this, Service.BIND_IMPORTANT)
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {}

    }
}
```







