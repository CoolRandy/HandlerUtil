package com.example.randy.helloworld.HandlerImage;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by randy on 2015/11/9.
 */
public class ImageLoader {

    //内存缓存
    private LruCache<String, Bitmap> mLrucache;

    //线程池
    private ExecutorService mThreadPool;

    /**
     * 线程池数量，默认为1
     */
    private int mThreadCount = 1;

    /**
     * 队列的调度方式
     */
    private Type mType = Type.LIFO;

    public enum Type{
        FIFO, LIFO
    }

    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTasks;
    /**
     * 轮询的线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;

    /**
     * 运行在UI线程的handler，用于给ImageView设置图片
     */
    private Handler handler;
    /**
     * 引入一个值为1的信号量，防止mPoolThreadHander未初始化完成
     */
    private volatile Semaphore mSemapnore = new Semaphore(1);

    /**
     * 引入一个值为1的信号量，由于线程池内部也有一个阻塞线程，防止加入任务的速度过快，使LIFO效果不明显
     */
    private volatile Semaphore mPoolSemaphore;

    private static ImageLoader mInstance;

    /**
     * 单例获取该实例对象
     */
    public static ImageLoader getInstance(){

        if(null == mInstance){
            synchronized (ImageLoader.class){
                if(null == mInstance){
                    mInstance = new ImageLoader(1, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    /**
     * 单例获得该实例对象
     *
     * @return
     */
    public static ImageLoader getInstance(int threadCount, Type type)
    {

        if (mInstance == null)
        {
            synchronized (ImageLoader.class)
            {
                if (mInstance == null)
                {
                    mInstance = new ImageLoader(threadCount, type);
                }
            }
        }
        return mInstance;
    }

    private ImageLoader(int threadCount, Type type){
        init(threadCount, type);
    }

    public void init(int threadCount, Type type){

        mPoolThread = new Thread(){
            @Override
            public void run() {
                //super.run();

                try{
                    //请求一个信号量  该信号量是为了锁住mPoolThreadHandler的实例创建
                    //由于待会会在UI线程中使用该handler实例，这里可以保证创建完成
                    mSemapnore.acquire();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

                //子线程中使用handler，需要准备looper
                Looper.prepare();
                mPoolThreadHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        //super.handleMessage(msg);
                        //直接取出一个任务执行
                        mThreadPool.execute(getTask());
                        try{
                            mPoolSemaphore.acquire();
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }

                    }
                };
                //释放信号量
                mSemapnore.release();
                Looper.loop();
            }
        };

        mPoolThread.start();
        //获取应用最大可用内存  设置缓存大小为可用最大内存的1/8，同时对比他马屁对象进行压缩
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        mLrucache = new LruCache<String, Bitmap>(cacheSize){

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };

        mThreadPool = Executors.newFixedThreadPool(threadCount);//新建固定线程大小的线程池
        mPoolSemaphore = new Semaphore(threadCount);//按照设置线程池数量设置了一个信号量
        mTasks = new LinkedList<Runnable>();
        mType = type == null ? Type.LIFO : type;
    }

    /**
     * 获取一个任务
     * @return
     */
    public synchronized Runnable getTask(){
        if(mType == Type.FIFO){
            return mTasks.removeFirst();
        }else if(mType == Type.LIFO){
            return mTasks.removeLast();
        }
        return null;
    }

    /**
     * 添加一个任务
     * @param runnable
     */
    public synchronized void addTask(Runnable runnable){
        try{
            if(null == mPoolThreadHandler){
                mSemapnore.acquire();
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        mTasks.add(runnable);
        mPoolThreadHandler.sendEmptyMessage(0x101);
    }

    


}
