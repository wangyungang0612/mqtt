package app.android.mingjiang.com.mqttapi;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Mqtt服务类，提供MqttClient创建、关闭、连接、判断、订阅及发布。
 * 作者：wangzs on 2016/1/20 13:38
 * 邮箱：wangzhaosen@shmingjiang.org.cn
 */
public class MqttServer {

    private static final String TAG = "MqttServer";
    /**
     * 生产MqttClient对象。
     * @param context
     * @param baseURL
     * @return
     */
    public static MqttClient createClient(Context context,String baseURL){
        MqttClient mqttClient = null;
        try {
            //生成Client对象
           String Client_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            mqttClient = new MqttClient(baseURL, Client_id,new MemoryPersistence());

           return mqttClient;
        } catch (MqttException e) {
            e.printStackTrace();
            if(mqttClient != null){
                closeMqtt(mqttClient);//出错关闭
            }
            return null;
        }
    }

    /**
     * 连接Mqtt服务。
     */
    public static boolean connect(MqttClient mqttClient,String userName,String password) {
        return connect(mqttClient,userName,password,MqttConfig.TOPIC,MqttConfig.TIME_OUT,MqttConfig.ALIVE_INTERVAL,MqttConfig.CLEAN_SESSION);
    }

    /**
     * 连接Mqtt服务。
     */
    public static boolean connect(MqttClient mqttClient,String userName,String password,String topic,int timeout,int aliveInterval,boolean cleansession) {
        //mqtt连接设置
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(cleansession);
        options.setUserName(userName);
        options.setPassword(password.toCharArray());
        //设置超时时间
        options.setConnectionTimeout(timeout);
        //设置会话心跳时间
        options.setKeepAliveInterval(aliveInterval);
        try {
            mqttClient.connect(options);
            mqttClient.subscribe(topic, 1);
            return true;
        } catch (MqttException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 默认连接。
     * @param mqttClient
     * @return
     */
    public static boolean connect(MqttClient mqttClient){
        return connect(mqttClient,MqttConfig.USER_NAME,MqttConfig.PASS_WORD);
    }

    /**
     * 关闭连接。
     * @return
     */
    public static boolean closeMqtt(MqttClient mqttClient) {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * 是否连接成功。
     * @return
     */
    public static boolean isConnect(MqttClient mqttClient){

        return mqttClient.isConnected();
    }

    /**
     * 重新连接。
     */
    public static boolean reConnnect(MqttClient mqttClient){
        return connect(mqttClient);
    }

    /**
     * 重新连接。
     * @param userName
     * @param password
     * @return
     */
    public static boolean reConnect(MqttClient mqttClient,String userName,String password){
        return connect(mqttClient,userName, password);
    }

    /**
     * 发送消息。
     * @param publishMsg
     * @param publishTopic
     */
    public static void sendMessage(MqttClient mqttClient,String publishMsg,String publishTopic){
        MqttMessage message = new MqttMessage();
        message.setQos(1);
        message.setRetained(true);
        Log.d(TAG + "ratained状态", message.isRetained() + "");
        message.setPayload(publishMsg.getBytes());
        MqttDeliveryToken token = null;
        try {
            //生成主题对象
            MqttTopic topic = mqttClient.getTopic(publishTopic);
            token = topic.publish(message);
            token.waitForCompletion();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Log.d(TAG+"token state", token.isComplete() + "");
    }
    /**
     * 发送消息。
     * @param publishMsg
     */
    public static void sendMessage(MqttClient mqttClient,String publishMsg){
        sendMessage(mqttClient,publishMsg, MqttConfig.TOPIC);
    }
}
