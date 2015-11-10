package com.example.randy.helloworld.HandlerImage;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.TextView;

import com.example.randy.helloworld.R;

/**
 * Created by randy on 2015/11/9.
 */
public class HandlerThreadActivity extends AppCompatActivity {

    private TextView mServiceInfo;
    private HandlerThread mCheckMsgThread;
    private Handler mCheckMsgHandler;

    private boolean isUpdateInfo;
    private static final int MSG_UPDATE_INFO = 0x110;

    //ui线程管理的handler
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.handler_thread_layout);

        //创建后台线程
        initBackThread();
        mServiceInfo = (TextView)findViewById(R.id.text);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //
        isUpdateInfo = true;
        mCheckMsgHandler.sendEmptyMessage(MSG_UPDATE_INFO);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        //停止查询
        isUpdateInfo = false;
        mCheckMsgHandler.removeMessages(MSG_UPDATE_INFO);

    }

    public void initBackThread(){

        mCheckMsgThread = new HandlerThread("check-massage-coming");
        mCheckMsgThread.start();
        mCheckMsgHandler = new Handler(mCheckMsgThread.getLooper()){

            @Override
            public void handleMessage(Message msg) {
                //super.handleMessage(msg);
                checkForUpdate();
                if(isUpdateInfo){
                    mCheckMsgHandler.sendEmptyMessageDelayed(MSG_UPDATE_INFO, 1000);
                }
            }
        };
    }

    /**
     * 模拟从服务器解析数据
     */
    private void checkForUpdate(){
        try{

            //模拟耗时逻辑
            Thread.sleep(1000); //这里依然运行在mCheckMsgThread这个子线程中
            //这个handler实例是定义在UI线程中的，所以下面的操作都是运行在UI线程中的，故而可以直接访问UI
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String result = "实时更新中，当前大盘指数： <font color='red'>%d</font>";
                    result = String.format(result, (int)(Math.random() * 3000 + 1000));
                    mServiceInfo.setText(Html.fromHtml(result));
                }
            });
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //释放资源
        mCheckMsgThread.quit();
    }

}
