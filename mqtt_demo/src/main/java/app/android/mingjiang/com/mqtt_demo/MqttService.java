package app.android.mingjiang.com.mqtt_demo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

import app.android.mingjiang.com.mqttapi.MqttConfig;
import app.android.mingjiang.com.mqttapi.MqttServer;
import app.android.mingjiang.com.mqttapi.MqttSubscribeListener;

/**
 * Mqtt服务类。
 * 作者：wangzs on 2016/1/20 14:03
 * 邮箱：wangzhaosen@shmingjiang.org.cn
 */
public class MqttService extends Service implements MqttCallback {

    private static final String TAG = "MqttService";

    //设备唯一标识符
    private static String clientId = "";
    //获取MqttClient对象
    private MqttClient mMqttClient = null;
    //是否连接成功
    private boolean isConntect = false;
    //注册消息监听接口
    private static List<MqttSubscribeListener> mOnPushMesageListeners = new ArrayList<MqttSubscribeListener>();
    //启动关闭标识
    private static  String ACTION_START = clientId + ".START";
    private static  String ACTION_STOP = clientId + ".STOP";
    //连接线程
    Thread connectThread = null;

    private Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            if(mMqttClient == null){
                createClient();
                connectClient();
            }else{
                connectClient();
            }
        }
    };
    @Override
    public void connectionLost(Throwable throwable) {
        Log.d(TAG, "- connectionLost()");
        Log.e(TAG, "- ", throwable);
    }

    @Override
    public void messageArrived(String sub, MqttMessage mqttMessage) throws Exception {
        Log.d(TAG, "- messageArrived()"+sub);
        if (mqttMessage != null) {
            byte bytes[] = mqttMessage.getPayload();
            if (bytes != null) {
                String message = new String(bytes);
                Log.d(TAG, "- deliveryComplete()-mesage-" + message);
                //可以通过EventBusPost到需要的地方，也可通过回调方法来实现。
                nofityPushMessage(message);
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        Log.d(TAG, "- deliveryComplete()");
        try {
            MqttMessage msg = iMqttDeliveryToken.getMessage();
            if (msg != null) {
                byte bytes[] = msg.getPayload();
                if (bytes != null) {
                    String message = new String(bytes);
                    Log.d(TAG, "- deliveryComplete()-mesage-" + message);
                    nofityPushMessage(message);
                }
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connectThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_STOP)) {//关闭服务
            stop();
            stopSelf();
        } else if (intent.getAction().equals(ACTION_START)) {//启动服务
            connectThread();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void connectThread(){
        if(!isConntect){
            connectThread = new Thread(connectRunnable);
            connectThread.start();
        }
    }

    //创建MqttClient对象。
    private void createClient(){
        mMqttClient = MqttServer.createClient(this, MqttConfig.URL);
        mMqttClient.setCallback(this);
    }

    //连接MqttClient对象。
    private void connectClient(){
        if(mMqttClient != null){
            isConntect = MqttServer.connect(mMqttClient, MqttConfig.USER_NAME, MqttConfig.PASS_WORD, MqttConfig.TOPIC,MqttConfig.TIME_OUT,MqttConfig.ALIVE_INTERVAL,MqttConfig.CLEAN_SESSION);
        }
    }

    /**
     * 关闭client对象。
     */
    private void stop() {
        if (mMqttClient != null && mMqttClient.isConnected()) {
            try {
                mMqttClient.disconnect();
                Log.d("关闭", "成功关闭Mqtt连接");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMqttClient != null){
            try {
                mMqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 添加消息注册。
     * @param mOnPushMesageListener
     */
    public final static void addPushMesageListeners(
            MqttSubscribeListener mOnPushMesageListener) {
        mOnPushMesageListeners.add(mOnPushMesageListener);
    }

    /**
     * 删除消息注册。
     * @param mOnPushMesageListener
     */
    public final static void romvePushMesageListeners(
            MqttSubscribeListener mOnPushMesageListener) {
        mOnPushMesageListeners.remove(mOnPushMesageListener);
    }

    /**
     * 消息更新处理。
     * @param msg
     */
    private final void nofityPushMessage(String msg) {
        List<MqttSubscribeListener> mLm = getOnPushMesageListeners();
        for (MqttSubscribeListener l : mLm) {
            try {
                if (l != null)
                    l.onNewPushMessage(msg);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 获取所有注册事件。
     * @return
     */
    public final static List<MqttSubscribeListener> getOnPushMesageListeners() {
        return java.util.Collections.unmodifiableList(mOnPushMesageListeners);
    }

    /**
     *  启动接收推送服务。
     * @param ctx
     */
    public static void actionStart(Context ctx) {
        initClient(ctx);
        Intent i = new Intent(ctx, MqttService.class);
        i.setAction(ACTION_START);
        ctx.startService(i);
    }

    /**
     * 关闭接收推送服务。
     * @param ctx
     */
    public static void actionStop(Context ctx) {
        initClient(ctx);
        Intent i = new Intent(ctx, MqttService.class);
        i.setAction(ACTION_STOP);
        ctx.startService(i);
    }

    /**
     * 初始化唯一标识。
     * @param context
     */
    private static void initClient(Context context){
        clientId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        ACTION_START = clientId + ".START";
        ACTION_STOP = clientId + ".STOP";
    }


}
