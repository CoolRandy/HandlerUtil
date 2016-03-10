package com.coolrandy.com.handlerservicedemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by admin on 2016/3/9.
 */
public class LoginHandler {

    private Context context;
    private Looper looper;
    private static final int LOGIN_FAILED = -1;
    private static final int LOOPER_STARTED = 0;
    private static final int LOGIN_FINISHED = 1;

    private static final String TAG = LoginHandler.class.getSimpleName();

    public LoginHandler(Context context, Looper looper) {
        this.context = context;
        this.looper = looper;
    }

    public interface LoginCallBack{
        void onLoginSuccess(Intent intent);
        void onLoginFail();
    }

    public void start(final LoginCallBack loginCallBack){

        final Handler handler = new Handler(looper){
            @Override
            public void handleMessage(Message msg) {

                switch (msg.what){

                    case LOGIN_FAILED:
                        loginCallBack.onLoginFail();
                        break;
                    case LOOPER_STARTED:
                        /**
                         * 这一块可以进行一些耗时的异步操作：比如
                         * 获得网络请求的身份验证令牌（auth token）
                         * 通过网络请求获取用户账户
                         * 获取远程数据库与新的本地数据库
                         * 整合新数据库
                         * 根据最后请求的结果发送不同的message
                         */
                        try {
                            Thread.sleep(1000);
                            Log.e("TAG", "延迟1s");
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
//                        Handler handler1 = new Handler();

//                        Message message = Message.obtain(handler1, LOGIN_FINISHED);
                        Message message = Message.obtain();
                        Log.e("TAG", "start: get data " + message.what + "");
                        Bundle data = new Bundle();
                        data.putString("MESSAGE_KEY", "Done！");
                        message.setData(data);
                        message.what = LOGIN_FINISHED;
                        sendMessage(message);
                        break;
                    case LOGIN_FINISHED:
                        Bundle msgData = msg.getData();
                        Log.e("TAG", "finish: get data " + msgData.getString("MESSAGE_KEY"));
                        final Intent intent = new Intent();
                        loginCallBack.onLoginSuccess(intent);
                        break;
                }
            }
        };


        handler.post(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(LOOPER_STARTED);
            }
        });
    }
}
