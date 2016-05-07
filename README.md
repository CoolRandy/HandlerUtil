# HandlerUtil
总结关于android所有handler相关的知识点

这个仓库中的代码全部是用于对Handler进行测试的一些demo，主要包括对于handler分别在UI线程以及子线程中的使用
此外还有对于HandlerThread的使用，具体后面会所有的类文件做一个详细说明，请稍后^_^
-------------------------

对各类的说明如下：
HandlerThreadActivity：该类主要用于对HandlerThread的使用做一番研究，具体使用代码中描述的很清楚，这里我主要说一下HandlerThread的作用，实际上HandlerThread继承于Thread，就是Thread的子类，只是Android系统对它做了一些封装，该方法中调用了Loop.prepare()和Loop.loop()方法，在传统的方法里worker thread如果在内部创建handler，则需要自己手动调用Loop的两个方法，而HandlerThread则已经封装好了

这样直接通过HandlerThread实例获取looper对象，然后通过looper对象来实例化Handler，接下来就可以在handler重写的handleMessage里处理一些耗时任务了，因为handler是运行在HandlerThread子线程中，具体使用详见代码哈

HandlerThread可以快速创建一个带有Looper的线程，有了这个Looper就可以创建相应的handler，下面结合一段代码来说明：

```java
public class HandlerThreadActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HandlerThread handlerThread = new HandlerThread("WorkerHandler");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        WorkerHandler workerHandler = new WorkerHandler(looper);
        Message message = new Message();
        message.what = 1;
        workerHandler.sendMessage(message);
    }

    class WorkerHandler extends Handler {

        public WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }

    }
}
```
这个示例很简单，主要用来说明HandlerThread的原理的，首先采用HandlerThread创建一个实例对象，然后调用该Thread线程实例的start方法，实际上内部会调用native的run方法，最后走到HandlerThread内部重写的run方法中，接下来看一下HandlerThread内部实现的run方法：

```java
@Override
    public void run() {
        mTid = Process.myTid();
        Looper.prepare();
        synchronized (this) {
            mLooper = Looper.myLooper();
            notifyAll();
        }
        Process.setThreadPriority(mPriority);
        onLooperPrepared();
        Looper.loop();
        mTid = -1;
    }
```
可以看到首先调用Looper的prepare方法设置Looper对象，然后加锁调用Looper的myLooper方法获取prepare设置的Looper实例，接着后面再调用Looper.loop()方法循环监控消息队列中是否有新的message进来。
回到前面的示例代码，调用HandlerThread实例的start方法走到重写的run方法获取到Looper实例之后，接下来就可以直接在子线程环境中创建自定义的WorkerHandler实例对象，然后直接调用Handler的sendMessage方法发送信息message，这样WorkerHandler对象中重写的handleMessage方法就可以获取到该messge，进而直接进行UI操作。
从上面的分析可以看到，采用HandlerThread的好处就是不需要创建子线程，并在子线程中创建Handler实例之前，调用Looper.prepare方法，也不需要在之后调用Looper.loop方法，因为HandlerThread都已经封装好了。
说到这里，我相信对于HandlerThread的原理一定理解的很清晰了~
