package app.android.mingjiang.com.mqttapi;

/**
 * 备注：返回回调信息。
 * 作者：wangzs on 2016/1/4 17:18
 * 邮箱：wangzhaosen@shmingjiang.org.cn
 */
public interface MqttSubscribeListener {
    //可以使用EventBus或者Handler来处理数据更新
    void onNewPushMessage(String message);
}