package com.coolrandy.com.handlerservicedemo;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

/**
 * Created by admin on 2016/3/9.
 * 有几个点需要注意：这里采用的是HandlerThread，即service用来维护的自己的线程，service终止时要记得退出线程
 * service是采用START_NOT_STICKY的启动方式，也就是说只有在用户主动发送请求时才开启service，不会再应用退出后自动重启该服务
 */
public class LoginService extends Service implements LoginHandler.LoginCallBack{

    public static final String BUNDLED_RECEIVER_KEY = "receiver";
    private static final String TAG = LoginService.class.getSimpleName();

    private HandlerThread handlerThread;
    private ResultReceiver resultReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //启动一个后台线程
        handlerThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle bundle = intent.getExtras();
        Log.e("TAG", "Bundle: " + bundle);
        if (bundle == null) {
            throw new IllegalArgumentException("LoginService must be provided with bundled arguments.");
        }

        if (!bundle.containsKey(BUNDLED_RECEIVER_KEY) || bundle.getParcelable(BUNDLED_RECEIVER_KEY) == null) {
            throw new IllegalArgumentException("LoginService must be provided with a ResultReceiver.");
        }

        resultReceiver = bundle.getParcelable(BUNDLED_RECEIVER_KEY);
        Log.e("TAG", resultReceiver.toString());
        // If you want to pass any more arguments to the Handler, pass them in the start() method.
        // For now, pass only this Service, in its capacity as a LoginCallback.
        new LoginHandler(getApplicationContext(), handlerThread.getLooper()).start(this);

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onLoginSuccess(Intent intent) {

        resultReceiver.send(Activity.RESULT_OK, intent.getExtras());
        stopSelf();
    }

    @Override
    public void onLoginFail() {

        resultReceiver.send(Activity.RESULT_CANCELED, null);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("TAG", "exit app");
        handlerThread.quit();
    }
}
